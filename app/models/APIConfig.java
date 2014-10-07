package models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
public class APIConfig extends Model {
    @Id
    private Long id;

    private String clientID;
    private String clientSecret;
    private String redirectURI;
    private String accessToken;
    private String instance;



    public static Finder<Long,APIConfig> find = new Finder(
            Long.class, APIConfig.class
    );

    public String getClientID() {
        return clientID;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    public static void create(APIConfig config) {
        config.save();
    }

    public static APIConfig getConfig(Long id) {
        return find.ref(id);
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }
}
