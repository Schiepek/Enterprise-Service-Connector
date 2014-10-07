package models.gmail;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.gdata.client.contacts.ContactsService;
import models.APIConfig;

import java.util.Arrays;


public class GMailConnector {

    private static final String SCOPE = "https://www.google.com/m8/feeds";
    private static final String APP_NAME = "Enterprise Service Connector";
    private static final String CALLBACK_URI = "http://localhost:9000/gmail/callback";
    private static final String GRAND_TYPE  ="authorization_code";
    private static GoogleAuthorizationCodeFlow flow;
    private static APIConfig account;
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    public GMailConnector(Long id) {
        account = APIConfig.getAPIConfig(id);
        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, account.getClientId(), account.getClientSecret(), Arrays.asList(SCOPE))
                .setAccessType("offline")
                .setApprovalPrompt("auto").build();
    }

    public String authorize() {
        return flow.newAuthorizationUrl().setAccessType("offline").setRedirectUri(CALLBACK_URI).setState(account.getId().toString()).build();
    }

    public void generateAccessToken(String code) {
        try {
            GoogleTokenResponse response = flow.newTokenRequest(code).setScopes(Arrays.asList(SCOPE)).setRedirectUri(CALLBACK_URI).execute();
            GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
                    .setTransport(httpTransport).setClientSecrets(account.getClientId(), account.getClientSecret()).build();
            credential.setFromTokenResponse(response);
            account.setAccessToken(credential.getAccessToken());
            account.setRefreshToken(credential.getRefreshToken());
            account.setMail(credential.getServiceAccountUser());
            account.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContactsService getContactService() {
        GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
                .setTransport(httpTransport).setClientSecrets(account.getClientId(), account.getClientSecret()).build();
        credential.setAccessToken(account.getAccessToken());
        credential.setRefreshToken(account.getRefreshToken());
        ContactsService service = new ContactsService(APP_NAME);
        service.setOAuth2Credentials(credential);
        return service;
    }
}
