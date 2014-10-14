package controllers;

import models.APIConfig;
import models.Container;
import logic.salesforce.SalesForceAccess;
import logic.salesforce.SalesForceConnector;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.salesforce.contacts;
import views.html.salesforce.oauth;

import java.util.Arrays;

public class SalesForceController extends Controller {

    static Form<APIConfig> configForm = Form.form(APIConfig.class);

    public static Result index() {
        return ok(oauth.render(configForm));
    }

    public static Result oauth2() throws OAuthSystemException {
        Form<APIConfig> filledForm = configForm.bindFromRequest();
        APIConfig config;
        if (filledForm.hasErrors()) return badRequest(views.html.index.render());
        else config = new SalesForceConnector().safeConfig(filledForm.get());
        return redirect(new SalesForceConnector().requestLocationURI(config));
    }

    public static Result callback() throws OAuthProblemException, OAuthSystemException {
        new SalesForceConnector().setAccessToken(request().getQueryString("code"));
        return ok(views.html.index.render());
    }

    public static Result getSalesforceContacts() throws OAuthSystemException, OAuthProblemException {
        new SalesForceConnector().setRefreshToken();
        Container container = new SalesForceAccess().getSalesforceContacts();
        return ok( contacts.render(Arrays.asList(container.getContacts())) );
    }
}
