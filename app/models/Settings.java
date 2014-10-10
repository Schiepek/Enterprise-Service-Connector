package models;


import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Settings extends Model {

    @Id
    private Long id;
    private String serverUrl;

    public String getServerUrl() { return serverUrl;  }

    public void setId(Long id) {   this.id = id;  }

    public void setServerUrl(String serverUrl) {   this.serverUrl = serverUrl;   }

    public static Finder<Long,Settings> find = new Finder( Long.class, Settings.class );

    public static Settings getSettings() {
        if(proofExistingSetting()) return find.ref(1L);
        else return createEmptySetting();
    }

    public static void create(Settings newsetting) {
        Settings existing;
        if(proofExistingSetting()) existing = find.ref(1L);
        else existing = createEmptySetting();
        existing.setServerUrl(newsetting.getServerUrl());
        existing.save();
    }

    public static Settings createEmptySetting() {
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setServerUrl("http://www.sampleserver.com:8000");
        return settings;
    }

    private static boolean proofExistingSetting() {
        return Settings.find.where().eq("id", 1L).findUnique() != null;
    }
}
