package social.entourage.android.newsfeed

import android.Manifest.permission
import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import social.entourage.android.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.map.*
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.configuration.Configuration
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.entourage.information.EntourageInformationFragment
import social.entourage.android.location.EntourageLocation
import social.entourage.android.location.LocationUtils.isLocationEnabled
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.map.*
import social.entourage.android.map.filter.MapFilterFactory
import social.entourage.android.map.filter.MapFilterFragment
import social.entourage.android.map.permissions.NoLocationPermissionFragment
import social.entourage.android.service.EntourageService
import social.entourage.android.tools.BusProvider
import social.entourage.android.user.edit.UserEditActionZoneFragment.FragmentListener
import social.entourage.android.view.EntourageSnackbar.make
import java.util.*
import javax.inject.Inject

abstract class BaseNewsfeedFragment : BaseMapFragment(R.layout.fragment_map), NewsFeedListener, FragmentListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Inject lateinit var presenter: NewsfeedPresenter
    private var onMapReadyCallback: OnMapReadyCallback? = null
    protected var userId = 0
    protected var longTapCoordinates: LatLng? = null
    private var previousEmptyListPopupLocation: Location? = null
    protected var entourageService: EntourageService? = null
    protected var loaderStop: ProgressDialog? = null
    protected var markersMap: MutableMap<String, Any?> =  TreeMap()
    private var initialNewsfeedLoaded = false
    protected var isRequestingToJoin = 0
    private var isStopped = false
    private val refreshToursHandler = Handler()

    protected var newsfeedAdapter: NewsfeedAdapter? = null
    private var refreshToursTimer: Timer? = null

    //pagination
    protected var pagination = NewsfeedPagination()
    private val scrollListener = OnScrollListener()

    // keeps tracks of the attached fragments
    private var fragmentLifecycleCallbacks: NewsfeedFragmentLifecycleCallbacks? = null

    // requested entourage group type
    private var entourageGroupType: String? = null

    // requested entourage group type
    private var entourageCategory: EntourageCategory? = null

    // current selected tab
    protected var selectedTab = NewsfeedTabItem.ALL_TAB

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BusProvider.instance.register(this)
        markersMap.clear()
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_TOURS_FROM_MENU)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(EntourageApplication.get(activity).entourageComponent)
        presenter.start()
        if (fragmentLifecycleCallbacks == null) {
            fragmentLifecycleCallbacks = NewsfeedFragmentLifecycleCallbacks()
            activity?.supportFragmentManager?.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks!!, false)
        }
        initializeMap()
        initializeFloatingMenu()
        initializeFilterTab()
        initializeNewsfeedView()
        initializeInvitations()
        (activity as MainActivity?)?.showEditActionZoneFragment()
        fragment_map_empty_list_popup_close?.setOnClickListener {onEmptyListPopupClose()}
        fragment_map_display_toggle?.setOnClickListener {onDisplayToggle()}
        map_longclick_button_entourage_action?.setOnClickListener {onCreateEntourageHelpAction()}
        fragment_map_filter_button?.setOnClickListener {onShowFilter()}
        fragment_map_new_entourages_button?.setOnClickListener {onNewEntouragesReceivedButton()}
        fragment_map_gps?.setOnClickListener {displayGeolocationPreferences()}
    }

    protected fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerNewsfeedComponent.builder()
                .entourageComponent(entourageComponent)
                .newsfeedModule(NewsfeedModule(this))
                .build()
                .inject(this)
    }

    override fun onStart() {
        super.onStart()
        if (!isLocationEnabled() && !isLocationPermissionGranted()) {
            (activity as MainActivity?)?.showEditActionZoneFragment(this)
        }
        fragment_map_feeditems_view?.addOnScrollListener(scrollListener)
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_FEED_FROM_TAB)
        isStopped = false
    }

    override fun onStop() {
        super.onStop()
        fragment_map_feeditems_view?.removeOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isStopped = true
    }

    override fun onResume() {
        super.onResume()
        timerStart()
        BusProvider.instance.post(OnLocationPermissionGranted(isLocationPermissionGranted()))
    }

    override fun onPause() {
        super.onPause()
        timerStop()
    }

    override fun onDestroy() {
        BusProvider.instance.unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        if (fragment_map_longclick?.visibility == View.VISIBLE) {
            fragment_map_longclick?.visibility = View.GONE
            return true
        }
        //before closing the fragment, send the cached tour points to server (if applicable)
        entourageService?.updateOngoingTour()
        return false
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    open fun onNotificationExtras(id: Int, choice: Boolean) {
        userId = id
    }

    fun dismissAllDialogs() {
        fragmentLifecycleCallbacks?.dismissAllDialogs()
    }

    @JvmOverloads
    fun displayChosenFeedItem(feedItemUUID: String, feedItemType: Int, invitationId: Long = 0) {
        //display the feed item
        val feedItem = newsfeedAdapter?.findCard(feedItemType, feedItemUUID) as FeedItem?
        if (feedItem != null) {
            displayChosenFeedItem(feedItem, invitationId)
            return
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItem(feedItemUUID, feedItemType, invitationId)
    }

    fun displayChosenFeedItem(feedItem: FeedItem, feedRank: Int) {
        displayChosenFeedItem(feedItem, 0, feedRank)
    }

    @JvmOverloads
    fun displayChosenFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int = 0) {
        if (context == null || isStateSaved) return
        // decrease the badge count
        EntourageApplication.get(context)?.removePushNotificationsForFeedItem(feedItem)
        //check if we are not already displaying the tour
        val fragmentManager = activity?.supportFragmentManager ?: return
        val entourageInformationFragment = fragmentManager.findFragmentByTag(EntourageInformationFragment.TAG) as EntourageInformationFragment?
        if (entourageInformationFragment != null && entourageInformationFragment.feedItemType == feedItem.type.toLong() && entourageInformationFragment.feedItemId != null && entourageInformationFragment.feedItemId.equals(feedItem.uuid, ignoreCase = true)) {
            //TODO refresh the tour info screen
            return
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItem(feedItem, invitationId, feedRank)
    }

    private fun displayChosenFeedItemFromShareURL(feedItemShareURL: String, feedItemType: Int) {
        //display the feed item
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItem(feedItemShareURL, feedItemType)
    }

    private fun act(timestampedObject: TimestampedObject) {
        if (entourageService != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_CONTACT)
            isRequestingToJoin++
            when (timestampedObject.type) {
                TimestampedObject.TOUR_CARD -> {
                    entourageService?.requestToJoinTour(timestampedObject as Tour)
                }
                TimestampedObject.ENTOURAGE_CARD -> {
                    entourageService?.requestToJoinEntourage(timestampedObject as Entourage)
                }
                else -> {
                    isRequestingToJoin--
                }
            }
        } else if (fragment_map_main_layout != null) {
            make(fragment_map_main_layout!!, R.string.tour_join_request_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    open fun displayEntourageDisclaimer() {
        // Hide the create entourage menu ui
        fragment_map_longclick?.visibility = View.GONE

        // Check if we need to show the entourage disclaimer
        if (Configuration.showEntourageDisclaimer()) {
            presenter.displayEntourageDisclaimer(entourageGroupType)
        } else {
            (activity as MainActivity?)?.onEntourageDisclaimerAccepted(null)
        }
    }

    fun createEntourage() {
        var location = EntourageLocation.getInstance().lastCameraPosition.target
        if (!BaseEntourage.TYPE_OUTING.equals(entourageGroupType, ignoreCase = true)) {
            // For demand/contribution, by default select the action zone location, if set
            val address = EntourageApplication.me(activity)?.address
            if (address != null) {
                location = LatLng(address.latitude, address.longitude)
            }
        }
        if (longTapCoordinates != null) {
            location = longTapCoordinates
            longTapCoordinates = null
        }
        presenter.createEntourage(location, entourageGroupType!!, entourageCategory)
    }

    protected fun refreshFeed() {
        clearAll()
        newsfeedAdapter?.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab)
        entourageService?.updateNewsfeed(pagination, selectedTab)
    }

    // ----------------------------------
    // BUS LISTENERS : don't susbcribe here but in children !
    // ----------------------------------
    open fun onMyEntouragesForceRefresh(event: OnMyEntouragesForceRefresh) {
        val item = event.feedItem
        if (item == null) {
            refreshFeed()
        } else {
            newsfeedAdapter?.updateCard(item)
        }
    }

    open fun onBetterLocation(event: OnBetterLocationEvent) {
        if (event.location != null) {
            centerMap(event.location)
        }
    }

    open fun onEntourageCreated(event: OnEntourageCreated) {
        //only if entourage if found
        event.entourage ?: return
        // Force the map filtering for entourages as ON
        MapFilterFactory.mapFilter.entourageCreated()
        presenter.saveMapFilter()

        // Update the newsfeed
        clearAll()
        entourageService?.updateNewsfeed(pagination, selectedTab)
    }

    open fun onEntourageUpdated(event: OnEntourageUpdated) {
        val entourage = event.entourage ?: return
        newsfeedAdapter?.updateCard(entourage)
    }

    open fun onMapFilterChanged(event: OnMapFilterChanged) {
        // Save the filter
        presenter.saveMapFilter()
        updateFilterButtonText()
        // Refresh the newsfeed
        refreshFeed()
    }

    open fun updateFilterButtonText() {
        val activefilters = (MapFilterFactory.mapFilter.isDefaultFilter() && selectedTab==NewsfeedTabItem.ALL_TAB)
        fragment_map_filter_button?.setText(if (activefilters) R.string.map_no_filter else R.string.map_filters_activated)
    }

    open fun onNewsfeedLoadMoreRequested(event: OnNewsfeedLoadMoreEvent) {
        when (selectedTab) {
            NewsfeedTabItem.ALL_TAB,
            NewsfeedTabItem.TOUR_TAB -> {
                ensureMapVisible()
                pagination.setNextDistance()
                refreshFeed()
            }
            NewsfeedTabItem.EVENTS_TAB -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_events_url)))
                try {
                    startActivity(browserIntent)
                } catch (ex: ActivityNotFoundException) {
                    if (fragment_map_main_layout != null) {
                        make(fragment_map_main_layout!!, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun onMapTabChanged(newSelectedTab: NewsfeedTabItem) {
        if (newSelectedTab == selectedTab) {
            return
        }
        selectedTab = newSelectedTab
        when(selectedTab) {
            NewsfeedTabItem.ALL_TAB -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_TAB_ALL)
                fragment_map_filter_button?.visibility = View.VISIBLE
            }
            NewsfeedTabItem.TOUR_TAB -> {
                EntourageEvents.logEvent(EntourageEvents.TOUR_FEED_TAB_EVENTS)
                fragment_map_filter_button?.visibility = View.VISIBLE
            }
            NewsfeedTabItem.EVENTS_TAB -> {
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_TAB_EVENTS)
                fragment_map_filter_button?.visibility = View.GONE
            }
        }
        clearAll()
        newsfeedAdapter?.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab)
        entourageService?.updateNewsfeed(pagination, selectedTab)
    }

    open fun userActRequested(event: OnUserActEvent) {
        if (OnUserActEvent.ACT_JOIN == event.act) {
            act(event.feedItem)
        } else if (OnUserActEvent.ACT_QUIT == event.act) {
            if (EntourageApplication.me(context) == null) {
                Toast.makeText(context, R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show()
            } else {
                val item = event.feedItem ?:return
                if (FeedItem.JOIN_STATUS_PENDING == item.joinStatus) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_CANCEL_JOIN_REQUEST)
                }
                entourageService?.removeUserFromFeedItem(item, userId)
            }
        }
    }

    open fun feedItemViewRequested(event: OnFeedItemInfoViewRequestedEvent) {
        val feedItem = event.feedItem
        if (feedItem != null) {
            displayChosenFeedItem(feedItem, event.getfeedRank())
            // update the newsfeed card
            onPushNotificationConsumedForFeedItem(feedItem)
            // update the my entourages card, if necessary
        } else {
            //check if we are receiving feed type and id
            val feedItemType = event.feedItemType
            if (feedItemType != 0) {
                val feedItemUUID = event.feedItemUUID
                if (feedItemUUID.isNullOrEmpty()) {
                    displayChosenFeedItemFromShareURL(event.feedItemShareURL, feedItemType)
                } else {
                    displayChosenFeedItem(feedItemUUID, feedItemType, event.invitationId)
                }
            }
        }
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------
    override fun onLocationUpdated(location: LatLng) {
        if (entourageService?.isRunning == true) {
            centerMap(location)
        }
    }

    override fun onNetworkException() {
        if (fragment_map_main_layout != null) {
            make(fragment_map_main_layout!!, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onCurrentPositionNotRetrieved() {
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onServerException(throwable: Throwable) {
        if (fragment_map_main_layout != null) {
            make(fragment_map_main_layout!!, R.string.server_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        if (fragment_map_main_layout != null) {
            make(fragment_map_main_layout!!, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onNewsFeedReceived(newsFeeds: List<Newsfeed>) {
        if (newsfeedAdapter == null || !isAdded) {
            pagination.isLoading = false
            pagination.isRefreshing = false
            return
        }
        val previousItemCount = newsfeedAdapter!!.dataItemCount
        val newNewsFeeds = removeRedundantNewsfeed(newsFeeds)
        //add or update the received newsfeed
        for (newsfeed in newNewsFeeds) {
            val newsfeedData = newsfeed.data
            if (newsfeedData is TimestampedObject) {
                addNewsfeedCard(newsfeedData)
            }
        }
        updatePagination(newNewsFeeds)
        redrawWholeNewsfeed(newNewsFeeds)

        // update the bottom view, if not refreshing
        if (!pagination.isRefreshing) {
            showNewsfeedBottomView(if (selectedTab == NewsfeedTabItem.ALL_TAB) newNewsFeeds.size < pagination.itemsPerPage else newsfeedAdapter!!.dataItemCount == 0)
        }
        if (newsfeedAdapter!!.dataItemCount == 0) {
            if (!pagination.isRefreshing) {
                displayFullMap()
            }
        } else {
            if (!initialNewsfeedLoaded) {
                displayListWithMapHeader()
                initialNewsfeedLoaded = true
            }
            if (!pagination.isRefreshing && previousItemCount == 0) {
                fragment_map_feeditems_view?.scrollToPosition(0)
            }
        }
        pagination.isLoading = false
        pagination.isRefreshing = false
    }

    protected open fun redrawWholeNewsfeed(newsFeeds: List<Newsfeed>) {
        //TODO do we need newsFeeds variable here ?
        if (map != null && newsFeeds.isNotEmpty() && newsfeedAdapter != null) {
            //redraw the whole newsfeed
            for (timestampedObject in newsfeedAdapter!!.items) {
                if (timestampedObject.type == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage(timestampedObject as Entourage)
                }
            }
            mapClusterManager?.cluster()
        }
    }

    private fun checkPermission() {
        if (activity == null) {
            return
        }
        if (isLocationPermissionGranted()) {
            BusProvider.instance.post(OnLocationPermissionGranted(true))
            return
        }

        // Check if the user allowed geolocation from screen 04.2 (login funnel)
        val geolocationAllowedByUser = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_GEOLOCATION_ENABLED, true)
        if (!geolocationAllowedByUser) {
            BusProvider.instance.post(OnLocationPermissionGranted(false))
            return
        }
        if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.map_permission_title)
                    .setMessage(R.string.map_permission_description)
                    .setPositiveButton(getString(R.string.activate)) { _: DialogInterface?, _: Int -> requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION) }
                    .setNegativeButton(R.string.map_permission_refuse) { _: DialogInterface?, _: Int ->
                        val noLocationPermissionFragment = NoLocationPermissionFragment()
                        noLocationPermissionFragment.show(requireActivity().supportFragmentManager, NoLocationPermissionFragment.TAG)
                        BusProvider.instance.post(OnLocationPermissionGranted(false))
                    }
                    .show()
        } else {
            requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
        }
    }

    private fun displayGeolocationPreferences() {
        displayGeolocationPreferences(false)
    }

    private fun onDisplayToggle() {
        if (!isFullMapShown) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_MAPVIEW_CLICK)
        } else {
            EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_LISTVIEW_CLICK)
        }
        toggleToursList()
    }

    fun createAction(newEntourageCategory: EntourageCategory?, newEntourageGroupType: String?) {
        entourageCategory = newEntourageCategory
        entourageGroupType = newEntourageGroupType
        entourageCategory?.isNewlyCreated = true
        displayEntourageDisclaimer()
    }

    private fun onCreateEntourageHelpAction() {
        createAction(EntourageCategoryManager.getInstance().getDefaultCategory(BaseEntourage.TYPE_DEMAND), BaseEntourage.TYPE_ACTION)
    }

    open fun onShowFilter() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_FILTERSCLICK)
        MapFilterFragment().show(parentFragmentManager, MapFilterFragment.TAG)
    }

    fun onShowEvents() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_TAB_EVENTS)
        fragment_map_top_tab?.getTabAt(NewsfeedTabItem.EVENTS_TAB.id)?.select()
    }

    private fun onNewEntouragesReceivedButton() {
        fragment_map_feeditems_view?.scrollToPosition(0)
        fragment_map_new_entourages_button?.visibility = View.GONE
    }

    // ----------------------------------
    // Map Options handler
    // ----------------------------------
    private fun initializeFloatingMenu() {
        updateFilterButtonText()
        updateFloatingMenuOptions()
    }

    protected open fun updateFloatingMenuOptions() {}
    protected open fun displayFeedItemOptions(feedItem: FeedItem) {}
    open fun feedItemCloseRequested(event: OnFeedItemCloseRequestEvent) {
        val feedItem = event.feedItem ?: return
        if (event.isShowUI) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTIVE_CLOSE_OVERLAY)
            displayFeedItemOptions(feedItem)
            return
        }
        // Only the author can close entourages/tours
        val myId = EntourageApplication.me(context)?.id
                ?: return
        if (feedItem.author == null) {
            return
        }
        if (feedItem.author.userID != myId) {
            return
        }
        if (!feedItem.isClosed) {
            // close
            stopFeedItem(feedItem, event.isSuccess)
        } else if (feedItem.type == TimestampedObject.TOUR_CARD && !feedItem.isFreezed) {
            // freeze
            freezeTour(feedItem as Tour)
        }
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    override fun showLongClickOnMapOptions(latLng: LatLng) {
        //save the tap coordinates
        longTapCoordinates = latLng
        //update the visible buttons
        map_longclick_buttons?.requestLayout()
        //hide the FAB menu
        super.showLongClickOnMapOptions(latLng)
    }

    // ----------------------------------
    // PRIVATE METHODS (lifecycle)
    // ----------------------------------
    override fun initializeMap() {
        originalMapLayoutHeight = resources.getDimensionPixelOffset(R.dimen.newsfeed_map_height)
        if (onMapReadyCallback == null) {
            onMapReadyCallback = OnMapReadyCallback { googleMap: GoogleMap? -> this.onMapReady(googleMap) }
        }
    }

    public override fun saveCameraPosition() {
        if (map != null) {
            EntourageLocation.getInstance().saveLastCameraPosition(map!!.cameraPosition)
        }
    }

    override val renderer: DefaultClusterRenderer<ClusterItem>?
        get() = MapClusterItemRenderer(requireActivity(), map, mapClusterManager as ClusterManager<MapClusterItem?>?) as DefaultClusterRenderer<ClusterItem>?


    protected fun onMapReady(googleMap: GoogleMap?) {
        super.onMapReady(googleMap,
                presenter.onClickListener as ClusterManager.OnClusterItemClickListener<ClusterItem>?,
                presenter.onGroundOverlayClickListener
        )
        map?.setOnCameraIdleListener {
            val cameraPosition = map!!.cameraPosition
            EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition)
            val currentLocation = EntourageLocation.getInstance().currentLocation
            val newLocation = EntourageLocation.cameraPositionToLocation(null, cameraPosition)
            val newZoom = cameraPosition.zoom
            if (entourageService != null && (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT)) {
                if (previousCameraZoom != newZoom) {
                    if (previousCameraZoom > newZoom) {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_ZOOM_IN)
                    } else {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_ZOOM_OUT)
                    }
                }
                previousCameraZoom = newZoom
                previousCameraLocation = newLocation

                // check if we need to cancel the current request
                if (pagination.isLoading) {
                    entourageService?.cancelNewsFeedUpdate()
                }
                newsfeedAdapter?.removeAll()
                newsfeedAdapter?.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab)
                pagination = NewsfeedPagination()
                entourageService?.updateNewsfeed(pagination, selectedTab)
                updateUserHistory()
            }
            if (isFollowing && currentLocation != null) {
                if (currentLocation.distanceTo(newLocation) > 1) {
                    isFollowing = false
                }
            }
            hideEmptyListPopup()
        }
        map?.setOnMapClickListener {
            if (activity != null) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_MAPCLICK)
                hideTourLauncher()
                if (isFullMapShown) {
                    // Hide the minicards if visible
                    if (fragment_map_entourage_mini_cards?.visibility == View.VISIBLE) {
                        fragment_map_entourage_mini_cards?.visibility = View.INVISIBLE
                    }
                } else {
                    toggleToursList()
                }
            }
        }
    }

    protected open fun updateUserHistory() {}
    private fun initializeNewsfeedView() {
        if (newsfeedAdapter == null) {
            fragment_map_feeditems_view?.layoutManager = LinearLayoutManager(context)
            newsfeedAdapter = NewsfeedAdapter()
            newsfeedAdapter!!.setOnMapReadyCallback(onMapReadyCallback)
            newsfeedAdapter!!.setOnFollowButtonClickListener { onFollowGeolocation() }
            fragment_map_feeditems_view?.adapter = newsfeedAdapter
        }
    }

    private fun initializeFilterTab() {
        if(EntourageApplication.me(activity)?.isPro ==false && fragment_map_top_tab.getTabAt(NewsfeedTabItem.TOUR_TAB.id)!=null) {
            fragment_map_top_tab?.removeTabAt(NewsfeedTabItem.TOUR_TAB.id)
            fragment_map_top_tab?.tabMode = TabLayout.MODE_FIXED
        }
        fragment_map_top_tab?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val newtab = when (tab.position) {
                    NewsfeedTabItem.EVENTS_TAB.id -> NewsfeedTabItem.EVENTS_TAB
                    NewsfeedTabItem.TOUR_TAB.id -> NewsfeedTabItem.TOUR_TAB
                    else -> NewsfeedTabItem.ALL_TAB
                }
                onMapTabChanged(newtab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------
    protected open fun hideTourLauncher() {}
    fun pauseTour(tour: Tour) {
        if (entourageService?.isRunning == true) {
            if (entourageService?.currentTourId.equals(tour.uuid, ignoreCase = true)) {
                entourageService?.pauseTreatment()
            }
        }
    }

    fun saveOngoingTour() {
        entourageService?.updateOngoingTour()
    }

    fun stopFeedItem(feedItem: FeedItem?, success: Boolean) {
        if (activity != null) {
            if (entourageService != null) {
                if (feedItem != null
                        && (!entourageService!!.isRunning
                                || feedItem.type != TimestampedObject.TOUR_CARD || entourageService!!.currentTourId.equals(feedItem.uuid, ignoreCase = true))) {
                    // Not ongoing tour, just stop the feed item
                    loaderStop = ProgressDialog.show(activity, requireActivity().getString(feedItem.closingLoaderMessage), requireActivity().getString(R.string.button_loading), true)
                    loaderStop?.setCancelable(true)
                    EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR)
                    entourageService?.stopFeedItem(feedItem, success)
                } else if (entourageService!!.isRunning) {
                    loaderStop = ProgressDialog.show(activity, requireActivity().getString(R.string.loader_title_tour_finish), requireActivity().getString(R.string.button_loading), true)
                    loaderStop?.setCancelable(true)
                    entourageService?.endTreatment()
                    EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR)
                }
            }
        }
    }

    private fun freezeTour(tour: Tour) {
        entourageService?.freezeTour(tour)
    }

    open fun userStatusChanged(content: PushNotificationContent, status: String) {
        if (entourageService != null && content.isEntourageRelated) {
            val timestampedObject = newsfeedAdapter?.findCard(TimestampedObject.ENTOURAGE_CARD, content.joinableId)
                    ?: return
            val user = TourUser()
            user.userId = userId
            user.status = status
            entourageService!!.notifyListenersUserStatusChanged(user, timestampedObject as FeedItem)
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (views)
    // ----------------------------------
    open fun needForGeoloc():Boolean {
        val me = EntourageApplication.me(activity) ?: return false
        return me.address == null
    }

    override fun updateGeolocBanner(active: Boolean) {
        if (fragment_map_gps != null) {
            //we force it because we don't need geoloc when Action zone is set
            val visibility = (!isLocationEnabled() || !isLocationPermissionGranted()) && needForGeoloc()
            fragment_map_gps.text = if (isLocationEnabled()) getString(R.string.map_gps_no_permission) else getString(R.string.map_gps_unavailable)
            fragment_map_gps.visibility = if (visibility) View.VISIBLE else View.GONE
            if(!visibility && isFullMapShown) {
                animFullMap()
            }
            adapter?.displayGeolocStatusIcon(!visibility)
        }
        super.updateGeolocBanner(active)
    }

    protected open fun removeRedundantNewsfeed(currentFeedList: List<Newsfeed>): List<Newsfeed> {
        val newsFeedList = ArrayList<Newsfeed>()
        for(newsfeed: Newsfeed in currentFeedList) {
            val card = newsfeed.data
            if (card !is TimestampedObject) {
                continue
            }
            val retrievedCard = newsfeedAdapter?.findCard(card)
            if(retrievedCard!=null) {
                if ((BaseEntourage.NEWSFEED_TYPE == newsfeed.type) && ((retrievedCard as Entourage).isSame(card as Entourage))) {
                    continue
                } else if (Announcement.NEWSFEED_TYPE == newsfeed.type) {
                    continue
                }
            }
            newsFeedList.add(newsfeed)
        }
        return newsFeedList
    }

    protected fun drawNearbyEntourage(feedItem: FeedItem?) {
        if (map == null) return
        if (feedItem?.startPoint == null) return
        if (markersMap[feedItem.hashString()] == null) {
            if (feedItem.showHeatmapAsOverlay()) {
                val position = feedItem.startPoint.location
                val heatmapIcon = BitmapDescriptorFactory.fromResource(feedItem.heatmapResourceId)
                val groundOverlayOptions = GroundOverlayOptions()
                        .image(heatmapIcon)
                        .position(position, BaseEntourage.HEATMAP_SIZE, BaseEntourage.HEATMAP_SIZE)
                        .clickable(true)
                        .anchor(0.5f, 0.5f)
                markersMap[feedItem.hashString()] = map!!.addGroundOverlay(groundOverlayOptions)
                presenter.onGroundOverlayClickListener?.addEntourageGroundOverlay(position, feedItem)
            } else {
                val mapClusterItem = MapClusterItem(feedItem)
                markersMap[feedItem.hashString()] = mapClusterItem
                mapClusterManager?.addItem(mapClusterItem)
            }
        }
    }

    private fun addNewsfeedCard(card: TimestampedObject) {
        if (newsfeedAdapter?.findCard(card) != null) {
            newsfeedAdapter?.updateCard(card)
        } else {
            // set the badge count
            if (card is FeedItem) {
                EntourageApplication.get(context)?.updateBadgeCountForFeedItem(card)
            }
            // add the card
            if (pagination.isRefreshing) {
                newsfeedAdapter?.addCardInfoBeforeTimestamp(card)
            } else {
                newsfeedAdapter?.addCardInfo(card)
            }
        }
    }

    open fun clearAll() {
        map?.clear()
        mapClusterManager?.clearItems()
        markersMap.clear()
        presenter.onClickListener?.clear()
        presenter.onGroundOverlayClickListener?.clear()
        resetFeed()
    }

    private fun resetFeed() {
        newsfeedAdapter?.removeAll()

        // check if we need to cancel the current request
        if (pagination.isLoading) {
            entourageService?.cancelNewsFeedUpdate()
        }
        pagination.reset()
    }

    fun displayFullMap() {
        if (newsfeedAdapter == null || fragment_map_main_layout == null) {
            return
        }
        // show the empty list popup if necessary
        if (newsfeedAdapter!!.dataItemCount == 0) {
            showEmptyListPopup()
        }
        if (isFullMapShown) {
            return
        }
        isFullMapShown = true
        fragment_map_new_entourages_button?.visibility = View.GONE
        fragment_map_display_toggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        ensureMapVisible()
        animFullMap()
    }

    private fun animFullMap() {
        val targetHeight = fragment_map_main_layout?.measuredHeight ?: return
        newsfeedAdapter?.setMapHeight(targetHeight) ?:return
        val anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight)
        anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
        anim.start()
    }

    private fun displayListWithMapHeader() {
        if (!isFullMapShown || fragment_map_main_layout==null) {
            return
        }
        isFullMapShown = false
        fragment_map_display_toggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_white_24dp))
        fragment_map_entourage_mini_cards?.visibility = View.INVISIBLE
        hideEmptyListPopup()
        val anim = ValueAnimator.ofInt(fragment_map_main_layout.measuredHeight, originalMapLayoutHeight)
        anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
        anim.start()
    }

    private fun toggleToursList() {
        if (!isFullMapShown) {
            displayFullMap()
            EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_06_2)
        } else {
            displayListWithMapHeader()
            EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_06_1)
        }
    }

    private val isToursListVisible: Boolean
        get() = !isFullMapShown

    private fun ensureMapVisible() {
        fragment_map_feeditems_view?.scrollToPosition(0)
    }

    private fun updatePagination(newsfeedList: List<Newsfeed>?) {
        if (newsfeedList.isNullOrEmpty()) {
            pagination.loadedItems(null, null)
            return
        }
        when (selectedTab) {
            NewsfeedTabItem.ALL_TAB,
            NewsfeedTabItem.TOUR_TAB -> {
                var newestUpdatedDate: Date? = null
                var oldestUpdateDate: Date? = null
                for (newsfeed in newsfeedList) {
                    if(newsfeed.data !is FeedItem) continue
                    val feedUpdatedDate = (newsfeed.data as FeedItem?)?.updatedTime
                            ?: continue
                    if (newestUpdatedDate == null || newestUpdatedDate.before(feedUpdatedDate)) {
                        newestUpdatedDate = feedUpdatedDate
                    }
                    if (oldestUpdateDate == null || oldestUpdateDate.after(feedUpdatedDate)) {
                        oldestUpdateDate = feedUpdatedDate
                    }
                }
                pagination.loadedItems(newestUpdatedDate, oldestUpdateDate)
            }
            NewsfeedTabItem.EVENTS_TAB -> if (newsfeedAdapter != null) {
                var position = newsfeedAdapter!!.itemCount
                while (position >= 0) {
                    val card = newsfeedAdapter!!.getCardAt(position)
                    if (card is FeedItem) {
                        pagination.lastFeedItemUUID = card.uuid
                        break
                    }
                    position--
                }
            }
        }
    }

    // ----------------------------------
    // Push handling
    // ----------------------------------
    fun onPushNotificationReceived(message: Message) {
        //refresh the newsfeed
        if (entourageService != null) {
            pagination.isRefreshing = true
            entourageService!!.updateNewsfeed(pagination, selectedTab)
        }
        //update the badge count on tour card
        val content = message.content ?: return
        if (newsfeedAdapter == null) {
            return
        }
        val joinableId = content.joinableId
        val isChatMessage = PushNotificationContent.TYPE_NEW_CHAT_MESSAGE == content.type
        if (content.isTourRelated) {
            val tour = newsfeedAdapter?.findCard(TimestampedObject.TOUR_CARD, joinableId) as Tour ?
                    ?: return
            tour.increaseBadgeCount(isChatMessage)
            newsfeedAdapter!!.updateCard(tour)
        } else if (content.isEntourageRelated) {
            val entourage = newsfeedAdapter?.findCard(TimestampedObject.ENTOURAGE_CARD, joinableId) as Entourage ?
                    ?: return
            entourage.increaseBadgeCount(isChatMessage)
            newsfeedAdapter!!.updateCard(entourage)
        }
    }

    private fun onPushNotificationConsumedForFeedItem(feedItem: FeedItem) {
        val feedItemCard = newsfeedAdapter?.findCard(feedItem) as FeedItem?
                ?: return
        feedItemCard.decreaseBadgeCount()
        newsfeedAdapter?.updateCard(feedItemCard)
    }

    // ----------------------------------
    // Refresh tours timer handling
    // ----------------------------------
    private fun timerStart() {
        //create the timer
        refreshToursTimer = Timer()
        //create the task
        val refreshToursTimerTask: TimerTask = object : TimerTask() {
            override fun run() {
                refreshToursHandler.post {
                    if (entourageService != null) {
                        if (selectedTab != NewsfeedTabItem.EVENTS_TAB) {
                            pagination.isRefreshing = true
                            entourageService!!.updateNewsfeed(pagination, selectedTab)
                        }
                    }
                }
            }
        }
        //schedule the timer
        refreshToursTimer?.schedule(refreshToursTimerTask, DELAY_REFRESH_TOURS_INTERVAL, REFRESH_TOURS_INTERVAL)
    }

    private fun timerStop() {
        refreshToursTimer?.cancel()
        refreshToursTimer = null
    }

    // ----------------------------------
    // INVITATIONS
    // ----------------------------------
    private fun initializeInvitations() {
        // Check if it's a valid user and onboarding
        if (EntourageApplication.me(activity)?.isOnboardingUser == true) {
            // Retrieve the list of invitations
            presenter.myPendingInvitations
        }
    }

    fun onInvitationsReceived(invitationList: List<Invitation>?) {
        // Check for errors and empty list
        if (invitationList.isNullOrEmpty()) {
            // Check if we need to show the carousel
            if (EntourageApplication.me(activity)?.isOnboardingUser == true) {
                showCarousel()
                // Reset the onboarding flag
                presenter.resetUserOnboardingFlag()
            }
        } else {
            for (invitation in invitationList) {
                presenter.acceptInvitation(invitation.id)
            }
            // Show the first invitation
            val firstInvitation = invitationList[0]
            presenter.openFeedItem(firstInvitation.entourageUUID, FeedItem.ENTOURAGE_CARD, firstInvitation.id)
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------
    private fun onEmptyListPopupClose() {
        presenter.isShowNoEntouragesPopup = false
        hideEmptyListPopup()
    }

    private fun showEmptyListPopup() {
        previousEmptyListPopupLocation = if (previousEmptyListPopupLocation == null) {
            EntourageLocation.getInstance().currentLocation
        } else {
            // Show the popup only we moved from the last position we show it
            val currentLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().currentCameraPosition)
            if (previousEmptyListPopupLocation!!.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                return
            }
            currentLocation
        }
        // Check if we need to show the popup
        if (presenter.isShowNoEntouragesPopup) {
            fragment_map_empty_list_popup?.visibility = View.VISIBLE
        }

    }

    private fun hideEmptyListPopup() {
        fragment_map_empty_list_popup?.visibility = View.GONE
    }

    // ----------------------------------
    // CAROUSEL
    // ----------------------------------
    private fun showCarousel() {
        val h = Handler()
        h.postDelayed({

            // Check if the activity is still running
            if (activity == null || requireActivity().isFinishing || isStopped) {
                return@postDelayed
            }
            // Check if the map fragment is still on top
            if(fragmentLifecycleCallbacks?.topFragment == null) return@postDelayed
            (activity as MainActivity?)!!.showTutorial()
        }, Constants.CAROUSEL_DELAY_MILLIS)
    }

    // ----------------------------------
    // Newsfeed Bottom View Handling
    // ----------------------------------
    private fun showNewsfeedBottomView(show: Boolean) {
        newsfeedAdapter?.showBottomView(show,
                when {
                    pagination.isNextDistanceAvailable -> {
                        // we can increase the distance
                        NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE
                    }
                    newsfeedAdapter!!.dataItemCount == 0 -> {
                        // max distance and still no items, show no items info
                        NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS
                    }
                    else -> {
                        // max distance and items, show no more items info
                        NewsfeedBottomViewHolder.CONTENT_TYPE_NO_MORE_ITEMS
                    }
                },
                selectedTab)
    }

    // ----------------------------------
    // Heatzone Tap Handling
    // ----------------------------------
    fun handleHeatzoneClick(location: LatLng?) {
        hideTourLauncher()
        if (isToursListVisible) {
            centerMapAndZoom(location, ZOOM_HEATZONE, true)
            toggleToursList()
        } else {
            showHeatzoneMiniCardsAtLocation(location)
        }
    }

    private fun showHeatzoneMiniCardsAtLocation(location: LatLng?) {
        if (newsfeedAdapter == null) {
            return
        }
        // get the list of entourages near this location
        val entourageArrayList = ArrayList<TimestampedObject>()
        for (feedItem in newsfeedAdapter!!.items) {
            if (feedItem.type != TimestampedObject.ENTOURAGE_CARD) continue
            val entourage = feedItem as Entourage
            if (entourage.distanceToLocation(location) < HEATZONE_SEARCH_RADIUS) {
                entourageArrayList.add(entourage)
            }
        }
        if (entourageArrayList.size == 0) return
        //show the minicards list
        fragment_map_entourage_mini_cards?.setEntourages(entourageArrayList)
        //zoom in the heatzone
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition(location, ZOOM_HEATZONE, 0F, 0F)))
        saveCameraPosition()
    }

    // ----------------------------------
    // UserEditActionZoneFragment.FragmentListener
    // ----------------------------------
    override fun onUserEditActionZoneFragmentDismiss() {}
    override fun onUserEditActionZoneFragmentAddressSaved() {
        storeActionZoneInfo(false)
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        storeActionZoneInfo(true)
        checkPermission()
    }

    private fun storeActionZoneInfo(ignoreAddress: Boolean) {
        val authenticationController = EntourageApplication.get().entourageComponent.authenticationController
        authenticationController.userPreferences.isIgnoringActionZone = ignoreAddress
        authenticationController.saveUserPreferences()
        if (!ignoreAddress) {
            EntourageApplication.me(activity)?.address?.let {
                centerMap(LatLng(it.latitude, it.longitude))
            }
        }
    }

    open fun onAddEncounter() {}
    open fun addEncounter() {}
    private fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        if (newsfeedAdapter == null) {
            return
        }
        val newHeight = valueAnimator.animatedValue as Int
        newsfeedAdapter!!.setMapHeight(newHeight)
        fragment_map_feeditems_view?.layoutManager?.requestLayout()
    }

    override val adapter: HeaderBaseAdapter?
        get() { return newsfeedAdapter}

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private inner class OnScrollListener : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0) {
                // Scrolling down
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                        ?: return
                val visibleItemCount = recyclerView.childCount
                val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                val totalItemCount = linearLayoutManager.itemCount
                if (totalItemCount - visibleItemCount <= firstVisibleItem + 2) {
                    if (entourageService != null && entourageService!!.updateNewsfeed(pagination, selectedTab)) {
                        //if update returns false no need to log this...
                        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_SCROLL_LIST)
                    }
                }
            }
        }

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {}
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.fragment_map"
        private const val DELAY_REFRESH_TOURS_INTERVAL: Long = 3000 // 3 seconds delay when starting the timer to refresh the feed
        private const val REFRESH_TOURS_INTERVAL: Long = 60000 //1 minute in ms

        // Radius of the circle where to search for entourages when user taps a heatzone
        private const val HEATZONE_SEARCH_RADIUS = BaseEntourage.HEATMAP_SIZE.toInt() / 2 // meters

        // Zoom in level when taping a heatzone
        private const val ZOOM_HEATZONE = 15.7f
        @JvmStatic
        fun getTransparentColor(color: Int): Int {
            return Color.argb(200, Color.red(color), Color.green(color), Color.blue(color))
        }

        @JvmStatic
        fun isToday(date: Date): Boolean {
            val calToday = Calendar.getInstance()
            calToday.time =  Date()
            val calDate = Calendar.getInstance()
            calDate.time = date
            return calToday[Calendar.ERA] == calDate[Calendar.ERA] && calToday[Calendar.DAY_OF_YEAR] == calDate[Calendar.DAY_OF_YEAR] && calToday[Calendar.YEAR] == calDate[Calendar.YEAR]
        }
    }

    init {
        eventLongClick = EntourageEvents.EVENT_MAP_LONGPRESS
    }
}