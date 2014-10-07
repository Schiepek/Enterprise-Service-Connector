package models.gmail1;

import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.List;

@Entity
public class GMailAccount extends Model {

    @Id
    public Long id;

    private String email_address;
    private String client_id;
    private String client_secret;
    private String access_token;
    private String refresh_token;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getEmail_address() {
        return email_address;
    }

    public void setEmail_address(String email_address) {
        this.email_address = email_address;
    }

    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    public String getClient_secret() {
        return client_secret;
    }

    public void setClient_secret(String client_secret) {
        this.client_secret = client_secret;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }



    public static List<GMailAccount> all() {
        return find.all();
    }

    public static Model.Finder<Long,GMailAccount> find = new Model.Finder(
            Long.class, GMailAccount.class
    );

    public static void create(GMailAccount account) {
        account.save();
    }

    public static void delete(Long id) {
        find.ref(id).delete();
    }

    public static GMailAccount getAccount(Long id) {
        return find.ref(id);
    }
}
