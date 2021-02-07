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
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.layout_map_longclick.*
import social.entourage.android.base.BackPressable
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.R
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.location.EntLocation
import social.entourage.android.location.LocationUpdateListener
import social.entourage.android.location.LocationUtils.isLocationEnabled
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.tools.EntBus
import timber.log.Timber

abstract class BaseMapFragment(protected var layout: Int) : Fragment(), BackPressable, LocationUpdateListener {
    protected lateinit var eventLongClick: String
    var isFollowing = true
    protected var isFullMapShown = true
    protected var previousCameraLocation: Location? = null
    protected var previousCameraZoom = 1.0f
    var map: GoogleMap? = null
    protected abstract val adapter: HeaderBaseAdapter?

    protected var originalMapLayoutHeight = 0
    private var toReturn: View? = null

    override fun onBackPressed(): Boolean = false

    protected open fun initializeMap() {}

    fun centerMap(latLng: LatLng?) {
        val cameraPosition = CameraPosition(latLng, EntLocation.lastCameraPosition.zoom, 0F, 0F)
        centerMap(cameraPosition)
    }

    protected fun centerMapAndZoom(latLng: LatLng?, zoom: Float, animated: Boolean) {
        val cameraPosition = CameraPosition(latLng, zoom, 0F, 0F)
        map?.let {
            if (animated) {
                it.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 300, null)
            } else {
                it.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            }
            saveCameraPosition()
        }
    }

    private fun centerMap(cameraPosition: CameraPosition) {
        if (isFollowing) {
            map?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            saveCameraPosition()
        }
    }

    protected open fun saveCameraPosition() {}

    fun initializeMapZoom() {
        EntourageApplication.get().entourageComponent.authenticationController.me?.address?.let {
            centerMap(LatLng(it.latitude, it.longitude))
            isFollowing = false
        } ?: run {
            centerMap(EntLocation.lastCameraPosition)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (index in permissions.indices) {
                if (permissions[index].equals(permission.ACCESS_FINE_LOCATION, ignoreCase = true)) {
                    EntBus.post(OnLocationPermissionGranted(grantResults[index] == PackageManager.PERMISSION_GRANTED))
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    protected fun onMapReady(googleMap: GoogleMap, onGroundOverlayClickListener: OnGroundOverlayClickListener?) {
        map = googleMap
        //we forced the setting of the map anyway
        if (activity == null) {
            Timber.e("No activity found")
            return
        }
        googleMap.isMyLocationEnabled = isLocationPermissionGranted()

        //mylocation is handled in MapViewHolder
        googleMap.uiSettings.isMyLocationButtonEnabled = false
        googleMap.uiSettings.isMapToolbarEnabled = false
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_styles_json))
        if (onGroundOverlayClickListener != null) {
            googleMap.setOnGroundOverlayClickListener(onGroundOverlayClickListener)
        }
        googleMap.setOnMapLongClickListener { latLng: LatLng ->
            //only show when map is in full screen and not visible
            if (!isFullMapShown || fragment_map_longclick?.visibility == View.VISIBLE) {
                return@setOnMapLongClickListener
            }
            if (activity != null) {
                AnalyticsEvents.logEvent(eventLongClick)
                showLongClickOnMapOptions(latLng)
            }
        }
    }

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
        previousCameraLocation = EntLocation.cameraPositionToLocation(null, EntLocation.lastCameraPosition)
        fragment_map_longclick?.setOnClickListener { hideLongClickView() }
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    protected open fun showLongClickOnMapOptions(latLng: LatLng) {
        //get the click point
        map?.let {
            map_longclick_buttons?.let { buttons ->
                buttons.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                val bW = buttons.measuredWidth
                val bH = buttons.measuredHeight
                val lp = buttons.layoutParams as RelativeLayout.LayoutParams
                val clickPoint = it.projection.toScreenLocation(latLng)
                //adjust the buttons holder layout
                val display = (requireContext().getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
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
            fragment_map_longclick?.visibility = View.VISIBLE
        }
    }

    private fun hideLongClickView() = onBackPressed()

    fun showAllowGeolocationDialog(source: Int) {
        activity?.let {
            @StringRes var messagedId = R.string.map_error_geolocation_disabled_use_entourage
            val eventName = when (source) {
                GEOLOCATION_POPUP_RECENTER -> {
                    messagedId = R.string.map_error_geolocation_disabled_recenter
                    AnalyticsEvents.EVENT_FEED_ACTIVATE_GEOLOC_RECENTER
                }
                GEOLOCATION_POPUP_GUIDE_RECENTER -> {
                    messagedId = R.string.map_error_geolocation_disabled_recenter
                    AnalyticsEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_RECENTER
                }
                GEOLOCATION_POPUP_TOUR -> {
                    messagedId = R.string.map_error_geolocation_disabled_create_tour
                    AnalyticsEvents.EVENT_FEED_ACTIVATE_GEOLOC_CREATE_TOUR
                }
                GEOLOCATION_POPUP_GUIDE_BANNER -> AnalyticsEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_FROM_BANNER
                GEOLOCATION_POPUP_BANNER -> AnalyticsEvents.EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER
                else -> AnalyticsEvents.EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER
            }

            AlertDialog.Builder(it)
               .setMessage(messagedId)
               .setPositiveButton(R.string.activate) { _: DialogInterface?, _: Int ->
                   AnalyticsEvents.logEvent(eventName)
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
    }

    fun displayGeolocationPreferences(forceDisplaySettings: Boolean) {
        activity?.let {
            if (forceDisplaySettings || !isLocationEnabled()) {
                it.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else if (!isLocationPermissionGranted()) {
                showAllowGeolocationDialog(GEOLOCATION_POPUP_BANNER)
            }
        }
    }

    open fun onLocationPermissionGranted(event: OnLocationPermissionGranted) = updateGeolocBanner(event.isPermissionGranted)

    protected open fun updateGeolocBanner(active: Boolean) {
        adapter?.setGeolocStatusIcon(isLocationEnabled() && isLocationPermissionGranted())
        try {
            map?.isMyLocationEnabled = isLocationPermissionGranted()
        } catch (ex: SecurityException) {
            Timber.w(ex)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    protected fun onFollowGeolocation() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_RECENTERCLICK)
        // Check if geolocation is enabled
        if (!isLocationEnabled() || !isLocationPermissionGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_RECENTER)
            return
        }
        isFollowing = true
        EntLocation.currentLocation?.let {
            centerMap(LatLng(it.latitude, it.longitude))
        }
    }

    override fun onLocationUpdated(location: LatLng) {}

    override fun onLocationStatusUpdated(active: Boolean) = updateGeolocBanner(active)

    companion object {
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