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
 * Presenter controlling the EntourageInformationFragment
 * @see EntourageInformationFragment
 */
class EntourageInformationPresenter @Inject constructor(private val fragment: EntourageInformationFragment?) {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @Inject
    var tourRequest: TourRequest? = null

    @JvmField
    @Inject
    var entourageRequest: EntourageRequest? = null

    @JvmField
    @Inject
    var invitationRequest: InvitationRequest? = null

    // ----------------------------------
    // Api calls
    // ----------------------------------
    fun getFeedItem(feedItemUUID: String, feedItemType: Int, feedRank: Int, distance: Int) {
        if(feedItemUUID.isBlank()) return
        fragment?.showProgressBar()
        when (feedItemType) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest!!.retrieveTourById(feedItemUUID)
                call.enqueue(object : Callback<TourWrapper> {
                    override fun onResponse(call: Call<TourWrapper>, response: Response<TourWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemReceived(response.body()!!.tour)
                        } else {
                            fragment?.onFeedItemNotFound()
                        }
                    }

                    override fun onFailure(call: Call<TourWrapper>, t: Throwable) {
                        fragment?.onFeedItemNotFound()
                    }
                })
            }
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest!!.retrieveEntourageById(feedItemUUID, distance, feedRank)
                call.enqueue(object : Callback<EntourageWrapper> {
                    override fun onResponse(call: Call<EntourageWrapper>, response: Response<EntourageWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemReceived(response.body()!!.entourage)
                        } else {
                            fragment?.onFeedItemNotFound()
                        }
                    }

                    override fun onFailure(call: Call<EntourageWrapper>, t: Throwable) {
                        fragment?.onFeedItemNotFound()
                    }
                })
            }
            else -> {
                fragment?.onFeedItemNotFound()
            }
        }
    }

    fun getFeedItem(feedItemShareURL: String, feedItemType: Int) {
        fragment?.showProgressBar()
        when (feedItemType) {
            TimestampedObject.TOUR_CARD -> {
                fragment?.onFeedItemNotFound()
            }
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest!!.retrieveEntourageByShareURL(feedItemShareURL)
                call.enqueue(object : Callback<EntourageWrapper> {
                    override fun onResponse(call: Call<EntourageWrapper>, response: Response<EntourageWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemReceived(response.body()!!.entourage)
                        } else {
                            fragment?.onFeedItemNotFound()
                        }
                    }

                    override fun onFailure(call: Call<EntourageWrapper>, t: Throwable) {
                        fragment?.onFeedItemNotFound()
                    }
                })
            }
            else -> {
                fragment?.onFeedItemNotFound()
            }
        }
    }

    fun getFeedItemMembers(feedItem: FeedItem) {
        getFeedItemUsers(feedItem, null)
    }

    fun getFeedItemJoinRequests(feedItem: FeedItem) {
        getFeedItemUsers(feedItem, "group_feed")
    }

    private fun getFeedItemUsers(feedItem: FeedItem, context: String?) {
        fragment?.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest!!.retrieveTourUsers(feedItem.uuid)
                call.enqueue(object : Callback<TourUsersWrapper> {
                    override fun onResponse(call: Call<TourUsersWrapper>, response: Response<TourUsersWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemUsersReceived(response.body()!!.users, context)
                        } else {
                            fragment?.onFeedItemNoUserReceived()
                        }
                    }

                    override fun onFailure(call: Call<TourUsersWrapper>, t: Throwable) {
                        fragment?.onFeedItemNoUserReceived()
                    }
                })
            }
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest!!.retrieveEntourageUsers(feedItem.uuid, context)
                call.enqueue(object : Callback<TourUsersWrapper> {
                    override fun onResponse(call: Call<TourUsersWrapper>, response: Response<TourUsersWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemUsersReceived(response.body()!!.users, context)
                        } else {
                            fragment?.onFeedItemNoUserReceived()
                        }
                    }

                    override fun onFailure(call: Call<TourUsersWrapper>, t: Throwable) {
                        fragment?.onFeedItemNoUserReceived()
                    }
                })
            }
            else -> {
                fragment?.onFeedItemNoUserReceived()
            }
        }
    }

    fun getFeedItemMessages(feedItem: FeedItem) {
        getFeedItemMessages(feedItem, null)
    }

    fun getFeedItemMessages(feedItem: FeedItem, lastMessageDate: Date?) {
        fragment?.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest!!.retrieveTourMessages(feedItem.uuid, lastMessageDate)
                call.enqueue(object : Callback<ChatMessagesWrapper> {
                    override fun onResponse(call: Call<ChatMessagesWrapper>, response: Response<ChatMessagesWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemMessagesReceived(response.body()!!.chatMessages)
                        } else {
                            fragment?.onFeedItemNoNewMessages()
                        }
                    }

                    override fun onFailure(call: Call<ChatMessagesWrapper>, t: Throwable) {
                        fragment?.onFeedItemNoNewMessages()
                    }
                })
            }
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest!!.retrieveEntourageMessages(feedItem.uuid, lastMessageDate)
                call.enqueue(object : Callback<ChatMessagesWrapper> {
                    override fun onResponse(call: Call<ChatMessagesWrapper>, response: Response<ChatMessagesWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemMessagesReceived(response.body()!!.chatMessages)
                        } else {
                            fragment?.onFeedItemNoNewMessages()
                        }
                    }

                    override fun onFailure(call: Call<ChatMessagesWrapper>, t: Throwable) {
                        fragment?.onFeedItemNoNewMessages()
                    }
                })
            }
            else -> {
                fragment?.onFeedItemNoNewMessages()
            }
        }
    }

    fun sendFeedItemMessage(feedItem: FeedItem, message: String) {
        fragment?.showProgressBar()
        EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ADD_MESSAGE)
        val chatMessageWrapper = ChatMessageWrapper()
        chatMessageWrapper.chatMessage = ChatMessage(message)
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest!!.chatMessage(feedItem.uuid, chatMessageWrapper)
                call.enqueue(object : Callback<ChatMessageWrapper> {
                    override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemMessageSent(response.body()!!.chatMessage)
                        } else {
                            fragment?.onFeedItemMessageSent(null)
                        }
                    }

                    override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                        fragment?.onFeedItemMessageSent(null)
                    }
                })
            }
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest!!.chatMessage(feedItem.uuid, chatMessageWrapper)
                call.enqueue(object : Callback<ChatMessageWrapper> {
                    override fun onResponse(call: Call<ChatMessageWrapper>, response: Response<ChatMessageWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemMessageSent(response.body()!!.chatMessage)
                        } else {
                            fragment?.onFeedItemMessageSent(null)
                        }
                    }

                    override fun onFailure(call: Call<ChatMessageWrapper>, t: Throwable) {
                        fragment?.onFeedItemMessageSent(null)
                    }
                })
            }
            else -> {
                fragment?.onFeedItemMessageSent(null)
            }
        }
    }

    fun getFeedItemEncounters(feedItem: FeedItem) {
        fragment?.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest!!.retrieveTourEncounters(feedItem.uuid)
                call.enqueue(object : Callback<EncountersWrapper> {
                    override fun onResponse(call: Call<EncountersWrapper>, response: Response<EncountersWrapper>) {
                        if (response.isSuccessful) {
                            fragment?.onFeedItemEncountersReceived(response.body()!!.encounters)
                        } else {
                            fragment?.onFeedItemEncountersReceived(null)
                        }
                    }

                    override fun onFailure(call: Call<EncountersWrapper>, t: Throwable) {
                        fragment?.onFeedItemEncountersReceived(null)
                    }
                })
            }
            TimestampedObject.ENTOURAGE_CARD -> {
                //Entourage doesn't have encounters
                fragment?.onFeedItemEncountersReceived(null)
            }
            else -> {
                fragment?.onFeedItemEncountersReceived(null)
            }
        }
    }

    // ----------------------------------
    // Update user join requests
    // ----------------------------------
    fun updateUserJoinRequest(userId: Int, status: String, feedItem: FeedItem) {
        fragment?.showProgressBar()
        when (feedItem.type) {
            TimestampedObject.TOUR_CARD -> {
                // Tour user update status
                when {
                    FeedItem.JOIN_STATUS_ACCEPTED == status -> {
                        acceptTourJoinRequest(feedItem.uuid, userId)
                    }
                    FeedItem.JOIN_STATUS_REJECTED == status -> {
                        rejectJoinTourRequest(feedItem.uuid, userId)
                    }
                    else -> {
                        fragment?.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
                    }
                }
            }
            TimestampedObject.ENTOURAGE_CARD -> {
                // Entourage user update status
                when {
                    FeedItem.JOIN_STATUS_ACCEPTED == status -> {
                        acceptEntourageJoinRequest(feedItem.uuid, userId)
                    }
                    FeedItem.JOIN_STATUS_REJECTED == status -> {
                        rejectJoinEntourageRequest(feedItem.uuid, userId)
                    }
                    else -> {
                        fragment?.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
                    }
                }
            }
            else -> {
                // Unknown type
                fragment?.onUserJoinRequestUpdated(userId, status, EntourageError.ERROR_UNKNOWN)
            }
        }
    }

    protected fun acceptTourJoinRequest(tourUUID: String?, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        val call = tourRequest!!.updateUserTourStatus(tourUUID, userId, user)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (fragment != null) {
                    if (response.isSuccessful) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NONE)
                    } else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, response.code())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                fragment?.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NETWORK)
            }
        })
    }

    protected fun rejectJoinTourRequest(tourUUID: String?, userId: Int) {
        val call = tourRequest!!.removeUserFromTour(tourUUID, userId)
        call.enqueue(object : Callback<TourUserWrapper?> {
            override fun onResponse(call: Call<TourUserWrapper?>, response: Response<TourUserWrapper?>) {
                if (fragment != null) {
                    if (response.isSuccessful) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NONE)
                    } else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, response.code())
                    }
                }
            }

            override fun onFailure(call: Call<TourUserWrapper?>, t: Throwable) {
                fragment?.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NETWORK)
            }
        })
    }

    protected fun acceptEntourageJoinRequest(entourageUUID: String?, userId: Int) {
        val status = HashMap<String, String>()
        status["status"] = FeedItem.JOIN_STATUS_ACCEPTED
        val user = HashMap<String, Any>()
        user["user"] = status
        val call = entourageRequest!!.updateUserEntourageStatus(entourageUUID, userId, user)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (fragment != null) {
                    if (response.isSuccessful) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NONE)
                    } else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, response.code())
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                fragment?.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_ACCEPTED, EntourageError.ERROR_NETWORK)
            }
        })
    }

    protected fun rejectJoinEntourageRequest(entourageUUID: String?, userId: Int) {
        val call = entourageRequest!!.removeUserFromEntourage(entourageUUID, userId)
        call.enqueue(object : Callback<TourUserWrapper?> {
            override fun onResponse(call: Call<TourUserWrapper?>, response: Response<TourUserWrapper?>) {
                if (fragment != null) {
                    if (response.isSuccessful) {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NONE)
                    } else {
                        fragment.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, response.code())
                    }
                }
            }

            override fun onFailure(call: Call<TourUserWrapper?>, t: Throwable) {
                fragment?.onUserJoinRequestUpdated(userId, FeedItem.JOIN_STATUS_REJECTED, EntourageError.ERROR_NETWORK)
            }
        })
    }

    // ----------------------------------
    // Update received invitation
    // ----------------------------------
    fun acceptInvitation(invitationId: Long) {
        val call = invitationRequest!!.acceptInvitation(invitationId)
        call.enqueue(object : Callback<InvitationWrapper?> {
            override fun onResponse(call: Call<InvitationWrapper?>, response: Response<InvitationWrapper?>) {
                if (fragment != null) {
                    if (response.isSuccessful) {
                        fragment.onInvitationStatusUpdated(true, Invitation.STATUS_ACCEPTED)
                    } else {
                        fragment.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED)
                    }
                }
            }

            override fun onFailure(call: Call<InvitationWrapper?>, t: Throwable) {
                fragment?.onInvitationStatusUpdated(false, Invitation.STATUS_ACCEPTED)
            }
        })
    }

    fun rejectInvitation(invitationId: Long) {
        val call = invitationRequest!!.refuseInvitation(invitationId)
        call.enqueue(object : Callback<InvitationWrapper?> {
            override fun onResponse(call: Call<InvitationWrapper?>, response: Response<InvitationWrapper?>) {
                if (fragment != null) {
                    if (response.isSuccessful) {
                        fragment.onInvitationStatusUpdated(true, Invitation.STATUS_REJECTED)
                    } else {
                        fragment.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED)
                    }
                }
            }

            override fun onFailure(call: Call<InvitationWrapper?>, t: Throwable) {
                fragment?.onInvitationStatusUpdated(false, Invitation.STATUS_REJECTED)
            }
        })
    }

}