package models;


import play.db.jpa.JPA;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Settings {

    @Id
    @GeneratedValue
    private Long id;
    private String serverUrl;
    private String domain;

    public String getServerUrl() {
        return serverUrl;
    }

    public String getDomain() {
        return domain;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public void save() {
        JPA.em().merge(this);
    }

    public static Settings getSettings() {
        if (proofExistingSetting()) return JPA.em().find(Settings.class, 1L);
        else return createEmptySetting();
    }

    public static void create(Settings newsetting) {
        Settings existing;
        existing = getSettings();
        existing.setServerUrl(newsetting.getServerUrl());
        existing.setDomain(newsetting.getDomain());
        existing.save();
    }

    public static Settings createEmptySetting() {
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setServerUrl("http://www.sampleserver.com:8000");
        settings.setDomain("exampledomain.com");
        return settings;
    }

    private static boolean proofExistingSetting() {
        return JPA.em().find(Settings.class, 1L) != null;
    }
}
