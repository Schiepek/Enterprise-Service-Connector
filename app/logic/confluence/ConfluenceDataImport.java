package logic.confluence;

import logic.salesforce.SalesForceAccess;
import models.ServiceGroup;
import models.ServiceProvider;
import models.ServiceUser;
import models.gsonmodels.ConfluenceUser;
import models.gsonmodels.SalesforceContact;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.util.Iterator;
import java.util.Map;

public class ConfluenceDataImport {
    private ConfluenceAccess confluenceAccess;
    private SalesForceAccess sfAccess;

    public ConfluenceDataImport() {
        confluenceAccess = new ConfluenceAccess();
    }

    public void importData() throws Exception {
        importConfluenceGroups();
        importConfluenceUserGroups();
    }

    private void importConfluenceGroups() throws Exception {
        for (String groupname : confluenceAccess.getGroups()) {
            ServiceGroup group = new ServiceGroup();
            group.setName(groupname);
            group.setProvider(ServiceProvider.CONFLUENCE);
            group.save();
        }
    }

    private void importConfluenceUserGroups() throws Exception {
        Iterator it = confluenceAccess.getUserGroups().entrySet().iterator();
        sfAccess = new SalesForceAccess();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            createUser((ConfluenceUser) pairs.getKey(), (String[]) pairs.getValue());
        }
    }

    private void createUser(ConfluenceUser cUser, String[] groups) throws OAuthSystemException, OAuthProblemException {
        ServiceUser user = ServiceUser.getUserByMail(cUser.getEmail());
        if (user != null) {
            user.setUsernameConfluence(cUser.getName());
            user.addToGroups(groups, ServiceProvider.CONFLUENCE);
            user.save();
        } else {
            SalesforceContact contact = sfAccess.getSalesForceContact(cUser.getEmail());
            if (contact != null) {
                new ServiceUser(contact, cUser.getName(), ServiceProvider.CONFLUENCE, groups);
            } else {
                new ServiceUser(cUser.getFullname(), cUser.getName(), cUser.getEmail(), ServiceProvider.CONFLUENCE, groups);
            }
        }
    }
}
