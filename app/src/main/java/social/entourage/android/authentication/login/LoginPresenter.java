package social.entourage.android.authentication.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;
import android.util.Patterns;

import com.squareup.okhttp.ResponseBody;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.Constants;
import social.entourage.android.R;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.LoginResponse;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.Newsletter;
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

        if (phoneNumber.startsWith("0")) {
            phoneNumber = "+33" + phoneNumber.substring(1);
        } else if (!phoneNumber.startsWith("+")) {
            phoneNumber = "+" + phoneNumber;
        }

        if(Patterns.PHONE.matcher(phoneNumber).matches())
            return phoneNumber;

        return null;
    }

    public String checkEmailFormat(String email) {
        if (email != null && !email.equals("")) {
            return email;
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
                Call<LoginResponse> call = loginRequest.login(user);
                call.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccess()) {
                            authenticationController.saveUser(response.body().getUser());
                            authenticationController.saveUserPhone(phoneNumber);
                            authenticationController.saveUserToursOnly(false);
                            if (isTutorialDone) {
                                activity.startMapActivity();
                            } else {
                                activity.launchFillInProfileView(phoneNumber, response.body().getUser());
                            }
                        } else {
                            activity.loginFail(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        activity.loginFail(true);
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

                Call<UserResponse> call = userRequest.regenerateSecretCode(request);
                call.enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccess()) {
                            activity.newCodeAsked(response.body().getUser());
                        } else {
                            activity.newCodeAsked(null);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
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

    public void subscribeToNewsletter(final String email) {
        if (activity != null) {
            String checkedEmail = checkEmailFormat(email);
            if (checkedEmail != null) {
                Newsletter newsletter = new Newsletter(email, true);
                Newsletter.NewsletterWrapper newsletterWrapper = new Newsletter.NewsletterWrapper(newsletter);
                Call<Newsletter.NewsletterWrapper> call = loginRequest.subscribeToNewsletter(newsletterWrapper);
                call.enqueue(new Callback<Newsletter.NewsletterWrapper>() {
                    @Override
                    public void onResponse(Call<Newsletter.NewsletterWrapper> call, Response<Newsletter.NewsletterWrapper> response) {
                        if (response.isSuccess()) {
                            activity.newsletterResult(true);
                        } else {
                            activity.newsletterResult(false);
                        }
                    }

                    @Override
                    public void onFailure(Call<Newsletter.NewsletterWrapper> call, Throwable t) {
                        activity.newsletterResult(false);
                    }
                });
            } else {
                activity.stopLoader();
                activity.displayToast(activity.getString(R.string.login_text_invalid_email));
            }
        }
    }
}
