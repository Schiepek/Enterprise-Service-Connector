package controllers;


import models.Logging;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.log;

public class LoggingController extends Controller {

    @Transactional
    public static Result index() {
        return ok(log.render(Logging.all()));
    }

}
