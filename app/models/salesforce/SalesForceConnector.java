package models.salesforce;

import models.APIConfig;
import models.ServiceProvider;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.common.OAuthProviderType;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.GrantType;

// CLIENT ID =  "3MVG9A_f29uWoVQtLyx_TNRfuq85aLHcIwEjVXgIOrrBWS4P5jZ6APwPmjjutvbNelxFEvodl7fpesAk9JV1l";
// redirectURI = "http://localhost:9000/salesforce/oauth2/callback";
//secret="5039637056870495392"
public class SalesForceConnector {

    public APIConfig safeConfig(APIConfig formConfig) {
        APIConfig config;
        //APIConfig config = APIConfig.getConfig(1L);
        if (APIConfig.getConfig(221L) == null) {
            config = formConfig;
            config.save();
        } else {
            config = APIConfig.getConfig(221L);
            config.setClientId(formConfig.getClientId());
            config.setClientSecret(formConfig.getClientSecret());
            config.setRedirectURI(formConfig.getRedirectURI());
            config.setProvider(ServiceProvider.SALESFORCE);
            config.save();
            //APIConfig config = APIConfig.getConfig(1L);
        }
        return config;
    }

    public String requestLocationURI(APIConfig config) throws OAuthSystemException {
        OAuthClientRequest request = null;
        request = OAuthClientRequest
                .authorizationProvider(OAuthProviderType.SALESFORCE)
                .setClientId(config.getClientId())
                .setRedirectURI(config.getRedirectURI())
                .setResponseType("code")
                .buildQueryMessage();
        return request.getLocationUri();
    }

    public void setAccessToken(String code) throws OAuthSystemException, OAuthProblemException {
        APIConfig config = APIConfig.getConfig(221L);
        OAuthClientRequest request = OAuthClientRequest
                .tokenProvider(OAuthProviderType.SALESFORCE)
                .setGrantType(GrantType.AUTHORIZATION_CODE)
                .setClientId(config.getClientId())
                .setClientSecret(config.getClientSecret())
                .setRedirectURI(config.getRedirectURI())
                .setCode(code)
                .buildQueryMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        SalesForceTokenResponse oAuthResponse = oAuthClient.accessToken(request, SalesForceTokenResponse.class);

        config.setAccessToken(oAuthResponse.getAccessToken());
        config.setRefreshToken(oAuthResponse.getRefreshToken());
        config.setInstance(oAuthResponse.getInstance());
        config.save();
    }

    public void setRefreshToken() throws OAuthSystemException, OAuthProblemException {
        APIConfig config = APIConfig.getConfig(221L);

        OAuthClientRequest request = OAuthClientRequest
                .tokenProvider(OAuthProviderType.SALESFORCE)
                .setGrantType(GrantType.REFRESH_TOKEN)
                .setClientId(config.getClientId())
                .setClientSecret(config.getClientSecret())
                .setRefreshToken(config.getRefreshToken())
                .buildQueryMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        SalesForceTokenResponse oAuthResponse = oAuthClient.accessToken(request, SalesForceTokenResponse.class);

        config.setAccessToken(oAuthResponse.getAccessToken());
        config.setInstance(oAuthResponse.getInstance());
        config.save();
    }
}
