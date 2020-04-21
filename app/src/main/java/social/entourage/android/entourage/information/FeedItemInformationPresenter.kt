package social.entourage.android.entourage.information

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageError
import social.entourage.android.EntourageEvents
import social.entourage.android.api.EntourageRequest
import social.entourage.android.api.InvitationRequest
import social.entourage.android.api.TourRequest
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.ChatMessage.ChatMessagesWrapper
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.Invitation.InvitationWrapper
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.BaseEntourage.EntourageWrapper
import social.entourage.android.api.model.map.Encounter.EncountersWrapper
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.Tour.TourWrapper
import social.entourage.android.api.model.map.TourUser.TourUserWrapper
import social.entourage.android.api.model.map.TourUser.TourUsersWrapper
import java.util.*
import javax.inject.Inject

/**
 * Presenter controlling the FeedItemInformationFragment
 * @see FeedItemInformationFragment
 */
abstract class FeedItemInformationPresenter {

    // ----------------------------------
    // Api calls
    // ----------------------------------
    abstract fun getFeedItem(feedItemUUID: String, feedItemType: Int, feedRank: Int, distance: Int)

    abstract fun getFeedItem(feedItemShareURL: String, feedItemType: Int)

    abstract fun getFeedItemMembers(feedItem: FeedItem)

    abstract fun getFeedItemJoinRequests(feedItem: FeedItem)

    abstract fun getFeedItemMessages(feedItem: FeedItem)

    abstract fun getFeedItemMessages(feedItem: FeedItem, lastMessageDate: Date?)

    abstract fun sendFeedItemMessage(feedItem: FeedItem, message: String)

    // ----------------------------------
    // Update user join requests
    // ----------------------------------
    abstract fun updateUserJoinRequest(userId: Int, status: String, feedItem: FeedItem)

    // ----------------------------------
    // Update received invitation
    // ----------------------------------
    abstract fun acceptInvitation(invitationId: Long)

    abstract fun rejectInvitation(invitationId: Long)

}