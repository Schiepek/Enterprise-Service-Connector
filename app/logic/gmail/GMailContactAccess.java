package logic.gmail;

import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.*;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.ParseException;
import com.google.gdata.util.ServiceException;
import models.*;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class GMailContactAccess {
    ContactsService service;
    private static final String GROUP_NAME = "salesforce";
    private static final String CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
    private static final String GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/default/full";
    private static final String SALESFORCE_INSTANCE = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE).getInstance() + "/";
    private static final String LAST_MODIFIED = "Last Modified";
    private static final String SALESFORCE_ID = "Salesforce Id";

    public GMailContactAccess() throws IOException {
        service = new GMailConnector().getContactService();
    }

    public void transferContacts(Container container) throws IOException, ServiceException, java.text.ParseException {
        String groupId = getSalesForceGroupId();
        if (groupId == "") groupId = createContactGroup();
        URL feedUrl = new URL(CONTACT_FEED_URL);
        HashMap<String, ContactEntry> googleContacts = getAllContacts(feedUrl);
        for (Contact contact : container.getContacts()) {
            ContactEntry entry = getContact(googleContacts, contact);
            if (entry == null) service.insert(feedUrl, createContactEntry(contact, groupId));
            else if (hasNewValues(entry, contact)) {
                updateContactEntry(entry, contact);
                URL editUrl = new URL(entry.getEditLink().getHref());
                service.update(editUrl, entry);
            }
            googleContacts.remove(SALESFORCE_INSTANCE + contact.getId());
        }
        for (ContactEntry entry : googleContacts.values()) entry.delete();
    }

    private ContactEntry getContact(HashMap<String, ContactEntry> googleContacts, Contact c) throws IOException, ServiceException {
        if (googleContacts.isEmpty()) return null;
        return googleContacts.get(SALESFORCE_INSTANCE + c.getId());
    }

    private HashMap<String, ContactEntry> getAllContacts(URL feedUrl) throws IOException, ServiceException {
        HashMap<String,ContactEntry> map = new HashMap<>();
        List<ContactEntry> contactList = service.getFeed(feedUrl, ContactFeed.class).getEntries();
        for(ContactEntry entry : contactList) {
            map.put(getSalesForceId(entry),entry);
        }
        return map;
    }

    private String getSalesForceId(ContactEntry entry) {
        if(entry.getWebsites().isEmpty()) return null;
        for(Website web : entry.getWebsites()) {
            if (web.getLabel().equals(SALESFORCE_ID)) return web.getHref();
        }
        return null;
    }

    private boolean hasNewValues(ContactEntry entry, Contact c) throws ServiceException, java.text.ParseException, IOException {
        String transferDate = EscDateTimeParser.parseSfDateToString((c.getLastModifiedDate()));
        String googleDate = null;
        for (UserDefinedField field : entry.getUserDefinedFields()) {
            if (field.getKey().equals(LAST_MODIFIED)) googleDate = field.getValue();
        }
        if (googleDate == null) return true;
        return !transferDate.equals(googleDate);
    }

    private ContactEntry createContactEntry(Contact c, String groupId) throws ParseException, java.text.ParseException {
        ContactEntry entry = new ContactEntry();
        entry.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
        createName(entry, c);
        createMail(entry, c);
        createLinks(entry, c);
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
        return entry;
    }

    private ContactEntry updateContactEntry(ContactEntry entry, Contact c) {
        updateName(entry, c);
        updateMail(entry, c);
        return entry;
    }

    private void createName(ContactEntry entry, Contact c) {
        if (c.getLastName() == null) return;
        Name name = new Name();
        if(c.getSalutation()!=null) name.setNamePrefix(new NamePrefix(c.getSalutation()));
        if(c.getFirstName()!=null) name.setGivenName(new GivenName(c.getFirstName(), null));
        name.setFamilyName(new FamilyName(c.getLastName(), null));
        name.setFullName(new FullName(generateFullName(c), null));
        entry.setName(name);
    }

    private void updateName(ContactEntry entry, Contact c) {
        Name name = entry.getName();
        if (name == null) createName(entry, c);
        if (name.getNamePrefix()!=null && c.getSalutation()!=null) name.getNamePrefix().setValue(c.getSalutation());
        else if (name.getNamePrefix()==null && c.getSalutation()!=null) name.setNamePrefix(new NamePrefix(c.getSalutation()));
        else if (name.getNamePrefix()!=null && c.getSalutation()==null) name.setNamePrefix(null);
        if(name.getGivenName()!=null && c.getFirstName()!=null) name.getGivenName().setValue(c.getFirstName());
        else if (name.getGivenName()==null && c.getFirstName()!=null) name.setGivenName(new GivenName(c.getFirstName(),null));
        else if (name.getGivenName()!=null && c.getFirstName()==null) name.setGivenName(null);
        name.getFamilyName().setValue(c.getLastName());
        name.getFullName().setValue(generateFullName(c));
    }

    private String generateFullName(Contact c) {
        String fullname = "";
        if(c.getSalutation()!=null) fullname += c.getSalutation() + " ";
        if(c.getFirstName()!=null ) fullname += c.getFirstName() + " ";
        fullname += c.getLastName();
        return fullname;
    }

    private void createMail(ContactEntry entry, Contact c) {
        if (c.getEmail() == null) return;
        Email email = new Email();
        email.setAddress(c.getEmail());
        email.setDisplayName(generateFullName(c));
        email.setRel(Email.Rel.WORK);
        email.setPrimary(true);
        entry.addEmailAddress(email);
    }

    private void updateMail(ContactEntry entry, Contact c) {
        if (entry.getEmailAddresses().isEmpty()) createMail(entry, c);
        Email email = entry.getEmailAddresses().get(0);
        if(c.getEmail()==null) entry.getEmailAddresses().remove(0);
        else {
            email.setAddress(c.getEmail());
            email.setDisplayName(generateFullName(c));
        }
    }

    private void createLinks(ContactEntry entry, Contact c) {
        if (c.getId()!=null) {
            Website id = new Website();
            id.setHref(SALESFORCE_INSTANCE  + c.getId());
            id.setLabel(SALESFORCE_ID);
            id.setPrimary(true);
            entry.addWebsite(id);
        }
        if (c.getAccountWebsite()!=null) {
            Website home = new Website();
            home.setHref(c.getAccountWebsite());
            home.setLabel("Homepage");
            home.setPrimary(false);
            entry.addWebsite(home);
        }
    }

    private void updateWebsite(ContactEntry entry, Contact c) {
        //
    }

    private void createBirthday(ContactEntry entry, Contact c) {
        if (c.getBirthdate() == null) return;
        Birthday birthday = new Birthday();
        birthday.setWhen(c.getBirthdate());
        entry.setBirthday(birthday);
    }

    private void createLanguage(ContactEntry entry, Contact c) {
        if (c.getLanguages() == null) return;
        if (!Arrays.asList(Locale.getISOLanguages()).contains(c.getLanguages())) return;
        Language lang = new Language();
        lang.setCode(c.getLanguages());
        entry.addLanguage(lang);
    }

    private void createPhoneNumber(ContactEntry entry, String number, String schema, boolean primary) {
        if (number == null) return;
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setPhoneNumber(number);
        phoneNumber.setRel(schema);
        phoneNumber.setPrimary(primary);
        entry.addPhoneNumber(phoneNumber);
    }

    private void createPostalAddress(ContactEntry entry, Contact c) {
        String street = c.getMailingStreet();
        String city = c.getMailingCity();
        String plz = c.getMailingPostalCode();
        String country = c.getMailingCountry();
        StructuredPostalAddress postalAddress = new StructuredPostalAddress();
        if (street == null && city == null && plz == null && country == null) return;
        postalAddress.setStreet(new Street(street));
        postalAddress.setCity(new City(city));
        postalAddress.setPostcode(new PostCode(plz));
        Country cy = new Country();
        cy.setValue(country);
        postalAddress.setCountry(cy);
        postalAddress.setFormattedAddress(new FormattedAddress(createFormattedAddresss(street, plz, city, country)));
        postalAddress.setRel(StructuredPostalAddress.Rel.WORK);
        postalAddress.setPrimary(true);
        entry.addStructuredPostalAddress(postalAddress);
    }

    private String createFormattedAddresss(String street, String plz, String city, String country) {
        String address = "";
        if (street != null) address += street;
        if (plz != null) address += " " + plz;
        if (city != null) address += " " + city;
        if (country != null) address += " " + country;
        return address;
    }

    private void createTitle(ContactEntry entry, Contact c) {
        if (c.getSalutationTitle()==null) return;
        entry.setTitle(new PlainTextConstruct(c.getSalutationTitle()));
    }

    private void createManager(ContactEntry entry, Contact c) {
        if (c.getReportsToName() == null) return;
        Relation rel = new Relation();
        rel.setRel(Relation.Rel.MANAGER);
        rel.setValue(c.getReportsToName());
        entry.addRelation(rel);
    }

    private void createOrganization(ContactEntry entry, Contact c) {
        if (c.getAccountId() == null) return;
        Organization org = new Organization();
        org.setRel(Organization.Rel.WORK);
        if(c.getTitle()!=null) org.setOrgTitle(new OrgTitle(c.getTitle()));
        if(c.getAccountName()!=null) org.setOrgName(new OrgName(c.getAccountName()));
        entry.addOrganization(org);
    }

    private void createUserField(ContactEntry entry, String key, String value) {
        if(value==null) return;
        entry.addUserDefinedField(new UserDefinedField(key,value));
    }

    private String createContactGroup() throws IOException, ServiceException {
        ContactGroupEntry group = new ContactGroupEntry();
        group.setTitle(new PlainTextConstruct(GROUP_NAME));
        URL postUrl = new URL(GROUP_DEFAULT);
        ContactGroupEntry createdGroup = service.insert(postUrl, group);
        return createdGroup.getId();
    }

    private String getSalesForceGroupId() throws IOException, ServiceException {
        URL feedUrl = new URL(GROUP_DEFAULT);
        ContactGroupFeed resultFeed = service.getFeed(feedUrl, ContactGroupFeed.class);
        for (ContactGroupEntry groupEntry : resultFeed.getEntries()) {
            if (groupEntry.getTitle().getPlainText().equals(GROUP_NAME)) return groupEntry.getId();
        }
        return "";
    }
}
