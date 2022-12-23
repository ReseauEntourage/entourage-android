package social.entourage.android.api.tape

import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.Partner
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.User
import social.entourage.android.api.model.feed.FeedItem

open class Events {
    /**
     * Event bearing the new location on which to center the map
     */
    class OnBetterLocationEvent(val location: LatLng)

    /**
     * Event signaling that user info is updated
     */
    class OnUserInfoUpdatedEvent(val user: User)

    /**
     * Event signaling that user wants to act on an entourage
     */
    class OnUserActEvent(val act: String, val feedItem: FeedItem) {

        companion object {
            var ACT_JOIN = "join"
            var ACT_QUIT = "quit"
        }

    }

    /**
     * Event signaling that user view is requested
     */
    class OnUserViewRequestedEvent(val userId: Int)

    /**
     * Event signaling that entourage info view is requested
     */
    class OnFeedItemInfoViewRequestedEvent {
        var feedItem: FeedItem? = null
            private set
        var feedItemType = 0
            private set
        var feedItemUUID: String? = ""
            private set
        var feedItemShareURL: String? = null
            private set
        var invitationId: Long = 0
            private set
        private var feedRank = 0

        var isFromCreate = false

        constructor(feedItem: FeedItem?) {
            this.feedItem = feedItem
        }

        constructor(feedItem: FeedItem?,isFromCreate:Boolean) {
            this.feedItem = feedItem
            this.isFromCreate = isFromCreate
        }

        constructor(feedItem: FeedItem?, feedRank: Int) {
            this.feedItem = feedItem
            this.feedRank = feedRank
        }

        constructor(feedItemType: Int, feedItemUUID: String?, feedItemShareURL: String?) {
            this.feedItemType = feedItemType
            this.feedItemUUID = feedItemUUID
            this.feedItemShareURL = feedItemShareURL
        }

        constructor(feedItemUUID: String, invitationId: Long) {
            feedItemType = TimestampedObject.ENTOURAGE_CARD
            this.feedItemUUID = feedItemUUID
            this.invitationId = invitationId
        }

        fun getfeedRank(): Int {
            return feedRank
        }

    }

    /**
     * Event signaling that the entourage needs to be closed/freezed
     */
    class OnFeedItemCloseRequestEvent {
        var feedItem: FeedItem
            private set
        var isShowUI = true
            private set
        var isSuccess = true
            private set
        var comment:String? = null

        constructor(feedItem: FeedItem) {
            this.feedItem = feedItem
        }

        constructor(feedItem: FeedItem, showUI: Boolean, success: Boolean, comment:String?) {
            this.feedItem = feedItem
            isShowUI = showUI
            isSuccess = success
            this.comment = comment
        }

    }

    /**
     * Event triggering the service location listener when the permission has been granted
     */
    class OnLocationPermissionGranted(val isPermissionGranted: Boolean)

    /**
     * Event signaling that an entourage was created
     */
    class OnEntourageCreated(val entourage: BaseEntourage)

    /**
     * Event signaling that an entourage was updated
     */
    class OnEntourageUpdated(val entourage: BaseEntourage)

    /**
     * Event signaling that the map filter was changed
     */
    class OnMapFilterChanged

    /**
     * Event signaling that the map filter was changed (all if feedItem is null)
     */
    class OnMyEntouragesForceRefresh(val feedItem: FeedItem?)

    /**
     * Event signaling that a photo was taken/chosen
     */
    class OnPhotoChosen(val photoUri: Uri)

    /**
     * Event signaling that an invitation status was changed
     */
    class OnInvitationStatusChanged(val feedItem: FeedItem, val status: String)

    /**
     * Event signaling that partner view is requested
     */
    class OnPartnerViewRequestedEvent(val partner: Partner)

    class OnUnreadCountUpdate(val unreadCount: Int?)

    class OnRefreshEntourageInformation
}
