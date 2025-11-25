package social.entourage.android.base

import android.content.Intent
import android.os.Bundle
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.language.LanguageManager
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingLanguage
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity

/**
 * Base Activity that only runs if the user is currently logged in
 */
abstract class BaseSecuredActivity : BaseActivity() {
    protected val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkConnexion()
    }

    override fun onResume() {
        super.onResume()
        checkConnexion()
    }

    private fun checkConnexion() {
        if (authenticationController.isAuthenticated) {
            entApp?.finishLoginActivity()
        } else {
            val intent =
                if(LanguageManager.isLanguagePreferenceAlreadySet(context = applicationContext))
                    Intent(this, PreOnboardingStartActivity::class.java)
                else
                    Intent(this, PreOnboardingLanguage::class.java)

            startActivity(intent)
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