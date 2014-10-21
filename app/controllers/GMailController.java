package controllers;

import com.google.gdata.util.ServiceException;
import logic.gmail.GMailConnector;
import logic.gmail.GMailContactAccess;
import logic.salesforce.SalesForceAccess;
import logic.salesforce.SalesForceConnector;
import models.APIConfig;
import models.Container;
import models.Logging;
import models.ServiceProvider;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.data.Form;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.gmail.gmail;

import java.io.IOException;
import java.text.ParseException;

public class GMailController extends Controller {

    static Form<APIConfig> mailForm = Form.form(APIConfig.class);

    @Transactional
    public static Result index() {
        return ok(gmail.render(APIConfig.all(), mailForm));
    }

    @Transactional
    public static Result save(String provider) {
        Form<APIConfig> filledForm = mailForm.bindFromRequest();
        if (filledForm.hasErrors()) return badRequest(gmail.render(APIConfig.all(), filledForm));
        else {
            APIConfig newAccount = filledForm.get();
            APIConfig account = APIConfig.getAPIConfig(ServiceProvider.valueOf(provider));
            account.setClientId(newAccount.getClientId());
            account.setClientSecret(newAccount.getClientSecret());
            return ok(gmail.render(APIConfig.all(), mailForm));
        }
    }
    @Transactional
    public static Result authorize(String provider) throws OAuthSystemException {
        switch (provider) {
            case "SALESFORCE": return redirect(new SalesForceConnector().requestLocationURI());
            case "GMAIL": return redirect(new GMailConnector().authorize());
            default: return badRequest("Provider doesn't exist");
        }
    }

    @Transactional
    public static Result callback() throws IOException {
        new GMailConnector().generateRefreshToken(request().getQueryString("code"));
        return redirect(routes.GMailController.index());
    }

    @Transactional
    public static Result transferContacts() throws IOException, OAuthProblemException, OAuthSystemException, ServiceException, ParseException {
        Container container = null;
      //  try {
            container = new SalesForceAccess().getSalesforceContacts();
            new GMailContactAccess().transferContacts(container);
      //  } catch (Exception e) {
      //      throw new TransferException(e.getMessage());
     //   }
        Logging.log("Transfer successfull");
        return redirect(routes.GMailController.index());
    }

}
