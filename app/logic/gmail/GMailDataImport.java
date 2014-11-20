package logic.gmail;

import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.model.Group;
import com.google.api.services.admin.directory.model.User;
import logic.salesforce.SalesForceAccess;
import models.APIConfig;
import models.ServiceGroup;
import models.ServiceProvider;
import models.ServiceUser;
import models.gsonmodels.SalesforceContact;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.io.IOException;
import java.util.List;


public class GMailDataImport {

    private Directory service;
    private SalesForceAccess sfAccess;

    public GMailDataImport() throws IOException {
        this(APIConfig.getAPIConfig(ServiceProvider.GMAIL));
    }

    public GMailDataImport(APIConfig config) throws IOException {
        service = new GMailConnector(config).getDirectoryService();
    }

    public void importData() throws OAuthProblemException, OAuthSystemException, IOException {
        importGMailGroups();
        importGMailUsers();
    }

    private void importGMailGroups() throws IOException {
        GMailGroupAccess access = new GMailGroupAccess();
        List<Group> groups = access.getAllGroups();
        for (Group group : groups) {
            new ServiceGroup(group, access.getGroupAliases(group.getId())).save();
        }
    }

    private void importGMailUsers() throws IOException, OAuthProblemException, OAuthSystemException {
        SalesForceAccess sfAccess = new SalesForceAccess();
        GMailGroupAccess groupAccess = new GMailGroupAccess();
        List<User> users = groupAccess.getAllUsers();
        for (User googleUser : users) {
           createUser(googleUser, sfAccess);
        }
    }

    private void createUser(User googleUser, SalesForceAccess sfAccess) throws IOException, OAuthSystemException, OAuthProblemException {
        GMailGroupAccess groupAccess = new GMailGroupAccess();
        ServiceUser user = ServiceUser.getUserByMail(googleUser.getPrimaryEmail());
        if (user != null) {
            user.setUsernameGoogle(googleUser.getPrimaryEmail());
            user.addToGroups(groupAccess.getMemberGroupNames(googleUser.getId()), ServiceProvider.GMAIL);
            user.save();
        } else {
            SalesforceContact contact = sfAccess.getSalesForceContact(googleUser.getPrimaryEmail());
            if (contact != null) {
                new ServiceUser(contact, googleUser.getPrimaryEmail(), ServiceProvider.GMAIL, groupAccess.getMemberGroupNames(googleUser.getId()));
            } else {
                new ServiceUser(googleUser.getName().getFullName(), googleUser.getPrimaryEmail(), googleUser.getPrimaryEmail(), ServiceProvider.GMAIL, groupAccess.getMemberGroupNames(googleUser.getId()));
            }
        }
    }

}
