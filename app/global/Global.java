package global;

import com.atlassian.connect.play.java.play.AcGlobalSettings;
import models.Logging;
import play.db.jpa.JPA;
import play.libs.F.Promise;
import play.mvc.Http.RequestHeader;
import play.mvc.SimpleResult;
import views.html.error;
import views.html.notFound;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;


public class Global extends AcGlobalSettings {

    @Override
    public Promise<SimpleResult> onError(RequestHeader request, Throwable t) {
        if (play.api.Play.isDev(play.api.Play.current())) {
            return super.onError(request, t);
        }
        JPA.withTransaction(() -> Logging.log(t.getMessage()));
        return Promise.<SimpleResult>pure(notFound(
                error.render()
        ));
    }

    public Promise<SimpleResult> onHandlerNotFound(RequestHeader request) {
        return Promise.<SimpleResult>pure(notFound(
                notFound.render()
        ));
    }

    public Promise<SimpleResult> onBadRequest(RequestHeader request, String error) {
        return Promise.<SimpleResult>pure(badRequest("Don't try to hack the URI!"));
    }


}