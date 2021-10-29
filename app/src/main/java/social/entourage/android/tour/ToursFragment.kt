package social.entourage.android.tour

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_home_news.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.fragment_map_feeditems_view
import kotlinx.android.synthetic.main.fragment_map.fragment_map_filter_button
import kotlinx.android.synthetic.main.fragment_map.fragment_map_longclick
import kotlinx.android.synthetic.main.fragment_map.fragment_map_main_layout
import kotlinx.android.synthetic.main.fragment_map.layout_map_launcher
import kotlinx.android.synthetic.main.fragment_map.tour_stop_button
import kotlinx.android.synthetic.main.layout_map_launcher.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import kotlinx.android.synthetic.main.layout_map_longclick.view.*
import social.entourage.android.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.feed.*
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourType
import social.entourage.android.api.tape.Events
import social.entourage.android.base.BackPressable
import social.entourage.android.entourage.FeedItemOptionsFragment
import social.entourage.android.entourage.join.EntourageJoinRequestFragment
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.base.map.MapClusterEncounterItem
import social.entourage.android.base.map.MapClusterTourItem
import social.entourage.android.base.map.filter.MapFilterFactory
import social.entourage.android.base.map.filter.MapFilterFragment
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.navigation.EntBottomNavigationView
import social.entourage.android.base.newsfeed.NewsfeedTabItem
import social.entourage.android.base.newsfeed.NewsfeedFragment
import social.entourage.android.service.EntService
import social.entourage.android.service.EntourageServiceListener
import social.entourage.android.service.TourServiceListener
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment
import social.entourage.android.tour.encounter.CreateEncounterActivity
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment
import social.entourage.android.tour.encounter.ReadEncounterActivity
import social.entourage.android.tour.join.TourJoinRequestFragment
import timber.log.Timber
import java.util.*

open class ToursFragment : NewsfeedFragment(), EntourageServiceListener, TourServiceListener, BackPressable {

    /******
     * BackPressable
     */
    override fun onBackPressed(): Boolean {
        if (layout_map_launcher?.visibility == View.VISIBLE) {
            hideTourLauncher()
            return true
        }
        if (fragment_map_longclick?.visibility == View.VISIBLE && entService?.isRunning==true ) {
            updateFloatingMenuOptions()
        }

        requireActivity().supportFragmentManager.popBackStackImmediate()

        //Reset navigation track
        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",false)
        editor.putString("navType",null)
        editor.apply()

        return true
    }

    private var currentTourUUID: String = ""
    private var color = 0
    private var drawnToursMap: MutableList<Polyline> = ArrayList()
    private var currentTourLines: MutableList<Polyline> = ArrayList()
    private var previousCoordinates: LatLng? = null
    //private var userHistory = false
    private var drawnUserHistory: MutableMap<Long, Polyline> = TreeMap()
    private var retrievedHistory: MutableMap<Long, Tour> = TreeMap()
    private var displayedTourHeads = 0
    private var shouldShowGPSDialog = true

    private val connection = ServiceConnection()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val returnView = inflater.inflate(R.layout.fragment_home_news, container, false)
        selectedTab = NewsfeedTabItem.TOUR_TAB
        return returnView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //bind views here
        map_longclick_button_start_tour_launcher?.setOnClickListener {onStartTourLauncher()}
        launcher_tour_outer_view?.setOnClickListener {hideTourLauncher()}
        tour_stop_button?.setOnClickListener {onStartStopConfirmation()}
        map_longclick_button_create_encounter?.setOnClickListener {onAddEncounter()}
        launcher_tour_go?.setOnClickListener {onStartNewTour()}

        ui_bt_search?.visibility = View.INVISIBLE
        ui_bt_back?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        ui_tv_title?.text = getString(R.string.map_filter_tours_text)
        ui_tv_title?.visibility = View.VISIBLE
        selectedTab = NewsfeedTabItem.TOUR_TAB
    }

    override fun onResume() {
        selectedTab = NewsfeedTabItem.TOUR_TAB
        super.onResume()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection.doBindService()
    }

    override fun onDestroy() {
        connection.doUnbindService()
        super.onDestroy()
    }

    override fun onLocationStatusUpdated(active: Boolean) {
        super.onLocationStatusUpdated(active)
        if (shouldShowGPSDialog && !active && entService?.isRunning == true) {
            //We always need GPS to be turned on during tour
            shouldShowGPSDialog = false
            val newIntent = Intent(this.context, MainActivity::class.java)
            newIntent.action = EntService.KEY_LOCATION_PROVIDER_DISABLED
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(newIntent)
        } else if (!shouldShowGPSDialog && active) {
            shouldShowGPSDialog = true
        }
    }

    private fun onStartTourLauncher() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_TOUR_CREATE_CLICK)
        if (entService==null || entService?.isRunning==true) return

        // Check if the geolocation is permitted
        if (!LocationUtils.isLocationEnabled() || !LocationUtils.isLocationPermissionGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_TOUR)
            return
        }
        fragment_map_longclick?.visibility = View.GONE
        layout_map_launcher?.visibility = View.VISIBLE
    }

    private fun onStartNewTour() {
        launcher_tour_type?.let {
            launcher_tour_go?.isEnabled = false
            launcher_tour_progressBar?.visibility = View.VISIBLE
            val tourType = TourType.findByRessourceId(it.checkedRadioButtonId)
            startTour(tourType.typeName)
            AnalyticsEvents.logEvent(
                    when (tourType) {
                        TourType.MEDICAL -> AnalyticsEvents.EVENT_TOUR_MEDICAL
                        TourType.BARE_HANDS -> AnalyticsEvents.EVENT_TOUR_SOCIAL
                        TourType.ALIMENTARY -> AnalyticsEvents.EVENT_TOUR_DISTRIBUTION
                    }
            )
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_START_TOUR)
        }
    }

    private fun onStartStopConfirmation() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_TOUR_SUSPEND)
        pauseTour()
        if (activity != null) {
            launchConfirmationFragment()
        }
    }

    fun onAddEncounter() {
        if (activity == null) {
            return
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_CREATE_ENCOUNTER_CLICK)
        // Hide the create entourage menu ui
        fragment_map_longclick?.visibility = View.GONE

        // MI: EMA-1669 Show the disclaimer only the first time when a tour was started
        // Show the disclaimer fragment
        if (!presenter.authenticationController.encounterDisclaimerShown) {
            activity?.supportFragmentManager?.let { fragmentManager -> EncounterDisclaimerFragment().show(fragmentManager, EncounterDisclaimerFragment.TAG) }
        } else {
            addEncounter()
        }
    }

    private fun addEncounter() {
        if (activity != null) {
            saveCameraPosition()
            val args = Bundle()
            args.putString(CreateEncounterActivity.BUNDLE_KEY_TOUR_ID, currentTourUUID)
            val encounterPosition  = longTapCoordinates ?: EntLocation.currentLatLng ?: EntLocation.lastCameraPosition.target
            args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, encounterPosition.latitude)
            args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, encounterPosition.longitude)
            longTapCoordinates = null
            val intent = Intent(activity, CreateEncounterActivity::class.java)
            intent.putExtras(args)
            startActivity(intent)
        }
    }

    override fun displayEntourageDisclaimer() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_ACTION_CREATE_CLICK)
        // if we have an ongoing tour
        if (activity != null && entService?.isRunning==true) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENCOUNTER_POPUP_SHOW)
            // Show the dialog that asks the user if he really wants to create an entourage instead of encounter
            AlertDialog.Builder(requireActivity())
                .setMessage(R.string.entourage_tour_ongoing_description)
                .setTitle(R.string.entourage_tour_ongoing_title)
                .setPositiveButton(R.string.entourage_tour_ongoing_proceed) { _, _ ->
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENCOUNTER_POPUP_ENCOUNTER)
                    onAddEncounter()
                }
                .setNegativeButton(R.string.entourage_tour_ongoing_demande) { _, _ ->
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENCOUNTER_POPUP_ENTOURAGE)
                    setGroupType(BaseEntourage.GROUPTYPE_ACTION_DEMAND)
                    super.displayEntourageDisclaimer()
                }
                .setNeutralButton(R.string.entourage_tour_ongoing_contrib) { _, _ ->
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_ENCOUNTER_POPUP_ENTOURAGE)
                    setGroupType(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
                    super.displayEntourageDisclaimer()
                }
                .show()
            return
        }
        updateFloatingMenuOptions()
        super.displayEntourageDisclaimer()
    }

    private fun putEncounterOnMap(encounter: Encounter) {
        if (map == null) {
            // The map is not yet initialized or the google play services are outdated on the phone
            return
        }
        if(presenter.onClickListener?.getEncounterMapClusterItem(encounter.id) != null) {
            //the item already exists
            return
        }
        val mapClusterItem = MapClusterEncounterItem(encounter)
        presenter.onClickListener?.addEncounterMapClusterItem(mapClusterItem, encounter)
        mapClusterManager?.addItem(mapClusterItem)
    }

    private fun checkAction(action: String) {
        when (action) {
            PlusFragment.KEY_CREATE_CONTRIBUTION -> createAction(BaseEntourage.GROUPTYPE_ACTION, BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
            PlusFragment.KEY_CREATE_DEMAND -> createAction(BaseEntourage.GROUPTYPE_ACTION, BaseEntourage.GROUPTYPE_ACTION_DEMAND)
            PlusFragment.KEY_CREATE_OUTING -> createAction(BaseEntourage.GROUPTYPE_OUTING)
            TourEndConfirmationFragment.KEY_RESUME_TOUR -> resumeTour()
            TourEndConfirmationFragment.KEY_END_TOUR-> {
                if (entService?.isRunning==true)
                    stopFeedItem(null, true)
                else if (entService?.isPaused==true)
                    launchConfirmationFragment()
            }
            EntService.KEY_NOTIFICATION_PAUSE_TOUR-> launchConfirmationFragment()
            EntService.KEY_NOTIFICATION_STOP_TOUR -> entService?.endTreatment()
            PlusFragment.KEY_START_TOUR-> onStartTourLauncher()
            PlusFragment.KEY_ADD_ENCOUNTER -> onAddEncounter()
        }
    }

    override fun updateFloatingMenuOptions() {
        tour_stop_button?.visibility = if (entService?.isRunning==true) View.VISIBLE else View.GONE
        EntBottomNavigationView.updatePlusBadge(entService?.isRunning==true)
    }

    override fun needForGeoloc():Boolean {
        return true
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    override fun showLongClickOnMapOptions(latLng: LatLng) {
        //hide the FAB menu
        tour_stop_button?.visibility = View.GONE
        //update the visible buttons
        val isTourRunning = entService?.isRunning==true
        map_longclick_buttons?.map_longclick_button_start_tour_launcher?.visibility = if (isTourRunning) View.INVISIBLE else View.VISIBLE
        map_longclick_buttons?.map_longclick_button_create_encounter?.visibility = if (isTourRunning) View.VISIBLE else View.GONE
        super.showLongClickOnMapOptions(latLng)
    }

    @Subscribe
    fun onUserChoiceChanged(event: Events.OnUserChoiceEvent) {
        if (event.isUserHistory) {
            entService?.updateUserHistory(userId, 1, 500)
            showUserHistory()
        } else {
            hideUserHistory()
        }
    }

    @Subscribe
    override fun onAddPushNotification(message: Message) {
        onAddPushNotification(message)
    }

    @Subscribe
    fun onUserInfoUpdated(event: Events.OnUserInfoUpdatedEvent) {
        if (newsfeedAdapter == null) return
        val meAsAuthor = EntourageApplication.me(context)?.asTourAuthor() ?: return
        val dirtyList: MutableList<TimestampedObject> = ArrayList()
        // See which cards needs updating
        newsfeedAdapter?.items?.filterIsInstance<FeedItem>()?.forEach { feedItem ->
            // Skip null author
            val author = feedItem.author ?: return@forEach
            // Skip not same author id
            if (author.userID != meAsAuthor.userID) return@forEach
            // Skip if nothing changed
            if (!author.isSame(meAsAuthor)) {
                // Update the tour author
                meAsAuthor.userName = author.userName
                feedItem.author = meAsAuthor
                // Mark as dirty
                dirtyList.add(feedItem)
            }
        }
        // Update the dirty cards
        for (dirty in dirtyList) {
            newsfeedAdapter?.updateCard(dirty)
        }
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
    fun onEncounterCreated(event: Events.TourEvents.OnEncounterCreated) {
        updateFloatingMenuOptions()
        event.encounter?.let { encounter ->
            addEncounter(encounter)
            putEncounterOnMap(encounter)
        }
    }

    @Subscribe
    fun onEncounterUpdated(event: Events.TourEvents.OnEncounterUpdated) {
        val updatedEncounter = event.encounter
        presenter.onClickListener?.removeEncounterMapClusterItem(updatedEncounter.id)?.let { mapClusterItem -> mapClusterManager?.removeItem(mapClusterItem) }
        updateEncounter(updatedEncounter)
        putEncounterOnMap(updatedEncounter)
    }

    @Subscribe
    override fun userActRequested(event: Events.OnUserActEvent) {
        super.userActRequested(event)
    }

    override fun onTourCreated(created: Boolean, tourUUID: String) {
        launcher_tour_go?.isEnabled = true
        launcher_tour_progressBar?.visibility = View.GONE

        if (activity != null) {
            val currentTour = entService?.currentTour
            if (created && currentTour !=null) {
                isFollowing = true
                currentTourUUID = tourUUID
                layout_map_launcher?.visibility = View.GONE
                if (fragment_map_feeditems_view?.visibility == View.VISIBLE) {
                    displayFullMap()
                }
                addTourCard(currentTour)
                presenter.incrementUserToursCount()
                presenter.authenticationController.encounterDisclaimerShown = true
            } else if (fragment_map_main_layout != null) {
                EntSnackbar.make(fragment_map_main_layout, R.string.tour_creation_fail, Snackbar.LENGTH_SHORT).show()
            }
            updateFloatingMenuOptions()
        }
    }

    override fun onTourUpdated(newPoint: LatLng) {
        drawCurrentLocation(newPoint)
    }

    override fun onTourResumed(pointsToDraw: List<LocationPoint>, tourType: String, startDate: Date) {
        if (pointsToDraw.isNotEmpty()) {
            drawCurrentTour(pointsToDraw, tourType, startDate)
            previousCoordinates = pointsToDraw[pointsToDraw.size - 1].location
            EntLocation.currentLocation?.let { centerMap(LatLng(it.latitude, it.longitude)) }
            isFollowing = true
        }
        updateFloatingMenuOptions()
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
            layout_map_launcher?.visibility = View.GONE
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
            newsfeedAdapter?.items?.forEach {timestampedObject->
                if (timestampedObject.type == TimestampedObject.TOUR_CARD) {
                    val tour = timestampedObject as Tour
                    if (tour.uuid.equals(currentTourUUID, ignoreCase = true)) {
                        return@forEach
                    }
                    drawNearbyTour(tour, false)
                } else if (timestampedObject.type == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage(timestampedObject as BaseEntourage)
                }
            }
            mapClusterManager?.cluster()
            //redraw the current ongoing tour, if any
            if (entService != null && currentTourUUID.isNotEmpty()) {
                val line = PolylineOptions()
                for (polyline in currentTourLines) {
                    line.addAll(polyline.points)
                }
                line.zIndex(2f)
                line.width(15f)
                line.color(color)
                map?.addPolyline(line)?.let {drawnToursMap.add(it)}
                addCurrentTourEncounters()
            }
        }
    }

    private fun startTour(type: String) {
        if (entService?.isRunning==true) return
        color = getTrackColor(false, type, Date())
        entService?.beginTreatment(type)
    }

    private fun pauseTour() {
        if (entService?.isRunning==true) {
            entService?.pauseTreatment()
        }
    }

    private fun resumeTour() {
        if (entService?.isRunning==true) return
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_RESTART_TOUR)
        entService?.resumeTreatment()
    }

    private fun launchConfirmationFragment() {
        pauseTour()
        entService?.currentTour?.let {
            TourEndConfirmationFragment.newInstance(it).show(parentFragmentManager, TourEndConfirmationFragment.TAG)
        }
    }

    private fun addEncounter(encounter: Encounter) {
        entService?.addEncounter(encounter)
    }

    private fun updateEncounter(encounter: Encounter) {
        entService?.updateEncounter(encounter)
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

    override fun onFeedItemClosed(closed: Boolean, updatedFeedItem: FeedItem) {
        (updatedFeedItem as? Tour)?.let { tour ->
            if (closed) {
                if (tour.uuid.equals(currentTourUUID, ignoreCase = true)) {
                    updateFloatingMenuOptions()
                    currentTourUUID = ""
                } else {
                    entService?.notifyListenersTourResumed()
                }
                if (presenter.authenticationController.isUserToursOnly) {
                    entService?.updateUserHistory(userId, 1, 1)
                }
                fragment_map_main_layout?.let { EntSnackbar.make(it, tour.getClosedToastMessage(), Snackbar.LENGTH_SHORT).show() }
            } else {
                val message = if(tour.status == Tour.STATUS_FREEZED) R.string.tour_freezed else R.string.tour_close_fail
                fragment_map_main_layout?.let { EntSnackbar.make(it, message, Snackbar.LENGTH_SHORT).show()}
            }
        }
        if (closed) {
            refreshFeed()
            fragment_map_main_layout?.let { layout -> EntSnackbar.make(layout, updatedFeedItem.getClosedToastMessage(), Snackbar.LENGTH_SHORT).show() }
        }
        loaderStop?.dismiss()
        loaderStop = null
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
        val encounters = entService?.currentTour?.encounters
        if (encounters?.isNotEmpty() == true) {
            for (encounter in encounters) {
                putEncounterOnMap(encounter)
            }
        }
    }

    private fun drawCurrentTour(pointsToDraw: List<LocationPoint>, tourType: String, startDate: Date) {
        map?.let {
            if (pointsToDraw.isNotEmpty()) {
                val line = PolylineOptions()
                color = getTrackColor(true, tourType, startDate)
                line.zIndex(2f)
                line.width(15f)
                line.color(color)
                for (tourPoint in pointsToDraw) {
                    line.add(tourPoint.location)
                }
                it.addPolyline(line)?.let { newline -> currentTourLines.add(newline)}
            }
        }
    }

    private fun drawNearbyTour(tour: Tour, isHistory: Boolean) {
        map?.let { gMap ->
            if (tour.tourPoints.isNotEmpty()) {
                val line = PolylineOptions()
                line.zIndex(if (isToday(tour.getStartTime())) 1f else 0f)
                line.width(15f)
                line.color(getTrackColor(isHistory, tour.tourType, tour.getStartTime()))
                for (tourPoint in tour.tourPoints) {
                    line.add(tourPoint.location)
                }
                gMap.addPolyline(line)?.let {
                    if (isHistory) {
                        retrievedHistory[tour.id] = tour
                        drawnUserHistory[tour.id] = it
                    } else {
                        drawnToursMap.add(it)
                        //addTourCard(tour);
                    }
                }
                addTourHead(tour)
            }
        }
    }

    private fun drawCurrentLocation(location: LatLng) {
        if (previousCoordinates != null) {
            val line = PolylineOptions()
            line.add(previousCoordinates, location)
            line.zIndex(2f)
            line.width(15f)
            line.color(color)
            map?.addPolyline(line)?.let {currentTourLines.add(it)}
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
    override fun onUserStatusChanged(user: EntourageUser, updatedFeedItem: FeedItem) {
        activity?.let {activity ->
            if (activity.isFinishing) return
            try {
                updatedFeedItem.joinStatus = user.status ?: ""
                if (user.status == FeedItem.JOIN_STATUS_PENDING) {
                    if (updatedFeedItem is Tour) {
                        TourJoinRequestFragment.newInstance(updatedFeedItem).show(activity.supportFragmentManager, TourJoinRequestFragment.TAG)
                    } else if (updatedFeedItem is BaseEntourage) {
                        EntourageJoinRequestFragment.newInstance(updatedFeedItem).show(activity.supportFragmentManager, EntourageJoinRequestFragment.TAG)
                    }
                }
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
            updateNewsfeedJoinStatus(updatedFeedItem)
            isRequestingToJoin--
        }
    }

    private fun updateNewsfeedJoinStatus(timestampedObject: TimestampedObject) {
        newsfeedAdapter?.updateCard(timestampedObject)
    }

    // ----------------------------------
    // BUS LISTENERS : needs to be in final class (not in parent class
    // ----------------------------------
    @Subscribe
    override fun onLocationPermissionGranted(event: Events.OnLocationPermissionGranted) {
        super.onLocationPermissionGranted(event)
    }

    @Subscribe
    override fun feedItemCloseRequested(event: Events.OnFeedItemCloseRequestEvent) {
        super.feedItemCloseRequested(event)
    }

    @Subscribe
    fun checkIntentAction(event: Events.OnCheckIntentActionEvent) {
        if (activity == null) {
            Timber.w("No activity found")
            return
        }
        checkAction(event.action)
        val content = (event.extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as? Message)?.content
            ?: return
        when (event.action) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE,
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST,
            PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED -> if (content.isTourRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.TOUR_CARD)
            } else if (content.isEntourageRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.ENTOURAGE_CARD)
            }
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION -> content.extra?.let { extra ->
                displayChosenFeedItem(extra.entourageId.toString(), TimestampedObject.ENTOURAGE_CARD, extra.invitationId.toLong())
            }
            PushNotificationContent.TYPE_INVITATION_STATUS -> content.extra?.let {
                if (content.isEntourageRelated || content.isTourRelated) {
                    displayChosenFeedItem(content.joinableUUID, if (content.isTourRelated) TimestampedObject.TOUR_CARD else TimestampedObject.ENTOURAGE_CARD)
                }
            }
        }
    }

    override fun removeRedundantNewsfeed(currentFeedList: List<NewsfeedItem>): List<NewsfeedItem> {
        val tempList = super.removeRedundantNewsfeed(currentFeedList)
        val newList = ArrayList<NewsfeedItem>()
        try {
            for (newsfeed in tempList) {
                (newsfeed.data as? Tour)?.let {card ->
                    //TODO verify if we can write !=true instead of !()==true when not a tour
                    if (!((newsfeedAdapter?.findCard(card) as? Tour)?.isSame(card)==true)) {
                        newList.add(newsfeed)
                    }
                } ?: run {
                    if(newsfeed.data != null) {
                        newList.add(newsfeed)
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
        return newList
    }

    override fun userStatusChanged(content: PushNotificationContent, status: String) {
        super.userStatusChanged(content, status)
        if (entService == null) return
        if (content.isTourRelated) {
            val timestampedObject = newsfeedAdapter?.findCard(TimestampedObject.TOUR_CARD, content.joinableId)
            if (timestampedObject is Tour) {
                val user = EntourageUser()
                user.userId = userId
                user.status = status
                entService?.notifyListenersUserStatusChanged(user, timestampedObject)
            }
        }
    }

    override fun displayFeedItemOptions(feedItem: FeedItem) {
        if (activity != null) {
            FeedItemOptionsFragment.show(feedItem, requireActivity().supportFragmentManager)
        }
    }

    fun updateFragmentFromService() {
        entService?.let {
            if (it.isRunning) {
                updateFloatingMenuOptions()
                currentTourUUID = it.currentTourId
                //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);
                addCurrentTourEncounters()
            }
            if (presenter.authenticationController.isUserToursOnly) {
                it.updateUserHistory(userId, 1, 500)
            }
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
            if (!presenter.authenticationController.isUserToursOnly) {
                Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))
            } else {
                Color.argb(255, Color.red(color), Color.green(color), Color.blue(color))
            }
        } else color
    }

    override fun updateUserHistory() {
        if (presenter.authenticationController.isUserToursOnly) {
            entService?.updateUserHistory(userId, 1, 500)
        }
    }

    override fun onShowFilter() {
        if(selectedTab== NewsfeedTabItem.TOUR_TAB) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_FILTERSCLICK)
            TourFilterFragment().show(parentFragmentManager, MapFilterFragment.TAG)
        } else {
            super.onShowFilter()
        }
    }

    override fun updateFilterButtonText() {
        val activefilters = (MapFilterFactory.mapFilter.isDefaultFilter() && selectedTab== NewsfeedTabItem.ALL_TAB)|| (TourFilter.isDefaultFilter() && selectedTab== NewsfeedTabItem.TOUR_TAB)
        fragment_map_filter_button?.setText(if (activefilters) R.string.map_no_filter else R.string.map_filters_activated)
    }

    private inner class ServiceConnection : android.content.ServiceConnection {
        private var isBound = false

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity == null) {
                isBound = false
                Timber.e("No activity for service")
                return
            }
            entService = (service as EntService.LocalBinder).service
            entService?.let {
                it.registerServiceListener(this@ToursFragment)
                it.registerApiListener(this@ToursFragment)
                updateFragmentFromService()
                it.updateNewsfeed(pagination, selectedTab)
                isBound = true
            } ?: run {
                Timber.e("Service not found")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entService?.unregisterServiceListener(this@ToursFragment)
            entService?.unregisterApiListener(this@ToursFragment)
            entService = null
            isBound = false
        }
        // ----------------------------------
        // SERVICE BINDING METHODS
        // ----------------------------------
        fun doBindService() {
            if(isBound) return
            activity?.let {
                if(EntourageApplication.me(it) ==null) {
                    // Don't start the service
                    return
                }
                try {
                    val intent = Intent(it, EntService::class.java)
                    it.startService(intent)
                    it.bindService(intent, this, Context.BIND_AUTO_CREATE)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }

        fun doUnbindService() {
            if (!isBound) return
            entService?.unregisterServiceListener(this@ToursFragment)
            activity?.unbindService(this)
            isBound = false
        }

    }
    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val MAX_TOUR_HEADS_DISPLAYED = 10
        const val TAG = "social.entourage.android.home.tours"

        fun newInstance(): ToursFragment {
            val intent = ToursFragment()
            intent.selectedTab = NewsfeedTabItem.TOUR_TAB
            intent.setGroupType(PlusFragment.KEY_START_TOUR)
            return intent
        }

        fun viewEncounter(activity: Context, encounter: Encounter) {
            if (encounter.isReadOnly) {
                val intent = Intent(activity, ReadEncounterActivity::class.java)
                val extras = Bundle()
                extras.putSerializable(ReadEncounterActivity.BUNDLE_KEY_ENCOUNTER, encounter)
                intent.putExtras(extras)
                activity.startActivity(intent)
            } else {
                val intent = Intent(activity, CreateEncounterActivity::class.java)
                val extras = Bundle()
                extras.putSerializable(CreateEncounterActivity.BUNDLE_KEY_ENCOUNTER, encounter)
                intent.putExtras(extras)
                activity.startActivity(intent)
            }
        }
    }
}