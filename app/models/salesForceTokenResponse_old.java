package models;

import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;

public class salesForceTokenResponse_old extends OAuthJSONAccessTokenResponse {

    public String getInstance() {
        return getParam("instance_url");
    }
}
