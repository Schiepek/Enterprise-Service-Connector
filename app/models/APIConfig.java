package models;

import play.db.ebean.Model;
import javax.persistence.*;
import java.util.List;

@Entity
public class APIConfig extends Model {
    @Id
    private Long id;

    private String clientID;
    private String clientSecret;
    private String redirectURI;
    private String accessToken;
    private String refreshToken;
    private String instance;



    private String mail;
    private ServiceProvider provider;



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

    public String getRefreshToken() {  return refreshToken;   }

    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken;  }

    public ServiceProvider getProvider() { return provider;  }

    public void setProvider(ServiceProvider provider) { this.provider = provider; }

    public Long getId() { return id; }

    public String getMail() { return mail; }

    public void setMail(String mail) { this.mail = mail;  }

    public static APIConfig getAPIConfig(Long id) {
        return find.ref(id);
    }

    public static List<APIConfig> all() {
        return find.all();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }
}
