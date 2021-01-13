package social.entourage.android.newsfeed

import com.google.android.gms.maps.GoogleMap.OnGroundOverlayClickListener
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.request.*
import social.entourage.android.api.tape.Events.OnTourEncounterViewRequestedEvent
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.map.MapClusterEntourageItem
import social.entourage.android.map.MapClusterTourItem
import social.entourage.android.onboarding.InputNamesFragment
import social.entourage.android.tools.EntBus
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Presenter controlling the BaseNewsfeedFragment
 *
 * @see BaseNewsfeedFragment
 */
class NewsfeedPresenter @Inject constructor(
        private val fragment: BaseNewsfeedFragment?,
        internal val authenticationController: AuthenticationController,
        private val entourageRequest: EntourageRequest,
        private val tourRequest: TourRequest,
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

    fun incrementUserToursCount() {
        authenticationController.incrementUserToursCount()
    }

    var isShowNoEntouragesPopup: Boolean
        get() = authenticationController.isShowNoEntouragesPopup
        set(show) {
            authenticationController.isShowNoEntouragesPopup = show
        }

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

    fun createEntourage(location: LatLng?, groupType: String, category: EntourageCategory?) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?: return
            BaseCreateEntourageFragment.newInstance(location, groupType, category).show(fragmentManager, BaseCreateEntourageFragment.TAG)
        }
    }

    fun displayEntourageDisclaimer(groupType: String) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?:return
            EntourageDisclaimerFragment.newInstance(groupType).show(fragmentManager, EntourageDisclaimerFragment.TAG)
        }
    }

    fun shouldDisplayEncounterDisclaimer(): Boolean {
        return authenticationController.isShowEncounterDisclaimer
    }

    fun setDisplayEncounterDisclaimer(displayEncounterDisclaimer: Boolean) {
        authenticationController.isShowEncounterDisclaimer = displayEncounterDisclaimer
    }

    fun displayEncounterDisclaimer() {
        val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
        EncounterDisclaimerFragment().show(fragmentManager, EncounterDisclaimerFragment.TAG)
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
    // PRIVATE METHODS
    // ----------------------------------
    private fun openEncounter(encounter: Encounter) {
        fragment?.saveCameraPosition()
        EntBus.post(OnTourEncounterViewRequestedEvent(encounter))
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------
    inner class OnEntourageMarkerClickListener : OnClusterItemClickListener<ClusterItem> {
        private val encounterMarkerHashMap: MutableMap<ClusterItem, Encounter?> = HashMap()
        fun addEncounterMapClusterItem(mapClusterItem: ClusterItem, encounter: Encounter?) {
            encounterMarkerHashMap[mapClusterItem] = encounter
        }

        fun getEncounterMapClusterItem(encounterId: Long): ClusterItem? {
            for (mapClusterItem in encounterMarkerHashMap.keys) {
                if (encounterMarkerHashMap[mapClusterItem]?.id == encounterId) {
                    return mapClusterItem
                }
            }
            return null
        }

        fun removeEncounterMapClusterItem(encounterId: Long): ClusterItem? {
            val mapClusterItem = getEncounterMapClusterItem(encounterId) ?: return null
            encounterMarkerHashMap.remove(mapClusterItem)
            return mapClusterItem
        }

        fun clear() {
            encounterMarkerHashMap.clear()
        }

        override fun onClusterItemClick(mapClusterItem: ClusterItem): Boolean {
            when {
                encounterMarkerHashMap[mapClusterItem] != null -> {
                    openEncounter(encounterMarkerHashMap[mapClusterItem]!!)
                }
                mapClusterItem is MapClusterEntourageItem -> {
                    fragment?.handleHeatzoneClick(mapClusterItem.position)
                }
                mapClusterItem is MapClusterTourItem -> {
                    openFeedItem(mapClusterItem.tour, 0, 0)
                }
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
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_HEATZONECLICK)
                fragment?.handleHeatzoneClick(markerPosition)
            }
        }
    }

}