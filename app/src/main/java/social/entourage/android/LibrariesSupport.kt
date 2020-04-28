package social.entourage.android

import android.content.Context

import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior

import timber.log.Timber

/**
 * Libraries support class specific to Entourage
 * Added: Facebook SDK
 * Created by Mihai Ionescu on 27/04/2018.
 */
class LibrariesSupport : BaseLibrariesSupport() {

    override fun setupLibraries(context: Context) {
        super.setupLibraries(context)
        setupFacebookSDK()
    }

    // ----------------------------------
    // Libraries setup
    // ----------------------------------

    private fun setupFacebookSDK() {
        if (BuildConfig.DEBUG) {
            Timber.d("Facebook version %s", FacebookSdk.getSdkVersion())
            FacebookSdk.setIsDebugEnabled(true)
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
        }
    }
}
