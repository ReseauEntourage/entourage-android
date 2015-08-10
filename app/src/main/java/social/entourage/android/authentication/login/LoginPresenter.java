package social.entourage.android.authentication.login;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.LoginResponse;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
public class LoginPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final LoginActivity activity;
    private final LoginRequest loginRequest;
    private final AuthenticationController authenticationController;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public LoginPresenter(
            final LoginActivity activity,
            final LoginRequest loginRequest,
            final AuthenticationController authenticationController) {
        this.activity = activity;
        this.loginRequest = loginRequest;
        this.authenticationController = authenticationController;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public String checkPhoneNumberFormat(String phoneNumber) {
        boolean isFormatValid = false;

        if (phoneNumber.length() == 10) {
            if (phoneNumber.startsWith("0")) {
                phoneNumber = "+33" + phoneNumber.substring(1, 10);
                isFormatValid = true;
            }
        }
        else if (phoneNumber.length() == 12) {
            if (phoneNumber.startsWith("+33")) {
                isFormatValid = true;
            }
        }

        if (isFormatValid) {
            Pattern pattern = Pattern.compile("^(\\+33)\\d{9}");
            Matcher matcher = pattern.matcher(phoneNumber);
            if (matcher.matches()) {
                return phoneNumber;
            }
        }
        return null;
    }

    public void login(final String phone, final String smsCode, final String type, final String id) {
        String phoneNumber = checkPhoneNumberFormat(phone);
        if (phoneNumber != null) {
            activity.startLoader();
            loginRequest.login(phoneNumber, smsCode, type, id, new Callback<LoginResponse>() {
                @Override
                public void success(LoginResponse loginResponse, Response response) {
                    authenticationController.saveUser(loginResponse.getUser());
                    activity.startMapActivity();
                }

                @Override
                public void failure(RetrofitError error) {
                    activity.loginFail();
                }
            });
        } else {
            activity.displayWrongFormat();
        }
    }

    public void signupForNewsletter(final String email) {

    }

    public void sendNewCode(final String phone) {

    }
}
