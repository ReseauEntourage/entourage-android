package social.entourage.android.authentication.login

import android.os.Bundle
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger
import social.entourage.android.api.LoginRequest
import social.entourage.android.api.UserRequest
import social.entourage.android.authentication.AuthenticationController
import javax.inject.Inject

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
class LoginPresenter  @Inject constructor(
        activity: LoginActivity?,
        loginRequest: LoginRequest,
        userRequest: UserRequest,
        authenticationController: AuthenticationController?) : BaseLoginPresenter(activity, loginRequest, userRequest, authenticationController) {
    /**
     * Post user registration call that sends the log event to Facebook.
     */
    override fun registerUserWithFacebook() {
        val logger = AppEventsLogger.newLogger(activity)
        val params = Bundle()
        params.putString(AppEventsConstants.EVENT_PARAM_REGISTRATION_METHOD, "entourage")
        logger.logEvent(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION, params)
    }
}