package models.gsonmodels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SalesforceContainer {
    private String totalSize;
    private String done;
    private SalesforceContact[] records;
    private String nextRecordsUrl;

    public SalesforceContact[] getContacts() {
        return records;
    }

    public boolean isDone() {
        if (done.equals("true")) {
            return true;
        }
        return false;
    }

    public String getNextRecordsUrl() {
        return nextRecordsUrl;
    }

    public void addContacts(SalesforceContact[] contacts) {
        List<SalesforceContact> contactList = new ArrayList<>();
        contactList.addAll(Arrays.asList(contacts));
        contactList.addAll(Arrays.asList(records));
        SalesforceContact[] c = new SalesforceContact[contactList.size()];
        contactList.toArray(c);
        records = c;
    }
}
