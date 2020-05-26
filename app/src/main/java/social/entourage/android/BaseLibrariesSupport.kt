package social.entourage.android

import android.content.Context

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import social.entourage.android.tools.log.CrashlyticsLog

/**
 * Created by Mihai Ionescu on 27/04/2018.
 */
abstract class BaseLibrariesSupport {

    // ----------------------------------
    // Members
    // ----------------------------------
    lateinit var firebaseAnalytics: FirebaseAnalytics
        private set

    // ----------------------------------
    // Public methods
    // ----------------------------------

    open fun setupLibraries(context: Context) {
        setupFabric(context)
        setupTimberTree()
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    // ----------------------------------
    // Libraries setup
    // ----------------------------------

    private fun setupFabric(context: Context) {
        // Set up Crashlytics, disabled for debug builds
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    private fun setupTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLog())
        }
    }
}
