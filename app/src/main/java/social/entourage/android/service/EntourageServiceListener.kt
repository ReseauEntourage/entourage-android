package social.entourage.android.service

import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.location.LocationUpdateListener

interface EntourageServiceListener : LocationUpdateListener {

    fun onFeedItemClosed(closed: Boolean, updatedFeedItem: FeedItem)

    fun onUserStatusChanged(user: EntourageUser, updatedFeedItem: FeedItem)
}