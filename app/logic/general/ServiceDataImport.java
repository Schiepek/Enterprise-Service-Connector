package logic.general;

import logic.confluence.ConfluenceDataImport;
import logic.gmail.GMailDataImport;
import logic.jira.JiraDataImport;
import models.Alias;
import net.oauth.OAuthException;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.db.jpa.JPA;

import java.io.IOException;
import java.net.URISyntaxException;

public class ServiceDataImport {

    public void importData() throws IOException, InterruptedException, OAuthSystemException, OAuthProblemException, OAuthException, URISyntaxException {
        deleteData();
        new GMailDataImport().importData();
        new ConfluenceDataImport().importData();
        new JiraDataImport().importData();
    }

    private void deleteData() {
        for (models.Group group : models.Group.all()) {
            JPA.em().remove(group);
        }
        for (models.User user : models.User.all()) {
            JPA.em().remove(user);
        }
        for (Alias alias : models.Alias.all()) {
            JPA.em().remove(alias);
        }
    }
}
