package logic.jira;

import models.APIConfig;
import models.ServiceProvider;
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

    public JiraAccess(OAuthAccessor accessor) {
        this.accessor = accessor;
    }

    public void AuthenticatedRestRequest(String url) throws IOException, URISyntaxException, OAuthException {
        OAuthClient client = new OAuthClient(new HttpClient4());
        accessor.accessToken = APIConfig.getAPIConfig(ServiceProvider.JIRA).getAccessToken();
        OAuthMessage response = client.invoke(accessor, url, Collections.<Map.Entry<?, ?>>emptySet());
        System.out.println(response.getParameters());
    }


    public void sampleRequest() throws OAuthException, IOException, URISyntaxException {
        String url = "http://sinv-56031.edu.hsr.ch/jira/rest/api/2/issue/SA-14";
        AuthenticatedRestRequest(url);
    }

    public void checkStatus() throws OAuthException, IOException, URISyntaxException {
        AuthenticatedRestRequest("http://sinv-56031.edu.hsr.ch/jira/rest/api/2/project");
    }


}
