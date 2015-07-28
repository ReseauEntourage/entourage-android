package social.entourage.android.authentication.login;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.LoginResponse;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
public class LoginPresenter implements Callback<LoginResponse> {
    private final LoginActivity activity;
    private final LoginRequest loginRequest;
    private final AuthenticationController authenticationController;

    @Inject
    public LoginPresenter(
            final LoginActivity activity,
            final LoginRequest loginRequest,
            final AuthenticationController authenticationController) {
        this.activity = activity;
        this.loginRequest = loginRequest;
        this.authenticationController = authenticationController;
    }

    public void login(final String email) {
        loginRequest.login(email, this);
    }

    @Override
    public void success(final LoginResponse loginResponse, final Response response) {
        authenticationController.saveUser(loginResponse.getUser());
        activity.startMapActivity();
    }

    @Override
    public void failure(final RetrofitError error) {
        activity.loginFail();
    }
}
