package logic.gmail;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.*;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.ServiceException;
import models.*;
import models.gsonmodels.SalesforceContact;
import models.gsonmodels.SalesforceContainer;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GMailContactAccess {
    private ContactsService service;
    private final String GROUP_NAME = "System Group: My Contacts";
    private String CONTACT_FEED_URL;
    private String GROUP_DEFAULT;
    private String SALESFORCE_INSTANCE = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE).getInstance() + "/";
    private String LAST_MODIFIED = "Last Modified";
    private String SALESFORCE_ID = "Salesforce Id";
    private String F_BRANCH = "f-Branch";
    private String F_CONTACT = "f-Contact";
    private int EXCEPTION_COUNT = 20;
    private int GOOGLE_MAX_RESULTS = 1000000;
    private ContactEntry entry;
    private SalesforceContact c;
    private List<String> loggingInformation = new ArrayList<String>();

    public GMailContactAccess() throws IOException {
        this(APIConfig.getAPIConfig(ServiceProvider.GMAIL), Settings.getSettings());
    }

    public GMailContactAccess(APIConfig config, Settings settings) throws IOException {
        service = new GMailConnector(config).getContactService();
        setDirectorySettings(settings);
    }

    private void setDirectorySettings(Settings settings) {
        if (settings.getSaveInDirectory()) {
            CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/" + Settings.getSettings().getDomain() + "/full";
            GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/" + Settings.getSettings().getDomain() + "/full";
        } else {
            CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
            GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/default/full";
        }
    }

    public void transferContacts(SalesforceContainer container) throws IOException, ServiceException, java.text.ParseException {
        String groupId = "";
        if (!Settings.getSettings().getSaveInDirectory()) {
            groupId = getMyContactsGroupId();
        }
        int exceptionCount = 0;
        URL feedUrl = new URL(CONTACT_FEED_URL);
        HashMap<String, ContactEntry> googleContacts = getAllContacts(feedUrl);
        int counter = 0; //TODO remove reducer
        for (SalesforceContact contact : container.getContacts()) {
            counter++;
            if (counter > 20) break; //TODO remove reducer
            try {
                c = contact;
                entry = getContact(googleContacts);
                if (entry == null) {
                    createContactEntry(groupId, feedUrl);
                } else if (hasNewValues()) {
                    updateContactEntry();
                }
                googleContacts.remove(SALESFORCE_INSTANCE + contact.getId());
                exceptionCount = 0;
            } catch (Exception e) {
                if (exceptionCount > EXCEPTION_COUNT) {
                    throw e;
                }
                exceptionCount++;
            }
        }
        for (ContactEntry del : googleContacts.values()) {
            if(getSalesForceId(del)!=null) {
                entry = del;
                addLoggingInformation("DELETE");
                del.delete();
            }
        }
        Logging.logTransfer(loggingInformation);
    }

    public void deleteContacts() throws IOException, ServiceException {
        for (ContactEntry del : getAllContacts(new URL(CONTACT_FEED_URL)).values()) {
            if(getSalesForceId(del)!=null) {
                entry = del;
                addLoggingInformation("DELETE");
                del.delete();
            }
        }
        Logging.logTransfer(loggingInformation);
    }

    private ContactEntry getContact(HashMap<String, ContactEntry> googleContacts) throws IOException, ServiceException {
        if (googleContacts.isEmpty()) return null;
        return googleContacts.get(SALESFORCE_INSTANCE + c.getId());
    }

    private HashMap<String, ContactEntry> getAllContacts(URL feedUrl) throws IOException, ServiceException {
        HashMap<String, ContactEntry> map = new HashMap<>();
        Query query = new Query(feedUrl);
        query.setMaxResults(GOOGLE_MAX_RESULTS);
        List<ContactEntry> contactList = service.query(query, ContactFeed.class).getEntries();
        for (ContactEntry entry : contactList) {
            String salesforceId = getSalesForceId(entry);
            if (salesforceId != null) {
                map.put(getSalesForceId(entry), entry);
            }
        }
        return map;
    }

    private String getSalesForceId(ContactEntry entry) {
        if (entry.getWebsites().isEmpty()) return null;
        for (Website web : entry.getWebsites()) {
            if (web.getLabel().equals(SALESFORCE_ID)) return web.getHref();
        }
        return null;
    }

    private boolean hasNewValues() throws ServiceException, java.text.ParseException, IOException {
        if (entry == null) return false;
        String transferDate = EscDateTimeParser.parseSfDateToString((c.getLastModifiedDate()));
        String googleDate = null;
        for (UserDefinedField field : entry.getUserDefinedFields()) {
            if (field.getKey().equals(LAST_MODIFIED)) googleDate = field.getValue();
        }
        if (googleDate == null) return true;
        return !transferDate.equals(googleDate);//TODO Account LAST EDITED???
    }

    private void createContactEntry(String groupId, URL feedUrl) throws ServiceException, java.text.ParseException, IOException {
        entry = new ContactEntry();
        if (!Settings.getSettings().getSaveInDirectory()) {
            entry.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
        }
        createName();
        createMail();
        createId();
        createWebsite();
        createBirthday();
        createLanguage();
        createPhoneNumber(c.getPhone(), PhoneNumber.Rel.WORK, true);
        createPhoneNumber(c.getMobilePhone(), PhoneNumber.Rel.MOBILE, false);
        createPhoneNumber(c.getAccountPhone(), PhoneNumber.Rel.COMPANY_MAIN, false);
        createPostalAddress();
        createTitle();
        createManager();
        createOrganization();
        createUserField(F_CONTACT, c.getF_contact());
        createUserField(F_BRANCH, c.getOwnerName());
        createUserField(LAST_MODIFIED, EscDateTimeParser.parseSfDateToString(c.getLastModifiedDate()));

        service.insert(feedUrl, entry);
        addLoggingInformation("CREATE");
    }

    private void updateContactEntry() throws java.text.ParseException, IOException, ServiceException {
        updateName();
        updateMail();
        updateWebsite();
        updateBirthday();
        updateLanguage();
        updatePhoneNumber(c.getPhone(), PhoneNumber.Rel.WORK, true);
        updatePhoneNumber(c.getMobilePhone(), PhoneNumber.Rel.MOBILE, false);
        updatePhoneNumber(c.getAccountPhone(), PhoneNumber.Rel.COMPANY_MAIN, false);
        updatePostalAddress();
        updateTitle();
        updateManager();
        updateOrganization();
        updateUserField(F_CONTACT, c.getF_contact());
        updateUserField(F_BRANCH, c.getOwnerName());
        updateUserField(LAST_MODIFIED, EscDateTimeParser.parseSfDateToString(c.getLastModifiedDate()));

        URL editUrl = new URL(entry.getEditLink().getHref());
        service.update(editUrl, entry);
        addLoggingInformation("UPDATE");
    }

    private void createName() {
        if (c.getLastName() != null) {
            Name name = new Name();
            if (c.getFirstName() != null) {
                name.setGivenName(new GivenName(c.getFirstName(), null));
            }
            name.setFamilyName(new FamilyName(c.getLastName(), null));
            name.setFullName(new FullName(generateFullName(), null));
            entry.setName(name);
        }
    }

    private void updateName() {
        Name name = entry.getName();
        if (name == null) {
            createName();
        } else {
            if (name.getGivenName() != null && c.getFirstName() != null) {
                name.getGivenName().setValue(c.getFirstName());
            } else if (name.getGivenName() == null && c.getFirstName() != null) {
                name.setGivenName(new GivenName(c.getFirstName(), null));
            } else if (name.getGivenName() != null && c.getFirstName() == null) {
                name.setGivenName(null);
            }
            name.getFamilyName().setValue(c.getLastName());
            name.getFullName().setValue(generateFullName());
        }
    }

    private String generateFullName() {
        String fullname = "";
        if (c.getFirstName() != null) fullname += c.getFirstName() + " ";
        fullname += c.getLastName();
        return fullname;
    }

    private void createMail() {
        if (c.getEmail() != null) {
            Email email = new Email();
            email.setAddress(c.getEmail());
            email.setDisplayName(generateFullName());
            email.setRel(Email.Rel.WORK);
            email.setPrimary(true);
            entry.addEmailAddress(email);
        }
    }

    private void updateMail() {
        if (entry.getEmailAddresses().isEmpty()) {
            createMail();
        } else if (c.getEmail() == null) {
            entry.getEmailAddresses().remove(0);
        } else {
            entry.getEmailAddresses().get(0).setAddress(c.getEmail());
        }
    }

    private void createId() {
        if (c.getId() != null) {
            Website id = new Website();
            id.setHref(SALESFORCE_INSTANCE + c.getId());
            id.setLabel(SALESFORCE_ID);
            id.setPrimary(true);
            entry.addWebsite(id);
        }
    }

    private void createWebsite() {
        if (c.getAccountWebsite() != null) {
            Website home = new Website();
            home.setHref(c.getAccountWebsite());
            home.setLabel("Homepage");
            home.setPrimary(false);
            entry.addWebsite(home);
        }
    }

    private void updateWebsite() {
        List<Website> websites = entry.getWebsites();
        if (websites.isEmpty()) {
            createWebsite();
        } else {
            for (int i = 0; i < entry.getWebsites().size(); i++) {
                if (entry.getWebsites().get(i).getLabel().equals("Homepage")) {
                    if (c.getAccountWebsite() != null) {
                        entry.getWebsites().get(i).setHref(c.getAccountWebsite());
                    } else if (c.getAccountWebsite() == null) {
                        entry.getWebsites().remove(i);
                    } else {
                        Website home = new Website();
                        home.setHref(c.getAccountWebsite());
                        home.setLabel("Homepage");
                        home.setPrimary(false);
                        entry.addWebsite(home);
                    }
                }
            }
        }
    }

    private void createBirthday() {
        if (c.getBirthdate() != null) {
            Birthday birthday = new Birthday();
            birthday.setWhen(c.getBirthdate());
            entry.setBirthday(birthday);
        }
    }

    private void updateBirthday() {
        if (entry.getBirthday() == null) {
            createBirthday();
        } else {
            entry.getBirthday().setWhen(c.getBirthdate());
        }
    }

    private void createLanguage() {
        if (c.getLanguages() != null && Arrays.asList(Locale.getISOLanguages()).contains(c.getLanguages())) {
            Language lang = new Language();
            lang.setCode(c.getLanguages());
            entry.addLanguage(lang);
        }

    }

    private void updateLanguage() {
        if (entry.getLanguages().isEmpty()) {
            createLanguage();
        } else if (c.getLanguages() == null) {
            entry.getLanguages().remove(0);
        } else if (Arrays.asList(Locale.getISOLanguages()).contains(c.getLanguages())) {
            entry.getLanguages().get(0).setCode(c.getLanguages());
        }
    }

    private void createPhoneNumber(String number, String schema, boolean primary) {
        if (number != null) {
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setPhoneNumber(number);
            phoneNumber.setRel(schema);
            phoneNumber.setPrimary(primary);
            entry.addPhoneNumber(phoneNumber);
        }
    }

    private void updatePhoneNumber(String number, String schema, boolean primary) {
        PhoneNumber phoneNumber = null;
        for (PhoneNumber pn : entry.getPhoneNumbers()) {
            if (pn.getRel().equals(schema)) {
                phoneNumber = pn;
            }
        }
        if (phoneNumber == null) {
            createPhoneNumber(number, schema, primary);
        } else if (number == null) {
            entry.getPhoneNumbers().remove(phoneNumber);
        } else {
            phoneNumber.setPhoneNumber(number);
        }
    }

    private void createPostalAddress() {
        String street = c.getMailingStreet();
        String city = c.getMailingCity();
        String plz = c.getMailingPostalCode();
        String country = c.getMailingCountry();
        if (street != null && city != null && plz != null && country != null) {
            StructuredPostalAddress postalAddress = new StructuredPostalAddress();
            postalAddress.setStreet(new Street(street));
            postalAddress.setCity(new City(city));
            postalAddress.setPostcode(new PostCode(plz));
            Country cy = new Country();
            cy.setValue(country);
            postalAddress.setCountry(cy);
            postalAddress.setFormattedAddress(new FormattedAddress(generateFormattedAddress(street, plz, city, country)));
            postalAddress.setRel(StructuredPostalAddress.Rel.WORK);
            postalAddress.setPrimary(true);
            entry.addStructuredPostalAddress(postalAddress);
        }

    }

    private void updatePostalAddress() {
        String street = c.getMailingStreet();
        String city = c.getMailingCity();
        String plz = c.getMailingPostalCode();
        String country = c.getMailingCountry();

        if (entry.getStructuredPostalAddresses().isEmpty()) {
            createPostalAddress();
        } else if (street == null && city == null && plz == null && country == null) {
            entry.getStructuredPostalAddresses().remove(entry.getStructuredPostalAddresses().get(0));
        } else {
            entry.getStructuredPostalAddresses().remove(entry.getStructuredPostalAddresses().get(0));
            createPostalAddress();
        }
    }

    private String generateFormattedAddress(String street, String plz, String city, String country) {
        String address = "";
        if (street != null) address += street;
        if (plz != null) address += " " + plz;
        if (city != null) address += " " + city;
        if (country != null) address += " " + country;
        return address;
    }

    private void createTitle() {
        if (c.getSalutationTitle() != null) {
            entry.setTitle(new PlainTextConstruct(c.getSalutationTitle()));
        }
    }

    private void updateTitle() {
        if (entry.getTitle() == null) {
            createTitle();
        } else if (c.getSalutationTitle() == null) {
            entry.setTitle(null);
        } else {
            entry.setTitle(new PlainTextConstruct(c.getSalutationTitle()));
        }
    }

    private void createManager() {
        if (c.getReportsToName() != null) {
            Relation rel = new Relation();
            rel.setRel(Relation.Rel.MANAGER);
            rel.setValue(c.getReportsToName());
            entry.addRelation(rel);
        }
    }

    private void updateManager() {
        if (entry.getRelations().isEmpty()) {
            createManager();
        } else {
            for (Relation rel : entry.getRelations()) {
                if (rel.getRel().equals(Relation.Rel.MANAGER)) {
                    if (c.getReportsToName() == null) {
                        entry.getRelations().remove(rel);
                    } else {
                        rel.setValue(c.getReportsToName());
                    }
                    break;
                }
            }
        }
    }

    private void createOrganization() {
        if (c.getAccountName() != null && c.getTitle() != null) {
            Organization org = new Organization();
            org.setRel(Organization.Rel.WORK);
            if (c.getTitle() != null) {
                org.setOrgTitle(new OrgTitle(c.getTitle()));
            }
            if (c.getAccountName() != null) {
                org.setOrgName(new OrgName(c.getAccountName()));
            }
            entry.addOrganization(org);
        }

    }

    private void updateOrganization() {
        if (entry.getOrganizations().isEmpty()) {
            createOrganization();
        } else if (c.getTitle() == null && c.getAccountName() == null) {
            entry.getOrganizations().remove(entry.getOrganizations().get(0));
        } else {
            entry.getOrganizations().remove(entry.getOrganizations().get(0));
            createOrganization();
        }
    }

    private void createUserField(String key, String value) {
        if (value != null) {
            entry.addUserDefinedField(new UserDefinedField(key, value));
        }
    }

    private void updateUserField(String key, String value) {
        UserDefinedField field = null;
        for (UserDefinedField f : entry.getUserDefinedFields()) {
            if (f.getKey().equals(key)) {
                field = f;
            }
        }
        if (field == null) {
            createUserField(key, value);
        } else if (value == null) {
            entry.getUserDefinedFields().remove(field);
        } else {
            field.setValue(value);
        }
    }

    private String getMyContactsGroupId() throws IOException, ServiceException {
        String myContactsGroupId = "";
        URL feedUrl = new URL(GROUP_DEFAULT);
        ContactGroupFeed resultFeed = service.getFeed(feedUrl, ContactGroupFeed.class);
        for (ContactGroupEntry groupEntry : resultFeed.getEntries()) {
            if (groupEntry.getTitle().getPlainText().equals(GROUP_NAME)) {
                myContactsGroupId = groupEntry.getId();
                break;
            }
        }
        return myContactsGroupId;
    }

    private void addLoggingInformation(String action) {
        loggingInformation.add(action + ": " + getSalesForceId(entry) + " | " + entry.getName().getFullName().getValue());
    }
}
