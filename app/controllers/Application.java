package controllers;

import com.google.gson.Gson;
import models.APIConfig;
import models.OAuth;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import play.data.Form;
import play.mvc.*;

import views.html.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class Application extends Controller {

    static Form<APIConfig> configForm = Form.form(APIConfig.class);

    public static Result index() {
        return ok(oauth.render(configForm));
    }

    public static Result oauth() {
        Form<APIConfig> filledForm = configForm.bindFromRequest();
        if(filledForm.hasErrors()) {
            return badRequest(
                    views.html.index.render("Fehler")
            );
        } else {
            APIConfig config = filledForm.get();
            APIConfig.create(config);
            String clientID = config.getClientID(); // "3MVG9A_f29uWoVQtLyx_TNRfuq85aLHcIwEjVXgIOrrBWS4P5jZ6APwPmjjutvbNelxFEvodl7fpesAk9JV1l";
            String redirectURI = config.getRedirectURI(); //"http://localhost:9000/oauth/callback";
            return redirect("https://login.salesforce.com/services/oauth2/authorize?response_type=code&client_id=" + clientID +
                    "&redirect_uri=" + redirectURI);
        }
    }

    public static Result callback() {

        String code = request().getQueryString("code");
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost post = new HttpPost("https://login.salesforce.com/services/oauth2/token");

        APIConfig config = APIConfig.getConfig(1L);

        String clientID = config.getClientID(); // "3MVG9A_f29uWoVQtLyx_TNRfuq85aLHcIwEjVXgIOrrBWS4P5jZ6APwPmjjutvbNelxFEvodl7fpesAk9JV1l";
        String redirectURI = config.getRedirectURI(); //"http://localhost:9000/oauth/callback";

        ArrayList<NameValuePair> postParameters = new ArrayList<NameValuePair>();

        postParameters.add(new BasicNameValuePair("code", code));
        postParameters.add(new BasicNameValuePair("grant_type", "authorization_code"));
        postParameters.add(new BasicNameValuePair("client_id", clientID)); //"3MVG9A_f29uWoVQtLyx_TNRfuq85aLHcIwEjVXgIOrrBWS4P5jZ6APwPmjjutvbNelxFEvodl7fpesAk9JV1l"));
        postParameters.add(new BasicNameValuePair("client_secret", "5039637056870495392"));
        postParameters.add(new BasicNameValuePair("redirect_uri", redirectURI)); //"http://localhost:9000/oauth/callback"));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParameters));
            HttpResponse response = httpClient.execute(post);

            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);

            Gson gson = new Gson();
            OAuth test = gson.fromJson(result, OAuth.class);

            return ok(
                    views.html.index.render("result: " + test.getAccess_token() + " *** " + test.getInstance_url() + " ***" + test.getToken_type())
            );
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ok(
                views.html.index.render("fail")
        );
    }

}
