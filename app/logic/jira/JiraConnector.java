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

/**
 * Created by Richard on 04.11.2014.
 */
public class JiraConnector {

    private static final String REQUEST_TOKEN_URL = "http://sinv-56031.edu.hsr.ch/jira" + "/plugins/servlet/oauth/request-token";
    private static final String AUTHORIZATION_URL = "http://sinv-56031.edu.hsr.ch/jira" + "/plugins/servlet/oauth/authorize";
    private static final String ACCESS_TOKEN_URL = "http://sinv-56031.edu.hsr.ch/jira" + "/plugins/servlet/oauth/access-token";
    private static final String OAUTH_SINING_TYPE = "RSA-SHA1";
    private static final String CONSUMER_KEY = "hardcoded-consumer";
    private static final String PRIVATE_KEY = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDFkPMZQaTqsSXI+bSI65rSVaDzic6WFA3WCZMVMi7lYXJAUdkXo4DgdfvEBO21Bno3bXIoxqS411G8S53I39yhSp7z2vcB76uQQifi0LEaklZfbTnFUXcKCyfwgKPp0tQVA+JZei6hnscbSw8qEItdc69ReZ6SK+3LHhvFUUP1nLhJDsgdPHRXSllgZzqvWAXQupGYZVANpBJuK+KAfiaVXCgA71N9xx/5XTSFi5K+e1T4HVnKAzDasAUt7Mmad+1PE+56Gpa73FLk1Ww+xaAEvss6LehjyWHM5iNswoNYzrNS2k6ZYkDnZxUlbrPDELETbz/n3YgBHGUlyrXi2PBjAgMBAAECggEAAtMctqq6meRofuQbEa4Uq5cv0uuQeZLV086VPMNX6k2nXYYODYl36T2mmNndMC5khvBYpn6Ykk/5yjBmlB2nQOMZPLFPwMZVdJ2Nhm+naJLZC0o7fje49PrN2mFsdoZeI+LHVLIrgoILpLdBAz/zTiW+RvLvMnXQU4wdp4eO6i8J/Jwh0AY8rWsAGkk1mdZDwklPZZiwR3z+DDsDwPxFs8z6cE5rWJd2c/fhAQrHwOXyrQPsGyLHTOqS3BkjtEZrKRUlfdgV76VlThwrE5pAWuO0GPyfK/XCklwcNS1a5XxCOq3uUogWRhCsqUX6pYfAVS6xzX56MGDndQVlp7U5uQKBgQDyTDwhsNTWlmr++FyYrc6liSF9NEMBNDubrfLJH1kaOp590bE8fu3BG0UlkVcueUr05e33Kx1DMSFW72lR4dht1jruWsbFp6LlT3SUtyW2kcSet3fC8gySs2r6NncsZ2XFPoxTkalKpQ1atGoBe3XIKeT8RDZtgoLztQy7/7yANQKBgQDQvSHEKS5SttoFFf4YkUh2QmNX5m7XaDlTLB/3xjnlz8NWOweK1aVysb4t2Tct/SR4ZZ/qZDBlaaj4X9h9nlxxIMoXEyX6Ilc4tyCWBXxn6HFMSa/Rrq662Vzz228cPvW2XGOQWdj7IqwKO9cXgJkI5W84YtMtYrTPLDSjhfpxNwKBgGVCoPq/iSOpN0wZhbE1KiCaP8mwlrQhHSxBtS6CkF1a1DPm97g9n6VNfUdnB1Vf0YipsxrSBOe416MaaRyUUzwMBRLqExo1pelJnIIuTG+RWeeu6zkoqUKCAxpQuttu1uRo8IJYZLTSZ9NZhNfbveyKPa2D4G9B1PJ+3rSO+ztlAoGAZNRHQEMILkpHLBfAgsuC7iUJacdUmVauAiAZXQ1yoDDo0Xl4HjcvUSTMkccQIXXbLREh2w4EVqhgR4G8yIk7bCYDmHvWZ2o5KZtD8VO7EVI1kD0z4Zx4qKcggGbp2AINnMYqDetopX7NDbB0KNUklyiEvf72tUCtyDk5QBgSrqcCgYEAnlg3ByRd/qTFz/darZi9ehT68Cq0CS7/B9YvfnF7YKTAv6J2Hd/i9jGKcc27x6IMi0vf7zrqCyTMq56omiLdu941oWfsOnwffWRBInvrUWTj6yGHOYUtg2z4xESUoFYDeWwe/vX6TugL3oXSX3Sy3KWGlJhn/OmsN2fgajHRip0=";
    private static final String CALLBACK_URI = Settings.getSettings().getServerUrl() + "/jira/callback";
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
