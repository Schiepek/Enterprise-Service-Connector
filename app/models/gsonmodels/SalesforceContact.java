package models.gsonmodels;

public class SalesforceContact {

    private String LastName;
    private String FirstName;
    private String Email;
    private String Title;
    private String Salutation_Title__c;
    private String Birthdate;
    private String Languages__c;
    private String Phone;
    private String MobilePhone;
    private String ReportsToId;
    private String AccountId;
    private String MailingStreet;
    private String MailingCity;
    private String MailingPostalCode;
    private String MailingCountry;
    private String f_contact__c;
    private String OwnerId;
    private String Id;
    private String LastModifiedDate;
    private SalesforceAccount Account;
    private SalesforceContact ReportsTo;
    private SalesforceContact Owner;

    public String getLastName() {
        return LastName;
    }

    public String getFirstName() {
        return FirstName;
    }

    public String getEmail() {
        return Email;
    }

    public String getTitle() {
        return Title;
    }

    public String getSalutationTitle() {
        return Salutation_Title__c;
    }

    public String getBirthdate() {
        return Birthdate;
    }

    public String getLanguages() {
        return Languages__c;
    }

    public String getPhone() {
        return Phone;
    }

    public String getMobilePhone() {
        return MobilePhone;
    }

    public String getReportsToId() {
        return ReportsToId;
    }

    public String getAccountId() {
        return AccountId;
    }

    public String getMailingStreet() {
        return MailingStreet;
    }

    public String getMailingCity() {
        return MailingCity;
    }

    public String getMailingPostalCode() {
        return MailingPostalCode;
    }

    public String getMailingCountry() {
        return MailingCountry;
    }

    public String getF_contact() {
        return f_contact__c;
    }

    public String getOwnerId() {
        return OwnerId;
    }

    public String getId() {
        return Id;
    }

    public String getLastModifiedDate() {
        return LastModifiedDate;
    }

    public String getAccountName() {
        return Account != null ? Account.getName() : null;
    }

    public String getAccountWebsite() {
        return Account != null ? Account.getWebsite() : null;
    }

    public String getAccountPhone() {
        return Account != null ? Account.getPhone() : null;
    }

    public String getName() {
        return LastName + " " + FirstName;
    }

    public String getReportsToName() {
        return ReportsTo != null ? ReportsTo.getName() : null;
    }

    public String getOwnerName() {
        return Owner != null ? Owner.getLastName() : null;
    }
}
