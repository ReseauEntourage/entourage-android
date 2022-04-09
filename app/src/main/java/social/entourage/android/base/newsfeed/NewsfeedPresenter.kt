package social.entourage.android.base.newsfeed

import com.google.android.gms.maps.GoogleMap.OnGroundOverlayClickListener
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.request.*
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.create.CreateEntourageFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.base.map.MapClusterEntourageItem
import social.entourage.android.onboarding.InputNamesFragment
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Presenter controlling the NewsfeedFragment
 *
 * @see NewsfeedFragment
 */
class NewsfeedPresenter @Inject constructor(
    private val fragment: NewsfeedFragment?,
    internal val authenticationController: AuthenticationController,
    private val entourageRequest: EntourageRequest,
    private val invitationRequest: InvitationRequest) {

    val isOnboardingUser: Boolean
      get() = authenticationController.isOnboardingUser

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    var onClickListener: OnEntourageMarkerClickListener? = null
        private set
    var onGroundOverlayClickListener: OnEntourageGroundOverlayClickListener? = null
        private set

    fun start() {
        onClickListener = OnEntourageMarkerClickListener()
        onGroundOverlayClickListener = OnEntourageGroundOverlayClickListener()
    }

    var isShowNoEntouragesPopup: Boolean
        get() = authenticationController.isShowNoEntouragesPopup
        set(show) {
            authenticationController.isShowNoEntouragesPopup = show
        }

    fun openFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int) {
        try {
            val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
            FeedItemInformationFragment.newInstance(feedItem, invitationId, feedRank,false).show(fragmentManager, FeedItemInformationFragment.TAG)
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
        }
    }

    fun createEntourage(location: LatLng?, groupType: String, category: EntourageCategory?) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?: return
            CreateEntourageFragment.newExpertInstance(location, groupType, category).show(fragmentManager, CreateEntourageFragment.TAG)
        }
    }

    fun displayEntourageDisclaimer(groupType: String) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?:return
            EntourageDisclaimerFragment.newInstance(groupType).show(fragmentManager, EntourageDisclaimerFragment.TAG)
        }
    }

    fun getMyPendingInvitations() {
        val call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING)
        call.enqueue(object : Callback<InvitationListResponse> {
            override fun onResponse(call: Call<InvitationListResponse>, response: Response<InvitationListResponse>) {
                response.body()?.invitations?.let {
                    if (response.isSuccessful) {
                        fragment?.onInvitationsReceived(it)
                        return
                    }
                }
                fragment?.onNoInvitationReceived()
            }

            override fun onFailure(call: Call<InvitationListResponse>, t: Throwable) {
                fragment?.onNoInvitationReceived()
            }
        })
    }

    fun acceptInvitation(invitationId: Long) {
        val call = invitationRequest.acceptInvitation(invitationId)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {}
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {}
        })
    }

    fun resetUserOnboardingFlag() {
        authenticationController.isOnboardingUser = false
    }

    fun saveMapFilter() {
        authenticationController.saveMapFilter()
    }

    fun checkUserNamesInfos() {
        authenticationController.me?.let { user ->
            if (user.firstName.isNullOrEmpty() && user.lastName.isNullOrEmpty()) {
                fragment?.let { InputNamesFragment().show(it.parentFragmentManager,"InputFGTag") }
            }
        }
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------
    inner class OnEntourageMarkerClickListener : OnClusterItemClickListener<ClusterItem> {

        override fun onClusterItemClick(mapClusterItem: ClusterItem): Boolean {
            if (mapClusterItem is MapClusterEntourageItem) {
                fragment?.handleHeatzoneClick(mapClusterItem.position)
            }
            return true
        }
    }

    inner class OnEntourageGroundOverlayClickListener : OnGroundOverlayClickListener {
        private val entourageMarkerHashMap: MutableMap<LatLng, FeedItem?> = HashMap()
        fun addEntourageGroundOverlay(markerPosition: LatLng, feedItem: FeedItem?) {
            entourageMarkerHashMap[markerPosition] = feedItem
        }

        fun clear() {
            entourageMarkerHashMap.clear()
        }

        override fun onGroundOverlayClick(groundOverlay: GroundOverlay) {
            val markerPosition = groundOverlay.position
            if (entourageMarkerHashMap[markerPosition] != null) {
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_HEATZONECLICK)
                fragment?.handleHeatzoneClick(markerPosition)
            }
        }
    }

}