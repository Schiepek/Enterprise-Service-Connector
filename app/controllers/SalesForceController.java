package controllers;

import com.google.gson.Gson;
import models.APIConfig;
import models.salesForce.SalesForceTokenResponse;
import models.salesForce.Container;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.salesForce.oauth;
import views.html.salesForce.contacts;
import java.util.Arrays;

public class SalesForceController extends Controller {

    static Form<APIConfig> configForm = Form.form(APIConfig.class);

    public static Result index() {
        return ok(oauth.render(configForm));
    }

    public static Result oauth2() {
        Form<APIConfig> filledForm = configForm.bindFromRequest();
        String clientID, redirectURI;
        if(filledForm.hasErrors()) {
            return badRequest(
                    views.html.index.render("Fehler Form")
            );
        } else {
            APIConfig config;
            //APIConfig config = APIConfig.getConfig(1L);
            if (APIConfig.getConfig(1L) == null) {
                config = filledForm.get();
                config.save();
            } else {
                config = APIConfig.getConfig(1L);
                config.setClientID(filledForm.get().getClientID());
                config.setClientSecret(filledForm.get().getClientSecret());
                config.setRedirectURI(filledForm.get().getRedirectURI());
                config.save();
                //APIConfig config = APIConfig.getConfig(1L);
            }

            clientID = config.getClientID(); // "3MVG9A_f29uWoVQtLyx_TNRfuq85aLHcIwEjVXgIOrrBWS4P5jZ6APwPmjjutvbNelxFEvodl7fpesAk9JV1l";
            redirectURI = config.getRedirectURI(); //"http://localhost:9000/salesforce/oauth2/callback";
            //secret="5039637056870495392"
        }
        OAuthClientRequest request = null;
        try {
            request = OAuthClientRequest
                    .authorizationProvider(OAuthProviderType.SALESFORCE)
                    .setClientId(clientID)
                    .setRedirectURI(redirectURI)
                    .setResponseType("code")
                    .buildQueryMessage();
        } catch (OAuthSystemException e) {
            e.printStackTrace();
        }

        return redirect(request.getLocationUri());
    }

    public static Result callback() {
        String code = request().getQueryString("code");
        APIConfig config = APIConfig.getConfig(1L);
        try {
            OAuthClientRequest request = OAuthClientRequest
                    .tokenProvider(OAuthProviderType.SALESFORCE)
                    .setGrantType(GrantType.AUTHORIZATION_CODE)
                    .setClientId(config.getClientID())
                    .setClientSecret(config.getClientSecret())
                    .setRedirectURI(config.getRedirectURI())
                    .setCode(code)
                    .buildQueryMessage();

            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            SalesForceTokenResponse oAuthResponse = oAuthClient.accessToken(request, SalesForceTokenResponse.class);

            String accessToken = oAuthResponse.getAccessToken();
            String instance = oAuthResponse.getInstance();

            config.setAccessToken(accessToken);
            config.setInstance(instance);
            config.save();

            return ok(
                    views.html.index.render("Success")
            );
        } catch (OAuthProblemException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return badRequest(
                views.html.index.render("Fehler")
        );
    }

    public static Result getSalesforceContacts() throws OAuthSystemException, OAuthProblemException {
        APIConfig config = APIConfig.getConfig(1L);

        String instance = config.getInstance();
        String accessToken = config.getAccessToken();

        OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(instance + "/services/data/v20.0/query/?q=SELECT+LastName,Firstname,Email+FROM+Contact")
                .setAccessToken(accessToken).buildHeaderMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

        OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);

        Gson gson = new Gson();
        Container container = gson.fromJson(resourceResponse.getBody(), Container.class);

        return ok(
                contacts.render(Arrays.asList(container.getContacts()))
        );
    }
}
