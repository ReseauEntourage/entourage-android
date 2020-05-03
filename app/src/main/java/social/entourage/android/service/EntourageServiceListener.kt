package social.entourage.android.service

import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.location.LocationUpdateListener

interface EntourageServiceListener : LocationUpdateListener {

    fun onFeedItemClosed(closed: Boolean, feedItem: FeedItem)

    fun onUserStatusChanged(user: EntourageUser, feedItem: FeedItem)
}