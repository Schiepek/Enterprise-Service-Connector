package models;

import play.db.jpa.JPA;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
public class APIConfig {
    @Id
    @GeneratedValue
    private Long id;

    private String clientId;
    private String clientSecret;
    private String redirectURI;
    private String accessToken;
    private String refreshToken;
    private String instance;



    private String mail;
    private ServiceProvider provider;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
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
        return JPA.em().find(APIConfig.class, id);
    }

    public static List<APIConfig> all() {
        CriteriaBuilder cb = JPA.em().getCriteriaBuilder();
        CriteriaQuery<APIConfig> cq = cb.createQuery(APIConfig.class);
        Root<APIConfig> root = cq.from(APIConfig.class);
        CriteriaQuery<APIConfig> all = cq.select(root);
        TypedQuery<APIConfig> allQuery = JPA.em().createQuery(all);
        return allQuery.getResultList();
    }

    public void save() {
        JPA.em().merge(this);//.find(APIConfig.class, id);
    }

    public static void delete(Long id) {
        APIConfig config = getAPIConfig(id);
        JPA.em().remove(config);
    }
}
