package social.entourage.android.home

import com.google.android.gms.maps.model.LatLng
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.request.*
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.onboarding.InputNamesFragment
import timber.log.Timber
import javax.inject.Inject

/**
 * Presenter controlling the HomeFragment
 *
 * @see HomeFragment
 */
class HomePresenter @Inject constructor(
    private val fragment: HomeFragment?,
    internal val authenticationController: AuthenticationController,
    private val entourageRequest: EntourageRequest,
    private val tourRequest: TourRequest,
    private val invitationRequest: InvitationRequest) {

    private val isOnboardingUser: Boolean
      get() = authenticationController.isOnboardingUser

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun openFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int) {
        try {
            val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
            FeedItemInformationFragment.newInstance(feedItem, invitationId, feedRank).show(fragmentManager, FeedItemInformationFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun openFeedItemFromUUID(feedItemUUID: String, feedItemType: Int, invitationId: Long) {
        if(feedItemUUID.isBlank()) return
        when (feedItemType) {
            TimestampedObject.ENTOURAGE_CARD -> {
                val call = entourageRequest.retrieveEntourageById(feedItemUUID, 0, 0)
                call.enqueue(object : Callback<EntourageResponse> {
                    override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                        response.body()?.entourage?.let {
                            if (response.isSuccessful) {
                                openFeedItem(it, invitationId, 0)
                            }
                        }
                    }
                    override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                    }
                })
            }
            TimestampedObject.TOUR_CARD -> {
                val call = tourRequest.retrieveTourById(feedItemUUID)
                call.enqueue(object : Callback<TourResponse> {
                    override fun onResponse(call: Call<TourResponse>, response: Response<TourResponse>) {
                        response.body()?.tour?.let {
                            if (response.isSuccessful) {
                                openFeedItem(it, invitationId, 0)
                            }
                        }
                    }
                    override fun onFailure(call: Call<TourResponse>, t: Throwable) {
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
                                openFeedItem(it,0,0)
                            }
                        }
                    }

                    override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                    }
                })
            }
        }
    }

    private fun getMyPendingInvitations() {
        val call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING)
        call.enqueue(object : Callback<InvitationListResponse> {
            override fun onResponse(call: Call<InvitationListResponse>, response: Response<InvitationListResponse>) {
                response.body()?.invitations?.let {
                    if (response.isSuccessful) {
                        onInvitationsReceived(it)
                        return
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
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    private fun resetUserOnboardingFlag() {
        authenticationController.isOnboardingUser = false
    }

    fun checkUserNamesInfos() {
        authenticationController.me?.let { user ->
            if (user.firstName.isNullOrEmpty() && user.lastName.isNullOrEmpty()) {
                fragment?.let { InputNamesFragment().show(it.parentFragmentManager,"InputFGTag") }
            }
        }
    }

    fun storeActionZoneInfo(ignoreAddress: Boolean) {
        authenticationController.isIgnoringActionZone = ignoreAddress
        authenticationController.saveUserPreferences()
    }

    // ----------------------------------
    // INVITATIONS
    // ----------------------------------
    fun initializeInvitations() {
        // Check if it's a valid user and onboarding
        if (isOnboardingUser) {
            // Retrieve the list of invitations and then accept them automatically
            getMyPendingInvitations()
            resetUserOnboardingFlag()
        }
    }

    fun onInvitationsReceived(invitationList: List<Invitation>) {
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
    }

    fun isNavigation(): Boolean {
        return EntourageApplication.get().sharedPreferences
            .getBoolean("isNavNews", false)
    }

    fun navType(): String? {
        return EntourageApplication.get().sharedPreferences
            .getString("navType", null)
    }

    fun saveInfo(isNav:Boolean, type:String?) {
        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",isNav)
        editor.putString("navType",type)
        editor.apply()
    }
}