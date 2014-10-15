package models;


import play.db.jpa.JPA;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Entity
public class Settings {

    @Id
    @GeneratedValue
    private Long id;
    private String serverUrl;

    public String getServerUrl() { return serverUrl;  }

    public void setId(Long id) {   this.id = id;  }

    public void setServerUrl(String serverUrl) {   this.serverUrl = serverUrl;   }

    public void save() {
        JPA.em().merge(this);
    }

    public static Settings getSettings() {
        if(proofExistingSetting()) return JPA.em().find(Settings.class, 1L);
        else return createEmptySetting();
    }

    public static void create(Settings newsetting) {
        Settings existing;
        existing = getSettings();
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
        return JPA.em().find(Settings.class, 1L) != null;
    }
}
