package social.entourage.android.entourage.information

import social.entourage.android.api.model.feed.FeedItem
import java.util.*

/**
 * Presenter controlling the FeedItemInformationFragment
 * @see FeedItemInformationFragment
 */
abstract class FeedItemInformationPresenter {

    // ----------------------------------
    // Api calls
    // ----------------------------------
    abstract fun getFeedItem(feedItemUUID: String, feedItemType: Int, feedRank: Int, distance: Int)

    abstract fun getUserInfo(userId: Int?)
    //abstract fun getFeedItem(feedItemShareURL: String, feedItemType: Int)

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