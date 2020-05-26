package social.entourage.android.tools.log

import com.google.firebase.crashlytics.FirebaseCrashlytics
import social.entourage.android.api.ApiConnectionListener

class CrashlyticsNewsFeedLogger : ApiConnectionListener {

    override fun onNetworkException() {
    }

    override fun onServerException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }

    override fun onTechnicalException(throwable: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
    }
}
