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
    private static final String SCHEMA_WORK = "http://schemas.google.com/g/2005#work";
    private static final String SCHEMA_MOBILE = "http://schemas.google.com/g/2005#mobile";
    private static final String CONTACT_QUERY = "https://www.google.com/m8/feeds/contacts/default/full?q=";
    private static final String GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/default/full";
    private static final String SALESFORCE_INSTANCE = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE).getInstance()+"/";
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
            if(entry==null) service.insert(postUrl, createContactEntry(contact, groupId));
            else if(hasNewValues(entry, contact)) service.update(postUrl, createContactEntry(contact, groupId));
        }
    }

    private ContactEntry getContact(Contact c) throws IOException, ServiceException {
        URL feedUrl = new URL(CONTACT_QUERY + SALESFORCE_INSTANCE + c.getId());
        Query myQuery = new Query(feedUrl);
        ContactFeed resultFeed = service.query(myQuery, ContactFeed.class);
        List<ContactEntry> resultList = resultFeed.getEntries();
        if(resultList.isEmpty()) return null;
        return resultFeed.getEntries().get(0);
    }

    private boolean hasNewValues(ContactEntry entry, Contact c) throws ServiceException, java.text.ParseException, IOException {
        String transferDate = EscDateTimeParser.parseSfDateToString((c.getLastModifiedDate()));
        String googleDate = null;
        for(UserDefinedField field : entry.getUserDefinedFields()) {
            if (field.getKey().equals(LAST_MODIFIED)) googleDate = field.getValue();
        }
        if (googleDate==null) return true;
        System.out.println(transferDate + "//" + googleDate);
        return !transferDate.equals(googleDate);
    }

    private ContactEntry createContactEntry(Contact c, String groupId) throws ParseException, java.text.ParseException {
        ContactEntry entry = new ContactEntry();
        entry.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
        createName(entry, c);
        createMail(entry, c);
        createWebsite(entry, c);
        createBirthday(entry, c);
        createLanguage(entry, c);
        createPhoneNumber(entry, c.getPhone(), SCHEMA_WORK, true);
        createPhoneNumber(entry, c.getMobilePhone(), SCHEMA_MOBILE, false);
        createPostalAddress(entry, c);
        createTitle(entry, c);
        createManager(entry, c);
        createFcontact(entry, c);
        createOrganization(entry, c);
        createId(entry, c);
        createLastModified(entry ,c);
        return entry;
    }

    private void createName(ContactEntry entry, Contact c) {
        if (c.getFirstName() == null && c.getLastName()==null) return;
        Name name = new Name();
        final String NO_YOMI = null;
        name.setNamePrefix(new NamePrefix(c.getSalutation()));
        name.setFullName(new FullName(c.getFirstName() + " " + c.getLastName(), NO_YOMI));
        name.setGivenName(new GivenName(c.getFirstName(), NO_YOMI));
        name.setFamilyName(new FamilyName(c.getLastName(), NO_YOMI));
        entry.setName(name);
    }

    private void createMail(ContactEntry entry, Contact c) {
        if (c.getEmail()==null) return;
        Email email = new Email();
        email.setAddress(c.getEmail());
        if(c.getFirstName()!=null && c.getLastName()!=null) email.setDisplayName(c.getFirstName() + " " + c.getLastName());
        email.setRel(SCHEMA_WORK);
        email.setPrimary(true);
        entry.addEmailAddress(email);
    }

    private void createWebsite(ContactEntry entry, Contact c) {
        //Überprüfung ob wesbsite==null noch einfügen
        Website website = new Website();
        website.setHref("www.example.com");
        website.setPrimary(true);
        website.setRel(Website.Rel.WORK);
        entry.addWebsite(website);
    }

    private void createBirthday(ContactEntry entry, Contact c) {
        if (c.getBirthdate()==null)  return;
        Birthday birthday = new Birthday();
        birthday.setWhen(c.getBirthdate());
        entry.setBirthday(birthday);
    }

    private void createLanguage(ContactEntry entry, Contact c) {
        if(c.getLanguages()==null) return;
        if (!Arrays.asList(Locale.getISOLanguages()).contains(c.getLanguages())) return;
        Language lang = new Language();
        lang.setCode(c.getLanguages());
        entry.addLanguage(lang);
    }

    private void createPhoneNumber(ContactEntry entry, String number, String schema, boolean primary) {
        if (number==null) return;
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
        if(street==null && city==null && plz==null && country==null) return;
        postalAddress.setStreet(new Street(street));
        postalAddress.setCity(new City(city));
        postalAddress.setPostcode(new PostCode(plz));
        Country cy= new Country();
        cy.setValue(country);
        postalAddress.setCountry(cy);
        postalAddress.setFormattedAddress(new FormattedAddress(street + " " + plz + " " + city + " " + country));
        createFormattedAddresss(postalAddress, street, plz, city, country);
        postalAddress.setRel(SCHEMA_WORK);
        postalAddress.setPrimary(true);
        entry.addStructuredPostalAddress(postalAddress);
    }

    private void createFormattedAddresss(StructuredPostalAddress postalAddress, String street, String plz, String city, String country) {
        String address = "";
        if (street != null) address += street;
        if (plz != null) address += " " + plz;
        if(city != null) address += " " + city;
        if (country != null) address += " " + country;
        if (address != "") postalAddress.setFormattedAddress(new FormattedAddress(address));
    }

    private void createTitle(ContactEntry entry, Contact c)  {
        if(c.getTitle()==null) return;
        entry.setOccupation(new Occupation(c.getTitle()));
    }

    private void createManager(ContactEntry entry, Contact c) {
        if(c.getReportsToId()==null) return;
        Relation rel = new Relation();
        rel.setRel(Relation.Rel.MANAGER);
        //Die ID ist hier nicht optimal
        rel.setValue(c.getReportsToId());
        entry.addRelation(rel);
    }

    private void createFcontact(ContactEntry entry, Contact c) {
        if (c.getF_contact()==null) return;
        entry.addUserDefinedField(new UserDefinedField("F-Contact", c.getF_contact()));
    }

    private void createOrganization(ContactEntry entry, Contact c) {
        if (c.getAccountId()==null) return;
        Organization org = new Organization();
        org.setRel(Organization.Rel.WORK);
        org.setOrgJobDescription(new OrgJobDescription(c.getTitle()));
        org.setOrgName(new OrgName(c.getAccountId()));
        entry.addOrganization(org);
    }

    private void createId(ContactEntry entry, Contact c) {
        if(c.getId()==null) return;
        Website website = new Website();
        website.setHref(SALESFORCE_INSTANCE + c.getId());
        website.setPrimary(false);
        website.setLabel("Salesforce Id");
        entry.addWebsite(website);
    }

    private void createLastModified(ContactEntry entry, Contact c) throws ParseException, java.text.ParseException {
        if(c.getLastModifiedDate()==null) return;
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
