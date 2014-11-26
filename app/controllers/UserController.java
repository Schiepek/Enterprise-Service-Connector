package controllers;


import com.google.gdata.util.ServiceException;
import global.TransferException;
import logic.general.ServiceDataImport;
import logic.gmail.GMailContactAccess;
import logic.salesforce.SalesForceAccess;
import models.APIConfig;
import models.ServiceProvider;
import models.ServiceUser;
import models.Settings;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.companies;
import views.html.importData;
import views.html.services;
import views.html.users;

import java.io.IOException;
import java.text.ParseException;
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
    public static Result transferContacts() throws
            IOException, OAuthProblemException, OAuthSystemException, ServiceException, ParseException {
        try {
            new GMailContactAccess().transferContacts(new SalesForceAccess().getSalesforceContacts());
        } catch (Exception e) {
            throw new TransferException(e.getMessage());
        }
        return importView();
    }

    @Transactional
    public static Result transferContactsToPhone() throws
            IOException, OAuthProblemException, OAuthSystemException, ServiceException, ParseException {
        try {
            new GMailContactAccess(APIConfig.getAPIConfig(ServiceProvider.GOOGLEPHONE)).transferContacts(new SalesForceAccess().getSalesforceContacts());
        } catch (Exception e) {
            throw new TransferException(e.getMessage());
        }
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
        return ok(companies.render(ServiceUser.all(), lastImport));
    }

    @Transactional
    public static Result services() {
        Date lastImport = Settings.getSettings().getLastImport();
        return ok(services.render(ServiceProvider.values(), lastImport));
    }

}
