package social.entourage.android.guide

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.request.ClusterPoi
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUpdateListener
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.databinding.FragmentGuideMapBinding
import social.entourage.android.guide.filter.GuideFilter.Companion.instance
import social.entourage.android.guide.filter.GuideFilterFragment
import social.entourage.android.guide.poi.PoiListFragment
import social.entourage.android.guide.poi.PoiRenderer
import social.entourage.android.guide.poi.PoisAdapter
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.guide.poi.ReadPoiFragment.Companion.newInstance
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntLinkMovementMethod
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.user.partner.PartnerFragment
import timber.log.Timber

class GuideMapFragment : Fragment(),
    LocationUpdateListener, PoiListFragment, ApiConnectionListener,
        GoogleMap.OnMarkerClickListener, OnMapReadyCallback {
    private var eventLongClick: String = AnalyticsEvents.EVENT_GUIDE_LONGPRESS
    private var isFullMapShown = true
    private var previousCameraLocation: Location? = null
    private var previousCameraZoom = 10.0f
    var map: GoogleMap? = null

    private var originalMapLayoutHeight = 0

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if(permissions.entries.any {
                    it.value
                }) {
                RefreshController.shouldRefreshLocationPermission = true
                onLocationPermissionGranted(true)
            }
        }

    private fun centerMap(latLng: LatLng) {
        val cameraPosition = CameraPosition(latLng, previousCameraZoom, 0F, 0F)
        centerMap(cameraPosition)
    }

    private fun centerMap(cameraPosition: CameraPosition) {
        map?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 300, null)
        saveCameraPosition()
    }

    private fun saveCameraPosition() {
        map?.cameraPosition?.let {
            EntLocation.lastCameraPosition = it
        }
    }

    private fun initializeMapZoom() {
        EntourageApplication.get().authenticationController.me?.address?.let {
            centerMap(LatLng(it.latitude, it.longitude))
        } ?: run {
            centerMap(EntLocation.lastCameraPosition)
        }
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    private fun showAllowGeolocationDialog(source: Int) {
        activity?.let {
            @StringRes var messagedId = R.string.map_error_geolocation_disabled_use_entourage
            val eventName = when (source) {
                GEOLOCATION_POPUP_GUIDE_RECENTER -> {
                    messagedId = R.string.map_error_geolocation_disabled_recenter
                    AnalyticsEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_RECENTER
                }
                else -> AnalyticsEvents.EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER
            }

            AlertDialog.Builder(it)
                .setMessage(messagedId)
                .setPositiveButton(R.string.activate) { _: DialogInterface?, _: Int ->
                    AnalyticsEvents.logEvent(eventName)
                    try {
                        if (!LocationUtils.isLocationPermissionGranted()
                            || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        ) {
                            requestPermissionLauncher.launch(arrayOf(
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ))
                        } else {
                            // User selected "Never ask again", so show the settings page
                            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        }
                    } catch (e: IllegalStateException) {
                        Timber.w(e)
                    }
                }
                .setNegativeButton(R.string.map_permission_refuse, null)
                .show()
        }
    }

    private fun onLocationPermissionGranted(isPermissionGranted: Boolean) =
        updateGeolocBanner()

    private fun updateGeolocBanner() {
        val granted = LocationUtils.isLocationPermissionGranted()
        poisAdapter.setGeolocStatusIcon(granted)
        try {
            map?.isMyLocationEnabled = granted
        } catch (ex: SecurityException) {
            Timber.w(ex)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    private fun onFollowGeolocation() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_RECENTERCLICK)
        // Check if geolocation is enabled
        if (!LocationUtils.isLocationPermissionGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_GUIDE_RECENTER)
        } else {
            centerMap(EntLocation.currentLocation?.let {
                LatLng(it.latitude, it.longitude)
            } ?: LatLng(0.0, 0.0))
        }
    }

    override fun onLocationUpdated(location: LatLng) {}

    override fun onLocationStatusUpdated() = updateGeolocBanner()

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
    private var toReturn: View? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (toReturn == null) {
            binding= FragmentGuideMapBinding.inflate(inflater, container, false)
            toReturn = binding.root
        }
        return toReturn
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previousCameraLocation =
            EntLocation.cameraPositionToLocation(null, EntLocation.lastCameraPosition)
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
        val isLocationGranted = LocationUtils.isLocationPermissionGranted()
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

    // Méthode pour vider la carte
    fun clearMap() {
        map?.clear()
    }

    // Méthode pour afficher les clusters et POIs
    fun putClustersAndPoisOnMap(clustersAndPois: List<ClusterPoi>) {
        val poisToAdd = mutableListOf<Poi>() // Liste pour stocker les POIs à ajouter à l'adaptateur

        clustersAndPois.forEach { item ->
            val position = LatLng(item.latitude, item.longitude)
            when (item.type) {
                "cluster" -> {
                    // Créer un marqueur personnalisé pour le cluster
                    val markerOptions = MarkerOptions()
                        .position(position)
                        .icon(createClusterIcon(item.count))
                        .title("Cluster de ${item.count} POIs")

                    map?.addMarker(markerOptions)
                }
                "poi" -> {
                    // Utiliser le PoiRenderer pour les POIs
                    val poi = item.toPoi()
                    context?.let { safeContext ->  // Vérification du contexte
                        mapRenderer.getMarkerOptions(poi, safeContext)?.let { markerOptions ->
                            map?.addMarker(markerOptions)?.apply {
                                this.tag = poi
                            }
                        }
                    }
                    // Ajouter le POI à la liste des POIs à ajouter à l'adaptateur
                    poisToAdd.add(poi)
                }
            }
        }

        // Ajouter les POIs à l'adaptateur
        if (poisToAdd.isNotEmpty()) {
            poisAdapter.removeAll()
            poisAdapter.addItems(poisToAdd)
        }
    }


    private fun createClusterIcon(poiCount: Int): BitmapDescriptor {
        val iconSize = 100  // Taille de l'icône
        val bitmap = Bitmap.createBitmap(iconSize, iconSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        context?.let { safeContext ->  // Vérification que le contexte est disponible
            // Récupérer les couleurs à partir des ressources
            val orangeSecondaryLocal = ContextCompat.getColor(safeContext, R.color.orange_secondary_local)
            val orangeTextMapLocal = ContextCompat.getColor(safeContext, R.color.orange_entourage)

            // Dessiner le fond de l'icône (un cercle par exemple)
            val paint = Paint().apply {
                color = orangeSecondaryLocal  // Utiliser la couleur orange_secondary_local
                isAntiAlias = true
            }
            val radius = iconSize / 2f
            canvas.drawCircle(radius, radius, radius, paint)

            // Dessiner le texte (nombre de POIs)
            paint.color = orangeTextMapLocal  // Utiliser la couleur orange_text_map_local
            paint.textSize = 40f
            paint.textAlign = Paint.Align.CENTER

            val textX = radius
            val textY = radius - (paint.descent() + paint.ascent()) / 2
            canvas.drawText(poiCount.toString(), textX, textY, paint)
        }

        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }



    // Convertir ClusterPoi en Poi pour l'utiliser avec PoiRenderer
    private fun ClusterPoi.toPoi(): Poi {
        return Poi().apply {
            id = this@toPoi.id?.toLong() ?: 0
            uuid = this@toPoi.uuid ?: "" // Transférez le uuid ici
            name = this@toPoi.name ?: "Unnamed POI"
            latitude = this@toPoi.latitude
            longitude = this@toPoi.longitude
            categoryId = this@toPoi.category_id ?: 0
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
        presenter.updatePoisAndClusters(map)
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun putPoiOnMap(pois: List<Poi>?) {
        if (activity != null) {
            clearOldPois()
            if (!pois.isNullOrEmpty()) {
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
                    hidePOIList()
                }
            }
        }
    }

    fun showErrorMessage() {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    private fun initializeMap() {
        originalMapLayoutHeight = resources.getDimension(R.dimen.solidarity_guide_map_height).toInt()
        if (onMapReadyCallback == null) {
            onMapReadyCallback = OnMapReadyCallback { googleMap: GoogleMap -> this.onMapReady(googleMap) }
        }
    }

    private fun proposePOI() {
        // Close the overlays
        onBackPressed()
        // Open the link to propose a POI
        (activity as? GDSMainActivity)?.showWebViewForLinkId(Constants.PROPOSE_POI_ID )
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun clearOldPois() {
        //TODO UNCOMMENT FOR PROD
        //presenter.clear()-
        poisAdapter.removeAll()
        map?.clear()
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap
        //we forced the setting of the map anyway
        if (activity == null) {
            Timber.e("No activity found")
            return
        }

        val isLocationPermissionGranted = LocationUtils.isLocationPermissionGranted()

        googleMap.isMyLocationEnabled = isLocationPermissionGranted

        //mylocation is handled in MapViewHolder
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                requireContext(),
                R.raw.map_styles_json
            )
        )

        googleMap.setOnMapLongClickListener { latLng: LatLng ->
            //only show when map is in full screen and not visible
            if (!isFullMapShown || binding.fragmentMapLongclick.parent.visibility == View.VISIBLE) {
                return@setOnMapLongClickListener
            }
            if (activity != null) {
                AnalyticsEvents.logEvent(eventLongClick)
                showLongClickOnMapOptions(latLng)
            }
        }
        initializeMapZoom()
        map?.setOnMarkerClickListener(this)

        // Appel pour charger les clusters et POIs au démarrage
        presenter.updatePoisAndClusters(map)

        // Gestion du zoom/dézoom ou des mouvements de caméra
        map?.setOnCameraIdleListener {
            map?.cameraPosition?.let { position ->
                val newLocation = EntLocation.cameraPositionToLocation(null, position)
                val newZoom = position.zoom
                if (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation ?: newLocation) >= REDRAW_LIMIT) {
                    previousCameraZoom = newZoom
                    previousCameraLocation = newLocation
                    presenter.updatePoisAndClusters(map)
                }
            }
        }
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
            binding.fragmentGuideEmptyListPopup.visibility = View.VISIBLE
        }
    }

    private fun hideEmptyListPopup() {
        binding.fragmentGuideEmptyListPopup.visibility = View.GONE
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
            binding.fragmentGuideInfoPopup.visibility = View.VISIBLE
        }
    }

    private fun hideInfoPopup() {
        binding.fragmentGuideInfoPopup.visibility = View.GONE
    }

    private val isInfoPopupVisible: Boolean
        get() = binding.fragmentGuideInfoPopup.visibility == View.VISIBLE

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
        binding.fragmentGuidePoisView.layoutManager = LinearLayoutManager(context)
        onMapReadyCallback?.let { poisAdapter.setOnMapReadyCallback(it) }
        poisAdapter.setOnFollowButtonClickListener { onFollowGeolocation() }
        binding.fragmentGuidePoisView.adapter = poisAdapter
    }

    private fun togglePOIList() {
        if (isFullMapShown) {
            showPOIList()
        } else {
            hidePOIList()
        }
    }

    private fun hidePOIList() {
        if (isFullMapShown) {
            return
        }
        binding.uiViewEmptyList.visibility = View.GONE
        isFullMapShown = true
        binding.fragmentGuideDisplayToggle.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        ensureMapVisible()
        val targetHeight = binding.fragmentGuideMainLayout.measuredHeight
        val anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight)
        anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
        anim.start()
    }

    private fun showPOIList() {
        if (!isFullMapShown) {
            return
        }
        isFullMapShown = false
        binding.fragmentGuideDisplayToggle.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_map_white_24dp))
        hideInfoPopup()
        hideEmptyListPopup()
        val anim = ValueAnimator.ofInt(binding.fragmentGuideMainLayout.measuredHeight, originalMapLayoutHeight)
        anim.addUpdateListener { valueAnimator: ValueAnimator -> onAnimationUpdate(valueAnimator) }
        anim.start()

        binding.uiViewEmptyList.visibility = if (poisAdapter.dataItemCount == 0)  View.VISIBLE else View.GONE
    }

    private fun ensureMapVisible() {
        binding.fragmentGuidePoisView.scrollToPosition(0)
    }

    // ----------------------------------
    // Floating Buttons
    // ----------------------------------
    private fun initializeFloatingButtons() {
        // Guide starts in full map mode, adjust the text accordingly
        if (context == null) return
        binding.fragmentGuideDisplayToggle.setImageDrawable(AppCompatResources.getDrawable(requireContext(), R.drawable.ic_list_white_24dp))
        binding.fragmentGuideDisplayToggle.setOnClickListener {onDisplayToggle()}
        binding.fragmentMapLongclick.guideLongclickButtonPoiPropose.setOnClickListener {proposePOI()}
        binding.buttonPoiPropose.setOnClickListener {
            onPOIProposeClicked()
        }
    }

    private fun initializeFilterButton() {
        binding.fragmentGuideFilterButton.let {
            it.setOnClickListener {onShowFilter()}
            it.setText(if (instance.hasFilteredCategories()) R.string.guide_filters_activated else R.string.guide_no_filter)
        }
    }

    private fun onAnimationUpdate(valueAnimator: ValueAnimator) {
        poisAdapter.setMapHeight(valueAnimator.animatedValue as Int)
        binding.fragmentGuidePoisView.layoutManager?.requestLayout()
    }

    companion object {
        // Constants used to track the source call of the geolocation popup
        private const val GEOLOCATION_POPUP_GUIDE_RECENTER = 3
        const val ZOOM_REDRAW_LIMIT = 1.1f
        const val REDRAW_LIMIT = 300
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.fragment_guide"
        const val MAX_ZOOM_VALUE = 14.0F
    }

    override fun onNetworkException() {
        binding.fragmentGuideCoordinator.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onServerException(throwable: Throwable) {
        binding.fragmentGuideCoordinator.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        binding.fragmentGuideCoordinator.let {
            EntSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
    }
    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    private fun showLongClickOnMapOptions(latLng: LatLng) {
        //get the click point
        map?.let {
            binding.fragmentMapLongclick.mapLongclickButtons.let { buttons ->
                buttons.measure(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                val bW = buttons.measuredWidth
                val bH = buttons.measuredHeight
                val lp = buttons.layoutParams as RelativeLayout.LayoutParams
                val clickPoint = it.projection.toScreenLocation(latLng)
                //adjust the buttons holder layout
                val display =
                    (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
                val screenSize = Point()
                display.getSize(screenSize)

                var marginLeft = clickPoint.x - bW / 2
                if (marginLeft + bW > screenSize.x) {
                    marginLeft -= bW / 2
                }
                if (marginLeft < 0) {
                    marginLeft = 0
                }
                var marginTop = clickPoint.y - bH / 2
                if (marginTop < 0) {
                    marginTop = clickPoint.y
                }
                lp.setMargins(marginLeft, marginTop, 0, 0)
                buttons.layoutParams = lp
            }
            //show the view
            binding.fragmentMapLongclick.parent.visibility = View.VISIBLE
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

    override fun onMarkerClick(marker: Marker): Boolean {
        val poi = marker.tag as? Poi
        val cluster = marker.title?.startsWith("Cluster") == true

        if (cluster) {
            val currentZoom = map?.cameraPosition?.zoom ?: 10f
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(marker.position, currentZoom + 2f) // Zoom in on the cluster
            )
            return true
        }

        poi?.let {
            showPoiDetails(it, false)
        }

        return true
    }

}