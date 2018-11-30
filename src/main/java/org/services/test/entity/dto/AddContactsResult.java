package org.services.test.entity.dto;

import java.io.Serializable;

public class AddContactsResult implements Serializable {

    private static final long serialVersionUID = -2281116879615285200L;
    private boolean status;

    private String message;

    private Contacts contacts;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Contacts getContacts() {
        return contacts;
    }

    public void setContacts(Contacts contacts) {
        this.contacts = contacts;
    }
}
