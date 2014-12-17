package models;

public enum Status {
    GMAILTRANSFER("Transfering Salesforce contacts to Gmail"),
    GOOGLEPHONETRANSFER("Transfering Salesforce contacts to Google phone account"),
    GROUPIMPORT("Importing user/group data from Jira, Confluence and Google"),
    DELETEGMAILCONTACTS("Deleting Salesforce Contacts from Gmail"),
    DELETEGOOGLEPHONECONTACTS("Deleting Salesforce Contacts from Google phone account"),
    NOTHING("no process is running");

    private String string;

    private Status(String string) {
        this.string = string;
    }

    public String toString() {
        return string;
    }
}