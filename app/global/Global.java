package global;

import com.atlassian.connect.play.java.play.AcGlobalSettings;
import models.Logging;
import play.db.jpa.JPA;
import play.mvc.*;
import play.mvc.Http.*;
import play.libs.F.*;
import views.html.error;

import java.util.Date;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;


public class Global extends AcGlobalSettings {

    @Override
    public Promise<SimpleResult> onError(RequestHeader request, Throwable t) {
        if (t.getCause() instanceof TransferException) {
            JPA.withTransaction(() -> {
                Logging log = new Logging();
                log.setDate(new Date());
                log.setMessage(t.getMessage());
                log.save();
            });

        }
        return super.onError(request, t);
    }

    public Promise<SimpleResult> onHandlerNotFound(RequestHeader request) {
        return Promise.<SimpleResult>pure(notFound(
                error.render()
        ));
    }

    public Promise<SimpleResult> onBadRequest(RequestHeader request, String error) {
        return Promise.<SimpleResult>pure(badRequest("Don't try to hack the URI!"));
    }


}