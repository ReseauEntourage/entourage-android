package social.entourage.android.api.tape

import android.net.Uri
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.api.model.*
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Encounter

open class Events {
    /**
     * Event triggering the checking of the intent action
     */
    class OnCheckIntentActionEvent(val action: String, val extras: Bundle?)

    /**
     * Event bearing connection state for the offline encounters queue
     */
    class OnConnectionChangedEvent(val isConnected: Boolean)

    /**
     * Event bearing the registration id when obtained from Google Cloud Messaging (for push notifications)
     */
    class OnGCMTokenObtainedEvent(val registrationId: String)

    /**
     * Event bearing the new location on which to center the map
     */
    class OnBetterLocationEvent(val location: LatLng)

    /**
     * Event bearing the user's choice regarding the displaying of his past tours
     */
    class OnUserChoiceEvent(val isUserHistory: Boolean)

    /**
     * Event signaling that current user is unauthorized
     */
    class OnUnauthorizedEvent

    /**
     * Event signaling that user info is updated
     */
    class OnUserInfoUpdatedEvent(val user: User)

    /**
     * Event signaling that user wants to act on a tour
     */
    class OnUserActEvent(val act: String, val feedItem: FeedItem) {

        companion object {
            var ACT_JOIN = "join"
            var ACT_QUIT = "quit"
        }

    }

    /**
     * Event signaling that the user wants to update a tour join request
     */
    class OnUserJoinRequestUpdateEvent(val userId: Int, val update: String, val feedItem: FeedItem)

    /**
     * Event signaling that user view is requested
     */
    class OnUserViewRequestedEvent(val userId: Int)

    /**
     * Event signaling that tour info view is requested
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
     * Event signaling that the tour needs to be closed/freezed
     */
    class OnFeedItemCloseRequestEvent {
        var feedItem: FeedItem
            private set
        var isShowUI = true
            private set
        var isSuccess = true
            private set

        constructor(feedItem: FeedItem) {
            this.feedItem = feedItem
        }

        constructor(feedItem: FeedItem, showUI: Boolean, success: Boolean) {
            this.feedItem = feedItem
            isShowUI = showUI
            isSuccess = success
        }

    }

    /**
     * Event triggering the tours service location listener when the permission has been granted
     */
    class OnLocationPermissionGranted(val isPermissionGranted: Boolean)

    /**
     * Event signaling a push notification has been received
     */
    class OnPushNotificationReceived(val message: Message)

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
     * Event signaling that the solidarity guide filter was changed
     */
    class OnSolidarityGuideFilterChanged

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

    /**
     * Event signaling that loading more newsfeed is requested
     */
    class OnNewsfeedLoadMoreEvent

    /**
     * Event signaling that showing an url is requested
     */
    class OnShowURLEvent(val url: String)

    class OnUnreadCountUpdate(val unreadCount: Int?)

    /**
     * Event to show poi detail fragment from feediteminformation (chat message)
     */
    class OnPoiViewDetail(val poiId: String)

    class OnShowEventDeeplink

    class OnShowDetailAssociation(val id:Int)

    class OnRefreshEntourageInformation
    class OnRefreshActionsInfos

    open class TourEvents {
        /**
         * Event signaling that an encounter was created (or not)
         */
        class OnEncounterCreated(val encounter: Encounter?)

        /**
         * Event signaling that an encounter was updated
         */
        class OnEncounterUpdated(val encounter: Encounter)

    }
}
