package controllers;


import models.GMailConnector;
import play.mvc.Controller;
import play.mvc.Result;

public class GMailController extends Controller {

    static GMailConnector gmail = new GMailConnector();

    public static Result index() {
        return ok();
    }

    public static Result authorize() {
        gmail.authorize();
        return ok();
    }

    public static Result callback() {
        String code = request().getQueryString("code");
        return ok(views.html.gmail.render(code));
    }

    public static Result test() {
        gmail.test();
        return ok();
    }
}
