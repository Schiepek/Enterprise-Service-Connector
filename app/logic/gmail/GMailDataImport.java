package logic.gmail;

import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.User;
import logic.salesforce.SalesForceAccess;
import models.*;
import models.Alias;
import models.gsonmodels.SalesforceContact;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import play.db.jpa.JPA;

import java.io.IOException;
import java.util.List;


public class GMailDataImport {

    private Directory service;
    List<SalesforceContact> sfContacts;

    public GMailDataImport() throws IOException {
        this(APIConfig.getAPIConfig(ServiceProvider.GMAIL));
    }

    public GMailDataImport(APIConfig config) throws IOException {
        service = new GMailConnector(config).getDirectoryService();
    }

    public void importGMail() throws OAuthProblemException, OAuthSystemException, IOException {
        deleteGMailData();
        importGMailGroups();
        importGMailUsers();
    }

    private void deleteGMailData() {
        deleteGMailGroups();
        deleteGMailAliases();
        deleteGmailUsers();
    }

    private void deleteGMailGroups() {
        List<models.Group> groups = models.Group.all();
        for (models.Group group : groups) {
            JPA.em().remove(group);
        }
    }

    private void deleteGMailAliases() {
        List<Alias> aliases = Alias.all();
        for (Alias alias : aliases) {
            JPA.em().remove(alias);
        }
    }

    private void deleteGmailUsers() {
        List<models.User> users = models.User.all();
        for (models.User user : users) {
            JPA.em().remove(user);
        }
    }


    private void importGMailGroups() throws IOException {
        GMailGroupAccess access = new GMailGroupAccess();
        List<Group> groups = access.getAllGroups();
        for (Group group : groups) {
            new models.Group(group, access.getGroupAliases(group.getId())).save();
        }

    }

    private void importGMailUsers() throws OAuthSystemException, OAuthProblemException, IOException {
        sfContacts = new SalesForceAccess().getSalesforceContactsList();
        GMailGroupAccess access = new GMailGroupAccess();
        List<User> users = access.getAllUsers();
        for (User user : users) {
            SalesforceContact contact = getSalesForceContact(user);
            if (contact != null) {
                models.User u = new models.User(contact, user.getId(), access.getMemberGroups(user.getId()), ServiceProvider.GMAIL);
            } else {
                models.User u = new models.User(user, access.getMemberGroups(user.getId()));
            }
        }
    }

    private SalesforceContact getSalesForceContact(User user) {
        for (SalesforceContact contact : sfContacts) {
            if (contact.getEmail() != null && user.getPrimaryEmail() != null && contact.getEmail().equals(user.getPrimaryEmail())) {
                return contact;
            }
        }
        return null;
    }


}
