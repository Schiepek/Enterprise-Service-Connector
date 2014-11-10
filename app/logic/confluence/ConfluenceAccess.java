package logic.confluence;

import com.google.gson.Gson;
import models.APIConfig;
import models.ServiceProvider;
import models.gsonmodels.ConfluenceAllContainer;
import models.gsonmodels.ConfluenceUser;
import models.gsonmodels.ConfluenceUserContainer;
import models.gsonmodels.ConfluenceUserGroupsContainer;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfluenceAccess {

    private APIConfig config;

    public ConfluenceAccess() {
        config = APIConfig.getAPIConfig(ServiceProvider.CONFLUENCE);
    }

    public Map<ConfluenceUser, String[]> getUserGroups() throws IOException, InterruptedException {
        String[] params = { "true" };
        Gson gson = new Gson();
        String activeUsersJson = authenticatedSoapRequest("getActiveUsers", params);
        ConfluenceAllContainer alluserscontainer = gson.fromJson(activeUsersJson, ConfluenceAllContainer.class);
        int exceptionCounter = 0;
        Map<ConfluenceUser, String[]> userGroupMap = new HashMap<>();

        for (int i = 0; i < alluserscontainer.getAllResults().length/50; i++) { //TODO Elminiere /50 (nur zum Testen)
            String username = alluserscontainer.getAllResults()[i];
            if(!username.contains("\uFFFD")) {
                String[] param = { username };
                try {
                    String userJson = authenticatedSoapRequest("getUser", param);
                    ConfluenceUserContainer usercontainer = gson.fromJson(userJson, ConfluenceUserContainer.class);
                    String userGroupJson = authenticatedSoapRequest("getUserGroups", param);
                    ConfluenceUserGroupsContainer userGroupContainer = gson.fromJson(userGroupJson, ConfluenceUserGroupsContainer.class);
                    String[] groupString = new String[userGroupContainer.getUserGroups().length];
                    for (int j = 0; j < userGroupContainer.getUserGroups().length ; j++ ) {
                        groupString[j] = userGroupContainer.getUserGroups()[j];
                    }
                    userGroupMap.put(usercontainer.getUser(), groupString);
                    exceptionCounter = 0;
                    System.out.println(usercontainer.getUser().getName() + " "  + i);
                } catch (Exception e) {
                    if(exceptionCounter > 20) {
                        throw e;
                    }
                    i--;
                    exceptionCounter++;
                }
            }
        }
        return userGroupMap;
    }

    public String[] getGroups() throws IOException {
        String[] params = {  };
        Gson gson = new Gson();
        String groupsJson = authenticatedSoapRequest("getGroups", params);
        ConfluenceAllContainer allGroups = gson.fromJson(groupsJson, ConfluenceAllContainer.class);
        return allGroups.getAllResults();
    }


    private String authenticatedSoapRequest(String method, String[] params) throws IOException {
        String url = "https://foryouandyourteam.com/rpc/json-rpc/confluenceservice-v2";

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        String userCredentials = config.getClientId() + ":" + config.getClientSecret();
        String basicAuth = "Basic " + new String(Base64.encodeBase64(userCredentials.getBytes()));

        post.setHeader("Authorization", basicAuth);
        post.setHeader("Content-Type","application/json");

        String param = "{ \"jsonrpc\" : \"2.0\",\"method\" : \"" + method + "\",\"params\" : [ " + generateParams(params) + " ],\"id\" : 1 }";
        post.setEntity(new StringEntity(param));

        HttpResponse response = client.execute(post);
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

    private String generateParams(String[] params) {
        String jsonParameters = "";
        for (int i = 0; i < params.length ; i++) {
            jsonParameters += "\"" + params[i] + "\"";
            if( i < params.length-1) {
                jsonParameters += ",";
            }
        }
        return jsonParameters;
    }

}
