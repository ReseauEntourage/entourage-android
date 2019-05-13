package social.entourage.android

import android.content.Context

import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior

import social.entourage.android.tools.log.CrashlyticsLog
import timber.log.Timber

/**
 * Libraries support class specific to Entourage
 * Added: Facebook SDK
 * Created by Mihai Ionescu on 27/04/2018.
 */
class LibrariesSupport : BaseLibrariesSupport() {

    override fun setupLibraries(context: Context) {
        super.setupLibraries(context)
        setupTimberTree()
        setupFacebookSDK()
    }

    // ----------------------------------
    // Libraries setup
    // ----------------------------------

    private fun setupFacebookSDK() {
        if (BuildConfig.DEBUG) {
            Timber.tag("Facebook").d("version %s", FacebookSdk.getSdkVersion())
            FacebookSdk.setIsDebugEnabled(true)
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
        }
    }

    private fun setupTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLog())
        }
    }
}
