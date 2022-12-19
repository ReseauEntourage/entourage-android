package social.entourage.android.old_v7.base.newsfeed

import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.feed.NewsfeedItem

interface NewsFeedListener: ApiConnectionListener {
    fun onNewsFeedReceived(newsFeeds: List<NewsfeedItem>)
}