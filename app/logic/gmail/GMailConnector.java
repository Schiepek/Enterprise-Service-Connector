package logic.gmail;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.admin.directory.Directory;
import com.google.api.services.admin.directory.DirectoryScopes;
import com.google.gdata.client.contacts.ContactsService;
import models.APIConfig;
import models.ServiceProvider;
import models.Settings;

import java.io.IOException;
import java.util.Arrays;


public class GMailConnector {

    private final String[] SCOPE = {"https://www.google.com/m8/feeds https://www.googleapis.com/auth/userinfo.email",
            DirectoryScopes.ADMIN_DIRECTORY_GROUP_READONLY, DirectoryScopes.ADMIN_DIRECTORY_GROUP_MEMBER_READONLY,
            DirectoryScopes.ADMIN_DIRECTORY_USER_READONLY};
    private final String APP_NAME = "esc-project";
    private String CALLBACK_URI_PATH;
    private String CALLBACK_URI;
    private GoogleAuthorizationCodeFlow flow;
    private APIConfig account;
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();

    public GMailConnector() {
        this(APIConfig.getAPIConfig(ServiceProvider.GMAIL));
    }

    public GMailConnector(APIConfig account) {
        if (account.getProvider().equals(ServiceProvider.GMAIL)) {
            CALLBACK_URI_PATH = "/gmail/callback";
        }
        if (account.getProvider().equals(ServiceProvider.GOOGLEPHONE)) {
            CALLBACK_URI_PATH = "/googlephone/callback";
        }
        this.account = account;
        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, account.getClientId(), account.getClientSecret(), Arrays.asList(SCOPE))
                .setAccessType("offline")
                .setApprovalPrompt("auto").build();
        CALLBACK_URI = Settings.getSettings().getServerUrl() + CALLBACK_URI_PATH;
    }

    public String authorize() {
        return flow.newAuthorizationUrl().setAccessType("offline").setRedirectUri(CALLBACK_URI).build();
    }

    public void generateRefreshToken(String code) throws IOException {
        GoogleTokenResponse response = flow.newTokenRequest(code).setScopes(Arrays.asList(SCOPE)).
                setRedirectUri(CALLBACK_URI).execute();
        GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
                .setTransport(httpTransport).setClientSecrets(account.getClientId(), account.getClientSecret()).build();
        credential.setFromTokenResponse(response);
        account.setAccessToken(credential.getAccessToken());
        if (credential.getRefreshToken() != null) account.setRefreshToken(credential.getRefreshToken());
        account.save();
    }


    public ContactsService getContactService() throws IOException {
        GoogleCredential credential = getCredential();
        ContactsService service = new ContactsService(APP_NAME);
        service.setOAuth2Credentials(credential);
        return service;
    }

    public Directory getDirectoryService() throws IOException {
        GoogleCredential credential = getCredential();
        Directory service = new Directory.Builder(httpTransport, jsonFactory, credential)
                .setApplicationName(APP_NAME)
                .build();
        return service;
    }

    private GoogleCredential getCredential() throws IOException {
        GoogleCredential credential = new GoogleCredential.Builder().setJsonFactory(jsonFactory)
                .setTransport(httpTransport).setClientSecrets(account.getClientId(), account.getClientSecret()).build();
        TokenResponse response = new GoogleRefreshTokenRequest(httpTransport, jsonFactory, account.getRefreshToken(),
                account.getClientId(), account.getClientSecret()).execute();
        credential.setAccessToken(response.getAccessToken());
        credential.setRefreshToken(account.getRefreshToken());
        return credential;
    }
}
