package social.entourage.android.tools.log

import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.newsfeed.NewsFeedListener
import timber.log.Timber

class LoggerNewsFeedLogger : NewsFeedListener {

    override fun onNetworkException() {
        Timber.e("Network exception")
    }

    override fun onServerException(throwable: Throwable) {
        Timber.e(throwable, "Server exception")
    }

    override fun onTechnicalException(throwable: Throwable) {
        Timber.e(throwable, "Technical exception")
    }

    override fun onNewsFeedReceived(newsFeeds: List<NewsfeedItem>) {
        Timber.d("NewsFeed received, size = %d", newsFeeds.size)
    }
}
