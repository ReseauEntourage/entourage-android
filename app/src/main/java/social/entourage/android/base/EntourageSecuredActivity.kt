package social.entourage.android.base

import android.content.Intent
import android.os.Bundle
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity
import javax.inject.Inject

/**
 * Base Activity that only runs if the user is currently logged in
 */
abstract class EntourageSecuredActivity : EntourageActivity() {
    @Inject lateinit var authenticationController: AuthenticationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (authenticationController.isAuthenticated && authenticationController.isTutorialDone()) {
            (application as? EntourageApplication)?.finishLoginActivity()
        } else {
            startActivity(Intent(this, PreOnboardingStartActivity::class.java))
            finish()
        }
    }

    protected open fun logout() {
        authenticationController.logOutUser()
        EntourageApplication[applicationContext].removeAllPushNotifications()
        //EntourageEvents.logEvent(EntourageEvents.EVENT_LOGOUT);
        startActivity(Intent(this, PreOnboardingStartActivity::class.java))
        finish()
    }

    override fun getLink(linkId: String): String {
        return authenticationController.me?.token?.let { getString(R.string.redirect_link_format, BuildConfig.ENTOURAGE_URL, linkId, it) } ?: super.getLink(linkId)
    }
}