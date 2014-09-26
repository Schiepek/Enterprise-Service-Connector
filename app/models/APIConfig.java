package models;

import play.db.ebean.Model;
import javax.persistence.*;

@Entity
public class APIConfig extends Model {
    @Id
    private Long id;

    private String clientID;
    private String redirectURI;


    public static Finder<Long,APIConfig> find = new Finder(
            Long.class, APIConfig.class
    );

    public String getClientID() {
        return clientID;
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

    public static void create(APIConfig config) {
        config.save();
    }

    public static APIConfig getConfig(Long id) {
        return find.ref(id);
    }
}
