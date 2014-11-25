package controllers;

import models.ServiceProvider;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.help.contactfolder;
import views.html.help.googledomain;
import views.html.help.serverurl;
import views.html.help.cron;
import views.html.help.provider;


public class HelpController extends Controller {

    public static Result serverurl() {
        return ok(serverurl.render());
    }

    public static Result googledomain() {
        return ok(googledomain.render());
    }

    public static Result contactfolder() {
        return ok(contactfolder.render());
    }

    public static Result cron() {
        return ok(cron.render());
    }

    public static Result provider(String providerstr) {
        return ok(provider.render(ServiceProvider.valueOf(providerstr)));
    }

}
