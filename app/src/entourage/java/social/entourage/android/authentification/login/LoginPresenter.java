package social.entourage.android.authentification.login;

import android.os.Bundle;

import com.facebook.appevents.AppEventsConstants;
import com.facebook.appevents.AppEventsLogger;

import javax.inject.Inject;

import social.entourage.android.api.LoginRequest;
import social.entourage.android.api.UserRequest;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.authentication.login.BaseLoginPresenter;
import social.entourage.android.authentication.login.LoginActivity;

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
public class LoginPresenter extends BaseLoginPresenter {

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public LoginPresenter(
            final LoginActivity activity,
            final LoginRequest loginRequest,
            final UserRequest userRequest,
            final AuthenticationController authenticationController) {
        super(activity, loginRequest, userRequest, authenticationController);
    }

    /**
     * Post user registration call that sends the log event to Facebook.
     */
    @Override
    protected void registerUserWithFacebook() {
        AppEventsLogger logger = AppEventsLogger.newLogger(activity);
        Bundle params = new Bundle();
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, "entourage");
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params);
    }
}
