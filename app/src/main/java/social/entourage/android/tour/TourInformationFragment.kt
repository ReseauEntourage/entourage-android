package social.entourage.android.tour

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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
import kotlinx.android.synthetic.main.layout_public_entourage_header.*
import kotlinx.android.synthetic.main.layout_public_entourage_information.*
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageComponent
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourInformation
import social.entourage.android.api.tape.Events
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.entourage.information.*
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.newsfeed.NewsfeedFragment
import social.entourage.android.tools.Utils
import social.entourage.android.tools.view.EntSnackbar
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TourInformationFragment : FeedItemInformationFragment(){
    @Inject lateinit var presenter: TourInformationPresenter
    override fun presenter(): FeedItemInformationPresenter { return presenter}

    val tour: Tour
        get() = feedItem as Tour

    private var mListener: OnTourInformationFragmentFinish? = null
    private var hiddenMapFragment: SupportMapFragment? = null
    private var hiddenGoogleMap: GoogleMap? = null
    private var isTakingSnapshot = false
    private var mapSnapshot: Bitmap? = null
    private var takeSnapshotOnCameraMove = false
    private var tourInformationList: MutableList<TourInformation> = ArrayList()

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
        if (tour.isOpen()) {
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
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
            mListener?.showStopTourFragment(tour)
        } else if (tour.isClosed()) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_OPTIONS_CLOSE)
            serviceConnection.boundService?.freezeTour(tour)
        }
    }

    override fun onJoinButton() {
        if (serviceConnection.boundService != null) {
            showProgressBar()
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENTOURAGE_VIEW_ASK_JOIN)
            serviceConnection.boundService?.requestToJoinTour(tour)
            entourage_info_options?.visibility = View.GONE
        } else {
            entourage_information_coordinator_layout?.let {EntSnackbar.make(it,  R.string.tour_join_request_message_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    override fun showInviteSource(isShareOnly:Boolean) {
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
        else if (content.joinableId != tour.id) {
            return false
        }
        //retrieve the last messages from server
        scrollToLastCard = true
        presenter.getFeedItemMessages(tour)
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

        entourage_option_reopen?.visibility = View.GONE

        val hideJoinButton = tour.isPrivate() || FeedItem.JOIN_STATUS_PENDING == tour.joinStatus || tour.isFreezed()
        entourage_option_join?.visibility =  View.GONE
        entourage_option_contact?.visibility = if (hideJoinButton) View.GONE else View.VISIBLE
        val authorId = tour.author?.userID ?: return
        val myId = EntourageApplication.me(activity)?.id ?: return
        if (authorId != myId) {
            if ((FeedItem.JOIN_STATUS_PENDING == tour.joinStatus || FeedItem.JOIN_STATUS_ACCEPTED == tour.joinStatus) && !tour.isFreezed()) {
                entourage_option_quit?.visibility = View.VISIBLE
                entourage_option_quit?.setText(if (FeedItem.JOIN_STATUS_PENDING == tour.joinStatus) R.string.tour_info_options_cancel_request else R.string.tour_info_options_quit_tour)
            }
        } else {
            entourage_option_stop?.visibility = if (tour.isFreezed() || !tour.canBeClosed()) View.GONE else View.VISIBLE
            entourage_option_stop?.setText(if (tour.isClosed()) R.string.tour_info_options_freeze_tour else R.string.tour_info_options_stop_tour)
        }

        ui_tv_button_close?.text = getString(R.string.tour_stop)
    }

    override fun addSpecificCards() {
        if (tour.type == TimestampedObject.TOUR_CARD) {
            val now = Date()
            //add the start time
            if (Tour.STATUS_ON_GOING == tour.status) {
                addDiscussionTourStartCard(now)
            }

            //check if we need to add the Tour closed card
            if (tour.isClosed()) {
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
        return if (!NewsfeedFragment.isToday(date)) {
            NewsfeedFragment.getTransparentColor(color)
        } else color
    }

    override fun drawMap(googleMap: GoogleMap) {
        val tourPoints = tour.tourPoints
        if (tourPoints.size > 0) {
            //setup the camera position to starting point
            val startPoint = tourPoints[0]
            val cameraPosition = CameraPosition(LatLng(startPoint.latitude, startPoint.longitude), EntLocation.INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW, 0F, 0F)
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
            val mapFragment = SupportMapFragment.newInstance(googleMapOptions)
            hiddenMapFragment = mapFragment
            childFragmentManager.beginTransaction().replace(R.id.tour_info_hidden_map_layout, mapFragment).commit()
            mapFragment.getMapAsync { googleMap ->
                googleMap.uiSettings.isMyLocationButtonEnabled = false
                googleMap.uiSettings.isMapToolbarEnabled = false
                googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                        requireContext(), R.raw.map_styles_json))
                tourInformationList.firstOrNull()?.locationPoint?.let { locationPoint->
                    //put the pin
                    val pin = MarkerOptions().position(locationPoint.location)
                    googleMap.addMarker(pin)
                    //move the camera
                    val camera = CameraUpdateFactory.newLatLngZoom(locationPoint.location, MAP_SNAPSHOT_ZOOM.toFloat())
                    googleMap.moveCamera(camera)
                } ?: run {
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
                    tourInformationList[1].locationPoint?.let{locationPoint ->
                        val distance = locationPoint.distanceTo(tourTimestamp.locationPoint)
                        val visibleRegion = hiddenMap.projection.visibleRegion
                        val nearLeft = visibleRegion.nearLeft
                        val nearRight = visibleRegion.nearRight
                        val result = floatArrayOf(0f)
                        Location.distanceBetween(nearLeft.latitude, nearLeft.longitude, nearRight.latitude, nearRight.longitude, result)
                        takeSnapshotOnCameraMove = distance < result[0]

                        //put the pin
                        hiddenMap.clear()
                        val pin = MarkerOptions().position(locationPoint.location)
                        hiddenMap.addMarker(pin)
                        //move the camera
                        val camera = CameraUpdateFactory.newLatLngZoom(locationPoint.location, MAP_SNAPSHOT_ZOOM.toFloat())
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
        val duration: Long = if (!tour.isClosed()) (now.time - tour.getStartTime().time) else 0L
        val startPoint = tour.getStartPoint()
        val tourInformation = TourInformation(
                tour.getStartTime(),
                now,
                tour.type,
                Tour.STATUS_ON_GOING,
                startPoint,
                duration,
                distance
        )
        discussionAdapter.addCardInfo(tourInformation)
    }

    override fun addDiscussionTourEndCard(now: Date) {
        var distance = 0f
        val duration = tour.getEndTime()?.let {it.time - tour.getStartTime().time } ?: 0L
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
        val tourInformation = TourInformation(
                tour.getStartTime(),
                tour.getEndTime() ?: now,
                tour.type,
                FeedItem.STATUS_CLOSED,
                tour.getEndPoint(),
                duration,
                distance
        )
        discussionAdapter.addCardInfoAfterTimestamp(tourInformation)
    }

    override fun updateMetadataView() {
        // show the view only for outing
        entourage_info_metadata_layout?.visibility = View.GONE
    }

    override fun loadPrivateCards() {
        super.loadPrivateCards()
        if (tour.isMine(context)) {
            presenter.getFeedItemEncounters(tour)
        }
    }

    // ----------------------------------
    // Bus handling
    // ----------------------------------
    override fun setReadOnly() {
        val encounters = tour.getTypedCardInfoList(TimestampedObject.ENCOUNTER)
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
    fun onEncounterUpdated(event: Events.TourEvents.OnEncounterUpdated) {
        val updatedEncounter = event.encounter
        val oldEncounter = discussionAdapter.findCard(TimestampedObject.ENCOUNTER, updatedEncounter.id) as Encounter?
                ?: return
        tour.removeCardInfo(oldEncounter)
        discussionAdapter.updateCard(updatedEncounter)
    }

    fun onFeedItemEncountersReceived(encounterList: List<Encounter>?) {
        encounterList?.let { list->
            EntourageApplication.me(context)?.let {user ->
                list.forEach {encounter ->
                    encounter.isMyEncounter = encounter.userId == user.id
                    encounter.isReadOnly = tour.isClosed()
                }
            }
            tour.addCardInfoList(list)
        }

        //hide the progress bar
        hideProgressBar()

        //update the discussion list
        updateDiscussionList()
    }

    override fun updateFeedItemActionEvent() {
        changeViewsVisibility(true)

        entourage_info_members_layout?.setBackgroundColor(ResourcesCompat.getColor(resources,R.color.white,null))
        entourage_info_title_full?.text = tour.getTitle()

        tour_summary_group_type?.text = tour.getFeedTypeLong(requireContext())
        tour_summary_author_name?.text = tour.author?.userName ?: ""
        entourage_info_location?.visibility = View.VISIBLE
        entourage_info_location?.text = tour.getDisplayAddress()

        updatePhotosAvatar(entourage_info_author_photo,entourage_info_partner_logo)

        entourage_info_people_count?.text = getString(R.string.tour_cell_numberOfPeople, tour.numberOfPeople)

        //   update description
        entourage_info_description?.let { description ->
            if (tour.getDescription()?.isNotEmpty() == true) {
                description.text = tour.getDescription()
                description.visibility = View.VISIBLE
            } else {
                description.visibility = View.GONE
            }
            DeepLinksManager.linkify(description)
        }

        //   metadata
        updateMetadataView()
        when(tour.getGroupType()) {
            Tour.GROUPTYPE_TOUR -> {
                showActionTimestamps(tour.getCreationTime(), tour.updatedTime)
            }
            else -> {
                entourage_info_timestamps?.visibility = View.GONE
            }
        }

    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    interface OnTourInformationFragmentFinish {
        fun showStopTourFragment(tour: Tour)
    }

    companion object {
        private  const val MAP_SNAPSHOT_ZOOM = 15
    }
}