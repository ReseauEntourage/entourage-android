package social.entourage.android.newsfeed

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.layout_map_launcher.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import kotlinx.android.synthetic.main.layout_map_longclick.view.*
import social.entourage.android.*
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.tour.TourType
import social.entourage.android.api.model.feed.*
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events
import social.entourage.android.location.EntourageLocation
import social.entourage.android.location.LocationUtils
import social.entourage.android.map.MapClusterEncounterItem
import social.entourage.android.map.MapClusterTourItem
import social.entourage.android.map.filter.MapFilterFactory
import social.entourage.android.map.filter.MapFilterFragment
import social.entourage.android.service.EntourageService
import social.entourage.android.service.TourServiceListener
import social.entourage.android.tour.TourFilter
import social.entourage.android.tour.TourFilterFragment
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment
import social.entourage.android.tour.encounter.CreateEncounterActivity
import social.entourage.android.view.EntourageSnackbar
import java.util.*

class NewsFeedWithTourFragment : NewsFeedFragment(), TourServiceListener {

    private var currentTourUUID: String = ""
    private var color = 0
    private var drawnToursMap: MutableList<Polyline> = ArrayList()
    private var currentTourLines: MutableList<Polyline> = ArrayList()
    private var previousCoordinates: LatLng? = null
    private var userHistory = false
    private var drawnUserHistory: MutableMap<Long, Polyline> = TreeMap()
    private var retrievedHistory: MutableMap<Long, Tour> = TreeMap()
    private var displayedTourHeads = 0
    private var shouldShowGPSDialog = true

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.REQUEST_CREATE_ENCOUNTER
                && resultCode == Constants.RESULT_CREATE_ENCOUNTER_OK) {
            val encounter = data?.extras?.getSerializable(CreateEncounterActivity.BUNDLE_KEY_ENCOUNTER) as Encounter?
                    ?: return
            addEncounter(encounter)
            putEncounterOnMap(encounter)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //bind views here
        map_longclick_button_start_tour_launcher.setOnClickListener {onStartTourLauncher()}
        launcher_tour_outer_view.setOnClickListener {hideTourLauncher()}
        tour_stop_button.setOnClickListener {onStartStopConfirmation()}
        map_longclick_button_create_encounter.setOnClickListener {onAddEncounter()}
        launcher_tour_go.setOnClickListener {onStartNewTour()}
    }

    override fun onBackPressed(): Boolean {
        if (layout_map_launcher?.visibility == View.VISIBLE) {
            hideTourLauncher()
            return true
        }
        if (fragment_map_longclick?.visibility == View.VISIBLE && entourageService?.isRunning==true ) {
            tour_stop_button?.visibility = View.VISIBLE
        }
        return super.onBackPressed()
    }

    override fun onLocationStatusUpdated(active: Boolean) {
        super.onLocationStatusUpdated(active)
        if (shouldShowGPSDialog && !active && entourageService?.isRunning == true) {
            //We always need GPS to be turned on during tour
            shouldShowGPSDialog = false
            val newIntent = Intent(this.context, MainActivity::class.java)
            newIntent.action = EntourageService.KEY_LOCATION_PROVIDER_DISABLED
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(newIntent)
        } else if (!shouldShowGPSDialog && active) {
            shouldShowGPSDialog = true
        }
    }

    private fun onStartTourLauncher() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_TOUR_CREATE_CLICK)
        if (entourageService!=null && !entourageService!!.isRunning) {
            // Check if the geolocation is permitted
            if (!LocationUtils.isLocationEnabled() || !LocationUtils.isLocationPermissionGranted()) {
                showAllowGeolocationDialog(GEOLOCATION_POPUP_TOUR)
                return
            }
            fragment_map_longclick?.visibility = View.GONE
            layout_map_launcher?.visibility = View.VISIBLE
        }
    }

    private fun onStartNewTour() {
        launcher_tour_go?.isEnabled = false
        launcher_tour_progressBar?.visibility = View.VISIBLE
        val tourType = TourType.findByRessourceId(launcher_tour_type.checkedRadioButtonId)
        startTour(tourType.typeName)
        when (tourType) {
            TourType.MEDICAL -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_MEDICAL)
            }
            TourType.BARE_HANDS -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_SOCIAL)
            }
            TourType.ALIMENTARY -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_DISTRIBUTION)
            }
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_START_TOUR)
    }

    private fun onStartStopConfirmation() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_SUSPEND)
        pauseTour()
        if (activity != null) {
            launchConfirmationFragment()
        }
    }

    override fun onAddEncounter() {
        if (activity == null) {
            return
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_CLICK)
        // Hide the create entourage menu ui
        fragment_map_longclick?.visibility = View.GONE

        // MI: EMA-1669 Show the disclaimer only the first time when a tour was started
        // Show the disclaimer fragment
        if (presenter.shouldDisplayEncounterDisclaimer()) {
            presenter.displayEncounterDisclaimer()
        } else {
            addEncounter()
        }
    }

    override fun addEncounter() {
        if (activity != null) {
            val intent = Intent(activity, CreateEncounterActivity::class.java)
            saveCameraPosition()
            val args = Bundle()
            args.putString(CreateEncounterActivity.BUNDLE_KEY_TOUR_ID, currentTourUUID)
            when {
                longTapCoordinates != null -> {
                    args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, longTapCoordinates!!.latitude)
                    args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, longTapCoordinates!!.longitude)
                    longTapCoordinates = null
                }
                EntourageLocation.getInstance().currentLocation != null -> {
                    args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, EntourageLocation.getInstance().currentLocation.latitude)
                    args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, EntourageLocation.getInstance().currentLocation.longitude)
                }
                else -> {
                    args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, EntourageLocation.getInstance().lastCameraPosition.target.latitude)
                    args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, EntourageLocation.getInstance().lastCameraPosition.target.longitude)
                }
            }
            intent.putExtras(args)
            //startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
            startActivity(intent)

            // show the disclaimer only once per tour
            presenter.setDisplayEncounterDisclaimer(false)
        }
    }

    override fun displayEntourageDisclaimer() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTION_CREATE_CLICK)
        // if we have an ongoing tour
        if (activity != null && entourageService?.isRunning==true) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENCOUNTER_POPUP_SHOW)
            // Show the dialog that asks the user if he really wants to create an entourage instead of encounter
            val builder = AlertDialog.Builder(requireActivity())
            builder
                    .setMessage(R.string.entourage_tour_ongoing_description)
                    .setTitle(R.string.entourage_tour_ongoing_title)
                    .setPositiveButton(R.string.entourage_tour_ongoing_proceed) { _, _ ->
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ENCOUNTER_POPUP_ENCOUNTER)
                        onAddEncounter()
                    }
                    .setNegativeButton(R.string.entourage_tour_ongoing_action) { _: DialogInterface?, _: Int ->
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ENCOUNTER_POPUP_ENTOURAGE)
                        super.displayEntourageDisclaimer()
                    }
            builder.show()
            return
        }
        if (entourageService?.isRunning==true) {
            tour_stop_button?.visibility = View.VISIBLE
        }
        super.displayEntourageDisclaimer()
    }

    private fun putEncounterOnMap(encounter: Encounter) {
        if (map == null) {
            // The map is not yet initialized or the google play services are outdated on the phone
            return
        }
        if(presenter.onClickListener?.getEncounterMapClusterItem(encounter.id) != null) {
            //the item aalready exists
            return
        }
        val mapClusterItem = MapClusterEncounterItem(encounter)
        presenter.onClickListener?.addEncounterMapClusterItem(mapClusterItem, encounter)
        mapClusterManager?.addItem(mapClusterItem)
    }

    override fun checkAction(action: String) {
        when (action) {
            TourEndConfirmationFragment.KEY_RESUME_TOUR -> resumeTour()
            TourEndConfirmationFragment.KEY_END_TOUR-> {
                if (entourageService?.isRunning==true)
                    stopFeedItem(null, true)
                else if (entourageService?.isPaused==true)
                    launchConfirmationFragment()
            }
            EntourageService.KEY_NOTIFICATION_PAUSE_TOUR-> launchConfirmationFragment()
            EntourageService.KEY_NOTIFICATION_STOP_TOUR -> entourageService?.endTreatment()
            PlusFragment.KEY_START_TOUR-> onStartTourLauncher()
            PlusFragment.KEY_ADD_ENCOUNTER -> onAddEncounter()
            else -> super.checkAction(action)
        }
    }

    override fun updateFloatingMenuOptions() {
        tour_stop_button?.visibility = if (entourageService?.isRunning==true) View.VISIBLE else View.GONE
    }

    override fun needForGeoloc():Boolean {
        return true
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    override fun showLongClickOnMapOptions(latLng: LatLng) {
        //save the tap coordinates
        longTapCoordinates = latLng
        //hide the FAB menu
        tour_stop_button?.visibility = View.GONE
        //update the visible buttons
        val isTourRunning = entourageService?.isRunning==true
        map_longclick_buttons?.map_longclick_button_start_tour_launcher?.visibility = if (isTourRunning) View.INVISIBLE else View.VISIBLE
        map_longclick_buttons?.map_longclick_button_create_encounter?.visibility = if (isTourRunning) View.VISIBLE else View.GONE
        super.showLongClickOnMapOptions(latLng)
    }

    @Subscribe
    override fun onUserChoiceChanged(event: Events.OnUserChoiceEvent) {
        super.onUserChoiceChanged(event)
        userHistory = event.isUserHistory
        if (userHistory) {
            entourageService?.updateUserHistory(userId, 1, 500)
            showUserHistory()
        } else {
            hideUserHistory()
        }
    }

    @Subscribe
    override fun onUserInfoUpdated(event: Events.OnUserInfoUpdatedEvent?) {
        super.onUserInfoUpdated(event)
    }
    @Subscribe
    override fun onMyEntouragesForceRefresh(event: Events.OnMyEntouragesForceRefresh) {
        super.onMyEntouragesForceRefresh(event)
    }

    @Subscribe
    override fun onEntourageCreated(event: Events.OnEntourageCreated) {
        super.onEntourageCreated(event)
    }

    @Subscribe
    override fun onEntourageUpdated(event: Events.OnEntourageUpdated) {
        super.onEntourageUpdated(event)
    }

    @Subscribe
    override fun onNewsfeedLoadMoreRequested(event: Events.OnNewsfeedLoadMoreEvent) {
        super.onNewsfeedLoadMoreRequested(event)
    }

    @Subscribe
    override fun onMapFilterChanged(event: Events.OnMapFilterChanged) {
        super.onMapFilterChanged(event)
    }

    @Subscribe
    override fun onBetterLocation(event: Events.OnBetterLocationEvent) {
        super.onBetterLocation(event)
    }

    @Subscribe
    fun onEncounterCreated(event: Events.OnEncounterCreated) {
        if (entourageService != null) { //refresh button just in case
            tour_stop_button?.visibility = if (entourageService?.isRunning==true) View.VISIBLE else View.GONE
        }
        val encounter = event.encounter ?: return
        addEncounter(encounter)
        putEncounterOnMap(encounter)
    }

    @Subscribe
    fun onEncounterUpdated(event: Events.OnEncounterUpdated?) {
        val updatedEncounter = event?.encounter ?: return
        val mapClusterItem = presenter.onClickListener?.removeEncounterMapClusterItem(updatedEncounter.id)
        if (mapClusterItem != null) {
            mapClusterManager?.removeItem(mapClusterItem)
        }
        updateEncounter(updatedEncounter)
        putEncounterOnMap(updatedEncounter)
    }

    @Subscribe
    override fun feedItemViewRequested(event: Events.OnFeedItemInfoViewRequestedEvent) {
        super.feedItemViewRequested(event)
    }

    @Subscribe
    override fun userActRequested(event: Events.OnUserActEvent) {
        super.userActRequested(event)
    }

    override fun onTourCreated(created: Boolean, tourUUID: String) {
        launcher_tour_go?.isEnabled = true
        launcher_tour_progressBar?.visibility = View.GONE

        if (activity != null) {
            if (created && entourageService?.currentTour !=null) {
                isFollowing = true
                currentTourUUID = tourUUID
                layout_map_launcher?.visibility = View.GONE
                if (fragment_map_feeditems_view?.visibility == View.VISIBLE) {
                    displayFullMap()
                }
                addTourCard(entourageService!!.currentTour)
                tour_stop_button?.visibility = View.VISIBLE
                presenter.incrementUserToursCount()
                presenter.setDisplayEncounterDisclaimer(true)
            } else if (fragment_map_main_layout != null) {
                EntourageSnackbar.make(fragment_map_main_layout, R.string.tour_creation_fail, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onTourUpdated(newPoint: LatLng) {
        drawCurrentLocation(newPoint)
    }

    override fun onTourResumed(pointsToDraw: List<LocationPoint>, tourType: String, startDate: Date) {
        if (pointsToDraw.isNotEmpty()) {
            drawCurrentTour(pointsToDraw, tourType, startDate)
            previousCoordinates = pointsToDraw[pointsToDraw.size - 1].location
            val currentLocation = EntourageLocation.getInstance().currentLocation
            centerMap(LatLng(currentLocation.latitude, currentLocation.longitude))
            isFollowing = true
        }
        tour_stop_button?.visibility = View.VISIBLE
    }

    override fun onRetrieveToursByUserId(tours: List<Tour>) {
        var nearbyTours = tours
        nearbyTours = removeRedundantTours(nearbyTours, true)
        nearbyTours = removeRecentTours(nearbyTours)
        Collections.sort(nearbyTours, Tour.TourComparatorOldToNew())
        for (tour in nearbyTours) {
            if (!tour.uuid.equals(currentTourUUID, ignoreCase = true)) {
                drawNearbyTour(tour, true)
            }
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------
    override fun hideTourLauncher() {
        if (layout_map_launcher?.visibility == View.VISIBLE) {
            layout_map_launcher.visibility = View.GONE
        }
    }

    override fun redrawWholeNewsfeed(newsFeeds: List<NewsfeedItem>) {
        //TODO do we need newsFeeds variable here ?
        if (map != null && newsFeeds.isNotEmpty() && newsfeedAdapter != null) {
            for (polyline in drawnToursMap) {
                polyline.remove()
            }
            drawnToursMap.clear()

            //redraw the whole newsfeed
            for (timestampedObject in newsfeedAdapter!!.items) {
                if (timestampedObject.type == TimestampedObject.TOUR_CARD) {
                    val tour = timestampedObject as Tour
                    if (tour.uuid.equals(currentTourUUID, ignoreCase = true)) {
                        continue
                    }
                    drawNearbyTour(tour, false)
                } else if (timestampedObject.type == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage(timestampedObject as BaseEntourage)
                }
            }
            mapClusterManager?.cluster()
            //redraw the current ongoing tour, if any
            if (entourageService != null && currentTourUUID.isNotEmpty()) {
                val line = PolylineOptions()
                for (polyline in currentTourLines) {
                    line.addAll(polyline.points)
                }
                line.zIndex(2f)
                line.width(15f)
                line.color(color)
                drawnToursMap.add(map!!.addPolyline(line))
                addCurrentTourEncounters()
            }
        }
    }

    private fun startTour(type: String) {
        if (entourageService!=null && !entourageService!!.isRunning) {
            color = getTrackColor(false, type, Date())
            entourageService?.beginTreatment(type)
        }
    }

    private fun pauseTour() {
        if (entourageService?.isRunning==true) {
            entourageService?.pauseTreatment()
        }
    }

    private fun resumeTour() {
        if (entourageService!=null && !entourageService!!.isRunning) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_RESTART_TOUR)
            entourageService?.resumeTreatment()
        }
    }

    private fun launchConfirmationFragment() {
        pauseTour()
        entourageService?.currentTour?.let {
            TourEndConfirmationFragment.newInstance(it).show(parentFragmentManager, TourEndConfirmationFragment.TAG)
        }
    }

    private fun addEncounter(encounter: Encounter?) {
        entourageService?.addEncounter(encounter)
    }

    private fun updateEncounter(encounter: Encounter) {
        entourageService?.updateEncounter(encounter)
    }

    private fun removeRedundantTours(allTours: List<Tour>, isHistory: Boolean): List<Tour> {
        if(!isHistory && newsfeedAdapter ==null) return allTours
        val uniqueTours = ArrayList<Tour>()
        for (tour in allTours) {
            if (!isHistory) {
                if ((newsfeedAdapter?.findCard(tour) as Tour?)?.isSame(tour) == true) {
                    continue
                }
            } else if (drawnUserHistory.containsKey(tour.id)) {
                continue
            }
            uniqueTours.add(tour)
        }
        return uniqueTours
    }

    override fun onNotificationExtras(id: Int, choice: Boolean) {
        super.onNotificationExtras(id, choice)
        userHistory = choice
    }

    override fun onFeedItemClosed(closed: Boolean, feedItem: FeedItem) {
        if (feedItem.type == TimestampedObject.TOUR_CARD) {
            if (closed) {
                if (feedItem.uuid.equals(currentTourUUID, ignoreCase = true)) {
                    updateFloatingMenuOptions()
                    currentTourUUID = ""
                } else {
                    entourageService?.notifyListenersTourResumed()
                }
                if (fragment_map_main_layout != null) {
                    EntourageSnackbar.make(fragment_map_main_layout, feedItem.getClosedToastMessage(), Snackbar.LENGTH_SHORT).show()
                }
                if (userHistory) {
                    entourageService?.updateUserHistory(userId, 1, 1)
                }
            } else {
                val message = if (feedItem.type == TimestampedObject.TOUR_CARD && feedItem.status == FeedItem.STATUS_FREEZED) R.string.tour_freezed else R.string.tour_close_fail
                if (fragment_map_main_layout != null) {
                    EntourageSnackbar.make(fragment_map_main_layout, message, Snackbar.LENGTH_SHORT).show()
                }
            }
        }
        super.onFeedItemClosed(closed, feedItem)
    }

    private fun removeRecentTours(tours: List<Tour>): List<Tour> {
        val tourList = ArrayList<Tour>()
        for(tour in tours) {
            if (newsfeedAdapter?.findCard(tour) != null) {
                continue
            }
            tourList.add(tour)
        }
        return tourList
    }

    private fun addCurrentTourEncounters() {
        val encounters = entourageService?.currentTour?.encounters
        if (encounters?.isNotEmpty() == true) {
            for (encounter in encounters) {
                putEncounterOnMap(encounter)
            }
        }
    }

    private fun drawCurrentTour(pointsToDraw: List<LocationPoint>, tourType: String, startDate: Date) {
        if (map != null && pointsToDraw.isNotEmpty()) {
            val line = PolylineOptions()
            color = getTrackColor(true, tourType, startDate)
            line.zIndex(2f)
            line.width(15f)
            line.color(color)
            for (tourPoint in pointsToDraw) {
                line.add(tourPoint.location)
            }
            currentTourLines.add(map!!.addPolyline(line))
        }
    }

    private fun drawNearbyTour(tour: Tour, isHistory: Boolean) {
        if (map != null && tour.tourPoints.isNotEmpty()) {
            val line = PolylineOptions()
            line.zIndex(if (isToday(tour.getStartTime())) 1f else 0f)
            line.width(15f)
            line.color(getTrackColor(isHistory, tour.tourType, tour.getStartTime()))
            for (tourPoint in tour.tourPoints) {
                line.add(tourPoint.location)
            }
            if (isHistory) {
                retrievedHistory[tour.id] = tour
                drawnUserHistory[tour.id] = map!!.addPolyline(line)
            } else {
                drawnToursMap.add(map!!.addPolyline(line))
                //addTourCard(tour);
            }
            addTourHead(tour)
        }
    }

    private fun drawCurrentLocation(location: LatLng) {
        if (previousCoordinates != null && map != null) {
            val line = PolylineOptions()
            line.add(previousCoordinates, location)
            line.zIndex(2f)
            line.width(15f)
            line.color(color)
            currentTourLines.add(map!!.addPolyline(line))
        }
        previousCoordinates = location
    }

    private fun addTourCard(tour: Tour) {
        if (newsfeedAdapter?.findCard(tour) != null) {
            newsfeedAdapter?.updateCard(tour)
        } else {
            newsfeedAdapter?.addCardInfoBeforeTimestamp(tour)
        }
    }

    private fun addTourHead(tour: Tour) {
        if (displayedTourHeads >= MAX_TOUR_HEADS_DISPLAYED || map == null || markersMap.containsKey(tour.hashString())) {
            return
        }
        displayedTourHeads++
        MapClusterTourItem(tour).let {
            markersMap[tour.hashString()] = it
            mapClusterManager?.addItem(it)
        }
    }

    override fun clearAll() {
        super.clearAll()
        currentTourLines.clear()
        drawnToursMap.clear()
        drawnUserHistory.clear()
        previousCoordinates = null
        displayedTourHeads = 0
    }

    private fun hideUserHistory() {
        for (tour in retrievedHistory.values) {
            val line = drawnUserHistory[tour.id] ?: continue
            line.color = getTrackColor(true, tour.tourType, tour.getStartTime())
        }
    }

    private fun showUserHistory() {
        for ((key, line) in drawnUserHistory) {
            val tour = retrievedHistory[key] ?: continue
            line.color = getTrackColor(true, tour.tourType, tour.getStartTime())
        }
    }

    @Subscribe
    override fun onLocationPermissionGranted(event: Events.OnLocationPermissionGranted) {
        super.onLocationPermissionGranted(event)
    }

    @Subscribe
    override fun feedItemCloseRequested(event: Events.OnFeedItemCloseRequestEvent) {
        super.feedItemCloseRequested(event)
    }

    @Subscribe
    override fun checkIntentAction(event: Events.OnCheckIntentActionEvent) {
        super.checkIntentAction(event)
    }

    override fun updateFragmentFromService() {
        if (entourageService?.isRunning==true) {
            updateFloatingMenuOptions()
            currentTourUUID = entourageService!!.currentTourId
            //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);
            addCurrentTourEncounters()
        }
        if (userHistory) {
            entourageService!!.updateUserHistory(userId, 1, 500)
        }
    }

    private fun getTrackColor(isHistory: Boolean, type: String, date: Date): Int {
        if (context == null) {
            return Color.GRAY
        }
        var color = ContextCompat.getColor(requireContext(), Tour.getTypeColorRes(type))
        if (!isToday(date)) {
            color = getTransparentColor(color)
        }
        return if (isHistory) {
            if (!userHistory) {
                Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))
            } else {
                Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
            }
        } else color
    }

    override fun updateUserHistory() {
        if (userHistory) {
            entourageService?.updateUserHistory(userId, 1, 500)
        }
    }

    override fun onShowFilter() {
        if(selectedTab==NewsfeedTabItem.TOUR_TAB) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_FILTERSCLICK)
            TourFilterFragment().show(parentFragmentManager, MapFilterFragment.TAG)
        } else {
            super.onShowFilter()
        }
    }

    override fun updateFilterButtonText() {
        val activefilters = (MapFilterFactory.mapFilter.isDefaultFilter() && selectedTab==NewsfeedTabItem.ALL_TAB)|| (TourFilter.isDefaultFilter() && selectedTab==NewsfeedTabItem.TOUR_TAB)
        fragment_map_filter_button?.setText(if (activefilters) R.string.map_no_filter else R.string.map_filters_activated)
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val MAX_TOUR_HEADS_DISPLAYED = 10
    }
}