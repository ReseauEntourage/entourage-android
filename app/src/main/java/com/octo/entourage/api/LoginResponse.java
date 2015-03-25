package com.octo.entourage.api;

import com.octo.entourage.api.model.User;

/**
 * Response from login WS
 */
public class LoginResponse {

    private final User user;

    public LoginResponse(final User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
