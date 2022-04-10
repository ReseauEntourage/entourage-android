package social.entourage.android.base.newsfeed

import android.Manifest.permission
import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import social.entourage.android.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.feed.*
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUtils.isLocationEnabled
import social.entourage.android.base.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.base.map.*
import social.entourage.android.base.map.filter.MapFilterFactory
import social.entourage.android.base.map.filter.MapFilterFragment
import social.entourage.android.base.map.permissions.NoLocationPermissionFragment
import social.entourage.android.configuration.Configuration
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import timber.log.Timber
import java.util.*

abstract class NewsfeedFragment : BaseMapFragment(R.layout.fragment_map), NewsFeedListener, UserEditActionZoneFragment.FragmentListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    protected var presenter: NewsfeedPresenter = NewsfeedPresenter(this)
    private var onMapReadyCallback: OnMapReadyCallback? = null
    protected var longTapCoordinates: LatLng? = null
    private var previousEmptyListPopupLocation: Location? = null
    protected var entService: EntService? = null
    protected var loaderStop: ProgressDialog? = null
    protected var markersMap: MutableMap<String, Any?> =  TreeMap()
    private var initialNewsfeedLoaded = false
    protected var isRequestingToJoin = 0
    private var isStopped = false

    protected var newsfeedAdapter: NewsfeedAdapter? = null

    //pagination
    protected var pagination = NewsfeedPagination()
    private val scrollListener = OnScrollListener()

    // keeps tracks of the attached fragments
    private var fragmentLifecycleCallbacks: NewsfeedFragmentLifecycleCallbacks? = null

    // requested group type
    private lateinit var groupType: String

    // requested entourage category
    private var entourageCategory: EntourageCategory? = null

    // current selected tab
    protected var selectedTab = NewsfeedTabItem.ALL_TAB

    protected var mapClusterManager: ClusterManager<ClusterItem>? = null

    protected val userId: Int
        get() = presenter.authenticationController.me?.id ?:0
    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EntBus.register(this)
        markersMap.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupType = BaseEntourage.GROUPTYPE_ACTION
        presenter.start()
        if (fragmentLifecycleCallbacks == null) {
            NewsfeedFragmentLifecycleCallbacks().let {
                fragmentLifecycleCallbacks = it
                activity?.supportFragmentManager?.registerFragmentLifecycleCallbacks(it, false)
            }
        }
        initializeMap()
        initializeFloatingMenu()
        initializeNewsfeedView()
        initializeInvitations()
        (activity as? MainActivity)?.showEditActionZoneFragment()
        fragment_map_empty_list_popup_close?.setOnClickListener {onEmptyListPopupClose()}
        fragment_map_display_toggle?.setOnClickListener {onDisplayToggle()}
        map_longclick_button_entourage_action?.setOnClickListener {onCreateEntourageHelpAction()}
        fragment_map_filter_button?.setOnClickListener {onShowFilter()}
        fragment_map_new_entourages_button?.setOnClickListener {onNewEntouragesReceivedButton()}
        fragment_map_gps?.setOnClickListener {displayGeolocationPreferences()}

        presenter.checkUserNamesInfos()
    }

    override fun onStart() {
        super.onStart()
        if (!isLocationEnabled() && !isLocationPermissionGranted()) {
            (activity as? MainActivity)?.showEditActionZoneFragment(this,false)
        }
        fragment_map_feeditems_view?.addOnScrollListener(scrollListener)
//        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_FEED_FROM_TAB)
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
        EntBus.post(OnLocationPermissionGranted(isLocationPermissionGranted()))
    }

    override fun onDestroy() {
        EntBus.unregister(this)
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        if (fragment_map_longclick?.visibility == View.VISIBLE) {
            fragment_map_longclick?.visibility = View.GONE
            return true
        }
        return false
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun displayChosenFeedItem(feedItemUUID: String, feedItemType: Int, invitationId: Long = 0) {
        //display the feed item
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        (newsfeedAdapter?.findCard(feedItemType, feedItemUUID) as? FeedItem)?.let { feedItem ->
            displayChosenFeedItem(feedItem, invitationId)
        } ?: run {
            presenter.openFeedItemFromUUID(feedItemUUID, feedItemType, invitationId)
        }
    }

    fun displayChosenFeedItem(feedItem: FeedItem, feedRank: Int) {
        displayChosenFeedItem(feedItem, 0, feedRank)
    }

    private fun displayChosenFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int = 0) {
        if (context == null || isStateSaved) return
        // decrease the badge count
        EntourageApplication.get(context).removePushNotificationsForFeedItem(feedItem)
        //check if we are not already displaying the item
        (activity?.supportFragmentManager?.findFragmentByTag(FeedItemInformationFragment.TAG) as? FeedItemInformationFragment)?.let {
            if (it.getItemType() == feedItem.type && it.feedItemId != null && it.feedItemId.equals(feedItem.uuid, ignoreCase = true)) {
                //TODO refresh the entourage info screen
                return
            }
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItem(feedItem, invitationId, feedRank)
    }

    private fun act(timestampedObject: TimestampedObject) {
        if (entService != null) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_CONTACT)
            isRequestingToJoin++
            when (timestampedObject.type) {
                TimestampedObject.ENTOURAGE_CARD -> {
                    entService?.requestToJoinEntourage(timestampedObject as BaseEntourage)
                }
                else -> {
                    isRequestingToJoin--
                }
            }
        } else if (fragment_map_main_layout != null) {
            fragment_map_main_layout?.let {EntSnackbar.make(it, R.string.entourage_join_request_error, Snackbar.LENGTH_SHORT).show()}
        }
    }

    open fun displayEntourageDisclaimer() {
        // Hide the create entourage menu ui
        fragment_map_longclick?.visibility = View.GONE

        // Check if we need to show the entourage disclaimer
        if (Configuration.showEntourageDisclaimer()) {
            presenter.displayEntourageDisclaimer(groupType)
        } else {
            (activity as? MainActivity)?.onEntourageDisclaimerAccepted(null)
        }
    }

    fun createEntourage() {
        var location = EntLocation.lastCameraPosition.target
        if (!BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
            // For demand/contribution, by default select the action zone location, if set
            EntourageApplication.me(activity)?.address?.let { address ->
                location = LatLng(address.latitude, address.longitude)
            }
        }
        longTapCoordinates?.let {
            location = it
            longTapCoordinates = null
        }
        presenter.createEntourage(location, groupType, entourageCategory)
    }

    protected fun refreshFeed() {
        clearAll()
        newsfeedAdapter?.showBottomView(false,
            NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab)
        entService?.updateNewsfeed(pagination, selectedTab)
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
        centerMap(event.location)
    }

    open fun onEntourageCreated(event: OnEntourageCreated) {
       // Force the map filtering for entourages as ON
        MapFilterFactory.mapFilter.entourageCreated()
        presenter.saveMapFilter()

        // Update the newsfeed
        clearAll()
        entService?.updateNewsfeed(pagination, selectedTab)
    }

    open fun onEntourageUpdated(event: OnEntourageUpdated) {
        newsfeedAdapter?.updateCard(event.entourage)
    }

    open fun onMapFilterChanged(event: OnMapFilterChanged) {
        // Save the filter
        presenter.saveMapFilter()
        updateFilterButtonText()
        // Refresh the newsfeed
        refreshFeed()
    }

    open fun updateFilterButtonText() {
        val activefilters = (MapFilterFactory.mapFilter.isDefaultFilter() && selectedTab== NewsfeedTabItem.ALL_TAB)
        fragment_map_filter_button?.setText(if (activefilters) R.string.map_no_filter else R.string.map_filters_activated)
    }

    open fun onNewsfeedLoadMoreRequested(event: OnNewsfeedLoadMoreEvent) {
        when (selectedTab) {
            NewsfeedTabItem.ALL_TAB -> {
                ensureMapVisible()
                pagination.setNextDistance()
                refreshFeed()
            }
            NewsfeedTabItem.EVENTS_TAB -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_events_url)))
                try {
                    startActivity(browserIntent)
                } catch (ex: ActivityNotFoundException) {
                    fragment_map_main_layout?.let {EntSnackbar.make(it, R.string.no_browser_error, Snackbar.LENGTH_SHORT).show()}
                }
            }
        }
    }

    open fun userActRequested(event: OnUserActEvent) {
        if (OnUserActEvent.ACT_JOIN == event.act) {
            act(event.feedItem)
        } else if (OnUserActEvent.ACT_QUIT == event.act) {
            if (EntourageApplication.me(context) != null) {
                val item = event.feedItem
                if (FeedItem.JOIN_STATUS_PENDING == item.joinStatus) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_CANCEL_JOIN_REQUEST)
                }
                presenter.authenticationController.me?.id?.let { entService?.removeUserFromFeedItem(item, it)}
            }
        }
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------
    override fun onNetworkException() {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onServerException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.server_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onNewsFeedReceived(newsFeeds: List<NewsfeedItem>) {
        if (newsfeedAdapter == null || !isAdded) {
            pagination.isLoading = false
            pagination.isRefreshing = false
            return
        }
        val previousItemCount = newsfeedAdapter?.dataItemCount ?: 0
        val newNewsFeeds = removeRedundantNewsfeed(newsFeeds)
        //add or update the received newsfeed
        for (newsfeed in newNewsFeeds) {
            (newsfeed.data as? TimestampedObject)?.let { addNewsfeedCard(it) }
        }
        updatePagination(newNewsFeeds)
        redrawWholeNewsfeed(newNewsFeeds)

        // update the bottom view, if not refreshing
        if (!pagination.isRefreshing) {
            showNewsfeedBottomView(if (selectedTab == NewsfeedTabItem.ALL_TAB) newNewsFeeds.size < pagination.itemsPerPage else newsfeedAdapter?.dataItemCount == 0)
        }
        if (newsfeedAdapter?.dataItemCount == 0) {

            if (!pagination.isRefreshing) {
                isFullMapShown = false
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

    protected open fun redrawWholeNewsfeed(newsFeeds: List<NewsfeedItem>) {
        //TODO do we need newsFeeds variable here ?
        if (newsFeeds.isNotEmpty()) {
            //redraw the whole newsfeed
            newsfeedAdapter?.items?.forEach { timestampedObject ->
                if (timestampedObject.type == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage(timestampedObject as BaseEntourage)
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
            EntBus.post(OnLocationPermissionGranted(true))
            return
        }

        // Check if the user allowed geolocation from screen 04.2 (login funnel)
        val geolocationAllowedByUser = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_GEOLOCATION_ENABLED, true)
        if (!geolocationAllowedByUser) {
            EntBus.post(OnLocationPermissionGranted(false))
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
                        EntBus.post(OnLocationPermissionGranted(false))
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
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWMAP)
        } else {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWLIST)
        }
        toggleActionsList()
    }

    fun createAction(newGroupType: String, newActionGroupType: String) {
        entourageCategory = EntourageCategoryManager.getDefaultCategory(newActionGroupType)
        groupType = newGroupType
        entourageCategory?.isNewlyCreated = true
        displayEntourageDisclaimer()
    }

    fun createAction(newEntourageGroupType: String) {
        entourageCategory = null
        groupType = newEntourageGroupType
        displayEntourageDisclaimer()
    }

    fun setGroupType(_groupString:String) {
        groupType = _groupString
    }

    private fun onCreateEntourageHelpAction() {
        createAction(BaseEntourage.GROUPTYPE_ACTION_DEMAND, BaseEntourage.GROUPTYPE_ACTION_DEMAND)
    }

    open fun onShowFilter() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWFILTERS)
        MapFilterFragment().show(parentFragmentManager, MapFilterFragment.TAG)
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
        val feedItem = event.feedItem
        if (event.isShowUI) {
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_ACTIVE_CLOSE_OVERLAY)
            displayFeedItemOptions(feedItem)
            return
        }
        // Only the author can close actions
        val myId = EntourageApplication.me(context)?.id
                ?: return
        val author = feedItem.author ?: return
        if (author.userID != myId) {
            return
        }
        if (!feedItem.isClosed()) {
            // close
            stopFeedItem(feedItem, event.isSuccess,event.comment)
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
            onMapReadyCallback = OnMapReadyCallback { googleMap: GoogleMap -> this.onMapReady(googleMap) }
        }
    }

    public override fun saveCameraPosition() {
        map?.cameraPosition?.let { EntLocation.lastCameraPosition = it}
    }


    private fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap,
                presenter.onGroundOverlayClickListener
        )

        if (activity == null) {
            Timber.e("No activity found")
            return
        }
        mapClusterManager = ClusterManager<ClusterItem>(activity, googleMap)
                .apply {
            this.renderer = MapClusterItemRenderer(requireActivity(), googleMap, this)
            this.setOnClusterItemClickListener(presenter.onClickListener)
            googleMap.setOnMarkerClickListener(this)
        }
        initializeMapZoom()
        map?.setOnCameraIdleListener {
            val cameraPosition = map?.cameraPosition ?: return@setOnCameraIdleListener
            EntLocation.currentCameraPosition = cameraPosition
            val currentLocation = EntLocation.currentLocation
            val newLocation = EntLocation.cameraPositionToLocation(null, cameraPosition)
            val newZoom = cameraPosition.zoom
            if (entService != null && (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT)) {
                if (previousCameraZoom != newZoom) {
                    if (previousCameraZoom > newZoom) {
                        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_ZOOM_IN)
                    } else {
                        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_MAP_ZOOM_OUT)
                    }
                }
                previousCameraZoom = newZoom
                previousCameraLocation = newLocation

                // check if we need to cancel the current request
                if (pagination.isLoading) {
                    entService?.cancelNewsFeedUpdate()
                }
                newsfeedAdapter?.removeAll()
                newsfeedAdapter?.showBottomView(false,
                    NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab)
                pagination = NewsfeedPagination()
                entService?.updateNewsfeed(pagination, selectedTab)
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
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_MAPCLICK)
                if (isFullMapShown) {
                    // Hide the minicards if visible
                    if (fragment_map_entourage_mini_cards?.visibility == View.VISIBLE) {
                        fragment_map_entourage_mini_cards?.visibility = View.INVISIBLE
                    }
                } else {
                    toggleActionsList()
                }
            }
        }
    }

    protected open fun updateUserHistory() {}
    private fun initializeNewsfeedView() {
        if (newsfeedAdapter == null) {
            fragment_map_feeditems_view?.layoutManager = LinearLayoutManager(context)
            newsfeedAdapter = NewsfeedAdapter().apply {
                onMapReadyCallback?.let { this.setOnMapReadyCallback(it) }
                this.setOnFollowButtonClickListener { onFollowGeolocation() }
                fragment_map_feeditems_view?.adapter = this
            }
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (action events)
    // ----------------------------------
    fun stopFeedItem(feedItem: FeedItem?, success: Boolean, comment:String?) {
        activity?.let { activity ->
            entService?.let { service ->
                if (feedItem != null) {
                    loaderStop = ProgressDialog.show(activity, activity.getString(feedItem.getClosingLoaderMessage()), activity.getString(R.string.button_loading), true)
                    loaderStop?.setCancelable(true)
                    service.stopFeedItem(feedItem, success,comment)
                }
            }
        }
    }

    open fun onJoinRequestAccepted(content: PushNotificationContent) {
        userStatusChanged(content, FeedItem.JOIN_STATUS_ACCEPTED)
    }

    open fun onAddPushNotification(message: Message) {
        onPushNotificationReceived(message)
    }

    open fun userStatusChanged(content: PushNotificationContent, status: String) {
        entService?.let { service->
            if (content.isEntourageRelated) {
                (newsfeedAdapter?.findCard(TimestampedObject.ENTOURAGE_CARD, content.joinableId) as? FeedItem)?.let { feedItem ->
                    val user = EntourageUser()
                    user.userId = userId
                    user.status = status
                    service.notifyListenersUserStatusChanged(user, feedItem)
                }
            }
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
            fragment_map_gps?.text = if (isLocationEnabled()) getString(R.string.map_gps_no_permission) else getString(R.string.map_gps_unavailable)
            fragment_map_gps?.visibility = if (visibility) View.VISIBLE else View.GONE
            if(!visibility && isFullMapShown) {
                animFullMap()
            }
            adapter?.displayGeolocStatusIcon(!visibility)
        }
        super.updateGeolocBanner(active)
    }

    protected open fun removeRedundantNewsfeed(currentFeedList: List<NewsfeedItem>): List<NewsfeedItem> {
        val newsFeedList = ArrayList<NewsfeedItem>()
        for(newsfeedItem: NewsfeedItem in currentFeedList) {
            (newsfeedItem.data as? TimestampedObject)?.let { card ->
                (newsfeedAdapter?.findCard(card) as? BaseEntourage)?.let { retrievedCard->
                    if(!((card is BaseEntourage) && ((retrievedCard as? BaseEntourage)?.isSame(card)== true))) {
                        newsFeedList.add(newsfeedItem)
                    }
                } ?: run {
                    newsFeedList.add(newsfeedItem)
                }
            }
        }
        return newsFeedList
    }

    protected fun drawNearbyEntourage(feedItem: BaseEntourage) {
        if (markersMap[feedItem.hashString()] == null) {
            if (feedItem.showHeatmapAsOverlay()) {
                val position = feedItem.getStartPoint()?.location ?:return
                map?.let {map->
                    val heatmapIcon = BitmapDescriptorFactory.fromResource(feedItem.getHeatmapResourceId())
                    val groundOverlayOptions = GroundOverlayOptions()
                            .image(heatmapIcon)
                            .position(position, BaseEntourage.HEATMAP_SIZE, BaseEntourage.HEATMAP_SIZE)
                            .clickable(true)
                            .anchor(0.5f, 0.5f)
                    map.addGroundOverlay(groundOverlayOptions)?.let {
                        markersMap[feedItem.hashString()] = it
                        presenter.onGroundOverlayClickListener?.addEntourageGroundOverlay(position, feedItem)
                    }
                }
            } else {
                val mapClusterItem = MapClusterEntourageItem(feedItem)
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
                EntourageApplication.get(context).updateBadgeCountForFeedItem(card)
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
        presenter.onGroundOverlayClickListener?.clear()
        resetFeed()
    }

    private fun resetFeed() {
        newsfeedAdapter?.removeAll()

        // check if we need to cancel the current request
        if (pagination.isLoading) {
            entService?.cancelNewsFeedUpdate()
        }
        pagination.reset()
    }

    fun displayFullMap() {
        if (newsfeedAdapter == null || fragment_map_main_layout == null) {
            return
        }
        // show the empty list popup if necessary
        if (newsfeedAdapter?.dataItemCount == 0) {
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

    private fun toggleActionsList() {
        if (!isFullMapShown) {
            displayFullMap()
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SCREEN_06_2)
        } else {
            displayListWithMapHeader()
            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_SCREEN_06_1)
        }
    }

    private val isActionsListVisible: Boolean
        get() = !isFullMapShown

    private fun ensureMapVisible() {
        fragment_map_feeditems_view?.scrollToPosition(0)
    }

    private fun updatePagination(newsfeedItemList: List<NewsfeedItem>?) {
        if (newsfeedItemList.isNullOrEmpty()) {
            pagination.loadedItems()
            return
        }
        when (selectedTab) {
            NewsfeedTabItem.ALL_TAB -> {
                var newestUpdatedDate: Date? = null
                var oldestUpdateDate: Date? = null
                for (newsfeed in newsfeedItemList) {
                    (newsfeed.data as? FeedItem)?.updatedTime?.also { feedUpdatedDate ->
                        if (newestUpdatedDate == null || newestUpdatedDate?.before(feedUpdatedDate) == true) {
                            newestUpdatedDate = feedUpdatedDate
                        }
                        if (oldestUpdateDate == null || oldestUpdateDate?.after(feedUpdatedDate) == true) {
                            oldestUpdateDate = feedUpdatedDate
                        }
                    }
                }
                newestUpdatedDate?.let {
                    pagination.loadedItems(it, oldestUpdateDate ?: it)
                } ?: run {
                    pagination.loadedItems()
                }
            }
            NewsfeedTabItem.EVENTS_TAB -> newsfeedAdapter?.let { adapter->
                var position = adapter.itemCount
                while (position >= 0) {
                    val card = adapter.getCardAt(position)
                    if (card is FeedItem) {
                        pagination.lastFeedItemUUID = card.uuid
                        break
                    }
                    position--
                }
            }
            NewsfeedTabItem.ANNOUNCEMENTS -> TODO()
        }
    }

    // ----------------------------------
    // Push handling
    // ----------------------------------
    private fun onPushNotificationReceived(message: Message) {
        //refresh the newsfeed
        if (entService != null) {
            pagination.isRefreshing = true
            entService?.updateNewsfeed(pagination, selectedTab)
        }
        //update the badge count on entourage card
        val content = message.content ?: return
        if (newsfeedAdapter == null) {
            return
        }
        val joinableId = content.joinableId
        val isChatMessage = PushNotificationContent.TYPE_NEW_CHAT_MESSAGE == content.type
        if (content.isEntourageRelated) {
            val entourage = newsfeedAdapter?.findCard(TimestampedObject.ENTOURAGE_CARD, joinableId) as? BaseEntourage
                    ?: return
            entourage.increaseBadgeCount(isChatMessage)
            newsfeedAdapter?.updateCard(entourage)
        }
    }

    // ----------------------------------
    // INVITATIONS
    // ----------------------------------
    private fun initializeInvitations() {
        // Check if it's a valid user and onboarding
        if (presenter.isOnboardingUser) {
            // Retrieve the list of invitations and then accept them automatically
            presenter.getMyPendingInvitations()
            presenter.resetUserOnboardingFlag()
        }
    }

    fun onNoInvitationReceived() {
    }

    fun onInvitationsReceived(invitationList: List<Invitation>) {
        //during onboarding we check if the new user was invited to specific entourages and then automatically accept them
        if (presenter.isOnboardingUser && !invitationList.isNullOrEmpty()) {
            invitationList.forEach {
                presenter.acceptInvitation(it.id)
            }
            // Show the first invitation
            invitationList.first().let {
                presenter.openFeedItemFromUUID(it.entourageUUID, TimestampedObject.ENTOURAGE_CARD, it.id)
            }
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
        previousEmptyListPopupLocation?.let {
            // Show the popup only we moved from the last position we show it
            val currentLocation = EntLocation.cameraPositionToLocation(null, EntLocation.currentCameraPosition)
            if (it.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                return
            }
            previousEmptyListPopupLocation = currentLocation
        } ?: run {
            previousEmptyListPopupLocation = EntLocation.currentLocation
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
    // Newsfeed Bottom View Handling
    // ----------------------------------
    private fun showNewsfeedBottomView(show: Boolean) {
        newsfeedAdapter?.showBottomView(show,
                when {
                    pagination.isNextDistanceAvailable -> {
                        // we can increase the distance
                        NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE
                    }
                    newsfeedAdapter?.dataItemCount == 0 -> {
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
    fun handleHeatzoneClick(location: LatLng) {
        if (isActionsListVisible) {
            centerMapAndZoom(location, ZOOM_HEATZONE, true)
            toggleActionsList()
        } else {
            showHeatzoneMiniCardsAtLocation(location)
        }
    }

    private fun showHeatzoneMiniCardsAtLocation(location: LatLng) {
        if (newsfeedAdapter == null) {
            return
        }
        // get the list of entourages near this location
        val entourageArrayList = ArrayList<BaseEntourage>()
        newsfeedAdapter?.items?.forEach { feedItem ->
            if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
                val entourage = feedItem as BaseEntourage
                if (entourage.distanceToLocation(location) < HEATZONE_SEARCH_RADIUS) {
                    entourageArrayList.add(entourage)
                }
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
        presenter.authenticationController.isIgnoringActionZone = ignoreAddress
        presenter.authenticationController.saveUserPreferences()
        if (!ignoreAddress) {
            EntourageApplication.me(activity)?.address?.let {
                centerMap(LatLng(it.latitude, it.longitude))
            }
        }
    }

    private fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        if (newsfeedAdapter == null) {
            return
        }
        val newHeight = valueAnimator.animatedValue as Int
        newsfeedAdapter?.setMapHeight(newHeight)
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
                (recyclerView.layoutManager as? LinearLayoutManager)?.let { linearLayoutManager ->
                    val visibleItemCount = recyclerView.childCount
                    val firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition()
                    val totalItemCount = linearLayoutManager.itemCount
                    if (totalItemCount - visibleItemCount <= firstVisibleItem + 2) {
                        if (entService?.updateNewsfeed(pagination, selectedTab) == true) {
                            //if update returns false no need to log this...
                            AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_SCROLL_LIST)
                        }
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
        // Radius of the circle where to search for entourages when user taps a heatzone
        private const val HEATZONE_SEARCH_RADIUS = BaseEntourage.HEATMAP_SIZE.toInt() / 2 // meters

        // Zoom in level when taping a heatzone
        private const val ZOOM_HEATZONE = 15.7f
    }

    init {
        eventLongClick = AnalyticsEvents.EVENT_MAP_LONGPRESS
    }
}