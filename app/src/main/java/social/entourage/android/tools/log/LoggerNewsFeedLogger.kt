package social.entourage.android.tools.log

import social.entourage.android.api.ApiConnectionListener
import timber.log.Timber

class LoggerNewsFeedLogger: ApiConnectionListener {

    override fun onNetworkException() {
        Timber.e("Network exception")
    }

    override fun onServerException(throwable: Throwable) {
        Timber.e(throwable, "Server exception")
    }

    override fun onTechnicalException(throwable: Throwable) {
        Timber.e(throwable, "Technical exception")
    }
}
