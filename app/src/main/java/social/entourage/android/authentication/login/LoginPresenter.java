package social.entourage.android.authentication.login;

import android.util.Log;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.R;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.LoginResponse;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
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
    private final UserRequest userRequest;
    private final AuthenticationController authenticationController;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public LoginPresenter(
            final LoginActivity activity,
            final LoginRequest loginRequest,
            final UserRequest userRequest,
            final AuthenticationController authenticationController) {
        this.activity = activity;
        this.loginRequest = loginRequest;
        this.userRequest = userRequest;
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

    public void login(final String phone, final String smsCode, final String type, final String id, final boolean isTutorialDone) {
        if (activity != null) {
            final String phoneNumber = checkPhoneNumberFormat(phone);
            if (phoneNumber != null) {
                activity.startLoader();
                loginRequest.login(phoneNumber, smsCode, type, id, new Callback<LoginResponse>() {
                    @Override
                    public void success(LoginResponse loginResponse, Response response) {
                        authenticationController.saveUser(loginResponse.getUser());
                        authenticationController.saveUserPhone(phoneNumber);
                        authenticationController.saveUserToursOnly(false);
                        if (isTutorialDone) {
                            activity.startMapActivity();
                        } else {
                            activity.launchFillInProfileView(loginResponse.getUser());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.loginFail();
                    }
                });
            } else {
                activity.resetLoginButton();
                activity.displayToast(activity.getString(R.string.login_text_invalid_format));
            }
        }
    }

    public void sendNewCode(final String phone) {

    }

    public void updateUserEmail(final String email) {
        if (activity != null) {
            activity.startLoader();
            HashMap<String, String> user = new HashMap<>();
            user.put("email", email);
            userRequest.updateUser(user, new Callback<UserResponse>() {
                @Override
                public void success(UserResponse userResponse, Response response) {
                    Log.d("login update:", "success");
                    authenticationController.getUser().setEmail(email);
                    activity.displayToast(activity.getString(R.string.login_text_email_update_success));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("login update:", "failure");
                    activity.displayToast(activity.getString(R.string.login_text_email_update_fail));
                }
            });
        }
    }
}
