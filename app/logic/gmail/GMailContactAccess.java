package logic.gmail;

import com.google.gdata.client.Query;
import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.contacts.ContactFeed;
import com.google.gdata.data.contacts.*;
import com.google.gdata.data.extensions.*;
import com.google.gdata.util.ServiceException;
import models.Contact;
import models.Container;
import play.data.validation.Constraints.EmailValidator;

import java.io.IOException;
import java.net.URL;

public class GMailContactAccess {
    ContactsService service;
    private static final String GROUP_NAME = "salesforce";
    private static final String CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";
    private static final String SCHEMA_WORK = "http://schemas.google.com/g/2005#work";
    private static final String SCHEMA_MOBILE = "http://schemas.google.com/g/2005#mobile";
    private static final String CONTACT_QUERY = "https://www.google.com/m8/feeds/contacts/default/full?q=";
    private static final String GROUP_DEFAULT = "https://www.google.com/m8/feeds/groups/default/full";

    public GMailContactAccess() throws IOException {
        service = new GMailConnector().getContactService();
    }

    public void insertContacts(Container container) throws IOException, ServiceException {
        String groupId = getSalesForceGroupId();
        if (groupId == "") groupId = createContactGroup();
        URL postUrl = new URL(CONTACT_FEED_URL);
        for (Contact contact : container.getContacts()) {
            System.out.println(contact.getEmail());
            if (!contactInsertAllowed(contact)) continue;
            ContactEntry newcontact = createContactEntry(contact, groupId);
            ContactEntry createdContact = service.insert(postUrl, newcontact);
            System.out.println("Contact's ID: " + createdContact.getId());
        }
    }

    private boolean contactInsertAllowed(Contact contact) throws IOException, ServiceException {
        return !contactExists(contact.getEmail()) && isValidEmail(contact.getEmail());
    }

    private boolean isValidEmail(String mail) {
        return new EmailValidator().isValid(mail) && mail != null;
    }

    private boolean contactExists(String mail) throws IOException, ServiceException {
        URL feedUrl = new URL(CONTACT_QUERY + mail);
        Query myQuery = new Query(feedUrl);
        ContactFeed resultFeed = service.query(myQuery, ContactFeed.class);
        return !resultFeed.getEntries().isEmpty();
    }


    private ContactEntry createContactEntry(Contact c, String groupId) {
        ContactEntry entry = new ContactEntry();
        entry.setName(createName(c.getFirstName(), c.getLastName()));
        entry.addEmailAddress(createMail(c.getFirstName(), c.getLastName(), c.getEmail()));
        entry.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
        entry.addWebsite(createWebsite("http://www.example.com"));
        if(c.getBirthdate() != null) entry.setBirthday(createBirthday(c.getBirthdate()));
        if(c.getPhone() != null) entry.addPhoneNumber(createPhoneNumber(c.getPhone(), SCHEMA_WORK, true));
        if(c.getMobilePhone() != null) entry.addPhoneNumber(createPhoneNumber(c.getMobilePhone(), SCHEMA_MOBILE, false));
        //contact.addStructuredPostalAddress(createPostalAddress(c.getMailingStreet(), c.getMailingCity(), c.getMailingPostalCode(), c.getMailingCountry()));
        createPostalAddress(entry, c);
        return entry;
    }

    private Name createName(String firstname, String lastname) {
        Name name = new Name();
        final String NO_YOMI = null;
        name.setFullName(new FullName(firstname + " " + lastname, NO_YOMI));
        name.setGivenName(new GivenName(firstname, NO_YOMI));
        name.setFamilyName(new FamilyName(lastname, NO_YOMI));
        return name;
    }

    private Email createMail(String firstname, String lastname, String mail) {
        Email email = new Email();
        email.setAddress(mail);
        email.setDisplayName(firstname + " " + lastname);
        email.setRel(SCHEMA_WORK);
        email.setPrimary(true);
        return email;
    }

    private Website createWebsite(String href) {
        Website website = new Website();
        website.setHref(href);
        website.setPrimary(true);
        website.setRel(Website.Rel.WORK);
        return website;
    }

    private Birthday createBirthday(String date) {
        Birthday birthday = new Birthday();
        birthday.setWhen(date);
        return birthday;
    }

    private PhoneNumber createPhoneNumber(String number, String schema, boolean primary) {
        if (number == null) return new PhoneNumber();
        PhoneNumber phoneNumber = new PhoneNumber();
        phoneNumber.setPhoneNumber(number);
        phoneNumber.setRel(schema);
        phoneNumber.setPrimary(primary);
        return phoneNumber;
    }

    private void createPostalAddress(ContactEntry entry, Contact c) {
        String street = c.getMailingStreet();
        String city = c.getMailingCity();
        String plz = c.getMailingPostalCode();
        String country = c.getMailingCountry();
        StructuredPostalAddress postalAddress = new StructuredPostalAddress();
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
