package controllers;

import models.Settings;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.settings.settings;

public class SettingsController extends Controller {
    static Form<Settings> settingsForm = Form.form(Settings.class);

    public static Result index() {
        return ok(settings.render(Settings.getSettings(), settingsForm));
    }

    public static Result setSettings() {
        Form<Settings> filledForm = settingsForm.bindFromRequest();
        if (filledForm.hasErrors()) {
            return badRequest(settings.render(Settings.getSettings(), settingsForm));
        } else {
            Settings settings = filledForm.get();
            Settings.create(settings);
            return index();
        }
    }
}
