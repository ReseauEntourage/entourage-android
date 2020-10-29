package social.entourage.android.guide

import android.animation.ValueAnimator
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_guide_map.*
import kotlinx.android.synthetic.main.fragment_guide_map.fragment_map_longclick
import kotlinx.android.synthetic.main.layout_guide_longclick.*
import social.entourage.android.*
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.model.guide.Category
import social.entourage.android.api.tape.EntouragePoiRequest.OnPoiViewRequestedEvent
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.api.tape.Events.OnSolidarityGuideFilterChanged
import social.entourage.android.base.EntourageLinkMovementMethod
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.guide.filter.GuideFilter.Companion.instance
import social.entourage.android.guide.filter.GuideFilterFragment
import social.entourage.android.guide.poi.PoiRenderer
import social.entourage.android.guide.poi.PoisAdapter
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.guide.poi.ReadPoiFragment.Companion.newInstance
import social.entourage.android.location.EntourageLocation
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.map.BaseMapFragment
import social.entourage.android.service.EntourageService
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.Utils
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.tools.view.EntourageSnackbar
import social.entourage.android.user.partner.PartnerFragmentV2
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

open class GuideMapFragment : BaseMapFragment(R.layout.fragment_guide_map), ApiConnectionListener {
    private var isAlertTextVisible: Boolean = false

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private val connection = ServiceConnection()

    @Inject lateinit var presenter: GuideMapPresenter

    private var onMapReadyCallback: OnMapReadyCallback? = null
    private var poisMap: MutableMap<String, Poi> = TreeMap()
    private var previousEmptyListPopupLocation: Location? = null
    private val poisAdapter: PoisAdapter = PoisAdapter()
    private var mapClusterItemRenderer: PoiRenderer? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(EntourageApplication.get(activity).entourageComponent)
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
        BusProvider.instance.register(this)
    }

    override fun onResume() {
        super.onResume()
        val isLocationGranted = isLocationPermissionGranted()
        BusProvider.instance.post(OnLocationPermissionGranted(isLocationGranted))
    }

    override fun onStop() {
        super.onStop()
        BusProvider.instance.unregister(this)
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
        EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_SHOWFILTERS)
        try {
            GuideFilterFragment().show(parentFragmentManager, GuideFilterFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun onDisplayToggle() {
        if (!isFullMapShown) {
            EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_SHOWMAP)
        } else {
            EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_SHOWLIST)
        }
        togglePOIList()
    }

    // ----------------------------------
    // BUS EVENTS
    // ----------------------------------
    @Subscribe
    fun onSolidarityGuideFilterChanged(event: OnSolidarityGuideFilterChanged?) {
        initializeFilterButton()
        mapClusterManager?.clearItems()
        poisMap.clear()
        poisAdapter.removeAll()
        presenter.updatePoisNearby(map)
    }

    @Subscribe
    fun onPoiViewRequested(event: OnPoiViewRequestedEvent?) {
        event?.poi?.let {
            showPoiDetails(event.poi)
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun putPoiOnMap(pois: List<Poi>?) {
        if (activity != null) {
            clearOldPois()
            if (pois != null && pois.isNotEmpty()) {
                val poiCollection = removeRedundantPois(pois)
                Timber.d("***** put poi on map new coll pois : ${poiCollection.size}")
                if (map != null) {
                    mapClusterManager?.let {
                        it.addItems(poiCollection)
                        it.cluster()
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

    override fun getClusterRenderer(): DefaultClusterRenderer<ClusterItem> {
        if(mapClusterItemRenderer==null) mapClusterItemRenderer = PoiRenderer(activity, map, mapClusterManager as ClusterManager<Poi>?)
        return mapClusterItemRenderer as DefaultClusterRenderer<ClusterItem>
    }


    private fun proposePOI() {
        // Close the overlays
        onBackPressed()
        // Open the link to propose a POI
//        (activity as? MainActivity)?.showWebViewForLinkId(Constants.PROPOSE_POI_ID)
        (activity as? GDSMainActivity)?.showWebViewForLinkId(Constants.PROPOSE_POI_ID)
    }

    override val adapter: HeaderBaseAdapter?
        get() { return poisAdapter}

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun clearOldPois() {
        poisMap.clear()
        mapClusterManager?.clearItems()
        poisAdapter.removeAll()
        map?.clear()
    }

    private fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap,
                OnEntourageMarkerClickListener() as OnClusterItemClickListener<ClusterItem>?,
                null
        )
        map?.setOnCameraIdleListener {
            map?.cameraPosition?.let {position ->
                val newLocation = EntourageLocation.cameraPositionToLocation(null, position)
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

    private fun removeRedundantPois(pois: List<Poi>): List<Poi> {
        val newPois: MutableList<Poi> = ArrayList()
        for(poi in pois) {
            if (!poisMap.containsKey(poi.uuid)) {
                poisMap[poi.uuid] = poi
                newPois.add(poi)
            }
        }
        return newPois
    }

    private fun showPoiDetails(poi: Poi) {
        EntourageEvents.logEvent(EntourageEvents.ACTION_GUIDE_POI)
        if (poi.partner_id != null) {
            PartnerFragmentV2.newInstance(null,poi.partner_id).show(parentFragmentManager, PartnerFragmentV2.TAG)
        }
        else {
            val readPoiFragment = newInstance(poi)
            try {
                readPoiFragment.show(parentFragmentManager, ReadPoiFragment.TAG)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    private fun initializeAlertBanner() {
        isAlertTextVisible = false
        fragment_guide_alert_description?.setHtmlString(getString(R.string.guide_alert_info_text), EntourageLinkMovementMethod)
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
//        val proposePOIUrl = (activity as? MainActivity)?.getLink(Constants.PROPOSE_POI_ID) ?: ""
        val proposePOIUrl = (activity as? GDSMainActivity)?.getLink(Constants.PROPOSE_POI_ID) ?: ""
        hideInfoPopup()
        fragment_guide_empty_list_popup_text?.movementMethod = EntourageLinkMovementMethod
        fragment_guide_empty_list_popup_text?.text = Utils.fromHtml(getString(R.string.map_poi_empty_popup, proposePOIUrl))
    }

    private fun onEmptyListPopupClose() {
        val authenticationController = EntourageApplication.get(context).entourageComponent.authenticationController
        //TODO add an "never display" button
        authenticationController.isShowNoPOIsPopup = false
        hideEmptyListPopup()
    }

    private fun showEmptyListPopup() {
        map?.let { googleMap ->
            previousEmptyListPopupLocation?.let {
                // Show the popup only we moved from the last position we show it
                val currentLocation = EntourageLocation.cameraPositionToLocation(null, googleMap.cameraPosition)
                if (it.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                    return
                }
                //We need to update the class object
                previousEmptyListPopupLocation = currentLocation
            } ?: run {
                previousEmptyListPopupLocation = EntourageLocation.cameraPositionToLocation(null, googleMap.cameraPosition)
            }
        }
        val isShowNoPOIsPopup = EntourageApplication.get(context).entourageComponent.authenticationController.isShowNoPOIsPopup
        if (!isShowNoPOIsPopup) {
            return
        }
        fragment_guide_empty_list_popup?.visibility = View.VISIBLE
    }

    private fun hideEmptyListPopup() {
        fragment_guide_empty_list_popup?.visibility = View.GONE
    }

    // ----------------------------------
    // INFO POPUP
    // ----------------------------------
    private fun onInfoPopupClose() {
        val authenticationController = EntourageApplication.get(context).entourageComponent.authenticationController
        authenticationController.isShowInfoPOIsPopup = false
        hideInfoPopup()
    }

    private fun showInfoPopup() {
        val authenticationController = EntourageApplication.get(context).entourageComponent.authenticationController
        if (!authenticationController.isShowInfoPOIsPopup) {
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
        EntourageEvents.logEvent(EntourageEvents.ACTION_PLUS_STRUCTURE)
        proposePOI()
    }

    // ----------------------------------
    // POI List
    // ----------------------------------
    private fun initializePOIList() {
        fragment_guide_pois_view?.layoutManager = LinearLayoutManager(context)
        onMapReadyCallback?.let { poisAdapter.setOnMapReadyCallback(it) }
        poisAdapter.setOnFollowButtonClickListener({ onFollowGeolocation() })
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

        if (poisAdapter.dataItemCount == 0) {
            Timber.d("***** ici Pois show list empty ;)")
            ui_view_empty_list?.visibility = View.VISIBLE
        }
        else {
            ui_view_empty_list?.visibility = View.GONE
        }
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

        /* TODO activate this !
        if(getActivity()!=null) {

            EntourageTapPrompt proposePrompt = new EntourageTapPrompt(R.id.button_poi_propose, "Proposer un POI","Clique ici pour envoyer les infos", null);
            if(!GuideFilter.getInstance().hasFilteredCategories()) {
                EntourageTapPrompt filterPrompt = new EntourageTapPrompt(R.id.fragment_guide_filter_button, "Filtrer les POI","Clique ici pour voir les filtres actifs", proposePrompt);
                filterPrompt.show(getActivity());
            } else {
                proposePrompt.show(getActivity());
            }
        }*/
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

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    inner class OnEntourageMarkerClickListener : OnClusterItemClickListener<Poi> {
        override fun onClusterItemClick(poi: Poi): Boolean {
            Timber.d("***** On cluster item click ? ${poi}")
            showPoiDetails(poi)
            return true
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.fragment_guide"
    }

    init {
        eventLongClick = EntourageEvents.EVENT_GUIDE_LONGPRESS
    }

    override fun onNetworkException() {
        fragment_guide_coordinator?.let {
            EntourageSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onServerException(throwable: Throwable) {
        fragment_guide_coordinator?.let {
            EntourageSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        fragment_guide_coordinator?.let {
            EntourageSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private inner class ServiceConnection : android.content.ServiceConnection {
        private var isBound = false
        var entourageService: EntourageService? = null

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity != null) {
                entourageService = (service as EntourageService.LocalBinder).service
                entourageService?.registerServiceListener(this@GuideMapFragment)
                isBound = true
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entourageService?.unregisterServiceListener(this@GuideMapFragment)
            entourageService = null
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
                    val intent = Intent(it, EntourageService::class.java)
                    it.startService(intent)
                    it.bindService(intent, this, Context.BIND_AUTO_CREATE)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }

        fun doUnbindService() {
            if (!isBound) return
            entourageService?.unregisterServiceListener(this@GuideMapFragment)
            activity?.unbindService(this)
            isBound = false
        }
    }
}