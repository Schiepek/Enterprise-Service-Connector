package global;

import akka.actor.Cancellable;
import com.atlassian.connect.play.java.play.AcGlobalSettings;
import models.Logging;
import models.Settings;
import play.Application;
import play.db.jpa.JPA;
import play.libs.Akka;
import play.libs.F.Promise;
import play.libs.Time;
import play.mvc.Http.RequestHeader;
import play.mvc.SimpleResult;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;
import views.html.error;
import views.html.notFound;

import javax.jdo.annotations.Transactional;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;


public class Global extends AcGlobalSettings {

    private static Cancellable scheduler;

    @Override
    public void onStart(Application application) {
        super.onStart(application);
        schedule();
    }

    @Override
    public void onStop(Application application) {
        if (scheduler != null) {
            scheduler.cancel();
        }
    }

    @Transactional
    private static void schedule() {
        try {
            String newCronExpression = JPA.withTransaction((() -> Settings.getSettings().getCronExpression()));
            Time.CronExpression e = new Time.CronExpression(newCronExpression);
            Date nextValidTimeAfter = e.getNextValidTimeAfter(new Date());
            FiniteDuration d = Duration.create(
                    nextValidTimeAfter.getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS);
            final String finalNewCronExpression = newCronExpression;
            scheduler = Akka.system().scheduler().scheduleOnce(d, (Runnable) () -> {
                JPA.withTransaction(() -> Logging.log("Scheduled Task executed"));
                //TODO Invoke GoogleContactstransfer and importGroupData
                schedule();
            }, Akka.system().dispatcher());
        } catch (Throwable t) {
            JPA.withTransaction(() -> Logging.log("Scheduling Error: " + t.getMessage()));
        }
    }

    private static void reschedule(String newCronExpression) {
        try {
            Time.CronExpression e = new Time.CronExpression(newCronExpression);
            Date nextValidTimeAfter = e.getNextValidTimeAfter(new Date());
            FiniteDuration d = Duration.create(
                    nextValidTimeAfter.getTime() - System.currentTimeMillis(),
                    TimeUnit.MILLISECONDS);
            final String finalNewCronExpression = newCronExpression;
            scheduler = Akka.system().scheduler().scheduleOnce(d, (Runnable) () -> {
                Logging.log("Scheduled Task executed");
                //TODO Invoke GoogleContactstransfer and importGroupData
                schedule();
            }, Akka.system().dispatcher());
        } catch (Throwable t) {
            Logging.log("Scheduling Error: " + t.getMessage());
        }
    }

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

    public static void setNewScheduler(String newCronExpression) {
        Logging.log("Scheduling time changed to: " + newCronExpression);
        reschedule(newCronExpression);
    }
}