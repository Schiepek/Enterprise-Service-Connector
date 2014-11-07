package models.gsonmodels;

public class SalesforceContainer {
    private String totalSize;
    private String done;
    private SalesforceContact[] records;

    public SalesforceContact[] getContacts() {
        return records;
    }
}
