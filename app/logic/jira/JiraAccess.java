package logic.jira;

import com.google.gson.Gson;
import logic.confluence.ConfluenceAccess;
import models.APIConfig;
import models.ServiceProvider;
import models.Settings;
import models.gsonmodels.JiraGroup;
import models.gsonmodels.JiraGroupContainer;
import models.gsonmodels.JiraUser;
import models.gsonmodels.JiraUserContainer;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Richard on 04.11.2014.
 */
public class JiraAccess {
    OAuthAccessor accessor;
    String JiraUrl = Settings.getSettings().getJiraUrl();

    public JiraAccess(OAuthAccessor accessor) {
        this.accessor = accessor;
    }

    public String authenticatedRestRequest(String url) throws IOException, URISyntaxException, OAuthException {
        OAuthClient client = new OAuthClient(new HttpClient4());
        accessor.accessToken = APIConfig.getAPIConfig(ServiceProvider.JIRA).getAccessToken();
        OAuthMessage response = client.invoke(accessor, url, Collections.<Map.Entry<?, ?>>emptySet());
        return response.readBodyAsString();
    }


    public void sampleRequest() throws OAuthException, IOException, URISyntaxException, InterruptedException {
        //String url = "http://sinv-56031.edu.hsr.ch/jira/rest/api/2/issue/SA-14";
        //authenticatedRestRequest(url);
        //getAllGroups();
        new ConfluenceAccess().showAllGroups();
    }

    public void getAllGroups() throws OAuthException, IOException, URISyntaxException {
        String getGroupsUrl = JiraUrl + "/rest/api/2/groups/picker?maxResults=10000";
        Gson gson = new Gson();
        JiraGroupContainer groupcontainer = gson.fromJson(authenticatedRestRequest(getGroupsUrl), JiraGroupContainer.class);

        for(JiraGroup group : groupcontainer.getJiraGroups()) {
            String getUsersInGroupUrl = JiraUrl + "/rest/api/2/group?groupname=" + group.getName() + "&expand=users";
            JiraUserContainer usercontainer = gson.fromJson(authenticatedRestRequest(getUsersInGroupUrl), JiraUserContainer.class);
            System.out.println(group.getName());
            for (JiraUser user : usercontainer.getUserCollection().getJiraUsers()) {
                System.out.print(user.getName() + " / ");
            }
            System.out.println("---------");
        }
    }



    public void checkStatus() throws OAuthException, IOException, URISyntaxException {
        authenticatedRestRequest("http://sinv-56031.edu.hsr.ch/jira/rest/api/2/project");
    }


}
