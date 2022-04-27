package social.entourage.android.tools

import android.content.Context

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
}
