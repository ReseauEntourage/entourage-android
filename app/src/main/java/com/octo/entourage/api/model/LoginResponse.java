package com.octo.entourage.api.model;

/**
 * Response from login WS
 */
public class LoginResponse {

    private final User user;


    public LoginResponse(final User user) {
        this.user = user;
    }
}
