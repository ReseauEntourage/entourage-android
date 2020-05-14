package social.entourage.android.map

import android.Manifest.permission
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnGroundOverlayClickListener
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.ClusterManager.OnClusterItemClickListener
import com.google.maps.android.clustering.view.ClusterRenderer
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import social.entourage.android.BackPressable
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.location.EntourageLocation
import social.entourage.android.location.LocationUpdateListener
import social.entourage.android.location.LocationUtils.isLocationEnabled
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.tools.BusProvider
import timber.log.Timber

abstract class BaseMapFragment(protected var layout: Int) : Fragment(), BackPressable, LocationUpdateListener {
    protected var eventLongClick: String? = null
    var isFollowing = true
    protected var isFullMapShown = true
    protected var previousCameraLocation: Location? = null
    protected var previousCameraZoom = 1.0f
    protected var map: GoogleMap? = null
    protected var mapClusterManager: ClusterManager<ClusterItem>? = null
    //protected var mapClusterItemRenderer: DefaultClusterRenderer<ClusterItem>? = null
    protected var originalMapLayoutHeight = 0
    private var toReturn: View? = null

    override fun onBackPressed(): Boolean {
        return false
    }

    protected open fun initializeMap() {}
    fun centerMap(latLng: LatLng?) {
        val cameraPosition = CameraPosition(latLng, EntourageLocation.getInstance().lastCameraPosition.zoom, 0F, 0F)
        centerMap(cameraPosition)
    }

    protected fun centerMapAndZoom(latLng: LatLng?, zoom: Float, animated: Boolean) {
        val cameraPosition = CameraPosition(latLng, zoom, 0F, 0F)
        if (map != null) {
            if (animated) {
                map!!.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 300, null)
            } else {
                map!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
            saveCameraPosition()
        }
    }

    private fun centerMap(cameraPosition: CameraPosition) {
        if (map != null && isFollowing) {
            map!!.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            saveCameraPosition()
        }
    }

    protected open fun saveCameraPosition() {}
    private fun initializeMapZoom() {
        val address = EntourageApplication.get().entourageComponent.authenticationController.user?.address
        if (address != null) {
            centerMap(LatLng(address.latitude, address.longitude))
            isFollowing = false
        } else {
            centerMap(EntourageLocation.getInstance().lastCameraPosition)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (index in permissions.indices) {
                if (permissions[index].equals(permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                    BusProvider.instance.post(OnLocationPermissionGranted(grantResults[index] == PackageManager.PERMISSION_GRANTED))
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    protected fun onMapReady(googleMap: GoogleMap?, onClickListener: OnClusterItemClickListener<ClusterItem>?, onGroundOverlayClickListener: OnGroundOverlayClickListener?) {
        map = googleMap ?: return
        //we forced the setting of the map anyway
        if (activity == null) {
            Timber.e("No activity found")
            return
        }
        map!!.isMyLocationEnabled = isLocationPermissionGranted()

        //mylocation is handled in MapViewHolder
        map!!.uiSettings.isMyLocationButtonEnabled = false
        map!!.uiSettings.isMapToolbarEnabled = false
        map!!.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_styles_json))
        mapClusterManager = ClusterManager<ClusterItem>(activity, map)
        //mapClusterItemRenderer = renderer
        mapClusterManager!!.renderer = renderer as ClusterRenderer<ClusterItem>
        mapClusterManager!!.setOnClusterItemClickListener(onClickListener)
        initializeMapZoom()
        map!!.setOnMarkerClickListener(mapClusterManager)
        if (onGroundOverlayClickListener != null) {
            map!!.setOnGroundOverlayClickListener(onGroundOverlayClickListener)
        }
        map!!.setOnMapLongClickListener { latLng: LatLng ->
            //only show when map is in full screen and not visible
            if (!isFullMapShown || fragment_map_longclick?.visibility == View.VISIBLE) {
                return@setOnMapLongClickListener
            }
            if (activity != null) {
                EntourageEvents.logEvent(eventLongClick)
                showLongClickOnMapOptions(latLng)
            }
        }
    }

    protected open val renderer: DefaultClusterRenderer<ClusterItem>?
        get() = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (toReturn == null) {
            toReturn = inflater.inflate(layout, container, false)
        }
        return toReturn
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().lastCameraPosition)
        fragment_map_longclick?.setOnClickListener {hideLongClickView()}
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    protected open fun showLongClickOnMapOptions(latLng: LatLng) {
        //get the click point
        val clickPoint = map!!.projection.toScreenLocation(latLng)
        //adjust the buttons holder layout
        val wm = requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val screenSize = Point()
        display.getSize(screenSize)
        map_longclick_buttons.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        val bW = map_longclick_buttons.measuredWidth
        val bH = map_longclick_buttons.measuredHeight
        val lp = map_longclick_buttons.layoutParams as RelativeLayout.LayoutParams
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
        map_longclick_buttons.layoutParams = lp
        //show the view
        fragment_map_longclick?.visibility = View.VISIBLE
    }

    private fun hideLongClickView() {
        onBackPressed()
    }

    fun showAllowGeolocationDialog(source: Int) {
        if (activity == null) {
            return
        }
        @StringRes var messagedId = R.string.map_error_geolocation_disabled_use_entourage
        var eventName = EntourageEvents.EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER
        when (source) {
            GEOLOCATION_POPUP_RECENTER -> {
                messagedId = R.string.map_error_geolocation_disabled_recenter
                eventName = EntourageEvents.EVENT_FEED_ACTIVATE_GEOLOC_RECENTER
            }
            GEOLOCATION_POPUP_GUIDE_RECENTER -> {
                messagedId = R.string.map_error_geolocation_disabled_recenter
                eventName = EntourageEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_RECENTER
            }
            GEOLOCATION_POPUP_TOUR -> {
                messagedId = R.string.map_error_geolocation_disabled_create_tour
                eventName = EntourageEvents.EVENT_FEED_ACTIVATE_GEOLOC_CREATE_TOUR
            }
            GEOLOCATION_POPUP_GUIDE_BANNER -> eventName = EntourageEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_FROM_BANNER
            GEOLOCATION_POPUP_BANNER -> {
            }
            else -> {
            }
        }
        val finalEventName = eventName // needs to be final for later functions
        AlertDialog.Builder(requireActivity())
                .setMessage(messagedId)
                .setPositiveButton(R.string.activate) { _: DialogInterface?, _: Int ->
                    EntourageEvents.logEvent(finalEventName)
                    try {
                        if (isLocationEnabled() || shouldShowRequestPermissionRationale(permission.ACCESS_FINE_LOCATION)) {
                            requestPermissions(arrayOf(permission.ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_LOCATION)
                        } else {
                            // User selected "Never ask again", so show the settings page
                            displayGeolocationPreferences(true)
                        }
                    } catch (e: IllegalStateException) {
                        Timber.w(e)
                    }
                }
                .setNegativeButton(R.string.map_permission_refuse, null)
                .show()
    }

    fun displayGeolocationPreferences(forceDisplaySettings: Boolean) {
        if (forceDisplaySettings || !isLocationEnabled()) {
            if (activity != null) {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        } else if (!isLocationPermissionGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_BANNER)
        }
    }

    open fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        updateGeolocBanner(event.isPermissionGranted)
    }

    protected open fun updateGeolocBanner(active: Boolean) {
        adapter?.setGeolocStatusIcon(isLocationEnabled() && isLocationPermissionGranted())
        try {
            map?.isMyLocationEnabled = isLocationEnabled()
        } catch (ignored: SecurityException) {
        }
    }

    protected abstract val adapter: HeaderBaseAdapter?
    protected fun onFollowGeolocation() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_RECENTERCLICK)
        // Check if geolocation is enabled
        if (!isLocationEnabled() || !isLocationPermissionGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_RECENTER)
            return
        }
        isFollowing = true
        EntourageLocation.getInstance().currentLocation?.let {
            centerMap(LatLng(it.latitude, it.longitude))
        }
    }

    override fun onLocationUpdated(location: LatLng) {}
    override fun onLocationStatusUpdated(active: Boolean) {
        updateGeolocBanner(active)
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val TAG = "social.entourage.android.base_fragment_map"

        // Constants used to track the source call of the geolocation popup
        const val GEOLOCATION_POPUP_TOUR = 0
        private const val GEOLOCATION_POPUP_RECENTER = 1
        private const val GEOLOCATION_POPUP_BANNER = 2
        private const val GEOLOCATION_POPUP_GUIDE_RECENTER = 3
        private const val GEOLOCATION_POPUP_GUIDE_BANNER = 4
        const val PERMISSIONS_REQUEST_LOCATION = 1
        const val ZOOM_REDRAW_LIMIT = 1.1f
        const val REDRAW_LIMIT = 300
    }

}