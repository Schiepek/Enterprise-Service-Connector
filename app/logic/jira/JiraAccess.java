package logic.jira;

import com.google.gson.Gson;
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
import java.util.*;

public class JiraAccess {
    OAuthAccessor accessor;
    private final String JIRA_URL = Settings.getSettings().getJiraUrl();
    private final String ALL_GROUPS = "/rest/api/2/groups/picker?maxResults=10000";
    private final String SINGLE_GROUP = "/rest/api/2/group?groupname=";
    private final String SERVER_INFO = "/rest/api/2/serverInfo";


    public JiraAccess() {
        this.accessor = new JiraConnector().getAccessor();
    }

    public String authenticatedRestRequest(String url) throws IOException, URISyntaxException, OAuthException {
        OAuthClient client = new OAuthClient(new HttpClient4());
        accessor.accessToken = APIConfig.getAPIConfig(ServiceProvider.JIRA).getAccessToken();
        OAuthMessage response = client.invoke(accessor, url, Collections.<Map.Entry<?, ?>>emptySet());
        return response.readBodyAsString();
    }

    public Map<String, ArrayList<JiraUser>> getAllGroupsWithUsers() throws OAuthException, IOException, URISyntaxException {
        String getGroupsUrl = JIRA_URL + ALL_GROUPS;
        Gson gson = new Gson();
        Map<String, ArrayList<JiraUser>> allGroupsWithUsersMap = new HashMap<>();
        JiraGroupContainer groupcontainer = gson.fromJson(authenticatedRestRequest(getGroupsUrl), JiraGroupContainer.class);

        for(JiraGroup group : groupcontainer.getJiraGroups()) {
            String getUsersInGroupUrl = JIRA_URL + SINGLE_GROUP + group.getName() + "&expand=users";
            JiraUserContainer usercontainer = gson.fromJson(authenticatedRestRequest(getUsersInGroupUrl), JiraUserContainer.class);
            allGroupsWithUsersMap.put(group.getName(),new ArrayList<>(Arrays.asList(usercontainer.getUserCollection().getJiraUsers())));
        }
        return allGroupsWithUsersMap;
    }



    public void checkStatus() throws Exception {
        String serverInfo = authenticatedRestRequest(JIRA_URL + SERVER_INFO);
        if (!serverInfo.contains(Settings.getSettings().getJiraUrl())) {
            throw new Exception();
        }
    }


}
