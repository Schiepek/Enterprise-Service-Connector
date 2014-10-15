package controllers;

import global.TransferException;
import models.APIConfig;
import models.Logging;
import models.ServiceProvider;
import logic.gmail.GMailConnector;
import logic.gmail.GMailContactAccess;
import models.Container;
import logic.salesforce.SalesForceAccess;
import play.data.Form;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.gmail.gmail;

import java.io.IOException;
import java.util.Date;

public class GMailController extends Controller {

    static Form<APIConfig> mailForm = Form.form(APIConfig.class);

    @Transactional
    public static Result index() {
        return ok(gmail.render(APIConfig.all(), mailForm));
    }

    @Transactional
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

    @Transactional
    public static Result deleteMail(Long id) {
        APIConfig.delete(id);
        return ok(gmail.render(APIConfig.all(), mailForm));
    }

    @Transactional
    public static Result authorize(Long id) {
        GMailConnector gmail = new GMailConnector(id);
        return redirect(gmail.authorize());
    }

    @Transactional
    public static Result callback() throws IOException {
        GMailConnector gmail = new GMailConnector(Long.parseLong(request().getQueryString("state")));
        gmail.generateRefreshToken(request().getQueryString("code"));
        return redirect(routes.GMailController.index());
    }

    @Transactional
    public static Result insertContact(Long id) throws Exception {
        GMailContactAccess access = new GMailContactAccess(id);
        //access.insertContact("77777", "77777", "lulumomo");
        return redirect(routes.GMailController.index());
    }

    @Transactional
    public static Result transferContacts(Long id) {
        Container container = null;
        try {
            container = new SalesForceAccess().getSalesforceContacts();
            new GMailContactAccess(id).insertContacts(container);
        } catch (Exception e) {
            throw new TransferException(e.getMessage());
        }
/*        for (Contact c : container.getContacts()) {
            System.out.println(c.getEmail() + "  " + c.getFirstName() + "  " + c.getLastName());
        }*/
            Logging log = new Logging();
            log.setDate(new Date());
            log.setMessage("Transfer successful");
            log.save();
        return redirect(routes.GMailController.index());
    }

}
