package logic.jira;

import logic.salesforce.SalesForceAccess;
import models.ServiceGroup;
import models.ServiceProvider;
import models.ServiceUser;
import models.gsonmodels.JiraUser;
import models.gsonmodels.SalesforceContact;
import net.oauth.OAuthException;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class JiraDataImport {

    private JiraAccess JiraAccess;
    private SalesForceAccess sfAccess;

    public JiraDataImport() {
        JiraAccess = new JiraAccess();
    }

    public void importData() throws OAuthException, IOException, URISyntaxException, OAuthProblemException, OAuthSystemException {
        importJiraGroupsWithUsers();
    }

    private void importJiraGroupsWithUsers() throws OAuthException, IOException, URISyntaxException, OAuthSystemException, OAuthProblemException {
        Iterator it = JiraAccess.getAllGroupsWithUsers().entrySet().iterator();
        sfAccess = new SalesForceAccess();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            ServiceGroup group = createGroup((String) pairs.getKey());
            for (JiraUser jiraUser : (ArrayList<JiraUser>) pairs.getValue()) {
                group.addMember(createOrFindUser(jiraUser));
            }
            group.save();
        }
    }

    private ServiceGroup createGroup(String groupname) {
        ServiceGroup group = new ServiceGroup();
        group.setName(groupname);
        group.setProvider(ServiceProvider.JIRA);
        return group;
    }

    private ServiceUser createOrFindUser(JiraUser jiraUser) throws OAuthProblemException, OAuthSystemException {
        ServiceUser user;
        user = ServiceUser.getUserByUsername(jiraUser.getName(), ServiceProvider.JIRA);
        if (user == null) {
            user = createUser(jiraUser);
        }
        return user;
    }

    private ServiceUser createUser(JiraUser jiraUser) throws OAuthSystemException, OAuthProblemException {
        SalesforceContact contact = sfAccess.getSalesForceContact(jiraUser.getEmailAddress());
        if (contact != null) {
            return new ServiceUser(contact, jiraUser.getName(), ServiceProvider.JIRA, new String[0]);
        } else {
            return new ServiceUser(jiraUser.getDisplayName(), jiraUser.getName(), jiraUser.getEmailAddress(), ServiceProvider.JIRA, new String[0]);
        }
    }

}
