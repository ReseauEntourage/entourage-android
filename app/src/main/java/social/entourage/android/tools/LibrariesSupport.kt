package social.entourage.android.tools

import android.content.Context

import com.facebook.FacebookSdk
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import social.entourage.android.BuildConfig
import social.entourage.android.tools.log.CrashlyticsLog

import timber.log.Timber

/**
 * Libraries support class
 * Created by Mihai Ionescu on 27/04/2018.
 */
class LibrariesSupport {
    // ----------------------------------
    // Members
    // ----------------------------------
    lateinit var firebaseAnalytics: FirebaseAnalytics
        private set

    // ----------------------------------
    // Libraries setup
    // ----------------------------------
    fun setupLibraries(context: Context) {
        setupFirebase(context)
        setupTimberTree()
        setupFacebookSDK()
    }

    private fun setupFirebase(context: Context) {
        // Set up Crashlytics, disabled for debug builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    private fun setupTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLog())
        }
    }

    private fun setupFacebookSDK() {
        // debug and preprod remove FacebookInitProvider from their manifest, so the SDK is
        // never initialized there and must not be called at all.
        if (!BuildConfig.BUILD_TYPE.equals("release", ignoreCase = true)) {
            Timber.d("Facebook SDK disabled for non-production build (%s)", BuildConfig.BUILD_TYPE)
            return
        }
        try {
            // Auto-init only completes the bootstrap started by FacebookInitProvider; without it
            // the startup Graph calls log "GraphRequest can't be used when Facebook SDK isn't
            // fully initialized". Tracking itself is driven by the flags below.
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            // Never collect the advertising ID: AD_ID is removed from the merged manifest.
            FacebookSdk.setAdvertiserIDCollectionEnabled(false)
            FacebookSdk.setIsDebugEnabled(false)
        } catch (e: Exception) {
            Timber.e(e, "Error initializing Facebook SDK")
        }
    }
}
