package logic.confluence;

import models.ServiceGroup;
import models.ServiceProvider;
import models.ServiceUser;
import models.gsonmodels.ConfluenceUser;

import java.util.Iterator;
import java.util.Map;

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
            ServiceGroup group = new ServiceGroup();
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

    private ServiceUser createUser(ConfluenceUser cUser) { //TODO Fullname = Firstname ISSUE
        ServiceUser user = new ServiceUser();
        user.setName(cUser.getName());
        user.setMail(cUser.getEmail());
        user.setFirstName(cUser.getFullname());
        user.setProvider(ServiceProvider.CONFLUENCE);
        return user;
    }

    private void addGroupsToUser(ServiceUser user, String[] groups) {
        for (int i = 0; i < groups.length ; i++) {
            ServiceGroup group = ServiceGroup.getGroupByGroupname(groups[i], ServiceProvider.CONFLUENCE);
            group.addMember(user);
        }
    }
}
