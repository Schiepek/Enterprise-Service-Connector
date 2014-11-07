import com.google.gdata.data.contacts.Birthday;
import com.google.gdata.data.contacts.ContactEntry;
import com.google.gdata.util.ServiceException;
import com.google.gson.Gson;
import logic.gmail.GMailContactAccess;
import models.APIConfig;
import models.ServiceProvider;
import models.Settings;
import models.gsonmodels.Container;
import models.gsonmodels.SalesforceContainer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.FakeApplication;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.start;



public class GmailTest {

    private static final String CONTACT_FEED_URL = "https://www.google.com/m8/feeds/contacts/default/full";

    protected EntityManager em;
    protected APIConfig config;
    protected Settings settings;
    private static final int TIME_TO_SLEEP_MS = 4000;

    @Before
    public void setUpIntegrationTest() {
        FakeApplication app = fakeApplication();
        start(app);
        em = app.getWrappedApplication().plugin(JPAPlugin.class).get().em("default");
        JPA.bindForCurrentThread(em);

        // Config for Gmail Account test.fyayc@gmail.com
        config = new APIConfig();
        config.setClientId("806785363902-r6ra00o651o2skfplosa6f37rhog9asn.apps.googleusercontent.com");
        config.setClientSecret("aA22bOel1pJ_2JH80KIKMnH-");
        config.setRefreshToken("1/lUnvzxVjenoUeVC24XY6iw7IpH_mlQ0qXxVdOuY6udh90RDknAdJa_sgfheVM0XT");
        config.setProvider(ServiceProvider.GMAIL);

        settings = new Settings();
        settings.setSaveInDirectory(false);
        settings.setServerUrl("http://localhost:9000");
    }

    @After
    public void tearDownIntegrationTest() {
        JPA.bindForCurrentThread(null);
        em.close();
    }

    @Test
    public void deleteContactsTest() {
        JPA.withTransaction(new play.libs.F.Callback0() {
            public void invoke() throws IOException, ServiceException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {

                GMailContactAccess access = new GMailContactAccess(config, settings);
                access.deleteContacts();

                // Sleep because API is to slow to refresh data
                Thread.sleep(TIME_TO_SLEEP_MS);

                URL feedUrl = new URL(CONTACT_FEED_URL);
                // use reflection because of private Method getAllContacts
                Class[] cArg = new Class[1];
                cArg[0] = URL.class;
                Method method = GMailContactAccess.class.getDeclaredMethod("getAllContacts", cArg);
                method.setAccessible(true);
                HashMap<String, ContactEntry> contacts = (HashMap<String, ContactEntry>) method.invoke(new GMailContactAccess(config, settings), feedUrl);

                assertThat(contacts.size()).isEqualTo(0);
            }
        });
    }

    @Test
    public void transferContactsTest() {
        JPA.withTransaction(new play.libs.F.Callback0() {
            public void invoke() throws IOException, ServiceException, ParseException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
                GMailContactAccess access = new GMailContactAccess(config, settings);
                access.deleteContacts();

                // Sleep because API is to slow to refresh data
                Thread.sleep(TIME_TO_SLEEP_MS);

                String json = "{  " +
                        "\"totalSize\":1," +
                        "\"done\":true," +
                        "\"records\":[  " +
                        "{  " +
                            "\"attributes\":{  " +
                                "\"type\":\"Contact\"," +
                                "\"url\":\"/services/data/v20.0/sobjects/Contact/003w000001GDZE9AAP\"" +
                            "}," +
                            "\"LastName\":\"Meier\"," +
                            "\"FirstName\":\"Hans\"," +
                            "\"Email\":\"hans.meier@foryouandyourcustomers.com\"," +
                            "\"Title\":\"null\"," +
                            "\"Salutation_Title__c\":null," +
                            "\"Birthdate\":\"1962-09-06\"," +
                            "\"Languages__c\":\"English\"," +
                            "\"Phone\":\"(512) 757-6000\"," +
                            "\"MobilePhone\":\"(512) 757-9340\"," +
                            "\"ReportsToId\":null," +
                            "\"AccountId\":\"001w000001AjnNiAAJ\"," +
                            "\"MailingStreet\":\"313 Constitution Place\\nAustin, TX 78767\\nUSA\"," +
                            "\"MailingCity\":null," +
                            "\"MailingPostalCode\":null," +
                            "\"MailingCountry\":null," +
                            "\"f_contact__c\":null," +
                            "\"OwnerId\":\"005w0000003ZX6SAAW\"," +
                            "\"Id\":\"003w000001GDQE9AAP\"," +
                            "\"Account\":{  " +
                                "\"attributes\":{  " +
                                    "\"type\":\"Account\"," +
                                    "\"url\":\"/services/data/v20.0/sobjects/Account/001w010001AjnNiAAJ\"" +
                                "}," +
                                "\"Name\":\"Edge Communications\"," +
                                "\"Website\":\"http://edgecomm.com\"," +
                                "\"Phone\":\"(512) 757-6000\"" +
                            "}," +
                            "\"ReportsTo\":null," +
                            "\"LastModifiedDate\":\"2014-09-24T08:50:23.000+0000\"," +
                            "\"Owner\":{  " +
                                "\"attributes\":{  " +
                                    "\"type\":\"User\"," +
                                    "\"url\":\"/services/data/v20.0/sobjects/User/005w0010003ZX6SAAW\"" +
                                "}," +
                                "\"FirstName\":\"Hans\"," +
                                "\"LastName\":\"Meier\"" +
                            "}" +
                        "}" +
                        "]" +
                        "}";
                Gson gson = new Gson();
                SalesforceContainer container = gson.fromJson(json, SalesforceContainer.class);
                access.transferContacts(container);

                // Sleep because API is to slow to refresh data
                Thread.sleep(TIME_TO_SLEEP_MS);

                URL feedUrl = new URL(CONTACT_FEED_URL);
                // use reflection because of private Method getAllContacts
                Class[] cArg = new Class[1];
                cArg[0] = URL.class;
                Method method = GMailContactAccess.class.getDeclaredMethod("getAllContacts", cArg);
                method.setAccessible(true);
                HashMap<String, ContactEntry> contacts = (HashMap<String, ContactEntry>) method.invoke(new GMailContactAccess(config, settings), feedUrl);

                assertThat(contacts.size()).isEqualTo(1);

                Iterator it = contacts.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pairs = (Map.Entry)it.next();
                    ContactEntry entry = (ContactEntry) pairs.getValue();
                    Birthday birthday = new Birthday();
                    birthday.setWhen("1962-09-06");
                    assertThat(entry.getBirthday()).isEqualTo(birthday);
                }
            }
        });

    }


}
