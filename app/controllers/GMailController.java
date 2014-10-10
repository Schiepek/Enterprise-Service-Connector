package controllers;

import com.google.gdata.util.ServiceException;
import models.APIConfig;
import models.ServiceProvider;
import models.gmail.GMailConnector;
import models.gmail.GMailContactAccess;
import models.salesforce.Container;
import models.salesforce.SalesForceAccess;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.gmail.gmail;

import java.io.IOException;

public class GMailController extends Controller {

    static Form<APIConfig> mailForm = Form.form(APIConfig.class);

    public static Result index() {
        return ok(gmail.render(APIConfig.all(), mailForm));
    }

    public static Result newMail() {
        Form<APIConfig> filledForm = mailForm.bindFromRequest();
        if (filledForm.hasErrors()) return badRequest(gmail.render(APIConfig.all(), filledForm));
        else {
            APIConfig account = filledForm.get();
            account.setProvider(ServiceProvider.GMAIL);
            APIConfig.create(account);
            return ok(gmail.render(APIConfig.all(), mailForm));
        }
    }

    public static Result deleteMail(Long id) {
        APIConfig.delete(id);
        return ok(gmail.render(APIConfig.all(), mailForm));
    }

    public static Result authorize(Long id) {
        GMailConnector gmail = new GMailConnector(id);
        return redirect(gmail.authorize());
    }

    public static Result callback() throws IOException {
        GMailConnector gmail = new GMailConnector(Long.parseLong(request().getQueryString("state")));
        gmail.generateRefreshToken(request().getQueryString("code"));
        return redirect(routes.GMailController.index());
    }

    public static Result insertContact(Long id) throws Exception {
        GMailContactAccess access = new GMailContactAccess(id);
        //access.insertContact("77777", "77777", "lulumomo");
        return redirect(routes.GMailController.index());
    }

    public static Result transferContacts(Long id) throws IOException, ServiceException, OAuthProblemException, OAuthSystemException {
        Container container = new SalesForceAccess().getSalesforceContacts();
/*        for (Contact c : container.getContacts()) {
            System.out.println(c.getEmail() + "  " + c.getFirstName() + "  " + c.getLastName());
        }*/
        new GMailContactAccess(id).insertContacts(container);
        return redirect(routes.GMailController.index());
    }
}
