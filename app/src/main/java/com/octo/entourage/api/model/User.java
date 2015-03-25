package com.octo.entourage.api.model;

import com.google.gson.annotations.SerializedName;

public class User {

    private final int id;

    private final String email;

    @SerializedName("first_name")
    private final String firstName;

    @SerializedName("last_name")
    private final String lastName;

    private final String token;

    public User(final int id, final String email, final String firstName, final String lastName, final String token) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getToken() {
        return token;
    }
}
