package social.entourage.android.tour

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageError
import social.entourage.android.EntourageEvents
import social.entourage.android.api.InvitationRequest
import social.entourage.android.api.TourRequest
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.ChatMessage.ChatMessagesWrapper
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.Invitation.InvitationWrapper
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.tour.Encounter.EncountersWrapper
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.Tour.TourWrapper
import social.entourage.android.api.model.EntourageUser.EntourageUserWrapper
import social.entourage.android.api.model.EntourageUser.EntourageUsersWrapper
import social.entourage.android.entourage.information.FeedItemInformationPresenter
import java.util.*
import javax.inject.Inject

/**
 * Presenter controlling the TourInformationFragment
 * @see TourInformationFragment
 */
class TourInformationPresenter @Inject constructor(
        private val fragment: TourInformationFragment,
        private val tourRequest: TourRequest,
        private val invitationRequest: InvitationRequest) : FeedItemInformationPresenter() {

    // ----------------------------------
    // Api calls
    // ----------------------------------
    override fun getFeedItem(feedItemUUID: String, feedItemType: Int, feedRank: Int, distance: Int) {
        if(feedItemUUID.isBlank()) return
        fragment.showProgressBar()
        when (feedItemType) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest.retrieveTourById(feedItemUUID)
                call.enqueue(object : Callback<TourWrapper> {
                    override fun onResponse(call: Call<TourWrapper>, response: Response<TourWrapper>) {
                        response.body()?.tour?.let {
                            if (response.isSuccessful) {
                                fragment.onFeedItemReceived(it)
                                return
                            }
                        }
                        fragment.onFeedItemNotFound()
                    }

                    override fun onFailure(call: Call<TourWrapper>, t: Throwable) {
                        fragment.onFeedItemNotFound()
                    }
                })
            }
            else -> {
                fragment.onFeedItemNotFound()
            }
        }
    }

    /*override fun getFeedItem(feedItemShareURL: String, feedItemType: Int) {
        fragment.onFeedItemNotFound()
    }*/

    override fun getFeedItemMembers(feedItem: FeedItem) {
        getFeedItemUsers(feedItem, null)
    }

    override fun getFeedItemJoinRequests(feedItem: FeedItem) {
        getFeedItemUsers(feedItem, "group_feed")
    }

    private fun getFeedItemUsers(feedItem: FeedItem, context: String?) {
        fragment.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest.retrieveTourUsers(feedItem.uuid)
                call.enqueue(object : Callback<EntourageUsersWrapper> {
                    override fun onResponse(call: Call<EntourageUsersWrapper>, response: Response<EntourageUsersWrapper>) {
                        response.body()?.users?.let {
                            if (response.isSuccessful) {
                                fragment.onFeedItemUsersReceived(it, context)
                                return
                            }
                        }
                        fragment.onFeedItemNoUserReceived()
                    }

                    override fun onFailure(call: Call<EntourageUsersWrapper>, t: Throwable) {
                        fragment.onFeedItemNoUserReceived()
                    }
                })
            }
            else -> {
                fragment.onFeedItemNoUserReceived()
            }
        }
    }

    override fun getFeedItemMessages(feedItem: FeedItem) {
        getFeedItemMessages(feedItem, null)
    }

    override fun getFeedItemMessages(feedItem: FeedItem, lastMessageDate: Date?) {
        fragment.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest.retrieveTourMessages(feedItem.uuid, lastMessageDate)
                call.enqueue(object : Callback<ChatMessagesWrapper> {
                    override fun onResponse(call: Call<ChatMessagesWrapper>, response: Response<ChatMessagesWrapper>) {
                        response.body()?.chatMessages?.let {
                            if (response.isSuccessful) {
                                fragment.onFeedItemMessagesReceived(it)
                                return
                            }
                        }
                        fragment.onFeedItemNoNewMessages()
                    }

                    override fun onFailure(call: Call<ChatMessagesWrapper>, t: Throwable) {
                        fragment.onFeedItemNoNewMessages()
                    }
                })
            }
            else -> {
                fragment.onFeedItemNoNewMessages()
            }
        }
    }

    override fun sendFeedItemMessage(feedItem: FeedItem, message: String) {
        fragment.showProgressBar()
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ADD_MESSAGE)
        val chatMessageWrapper = ChatMessageWrapper()
        chatMessageWrapper.chatMessage = ChatMessage(message)
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest.chatMessage(feedItem.uuid, chatMessageWrapper)
                call.enqueue(object : Callback<ChatMessageWrapper> {
                    override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                        response.body()?.chatMessage?.let {
                            if (response.isSuccessful) {
                                fragment.onFeedItemMessageSent(it)
                                return
                            }
                        }
                        fragment.onFeedItemMessageSent(null)
                    }

                    override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                        fragment.onFeedItemMessageSent(null)
                    }
                })
            }
            else -> {
                fragment.onFeedItemMessageSent(null)
            }
        }
    }

    fun getFeedItemEncounters(tour: Tour) {
        fragment.showProgressBar()
        val call = tourRequest.retrieveTourEncounters(tour.uuid)
        call.enqueue(object : Callback<EncountersWrapper> {
            override fun onResponse(call: Call<EncountersWrapper>, response: Response<EncountersWrapper>) {
                response.body()?.encounters?.let {
                    if (response.isSuccessful) {
                        fragment.onFeedItemEncountersReceived(it)
                        return
                    }
                }
                fragment.onFeedItemEncountersReceived(null)
            }

            override fun onFailure(call: Call<EncountersWrapper>, t: Throwable) {
                fragment.onFeedItemEncountersReceived(null)
            }
        })
    }

    // ----------------------------------
    // Update user join requests
    // ----------------------------------
    override fun updateUserJoinRequest(userId: Int, status: String, feedItem: FeedItem) {
        fragment.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                // Tour user update status
                when (status) {
                    FeedItem.JOIN_STATUS_ACCEPTED -> {
                        acceptJoinRequest(feedItem.uuid, userId)
                    }
                    FeedItem.JOIN_STATUS_REJECTED -> {
                        rejectJoinRequest(feedItem.uuid, userId)
                    }
                    else -> {
                        fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
                    }
                }
            }
            else -> {
                // Unknown type
                fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
            }
        }
    }

    private fun acceptJoinRequest(tourUUID: String?, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        val call = tourRequest.updateUserTourStatus(tourUUID, userId, user)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NONE)
                } else {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NETWORK)
            }
        })
    }

    private fun rejectJoinRequest(tourUUID: String?, userId: Int) {
        val call = tourRequest.removeUserFromTour(tourUUID, userId)
        call.enqueue(object : Callback<EntourageUserWrapper?> {
            override fun onResponse(call: Call<EntourageUserWrapper?>, response: Response<EntourageUserWrapper?>) {
                if (response.isSuccessful) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NONE)
                } else {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, response.code())
                }
            }

            override fun onFailure(call: Call<EntourageUserWrapper?>, t: Throwable) {
                fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NETWORK)
            }
        })
    }

    // ----------------------------------
    // Update received invitation
    // ----------------------------------
    override fun acceptInvitation(invitationId: Long) {
        val call = invitationRequest.acceptInvitation(invitationId)
        call.enqueue(object : Callback<InvitationWrapper?> {
            override fun onResponse(call: Call<InvitationWrapper?>, response: Response<InvitationWrapper?>) {
                if (response.isSuccessful) {
                    fragment.onInvitationStatusUpdated(true, Invitation.STATUS_ACCEPTED)
                } else {
                    fragment.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED)
                }
            }

            override fun onFailure(call: Call<InvitationWrapper?>, t: Throwable) {
                fragment.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED)
            }
        })
    }

    override fun rejectInvitation(invitationId: Long) {
        val call = invitationRequest.refuseInvitation(invitationId)
        call.enqueue(object : Callback<InvitationWrapper?> {
            override fun onResponse(call: Call<InvitationWrapper?>, response: Response<InvitationWrapper?>) {
                if (response.isSuccessful) {
                    fragment.onInvitationStatusUpdated(true, Invitation.STATUS_REJECTED)
                } else {
                    fragment.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED)
                }
            }

            override fun onFailure(call: Call<InvitationWrapper?>, t: Throwable) {
                fragment.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED)
            }
        })
    }

}