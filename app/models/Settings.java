package models;


import play.db.jpa.JPA;
import play.libs.Time;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.util.Date;

@Entity
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String serverUrl;
    private String domain;
    private String jiraUrl;
    private String confluenceUrl;
    private String cronExpression;
    private boolean saveInDirectory;
    private Date lastImport;

    public String getJiraUrl() {
        return jiraUrl;
    }

    public void setJiraUrl(String jiraUrl) {
        this.jiraUrl = jiraUrl;
    }

    public boolean getSaveInDirectory() {
        return saveInDirectory;
    }

    public void setSaveInDirectory(boolean saveInDirectory) {
        this.saveInDirectory = saveInDirectory;
    }

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

    public String getConfluenceUrl() {
        return confluenceUrl;
    }

    public void setConfluenceUrl(String confluenceUrl) {
        this.confluenceUrl = confluenceUrl;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        //boolean hasChanged = this.cronExpression.equals(cronExpression);
        this.cronExpression = cronExpression;
      //  if (Time.CronExpression.isValidExpression(getCronExpression())&& hasChanged) {
      //      Global.setNewScheduler();
       // }
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
        existing.setSaveInDirectory(newsetting.getSaveInDirectory());
        existing.setJiraUrl(newsetting.getJiraUrl());
        existing.setConfluenceUrl(newsetting.getConfluenceUrl());
        existing.setCronExpression(newsetting.getCronExpression());
        existing.save();
    }

    public static Settings createEmptySetting() {
        Settings settings = new Settings();
        settings.setId(1L);
        settings.setServerUrl("http://www.sampleserver.com:8000");
        settings.setDomain("exampledomain.com");
        settings.setJiraUrl("exampledomain.com/jira");
        settings.setConfluenceUrl("exampledomain.com/confluence");
        settings.setCronExpression("0 0 12 * * ?");
        settings.setSaveInDirectory(true);
        return settings;
    }

    private static boolean proofExistingSetting() {
        return JPA.em().find(Settings.class, 1L) != null;
    }

    public String validate() {
        if (!Time.CronExpression.isValidExpression(getCronExpression())) {
            return "Invalid Cron Expression";
        }
        return null;
    }

    public Date getLastImport() {
        return lastImport;
    }

    public void setLastImport(Date lastImport) {
        this.lastImport = lastImport;
    }
}
