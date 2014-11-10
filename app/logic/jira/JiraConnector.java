package logic.jira;

import com.google.common.collect.ImmutableList;
import models.APIConfig;
import models.ServiceProvider;
import models.Settings;
import net.oauth.*;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import net.oauth.signature.RSA_SHA1;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

public class JiraConnector {

    private final String REQUEST_TOKEN_URL = Settings.getSettings().getJiraUrl() + "/plugins/servlet/oauth/request-token";
    private final String AUTHORIZATION_URL = Settings.getSettings().getJiraUrl() + "/plugins/servlet/oauth/authorize";
    private final String ACCESS_TOKEN_URL = Settings.getSettings().getJiraUrl() + "/plugins/servlet/oauth/access-token";
    private final String CONSUMER_KEY = APIConfig.getAPIConfig(ServiceProvider.JIRA).getClientId();
    private final String PRIVATE_KEY = APIConfig.getAPIConfig(ServiceProvider.JIRA).getClientSecret();
    private final String CALLBACK_URI = Settings.getSettings().getServerUrl() + "/jira/callback";
    private OAuthAccessor accessor;

    public JiraConnector() {
        OAuthServiceProvider serviceProvider = new OAuthServiceProvider(REQUEST_TOKEN_URL, AUTHORIZATION_URL, ACCESS_TOKEN_URL);
        OAuthConsumer consumer = new OAuthConsumer(CALLBACK_URI, CONSUMER_KEY, null, serviceProvider);
        consumer.setProperty(RSA_SHA1.PRIVATE_KEY, PRIVATE_KEY);
        consumer.setProperty(OAuth.OAUTH_SIGNATURE_METHOD, OAuth.RSA_SHA1);
        accessor = new OAuthAccessor(consumer);
    }

    public String authorize() throws OAuthException, IOException, URISyntaxException {
        OAuthClient oAuthClient = new OAuthClient(new HttpClient4());
        List<OAuth.Parameter> callBack = ImmutableList.of(new OAuth.Parameter(OAuth.OAUTH_CALLBACK, CALLBACK_URI));
        oAuthClient.getRequestTokenResponse(accessor, "POST", callBack);
        return AUTHORIZATION_URL + "?oauth_token=" + accessor.requestToken;
    }

    public void setAccessToken(String requestToken, String verifier) throws OAuthException, IOException, URISyntaxException {
        OAuthClient client = new OAuthClient(new HttpClient4());
        accessor.requestToken = requestToken;
        OAuthMessage message = client.getAccessToken(accessor, "POST",
                ImmutableList.of(new OAuth.Parameter(OAuth.OAUTH_VERIFIER, verifier)));
        APIConfig config = APIConfig.getAPIConfig(ServiceProvider.JIRA);
        config.setAccessToken(message.getToken());
        config.save();
    }

    public OAuthAccessor getAccessor() {
        return accessor;
    }

}
