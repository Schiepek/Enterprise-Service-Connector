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

    public GMailContactAccess() throws IOException {
        service = new GMailConnector().getContactService();
    }

    public void insertContact(String firstname, String lastname, String mail) throws IOException, ServiceException {
        String groupId = getSalesForceGroupId();
        if (groupId == "") groupId = createContactGroup();
        if(contactExists(mail) || !isValidEmail(mail)) return;
        ContactEntry contact = createContactEntry(firstname, lastname, mail, groupId);
        URL postUrl = new URL(CONTACT_FEED_URL);
        ContactEntry createdContact = service.insert(postUrl, contact);
        System.out.println("Contact's ID: " + createdContact.getId());
    }

    public void insertContact(Contact contact) throws IOException, ServiceException {
        String groupId = getSalesForceGroupId();
        if (groupId == "") groupId = createContactGroup();
        if(!contactInsertAllowed(contact)) return;
        ContactEntry newcontact = createContactEntry(contact.getFirstName(), contact.getLastName(), contact.getEmail(), groupId);
        URL postUrl = new URL(CONTACT_FEED_URL);
        ContactEntry createdContact = service.insert(postUrl, newcontact);
        System.out.println("Contact's ID: " + createdContact.getId());
    }

    public void insertContacts(Container container) throws IOException, ServiceException {
        String groupId = getSalesForceGroupId();
        if (groupId == "") groupId = createContactGroup();
        URL postUrl = new URL(CONTACT_FEED_URL);
        for(Contact contact : container.getContacts()) {
            System.out.println(contact.getEmail());
            if(!contactInsertAllowed(contact)) continue;
            ContactEntry newcontact = createContactEntry(contact.getFirstName(), contact.getLastName(), contact.getEmail(), groupId);
            ContactEntry createdContact = service.insert(postUrl, newcontact);
            //service.insert
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
        URL feedUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full?q=" + mail);
        Query myQuery = new Query(feedUrl);
        ContactFeed resultFeed = service.query(myQuery, ContactFeed.class);
        return !resultFeed.getEntries().isEmpty();
    }


    private ContactEntry createContactEntry(String firstname, String lastname, String mail, String groupId) {
        ContactEntry contact = new ContactEntry();
        contact.setName(createName(firstname, lastname));
        contact.addEmailAddress(createMail(firstname, lastname, mail));
        contact.addGroupMembershipInfo(new GroupMembershipInfo(false, groupId));
        return contact;
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
        email.setRel("http://schemas.google.com/g/2005#home");
        email.setPrimary(true);
        return email;
    }

    private String createContactGroup() throws IOException, ServiceException {
        ContactGroupEntry group = new ContactGroupEntry();
        group.setTitle(new PlainTextConstruct(GROUP_NAME));
        URL postUrl = new URL("https://www.google.com/m8/feeds/groups/default/full");
        ContactGroupEntry createdGroup = service.insert(postUrl, group);
        return createdGroup.getId();
    }

    private String getSalesForceGroupId() throws IOException, ServiceException {
        URL feedUrl = new URL("https://www.google.com/m8/feeds/groups/default/full");
        ContactGroupFeed resultFeed = service.getFeed(feedUrl, ContactGroupFeed.class);
        for (ContactGroupEntry groupEntry : resultFeed.getEntries()) {
            if (groupEntry.getTitle().getPlainText().equals(GROUP_NAME)) return groupEntry.getId();
        }
        return "";
    }
}
