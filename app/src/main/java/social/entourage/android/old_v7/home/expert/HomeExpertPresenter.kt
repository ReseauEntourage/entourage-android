package social.entourage.android.old_v7.home.expert

import android.content.SharedPreferences
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.request.*
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

/**
 * Presenter controlling the HomeExpertFragment
 *
 * @see HomeExpertFragment
 */
class HomeExpertPresenter(private val fragment: HomeExpertFragment) {
    internal val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController
    private val entourageRequest: EntourageRequest
        get() = EntourageApplication.get().apiModule.entourageRequest
    private val invitationRequest: InvitationRequest
        get() = EntourageApplication.get().apiModule.invitationRequest
    private val sharedPreferences : SharedPreferences
        get() = EntourageApplication.get().sharedPreferences

    private val isOnboardingUser: Boolean
        get() = authenticationController.isOnboardingUser

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun checkIntentAction(content: PushNotificationContent, action: String) {
        when (action) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE,
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST,
            PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED -> if (content.isEntourageRelated) {
                openFeedItemFromUUID(content.joinableUUID, TimestampedObject.ENTOURAGE_CARD)
            }
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION -> content.extra?.let { extra ->
                openFeedItemFromUUID(extra.entourageId.toString(), TimestampedObject.ENTOURAGE_CARD, extra.invitationId.toLong())
            }
            PushNotificationContent.TYPE_INVITATION_STATUS -> content.extra?.let {
                if (content.isEntourageRelated) {
                    openFeedItemFromUUID(content.joinableUUID, TimestampedObject.ENTOURAGE_CARD)
                }
            }
        }
    }

    fun openFeedItemFromUUID(feedItemUUID: String, feedItemType: Int, invitationId: Long=0) {
        if(feedItemUUID.isBlank()) return
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        when (feedItemType) {
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest.retrieveEntourageById(feedItemUUID, 0, 0)
                call.enqueue(object : Callback<EntourageResponse> {
                    override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                        response.body()?.entourage?.let {
                            if (response.isSuccessful) {
                                fragment.openFeedItem(it, invitationId)
                            }
                        }
                    }
                    override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                    }
                })
            }
        }
    }

    fun openFeedItemFromShareURL(feedItemShareURL: String, feedItemType: Int) {
        when (feedItemType) {
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest.retrieveEntourageByShareURL(feedItemShareURL)
                call.enqueue(object : Callback<EntourageResponse> {
                    override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                        response.body()?.entourage?.let {
                            if (response.isSuccessful) {
                                fragment.openFeedItem(it)
                            }
                        }
                    }

                    override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                    }
                })
            }
        }
    }

    fun checkUserNamesInfo(): Boolean {
        authenticationController.me?.let { user ->
            if (user.firstName.isNullOrEmpty() && user.lastName.isNullOrEmpty()) {
                return true
            }
        }
        return false
    }

    fun isNavigation(): Boolean {
        return sharedPreferences.getBoolean("isNavNews", false)
    }

    fun navType(): String? {
        return sharedPreferences.getString("navType", null)
    }

    fun saveInfo(isNav:Boolean, type:String?) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isNavNews",isNav)
        editor.putString("navType",type)
        editor.apply()
    }

    fun storeActionZoneInfo(ignoreAddress: Boolean) {
        authenticationController.isIgnoringActionZone = ignoreAddress
        authenticationController.saveUserPreferences()
    }

    // ----------------------------------
    // INVITATIONS
    // ----------------------------------
    private fun getMyPendingInvitations() {
        val call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING)
        call.enqueue(object : Callback<InvitationListResponse> {
            override fun onResponse(call: Call<InvitationListResponse>, response: Response<InvitationListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.invitations?.let {
                        onInvitationsReceived(it)
                    }
                }
            }

            override fun onFailure(call: Call<InvitationListResponse>, t: Throwable) {
            }
        })
    }

    private fun acceptInvitation(invitationId: Long) {
        val call = invitationRequest.acceptInvitation(invitationId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {}
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Timber.e(t)
            }
        })
    }

    private fun resetUserOnboardingFlag() {
        authenticationController.isOnboardingUser = false
    }

    fun initializeInvitations() {
        // Check if it's a valid user and onboarding
        if (isOnboardingUser) {
            // Retrieve the list of invitations and then accept them automatically
            getMyPendingInvitations()
        }
    }

    private fun onInvitationsReceived(invitationList: List<Invitation>) {
        //during onboarding we check if the new user was invited to specific entourages and then automatically accept them
        if (isOnboardingUser && !invitationList.isNullOrEmpty()) {
            invitationList.forEach {
                acceptInvitation(it.id)
            }
            // Show the first invitation
            invitationList.first().let {
                openFeedItemFromUUID(it.entourageUUID, TimestampedObject.ENTOURAGE_CARD, it.id)
            }
        }
        resetUserOnboardingFlag()
    }
}