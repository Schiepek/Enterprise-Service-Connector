package controllers;


import models.GMailAccount;
import models.GMailConnector;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;

public class GMailController extends Controller {

    static Form<GMailAccount> mailForm = Form.form(GMailAccount.class);



    public static Result index() {
        return ok();
    }

    public static Result mails() {
        return ok(
                views.html.gmail.render(GMailAccount.all(), mailForm)
        );
    }

    public static Result newMail() {
        Form<GMailAccount> filledForm = mailForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(
                    views.html.gmail.render(GMailAccount.all(), filledForm)
            );
        } else {
            GMailAccount.create(filledForm.get());
            return ok(
                    views.html.gmail.render(GMailAccount.all(), mailForm)
            );
        }
    }

    public static Result deleteMail(Long id) {
        GMailAccount.delete(id);
        return ok(
                views.html.gmail.render(GMailAccount.all(), mailForm)
        );
    }

    public static Result authorize(Long id) {
        GMailConnector gmail = new GMailConnector(id);
        String authUrl = gmail.authorize();
        redirect(authUrl);
        return ok(
                views.html.gmail.render(GMailAccount.all(), mailForm)
        );
    }

    public static Result callback() {
        Long id = Long.valueOf(request().getQueryString("state"));
        GMailConnector gmail = new GMailConnector(id);
        String code = request().getQueryString("code");
        gmail.generateAccessToken(code);
        return ok();
    }

    public static Result service(Long id)  {
        GMailConnector gmail = new GMailConnector(id);
        gmail.getGMailService();
        return ok();
    }
}
