package social.entourage.android.tools.log

import com.crashlytics.android.Crashlytics
import social.entourage.android.api.ApiConnectionListener

class CrashlyticsNewsFeedLogger : ApiConnectionListener {

    override fun onNetworkException() {
    }

    override fun onServerException(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }

    override fun onTechnicalException(throwable: Throwable) {
        Crashlytics.logException(throwable)
    }
}
