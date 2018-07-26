package social.entourage.android.authentication.login;

import android.content.SharedPreferences;
import android.support.v4.util.ArrayMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.R;
import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.LoginResponse;
import social.entourage.android.api.UserRequest;
import social.entourage.android.api.UserResponse;
import social.entourage.android.api.model.Newsletter;
import social.entourage.android.api.model.User;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.tools.Utils;

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
public abstract class BaseLoginPresenter {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final static String COUNTRY_CODE_FR = "FR";
    private final static String COUNTRY_CODE_CA = "CA";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    protected final LoginActivity activity;
    private final LoginRequest loginRequest;
    private final UserRequest userRequest;
    protected final AuthenticationController authenticationController;

    protected boolean isTutorialDone = false;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public BaseLoginPresenter(
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

    public void login(String countryCode, final String phone, final String smsCode) {
        if (activity != null) {
            final String phoneNumber = Utils.checkPhoneNumberFormat(countryCode, phone);
            if (phoneNumber != null) {
                HashMap<String, String> user = new HashMap<>();
                user.put("phone", phoneNumber);
                user.put("sms_code", smsCode);
                SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
                HashSet<String> loggedNumbers = (HashSet<String>) sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, new HashSet<String>());
                isTutorialDone = loggedNumbers.contains(phoneNumber);
                activity.startLoader();
                Call<LoginResponse> call = loginRequest.login(user);
                call.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        if (response.isSuccessful()) {
                            activity.stopLoader();
                            authenticationController.saveUser(response.body().getUser());
                            authenticationController.saveUserPhoneAndCode(phoneNumber, smsCode);
                            authenticationController.saveUserToursOnly(false);
                            if (isTutorialDone) {
                                activity.startMapActivity();
                            } else {
                                activity.launchFillInProfileView(phoneNumber, response.body().getUser());
                            }
                        } else {
                            if (response.errorBody() != null) {
                                try {
                                    String errorBody = response.errorBody().string();
                                    if (errorBody != null) {
                                        if (errorBody.contains("INVALID_PHONE_FORMAT")) {
                                            activity.loginFail(LoginActivity.LOGIN_ERROR_INVALID_PHONE_FORMAT);
                                        } else if (errorBody.contains("UNAUTHORIZED")) {
                                            activity.loginFail(LoginActivity.LOGIN_ERROR_UNAUTHORIZED);
                                        } else {
                                            activity.loginFail(LoginActivity.LOGIN_ERROR_UNKNOWN);
                                        }
                                    } else {
                                        activity.loginFail(LoginActivity.LOGIN_ERROR_UNKNOWN);
                                    }
                                } catch (IOException e) {
                                    activity.loginFail(LoginActivity.LOGIN_ERROR_UNKNOWN);
                                }
                            } else {
                                activity.loginFail(LoginActivity.LOGIN_ERROR_UNKNOWN);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        activity.loginFail(LoginActivity.LOGIN_ERROR_NETWORK);
                    }
                });
            } else {
                activity.stopLoader();
                activity.loginFail(LoginActivity.LOGIN_ERROR_INVALID_PHONE_FORMAT);
            }
        }
    }

    public void sendNewCode(String phone) {
        sendNewCode(phone, false);
    }

    public void sendNewCode(final String phone, final boolean isOnboarding) {
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
                        if (response.isSuccessful()) {
                            activity.newCodeAsked(response.body().getUser(), isOnboarding);
                        } else {
                            activity.newCodeAsked(null, isOnboarding);
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        activity.newCodeAsked(null, isOnboarding);
                    }
                });
            }
        }
    }

    public void updateUserEmail(final String email) {
        authenticationController.getUser().setEmail(email);
    }

    public void updateUserName(String firstname, String lastname) {
        authenticationController.getUser().setFirstName(firstname);
        authenticationController.getUser().setLastName(lastname);
    }

    protected void updateUserToServer() {
        final User user = authenticationController.getUser();

        if (activity != null) {
            activity.startLoader();

            final ArrayMap<String, Object> request = new ArrayMap<>();
            request.put("user", user);

            Call<UserResponse> call = userRequest.updateUser(request);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(final Call<UserResponse> call, final Response<UserResponse> response) {
                    if (activity != null) activity.stopLoader();
                    if (response.isSuccessful()) {
                        if (authenticationController != null) authenticationController.saveUser(response.body().getUser());
                        if (activity != null) {
                            activity.showPhotoChooseSource();
                            activity.displayToast(R.string.login_text_profile_update_success);
                        }
                    }
                    else {
                        if (activity != null) activity.displayToast(R.string.login_text_profile_update_fail);
                        EntourageEvents.logEvent(user.getEmail() == null ? Constants.EVENT_NAME_SUBMIT_ERROR : Constants.EVENT_EMAIL_SUBMIT_ERROR);
                    }
                }

                @Override
                public void onFailure(final Call<UserResponse> call, final Throwable t) {
                    if (activity != null) {
                        activity.stopLoader();
                        activity.displayToast(R.string.login_text_profile_update_fail);
                    }
                    EntourageEvents.logEvent(user.getEmail() == null ? Constants.EVENT_NAME_SUBMIT_ERROR : Constants.EVENT_EMAIL_SUBMIT_ERROR);
                }
            });
        }
    }

    public void updateUserPhoto(String amazonFile) {
        if (activity != null) {

            ArrayMap<String, Object> user = new ArrayMap<>();
            user.put("avatar_key", amazonFile);
            ArrayMap<String, Object> request = new ArrayMap<>();
            request.put("user", user);

            Call<UserResponse> call = userRequest.updateUser(request);
            call.enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                    if (response.isSuccessful()) {
                        if (authenticationController.isAuthenticated()) {
                            authenticationController.saveUser(response.body().getUser());
                        }
                        activity.onUserPhotoUpdated(true);
                    }
                    else {
                        activity.onUserPhotoUpdated(false);
                    }
                }

                @Override
                public void onFailure(Call<UserResponse> call, Throwable t) {
                    activity.onUserPhotoUpdated(false);
                }
            });
        }
    }

    public void subscribeToNewsletter(final String email) {
        if (activity != null) {
            String checkedEmail = Utils.checkEmailFormat(email);
            if (checkedEmail != null) {
                Newsletter newsletter = new Newsletter(email, true);
                Newsletter.NewsletterWrapper newsletterWrapper = new Newsletter.NewsletterWrapper(newsletter);
                Call<Newsletter.NewsletterWrapper> call = loginRequest.subscribeToNewsletter(newsletterWrapper);
                call.enqueue(new Callback<Newsletter.NewsletterWrapper>() {
                    @Override
                    public void onResponse(Call<Newsletter.NewsletterWrapper> call, Response<Newsletter.NewsletterWrapper> response) {
                        if (response.isSuccessful()) {
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
                activity.displayToast(R.string.login_text_invalid_email);
            }
        }
    }

    public void registerUserPhone(final String phoneNumber) {
        Map<String, String> user = new ArrayMap<>();
        user.put("phone", phoneNumber);

        ArrayMap<String, Object> request = new ArrayMap<>();
        request.put("user", user);

        Call<UserResponse> call = userRequest.registerUser(request);
        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(final Call<UserResponse> call, final Response<UserResponse> response) {
                if (response.isSuccessful()) {
                    if (activity != null) {
                        activity.registerPhoneNumberSent(phoneNumber, true);

                        // send the facebook event
                        registerUserWithFacebook();
                    }
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorString = response.errorBody().string();
                            if (errorString.contains("PHONE_ALREADY_EXIST")) {
                                // Phone number already registered
                                EntourageEvents.logEvent(Constants.EVENT_SCREEN_30_2_E);
                                activity.registerPhoneNumberSent(phoneNumber, false);
                                activity.displayToast(R.string.registration_number_error_already_registered);
                            } else if (errorString.contains("INVALID_PHONE_FORMAT")) {
                                activity.displayToast(R.string.login_text_invalid_format);
                            } else {
                                activity.displayToast(R.string.login_error);
                            }
                        } catch (IOException e) {
                            activity.displayToast(R.string.login_error);
                        }
                    } else {
                        activity.displayToast(R.string.login_error);
                    }
                    EntourageEvents.logEvent(Constants.EVENT_PHONE_SUBMIT_FAIL);
                }
            }

            @Override
            public void onFailure(final Call<UserResponse> call, final Throwable t) {
                activity.displayToast(R.string.login_error_network);
                EntourageEvents.logEvent(Constants.EVENT_PHONE_SUBMIT_ERROR);
            }
        });
    }

    // ----------------------------------
    // OVERRIDABLE METHODS
    // ----------------------------------

    /**
     * Method that shows if we need to show Terms and conditions screen when user presses login button at startup
     * @return true if we need to show the screen, false otherwise
     */
    public boolean shouldShowTC() {
        return false;
    }

    /**
     * Returns true if we continue with the registration funnel, when the user presses the 'Commencer' button
     * false if we just dismiss the T&C screen and proceed to the login page
     * @return
     */
    public boolean shouldContinueWithRegistration() {
        return true;
    }

    /**
     * Returns true if we need to show the input name view
     * @param user the user to check
     * @return
     */
    public boolean shouldShowNameView(User user) {
        return (user.getFirstName() == null || user.getFirstName().length() == 0 || user.getLastName() == null || user.getLastName().length() == 0);
    }

    /**
     * Returns true if we need to show the input email view
     * @param user the user to check
     * @return
     */
    public boolean shouldShowEmailView(User user) {
        return (user.getEmail() == null || user.getEmail().length() == 0);
    }

    /**
     * Returns true if we need to show the set action zone view
     * @param user the user to check
     * @return
     */
    public boolean shouldShowActionZoneView(User user) {
        return (user.getAddress() == null || user.getAddress().getDisplayAddress() == null || user.getAddress().getDisplayAddress().length() == 0);
    }

    /**
     * Returns true if we need to show the choose photo source view
     * @param user the user to check
     * @return
     */
    public boolean shouldShowPhotoChooseView(User user) {
        return (user.getAvatarURL() == null || user.getAvatarURL().length() == 0);
    }

    /**
     * Post user registration call that should send the log event to Facebook.<br/>
     * By default it does nothing.
     */
    protected void registerUserWithFacebook() {}

}
