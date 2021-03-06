package logic.general;

import logic.confluence.ConfluenceDataImport;
import logic.gmail.GMailDataImport;
import logic.jira.JiraDataImport;
import models.*;
import play.db.jpa.JPA;

import java.util.Date;

public class ServiceDataImport {

    public void importData() throws Exception {
        Settings settings = Settings.getSettings();
        deleteData();
        if(settings.getSaveInDirectory()) {
            new GMailDataImport().importData();
        }
        new ConfluenceDataImport().importData();
        new JiraDataImport().importData();
        settings.setLastImport(new Date());
        Logging.log("Group Data Import successfull");
    }

    private void deleteData() {
        for (ServiceGroup group : ServiceGroup.all()) {
            JPA.em().remove(group);
        }
        for (ServiceUser user : ServiceUser.all()) {
            JPA.em().remove(user);
        }
        for (ServiceAlias alias : ServiceAlias.all()) {
            JPA.em().remove(alias);
        }
    }
}
