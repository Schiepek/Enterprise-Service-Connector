package logic.salesforce;

import models.APIConfig;
import models.ServiceProvider;
import models.Settings;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

// CLIENT ID =  "3MVG9A_f29uWoVQtLyx_TNRfuq85aLHcIwEjVXgIOrrBWS4P5jZ6APwPmjjutvbNelxFEvodl7fpesAk9JV1l";
// redirectURI = "http://localhost:9000/account/callback";
//secret="5039637056870495392"
public class SalesForceConnector {

    String CALLBACK_URI;
    String CALLBACK_URI_PATH = "/salesforce/callback";

    public SalesForceConnector() {
        CALLBACK_URI = Settings.getSettings().getServerUrl() + CALLBACK_URI_PATH;
    }

    public String requestLocationURI() throws OAuthSystemException {
        APIConfig config = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE);
        OAuthClientRequest request = null;
        request = OAuthClientRequest
                .authorizationProvider(OAuthProviderType.SALESFORCE)
                .setClientId(config.getClientId())
                .setRedirectURI(CALLBACK_URI)
                .setResponseType("code")
                .buildQueryMessage();
        return request.getLocationUri();
    }

    public void setAccessToken(String code) throws OAuthSystemException, OAuthProblemException {
        APIConfig config = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE);
        OAuthClientRequest request = OAuthClientRequest
                .tokenProvider(OAuthProviderType.SALESFORCE)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(config.getClientId())
                .setClientSecret(config.getClientSecret())
                .setRedirectURI(CALLBACK_URI)
                .setCode(code)
                .buildQueryMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        SalesForceTokenResponse oAuthResponse = oAuthClient.accessToken(request, SalesForceTokenResponse.class);

        config.setAccessToken(oAuthResponse.getAccessToken());
        config.setRefreshToken(oAuthResponse.getRefreshToken());
        config.setInstance(oAuthResponse.getInstance());
        config.save();
    }

    public void setRefreshToken() throws OAuthSystemException, OAuthProblemException {
        APIConfig config = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE);

        OAuthClientRequest request = OAuthClientRequest
                .tokenProvider(OAuthProviderType.SALESFORCE)
                .setGrantType(GrantType.REFRESH_TOKEN)
                .setClientId(config.getClientId())
                .setClientSecret(config.getClientSecret())
                .setRefreshToken(config.getRefreshToken())
                .buildQueryMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        SalesForceTokenResponse oAuthResponse = oAuthClient.accessToken(request, SalesForceTokenResponse.class);

        config.setAccessToken(oAuthResponse.getAccessToken());
        config.setInstance(oAuthResponse.getInstance());
        config.save();
    }
}
