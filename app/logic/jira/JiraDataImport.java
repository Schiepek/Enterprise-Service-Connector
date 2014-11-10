package logic.jira;

import models.Group;
import models.ServiceProvider;
import models.User;
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
            Group group = createGroup((String) pairs.getKey());
            for (JiraUser jiraUser : (ArrayList<JiraUser>) pairs.getValue()) {
                group.addMember(createOrFindUser(jiraUser));
            }
            group.save();
        }
    }

    private Group createGroup(String groupname) {
        Group group = new Group();
        group.setName(groupname);
        group.setProvider(ServiceProvider.JIRA);
        return group;
    }

    private User createOrFindUser(JiraUser jiraUser) {
        User user;
        user = User.getUserByUsername(jiraUser.getName(), ServiceProvider.JIRA);
        if (user == null) {
            user = createNewUser(jiraUser);
        }
        return user;
    }

    private User createNewUser(JiraUser jiraUser) { //TODO Firstname Fullname ISSUE
        User user = new User();
        user.setName(jiraUser.getName());
        user.setMail(jiraUser.getEmailAddress());
        user.setFirstName(jiraUser.getDisplayName());
        user.setProvider(ServiceProvider.JIRA);
        return user;
    }

}
