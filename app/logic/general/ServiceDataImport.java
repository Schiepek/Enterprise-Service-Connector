package logic.general;

import logic.confluence.ConfluenceDataImport;
import logic.gmail.GMailDataImport;
import models.Alias;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.db.jpa.JPA;

import java.io.IOException;

/**
 * Created by Richard on 10.11.2014.
 */
public class ServiceDataImport {

    public void importData() throws IOException, InterruptedException, OAuthSystemException, OAuthProblemException {
        deleteData();
        new GMailDataImport().importGMail();
        new ConfluenceDataImport().importConfluence();
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
