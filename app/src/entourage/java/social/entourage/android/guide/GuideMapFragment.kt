package social.entourage.android.guide

import android.animation.ValueAnimator
import android.location.Location
import android.os.Bundle
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_guide_map.*
import kotlinx.android.synthetic.main.layout_guide_longclick.*
import social.entourage.android.*
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.model.map.Category
import social.entourage.android.api.model.tape.EntouragePoiRequest.OnPoiViewRequestedEvent
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
import social.entourage.android.tools.BusProvider
import social.entourage.android.tools.Utils
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class GuideMapFragment : BaseMapFragment(R.layout.fragment_guide_map) {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @Inject
    var presenter: GuideMapPresenter? = null
    private var onMapReadyCallback: OnMapReadyCallback? = null
    private var poisMap: MutableMap<Long, Poi> = TreeMap()
    private var previousEmptyListPopupLocation: Location? = null
    private var poisAdapter: PoisAdapter? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (presenter == null) {
            setupComponent(EntourageApplication.get(activity).entourageComponent)
        }
        initializeMap()
        initializeEmptyListPopup()
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
        presenter?.start()
        if (map != null) {
            presenter?.updatePoisNearby(map)
        }
        showInfoPopup()
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_GUIDE_FROM_TAB)
        BusProvider.getInstance().register(this)
    }

    override fun onResume() {
        super.onResume()
        val isLocationGranted = isLocationPermissionGranted()
        BusProvider.getInstance().post(OnLocationPermissionGranted(isLocationGranted))
    }

    override fun onStop() {
        super.onStop()
        BusProvider.getInstance().unregister(this)
    }

    override fun onBackPressed(): Boolean {
        if (mapLongClickView.visibility == View.VISIBLE) {
            mapLongClickView.visibility = View.GONE
            //fabProposePOI.setVisibility(View.VISIBLE);
            return true
        }
        return false
    }

    fun onShowFilter() {
        try {
            GuideFilterFragment().show(parentFragmentManager, GuideFilterFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun onDisplayToggle() {
        if (!isFullMapShown) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_MAP_VIEW)
        } else {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_LIST_VIEW)
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
        poisAdapter?.removeAll()
        presenter?.updatePoisNearby(map)
    }

    @Subscribe
    fun onPoiViewRequested(event: OnPoiViewRequestedEvent?) {
        event?.poi?.let {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_POI_VIEW)
            showPoiDetails(event.poi)
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun putPoiOnMap(categories: List<Category>?, pois: List<Poi>?) {
        if (activity != null) {
            if (categories != null && mapClusterItemRenderer is PoiRenderer) {
                (mapClusterItemRenderer as PoiRenderer).setCategories(categories)
            }
            clearOldPois()
            if (pois != null && pois.isNotEmpty()) {
                val poiCollection = removeRedundantPois(pois)
                if (map != null && mapClusterManager != null) {
                    mapClusterManager.addItems(poiCollection)
                    mapClusterManager.cluster()
                    hideEmptyListPopup()
                }
                if (poisAdapter != null) {
                    val previousPoiCount = poisAdapter!!.dataItemCount
                    poisAdapter!!.addItems(poiCollection)
                    if (previousPoiCount == 0) {
                        fragment_guide_pois_view?.scrollToPosition(0)
                    }
                }
            } else {
                if (!isInfoPopupVisible) {
                    showEmptyListPopup()
                }
                if (poisAdapter != null && poisAdapter!!.dataItemCount == 0) {
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

    override fun getRenderer(): DefaultClusterRenderer<*> {
        return PoiRenderer(activity, map, mapClusterManager as ClusterManager<Poi?>?)
    }

    private fun proposePOI() {
        // Close the overlays
        onBackPressed()
        // Open the link to propose a POI
        if (activity is MainActivity) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_PROPOSE_POI)
            (activity as MainActivity).showWebViewForLinkId(Constants.PROPOSE_POI_ID)
        }
    }

    override fun getAdapter(): HeaderBaseAdapter {
        return poisAdapter!!
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun clearOldPois() {
        poisMap.clear()
        mapClusterManager?.clearItems()
        poisAdapter?.removeAll()
    }

    private fun onMapReady(googleMap: GoogleMap) {
        super.onMapReady(googleMap,
                OnEntourageMarkerClickListener(),
                null
        )
        map.setOnCameraIdleListener {
            val position = map.cameraPosition
            val newLocation = EntourageLocation.cameraPositionToLocation(null, position)
            val newZoom = position.zoom
            if (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT) {
                previousCameraZoom = newZoom
                previousCameraLocation = newLocation
                presenter?.updatePoisNearby(map)
            }
        }
        presenter?.updatePoisNearby(map)
    }

    private fun removeRedundantPois(pois: List<Poi>): List<Poi> {
        val newPois: MutableList<Poi> = ArrayList()
        for(poi in pois) {
            if (!poisMap.containsKey(poi.id)) {
                poisMap[poi.id] = poi
                newPois.add(poi)
            }
        }
        return newPois
    }

    private fun showPoiDetails(poi: Poi) {
        val readPoiFragment = newInstance(poi)
        try {
            readPoiFragment.show(parentFragmentManager, ReadPoiFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------
    private fun initializeEmptyListPopup() {
        fragment_guide_empty_list_popup.setOnClickListener {onEmptyListPopupClose()}
        fragment_guide_info_popup_close.setOnClickListener {onInfoPopupClose()}
        fragment_guide_info_popup.setOnClickListener {onInfoPopupClose()}
        var proposePOIUrl: String? = ""
        if (activity != null && activity is MainActivity) {
            proposePOIUrl = (activity as MainActivity).getLink(Constants.PROPOSE_POI_ID)
        }
        fragment_guide_empty_list_popup_text?.movementMethod = EntourageLinkMovementMethod.getInstance()
        fragment_guide_empty_list_popup_text?.text = Utils.fromHtml(getString(R.string.map_poi_empty_popup, proposePOIUrl))
    }

    fun onEmptyListPopupClose() {
        val authenticationController = EntourageApplication.get(context).entourageComponent.authenticationController
        //TODO add an "never display" button
        authenticationController?.isShowNoPOIsPopup = false
        hideEmptyListPopup()
    }

    private fun showEmptyListPopup() {
        if (map != null) {
            previousEmptyListPopupLocation = if (previousEmptyListPopupLocation == null) {
                EntourageLocation.cameraPositionToLocation(null, map.cameraPosition)
            } else {
                // Show the popup only we moved from the last position we show it
                val currentLocation = EntourageLocation.cameraPositionToLocation(null, map.cameraPosition)
                if (previousEmptyListPopupLocation!!.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                    return
                }
                currentLocation
            }
        }
        val authenticationController = EntourageApplication.get(context).entourageComponent.authenticationController
        if (authenticationController != null && !authenticationController.isShowNoPOIsPopup) {
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
        if (authenticationController != null) {
            authenticationController.isShowInfoPOIsPopup = false
        }
        hideInfoPopup()
    }

    private fun showInfoPopup() {
        val authenticationController = EntourageApplication.get(context).entourageComponent.authenticationController
        if (authenticationController == null || !authenticationController.isShowInfoPOIsPopup) {
            fragment_guide_info_popup.visibility = View.VISIBLE
        }
    }

    private fun hideInfoPopup() {
        fragment_guide_info_popup.visibility = View.GONE
    }

    private val isInfoPopupVisible: Boolean
        get() = fragment_guide_info_popup.visibility == View.VISIBLE

    // ----------------------------------
    // FAB HANDLING
    // ----------------------------------
    private fun onPOIProposeClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_PLUS_CLICK)
        proposePOI()
    }

    // ----------------------------------
    // POI List
    // ----------------------------------
    private fun initializePOIList() {
        if (poisAdapter == null) {
            fragment_guide_pois_view.layoutManager = LinearLayoutManager(context)
            poisAdapter = PoisAdapter()
            poisAdapter!!.setOnMapReadyCallback(onMapReadyCallback)
            poisAdapter!!.setOnFollowButtonClickListener { onFollowGeolocation() }
            fragment_guide_pois_view.adapter = poisAdapter
        }
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
        isFullMapShown = true
        fragment_map_display_toggle.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        ensureMapVisible()
        val targetHeight = fragment_guide_main_layout.measuredHeight
        if (animated) {
            val anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight)
            anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
            anim.start()
        } else {
            poisAdapter?.setMapHeight(targetHeight)
            fragment_guide_pois_view?.layoutManager?.requestLayout()
        }
    }

    private fun showPOIList(animated: Boolean) {
        if (fragment_guide_main_layout == null || fragment_guide_pois_view == null || fragment_map_display_toggle == null) {
            return
        }
        if (!isFullMapShown) {
            return
        }
        isFullMapShown = false
        fragment_map_display_toggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_white_24dp))
        hideInfoPopup()
        hideEmptyListPopup()
        if (animated) {
            val anim = ValueAnimator.ofInt(fragment_guide_main_layout.measuredHeight, originalMapLayoutHeight)
            anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
            anim.start()
        } else {
            poisAdapter?.setMapHeight(originalMapLayoutHeight)
            fragment_guide_pois_view?.layoutManager?.requestLayout()
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
        fragment_map_display_toggle?.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        fragment_map_display_toggle.setOnClickListener {onDisplayToggle()}
        guide_longclick_button_poi_propose.setOnClickListener {proposePOI()}
        button_poi_propose.setOnClickListener {onPOIProposeClicked()}

        /* TODO activate this !
        if(getActivity()!=null) {

            EntourageTapPrompt proposePrompt = new EntourageTapPrompt(R.id.button_poi_propose, "Proposer un POI","Clique ici pour envoyer les infos", null);
            if(!GuideFilter.getInstance().hasFilteredCategories()) {
                EntourageTapPrompt filterPrompt = new EntourageTapPrompt(R.id.fragment_map_filter_button, "Filtrer les POI","Clique ici pour voir les filtres actifs", proposePrompt);
                filterPrompt.show(getActivity());
            } else {
                proposePrompt.show(getActivity());
            }
        }*/
    }

    private fun initializeFilterButton() {
        fragment_map_filter_button.setOnClickListener {onShowFilter()}
        if (instance.hasFilteredCategories()) {
            //fragment_map_filter_button.extend();
            (fragment_map_filter_button as ExtendedFloatingActionButton).setText(R.string.guide_filters_activated)
        } else {
            (fragment_map_filter_button as ExtendedFloatingActionButton).setText(R.string.guide_no_filter)
            //fragment_map_filter_button.shrink();
        }
    }

    private fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        poisAdapter?.setMapHeight(valueAnimator.animatedValue as Int)
        fragment_guide_pois_view?.layoutManager?.requestLayout()
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    inner class OnEntourageMarkerClickListener : OnClusterItemClickListener<Poi> {
        override fun onClusterItemClick(poi: Poi): Boolean {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_POI_VIEW)
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
}