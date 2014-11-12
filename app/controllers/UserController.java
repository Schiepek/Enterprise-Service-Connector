package controllers;


import logic.general.ServiceDataImport;
import models.Logging;
import models.ServiceUser;
import models.Settings;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.companies;
import views.html.importData;
import views.html.users;

import java.util.Date;

public class UserController extends Controller {

    @Transactional
    public static Result importView() {
        Date lastImport = Settings.getSettings().getLastImport();
        return ok(importData.render(lastImport));
    }

    @Transactional
    public static Result importData() throws Exception {
        new ServiceDataImport().importData();
        return importView();
    }

    @Transactional
    public static Result users() {
        Date lastImport = Settings.getSettings().getLastImport();
        return ok(users.render(ServiceUser.all(), lastImport));
    }

    @Transactional
    public static Result companies() {
        Date lastImport = Settings.getSettings().getLastImport();
        return ok(companies.render(ServiceUser.allCompanies(), lastImport));
    }

}
