package logic.jira;

import models.ServiceGroup;
import models.ServiceProvider;
import models.ServiceUser;
import models.gsonmodels.JiraUser;
import net.oauth.OAuthException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class JiraDataImport {

    JiraAccess access;

    public JiraDataImport() {
        access = new JiraAccess();
    }

    public void importData() throws OAuthException, IOException, URISyntaxException {
        importJiraGroupsWithUsers();
    }

    private void importJiraGroupsWithUsers() throws OAuthException, IOException, URISyntaxException {
        Iterator it = access.getAllGroupsWithUsers().entrySet().iterator();
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

    private ServiceUser createOrFindUser(JiraUser jiraUser) {
        ServiceUser user;
        user = ServiceUser.getUserByUsername(jiraUser.getName(), ServiceProvider.JIRA);
        if (user == null) {
            user = createNewUser(jiraUser);
        }
        return user;
    }

    private ServiceUser createNewUser(JiraUser jiraUser) { //TODO Firstname Fullname ISSUE
        ServiceUser user = new ServiceUser();
        user.setName(jiraUser.getName());
        user.setMail(jiraUser.getEmailAddress());
        user.setFirstName(jiraUser.getDisplayName());
        user.setProvider(ServiceProvider.JIRA);
        return user;
    }

}
