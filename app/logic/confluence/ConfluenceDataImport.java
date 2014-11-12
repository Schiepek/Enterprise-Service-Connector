package logic.confluence;

import models.Group;
import models.ServiceProvider;
import models.User;
import models.gsonmodels.ConfluenceUser;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by Richard on 10.11.2014.
 */
public class ConfluenceDataImport {
    ConfluenceAccess access;

    public ConfluenceDataImport() {
        access = new ConfluenceAccess();
    }

    public void importData() throws Exception {
        importConfluenceGroups();
        importConfluenceUserGroups();
    }

    private void importConfluenceGroups() throws Exception {
        for (String groupname : access.getGroups()) {
            Group group = new Group();
            group.setName(groupname);
            group.setProvider(ServiceProvider.CONFLUENCE);
            group.save();
        }
    }

    private void importConfluenceUserGroups() throws Exception {
        Iterator it = access.getUserGroups().entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            addGroupsToUser(createUser((ConfluenceUser) pairs.getKey()), (String[]) pairs.getValue());
            System.out.println(((ConfluenceUser) pairs.getKey()).getFullname());
        }
    }

    private User createUser(ConfluenceUser cUser) { //TODO Fullname = Firstname ISSUE
        User user = new User();
        user.setName(cUser.getName());
        user.setMail(cUser.getEmail());
        user.setFirstName(cUser.getFullname());
        user.setProvider(ServiceProvider.CONFLUENCE);
        return user;
    }

    private void addGroupsToUser(User user, String[] groups) {
        for (int i = 0; i < groups.length ; i++) {
            Group group = Group.getGroupByGroupname(groups[i], ServiceProvider.CONFLUENCE);
            group.addMember(user);
        }
    }
}
