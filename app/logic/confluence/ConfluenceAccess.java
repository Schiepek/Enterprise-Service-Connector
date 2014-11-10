package logic.confluence;

import com.google.gson.Gson;
import models.APIConfig;
import models.ServiceProvider;
import models.gsonmodels.ConfluenceAllUsersContainer;
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

public class ConfluenceAccess {

    private APIConfig config;

    public ConfluenceAccess() {
        config = APIConfig.getAPIConfig(ServiceProvider.CONFLUENCE);
    }

    public void showAllGroups() throws IOException, InterruptedException {
        String[] params = { "true" };
        Gson gson = new Gson();
        String activeUsersJson = authenticatedSoapRequest("getActiveUsers", params);
        ConfluenceAllUsersContainer alluserscontainer = gson.fromJson(activeUsersJson, ConfluenceAllUsersContainer.class);
        int exceptionCounter = 0;

        for (int i = 0; i < alluserscontainer.getAllActiveUsers().length; i++) {
            String username = alluserscontainer.getAllActiveUsers()[i];
            if(!username.contains("\uFFFD")) {
                String[] param = { username };
                try {
                    String userJson = authenticatedSoapRequest("getUser", param);
                    ConfluenceUserContainer usercontainer = gson.fromJson(userJson, ConfluenceUserContainer.class);
                    String userGroupJson = authenticatedSoapRequest("getUserGroups", param);
                    ConfluenceUserGroupsContainer userGroupContainer = gson.fromJson(userGroupJson, ConfluenceUserGroupsContainer.class);
                    System.out.print(username + ": ");
                    for (String group : userGroupContainer.getUserGroups()) {
                        System.out.print(group + " / ");
                    }
                    System.out.println();
                    exceptionCounter = 0;
                } catch (Exception e) {
                    if(exceptionCounter > 20) {
                        throw e;
                    }
                    i--;
                    exceptionCounter++;
                    System.out.println("EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
                }
            }
        }
/*        Gson gson = new Gson();
        String[] params = {  "j\u00E9rome.moine" };
        String jerome = authenticatedSoapRequest("getUser", params);
        CharSequence seq = "\uFFFD";
        if(jerome.contains(seq)) System.out.println("xx");
        ConfluenceUserContainer container = gson.fromJson(jerome, ConfluenceUserContainer.class);
        System.out.println(container.getUser().getName());*/

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
