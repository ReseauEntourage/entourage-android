package social.entourage.android.newsfeed

import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.Newsfeed

interface NewsFeedListener: ApiConnectionListener {
    fun onCurrentPositionNotRetrieved()

    fun onNewsFeedReceived(newsFeeds: List<Newsfeed>)
}