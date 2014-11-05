
import logic.salesforce.SalesForceAccess;
import models.Contact;
import models.Container;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.junit.*;
import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.test.*;
import javax.persistence.EntityManager;

import java.lang.reflect.Field;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class SalesforceTest {

    protected EntityManager em;

    @Before
    public void setUpIntegrationTest() {
        FakeApplication app = fakeApplication();
        start(app);
        em = app.getWrappedApplication().plugin(JPAPlugin.class).get().em("default");
        JPA.bindForCurrentThread(em);
    }

    @After
    public void tearDownIntegrationTest() {
        JPA.bindForCurrentThread(null);
        em.close();
    }

    @Test
    public void getContactTest() {
        JPA.withTransaction(new play.libs.F.Callback0() {
            public void invoke() throws OAuthProblemException, OAuthSystemException, NoSuchFieldException, IllegalAccessException {
                Container container = new SalesForceAccess().getSalesforceContacts();

                // Use reflection because of private Field totalSize of Container class
                Field field = Container.class.getDeclaredField("totalSize");
                field.setAccessible(true);
                String totalSize = (String) field.get(container);
                int size = Integer.parseInt(totalSize);

                assertThat(size).isGreaterThan(0);
                assertThat(size).isEqualTo(container.getContacts().length);

                Contact[] contacts = container.getContacts();

                boolean found = false;
                for(Contact c : contacts){
                    if(c.getFirstName() != null && c.getBirthdate() != null && c.getFirstName().equals("Peter") && c.getBirthdate().equals("1979-10-19") ) {
                        found = true;
                    }
                }
                assertThat(found).isEqualTo(true);

            }
        });
    }


}
