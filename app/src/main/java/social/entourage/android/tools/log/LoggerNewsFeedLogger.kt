package social.entourage.android.tools.log

import social.entourage.android.api.model.Newsfeed
import social.entourage.android.newsfeed.NewsFeedListener
import timber.log.Timber

class LoggerNewsFeedLogger : NewsFeedListener {

    override fun onNetworkException() {
        Timber.e("Network exception")
    }

    override fun onCurrentPositionNotRetrieved() {
        Timber.e("Current position not retrieved")
    }

    override fun onServerException(throwable: Throwable) {
        Timber.e(throwable, "Server exception")
    }

    override fun onTechnicalException(throwable: Throwable) {
        Timber.e(throwable, "Technical exception")
    }

    override fun onNewsFeedReceived(newsFeeds: List<Newsfeed>) {
        Timber.d("NewsFeed received, size = %d", newsFeeds.size)
    }
}
