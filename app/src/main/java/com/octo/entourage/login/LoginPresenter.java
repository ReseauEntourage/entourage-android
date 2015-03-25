package com.octo.entourage.login;

import com.octo.entourage.api.LoginService;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LoginPresenter {
    private final LoginActivity activity;
    private final LoginService loginService;

    @Inject
    public LoginPresenter(final LoginActivity activity, final LoginService loginService) {
        this.activity = activity;
        this.loginService = loginService;
    }

}
