package logic.confluence;

import com.google.gson.Gson;
import logic.general.AES128Encryptor;
import models.APIConfig;
import models.ServiceProvider;
import models.Settings;
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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ConfluenceAccess {

    private APIConfig config;
    private final String SOAP_GETALLUSERS = "getActiveUsers";
    private final String SOAP_GETUSER = "getUser";
    private final String SOAP_GETUSERGROUPS = "getUserGroups";
    private final String SOAP_GETALLGROUPS = "getGroups";
    private final String SOAP_GETSERVERINFO ="getServerInfo";
    private final int EXCEPTION_COUNT = 20;
    private final String CONFLUENCE_SOAP_URL = Settings.getSettings().getConfluenceUrl() + "/rpc/json-rpc/confluenceservice-v2";

    public ConfluenceAccess() {
        config = APIConfig.getAPIConfig(ServiceProvider.CONFLUENCE);
    }

    public Map<ConfluenceUser, String[]> getUserGroups() throws Exception {
        String[] params = { "true" };
        Gson gson = new Gson();
        String activeUsersJson = authenticatedSoapRequest(SOAP_GETALLUSERS, params);
        ConfluenceAllContainer alluserscontainer = gson.fromJson(activeUsersJson, ConfluenceAllContainer.class);
        int exceptionCounter = 0;
        Map<ConfluenceUser, String[]> userGroupMap = new HashMap<>();

        for (int i = 0; i < alluserscontainer.getAllResults().length; i++) {
            String username = alluserscontainer.getAllResults()[i];
            if(!username.contains("\uFFFD")) {
                String[] param = { username };
                try {
                    String userJson = authenticatedSoapRequest(SOAP_GETUSER, param);
                    ConfluenceUserContainer usercontainer = gson.fromJson(userJson, ConfluenceUserContainer.class);
                    String userGroupJson = authenticatedSoapRequest(SOAP_GETUSERGROUPS, param);
                    ConfluenceUserGroupsContainer userGroupContainer = gson.fromJson(userGroupJson, ConfluenceUserGroupsContainer.class);
                    String[] groupString = new String[userGroupContainer.getUserGroups().length];
                    for (int j = 0; j < userGroupContainer.getUserGroups().length ; j++ ) {
                        groupString[j] = userGroupContainer.getUserGroups()[j];
                    }
                    userGroupMap.put(usercontainer.getUser(), groupString);
                    exceptionCounter = 0;
                } catch (Exception e) {
                    if(exceptionCounter > EXCEPTION_COUNT) {
                        throw e;
                    }
                    i--;
                    exceptionCounter++;
                }
            }
        }
        return userGroupMap;
    }

    public String[] getGroups() throws Exception {
        String[] params = {  };
        Gson gson = new Gson();
        String groupsJson = authenticatedSoapRequest(SOAP_GETALLGROUPS, params);
        ConfluenceAllContainer allGroups = gson.fromJson(groupsJson, ConfluenceAllContainer.class);
        return allGroups.getAllResults();
    }

    public void checkStatus() throws Exception {
        String[] params = {  };
        String serverInfo = authenticatedSoapRequest(SOAP_GETSERVERINFO, params);
        if (!serverInfo.contains(Settings.getSettings().getConfluenceUrl())) {
            throw new Exception();
        }
    }

    private String authenticatedSoapRequest(String method, String[] params) throws Exception {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(CONFLUENCE_SOAP_URL);

        String userCredentials = config.getClientId() + ":" + new AES128Encryptor().decrypt(config.getClientSecret());
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
