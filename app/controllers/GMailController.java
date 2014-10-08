package controllers;


import com.google.gdata.client.contacts.ContactsService;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.data.extensions.Email;
import com.google.gdata.data.extensions.FullName;
import com.google.gdata.data.extensions.Name;
import models.APIConfig;
import models.ServiceProvider;
import models.gmail.GMailConnector;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.gmail.gmail;

import java.net.URL;

public class GMailController extends Controller {

    static Form<APIConfig> mailForm = Form.form(APIConfig.class);


    public static Result index() {
        return ok(gmail.render(APIConfig.all(), mailForm));
    }

    public static Result newMail() {
        Form<APIConfig> filledForm = mailForm.bindFromRequest();
        if (filledForm.hasErrors()) return badRequest(gmail.render(APIConfig.all(), filledForm));
        else {
            APIConfig account = filledForm.get();
            account.setProvider(ServiceProvider.GMAIL);
            APIConfig.create(account);
            return ok(gmail.render(APIConfig.all(), mailForm));
        }
    }

    public static Result deleteMail(Long id) {
        APIConfig.delete(id);
        return ok(gmail.render(APIConfig.all(), mailForm));
    }

    public static Result authorize(Long id) {
        GMailConnector gmail = new GMailConnector(id);
        return redirect(gmail.authorize());
    }

    public static Result callback() {
        GMailConnector gmail = new GMailConnector(Long.parseLong(request().getQueryString("state")));
        gmail.generateAccessToken(request().getQueryString("code"));
        return redirect(routes.GMailController.index());
    }

    public static Result insertContact(Long id) throws Exception {
        ContactsService service = new GMailConnector(id).getContactService();
        APIConfig account = APIConfig.getAPIConfig(id);

        // Create the entry to insert.
        ContactEntry contact = new ContactEntry();
        // Set the contact's name.
        Name name = new Name();
        final String NO_YOMI = null;
        name.setFullName(new FullName("newnewnen", NO_YOMI));
/*        name.setGivenName(new GivenName("Elizabeth", NO_YOMI));
        name.setFamilyName(new FamilyName("Bennet", NO_YOMI))*/
        contact.setName(name);
        // Set contact's e-mail addresses.
        Email primaryMail = new Email();
        primaryMail.setAddress("xxxx@gmail.com");
        primaryMail.setDisplayName("5555555555");
        primaryMail.setRel("http://schemas.google.com/g/2005#home");
        primaryMail.setPrimary(true);
        contact.addEmailAddress(primaryMail);

        URL postUrl = new URL("https://www.google.com/m8/feeds/contacts/default/full");
        ContactEntry createdContact = service.insert(postUrl, contact);
        System.out.println("Contact's ID: " + createdContact.getId());

        return redirect(routes.GMailController.index());
    }
}
