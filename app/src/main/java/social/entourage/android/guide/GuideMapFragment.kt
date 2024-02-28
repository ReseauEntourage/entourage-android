package social.entourage.android.guide

import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.Marker
import com.google.android.material.snackbar.Snackbar

import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.base.map.BaseMapFragment
import social.entourage.android.databinding.FragmentGuideMapBinding
import social.entourage.android.guide.filter.GuideFilter.Companion.instance
import social.entourage.android.guide.filter.GuideFilterFragment
import social.entourage.android.guide.poi.PoiListFragment
import social.entourage.android.guide.poi.PoiRenderer
import social.entourage.android.guide.poi.PoisAdapter
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.guide.poi.ReadPoiFragment.Companion.newInstance
import social.entourage.android.tools.utils.Utils
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntLinkMovementMethod
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.user.partner.PartnerFragment
import timber.log.Timber

class GuideMapFragment : BaseMapFragment(R.layout.fragment_guide_map), PoiListFragment, ApiConnectionListener,
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private lateinit var binding: FragmentGuideMapBinding
    private var isAlertTextVisible: Boolean = false
    private val connection = ServiceConnection()

    private var presenter: GuideMapPresenter = GuideMapPresenter(this)

    private var onMapReadyCallback: OnMapReadyCallback? = null
    private val poisAdapter: PoisAdapter = PoisAdapter()
    private var mapRenderer: PoiRenderer = PoiRenderer()

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGuideMapBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeMap()
        initializeAlertBanner()
        initializePopups()
        initializePOIList()
        initializeFloatingButtons()
        initializeFilterButton()
    }

    override fun onStart() {
        super.onStart()
        connection.doBindService()
        presenter.start()
        showInfoPopup()
    }

    override fun onResume() {
        super.onResume()
        val isLocationGranted = isLocationPermissionGranted()
        onLocationPermissionGranted(isLocationGranted)
    }

    override fun onDestroy() {
        connection.doUnbindService()
        super.onDestroy()
    }

    fun onBackPressed(): Boolean {
        if (binding.fragmentMapLongclick.parent.visibility == View.VISIBLE) {
            binding.fragmentMapLongclick.parent.visibility = View.GONE
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

    fun onSolidarityGuideFilterChanged() {
        initializeFilterButton()
        map?.clear()
        presenter.clear()
        poisAdapter.removeAll()
        presenter.updatePoisNearby(map)
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
                                this?.tag = poi
                            }
                        }
                    }
                    hideEmptyListPopup()
                }
                poisAdapter.let {
                    val previousPoiCount = it.dataItemCount
                    it.addItems(poiCollection)
                    if (previousPoiCount == 0) {
                        binding.fragmentGuidePoisView.scrollToPosition(0)
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
        //TODO UNCOMMENT FOR PROD
        //presenter.clear()-
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
                if (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation ?: newLocation) >= REDRAW_LIMIT) {
                    previousCameraZoom = newZoom
                    previousCameraLocation = newLocation
                    presenter.updatePoisNearby(map)
                }
            }
        }
        presenter.updatePoisNearby(map)
    }

    override fun showPoiDetails(poi: Poi, isTxtSearch:Boolean) {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_POI)
        try {
            poi.partner_id?.let { partner_id ->
                PartnerFragment.newInstance(partner_id).show(parentFragmentManager, PartnerFragment.TAG)
            } ?: run {

                val stringFilters = when {
                    isTxtSearch -> "TXT"
                    instance.hasFilteredCategories() -> instance.getFiltersSelected()
                    else -> "ALL"
                }

                newInstance(poi,stringFilters).show(parentFragmentManager, ReadPoiFragment.TAG)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun initializeAlertBanner() {
        isAlertTextVisible = false
        binding.fragmentGuideAlertDescription.setHtmlString(getString(R.string.guide_alert_info_text), EntLinkMovementMethod)
        binding.fragmentGuideAlertArrow.setOnClickListener {onClickAlertArrow()}
        binding.fragmentGuideAlert.setOnClickListener {onClickAlertArrow()}
    }

    private fun onClickAlertArrow() {
        if(!isAlertTextVisible) {
            isAlertTextVisible = true
            binding.fragmentGuideAlertDescription.visibility = View.VISIBLE
            binding.fragmentGuideAlertArrow.setImageDrawable((AppCompatResources.getDrawable(requireContext(), R.drawable.ic_expand_less_black_24dp)))
        } else {
            isAlertTextVisible = false
            binding.fragmentGuideAlertDescription.visibility = View.GONE
            binding.fragmentGuideAlertArrow.setImageDrawable((AppCompatResources.getDrawable(requireContext(), R.drawable.ic_expand_more_black_24dp)))
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------
    private fun initializePopups() {
        binding.fragmentGuideEmptyListPopup.setOnClickListener {onEmptyListPopupClose()}
        binding.fragmentGuideInfoPopupClose.setOnClickListener {onInfoPopupClose()}
        binding.fragmentGuideInfoPopup.setOnClickListener {onInfoPopupClose()}
        val proposePOIUrl = (activity as? GDSMainActivity)?.getLink(Constants.PROPOSE_POI_ID) ?: ""
        hideInfoPopup()
        binding.fragmentGuideEmptyListPopupText.movementMethod = EntLinkMovementMethod
        binding.fragmentGuideEmptyListPopupText.text = Utils.fromHtml(getString(R.string.map_poi_empty_popup, proposePOIUrl))
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
            binding.fragmentGuideEmptyListPopup?.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyListPopup() {
        binding.fragmentGuideEmptyListPopup?.visibility = View.GONE
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
            binding.fragmentGuideInfoPopup?.visibility = View.VISIBLE
        }
    }

    private fun hideInfoPopup() {
        binding.fragmentGuideInfoPopup?.visibility = View.GONE
    }

    private val isInfoPopupVisible: Boolean
        get() = binding.fragmentGuideInfoPopup?.visibility == View.VISIBLE

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
        binding.fragmentGuidePoisView?.layoutManager = LinearLayoutManager(context)
        onMapReadyCallback?.let { poisAdapter.setOnMapReadyCallback(it) }
        poisAdapter.setOnFollowButtonClickListener { onFollowGeolocation() }
        binding.fragmentGuidePoisView?.adapter = poisAdapter
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
        binding.uiViewEmptyList?.visibility = View.GONE
        isFullMapShown = true
        binding.fragmentGuideDisplayToggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        ensureMapVisible()
        val targetHeight = binding.fragmentGuideMainLayout?.measuredHeight ?: originalMapLayoutHeight
        if (animated) {
            val anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight)
            anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
            anim.start()
        } else {
            poisAdapter.setMapHeight(targetHeight)
            binding.fragmentGuidePoisView?.layoutManager?.requestLayout()
        }
    }

    private fun showPOIList(animated: Boolean) {
        if (binding.fragmentGuideMainLayout == null || binding.fragmentGuidePoisView == null || binding.fragmentGuideDisplayToggle == null) {
            return
        }
        if (!isFullMapShown) {
            return
        }
        isFullMapShown = false
        binding.fragmentGuideDisplayToggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_white_24dp))
        hideInfoPopup()
        hideEmptyListPopup()
        if (animated) {
            val anim = ValueAnimator.ofInt(binding.fragmentGuideMainLayout.measuredHeight, originalMapLayoutHeight)
            anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
            anim.start()
        } else {
            poisAdapter.setMapHeight(originalMapLayoutHeight)
            binding.fragmentGuidePoisView?.layoutManager?.requestLayout()
        }

        binding.uiViewEmptyList?.visibility = if (poisAdapter.dataItemCount == 0)  View.VISIBLE else View.GONE
    }

    private fun ensureMapVisible() {
        binding.fragmentGuidePoisView?.scrollToPosition(0)
    }

    // ----------------------------------
    // Floating Buttons
    // ----------------------------------
    private fun initializeFloatingButtons() {
        // Guide starts in full map mode, adjust the text accordingly
        if (context == null) return
        binding.fragmentGuideDisplayToggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        binding.fragmentGuideDisplayToggle?.setOnClickListener {onDisplayToggle()}
        binding.fragmentMapLongclick.guideLongclickButtonPoiPropose.setOnClickListener {proposePOI()}
        binding.buttonPoiPropose?.setOnClickListener {onPOIProposeClicked()}
    }

    private fun initializeFilterButton() {
        binding.fragmentGuideFilterButton?.let {
            it.setOnClickListener {onShowFilter()}
            it.setText(if (instance.hasFilteredCategories()) R.string.guide_filters_activated else R.string.guide_no_filter)
        }
    }

    private fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        poisAdapter.setMapHeight(valueAnimator.animatedValue as Int)
        binding.fragmentGuidePoisView?.layoutManager?.requestLayout()
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
        binding.fragmentGuideCoordinator?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onServerException(throwable: Throwable) {
        binding.fragmentGuideCoordinator?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        binding.fragmentGuideCoordinator?.let {
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

    override fun onMarkerClick(poiMarker: Marker): Boolean {
        (poiMarker.tag as? Poi)?.let { poi ->
            showPoiDetails(poi,false)
        }
        return true
    }
}