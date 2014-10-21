package logic.salesforce;

import com.google.gson.Gson;
import models.APIConfig;
import models.Container;
import models.ServiceProvider;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

public class SalesForceAccess {
    private static final String GET_ALL_CONTACTS = "/services/data/v20.0/query/?q=SELECT+" +
            "LastName," +
            "Firstname," +
            "Email," +
            "Salutation," +
            "Title," +
            "Salutation_Title__c," +
            "Birthdate," +
            "Languages__c," +
            "Phone," +
            "MobilePhone," +
            "ReportsToId," +
            "AccountId," +
            "MailingStreet," +
            "MailingCity," +
            "MailingPostalCode," +
            "MailingCountry," +
            "f_contact__c," +
            "OwnerId," +
            "Id," +
            "Account.Name," +
            "Account.Website," +
            "Account.Phone," +
            "ReportsTo.FirstName," +
            "ReportsTo.LastName," +
            "LastModifiedDate" +
            "+FROM+Contact";


    public Container getSalesforceContacts() throws OAuthSystemException, OAuthProblemException {
        new SalesForceConnector().setRefreshToken();
        APIConfig config = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE);
        OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(config.getInstance() + GET_ALL_CONTACTS)
                .setAccessToken(config.getAccessToken()).buildHeaderMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        Gson gson = new Gson();
        Container container = gson.fromJson(resourceResponse.getBody(), Container.class);
        return container;
    }

}
