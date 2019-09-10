package social.entourage.android.tools.log

import com.crashlytics.android.Crashlytics

import social.entourage.android.api.model.Newsfeed
import social.entourage.android.tour.NewsFeedListener

class CrashlyticsNewsFeedLogger : NewsFeedListener {

    override fun onNetworkException() {
    }

    override fun onCurrentPositionNotRetrieved() {
    }

    override fun onServerException(throwable: Throwable) {
        Crashlytics.logException(throwable);
    }

    override fun onTechnicalException(throwable: Throwable) {
        Crashlytics.logException(throwable);
    }

    override fun onNewsFeedReceived(newsFeeds: List<Newsfeed>) {
    }
}
