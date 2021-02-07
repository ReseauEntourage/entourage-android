package social.entourage.android.tour

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.tools.EntError
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.request.*
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
                call.enqueue(object : Callback<TourResponse> {
                    override fun onResponse(call: Call<TourResponse>, response: Response<TourResponse>) {
                        response.body()?.tour?.let {
                            if (response.isSuccessful) {
                                fragment.onFeedItemReceived(it)
                                return
                            }
                        }
                        fragment.onFeedItemNotFound()
                    }

                    override fun onFailure(call: Call<TourResponse>, t: Throwable) {
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
                feedItem.uuid?.let { uuid ->
                    tourRequest.retrieveTourUsers(uuid).enqueue(object : Callback<EntourageUserListResponse> {
                        override fun onResponse(call: Call<EntourageUserListResponse>, response: Response<EntourageUserListResponse>) {
                            response.body()?.users?.let { users->
                                if (response.isSuccessful) {
                                    fragment.onFeedItemUsersReceived(users, context)
                                    return
                                }
                            }
                            fragment.onFeedItemNoUserReceived()
                        }

                        override fun onFailure(call: Call<EntourageUserListResponse>, t: Throwable) {
                            fragment.onFeedItemNoUserReceived()
                        }
                    })
                } ?: run {
                    fragment.onFeedItemNoUserReceived()
                }
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
                feedItem.uuid?.let {uuid->
                    tourRequest.retrieveTourMessages(uuid, lastMessageDate).enqueue(object : Callback<ChatMessageListResponse> {
                        override fun onResponse(call: Call<ChatMessageListResponse>, response: Response<ChatMessageListResponse>) {
                            response.body()?.chatMessages?.let {messages->
                                if (response.isSuccessful) {
                                    fragment.onFeedItemMessagesReceived(messages)
                                    return
                                }
                            }
                            fragment.onFeedItemNoNewMessages()
                        }

                        override fun onFailure(call: Call<ChatMessageListResponse>, t: Throwable) {
                            fragment.onFeedItemNoNewMessages()
                        }
                    })
                } ?: run {
                    fragment.onFeedItemNoNewMessages()
                }
            }
            else -> {
                fragment.onFeedItemNoNewMessages()
            }
        }
    }

    override fun sendFeedItemMessage(feedItem: FeedItem, message: String) {
        fragment.showProgressBar()
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_ADD_MESSAGE)
        val chatMessageWrapper = ChatMessageWrapper(ChatMessage(message))
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                feedItem.uuid?.let {
                    tourRequest.addChatMessage(it, chatMessageWrapper).enqueue(object : Callback<ChatMessageResponse> {
                        override fun onResponse(call: Call<ChatMessageResponse>, response: Response<ChatMessageResponse>) {
                            response.body()?.chatMessage?.let {message->
                                if (response.isSuccessful) {
                                    fragment.onFeedItemMessageSent(message)
                                    return
                                }
                            }
                            fragment.onFeedItemMessageSent(null)
                        }

                        override fun onFailure(call: Call<ChatMessageResponse>, t: Throwable) {
                            fragment.onFeedItemMessageSent(null)
                        }
                    })
                } ?: run {
                    fragment.onFeedItemMessageSent(null)
                }
            }
            else -> {
                fragment.onFeedItemMessageSent(null)
            }
        }
    }

    fun getFeedItemEncounters(tour: Tour) {
        tour.uuid?.let {
            fragment.showProgressBar()
            tourRequest.retrieveTourEncounters(it).enqueue(object : Callback<EncounterListResponse> {
                override fun onResponse(call: Call<EncounterListResponse>, response: Response<EncounterListResponse>) {
                    response.body()?.encounters?.let {encounters ->
                        if (response.isSuccessful) {
                            fragment.onFeedItemEncountersReceived(encounters)
                            return
                        }
                    }
                    fragment.onFeedItemEncountersReceived(null)
                }

                override fun onFailure(call: Call<EncounterListResponse>, t: Throwable) {
                    fragment.onFeedItemEncountersReceived(null)
                }
            })
        }
    }

    // ----------------------------------
    // Update user join requests
    // ----------------------------------
    override fun updateUserJoinRequest(userId: Int, status: String, feedItem: FeedItem) {
        fragment.showProgressBar()
        if (feedItem.type== TimestampedObject.TOUR_CARD) {
            // Tour user update status
            feedItem.uuid?.let {
                when (status) {
                    FeedItem.JOIN_STATUS_ACCEPTED -> {
                        acceptJoinRequest(it, userId)
                        return
                    }
                    FeedItem.JOIN_STATUS_REJECTED -> {
                        rejectJoinRequest(it, userId)
                        return
                    }
                    else -> {}
                }
            }
        }
        // Unknown type
        fragment.onUserJoinRequestUpdated(userId, status, EntError.ERROR_UNKNOWN)
    }

    private fun acceptJoinRequest(tourUUID: String, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        val call = tourRequest.updateUserTourStatus(tourUUID, userId, user)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntError.ERROR_NONE)
                } else {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntError.ERROR_NETWORK)
            }
        })
    }

    private fun rejectJoinRequest(tourUUID: String, userId: Int) {
        val call = tourRequest.removeUserFromTour(tourUUID, userId)
        call.enqueue(object : Callback<EntourageUserResponse> {
            override fun onResponse(call: Call<EntourageUserResponse>, response: Response<EntourageUserResponse>) {
                if (response.isSuccessful) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntError.ERROR_NONE)
                } else {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, response.code())
                }
            }

            override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntError.ERROR_NETWORK)
            }
        })
    }

    // ----------------------------------
    // Update received invitation
    // ----------------------------------
    override fun acceptInvitation(invitationId: Long) {
        val call = invitationRequest.acceptInvitation(invitationId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fragment.onInvitationStatusUpdated(true, Invitation.STATUS_ACCEPTED)
                } else {
                    fragment.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                fragment.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED)
            }
        })
    }

    override fun rejectInvitation(invitationId: Long) {
        val call = invitationRequest.refuseInvitation(invitationId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fragment.onInvitationStatusUpdated(true, Invitation.STATUS_REJECTED)
                } else {
                    fragment.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                fragment.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED)
            }
        })
    }

}