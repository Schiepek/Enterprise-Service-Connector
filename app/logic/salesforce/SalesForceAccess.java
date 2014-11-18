package logic.salesforce;

import com.google.gson.Gson;
import models.APIConfig;
import models.gsonmodels.SalesforceContact;
import models.gsonmodels.SalesforceContainer;
import models.ServiceProvider;
import org.apache.oltu.oauth2.client.OAuthClient;
import org.apache.oltu.oauth2.client.URLConnectionClient;
import org.apache.oltu.oauth2.client.request.OAuthBearerClientRequest;
import org.apache.oltu.oauth2.client.request.OAuthClientRequest;
import org.apache.oltu.oauth2.client.response.OAuthResourceResponse;
import org.apache.oltu.oauth2.common.OAuth;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SalesForceAccess {

    List<SalesforceContact> sfContacts;

    private static final String GET_ALL_CONTACTS = "/services/data/v20.0/query/?q=SELECT+" +
            "LastName," +
            "Firstname," +
            "Email," +
            "Title," +
            "Salutation_Title__c," +
            "Birthdate," +
            "Language__c," +
            //"Languages__c," +
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
            "LastModifiedDate," +
            "Owner.FirstName," +
            "Owner.LastName" +
            "+FROM+Contact";


    public SalesforceContainer getSalesforceContacts() throws OAuthSystemException, OAuthProblemException {
        new SalesForceConnector().setRefreshToken();
        APIConfig config = APIConfig.getAPIConfig(ServiceProvider.SALESFORCE);
        OAuthClientRequest bearerClientRequest = new OAuthBearerClientRequest(config.getInstance() + GET_ALL_CONTACTS)
                .setAccessToken(config.getAccessToken()).buildHeaderMessage();

        OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());
        OAuthResourceResponse resourceResponse = oAuthClient.resource(bearerClientRequest, OAuth.HttpMethod.GET, OAuthResourceResponse.class);
        Gson gson = new Gson();
        SalesforceContainer container = gson.fromJson(resourceResponse.getBody(), SalesforceContainer.class);
        return container;
    }

    private ArrayList<SalesforceContact> getSalesforceContactsList() throws OAuthProblemException, OAuthSystemException {
        SalesforceContainer container = getSalesforceContacts();
        return new ArrayList<SalesforceContact>(Arrays.asList(container.getContacts()));
    }

    public SalesforceContact getSalesForceContact(String mail) throws OAuthProblemException, OAuthSystemException {
        if (sfContacts == null) {
            this.sfContacts = getSalesforceContactsList();
        }
        for (SalesforceContact contact : sfContacts) {
            if (contact.getEmail() != null && mail != null && contact.getEmail().equals(mail)) {
                return contact;
            }
        }
        return null;
    }

}
