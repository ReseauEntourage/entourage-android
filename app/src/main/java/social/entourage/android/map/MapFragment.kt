package social.entourage.android.map

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
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.entourage.fragment_map.*
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
import social.entourage.android.map.filter.MapFilterFactory.mapFilter
import social.entourage.android.map.filter.MapFilterFragment
import social.entourage.android.map.permissions.NoLocationPermissionFragment
import social.entourage.android.newsfeed.NewsFeedListener
import social.entourage.android.newsfeed.NewsfeedAdapter
import social.entourage.android.newsfeed.NewsfeedBottomViewHolder
import social.entourage.android.newsfeed.NewsfeedPagination
import social.entourage.android.service.EntourageService
import social.entourage.android.tools.BusProvider
import social.entourage.android.user.edit.UserEditActionZoneFragment.FragmentListener
import social.entourage.android.view.EntourageSnackbar.make
import java.util.*
import javax.inject.Inject

abstract class MapFragment protected constructor() : BaseMapFragment(R.layout.fragment_map), NewsFeedListener, FragmentListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @Inject
    var presenter: MapPresenter? = null
    private var onMapReadyCallback: OnMapReadyCallback? = null
    protected var userId = 0
    protected var longTapCoordinates: LatLng? = null
    private var previousEmptyListPopupLocation: Location? = null
    protected var entourageService: EntourageService? = null
    protected var loaderStop: ProgressDialog? = null
    protected var markersMap: MutableMap<String, Any?>? = null
    private var initialNewsfeedLoaded = false
    protected var isRequestingToJoin = 0
    private var isStopped = false
    private val refreshToursHandler = Handler()
    private var onTabSelectedListener: OnTabSelectedListener? = null

    /*@BindView(R.id.fragment_map_main_layout)
    var layoutMain: RelativeLayout? = null

    @BindView(R.id.fragment_map_display_toggle)
    var mapDisplayToggle: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.fragment_map_new_entourages_button)
    var newEntouragesButton: Button? = null

    @JvmField
    @BindView(R.id.fragment_map_empty_list)
    var emptyListTextView: TextView? = null

    @JvmField
    @BindView(R.id.fragment_map_empty_list_popup)
    var emptyListPopup: View? = null

    @JvmField
    @BindView(R.id.fragment_map_entourage_mini_cards)
    var miniCardsView: EntourageMiniCardsView? = null*/

    protected var newsfeedAdapter: NewsfeedAdapter? = null
    private var refreshToursTimer: Timer? = null

    //pagination
    protected var pagination = NewsfeedPagination()
    private val scrollListener = OnScrollListener()

    // keeps tracks of the attached fragments
    private var fragmentLifecycleCallbacks: MapFragmentLifecycleCallbacks? = null

    // requested entourage group type
    private var entourageGroupType: String? = null

    // requested entourage group type
    private var entourageCategory: EntourageCategory? = null

    // current selected tab
    protected var selectedTab = MapTabItem.ALL_TAB

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BusProvider.getInstance().register(this)
        markersMap = TreeMap()
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_TOURS_FROM_MENU)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (presenter == null) {
            setupComponent(EntourageApplication.get(activity).entourageComponent)
            presenter!!.start()
        }
        if (fragmentLifecycleCallbacks == null && fragmentManager != null) {
            fragmentLifecycleCallbacks = MapFragmentLifecycleCallbacks()
            fragmentManager!!.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks!!, false)
        }
        initializeMap()
        initializeFloatingMenu()
        initializeFilterTab()
        initializeNewsfeedView()
        initializeInvitations()
        if (activity != null) {
            (activity as MainActivity?)!!.showEditActionZoneFragment()
        }
        fragment_map_empty_list_popup_close.setOnClickListener {onEmptyListPopupClose()}
    }

    protected fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerMapComponent.builder()
                .entourageComponent(entourageComponent)
                .mapModule(MapModule(this))
                .build()
                .inject(this)
    }

    override fun onStart() {
        super.onStart()
        if (!isLocationEnabled() && !isLocationPermissionGranted() && activity != null) {
            (activity as MainActivity?)!!.showEditActionZoneFragment(this)
        }
        fragment_map_tours_view?.addOnScrollListener(scrollListener)
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_FEED_FROM_TAB)
        isStopped = false
    }

    override fun onStop() {
        super.onStop()
        fragment_map_tours_view?.removeOnScrollListener(scrollListener)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isStopped = true
    }

    override fun onResume() {
        super.onResume()
        timerStart()
        val isLocationGranted = isLocationPermissionGranted()
        BusProvider.getInstance().post(OnLocationPermissionGranted(isLocationGranted))
    }

    override fun onPause() {
        super.onPause()
        timerStop()
    }

    override fun onDestroy() {
        BusProvider.getInstance().unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        if (mapLongClickView != null && mapLongClickView.visibility == View.VISIBLE) {
            mapLongClickView.visibility = View.GONE
            return true
        }
        //before closing the fragment, send the cached tour points to server (if applicable)
        if (entourageService != null) {
            entourageService!!.updateOngoingTour()
        }
        return false
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    open fun onNotificationExtras(id: Int, choice: Boolean) {
        userId = id
    }

    fun dismissAllDialogs() {
        if (fragmentLifecycleCallbacks != null) {
            fragmentLifecycleCallbacks!!.dismissAllDialogs()
        }
    }

    @JvmOverloads
    fun displayChosenFeedItem(feedItemUUID: String?, feedItemType: Int, invitationId: Long = 0) {
        //display the feed item
        if (newsfeedAdapter != null) {
            val feedItem = newsfeedAdapter!!.findCard(feedItemType, feedItemUUID) as FeedItem
            if (feedItem != null) {
                displayChosenFeedItem(feedItem, invitationId)
                return
            }
        }
        if (presenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE)
            presenter!!.openFeedItem(feedItemUUID, feedItemType, invitationId)
        }
    }

    fun displayChosenFeedItem(feedItem: FeedItem, feedRank: Int) {
        displayChosenFeedItem(feedItem, 0, feedRank)
    }

    @JvmOverloads
    fun displayChosenFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int = 0) {
        if (context == null || isStateSaved) return
        // decrease the badge count
        val application = EntourageApplication.get(context)
        application?.removePushNotificationsForFeedItem(feedItem)
        //check if we are not already displaying the tour
        if (activity != null) {
            val fragmentManager = activity!!.supportFragmentManager
            val entourageInformationFragment = fragmentManager.findFragmentByTag(EntourageInformationFragment.TAG) as EntourageInformationFragment?
            if (entourageInformationFragment != null && entourageInformationFragment.feedItemType == feedItem.type.toLong() && entourageInformationFragment.feedItemId != null && entourageInformationFragment.feedItemId.equals(feedItem.uuid, ignoreCase = true)) {
                //TODO refresh the tour info screen
                return
            }
        }
        if (presenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE)
            presenter!!.openFeedItem(feedItem, invitationId, feedRank)
        }
    }

    fun displayChosenFeedItemFromShareURL(feedItemShareURL: String?, feedItemType: Int) {
        //display the feed item
        if (presenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE)
            presenter!!.openFeedItem(feedItemShareURL, feedItemType)
        }
    }

    private fun act(timestampedObject: TimestampedObject) {
        if (entourageService != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_CONTACT)
            isRequestingToJoin++
            if (timestampedObject.type == TimestampedObject.TOUR_CARD) {
                entourageService!!.requestToJoinTour(timestampedObject as Tour)
            } else if (timestampedObject.type == TimestampedObject.ENTOURAGE_CARD) {
                entourageService!!.requestToJoinEntourage(timestampedObject as Entourage)
            } else {
                isRequestingToJoin--
            }
        } else if (fragment_map_main_layout != null) {
            make(fragment_map_main_layout!!, R.string.tour_join_request_error, Snackbar.LENGTH_SHORT).show()
        }
    }

    open fun displayEntourageDisclaimer() {
        // Hide the create entourage menu ui
        if (mapLongClickView != null) {
            mapLongClickView.visibility = View.GONE
        }

        // Check if we need to show the entourage disclaimer
        if (Configuration.showEntourageDisclaimer()) {
            if (presenter != null) {
                presenter!!.displayEntourageDisclaimer(entourageGroupType)
            }
        } else {
            if (activity != null) {
                (activity as MainActivity?)!!.onEntourageDisclaimerAccepted(null)
            }
        }
    }

    fun createEntourage() {
        var location = EntourageLocation.getInstance().lastCameraPosition.target
        if (!Entourage.TYPE_OUTING.equals(entourageGroupType, ignoreCase = true)) {
            // For demand/contribution, by default select the action zone location, if set
            val me = EntourageApplication.me(activity)
            if (me != null) {
                val address = me.address
                if (address != null) {
                    location = LatLng(address.latitude, address.longitude)
                }
            }
        }
        if (longTapCoordinates != null) {
            location = longTapCoordinates
            longTapCoordinates = null
        }
        if (presenter != null) {
            presenter!!.createEntourage(location, entourageGroupType!!, entourageCategory)
        }
    }

    protected fun refreshFeed() {
        clearAll()
        if (newsfeedAdapter != null) {
            newsfeedAdapter!!.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab)
        }
        if (entourageService != null) {
            entourageService!!.updateNewsfeed(pagination, selectedTab)
        }
    }

    // ----------------------------------
    // BUS LISTENERS : don't susbcribe here but in children !
    // ----------------------------------
    open fun onMyEntouragesForceRefresh(event: OnMyEntouragesForceRefresh) {
        val item = event.feedItem
        if (item == null) {
            refreshFeed()
        } else if (newsfeedAdapter != null) {
            newsfeedAdapter!!.updateCard(item)
        }
    }

    open fun onBetterLocation(event: OnBetterLocationEvent) {
        if (event.location != null) {
            centerMap(event.location)
        }
    }

    open fun onEntourageCreated(event: OnEntourageCreated) {
        val entourage = event.entourage ?: return

        // Force the map filtering for entourages as ON
        val mapFilter = mapFilter
        mapFilter.entourageCreated()
        if (presenter != null) {
            presenter!!.saveMapFilter()
        }

        // Update the newsfeed
        clearAll()
        if (entourageService != null) {
            entourageService!!.updateNewsfeed(pagination, selectedTab)
        }
    }

    open fun onEntourageUpdated(event: OnEntourageUpdated?) {
        if (event == null || newsfeedAdapter == null) {
            return
        }
        val entourage = event.entourage ?: return
        newsfeedAdapter!!.updateCard(entourage)
    }

    open fun onMapFilterChanged(event: OnMapFilterChanged?) {
        // Save the filter
        if (presenter != null) {
            presenter!!.saveMapFilter()
        }
        updateFilterButtonText()
        // Refresh the newsfeed
        refreshFeed()
    }

    private fun updateFilterButtonText() {
        val v = if (view != null) view!!.findViewById<View>(R.id.fragment_map_filter_button) else null
        if (v is ExtendedFloatingActionButton) {
            v.setText(if (mapFilter.isDefaultFilter()) R.string.map_no_filter else R.string.map_filters_activated)
        }
    }

    open fun onNewsfeedLoadMoreRequested(event: OnNewsfeedLoadMoreEvent?) {
        when (selectedTab) {
            MapTabItem.ALL_TAB -> {
                ensureMapVisible()
                pagination.setNextDistance()
                refreshFeed()
            }
            MapTabItem.EVENTS_TAB -> {
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

    private fun onMapTabChanged(newSelectedTab: MapTabItem) {
        if (newSelectedTab == selectedTab) {
            return
        }
        selectedTab = newSelectedTab
        EntourageEvents.logEvent(if (selectedTab == MapTabItem.ALL_TAB) EntourageEvents.EVENT_FEED_TAB_ALL else EntourageEvents.EVENT_FEED_TAB_EVENTS)
        if (activity != null) {
            val filterButton = activity!!.findViewById<View>(R.id.fragment_map_filter_button)
            if (filterButton != null) {
                filterButton.visibility = if (selectedTab == MapTabItem.ALL_TAB) View.VISIBLE else View.GONE
            }
        }
        clearAll()
        if (newsfeedAdapter != null) {
            newsfeedAdapter!!.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab)
        }
        if (entourageService != null) {
            entourageService!!.updateNewsfeed(pagination, selectedTab)
        }
    }

    open fun userActRequested(event: OnUserActEvent) {
        if (OnUserActEvent.ACT_JOIN == event.act) {
            act(event.feedItem)
        } else if (OnUserActEvent.ACT_QUIT == event.act) {
            val me = EntourageApplication.me(context)
            if (me == null) {
                Toast.makeText(context, R.string.tour_info_quit_tour_error, Toast.LENGTH_SHORT).show()
            } else {
                val item = event.feedItem
                if (item != null && FeedItem.JOIN_STATUS_PENDING == item.joinStatus) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_CANCEL_JOIN_REQUEST)
                }
                if (entourageService != null) {
                    entourageService!!.removeUserFromFeedItem(item, userId)
                }
            }
        }
    }

    open fun feedItemViewRequested(event: OnFeedItemInfoViewRequestedEvent?) {
        if (event != null) {
            val feedItem = event.feedItem
            if (feedItem != null) {
                displayChosenFeedItem(feedItem, event.getfeedRank())
                // update the newsfeed card
                onPushNotificationConsumedForFeedItem(feedItem)
                // update the my entourages card, if necessary
            } else {
                //check if we are receiving feed type and id
                val feedItemType = event.feedItemType
                if (feedItemType == 0) {
                    return
                }
                val feedItemUUID = event.feedItemUUID
                if (feedItemUUID == null || feedItemUUID.length == 0) {
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
        if (entourageService!!.isRunning) {
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
        var newsFeeds = newsFeeds
        if (newsfeedAdapter == null || !isAdded) {
            pagination.isLoading = false
            pagination.isRefreshing = false
            return
        }
        val previousItemCount = newsfeedAdapter!!.dataItemCount
        newsFeeds = removeRedundantNewsfeed(newsFeeds, false)!!
        //add or update the received newsfeed
        for (newsfeed in newsFeeds) {
            val newsfeedData = newsfeed.data
            if (newsfeedData is TimestampedObject) {
                addNewsfeedCard(newsfeedData)
            }
        }
        updatePagination(newsFeeds)
        redrawWholeNewsfeed(newsFeeds)

        // update the bottom view, if not refreshing
        if (!pagination.isRefreshing) {
            showNewsfeedBottomView(if (selectedTab == MapTabItem.ALL_TAB) newsFeeds.size < pagination.itemsPerPage else newsfeedAdapter!!.dataItemCount == 0)
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
                fragment_map_tours_view?.scrollToPosition(0)
            }
        }
        pagination.isLoading = false
        pagination.isRefreshing = false
    }

    protected open fun redrawWholeNewsfeed(newsFeeds: List<Newsfeed>) {
        if (map != null && newsFeeds.size > 0 && newsfeedAdapter != null) {
            //redraw the whole newsfeed
            for (timestampedObject in newsfeedAdapter!!.items) {
                if (timestampedObject.type == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage(timestampedObject as Entourage)
                }
            }
            mapClusterManager.cluster()
        }
    }

    private fun checkPermission() {
        if (activity == null) {
            return
        }
        if (isLocationPermissionGranted()) {
            BusProvider.getInstance().post(OnLocationPermissionGranted(true))
            return
        }

        // Check if the user allowed geolocation from screen 04.2 (login funnel)
        val geolocationAllowedByUser = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_GEOLOCATION_ENABLED, true)
        if (!geolocationAllowedByUser) {
            BusProvider.getInstance().post(OnLocationPermissionGranted(false))
            return
        }
        if (shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
            AlertDialog.Builder(activity!!)
                    .setTitle(R.string.map_permission_title)
                    .setMessage(R.string.map_permission_description)
                    .setPositiveButton(getString(R.string.activate)) { dialogInterface: DialogInterface?, i: Int -> requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION) }
                    .setNegativeButton(R.string.map_permission_refuse) { dialog: DialogInterface?, i: Int ->
                        val noLocationPermissionFragment = NoLocationPermissionFragment()
                        noLocationPermissionFragment.show(activity!!.supportFragmentManager, NoLocationPermissionFragment.TAG)
                        BusProvider.getInstance().post(OnLocationPermissionGranted(false))
                    }
                    .show()
        } else {
            requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
        }
    }

    @OnClick(R.id.fragment_map_gps)
    fun displayGeolocationPreferences() {
        displayGeolocationPreferences(false)
    }

    @OnClick(R.id.fragment_map_display_toggle)
    fun onDisplayToggle() {
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
        displayEntourageDisclaimer()
    }

    @OnClick(R.id.map_longclick_button_entourage_action)
    fun onCreateEntourageHelpAction() {
        createAction(EntourageCategoryManager.getInstance().getDefaultCategory(Entourage.TYPE_DEMAND), Entourage.TYPE_ACTION)
    }

    @OnClick(R.id.fragment_map_filter_button)
    fun onShowFilter() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_FILTERSCLICK)
        val me = EntourageApplication.me(activity)
        val isPro = me != null && me.isPro
        val mapFilterFragment: MapFilterFragment = MapFilterFragment.newInstance(isPro)
        mapFilterFragment.show(parentFragmentManager, MapFilterFragment.TAG)
    }

    @OnClick(R.id.fragment_map_new_entourages_button)
    fun onNewEntouragesReceivedButton() {
        fragment_map_tours_view?.scrollToPosition(0)
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
    protected open fun displayFeedItemOptions(feedItem: FeedItem?) {}
    open fun feedItemCloseRequested(event: OnFeedItemCloseRequestEvent) {
        val feedItem = event.feedItem ?: return
        if (event.isShowUI) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTIVE_CLOSE_OVERLAY)
            displayFeedItemOptions(feedItem)
            return
        }
        // Only the author can close entourages/tours
        val me = EntourageApplication.me(context)
        if (me == null || feedItem.author == null) {
            return
        }
        val myId = me.id
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
        mapLongClickButtonsView.requestLayout()
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
            EntourageLocation.getInstance().saveLastCameraPosition(map.cameraPosition)
        }
    }

    override fun getRenderer(): DefaultClusterRenderer<*> {
        return MapClusterItemRenderer(activity!!, map, mapClusterManager)
    }

    protected fun onMapReady(googleMap: GoogleMap?) {
        super.onMapReady(googleMap,
                presenter!!.onClickListener,
                presenter!!.onGroundOverlayClickListener
        )
        map.setOnCameraIdleListener {
            val cameraPosition = map.cameraPosition
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
                    entourageService!!.cancelNewsFeedUpdate()
                }
                if (newsfeedAdapter != null) {
                    newsfeedAdapter!!.removeAll()
                    newsfeedAdapter!!.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab)
                }
                pagination = NewsfeedPagination()
                entourageService!!.updateNewsfeed(pagination, selectedTab)
                updateUserHistory()
            }
            if (isFollowing && currentLocation != null) {
                if (currentLocation.distanceTo(newLocation) > 1) {
                    isFollowing = false
                }
            }
            hideEmptyListPopup()
        }
        map.setOnMapClickListener { latLng: LatLng? ->
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
            fragment_map_tours_view?.layoutManager = LinearLayoutManager(context)
            newsfeedAdapter = NewsfeedAdapter()
            newsfeedAdapter!!.setOnMapReadyCallback(onMapReadyCallback)
            newsfeedAdapter!!.setOnFollowButtonClickListener { v: View? -> onFollowGeolocation() }
            fragment_map_tours_view?.adapter = newsfeedAdapter
        }
    }

    private fun initializeFilterTab() {
        val tabLayout: TabLayout = view!!.findViewById(R.id.fragment_map_top_tab) ?: return
        if (onTabSelectedListener == null) {
            onTabSelectedListener = object : OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    onMapTabChanged(if (tab.position == 1) MapTabItem.EVENTS_TAB else MapTabItem.ALL_TAB)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            }
        }
        tabLayout.addOnTabSelectedListener(onTabSelectedListener)
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------
    protected open fun hideTourLauncher() {}
    fun pauseTour(tour: Tour) {
        if (entourageService != null && entourageService!!.isRunning) {
            if (entourageService!!.currentTourId.equals(tour.uuid, ignoreCase = true)) {
                entourageService!!.pauseTreatment()
            }
        }
    }

    fun saveOngoingTour() {
        if (entourageService != null) {
            entourageService!!.updateOngoingTour()
        }
    }

    fun stopFeedItem(feedItem: FeedItem?, success: Boolean) {
        if (activity != null) {
            if (entourageService != null) {
                if (feedItem != null
                        && (!entourageService!!.isRunning
                                || feedItem.type != TimestampedObject.TOUR_CARD || entourageService!!.currentTourId.equals(feedItem.uuid, ignoreCase = true))) {
                    // Not ongoing tour, just stop the feed item
                    loaderStop = ProgressDialog.show(activity, activity!!.getString(feedItem.closingLoaderMessage), activity!!.getString(R.string.button_loading), true)
                    loaderStop.setCancelable(true)
                    EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR)
                    entourageService!!.stopFeedItem(feedItem, success)
                } else if (entourageService!!.isRunning) {
                    loaderStop = ProgressDialog.show(activity, activity!!.getString(R.string.loader_title_tour_finish), activity!!.getString(R.string.button_loading), true)
                    loaderStop.setCancelable(true)
                    entourageService!!.endTreatment()
                    EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR)
                }
            }
        }
    }

    fun freezeTour(tour: Tour?) {
        if (activity != null) {
            if (entourageService != null) {
                entourageService!!.freezeTour(tour)
            }
        }
    }

    open fun userStatusChanged(content: PushNotificationContent, status: String?) {
        if (entourageService != null) {
            var timestampedObject: TimestampedObject? = null
            if (content.isEntourageRelated && newsfeedAdapter != null) {
                timestampedObject = newsfeedAdapter!!.findCard(TimestampedObject.ENTOURAGE_CARD, content.joinableId)
            }
            if (timestampedObject != null) {
                val user = TourUser()
                user.userId = userId
                user.status = status
                entourageService!!.notifyListenersUserStatusChanged(user, timestampedObject as FeedItem?)
            }
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (views)
    // ----------------------------------
    protected open fun removeRedundantNewsfeed(newsFeedList: List<Newsfeed>?, isHistory: Boolean): List<Newsfeed>? {
        if (newsFeedList == null || newsfeedAdapter == null) {
            return null
        }
        val iteratorNewsfeed: Iterator<*> = newsFeedList.iterator()
        while (iteratorNewsfeed.hasNext()) {
            val newsfeed = iteratorNewsfeed.next() as Newsfeed? ?: continue
            if (!isHistory) {
                val card = newsfeed.data
                if (card !is TimestampedObject) {
                    iteratorNewsfeed.remove()
                    continue
                }
                var retrievedCard: TimestampedObject?
                retrievedCard = newsfeedAdapter!!.findCard(card)
                if (retrievedCard != null) {
                    if (Entourage.NEWSFEED_TYPE == newsfeed.type) {
                        if ((retrievedCard as Entourage).isSame(card as Entourage)) {
                            iteratorNewsfeed.remove()
                        }
                    } else if (Announcement.NEWSFEED_TYPE == newsfeed.type) {
                        iteratorNewsfeed.remove()
                    }
                }
            }
        }
        return newsFeedList
    }

    protected fun drawNearbyEntourage(feedItem: FeedItem?) {
        if (map == null || markersMap == null ) return
        if (feedItem?.startPoint == null) return
        if (markersMap!![feedItem.hashString()] == null) {
            if (feedItem.showHeatmapAsOverlay()) {
                val position = feedItem.startPoint.location
                val heatmapIcon = BitmapDescriptorFactory.fromResource(feedItem.heatmapResourceId)
                val groundOverlayOptions = GroundOverlayOptions()
                        .image(heatmapIcon)
                        .position(position, Entourage.HEATMAP_SIZE, Entourage.HEATMAP_SIZE)
                        .clickable(true)
                        .anchor(0.5f, 0.5f)
                markersMap!![feedItem.hashString()] = map.addGroundOverlay(groundOverlayOptions)
                presenter?.onGroundOverlayClickListener?.addEntourageGroundOverlay(position, feedItem)
            } else {
                val mapClusterItem = MapClusterItem(feedItem)
                markersMap!![feedItem.hashString()] = mapClusterItem
                mapClusterManager.addItem(mapClusterItem)
            }
        }
    }

    private fun addNewsfeedCard(card: TimestampedObject) {
        if (newsfeedAdapter == null) {
            return
        }
        if (newsfeedAdapter!!.findCard(card) != null) {
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
        markersMap?.clear()
        presenter?.onClickListener?.clear()
        presenter?.onGroundOverlayClickListener?.clear()
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
        if (newsfeedAdapter == null || fragment_map_main_layout==null) {
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
        val targetHeight = fragment_map_main_layout.measuredHeight
        newsfeedAdapter!!.setMapHeight(targetHeight)
        val anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight)
        anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
        anim.start()
    }

    protected fun displayListWithMapHeader() {
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
        fragment_map_tours_view?.scrollToPosition(0)
    }

    private fun updatePagination(newsfeedList: List<Newsfeed>?) {
        if (newsfeedList == null || newsfeedList.isEmpty()) {
            pagination.loadedItems(null, null)
            return
        }
        when (selectedTab) {
            MapTabItem.ALL_TAB -> {
                var newestUpdatedDate: Date? = null
                var oldestUpdateDate: Date? = null
                for (newsfeed in newsfeedList) {
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
            MapTabItem.EVENTS_TAB -> if (newsfeedAdapter != null) {
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

    fun onPushNotificationConsumedForFeedItem(feedItem: FeedItem) {
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
                        if (selectedTab == MapTabItem.ALL_TAB) {
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
            presenter?.getMyPendingInvitations()
        }
    }

    fun onInvitationsReceived(invitationList: List<Invitation>?) {
        // Check for errors and empty list
        if (invitationList == null || invitationList.isEmpty()) {
            // Check if we need to show the carousel
            if (EntourageApplication.me(activity)?.isOnboardingUser == true) {
                showCarousel()
                // Reset the onboarding flag
                presenter?.resetUserOnboardingFlag()
            }
        } else {
            for (invitation in invitationList) {
                presenter?.acceptInvitation(invitation.id)
            }
            // Show the first invitation
            val firstInvitation = invitationList[0]
            presenter?.openFeedItem(firstInvitation.entourageUUID, FeedItem.ENTOURAGE_CARD, firstInvitation.id)
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------
    fun onEmptyListPopupClose() {
        presenter?.isShowNoEntouragesPopup = false
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
        if (presenter?.isShowNoEntouragesPopup ==true) {
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
            if (fragmentLifecycleCallbacks == null) return@postDelayed
            val topFragment = fragmentLifecycleCallbacks!!.topFragment ?: return@postDelayed
            (activity as MainActivity?)!!.showTutorial()
        }, Constants.CAROUSEL_DELAY_MILLIS)
    }

    // ----------------------------------
    // Newsfeed Bottom View Handling
    // ----------------------------------
    private fun showNewsfeedBottomView(show: Boolean) {
        if (newsfeedAdapter == null) return
        if (pagination.isNextDistanceAvailable) {
            // we can increase the distance
            newsfeedAdapter!!.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab)
        } else {
            if (newsfeedAdapter!!.dataItemCount == 0) {
                // max distance and still no items, show no items info
                newsfeedAdapter!!.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab)
            } else {
                // max distance and items, show no more items info
                newsfeedAdapter!!.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_MORE_ITEMS, selectedTab)
            }
        }
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

    protected fun showHeatzoneMiniCardsAtLocation(location: LatLng?) {
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
        val `val` = valueAnimator.animatedValue as Int
        newsfeedAdapter!!.setMapHeight(`val`)
        fragment_map_tours_view?.layoutManager!!.requestLayout()
    }

    override fun getAdapter(): HeaderBaseAdapter {
        return newsfeedAdapter!!
    }

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
        private const val HEATZONE_SEARCH_RADIUS = Entourage.HEATMAP_SIZE.toInt() / 2 // meters

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