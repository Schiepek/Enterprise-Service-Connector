package models.salesForce;

import org.apache.oltu.oauth2.client.response.OAuthJSONAccessTokenResponse;

public class SalesForceTokenResponse extends OAuthJSONAccessTokenResponse {

    public String getInstance() {
        return getParam("instance_url");
    }
}
