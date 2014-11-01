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

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class GMailContactAccess {
    ContactsService service;
    private static final String GROUP_NAME = "salesforce";
    private static String CONTACT_FEED_URL;
    private static String GROUP_DEFAULT;
    private static final String SALESFORCE_INSTANCE = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE).getInstance() + "/";
    private static final String LAST_MODIFIED = "Last Modified";
    private static final String SALESFORCE_ID = "Salesforce Id";
    private static final int GOOGLE_MAX_RESULTS = 1000000;
    private int created, updated, deleted;

    public GMailContactAccess() throws IOException {
        this(APIConfig.getAPIConfig(ServiceProvider.GMAIL), Settings.getSettings());
    }

    public GMailContactAccess(APIConfig config, Settings settings) throws IOException {
        service = new GMailConnector(config).getContactService();
        setDirectorySettings(settings);
    }

    private void setDirectorySettings(Settings settings) {
        if(settings.getSaveInDirectory()) {
            CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/" + Settings.getSettings().getDomain() + "/full";
            GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/" + Settings.getSettings().getDomain() + "/full";
        } else {
            CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
            GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/default/full";
        }
    }

    public void transferContacts(Container container) throws IOException, ServiceException, java.text.ParseException {
        String groupId = "";
        ContactEntry entry;
        if(!Settings.getSettings().getSaveInDirectory()) {
            groupId = getSalesForceGroupId();
            if (groupId == "") {
                groupId = createContactGroup();
            }
        }
        URL feedUrl = new URL(CONTACT_FEED_URL);
        HashMap<String, ContactEntry> googleContacts = getAllContacts(feedUrl);
        for (Contact contact : container.getContacts()) {
            entry = getContact(googleContacts, contact);
            if (entry == null && contact.getEmail() != null) {
                createContactEntry(contact, groupId, feedUrl);
            } else if (hasNewValues(entry, contact)) {
                updateContactEntry(entry, contact);
            } else if (entry!=null) {
                System.out.println("No Update: " + entry.getName().getFullName().getValue());
            }
            googleContacts.remove(SALESFORCE_INSTANCE + contact.getId());
        }
        for (ContactEntry del : googleContacts.values()) {
            del.delete();
            deleted++;
            System.out.println("Delete: " + del.getName().getFullName());
        }
        Logging.log(created, updated, deleted);
    }

    public void deleteContacts() throws IOException, ServiceException {
        for (ContactEntry del : getAllContacts(new URL(CONTACT_FEED_URL)).values()) {
            del.delete();
            deleted++;
            System.out.println("Delete: " + del.getName().getFullName().getValue());
        }
        Logging.log(created, updated, deleted);
    }

    private ContactEntry getContact(HashMap<String, ContactEntry> googleContacts, Contact c) throws IOException, ServiceException {
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

    private boolean hasNewValues(ContactEntry entry, Contact c) throws ServiceException, java.text.ParseException, IOException {
        if(entry==null && c.getEmail()==null) return false;
        String transferDate = EscDateTimeParser.parseSfDateToString((c.getLastModifiedDate()));
        String googleDate = null;
        for (UserDefinedField field : entry.getUserDefinedFields()) {
            if (field.getKey().equals(LAST_MODIFIED)) googleDate = field.getValue();
        }
        if (googleDate == null) return true;
        return !transferDate.equals(googleDate);//TODO Account LAST EDITED???
    }

    private void createContactEntry(Contact c, String groupId, URL feedUrl) throws ServiceException, java.text.ParseException, IOException {
        ContactEntry entry = new ContactEntry();
        if(!Settings.getSettings().getSaveInDirectory()){
            entry.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
        }
        createName(entry, c);
        createMail(entry, c);
        createId(entry, c);
        createWebsite(entry, c);
        createBirthday(entry, c);
        createLanguage(entry, c);
        createPhoneNumber(entry, c.getPhone(), PhoneNumber.Rel.WORK, true);
        createPhoneNumber(entry, c.getMobilePhone(), PhoneNumber.Rel.MOBILE, false);
        createPhoneNumber(entry, c.getAccountPhone(), PhoneNumber.Rel.COMPANY_MAIN, false);
        createPostalAddress(entry, c);
        createTitle(entry, c);
        createManager(entry, c);
        createOrganization(entry, c);
        createUserField(entry, "F-Contact", c.getF_contact());
        createUserField(entry, "F-Branch", c.getOwnerName());
        createUserField(entry, LAST_MODIFIED, EscDateTimeParser.parseSfDateToString(c.getLastModifiedDate()));

        service.insert(feedUrl, entry);
        created++;
        System.out.println("Create: " + entry.getName().getFullName().getValue());
    }

    private void updateContactEntry(ContactEntry entry, Contact c) throws java.text.ParseException, IOException, ServiceException {
        if (c.getEmail() == null) {
            entry.delete();
            deleted++;
        } else {
            updateName(entry, c);
            updateMail(entry, c);
            updateWebsite(entry, c);
            updateBirthday(entry, c);
            updateLanguage(entry, c);
            updatePhoneNumber(entry, c.getPhone(), PhoneNumber.Rel.WORK, true);
            updatePhoneNumber(entry, c.getMobilePhone(), PhoneNumber.Rel.MOBILE, false);
            updatePhoneNumber(entry, c.getAccountPhone(), PhoneNumber.Rel.COMPANY_MAIN, false);
            updatePostalAddress(entry, c);
            updateTitle(entry, c);
            updateManager(entry, c);
            updateOrganization(entry, c);
            updateUserField(entry, "F-Contact", c.getF_contact());
            updateUserField(entry, "F-Branch", c.getOwnerName());
            updateUserField(entry, LAST_MODIFIED, EscDateTimeParser.parseSfDateToString(c.getLastModifiedDate()));

            URL editUrl = new URL(entry.getEditLink().getHref());
            service.update(editUrl, entry);
            updated++;
            System.out.println("Update: " + entry.getName().getFullName().getValue());
        }
    }

    private void createName(ContactEntry entry, Contact c) {
        if (c.getLastName() != null) {
            Name name = new Name();
            if (c.getFirstName() != null) {
                name.setGivenName(new GivenName(c.getFirstName(), null));
            }
            name.setFamilyName(new FamilyName(c.getLastName(), null));
            name.setFullName(new FullName(generateFullName(c), null));
            entry.setName(name);
        }
    }

    private void updateName(ContactEntry entry, Contact c) {
        Name name = entry.getName();
        if (name == null) {
            createName(entry, c);
        } else {
            if (name.getGivenName() != null && c.getFirstName() != null) {
                name.getGivenName().setValue(c.getFirstName());
            } else if (name.getGivenName() == null && c.getFirstName() != null) {
                name.setGivenName(new GivenName(c.getFirstName(), null));
            } else if (name.getGivenName() != null && c.getFirstName() == null) {
                name.setGivenName(null);
            }
            name.getFamilyName().setValue(c.getLastName());
            name.getFullName().setValue(generateFullName(c));
        }
    }

    private String generateFullName(Contact c) {
        String fullname = "";
        if (c.getFirstName() != null) fullname += c.getFirstName() + " ";
        fullname += c.getLastName();
        return fullname;
    }

    private void createMail(ContactEntry entry, Contact c) {
        if (c.getEmail() != null) {
            Email email = new Email();
            email.setAddress(c.getEmail());
            email.setDisplayName(generateFullName(c));
            email.setRel(Email.Rel.WORK);
            email.setPrimary(true);
            entry.addEmailAddress(email);
        }
    }

    private void updateMail(ContactEntry entry, Contact c) {
        if (entry.getEmailAddresses().isEmpty()) {
            createMail(entry, c);
        } else if (c.getEmail() == null) {
            entry.getEmailAddresses().remove(0);
        } else {
            entry.getEmailAddresses().get(0).setAddress(c.getEmail());
        }
    }

    private void createId(ContactEntry entry, Contact c) {
        if (c.getId() != null) {
            Website id = new Website();
            id.setHref(SALESFORCE_INSTANCE + c.getId());
            id.setLabel(SALESFORCE_ID);
            id.setPrimary(true);
            entry.addWebsite(id);
        }
    }

    private void createWebsite(ContactEntry entry, Contact c) {
        if (c.getAccountWebsite() != null) {
            Website home = new Website();
            home.setHref(c.getAccountWebsite());
            home.setLabel("Homepage");
            home.setPrimary(false);
            entry.addWebsite(home);
        }
    }

    private void updateWebsite(ContactEntry entry, Contact c) {
        List<Website> websites = entry.getWebsites();
        if (websites.isEmpty()) {
            createWebsite(entry, c);
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

    private void createBirthday(ContactEntry entry, Contact c) {
        if (c.getBirthdate() != null) {
            Birthday birthday = new Birthday();
            birthday.setWhen(c.getBirthdate());
            entry.setBirthday(birthday);
        }
    }

    private void updateBirthday(ContactEntry entry, Contact c) {
        if (entry.getBirthday() == null) {
            createBirthday(entry, c);
        } else {
            entry.getBirthday().setWhen(c.getBirthdate());
        }
    }

    private void createLanguage(ContactEntry entry, Contact c) {
        if (c.getLanguages() != null && Arrays.asList(Locale.getISOLanguages()).contains(c.getLanguages())) {
            Language lang = new Language();
            lang.setCode(c.getLanguages());
            entry.addLanguage(lang);
        }

    }

    private void updateLanguage(ContactEntry entry, Contact c) {
        if (entry.getLanguages().isEmpty()) {
            createLanguage(entry, c);
        } else if (c.getLanguages() == null) {
            entry.getLanguages().remove(0);
        } else if (Arrays.asList(Locale.getISOLanguages()).contains(c.getLanguages())) {
            entry.getLanguages().get(0).setCode(c.getLanguages());
        }
    }

    private void createPhoneNumber(ContactEntry entry, String number, String schema, boolean primary) {
        if (number != null) {
            PhoneNumber phoneNumber = new PhoneNumber();
            phoneNumber.setPhoneNumber(number);
            phoneNumber.setRel(schema);
            phoneNumber.setPrimary(primary);
            entry.addPhoneNumber(phoneNumber);
        }
    }

    private void updatePhoneNumber(ContactEntry entry, String number, String schema, boolean primary) {
        PhoneNumber phoneNumber = null;
        for (PhoneNumber pn : entry.getPhoneNumbers()) {
            if (pn.getRel().equals(schema)) {
                phoneNumber = pn;
            }
        }
        if (phoneNumber == null) {
            createPhoneNumber(entry, number, schema, primary);
        } else if (number == null) {
            entry.getPhoneNumbers().remove(phoneNumber);
        } else {
            phoneNumber.setPhoneNumber(number);
        }
    }

    private void createPostalAddress(ContactEntry entry, Contact c) {
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

    private void updatePostalAddress(ContactEntry entry, Contact c) {
        String street = c.getMailingStreet();
        String city = c.getMailingCity();
        String plz = c.getMailingPostalCode();
        String country = c.getMailingCountry();

        if (entry.getStructuredPostalAddresses().isEmpty()) {
            createPostalAddress(entry, c);
        } else if (street == null && city == null && plz == null && country == null) {
            entry.getStructuredPostalAddresses().remove(entry.getStructuredPostalAddresses().get(0));
        } else {
            entry.getStructuredPostalAddresses().remove(entry.getStructuredPostalAddresses().get(0));
            createPostalAddress(entry, c);
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

    private void createTitle(ContactEntry entry, Contact c) {
        if (c.getSalutationTitle() != null) {
            entry.setTitle(new PlainTextConstruct(c.getSalutationTitle()));
        }
    }

    private void updateTitle(ContactEntry entry, Contact c) {
        if (entry.getTitle() == null) {
            createTitle(entry, c);
        } else if (c.getSalutationTitle() == null) {
            entry.setTitle(null);
        } else {
            entry.setTitle(new PlainTextConstruct(c.getSalutationTitle()));
        }
    }

    private void createManager(ContactEntry entry, Contact c) {
        if (c.getReportsToName() != null) {
            Relation rel = new Relation();
            rel.setRel(Relation.Rel.MANAGER);
            rel.setValue(c.getReportsToName());
            entry.addRelation(rel);
        }
    }

    private void updateManager(ContactEntry entry, Contact c) {
        if (entry.getRelations().isEmpty()) {
            createManager(entry, c);
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

    private void createOrganization(ContactEntry entry, Contact c) {
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

    private void updateOrganization(ContactEntry entry, Contact c) {
        if (entry.getOrganizations().isEmpty()) {
            createOrganization(entry, c);
        } else if (c.getTitle() == null && c.getAccountName() == null) {
            entry.getOrganizations().remove(entry.getOrganizations().get(0));
        } else {
            entry.getOrganizations().remove(entry.getOrganizations().get(0));
            createOrganization(entry, c);
        }
    }

    private void createUserField(ContactEntry entry, String key, String value) {
        if (value != null) {
            entry.addUserDefinedField(new UserDefinedField(key, value));
        }
    }

    private void updateUserField(ContactEntry entry, String key, String value) {
        UserDefinedField field = null;
        for (UserDefinedField f : entry.getUserDefinedFields()) {
            if (f.getKey().equals(key)) {
                field = f;
            }
        }
        if (field == null) {
            createUserField(entry, key, value);
        } else if (value == null) {
            entry.getUserDefinedFields().remove(field);
        } else {
            field.setValue(value);
        }
    }

    private String createContactGroup() throws IOException, ServiceException {
        ContactGroupEntry group = new ContactGroupEntry();
        group.setTitle(new PlainTextConstruct(GROUP_NAME));
        URL postUrl = new URL(GROUP_DEFAULT);
        ContactGroupEntry createdGroup = service.insert(postUrl, group);
        return createdGroup.getId();
    }

    private String getSalesForceGroupId() throws IOException, ServiceException {
        String salesForceGroupId = "";
        URL feedUrl = new URL(GROUP_DEFAULT);
        ContactGroupFeed resultFeed = service.getFeed(feedUrl, ContactGroupFeed.class);
        for (ContactGroupEntry groupEntry : resultFeed.getEntries()) {
            if (groupEntry.getTitle().getPlainText().equals(GROUP_NAME)) {
                salesForceGroupId = groupEntry.getId();
                break;
            }
        }
        return salesForceGroupId;
    }
}
