package social.entourage.android.base.newsfeed

import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.feed.NewsfeedItem

interface NewsFeedListener: ApiConnectionListener {
    fun onNewsFeedReceived(newsFeeds: List<NewsfeedItem>)
}