package models.gsonmodels;

/**
 * Created by Richard on 07.11.2014.
 */
public class JiraUser {
    private String name;
    private String emailAddress;
    private String displayName;
    private boolean active;

    public String getName() {
        return name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isActive() {
        return active;
    }
}
