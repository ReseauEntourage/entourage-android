package social.entourage.android.login;

import social.entourage.android.api.LoginService;
import social.entourage.android.api.LoginResponse;
import social.entourage.android.authentication.AuthenticationController;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

@Singleton
public class LoginPresenter {
    private final LoginActivity activity;
    private final LoginService loginService;
    private final AuthenticationController authenticationController;

    @Inject
    public LoginPresenter(
            final LoginActivity activity,
            final LoginService loginService,
            final AuthenticationController authenticationController) {
        this.activity = activity;
        this.loginService = loginService;
        this.authenticationController = authenticationController;
    }

    public void login(final String email) {
        loginService.login(email, new Callback<LoginResponse>() {
            @Override
            public void success(final LoginResponse loginResponse, final Response response) {
                activity.loginSuccess(loginResponse.getUser().getFirstName());
            }

            @Override
            public void failure(final RetrofitError error) {
                activity.loginFail();
            }
        });
    }
}
