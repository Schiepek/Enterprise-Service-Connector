package models;

import models.Contact;

public class Container {
    private String totalSize;
    private String done;
    private Contact[] records;

    public Contact[] getContacts() {
        return records;
    }
}
