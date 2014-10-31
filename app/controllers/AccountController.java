package controllers;

import com.google.gdata.util.ServiceException;
import logic.gmail.GMailConnector;
import logic.gmail.GMailContactAccess;
import logic.salesforce.SalesForceAccess;
import logic.salesforce.SalesForceConnector;
import models.APIConfig;
import models.Logging;
import models.ServiceProvider;
import models.Settings;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.account;

import java.io.IOException;
import java.text.ParseException;

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
    public static Result setSettings() {
        Form<Settings> filledForm = settingsForm.bindFromRequest();
        if (filledForm.hasErrors()) return errorRequest();
        Settings.create(filledForm.get());
        return index();
    }

    @Transactional
    public static Result save(String provider) {
        Form<APIConfig> filledForm = apiForm.bindFromRequest();
        if (filledForm.hasErrors()) return errorRequest();
        APIConfig account = APIConfig.getAPIConfig(ServiceProvider.valueOf(provider));
        account.setClientId(filledForm.get().getClientId());
        account.setClientSecret(filledForm.get().getClientSecret());
        return index();
    }

    @Transactional
    public static Result authorize(String provider) throws OAuthSystemException {
        switch (ServiceProvider.valueOf(provider)) {
            case SALESFORCE:
                return redirect(new SalesForceConnector().requestLocationURI());
            case GMAIL:
                return redirect(new GMailConnector().authorize());
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
    public static Result callbackSalesForce() throws OAuthProblemException, OAuthSystemException {
        new SalesForceConnector().setAccessToken(request().getQueryString("code"));
        return index();
    }

    @Transactional
    public static Result transferContacts() throws
            IOException, OAuthProblemException, OAuthSystemException, ServiceException, ParseException {
//        try {
            new GMailContactAccess().transferContacts(new SalesForceAccess().getSalesforceContacts());
 //       } catch (Exception e) {
//            throw new TransferException(e.getMessage());
//        }
        Logging.log("Transfer successfull");
        return index();
    }

    @Transactional
    public static Result deleteContacts() throws IOException, ServiceException {
        new GMailContactAccess().deleteContacts();
        return index();
    }

    @Transactional
    public static Result checkStatus(String provider) {
        try {
            switch (ServiceProvider.valueOf(provider)) {
                case SALESFORCE:
                    new SalesForceConnector().setRefreshToken();
                case GMAIL:
                    new GMailConnector().getContactService();
            }

        } catch (Exception e) {
            return ok("<span class=\"aui-lozenge aui-lozenge-error\">NOK</span>");
        }
        return ok("<span class=\"aui-lozenge aui-lozenge-success\">OK</span>");
    }
}
