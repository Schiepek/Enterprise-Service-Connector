package models;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.Thread;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GMailConnector {

    // Check https://developers.google.com/gmail/api/auth/scopes for all available scopes
    private static final String SCOPE = "https://www.googleapis.com/auth/gmail.modify";
    private static final String APP_NAME = "My Project";
    // Email address of the user, or "me" can be used to represent the currently authorized user.
    private static final String USER = "me";
    // Path to the client_secret.json file downloaded from the Developer Console
    private static final String CLIENT_ID = "806785363902-r6ra00o651o2skfplosa6f37rhog9asn.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "aA22bOel1pJ_2JH80KIKMnH-";
    private static final String CALLBACK_URI = "http://localhost:9000/gmail/callback";

    private static final String AUTH_CODE = "4/hNSdPHg8xpT2pZ7ZvZbHONd2Nb1_.cmjNUuwTjx8boiIBeO6P2m9qq3ZxkQI";

    public void authorize() {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            // Allow user to authorize via url.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(SCOPE))
                    .setAccessType("online")
                    .setApprovalPrompt("auto").build();

            String url = flow.newAuthorizationUrl().setRedirectUri(CALLBACK_URI).build();

            System.out.println("Please open the following URL in your browser then type"
                    + " the authorization code:\n" + url);

            // Read code entered by user.
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String code = br.readLine();

            // Generate Credential using retrieved code.
            GoogleTokenResponse response = flow.newTokenRequest(code)
                    .setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
            GoogleCredential credential = new GoogleCredential()
                    .setFromTokenResponse(response);

            // Create a new authorized Gmail API client
            Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APP_NAME).build();

            // Retrieve a page of Threads; max of 100 by default.
            ListThreadsResponse threadsResponse = service.users().threads().list(USER).execute();
            List<Thread> threads = threadsResponse.getThreads();

            // Print ID of each Thread.
            for (Thread thread : threads) {
                System.out.println("Thread ID: " + thread.getId());
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }



    public void addContact() {

    }

    public void test() {
        try {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            // Allow user to authorize via url.
            GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                    httpTransport, jsonFactory, CLIENT_ID, CLIENT_SECRET, Arrays.asList(SCOPE))
                    .setAccessType("online")
                    .setApprovalPrompt("auto").build();

            String url = flow.newAuthorizationUrl().setRedirectUri(CALLBACK_URI).build();


            GoogleTokenResponse response = flow.newTokenRequest(AUTH_CODE)
                    .setRedirectUri(CALLBACK_URI).execute();
            GoogleCredential credential = new GoogleCredential()
                    .setFromTokenResponse(response);

            // Create a new authorized Gmail API client
            Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APP_NAME).build();

            // Retrieve a page of Threads; max of 100 by default.
            ListThreadsResponse threadsResponse = service.users().threads().list(USER).execute();
            List<Thread> threads = threadsResponse.getThreads();

            // Print ID of each Thread.
            for (Thread thread : threads) {
                System.out.println("Thread ID: " + thread.getId());
            }

            ListMessagesResponse res = service.users().messages().list("mail-noreply@google.com").execute();
            List<Message> messages = new ArrayList<Message>();

            for (Message message : messages) {
                System.out.println(message.toPrettyString());
            }
        } catch ( Exception e) {
            e.printStackTrace();
        }
    }
}
