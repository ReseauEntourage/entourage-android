package social.entourage.android.base

import android.content.Intent
import android.os.Bundle
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingLanguage

/**
 * Base Activity that only runs if the user is currently logged in
 */
abstract class BaseSecuredActivity : BaseActivity() {
    protected val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (authenticationController.isAuthenticated) {
            entApp?.finishLoginActivity()
        } else {
            startActivity(Intent(this, PreOnboardingLanguage::class.java))
            finish()
        }
    }

    override fun getLink(linkId: String): String {
        return authenticationController.me?.token?.let {
            getString(
                R.string.redirect_link_format,
                BuildConfig.ENTOURAGE_URL,
                linkId,
                it
            )
        } ?: super.getLink(linkId)
    }
}