package social.entourage.android.map

import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.entourage.layout_map_launcher.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import kotlinx.android.synthetic.main.layout_map_longclick.view.*
import social.entourage.android.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.map.*
import social.entourage.android.api.model.map.Tour.TourComparatorOldToNew
import social.entourage.android.api.model.map.Tour.Tours
import social.entourage.android.api.tape.Events.*
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.location.EntourageLocation
import social.entourage.android.location.LocationUtils.isLocationEnabled
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.newsfeed.FeedItemOptionsFragment
import social.entourage.android.service.EntourageService
import social.entourage.android.service.EntourageService.LocalBinder
import social.entourage.android.service.TourServiceListener
import social.entourage.android.tour.choice.ChoiceFragment
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment
import social.entourage.android.tour.encounter.CreateEncounterActivity
import social.entourage.android.tour.join.TourJoinRequestFragment
import social.entourage.android.view.EntourageSnackbar.make
import timber.log.Timber
import java.util.*

class MapWithTourFragment : MapFragment(), TourServiceListener {
    private val connection = ServiceConnection()
    private var isBound = false
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

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isBound) {
            doBindService()
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

    override fun onDestroy() {
        if (isBound ) {
            entourageService?.unregisterServiceListener(this)
            doUnbindService()
        }
        super.onDestroy()
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

    override fun displayEntourageDisclaimer() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTION_CREATE_CLICK)
        // if we have an ongoing tour
        if (activity != null && isBound && entourageService?.isRunning==true) {
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

    private fun putEncounterOnMap(encounter: Encounter?) {
        if (map == null || presenter == null) {
            // The map is not yet initialized or the google play services are outdated on the phone
            return
        }
        if(presenter?.onClickListener?.getEncounterMapClusterItem(encounter!!.id) != null) {
            //the item aalready exists
            return
        }
        val mapClusterItem = MapClusterItem(encounter)
        presenter?.onClickListener?.addEncounterMapClusterItem(mapClusterItem, encounter)
        mapClusterManager?.addItem(mapClusterItem)
    }

    private fun checkAction(action: String) {
        if (activity == null || !isBound) return
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
            PlusFragment.KEY_CREATE_CONTRIBUTION -> createAction(EntourageCategoryManager.getInstance().getDefaultCategory(Entourage.TYPE_CONTRIBUTION), Entourage.TYPE_ACTION)
            PlusFragment.KEY_CREATE_DEMAND -> createAction(EntourageCategoryManager.getInstance().getDefaultCategory(Entourage.TYPE_DEMAND), Entourage.TYPE_ACTION)
            PlusFragment.KEY_CREATE_OUTING -> createAction(null, Entourage.TYPE_OUTING)
            PlusFragment.KEY_ADD_ENCOUNTER -> onAddEncounter()
        }
    }

    override fun onNotificationExtras(id: Int, choice: Boolean) {
        super.onNotificationExtras(id, choice)
        userHistory = choice
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun onUserChoiceChanged(event: OnUserChoiceEvent) {
        userHistory = event.isUserHistory
        if (userHistory) {
            entourageService?.updateUserHistory(userId, 1, 500)
            showUserHistory()
        } else {
            hideUserHistory()
        }
    }

    @Subscribe
    fun onUserInfoUpdated(event: OnUserInfoUpdatedEvent?) {
        if (newsfeedAdapter == null) return
        val meAsAuthor = EntourageApplication.me(context)?.asTourAuthor() ?: return
        val dirtyList: MutableList<TimestampedObject> = ArrayList()
        // See which cards needs updating
        for (timestampedObject in newsfeedAdapter!!.items) {
            if (timestampedObject !is FeedItem) continue
            // Skip null author
            val author = timestampedObject.author ?: continue
            // Skip not same author id
            if (author.userID != meAsAuthor.userID) continue
            // Skip if nothing changed
            if (author.isSame(meAsAuthor)) continue
            // Update the tour author
            meAsAuthor.userName = author.userName
            timestampedObject.author = meAsAuthor
            // Mark as dirty
            dirtyList.add(timestampedObject)
        }
        // Update the dirty cards
        for (dirty in dirtyList) {
            newsfeedAdapter!!.updateCard(dirty)
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------
    private fun onStartTourLauncher() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_TOUR_CREATE_CLICK)
        if (entourageService?.isRunning==true) {
            // Check if the geolocation is permitted
            if (!isLocationEnabled() || !isLocationPermissionGranted()) {
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
        startTour(tourType.getName())
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
        if (presenter != null) {
            if (presenter!!.shouldDisplayEncounterDisclaimer()) {
                presenter!!.displayEncounterDisclaimer()
            } else {
                addEncounter()
            }
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
            presenter?.setDisplayEncounterDisclaimer(false)
        }
    }

    override fun updateFloatingMenuOptions() {
        tour_stop_button?.visibility = if (entourageService?.isRunning==true) View.VISIBLE else View.GONE
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    override fun showLongClickOnMapOptions(latLng: LatLng) {
        //save the tap coordinates
        longTapCoordinates = latLng
        //hide the FAB menu
        tour_stop_button?.visibility = View.GONE
        //for public user, start the create entourage funnel directly
        val me = EntourageApplication.me(activity)
        val isPro = me != null && me.isPro
        if (!isPro) {
            displayEntourageDisclaimer()
            return
        }
        //update the visible buttons
        val isTourRunning = entourageService?.isRunning==true
        map_longclick_buttons?.map_longclick_button_start_tour_launcher?.visibility = if (isTourRunning) View.INVISIBLE else View.VISIBLE
        map_longclick_buttons?.map_longclick_button_create_encounter?.visibility = if (isTourRunning) View.VISIBLE else View.GONE
        super.showLongClickOnMapOptions(latLng)
    }

    // ----------------------------------
    // BUS LISTENERS : needs to be in final class (not in parent class
    // ----------------------------------
    @Subscribe
    override fun onMyEntouragesForceRefresh(event: OnMyEntouragesForceRefresh) {
        super.onMyEntouragesForceRefresh(event)
    }

    @Subscribe
    override fun onEntourageCreated(event: OnEntourageCreated) {
        super.onEntourageCreated(event)
    }

    @Subscribe
    override fun onEntourageUpdated(event: OnEntourageUpdated) {
        super.onEntourageUpdated(event)
    }

    @Subscribe
    override fun onNewsfeedLoadMoreRequested(event: OnNewsfeedLoadMoreEvent) {
        super.onNewsfeedLoadMoreRequested(event)
    }

    @Subscribe
    override fun onMapFilterChanged(event: OnMapFilterChanged) {
        super.onMapFilterChanged(event)
    }

    @Subscribe
    override fun onBetterLocation(event: OnBetterLocationEvent) {
        super.onBetterLocation(event)
    }

    @Subscribe
    fun onEncounterCreated(event: OnEncounterCreated) {
        if (entourageService != null) { //refresh button just in case
            tour_stop_button?.visibility = if (entourageService?.isRunning==true) View.VISIBLE else View.GONE
        }
        val encounter = event.encounter ?: return
        addEncounter(encounter)
        putEncounterOnMap(encounter)
    }

    @Subscribe
    fun onEncounterUpdated(event: OnEncounterUpdated?) {
        if (presenter == null) return
        val updatedEncounter = event?.encounter ?: return
        val mapClusterItem = presenter?.onClickListener?.removeEncounterMapClusterItem(updatedEncounter.id)
        if (mapClusterItem != null) {
            mapClusterManager?.removeItem(mapClusterItem)
        }
        updateEncounter(updatedEncounter)
        putEncounterOnMap(updatedEncounter)
    }

    @Subscribe
    override fun feedItemViewRequested(event: OnFeedItemInfoViewRequestedEvent) {
        super.feedItemViewRequested(event)
    }

    @Subscribe
    override fun userActRequested(event: OnUserActEvent) {
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
                if (fragment_map_tours_view.visibility == View.VISIBLE) {
                    displayFullMap()
                }
                addTourCard(entourageService!!.currentTour)
                tour_stop_button?.visibility = View.VISIBLE
                presenter?.incrementUserToursCount()
                presenter?.setDisplayEncounterDisclaimer(true)
            } else if (fragment_map_main_layout != null) {
                make(fragment_map_main_layout, R.string.tour_creation_fail, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onTourUpdated(newPoint: LatLng) {
        drawCurrentLocation(newPoint)
    }

    override fun onTourResumed(pointsToDraw: List<TourPoint>, tourType: String, startDate: Date) {
        if (pointsToDraw.isNotEmpty()) {
            drawCurrentTour(pointsToDraw, tourType, startDate)
            previousCoordinates = pointsToDraw[pointsToDraw.size - 1].location
            val currentLocation = EntourageLocation.getInstance().currentLocation
            centerMap(LatLng(currentLocation.latitude, currentLocation.longitude))
            isFollowing = true
        }
        tour_stop_button.visibility = View.VISIBLE
    }

    override fun onRetrieveToursNearby(tours: List<Tour>) {
        if (newsfeedAdapter == null) {
            return
        }
        var nearbyTours = tours
        //check if there are tours to add or update
        val previousToursCount = newsfeedAdapter!!.dataItemCount
        nearbyTours = removeRedundantTours(nearbyTours, false)
        Collections.sort(nearbyTours, TourComparatorOldToNew())
        for (tour in nearbyTours) {
            if (!tour.uuid.equals(currentTourUUID, ignoreCase = true)) {
                addTourCard(tour)
            }
        }
        //recreate the map if needed
        if (nearbyTours.isNotEmpty() && map != null) {
            map?.clear()
            for (timestampedObject in newsfeedAdapter!!.items) {
                if (timestampedObject.type == TimestampedObject.TOUR_CARD) {
                    val tour = timestampedObject as Tour
                    if (!tour.uuid.equals(currentTourUUID, ignoreCase = true)) {
                        drawNearbyTour(timestampedObject, false)
                    }
                }
            }
            if (entourageService != null && currentTourUUID.isNotEmpty()) {
                val line = PolylineOptions()
                for (polyline in currentTourLines) {
                    line.addAll(polyline.points)
                }
                line.zIndex(2f)
                line.width(15f)
                line.color(color)
                map?.addPolyline(line)
                addCurrentTourEncounters()
            }
        }

        //show the map if no tours
        if (newsfeedAdapter!!.dataItemCount == 0) {
            displayFullMap()
        } else if (previousToursCount == 0) {
            displayListWithMapHeader()
        }
        //scroll to latest
        if (newsfeedAdapter!!.dataItemCount > 0) {
            fragment_map_tours_view.scrollToPosition(0)
        }
    }

    override fun onRetrieveToursByUserId(tours: List<Tour>) {
        var nearbyTours = tours
        nearbyTours = removeRedundantTours(nearbyTours, true)
        nearbyTours = removeRecentTours(nearbyTours)
        Collections.sort(nearbyTours, TourComparatorOldToNew())
        for (tour in nearbyTours) {
            if (!tour.uuid.equals(currentTourUUID, ignoreCase = true)) {
                drawNearbyTour(tour, true)
            }
        }
    }

    override fun onUserToursFound(tours: Map<Long, Tour>) {}

    override fun onToursFound(tours: Map<Long, Tour>) {
        if (activity == null) {
            return
        }
        if (tours.isEmpty()) {
            if (fragment_map_main_layout != null) {
                make(fragment_map_main_layout, R.string.tour_info_text_nothing_found, Snackbar.LENGTH_SHORT).show()
            }
        } else if (tours.size > 1) { //more than 1 tour
                val choiceFragment = ChoiceFragment.newInstance(Tours(ArrayList(tours.values)))
                choiceFragment.show(requireActivity().supportFragmentManager, "fragment_choice")
        } else
            presenter?.openFeedItem(tours[0] as FeedItem, 0, 0)
    }

    override fun onFeedItemClosed(closed: Boolean, feedItem: FeedItem) {
        @StringRes var message: Int
        if (closed) {
            refreshFeed()
            if (feedItem.type == TimestampedObject.TOUR_CARD) {
                if (feedItem.uuid.equals(currentTourUUID, ignoreCase = true)) {
                    updateFloatingMenuOptions()
                    currentTourUUID = ""
                } else {
                    entourageService?.notifyListenersTourResumed()
                }
            }
            if (userHistory) {
                entourageService?.updateUserHistory(userId, 1, 1)
            }
            message = feedItem.closedToastMessage
        } else {
            message = R.string.tour_close_fail
            if (feedItem.type == TimestampedObject.TOUR_CARD
                    && feedItem.status == FeedItem.STATUS_FREEZED) {
                message = R.string.tour_freezed
            }
        }
        if (fragment_map_main_layout != null) {
            make(fragment_map_main_layout, message, Snackbar.LENGTH_SHORT).show()
        }
        loaderStop?.dismiss()
        loaderStop = null
    }

    override fun onUserStatusChanged(user: TourUser, feedItem: FeedItem) {
        if (activity == null || requireActivity().isFinishing) return
        if (feedItem.type == TimestampedObject.TOUR_CARD || feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            feedItem.joinStatus = user.status
            if (user.status == Tour.JOIN_STATUS_PENDING) {
                try {
                    TourJoinRequestFragment.newInstance(feedItem).show(requireActivity().supportFragmentManager, TourJoinRequestFragment.TAG)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }
        updateNewsfeedJoinStatus(feedItem)
        isRequestingToJoin--
    }

    public override fun hideTourLauncher() {
        if (layout_map_launcher?.visibility == View.VISIBLE) {
            layout_map_launcher.visibility = View.GONE
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------
    override fun redrawWholeNewsfeed(newsFeeds: List<Newsfeed>) {
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
                    drawNearbyEntourage(timestampedObject as Entourage)
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

    private val currentTour: Tour?
        get() = entourageService?.currentTour

    private fun startTour(type: String) {
        if (entourageService?.isRunning==true) {
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
        if (entourageService?.isRunning==true) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_RESTART_TOUR)
            entourageService?.resumeTreatment()
        }
    }

    private fun launchConfirmationFragment() {
        pauseTour()
        TourEndConfirmationFragment.newInstance(currentTour).show(parentFragmentManager, TourEndConfirmationFragment.TAG)
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

    override fun removeRedundantNewsfeed(currentFeedList: List<Newsfeed>): List<Newsfeed> {
        val tempList = super.removeRedundantNewsfeed(currentFeedList).toMutableList()
        val newList = ArrayList<Newsfeed>()
        try {
            for (newsfeed in tempList) {
                val card = newsfeed.data
                if (card !is TimestampedObject) {
                    continue
                }
                val retrievedCard = newsfeedAdapter?.findCard(card)
                if (retrievedCard!=null && (Tour.NEWSFEED_TYPE == newsfeed.type) && ((retrievedCard as Tour).isSame(card as Tour))) {
                    continue
                }
                newList.add(newsfeed)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
        return newList
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
        if (presenter == null) {
            Timber.e("MapPresenter not ready")
            return
        }
        val encounters = entourageService?.currentTour?.encounters
        if (encounters?.isNotEmpty() == true) {
            for (encounter in encounters) {
                putEncounterOnMap(encounter)
            }
        }
    }

    private fun drawCurrentTour(pointsToDraw: List<TourPoint>, tourType: String, startDate: Date) {
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
            line.zIndex(if (isToday(tour.startTime)) 1f else 0f)
            line.width(15f)
            line.color(getTrackColor(isHistory, tour.tourType, tour.startTime))
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
            if (tour.tourStatus == null) {
                tour.tourStatus = FeedItem.STATUS_CLOSED
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
        MapClusterItem(tour).let {
            markersMap[tour.hashString()] = it
            mapClusterManager?.addItem(it)
        }
    }

    @Subscribe
    override fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        super.onLocationPermissionGranted(event)
    }

    override fun clearAll() {
        super.clearAll()
        currentTourLines.clear()
        drawnToursMap.clear()
        drawnUserHistory.clear()
        previousCoordinates = null
        displayedTourHeads = 0
    }

    private fun updateNewsfeedJoinStatus(timestampedObject: TimestampedObject) {
        newsfeedAdapter?.updateCard(timestampedObject)
    }

    private fun hideUserHistory() {
        for (tour in retrievedHistory.values) {
            val line = drawnUserHistory[tour.id] ?: continue
            line.color = getTrackColor(true, tour.tourType, tour.startTime)
        }
    }

    private fun showUserHistory() {
        for ((key, line) in drawnUserHistory) {
            val tour = retrievedHistory[key] ?: continue
            line.color = getTrackColor(true, tour.tourType, tour.startTime)
        }
    }

    override fun userStatusChanged(content: PushNotificationContent, status: String) {
        super.userStatusChanged(content, status)
        if (entourageService == null) return
        if (content.isTourRelated) {
            val timestampedObject = newsfeedAdapter?.findCard(TimestampedObject.TOUR_CARD, content.joinableId)
            if (timestampedObject is Tour) {
                val user = TourUser()
                user.userId = userId
                user.status = status
                entourageService?.notifyListenersUserStatusChanged(user, timestampedObject)
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

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------
    private fun doBindService() {
        activity?.let {
            if(EntourageApplication.me(it) ==null) {
                // Don't start the service
                return
            }
            try {
                val intent = Intent(it, EntourageService::class.java)
                it.startService(intent)
                it.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    private fun doUnbindService() {
        if (isBound) {
            activity?.unbindService(connection)
            isBound = false
        }
    }

    override fun displayFeedItemOptions(feedItem: FeedItem) {
        if (activity != null) {
            FeedItemOptionsFragment.newInstance(feedItem).show(requireActivity().supportFragmentManager, FeedItemOptionsFragment.TAG)
        }
    }

    @Subscribe
    override fun feedItemCloseRequested(event: OnFeedItemCloseRequestEvent) {
        super.feedItemCloseRequested(event)
    }

    @Subscribe
    fun checkIntentAction(event: OnCheckIntentActionEvent) {
        if (activity == null) {
            Timber.w("No activity found")
            return
        }
        checkAction(event.action)
        val message: Message = event.extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as Message?
                ?: return
        val content = message.content
                ?: return
        val extra = content.extra
        when (event.action) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE,
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST,
            PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED -> if (content.isTourRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.TOUR_CARD)
            } else if (content.isEntourageRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.ENTOURAGE_CARD)
            }
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION -> if (extra != null) {
                displayChosenFeedItem(extra.entourageId.toString(), TimestampedObject.ENTOURAGE_CARD, extra.invitationId.toLong())
            }
            PushNotificationContent.TYPE_INVITATION_STATUS -> if (extra != null && (content.isEntourageRelated || content.isTourRelated)) {
                displayChosenFeedItem(content.joinableUUID, if (content.isTourRelated) TimestampedObject.TOUR_CARD else TimestampedObject.ENTOURAGE_CARD)
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private inner class ServiceConnection : android.content.ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity == null) {
                Timber.e("No activity for service")
                return
            }
            entourageService = (service as LocalBinder).service
            if (entourageService == null) {
                Timber.e("Service not found")
                return
            }
            entourageService!!.registerServiceListener(this@MapWithTourFragment)
            entourageService!!.registerApiListener(this@MapWithTourFragment)
            if (entourageService?.isRunning==true) {
                updateFloatingMenuOptions()
                currentTourUUID = entourageService!!.currentTourId
                //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);
                addCurrentTourEncounters()
            }
            entourageService!!.updateNewsfeed(pagination, selectedTab)
            if (userHistory) {
                entourageService!!.updateUserHistory(userId, 1, 500)
            }
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entourageService?.unregisterServiceListener(this@MapWithTourFragment)
            entourageService?.unregisterApiListener(this@MapWithTourFragment)
            entourageService = null
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val MAX_TOUR_HEADS_DISPLAYED = 10
    }
}