package social.entourage.android.authentication.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.LoginResponse;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
public class LoginPresenter {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final static String COUNTRY_CODE_FR = "FR";
    private final static String COUNTRY_CODE_CA = "CA";

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

        String regionFormat = null;
        boolean isFormatValid = false;


        switch (phoneNumber.length()) {
            case 10:
                if (phoneNumber.startsWith("0")) {
                    phoneNumber = "+33" + phoneNumber.substring(1, 10);
                    isFormatValid = true;
                    regionFormat = COUNTRY_CODE_FR;
                }
                break;
            case 11:
                if (phoneNumber.startsWith("1")) {
                    phoneNumber = "+" + phoneNumber;
                    isFormatValid = true;
                    regionFormat = COUNTRY_CODE_CA;
                }
                break;
            case 12:
                if (phoneNumber.startsWith("+33")) {
                    isFormatValid = true;
                    regionFormat = COUNTRY_CODE_FR;
                }
                else if (phoneNumber.startsWith("+1")) {
                    isFormatValid = true;
                    regionFormat = COUNTRY_CODE_CA;
                }
                break;
            default:
                break;
        }

        if (isFormatValid) {
            Pattern pattern = null;
            if (regionFormat.equals(COUNTRY_CODE_FR)) {
                pattern = Pattern.compile("^(\\+33)\\d{9}");
            }
            else if (regionFormat.equals(COUNTRY_CODE_FR)) {
                pattern = Pattern.compile("^(\\+1)\\d{10}");
            }
            Matcher matcher = pattern.matcher(phoneNumber);
            if (matcher.matches()) {
                return phoneNumber;
            }
        }

        return null;
    }

    public void login(final String phone, final String smsCode) {
        if (activity != null) {
            final String phoneNumber = checkPhoneNumberFormat(phone);
            if (phoneNumber != null) {
                HashMap<String, String> user = new HashMap<>();
                user.put("phone", phoneNumber);
                user.put("sms_code", smsCode);
                SharedPreferences sharedPreferences = activity.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
                HashSet<String> loggedNumbers = (HashSet) sharedPreferences.getStringSet(LoginActivity.KEY_TUTORIAL_DONE, new HashSet<String>());
                final boolean isTutorialDone = loggedNumbers.contains(phoneNumber);
                activity.startLoader();
                loginRequest.login(user, new Callback<LoginResponse>() {
                    @Override
                    public void success(LoginResponse loginResponse, Response response) {
                        authenticationController.saveUser(loginResponse.getUser());
                        authenticationController.saveUserPhone(phoneNumber);
                        authenticationController.saveUserToursOnly(false);
                        if (isTutorialDone) {
                            activity.startMapActivity();
                        } else {
                            activity.launchFillInProfileView(phoneNumber, loginResponse.getUser());
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.loginFail();
                    }
                });
            } else {
                activity.stopLoader();
                activity.displayToast(activity.getString(R.string.login_text_invalid_format));
            }
        }
    }

    public void sendNewCode(final String phone) {
        if (activity != null) {
            if (phone != null) {
                Map<String, String> user = new ArrayMap<>();
                user.put("phone", phone);

                Map<String, String> code = new ArrayMap<>();
                code.put("action", "regenerate");

                ArrayMap<String, Object> request = new ArrayMap<>();
                request.put("user", user);
                request.put("code", code);

                userRequest.regenerateSecretCode(request, new Callback<UserResponse>() {
                    @Override
                    public void success(UserResponse userResponse, Response response) {
                        activity.newCodeAsked(userResponse.getUser());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        activity.newCodeAsked(null);
                    }
                });
            }
        }
    }

    public void updateUserEmail(final String email) {
        /*if (activity != null) {
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
        }*/
    }
}
