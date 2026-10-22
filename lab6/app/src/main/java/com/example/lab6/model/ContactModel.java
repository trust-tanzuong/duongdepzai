package com.example.lab6.model;

import java.io.Serializable;

public class ContactModel implements Serializable {
    private String id;
    private String firstName;
    private String lastName;
    private String displayName;
    private String phoneNumber;
    private String photoUri;

    public ContactModel(String id, String displayName, String phoneNumber, String photoUri) {
        this.id = id;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.photoUri = photoUri;
    }

    public ContactModel(String id, String firstName, String lastName, String displayName, String phoneNumber, String photoUri) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.photoUri = photoUri;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDisplayName() {
        return displayName != null ? displayName : "";
    }

    public String getPhoneNumber() {
        return phoneNumber != null ? phoneNumber : "";
    }

    public String getPhotoUri() {
        return photoUri;
    }
}