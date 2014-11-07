package logic.confluence;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Richard on 07.11.2014.
 */
public class ConfluenceAccess {


    public void exampleRequest() throws IOException {
        String url = "https://foryouandyourteam.com/rpc/json-rpc/confluenceservice-v2";

        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);

        String userCredentials = "hsr.stu:fyayc12345678";
        String basicAuth = "Basic " + new String(Base64.encodeBase64(userCredentials.getBytes()));

        post.setHeader("Authorization", basicAuth);
        post.setHeader("hsr.stu", "fyayc12345678");
        post.setHeader("Content-Type","application/json");

        String param = "{ \"jsonrpc\" : \"2.0\",\"method\" : \"getUser\",\"params\" : [ \"hsr.stu\" ],\"id\" : 123453333 }";
        StringEntity params = new StringEntity(param);
        post.setEntity(params);

        HttpResponse response = client.execute(post);
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + post.getEntity());
        System.out.println("Response Code : " +
                response.getStatusLine().getStatusCode());

        BufferedReader rd = new BufferedReader(
                new InputStreamReader(response.getEntity().getContent()));

        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }

        System.out.println(result.toString());
    }

}
