package logic.gmail;

import com.google.gdata.client.Query;
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
import java.util.List;
import java.util.Locale;

public class GMailContactAccess {
    ContactsService service;
    private static final String GROUP_NAME = "salesforce";
    private static final String CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
    private static final String CONTACT_QUERY = "https://www.google.com/m8/feeds/contacts/default/full?q=";
    private static final String GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/default/full";
    private static final String SALESFORCE_INSTANCE = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE).getInstance() + "/";
    private static final String LAST_MODIFIED = "Last Modified";

    public GMailContactAccess() throws IOException {
        service = new GMailConnector().getContactService();
    }

    public void transferContacts(Container container) throws IOException, ServiceException, java.text.ParseException {
        String groupId = getSalesForceGroupId();
        if (groupId == "") groupId = createContactGroup();
        URL postUrl = new URL(CONTACT_FEED_URL);
        for (Contact contact : container.getContacts()) {
            ContactEntry entry = getContact(contact);
            if (entry == null) service.insert(postUrl, createContactEntry(contact, groupId));
            else if (hasNewValues(entry, contact)) service.update(postUrl, createContactEntry(contact, groupId));
        }
    }

    private ContactEntry getContact(Contact c) throws IOException, ServiceException {
        URL feedUrl = new URL(CONTACT_QUERY + SALESFORCE_INSTANCE + c.getId());
        Query myQuery = new Query(feedUrl);
        ContactFeed resultFeed = service.query(myQuery, ContactFeed.class);
        List<ContactEntry> resultList = resultFeed.getEntries();
        if (resultList.isEmpty()) return null;
        return resultFeed.getEntries().get(0);
    }

    private boolean hasNewValues(ContactEntry entry, Contact c) throws ServiceException, java.text.ParseException, IOException {
        String transferDate = EscDateTimeParser.parseSfDateToString((c.getLastModifiedDate()));
        String googleDate = null;
        for (UserDefinedField field : entry.getUserDefinedFields()) {
            if (field.getKey().equals(LAST_MODIFIED)) googleDate = field.getValue();
        }
        if (googleDate == null) return true;
        System.out.println(transferDate + "//" + googleDate);
        return !transferDate.equals(googleDate);
    }

    private ContactEntry createContactEntry(Contact c, String groupId) throws ParseException, java.text.ParseException {
        ContactEntry entry = new ContactEntry();
        entry.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
        createName(entry, c);
     //   createMail(entry, c);
        createLinks(entry, c);
        createBirthday(entry, c);
        createLanguage(entry, c);
        createPhoneNumber(entry, c.getPhone(), PhoneNumber.Rel.WORK, true);
        createPhoneNumber(entry, c.getMobilePhone(), PhoneNumber.Rel.MOBILE, false);
        createPostalAddress(entry, c);
        createTitle(entry, c);
        createManager(entry, c);
        createFcontact(entry, c);
        createOrganization(entry, c);
        createLastModified(entry, c);
        return entry;
    }

    private void createName(ContactEntry entry, Contact c) {
        if (c.getLastName() == null) return;
        Name name = new Name();
        if(c.getFirstName()!=null) {
            name.setGivenName(new GivenName(c.getFirstName(), null));
            name.setFullName(new FullName(c.getFirstName() + " " + c.getLastName(), null));
        } else { name.setFullName(new FullName(c.getLastName(), null)); }
        name.setFamilyName(new FamilyName(c.getLastName(), null));
        entry.setName(name);
    }

    private void updateName(ContactEntry entry, Contact c) {
        Name name = entry.getName();
        if (name == null) createName(entry, c);
        name.getFullName().setValue(c.getFirstName() + " " + c.getLastName());
        name.getGivenName().setValue(c.getFirstName());
        name.getFamilyName().setValue(c.getLastName());
    }

    private void createMail(ContactEntry entry, Contact c) {
        if (c.getEmail() == null) return;
        Email email = new Email();
        email.setAddress(c.getEmail());
        if (c.getFirstName() != null && c.getLastName() != null) email.setDisplayName(c.getFirstName() + " " + c.getLastName());
        email.setRel(Email.Rel.GENERAL);
        email.setPrimary(true);
        entry.addEmailAddress(email);
    }

    private void updateMail(ContactEntry entry, Contact c) {
        if (entry.getEmailAddresses().isEmpty()) createMail(entry, c);
        Email email = entry.getEmailAddresses().get(0);
        email.setDisplayName(c.getFirstName() + " " + c.getLastName());
        email.setAddress(c.getEmail());
    }

    private void createLinks(ContactEntry entry, Contact c) {
        if (c.getId()!=null) {
            Website id = new Website();
            id.setHref(SALESFORCE_INSTANCE  + c.getId());
            id.setLabel("Salesforce Id");
            id.setPrimary(true);
            entry.addWebsite(id);
        }
        if (true) {
            Website home = new Website();
            home.setHref("www.blabla.com");
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
        if (c.getReportsToId() == null) return;
        Relation rel = new Relation();
        rel.setRel(Relation.Rel.MANAGER);
        //Die ID ist hier nicht optimal
        rel.setValue(c.getReportsToId());
        entry.addRelation(rel);
    }

    private void createFcontact(ContactEntry entry, Contact c) {
        if (c.getF_contact() == null) return;
        entry.addUserDefinedField(new UserDefinedField("F-Contact", c.getF_contact()));
    }

    private void createOrganization(ContactEntry entry, Contact c) {
        if (c.getAccountId() == null) return;
        Organization org = new Organization();
        org.setRel(Organization.Rel.WORK);
        org.setOrgJobDescription(new OrgJobDescription(c.getTitle()));
        org.setOrgName(new OrgName(c.getAccountId()));
        entry.addOrganization(org);
    }

    private void createLastModified(ContactEntry entry, Contact c) throws ParseException, java.text.ParseException {
        if (c.getLastModifiedDate() == null) return;
        UserDefinedField field = new UserDefinedField();
        field.setKey(LAST_MODIFIED);
        field.setValue(EscDateTimeParser.parseSfDateToString(c.getLastModifiedDate()));
        entry.addUserDefinedField(field);
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
