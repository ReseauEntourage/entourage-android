package social.entourage.android.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingLanguage
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity
import timber.log.Timber

/**
 * Base Activity that only runs if the user is currently logged in
 */
abstract class BaseSecuredActivity : BaseActivity() {
    protected val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isMigrationAfterV7 = EntourageApplication.get().sharedPreferences.getBoolean(
            EntourageApplication.KEY_MIGRATION_V7_OK, false)
        if (authenticationController.isAuthenticated && isMigrationAfterV7) {
            entApp?.finishLoginActivity()
        } else {
            Log.wtf("wtf", "hello there")
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