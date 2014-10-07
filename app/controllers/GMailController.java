package controllers;


import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.Name;
import models.gmail.GMailAccount;
import models.gmail.GMailConnector;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.gmail.gmail;

import java.net.URL;

public class GMailController extends Controller {

    static Form<GMailAccount> mailForm = Form.form(GMailAccount.class);



    public static Result index() {
        return ok(
                gmail.render(GMailAccount.all(), mailForm)
        );
    }

    public static Result newMail() {
        Form<GMailAccount> filledForm = mailForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(
                    gmail.render(GMailAccount.all(), filledForm)
            );
        } else {
            GMailAccount.create(filledForm.get());
            return ok(
                    gmail.render(GMailAccount.all(), mailForm)
            );
        }
    }

    public static Result deleteMail(Long id) {
        GMailAccount.delete(id);
        return ok(
                gmail.render(GMailAccount.all(), mailForm)
        );
    }

    public static Result authorize(Long id) {
        System.out.println("authorize");
        GMailConnector gmail = new GMailConnector(id);
        String authUrl = gmail.authorize();
        System.out.println(authUrl);
        return redirect(authUrl);
    }

    public static Result callback() {
        String code = request().getQueryString("code");
        Long id = Long.parseLong(request().getQueryString("state"));
        GMailConnector gmail = new GMailConnector(id);
        gmail.generateAccessToken(code);
        return redirect(routes.GMailController.index());
    }

    public static Result service(Long id)  {
        GMailConnector gmail = new GMailConnector(id);
        gmail.getContactService();
        return ok();
    }

    public static Result insertContact(Long id) throws Exception {
        ContactsService service = new GMailConnector(id).getContactService();
        GMailAccount account = GMailAccount.getAccount(id);

        // Create the entry to insert.
        ContactEntry contact = new ContactEntry();
        // Set the contact's name.
        Name name = new Name();
        final String NO_YOMI = null;
        name.setFullName(new FullName("haaalo neuer kontakt", NO_YOMI));
/*        name.setGivenName(new GivenName("Elizabeth", NO_YOMI));
        name.setFamilyName(new FamilyName("Bennet", NO_YOMI))*/
        contact.setName(name);
        // Set contact's e-mail addresses.
        Email primaryMail = new Email();
        primaryMail.setAddress("blabla@gmail.com");
        primaryMail.setDisplayName("blablaaaa");
        primaryMail.setRel("http://schemas.google.com/g/2005#home");
        primaryMail.setPrimary(true);
        contact.addEmailAddress(primaryMail);

        URL postUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
        ContactEntry createdContact = service.insert(postUrl, contact);
        System.out.println("Contact's ID: " + createdContact.getId());

        return redirect(routes.GMailController.index());
    }
}
