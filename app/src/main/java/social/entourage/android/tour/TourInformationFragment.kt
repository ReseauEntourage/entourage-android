package social.entourage.android.tour

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.layout_entourage_options.*
import kotlinx.android.synthetic.main.fragment_entourage_information.*
import kotlinx.android.synthetic.main.layout_invite_source.*
import kotlinx.android.synthetic.main.layout_public_entourage_information.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourInformation
import social.entourage.android.api.tape.Events
import social.entourage.android.entourage.information.*
import social.entourage.android.location.EntourageLocation
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.tools.Utils
import social.entourage.android.view.EntourageSnackbar
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TourInformationFragment : FeedItemInformationFragment(){
    @Inject lateinit var presenter: TourInformationPresenter
    override fun presenter(): FeedItemInformationPresenter { return presenter}

    private var mListener: OnTourInformationFragmentFinish? = null
    private var hiddenMapFragment: SupportMapFragment? = null
    private var hiddenGoogleMap: GoogleMap? = null
    private var isTakingSnapshot = false
    private var mapSnapshot: Bitmap? = null
    private var takeSnapshotOnCameraMove = false
    private var tourInformationList: MutableList<TourInformation> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerTourInformationComponent.builder()
                .entourageComponent(entourageComponent)
                .tourInformationModule(TourInformationModule(this))
                .build()
                .inject(this)
    }

    override fun onAttach(context: Context) {
        if (context !is OnTourInformationFragmentFinish) {
            throw ClassCastException("$context must implement OnTourInformationFragmentFinish")
        }
        mListener = context
        super.onAttach(context)
    }

    override fun onDetach() {
        mListener = null
        super.onDetach()
    }

    override fun getItemType(): Int {
        return TimestampedObject.TOUR_CARD
    }

    override fun onStopTourButton() {
        if (feedItem.status == FeedItem.STATUS_ON_GOING || feedItem.status == FeedItem.STATUS_OPEN) {
            val tour = feedItem as Tour
            //compute distance
            var distance = 0.0f
            val tourPointsList = tour.tourPoints
            if (tourPointsList.size > 1) {
                var startPoint = tourPointsList[0]
                for (i in 1 until tourPointsList.size) {
                    val p = tourPointsList[i]
                    distance += p.distanceTo(startPoint)
                    startPoint = p
                }
            }
            tour.distance = distance

            //duration
            val now = Date()
            tour.duration = Utils.getDateStringFromSeconds(now.time - tour.getStartTime().time)

            //hide the options
            entourage_info_options?.visibility = View.GONE

            //show stop tour activity
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
            mListener?.showStopTourActivity(tour)
        } else if (feedItem.status == FeedItem.STATUS_CLOSED) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
            entourageServiceConnection.boundService?.freezeTour(feedItem as Tour)
        }
    }

    override fun onJoinTourButton() {
        if (entourageServiceConnection.boundService != null) {
            showProgressBar()
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENTOURAGE_VIEW_ASK_JOIN)
            entourageServiceConnection.boundService?.requestToJoinTour(feedItem as Tour?)
            entourage_info_options?.visibility = View.GONE
        } else {
            entourage_information_coordinator_layout?.let {EntourageSnackbar.make(it,  R.string.tour_join_request_message_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    override fun showInviteSource() {
        entourage_info_invite_source_layout?.visibility = View.VISIBLE
        invite_source_description?.setText(R.string.invite_source_to_tour_description)
    }

    override fun onPushNotificationChatMessageReceived(message: Message): Boolean {
        //we received a chat notification
        //check if it is referring to this feed item
        val content = message.content ?: return false
        if (content.isEntourageRelated) {
            return false
        }
        else if (content.joinableId != feedItem.id) {
            return false
        }
        //retrieve the last messages from server
        scrollToLastCard = true
        presenter.getFeedItemMessages(feedItem)
        return true
    }

    override fun initializeOptionsView() {
        entourage_option_stop?.visibility = View.GONE
        entourage_option_quit?.visibility = View.GONE
        entourage_option_edit?.visibility = View.GONE
        entourage_option_share?.visibility = View.GONE
        entourage_option_report?.visibility = View.GONE
        entourage_option_join?.visibility = View.GONE
        entourage_option_contact?.visibility = View.GONE
        entourage_option_promote?.visibility = View.GONE
        val hideJoinButton = feedItem.isPrivate() || FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || feedItem.isFreezed()
        entourage_option_join?.visibility =  View.GONE
        entourage_option_contact?.visibility = if (hideJoinButton) View.GONE else View.VISIBLE
        val authorId = feedItem.author?.userID ?: return
        val myId = EntourageApplication.me(activity)?.id ?: return
        if (authorId != myId) {
            if ((FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus || FeedItem.JOIN_STATUS_ACCEPTED == feedItem.joinStatus) && !feedItem.isFreezed()) {
                entourage_option_quit?.visibility = View.VISIBLE
                entourage_option_quit?.setText(if (FeedItem.JOIN_STATUS_PENDING == feedItem.joinStatus) R.string.tour_info_options_cancel_request else R.string.tour_info_options_quit_tour)
            }
        } else {
            entourage_option_stop?.visibility = if (feedItem.isFreezed() || !feedItem.canBeClosed()) View.GONE else View.VISIBLE
            entourage_option_stop?.setText(if (feedItem.isClosed()) R.string.tour_info_options_freeze_tour else R.string.tour_info_options_stop_tour)
        }
    }

    override fun addSpecificCards() {
        if (feedItem.type == TimestampedObject.TOUR_CARD) {
            val now = Date()
            //add the start time
            if (FeedItem.STATUS_ON_GOING == feedItem.status) {
                addDiscussionTourStartCard(now)
            }

            //check if we need to add the Tour closed card
            if (feedItem.isClosed()) {
                addDiscussionTourEndCard(now)
            }
        }
    }

    private fun updateMap() {
        if (mapFragment?.isAdded == true) {
            drawFeedItemOnMap()
        } else {
            initializeMap()
        }
    }

    private fun getTrackColor(type: String, date: Date): Int {
        if (context == null) return Color.GRAY
        val color = ContextCompat.getColor(requireContext(), Tour.getTypeColorRes(type))
        return if (!BaseNewsfeedFragment.isToday(date)) {
            BaseNewsfeedFragment.getTransparentColor(color)
        } else color
    }

    override fun drawMap(googleMap: GoogleMap) {
        val tour = feedItem as Tour? ?: return
        val tourPoints = tour.tourPoints
        if (tourPoints.size > 0) {
            //setup the camera position to starting point
            val startPoint = tourPoints[0]
            val cameraPosition = CameraPosition(LatLng(startPoint.latitude, startPoint.longitude), EntourageLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0F, 0F)
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            val markerOptions = MarkerOptions().position(LatLng(startPoint.latitude, startPoint.longitude))
            googleMap.addMarker(markerOptions)

            //add the tour points
            val line = PolylineOptions()
            val color = getTrackColor(tour.tourType, tour.getStartTime())
            line.zIndex(2f)
            line.width(15f)
            line.color(color)
            for (tourPoint in tourPoints) {
                line.add(tourPoint.location)
            }
            googleMap.addPolyline(line)
        }
    }

    override fun initializeHiddenMap() {
        if (hiddenMapFragment != null) return
        if (!isAdded) return
        try {
            val googleMapOptions = GoogleMapOptions()
            googleMapOptions.zOrderOnTop(true)
            SupportMapFragment.newInstance(googleMapOptions)?.let {
                hiddenMapFragment = it
                childFragmentManager.beginTransaction().replace(R.id.tour_info_hidden_map_layout, it).commit()
                it.getMapAsync { googleMap ->
                    googleMap.uiSettings.isMyLocationButtonEnabled = false
                    googleMap.uiSettings.isMapToolbarEnabled = false
                    googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                            activity, R.raw.map_styles_json))
                    if (tourInformationList.size > 0) {
                        val tourTimestamp = tourInformationList[0]
                        if (tourTimestamp.locationPoint != null) {
                            //put the pin
                            val pin = MarkerOptions().position(tourTimestamp.locationPoint.location)
                            googleMap.addMarker(pin)
                            //move the camera
                            val camera = CameraUpdateFactory.newLatLngZoom(tourTimestamp.locationPoint.location, MAP_SNAPSHOT_ZOOM.toFloat())
                            googleMap.moveCamera(camera)
                        }
                    } else {
                        googleMap.moveCamera(CameraUpdateFactory.zoomTo(MAP_SNAPSHOT_ZOOM.toFloat()))
                    }
                    googleMap.setOnMapLoadedCallback { getMapSnapshot() }
                    googleMap.setOnCameraIdleListener {
                        if (takeSnapshotOnCameraMove) {
                            getMapSnapshot()
                            hiddenGoogleMap = null
                        }
                    }
                    hiddenGoogleMap = googleMap
                }
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun getMapSnapshot() {
        if (hiddenGoogleMap == null) return
        if (tourInformationList.size == 0) {
            hiddenGoogleMap = null
            return
        }
        val tourTimestamp = tourInformationList[0]
        isTakingSnapshot = true
        //take the snapshot
        hiddenGoogleMap?.let { hiddenMap ->
            hiddenMap.snapshot { bitmap -> //save the snapshot
                mapSnapshot = bitmap
                snapshotTaken(tourTimestamp)
                //signal it has finished taking the snapshot
                isTakingSnapshot = false
                //check if we need more snapshots
                if (tourInformationList.size > 1) {
                    val nextTourTimestamp = tourInformationList[1]
                    if (nextTourTimestamp.locationPoint != null) {
                        val distance = nextTourTimestamp.locationPoint.distanceTo(tourTimestamp.locationPoint)
                        val visibleRegion = hiddenMap.projection.visibleRegion
                        val nearLeft = visibleRegion.nearLeft
                        val nearRight = visibleRegion.nearRight
                        val result = floatArrayOf(0f)
                        Location.distanceBetween(nearLeft.latitude, nearLeft.longitude, nearRight.latitude, nearRight.longitude, result)
                        takeSnapshotOnCameraMove = distance < result[0]

                        //put the pin
                        hiddenMap.clear()
                        val pin = MarkerOptions().position(nextTourTimestamp.locationPoint.location)
                        hiddenMap.addMarker(pin)
                        //move the camera
                        val camera = CameraUpdateFactory.newLatLngZoom(nextTourTimestamp.locationPoint.location, MAP_SNAPSHOT_ZOOM.toFloat())
                        hiddenMap.moveCamera(camera)
                    }
                } else {
                    hiddenGoogleMap = null
                }
                tourInformationList.remove(tourTimestamp)
            }
        }
    }

    private fun snapshotTaken(tourInformation: TourInformation?) {
        if (mapSnapshot == null || tourInformation == null) return
        tourInformation.snapshot = mapSnapshot
        discussionAdapter.updateCard(tourInformation)
    }

    private fun addDiscussionTourStartCard(now: Date) {
        val distance = 0f
        val duration: Long = if (!feedItem.isClosed()) (now.time - feedItem.getStartTime().time) else 0L
        val startPoint = feedItem.getStartPoint()
        val tourTimestamp = TourInformation(
                feedItem.getStartTime(),
                now,
                feedItem.type,
                FeedItem.STATUS_ON_GOING,
                startPoint,
                duration,
                distance
        )
        discussionAdapter.addCardInfo(tourTimestamp)
    }

    override fun addDiscussionTourEndCard(now: Date) {
        var distance = 0f
        val duration = feedItem.getEndTime()?.let {it.time - feedItem.getStartTime().time } ?: 0L
        val tour = feedItem as Tour
        val tourPointsList = tour.tourPoints
        if (tourPointsList.size > 1) {
            var startPoint = tourPointsList[0]
            for (i in 1 until tourPointsList.size) {
                val p = tourPointsList[i]
                distance += p.distanceTo(startPoint)
                startPoint = p
            }
        }
        //TODO Should we use distance and duration from tour object ?
        val tourTimestamp = TourInformation(
                tour.getEndTime(),
                tour.getEndTime() ?: now,
                tour.type,
                FeedItem.STATUS_CLOSED,
                tour.getEndPoint(),
                duration,
                distance
        )
        discussionAdapter.addCardInfoAfterTimestamp(tourTimestamp)
    }

    override fun updateMetadataView() {
        // show the view only for outing
        entourage_info_metadata_layout?.visibility = View.GONE
    }

    override fun loadPrivateCards() {
        super.loadPrivateCards()
        if (feedItem.isMine(context)) {
            presenter.getFeedItemEncounters(feedItem as Tour)
        }
    }

    // ----------------------------------
    // Bus handling
    // ----------------------------------
    override fun setReadOnly() {
        val encounters = feedItem.getTypedCardInfoList(TimestampedObject.ENCOUNTER)
        if (encounters.isNullOrEmpty()) return
        for (timestampedObject in encounters) {
            val encounter = timestampedObject as Encounter
            encounter.isReadOnly = true
            discussionAdapter.updateCard(encounter)
        }
    }

    @Subscribe
    override fun onUserJoinRequestUpdateEvent(event: Events.OnUserJoinRequestUpdateEvent) {
        super.onUserJoinRequestUpdateEvent(event)
    }

    @Subscribe
    override fun onEntourageUpdated(event: Events.OnEntourageUpdated) {
        super.onEntourageUpdated(event)
        updateMap()
    }

    @Subscribe
    fun onEncounterUpdated(event: Events.OnEncounterUpdated) {
        val updatedEncounter = event.encounter ?: return
        val oldEncounter = discussionAdapter.findCard(TimestampedObject.ENCOUNTER, updatedEncounter.id) as Encounter?
                ?: return
        feedItem.removeCardInfo(oldEncounter)
        discussionAdapter.updateCard(updatedEncounter)
    }

    fun onFeedItemEncountersReceived(encounterList: List<Encounter>?) {
        if (encounterList != null) {
            EntourageApplication.me(context)?.let {
                for (encounter in encounterList) {
                    encounter.setIsMyEncounter(encounter.userId == it.id)
                    encounter.isReadOnly = feedItem.isClosed()
                }
            }
            feedItem.addCardInfoList(encounterList)
        }

        //hide the progress bar
        hideProgressBar()

        //update the discussion list
        updateDiscussionList()
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    interface OnTourInformationFragmentFinish {
        fun showStopTourActivity(tour: Tour)
    }

    companion object {
        private  const val MAP_SNAPSHOT_ZOOM = 15
    }
}