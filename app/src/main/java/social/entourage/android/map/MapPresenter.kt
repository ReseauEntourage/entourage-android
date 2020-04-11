package social.entourage.android.map

import com.google.android.gms.maps.GoogleMap.OnGroundOverlayClickListener
import com.google.android.gms.maps.model.GroundOverlay
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageEvents
import social.entourage.android.api.InvitationRequest
import social.entourage.android.api.model.Invitation
import social.entourage.android.api.model.Invitation.InvitationWrapper
import social.entourage.android.api.model.Invitation.InvitationsWrapper
import social.entourage.android.api.model.map.Encounter
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.tape.Events.OnTourEncounterViewRequestedEvent
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.EntourageInformationFragment
import social.entourage.android.tools.BusProvider
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment
import java.util.*
import javax.inject.Inject

/**
 * Presenter controlling the MapFragment
 *
 * @see MapFragment
 */
class MapPresenter @Inject constructor(
        private val fragment: MapFragment?,
        private val authenticationController: AuthenticationController?,
        private val invitationRequest: InvitationRequest) {

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
        authenticationController?.incrementUserToursCount()
    }

    var isShowNoEntouragesPopup: Boolean
        get() = authenticationController!=null && authenticationController.isShowNoEntouragesPopup
        set(show) {
            authenticationController?.isShowNoEntouragesPopup = show
        }

    fun openFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int) {
        val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
        EntourageInformationFragment.newInstance(feedItem, invitationId, feedRank).show(fragmentManager, EntourageInformationFragment.TAG)
    }

    fun openFeedItem(feedItemUUID: String, feedItemType: Int, invitationId: Long) {
        val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
        EntourageInformationFragment.newInstance(feedItemUUID, feedItemType, invitationId).show(fragmentManager, EntourageInformationFragment.TAG)
    }

    fun openFeedItem(feedItemShareURL: String, feedItemType: Int) {
        val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
        EntourageInformationFragment.newInstance(feedItemShareURL, feedItemType).show(fragmentManager, EntourageInformationFragment.TAG)
    }

    fun createEntourage(location: LatLng?, groupType: String, category: EntourageCategory?) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?: return
            BaseCreateEntourageFragment.newInstance(location, groupType, category).show(fragmentManager, BaseCreateEntourageFragment.TAG)
        }
    }

    fun displayEntourageDisclaimer(groupType: String?) {
        if (fragment != null && !fragment.isStateSaved) {
            val fragmentManager = fragment.activity?.supportFragmentManager ?:return
            EntourageDisclaimerFragment.newInstance(groupType).show(fragmentManager, EntourageDisclaimerFragment.TAG)
        }
    }

    fun shouldDisplayEncounterDisclaimer(): Boolean {
        return authenticationController != null && authenticationController.isShowEncounterDisclaimer
    }

    fun setDisplayEncounterDisclaimer(displayEncounterDisclaimer: Boolean) {
        authenticationController?.isShowEncounterDisclaimer = displayEncounterDisclaimer
    }

    fun displayEncounterDisclaimer() {
        val fragmentManager = fragment?.activity?.supportFragmentManager ?: return
        EncounterDisclaimerFragment.newInstance().show(fragmentManager, EncounterDisclaimerFragment.TAG)
    }

    val myPendingInvitations: Unit
        get() {
            val call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING)
            call.enqueue(object : Callback<InvitationsWrapper> {
                override fun onResponse(call: Call<InvitationsWrapper>, response: Response<InvitationsWrapper>) {
                    if (response.isSuccessful) {
                        fragment?.onInvitationsReceived(response.body()?.invitations)
                    } else {
                        fragment?.onInvitationsReceived(null)
                    }
                }

                override fun onFailure(call: Call<InvitationsWrapper>, t: Throwable) {
                    fragment?.onInvitationsReceived(null)
                }
            })
        }

    fun acceptInvitation(invitationId: Long) {
        val call = invitationRequest.acceptInvitation(invitationId)
        call.enqueue(object : Callback<InvitationWrapper?> {
            override fun onResponse(call: Call<InvitationWrapper?>, response: Response<InvitationWrapper?>) {}
            override fun onFailure(call: Call<InvitationWrapper?>, t: Throwable) {}
        })
    }

    fun resetUserOnboardingFlag() {
        val me = authenticationController?.user ?: return
        me.isOnboardingUser = false
        authenticationController.saveUser(me)
    }

    fun saveMapFilter() {
        authenticationController?.saveMapFilter()
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun openEncounter(encounter: Encounter?) {
        fragment?.saveCameraPosition()
        BusProvider.instance.post(OnTourEncounterViewRequestedEvent(encounter))
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------
    inner class OnEntourageMarkerClickListener : OnClusterItemClickListener<MapClusterItem> {
        val encounterMarkerHashMap: MutableMap<MapClusterItem, Encounter?> = HashMap()
        fun addEncounterMapClusterItem(mapClusterItem: MapClusterItem, encounter: Encounter?) {
            encounterMarkerHashMap[mapClusterItem] = encounter
        }

        fun getEncounterMapClusterItem(encounterId: Long): MapClusterItem? {
            for (mapClusterItem in encounterMarkerHashMap.keys) {
                if (encounterMarkerHashMap[mapClusterItem]?.id == encounterId) {
                    return mapClusterItem
                }
            }
            return null
        }

        fun removeEncounterMapClusterItem(encounterId: Long): MapClusterItem? {
            val mapClusterItem = getEncounterMapClusterItem(encounterId)
            if (mapClusterItem != null) {
                encounterMarkerHashMap.remove(mapClusterItem)
            }
            return mapClusterItem
        }

        fun clear() {
            encounterMarkerHashMap.clear()
        }

        override fun onClusterItemClick(mapClusterItem: MapClusterItem): Boolean {
            if (encounterMarkerHashMap[mapClusterItem] != null) {
                openEncounter(encounterMarkerHashMap[mapClusterItem])
            } else {
                val mapItem: Any? = mapClusterItem.mapItem ?: return true
                if (mapItem is FeedItem) {
                    if (FeedItem.TOUR_CARD == mapItem.type) {
                        openFeedItem(mapItem, 0, 0)
                    } else {
                        fragment?.handleHeatzoneClick(mapClusterItem.position)
                    }
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