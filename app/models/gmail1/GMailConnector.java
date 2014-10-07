package models.gmail1;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.contacts.ContactsService;

import java.util.Arrays;


public class GMailConnector {

    // Check https://developers.google.com/gmail/api/auth/scopes for all available scopes
    private static final String SCOPE = "https://www.google.com/m8/feeds";
    private static final String APP_NAME = "My Project";
    // Email address of the user, or "me" can be used to represent the currently authorized user.
    private static final String USER = "me";
    private static final String CALLBACK_URI = "http://localhost:9000/gmail/callback";
    private static GoogleAuthorizationCodeFlow flow;
    private static GMailAccount account;

    public GMailConnector(Long id) {
        account = GMailAccount.getAccount(id);
        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, account.getClient_id(), account.getClient_secret(), Arrays.asList(SCOPE))
                .setAccessType("online")
                .setApprovalPrompt("auto").build();
    }

    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    public String authorize() {
        String url = flow.newAuthorizationUrl().setRedirectUri(CALLBACK_URI).setState(account.id.toString()).build();
        return url;

/*            // Generate Credential using retrieved code.
            GoogleTokenResponse response = flow.newTokenRequest("xxxxxxxxxxx")
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
            }*/
    }


    public void addContact() {

    }

    public void generateAccessToken(String code) {
        try {
            GoogleTokenResponse response = flow.newTokenRequest(code)
                    .setRedirectUri(CALLBACK_URI).execute();
            GoogleCredential credential = new GoogleCredential()
                    .setFromTokenResponse(response);
            account.setAccess_token(credential.getAccessToken());
            account.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContactsService getContactService() {
        GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
                .setTransport(httpTransport).setClientSecrets(account.getClient_id(), account.getClient_secret()).build();
        credential.setAccessToken(account.getAccess_token());
        //credential.setRefreshToken(refreshToken);

/*        Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APP_NAME).build();*/

        ContactsService service = new ContactsService(APP_NAME);
        service.setOAuth2Credentials(credential);
        return service;
    }
}
