package social.entourage.android.guide

import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_guide_map.*
import kotlinx.android.synthetic.main.layout_guide_longclick.*
import social.entourage.android.*
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.api.tape.Events.OnSolidarityGuideFilterChanged
import social.entourage.android.api.tape.PoiRequestEvents.OnPoiViewRequestedEvent
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.guide.filter.GuideFilter.Companion.instance
import social.entourage.android.guide.filter.GuideFilterFragment
import social.entourage.android.guide.poi.PoiRenderer
import social.entourage.android.guide.poi.PoisAdapter
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.guide.poi.ReadPoiFragment.Companion.newInstance
import social.entourage.android.location.EntLocation
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.map.BaseMapFragment
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.EntLinkMovementMethod
import social.entourage.android.tools.Utils
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.user.partner.PartnerFragment
import timber.log.Timber
import java.util.*
import javax.inject.Inject

open class GuideMapFragment : BaseMapFragment(R.layout.fragment_guide_map), ApiConnectionListener,
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var isAlertTextVisible: Boolean = false
    private val connection = ServiceConnection()

    @Inject lateinit var presenter: GuideMapPresenter

    private var onMapReadyCallback: OnMapReadyCallback? = null
    private val poisAdapter: PoisAdapter = PoisAdapter()
    private var mapRenderer: PoiRenderer = PoiRenderer()

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(EntourageApplication.get(activity).components)
        initializeMap()
        initializeAlertBanner()
        initializePopups()
        initializePOIList()
        initializeFloatingButtons()
        initializeFilterButton()


    }

    fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerGuideMapComponent.builder()
                .entourageComponent(entourageComponent)
                .guideMapModule(GuideMapModule(this))
                .build()
                .inject(this)
    }

    override fun onStart() {
        super.onStart()
        connection.doBindService()
        presenter.start()
        showInfoPopup()
        EntBus.register(this)
    }

    override fun onResume() {
        super.onResume()
        val isLocationGranted = isLocationPermissionGranted()
        EntBus.post(OnLocationPermissionGranted(isLocationGranted))
    }

    override fun onStop() {
        super.onStop()
        EntBus.unregister(this)
    }

    override fun onDestroy() {
        connection.doUnbindService()
        super.onDestroy()
    }

    override fun onBackPressed(): Boolean {
        if (fragment_map_longclick?.visibility == View.VISIBLE) {
            fragment_map_longclick?.visibility = View.GONE
            //fabProposePOI.setVisibility(View.VISIBLE);
            return true
        }
        return false
    }

    private fun onShowFilter() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_SHOWFILTERS)
        try {
            GuideFilterFragment().show(parentFragmentManager, GuideFilterFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun onDisplayToggle() {
        AnalyticsEvents.logEvent(if (!isFullMapShown) AnalyticsEvents.ACTION_GUIDE_SHOWMAP else AnalyticsEvents.ACTION_GUIDE_SHOWLIST)
        togglePOIList()
    }

    // ----------------------------------
    // BUS EVENTS
    // ----------------------------------
    @Subscribe
    fun onSolidarityGuideFilterChanged(event: OnSolidarityGuideFilterChanged?) {
        initializeFilterButton()
        map?.clear()
        presenter.clear()
        poisAdapter.removeAll()
        presenter.updatePoisNearby(map)
    }

    @Subscribe
    fun onPoiViewRequested(event: OnPoiViewRequestedEvent?) {
        event?.poi?.let { showPoiDetails(it) }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun putPoiOnMap(pois: List<Poi>?) {
        if (activity != null) {
            clearOldPois()
            if (pois != null && pois.isNotEmpty()) {
                val poiCollection = presenter.removeRedundantPois(pois)
                map?.let { map ->
                    poiCollection.forEach { poi ->
                        mapRenderer.getMarkerOptions(poi, requireContext())?.let { markerOptions ->
                            map.addMarker(markerOptions).apply {
                                this.tag = poi
                            }
                        }
                    }
                    hideEmptyListPopup()
                }
                poisAdapter.let {
                    val previousPoiCount = it.dataItemCount
                    it.addItems(poiCollection)
                    if (previousPoiCount == 0) {
                        fragment_guide_pois_view?.scrollToPosition(0)
                    }
                }
            } else {
                if (!isInfoPopupVisible) {
                    showEmptyListPopup()
                }
                if (poisAdapter.dataItemCount == 0) {
                    hidePOIList(false)
                }
            }
        }
    }

    fun showErrorMessage() {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun initializeMap() {
        originalMapLayoutHeight = resources.getDimension(R.dimen.solidarity_guide_map_height).toInt()
        if (onMapReadyCallback == null) {
            onMapReadyCallback = OnMapReadyCallback { googleMap: GoogleMap -> this.onMapReady(googleMap) }
        }
    }

    @Subscribe
    override fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        super.onLocationPermissionGranted(event)
    }


    private fun proposePOI() {
        // Close the overlays
        onBackPressed()
        // Open the link to propose a POI
        (activity as? GDSMainActivity)?.showWebViewForLinkId(Constants.PROPOSE_POI_ID)
    }

    override val adapter: HeaderBaseAdapter?
        get() { return poisAdapter}

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun clearOldPois() {
        presenter.clear()
        poisAdapter.removeAll()
        map?.clear()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap,
                null
        )
        initializeMapZoom()
        map?.setOnMarkerClickListener(this)
        //map?.setMinZoomPreference(MAX_ZOOM_VALUE)

        map?.setOnCameraIdleListener {
            map?.cameraPosition?.let {position ->
                val newLocation = EntLocation.cameraPositionToLocation(null, position)
                val newZoom = position.zoom
                if (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT) {
                    previousCameraZoom = newZoom
                    previousCameraLocation = newLocation
                    presenter.updatePoisNearby(map)
                }
            }
        }
        presenter.updatePoisNearby(map)
    }

    private fun showPoiDetails(poi: Poi) {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_POI)
        try {
            poi.partner_id?.let { partner_id ->
                PartnerFragment.newInstance(partner_id).show(parentFragmentManager, PartnerFragment.TAG)
            } ?: run {
                newInstance(poi).show(parentFragmentManager, ReadPoiFragment.TAG)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun initializeAlertBanner() {
        isAlertTextVisible = false
        fragment_guide_alert_description?.setHtmlString(getString(R.string.guide_alert_info_text), EntLinkMovementMethod)
        fragment_guide_alert_arrow?.setOnClickListener {onClickAlertArrow()}
        fragment_guide_alert?.setOnClickListener {onClickAlertArrow()}
    }

    private fun onClickAlertArrow() {
        if(!isAlertTextVisible) {
            isAlertTextVisible = true
            fragment_guide_alert_description?.visibility = View.VISIBLE
            fragment_guide_alert_arrow?.setImageDrawable((AppCompatResources.getDrawable(requireContext(), R.drawable.ic_expand_less_black_24dp)))
        } else {
            isAlertTextVisible = false
            fragment_guide_alert_description?.visibility = View.GONE
            fragment_guide_alert_arrow?.setImageDrawable((AppCompatResources.getDrawable(requireContext(), R.drawable.ic_expand_more_black_24dp)))
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------
    private fun initializePopups() {
        fragment_guide_empty_list_popup?.setOnClickListener {onEmptyListPopupClose()}
        fragment_guide_info_popup_close?.setOnClickListener {onInfoPopupClose()}
        fragment_guide_info_popup?.setOnClickListener {onInfoPopupClose()}
        val proposePOIUrl = (activity as? GDSMainActivity)?.getLink(Constants.PROPOSE_POI_ID) ?: ""
        hideInfoPopup()
        fragment_guide_empty_list_popup_text?.movementMethod = EntLinkMovementMethod
        fragment_guide_empty_list_popup_text?.text = Utils.fromHtml(getString(R.string.map_poi_empty_popup, proposePOIUrl))
    }

    private fun onEmptyListPopupClose() {
        //TODO add an "never display" button
        presenter.isShowNoPOIsPopup = false
        hideEmptyListPopup()
    }

    private fun showEmptyListPopup() {
        map?.cameraPosition?.let { cameraPosition ->
            presenter.updatePreviousEmptyListPopupLocation(cameraPosition)
        }
        if (presenter.isShowNoPOIsPopup) {
            fragment_guide_empty_list_popup?.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyListPopup() {
        fragment_guide_empty_list_popup?.visibility = View.GONE
    }

    // ----------------------------------
    // INFO POPUP
    // ----------------------------------
    private fun onInfoPopupClose() {
        presenter.isShowInfoPOIsPopup = false
        hideInfoPopup()
    }

    private fun showInfoPopup() {
        if (!presenter.isShowInfoPOIsPopup) {
            fragment_guide_info_popup?.visibility = View.VISIBLE
        }
    }

    private fun hideInfoPopup() {
        fragment_guide_info_popup?.visibility = View.GONE
    }

    private val isInfoPopupVisible: Boolean
        get() = fragment_guide_info_popup?.visibility == View.VISIBLE

    // ----------------------------------
    // FAB HANDLING
    // ----------------------------------
    private fun onPOIProposeClicked() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PLUS_STRUCTURE)
        proposePOI()
    }

    // ----------------------------------
    // POI List
    // ----------------------------------
    private fun initializePOIList() {
        fragment_guide_pois_view?.layoutManager = LinearLayoutManager(context)
        onMapReadyCallback?.let { poisAdapter.setOnMapReadyCallback(it) }
        poisAdapter.setOnFollowButtonClickListener { onFollowGeolocation() }
        fragment_guide_pois_view?.adapter = poisAdapter
    }

    private fun togglePOIList() {
        if (isFullMapShown) {
            showPOIList(true)
        } else {
            hidePOIList(true)
        }
    }

    private fun hidePOIList(animated: Boolean) {
        if (isFullMapShown) {
            return
        }
        ui_view_empty_list?.visibility = View.GONE
        isFullMapShown = true
        fragment_guide_display_toggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        ensureMapVisible()
        val targetHeight = fragment_guide_main_layout?.measuredHeight ?: originalMapLayoutHeight
        if (animated) {
            val anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight)
            anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
            anim.start()
        } else {
            poisAdapter.setMapHeight(targetHeight)
            fragment_guide_pois_view?.layoutManager?.requestLayout()
        }
    }

    private fun showPOIList(animated: Boolean) {
        if (fragment_guide_main_layout == null || fragment_guide_pois_view == null || fragment_guide_display_toggle == null) {
            return
        }
        if (!isFullMapShown) {
            return
        }
        isFullMapShown = false
        fragment_guide_display_toggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_white_24dp))
        hideInfoPopup()
        hideEmptyListPopup()
        if (animated) {
            val anim = ValueAnimator.ofInt(fragment_guide_main_layout.measuredHeight, originalMapLayoutHeight)
            anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
            anim.start()
        } else {
            poisAdapter.setMapHeight(originalMapLayoutHeight)
            fragment_guide_pois_view?.layoutManager?.requestLayout()
        }

        ui_view_empty_list?.visibility = if (poisAdapter.dataItemCount == 0)  View.VISIBLE else View.GONE
    }

    private fun ensureMapVisible() {
        fragment_guide_pois_view?.scrollToPosition(0)
    }

    // ----------------------------------
    // Floating Buttons
    // ----------------------------------
    private fun initializeFloatingButtons() {
        // Guide starts in full map mode, adjust the text accordingly
        if (context == null) return
        fragment_guide_display_toggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        fragment_guide_display_toggle?.setOnClickListener {onDisplayToggle()}
        guide_longclick_button_poi_propose?.setOnClickListener {proposePOI()}
        button_poi_propose?.setOnClickListener {onPOIProposeClicked()}
    }

    private fun initializeFilterButton() {
        fragment_guide_filter_button?.let {
            it.setOnClickListener {onShowFilter()}
            it.setText(if (instance.hasFilteredCategories()) R.string.guide_filters_activated else R.string.guide_no_filter)
        }
    }

    private fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        poisAdapter.setMapHeight(valueAnimator.animatedValue as Int)
        fragment_guide_pois_view?.layoutManager?.requestLayout()
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.fragment_guide"
        const val MAX_ZOOM_VALUE = 14.0F
    }

    init {
        eventLongClick = AnalyticsEvents.EVENT_GUIDE_LONGPRESS
    }

    override fun onNetworkException() {
        fragment_guide_coordinator?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onServerException(throwable: Throwable) {
        fragment_guide_coordinator?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        fragment_guide_coordinator?.let {
            EntSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private inner class ServiceConnection : android.content.ServiceConnection {
        private var isBound = false
        var entService: EntService? = null

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity != null) {
                entService = (service as EntService.LocalBinder).service
                entService?.registerServiceListener(this@GuideMapFragment)
                isBound = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entService?.unregisterServiceListener(this@GuideMapFragment)
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
            entService?.unregisterServiceListener(this@GuideMapFragment)
            activity?.unbindService(this)
            isBound = false
        }
    }

    override fun onMarkerClick(poiMarker: Marker?): Boolean {
        (poiMarker?.tag as? Poi)?.let { poi ->
            showPoiDetails(poi)
        }
        return true
    }
}