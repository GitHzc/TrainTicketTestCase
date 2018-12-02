package org.services.test.entity.dto;

import java.io.Serializable;

public class AddContactsInfo implements Serializable {

    private static final long serialVersionUID = -1289890882983455869L;
    private String name;

    private int documentType;

    private String documentNumber;

    private String phoneNumber;

    public AddContactsInfo() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDocumentType() {
        return documentType;
    }

    public void setDocumentType(int documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
