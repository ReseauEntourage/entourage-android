package social.entourage.android

import android.content.Context

import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.firebase.analytics.FirebaseAnalytics
import com.mixpanel.android.mpmetrics.MixpanelAPI

import org.json.JSONException
import org.json.JSONObject

import io.fabric.sdk.android.Fabric
import timber.log.Timber

import social.entourage.android.BuildConfig.FLAVOR
import social.entourage.android.tools.log.CrashlyticsLog

/**
 * Created by Mihai Ionescu on 27/04/2018.
 */
abstract class BaseLibrariesSupport {

    // ----------------------------------
    // Members
    // ----------------------------------
    var mixpanel: MixpanelAPI? = null
        private set
    var firebaseAnalytics: FirebaseAnalytics? = null
        private set

    // ----------------------------------
    // Public methods
    // ----------------------------------

    open fun setupLibraries(context: Context) {
        setupFabric(context)
        setupTimberTree()
        setupMixpanel(context)
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    fun onActivityDestroyed(activity: EntourageActivity) {
        if (mixpanel != null) {
            mixpanel!!.flush()
        }
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

    private fun setupMixpanel(context: Context) {
        mixpanel = MixpanelAPI.getInstance(context, BuildConfig.MIXPANEL_TOKEN)
        val props = JSONObject()
        try {
            props.put("Flavor", FLAVOR)
        } catch (e: JSONException) {
            Timber.e(e)
        }

        mixpanel!!.registerSuperProperties(props)
    }

    private fun setupTimberTree() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(CrashlyticsLog())
        }
    }
}
