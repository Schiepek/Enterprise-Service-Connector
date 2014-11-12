package logic.general;

import logic.confluence.ConfluenceDataImport;
import logic.gmail.GMailDataImport;
import logic.jira.JiraDataImport;
import models.ServiceAlias;
import models.ServiceGroup;
import models.ServiceUser;
import play.db.jpa.JPA;

public class ServiceDataImport {

    public void importData() throws Exception {
        deleteData();
        new GMailDataImport().importData();
        new ConfluenceDataImport().importData();
        new JiraDataImport().importData();
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
