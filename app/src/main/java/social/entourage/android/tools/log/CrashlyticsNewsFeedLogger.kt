package social.entourage.android.tools.log

import social.entourage.android.api.ApiConnectionListener
import timber.log.Timber

class CrashlyticsNewsFeedLogger : ApiConnectionListener {

    override fun onNetworkException() {
    }

    override fun onServerException(throwable: Throwable) {
        Timber.e(throwable)
    }

    override fun onTechnicalException(throwable: Throwable) {
        Timber.e(throwable)
    }
}
