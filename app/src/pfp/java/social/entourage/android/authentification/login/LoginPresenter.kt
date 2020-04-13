package social.entourage.android.authentification.login

import social.entourage.android.api.LoginRequest
import social.entourage.android.api.UserRequest
import social.entourage.android.api.model.User
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.authentication.login.BaseLoginPresenter
import social.entourage.android.authentication.login.LoginActivity
import javax.inject.Inject

/**
 * Presenter controlling the LoginActivity
 * @see LoginActivity
 */
class LoginPresenter @Inject constructor(
        activity: LoginActivity?,
        loginRequest: LoginRequest?,
        userRequest: UserRequest?,
        authenticationController: AuthenticationController?) : BaseLoginPresenter(activity, loginRequest, userRequest, authenticationController) {
    protected fun shouldShowChoosePhotoAfterLogin(): Boolean {
        val me = authenticationController.user
        return me != null && (me.avatarURL == null || me.avatarURL.length == 0)
    }

    override fun shouldShowTermsAndConditions(): Boolean {
        return true
    }

    override fun shouldContinueWithRegistration(): Boolean {
        return false
    }

    override fun shouldShowNameView(user: User): Boolean {
        return super.shouldShowNameView(user) || authenticationController.isNewUser
    }

    override fun shouldShowEmailView(user: User): Boolean {
        return super.shouldShowEmailView(user) || authenticationController.isNewUser
    }
}