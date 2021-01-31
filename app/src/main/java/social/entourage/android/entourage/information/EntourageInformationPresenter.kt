package social.entourage.android.entourage.information

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.tools.EntourageError
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.api.model.ChatMessage
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.request.*
import java.util.*
import javax.inject.Inject

/**
 * Presenter controlling the EntourageInformationFragment
 * @see EntourageInformationFragment
 */
class EntourageInformationPresenter @Inject constructor(
        private val fragment: EntourageInformationFragment,
        private val entourageRequest: EntourageRequest,
        private val invitationRequest: InvitationRequest)  : FeedItemInformationPresenter() {

    // ----------------------------------
    // Api calls
    // ----------------------------------
    override fun getFeedItem(feedItemUUID: String, feedItemType: Int, feedRank: Int, distance: Int) {
        if(feedItemUUID.isBlank()) return
        fragment.showProgressBar()
        when (feedItemType) {
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest.retrieveEntourageById(feedItemUUID, distance, feedRank)
                call.enqueue(object : Callback<EntourageResponse> {
                    override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                        response.body()?.let {
                            if (response.isSuccessful) {
                                fragment.onFeedItemReceived(it.entourage)
                                return
                            }
                        }
                        fragment.onFeedItemNotFound()
                    }

                    override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                        fragment.onFeedItemNotFound()
                    }
                })
            }
            else -> {
                fragment.onFeedItemNotFound()
            }
        }
    }

    override fun getFeedItemMembers(feedItem: FeedItem) {
        getFeedItemUsers(feedItem, null)
    }

    override fun getFeedItemJoinRequests(feedItem: FeedItem) {
        getFeedItemUsers(feedItem, "group_feed")
    }

    private fun getFeedItemUsers(feedItem: FeedItem, context: String?) {
        feedItem.uuid?.let {uuid ->
            fragment.showProgressBar()
            when (feedItem.type) {
                TimestampedObject.ENTOURAGE_CARD -> {
                    val call = entourageRequest.retrieveEntourageUsers(uuid, context)
                    call.enqueue(object : Callback<EntourageUserListResponse> {
                        override fun onResponse(call: Call<EntourageUserListResponse>, response: Response<EntourageUserListResponse>) {
                            response.body()?.users?.let {
                                if (response.isSuccessful) {
                                    fragment.onFeedItemUsersReceived(it, context)
                                    return
                                }
                            }
                            fragment.onFeedItemNoUserReceived()
                        }

                        override fun onFailure(call: Call<EntourageUserListResponse>, t: Throwable) {
                            fragment.onFeedItemNoUserReceived()
                        }
                    })
                }
                else -> {
                    fragment.onFeedItemNoUserReceived()
                }
            }
        }?: run {
            fragment.onFeedItemNoUserReceived()
        }
    }

    override fun getFeedItemMessages(feedItem: FeedItem) {
        getFeedItemMessages(feedItem, null)
    }

    override fun getFeedItemMessages(feedItem: FeedItem, lastMessageDate: Date?) {
        feedItem.uuid?.let { uuid ->
            fragment.showProgressBar()
            when (feedItem.type) {
                TimestampedObject.ENTOURAGE_CARD -> {
                    val call = entourageRequest.retrieveMessages(uuid, lastMessageDate)
                    call.enqueue(object : Callback<ChatMessageListResponse> {
                        override fun onResponse(call: Call<ChatMessageListResponse>, response: Response<ChatMessageListResponse>) {
                            response.body()?.chatMessages?.let {
                                if (response.isSuccessful) {
                                    fragment.onFeedItemMessagesReceived(it)
                                    return
                                }
                            }
                            fragment.onFeedItemNoNewMessages()
                        }

                        override fun onFailure(call: Call<ChatMessageListResponse>, t: Throwable) {
                            fragment.onFeedItemNoNewMessages()
                        }
                    })
                }
                else -> {
                    fragment.onFeedItemNoNewMessages()
                }
            }
        } ?: run {
            fragment.onFeedItemNoNewMessages()
        }
    }

    override fun sendFeedItemMessage(feedItem: FeedItem, message: String) {
        feedItem.uuid?.let { uuid ->
            fragment.showProgressBar()
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ADD_MESSAGE)
            when (feedItem.type) {
                TimestampedObject.ENTOURAGE_CARD -> {
                    val chatMessageWrapper = ChatMessageWrapper(ChatMessage(message))
                    val call = entourageRequest.addChatMessage(uuid, chatMessageWrapper)
                    call.enqueue(object : Callback<ChatMessageResponse> {
                        override fun onResponse(call: Call<ChatMessageResponse>, response: Response<ChatMessageResponse>) {
                            response.body()?.chatMessage?.let {
                                if (response.isSuccessful) {
                                    fragment.onFeedItemMessageSent(it)
                                    return
                                }
                            }
                            fragment.onFeedItemMessageSent(null)
                        }

                        override fun onFailure(call: Call<ChatMessageResponse>, t: Throwable) {
                            fragment.onFeedItemMessageSent(null)
                        }
                    })
                }
                else -> {
                    fragment.onFeedItemMessageSent(null)
                }
            }
        } ?: run {
            fragment.onFeedItemMessageSent(null)
        }
    }

    // ----------------------------------
    // Update user join requests
    // ----------------------------------
    override fun updateUserJoinRequest(userId: Int, status: String, feedItem: FeedItem) {
        fragment.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.ENTOURAGE_CARD -> {
                feedItem.uuid?.let { uuid ->
                    // Entourage user update status
                    when (status) {
                        FeedItem.JOIN_STATUS_ACCEPTED -> {
                            acceptEntourageJoinRequest(uuid, userId)
                        }
                        FeedItem.JOIN_STATUS_REJECTED -> {
                            rejectJoinEntourageRequest(uuid, userId)
                        }
                        else -> {
                            fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
                        }
                    }
                } ?: run {
                    fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
                }
            }
            else -> {
                // Unknown type
                fragment.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
            }
        }
    }

    private fun acceptEntourageJoinRequest(entourageUUID: String, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        val call = entourageRequest.updateUserEntourageStatus(entourageUUID, userId, user)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NONE)
                } else {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, response.code())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NETWORK)
            }
        })
    }

    private fun rejectJoinEntourageRequest(entourageUUID: String, userId: Int) {
        val call = entourageRequest.removeUserFromEntourage(entourageUUID, userId)
        call.enqueue(object : Callback<EntourageUserResponse> {
            override fun onResponse(call: Call<EntourageUserResponse>, response: Response<EntourageUserResponse>) {
                if (response.isSuccessful) {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NONE)
                } else {
                    fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, response.code())
                }
            }

            override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NETWORK)
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
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

}