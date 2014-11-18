import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import logic.confluence.ConfluenceDataImport;
import logic.general.ServiceDataImport;
import logic.gmail.GMailDataImport;
import logic.jira.JiraDataImport;
import models.*;
import org.junit.Before;
import org.junit.Test;
import play.Configuration;
import play.db.jpa.JPA;

import java.io.File;
import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class ServiceDataImportTest {

    private APIConfig gmailConfig;
    private APIConfig salesforceConfig;
    private APIConfig confluenceConfig;
    private APIConfig jiraConfig;
    private Settings settings;
    private Configuration additionalConfigurations;

    @Before
    public void initialize(){
        Config additionalConfig = ConfigFactory.parseFile(new File("conf/test.conf"));
        additionalConfigurations = new Configuration(additionalConfig);

        // Config for Gmail Account test@darioandreoli.ch
        gmailConfig = new APIConfig();
        gmailConfig.setClientId("671544857019-rsv3s7g7j1b9a4l6c0e204m2dc7mndsd.apps.googleusercontent.com");
        gmailConfig.setClientSecret("nL8BiPbnRdL8tWNQP81KDvry");
        gmailConfig.setRefreshToken("1/0v0xt5TtPHGpOOfbsDOaFUQZB_FHu27bOtkUume30FkMEudVrK5jSpoR30zcRFq6");
        gmailConfig.setProvider(ServiceProvider.GMAIL);

        // Config for Salesforce Account zurich@foryouandyourcustomers.com
        salesforceConfig = new APIConfig();
        salesforceConfig.setClientId("3MVG9PhR6g6B7ps76O5Ij_Y.13u1J4J5Utg7OxIBqZYvKqDND9BNIjFY1OeYjAuCzbw7qf8pmTPEpyvnPmUO6");
        salesforceConfig.setClientSecret("3774273860981867577");
        salesforceConfig.setRefreshToken("5Aep8615VRsd_GrUz2E1IJHKMA6iPjpDjKqROv8YG0VR.Yjl98GzqTiojHjVrzaY3GVFLTloPNBu06mQnGvyHgK");
        salesforceConfig.setInstance("https://eu1.salesforce.com");
        salesforceConfig.setProvider(ServiceProvider.SALESFORCE);

        // Config for Confluence Account hsr.stu of https://foryouandyourteam.com
        confluenceConfig = new APIConfig();
        confluenceConfig.setClientId("hsr.stu");
        confluenceConfig.setClientSecret("8xJw0is//VW7r3n+y0/NtQ==");
        confluenceConfig.setProvider(ServiceProvider.CONFLUENCE);

        // Config for Jira Account
        jiraConfig = new APIConfig();
        jiraConfig.setAccessToken("QPg237AWVc2Vv1TPL7xeiulgof5zlUhZ");
        jiraConfig.setClientId("esc_Spotify2018");
        jiraConfig.setClientSecret("MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBAPCa1xHyz8klqKBUpxQ5JOueWY5H18HPqFikkqWhjm/rKTsqrshTLt51MNI87DfGY7PCoZds+Se2sR3251LLRQEQc1aB7x4emufVQBga7eDerDuOAXAqNE75ezaPua22Ok12HVZG2KPJEPA7d6jgsDjCt+cXrHDORWOZxAZy2J0hAgMBAAECgYB/eWntJnoUFhGrMG589nGrK/BCw6l6E60bmcXgXyH6BduIb2q+0+OHFQvSt1hnjIKSYNIASkoPUmmweHnCkdNyX+XtIgzvWKA87OuH4UgAl8x4O1WHKyu+NI7/Ss8QqeEPG7Boi8Q9QoZvinad7ozW9kr8Zm5NvOsW5ntwezoMqQJBAPtYQXG6hOf8WiOvBM5XwwEGQDoHyRKEbtEwgHwDe3q3b6WNvS0kCiGGxN5t4g8tQs6EDRRHaRJIF4ps/a7lGoMCQQD1D6lYnv49RdE0o0fJsJJFERvnGELU1osd4yN6C0/wJeM4iVCM4NgvcDOk1XS8OlO6aiAalZ20KwoilKJ4OmiLAkEA6iLqxADWciy342nUdkt20mt6RRSfkGphuOkPp3au/kAG9xe0VGqwLU8X8YQ3A6BMDYkhjfR/wpH5q++f2d599QJAZS1sK6ypJz8WGvd3Aiyml2Fy9bysixdxJIvM7+yPuoTKBWOcIC4M07kteVav9a7XNtXuH0u6atKCQri/Q4hJ9QJBAPOfnN6weRJzxQOGR6XN5Uv9hwtn5NvWgfw0F3ugwbvqKGbQGR0yucpz8WlfkF2ITI+5YafDxuysGC+p/i6Tdvc=");
        jiraConfig.setProvider(ServiceProvider.JIRA);

        settings = new Settings();
        settings.setConfluenceUrl("https://foryouandyourteam.com");
        settings.setJiraUrl("https://jira.foryouandyourteam.com");
        settings.setServerUrl("http://localhost:9000");
    }

    @Test
    public void deleteAllDataTest() {
        running(fakeApplication(additionalConfigurations.asMap()), () -> {
            JPA.withTransaction(() -> {
                new ServiceUser("Hans Muster", "hans.muster@gmail.com", "hans.muster@gmail.com", ServiceProvider.GMAIL, new String[0]).save();
                assertThat(ServiceUser.all().size()).isEqualTo(1);

                Method method = ServiceDataImport.class.getDeclaredMethod("deleteData");
                method.setAccessible(true);
                method.invoke(new ServiceDataImport());

                assertThat(ServiceUser.all().size()).isEqualTo(0);
            });
        });
    }

    @Test
    public void importGMailDataTest() {
        running(fakeApplication(additionalConfigurations.asMap()), () -> {
            JPA.withTransaction(() -> {
                gmailConfig.save();
                salesforceConfig.save();

                assertThat(ServiceUser.all().size()).isEqualTo(0);
                assertThat(ServiceGroup.all().size()).isEqualTo(0);
                assertThat(ServiceAlias.all().size()).isEqualTo(0);

                new GMailDataImport(gmailConfig).importData();

                assertThat(ServiceUser.all().size()).isGreaterThan(0);
                assertThat(ServiceGroup.all().size()).isGreaterThan(0);
                assertThat(ServiceAlias.all().size()).isGreaterThan(0);
            });
        });

    }

    @Test
     public void importConfluenceDataTest() {
        running(fakeApplication(additionalConfigurations.asMap()), () -> {
            JPA.withTransaction(() -> {
                confluenceConfig.save();
                salesforceConfig.save();
                settings.save();

                assertThat(ServiceUser.all().size()).isEqualTo(0);
                assertThat(ServiceGroup.all().size()).isEqualTo(0);
                assertThat(ServiceAlias.all().size()).isEqualTo(0);

                new ConfluenceDataImport().importData();

                assertThat(ServiceUser.all().size()).isGreaterThan(0);
                assertThat(ServiceGroup.all().size()).isGreaterThan(0);
                assertThat(ServiceAlias.all().size()).isEqualTo(0);
            });
        });

    }

    @Test
    public void importJiraDataTest() {
        running(fakeApplication(additionalConfigurations.asMap()), () -> {
            JPA.withTransaction(() -> {
                jiraConfig.save();
                salesforceConfig.save();
                settings.save();

                assertThat(ServiceUser.all().size()).isEqualTo(0);
                assertThat(ServiceGroup.all().size()).isEqualTo(0);
                assertThat(ServiceAlias.all().size()).isEqualTo(0);

                new JiraDataImport().importData();

                assertThat(ServiceUser.all().size()).isGreaterThan(0);
                assertThat(ServiceGroup.all().size()).isGreaterThan(0);
                assertThat(ServiceAlias.all().size()).isEqualTo(0);
            });
        });

    }
}
