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