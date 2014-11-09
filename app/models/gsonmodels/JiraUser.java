package models.gsonmodels;

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
