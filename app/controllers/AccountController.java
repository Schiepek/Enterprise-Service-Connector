package controllers;

import com.google.gdata.util.ServiceException;
import global.Global;
import logic.confluence.ConfluenceAccess;
import logic.confluence.ConfluenceConnector;
import logic.general.AES128Encryptor;
import logic.gmail.GMailConnector;
import logic.gmail.GMailContactAccess;
import logic.jira.JiraAccess;
import logic.jira.JiraConnector;
import logic.salesforce.SalesForceConnector;
import models.APIConfig;
import models.Logging;
import models.ServiceProvider;
import models.Settings;
import net.oauth.OAuthException;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account;

import java.io.IOException;
import java.net.URISyntaxException;

public class AccountController extends Controller {

    static Form<APIConfig> apiForm = Form.form(APIConfig.class);
    static Form<Settings> settingsForm = Form.form(Settings.class);

    @Transactional
    public static Result index() {
        return ok(account.render(APIConfig.all(), apiForm, Settings.getSettings(), settingsForm));
    }

    @Transactional
    public static Result errorRequest() {
        return badRequest(account.render(APIConfig.all(), apiForm, Settings.getSettings(), settingsForm));
    }

    @Transactional
    public static Result setSettings() throws Throwable {
        Form<Settings> filledForm = settingsForm.bindFromRequest();
        if (filledForm.hasErrors()) return errorRequest();
        String oldCronExpression = Settings.getSettings().getCronExpression();
        String newCronExpression = filledForm.get().getCronExpression();
        Settings.create(filledForm.get());
        if(!oldCronExpression.equals(newCronExpression)) {
            Global.setNewScheduler(newCronExpression);
        }
        return index();
    }

    @Transactional
    public static Result save(String provider) throws Exception {
        Form<APIConfig> filledForm = apiForm.bindFromRequest();
        if (filledForm.hasErrors()) return errorRequest();
        APIConfig account = APIConfig.getAPIConfig(ServiceProvider.valueOf(provider));
        account.setClientId(filledForm.get().getClientId());
        if(provider.equals("CONFLUENCE")) {
            account.setClientSecret(new AES128Encryptor().encrypt(filledForm.get().getClientSecret()));
        } else {
            account.setClientSecret(filledForm.get().getClientSecret());
        }
        return index();
    }

    @Transactional
    public static Result authorize(String provider) throws OAuthSystemException, OAuthException, IOException, URISyntaxException {
        switch (ServiceProvider.valueOf(provider)) {
            case SALESFORCE:
                return redirect(new SalesForceConnector().requestLocationURI());
            case GMAIL:
                return redirect(new GMailConnector().authorize());
            case GOOGLEPHONE:
                return redirect(new GMailConnector(APIConfig.getAPIConfig(ServiceProvider.GOOGLEPHONE)).authorize());
            case JIRA:
                return redirect(new JiraConnector().authorize());
            case CONFLUENCE:
                return redirect(new ConfluenceConnector().authorize());
            default:
                return badRequest("Provider doesn't exist");
        }
    }

    @Transactional
    public static Result callbackGmail() throws IOException {
        new GMailConnector().generateRefreshToken(request().getQueryString("code"));
        return index();
    }

    @Transactional
    public static Result callbackGooglePhone() throws IOException {
        new GMailConnector(APIConfig.getAPIConfig(ServiceProvider.GOOGLEPHONE)).generateRefreshToken(request().getQueryString("code"));
        return index();
    }


    @Transactional
    public static Result callbackSalesForce() throws OAuthProblemException, OAuthSystemException {
        new SalesForceConnector().setAccessToken(request().getQueryString("code"));
        return index();
    }

    @Transactional
    public static Result callbackJira() throws OAuthProblemException, OAuthSystemException, OAuthException, IOException, URISyntaxException {
        new JiraConnector().setAccessToken(request().getQueryString("oauth_token"), request().getQueryString("oauth_verifier"));
        return index();
    }

    @Transactional
    public static Result callbackConfluence() throws OAuthProblemException, OAuthSystemException, OAuthException, IOException, URISyntaxException {
        new ConfluenceConnector().setAccessToken(request().getQueryString("oauth_token"), request().getQueryString("oauth_verifier"));
        return index();
    }

    @Transactional
    public static Result deleteContacts() throws IOException, ServiceException {
        new Thread(() ->  JPA.withTransaction(() -> new GMailContactAccess().deleteContacts())).start();
        return index();
    }

    @Transactional
    public static Result deletePhoneContacts() throws IOException, ServiceException {
        new Thread(() ->  JPA.withTransaction(() -> new GMailContactAccess(APIConfig.getAPIConfig(ServiceProvider.GOOGLEPHONE)).
                deleteContacts())).start();
        return index();
    }

    @Transactional
    public static Result checkStatus(String provider) {
        try {
            switch (ServiceProvider.valueOf(provider)) {
                case SALESFORCE:
                    new SalesForceConnector().setRefreshToken();
                    break;
                case GMAIL:
                    new GMailConnector().getDirectoryService();
                    new GMailConnector().getContactService();
                    break;
                case GOOGLEPHONE:
                    new GMailConnector(APIConfig.getAPIConfig(ServiceProvider.GOOGLEPHONE)).getDirectoryService();
                    new GMailConnector(APIConfig.getAPIConfig(ServiceProvider.GOOGLEPHONE)).getContactService();
                    break;
                case JIRA:
                    new JiraAccess().checkStatus();
                    break;
                case CONFLUENCE:
                    new ConfluenceAccess().checkStatus();
                    break;
            }

        } catch (Exception e) {
            Logging.log("Authorization Error: " + e.getMessage());
            return ok("<span class=\"aui-lozenge aui-lozenge-error\">NOK</span>");
        }
        return ok("<span class=\"aui-lozenge aui-lozenge-success\">OK</span>");
    }

}
