import org.junit.Test;
import play.mvc.Result;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.*;

public class ControllerTest {

    @Test
    public void callIndex() {
        running(fakeApplication(), () -> {
            Result result = route(fakeRequest("GET", "/"));
            assertThat(status(result)).isEqualTo(OK);
            assertThat(contentType(result)).isEqualTo("text/html");
        });
    }

    @Test
    public void badRequest() {
        running(fakeApplication(), () -> {
            Result result = route(fakeRequest("GET", "/nothere"));
            assertThat(result).isNull();
        });
    }

    @Test
    public void callUserIndex() {
        running(fakeApplication(), () -> {
            Result result = route(fakeRequest("GET", "/users"));
            assertThat(status(result)).isEqualTo(OK);
            assertThat(contentType(result)).isEqualTo("text/html");
        });
    }

    @Test
    public void callLogIndex() {
        running(fakeApplication(), () -> {
            Result result = route(fakeRequest("GET", "/logs"));
            assertThat(status(result)).isEqualTo(OK);
            assertThat(contentType(result)).isEqualTo("text/html");
        });
    }

}


//GET         /salesforce/callback                 controllers.AccountController.callbackSalesForce()
//        GET         /gmail/callback                      controllers.AccountController.callbackGmail()
//        GET         /jira/callback                       controllers.AccountController.callbackJira()
//        GET         /confluence/callback                 controllers.AccountController.callbackConfluence()
//
//        GET         /accounts                            controllers.AccountController.index
//        GET         /accounts/authorize/:provider        controllers.AccountController.authorize(provider: String)
//        POST        /accounts/save/:provider             controllers.AccountController.save(provider: String)
//        POST        /accounts/transfer                   controllers.AccountController.transferContacts()
//        POST        /accounts/delete                     controllers.AccountController.deleteContacts()
//        POST        /accounts/settings                   controllers.AccountController.setSettings()
//        GET         /accounts/check/:provider            controllers.AccountController.checkStatus(provider: String)
//
//        # routes for user and group views
//        GET         /users                               controllers.UserController.users()
//        GET         /users/companies                     controllers.UserController.companies()
//        GET         /users/services                     controllers.UserController.services()
//
//        GET        /users/import                        controllers.UserController.importView()
//        POST        /users/import/start                  controllers.UserController.importData()
//
//
//
//        GET         /logs                                controllers.LoggingController.index()