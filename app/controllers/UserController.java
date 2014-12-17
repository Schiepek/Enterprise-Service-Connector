package controllers;


import global.Global;
import global.TransferException;
import logic.general.ServiceDataImport;
import logic.gmail.GMailContactAccess;
import logic.salesforce.SalesForceAccess;
import models.*;
import play.db.jpa.JPA;
import play.db.jpa.Transactional;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.companies;
import views.html.importData;
import views.html.services;
import views.html.users;

import java.util.Date;

public class UserController extends Controller {

    @Transactional
    public static Result importView() {
        Date lastImport = Settings.getSettings().getLastImport();
        return ok(importData.render(lastImport, models.Status.NOTHING));
    }

    @Transactional
    public static Result importView(models.Status status) {
        Date lastImport = Settings.getSettings().getLastImport();
        return ok(importData.render(lastImport, status));
    }

    @Transactional
    public static Result genericDataProcess(models.Status status) throws Exception {
        if (!Global.getStatus().contains(status)) {
            new Thread(() -> JPA.withTransaction(() -> {
                try {
                    Global.addStatus(status);
                    switch (status) {
                        case GROUPIMPORT:
                            JPA.withTransaction(() -> new ServiceDataImport().importData());
                            break;
                        case GMAILTRANSFER:
                            new GMailContactAccess().transferContacts(new SalesForceAccess().getSalesforceContacts());
                            break;
                        case GOOGLEPHONETRANSFER:
                            new GMailContactAccess(APIConfig.getAPIConfig(ServiceProvider.GOOGLEPHONE)).transferContacts(new SalesForceAccess().getSalesforceContacts());
                            break;
                    }
                    Global.removeStatus(status);
                } catch (Exception e) {
                    throw new TransferException(status, e.getMessage());
                }
            })).start();
        } else {
            return importView(status);
        }
        return importView();
    }

    @Transactional
    public static Result importData() throws Exception {
        return genericDataProcess(models.Status.GROUPIMPORT);
    }

    @Transactional
    public static Result transferContacts() throws Exception {
        return genericDataProcess(models.Status.GMAILTRANSFER);
    }

    @Transactional
    public static Result transferContactsToPhone() throws Exception {
        return genericDataProcess(models.Status.GOOGLEPHONETRANSFER);
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
