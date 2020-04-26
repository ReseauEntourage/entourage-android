package social.entourage.android

import android.content.Context
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.firebase.analytics.FirebaseAnalytics
import io.fabric.sdk.android.Fabric
import social.entourage.android.tools.log.CrashlyticsLog
import timber.log.Timber

/**
 * Created by Mihai Ionescu on 27/04/2018.
 */
abstract class BaseLibrariesSupport {

    // ----------------------------------
    // Members
    // ----------------------------------
    var firebaseAnalytics: FirebaseAnalytics? = null
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
        val crashlyticsKit = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()

        Fabric.with(context, crashlyticsKit)
    }

    private fun setupTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLog())
        }
    }
}
