package social.entourage.android.base.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.base.HeaderBaseAdapter
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUpdateListener
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.databinding.FragmentMapBinding
import social.entourage.android.databinding.LayoutMapLongclickBinding
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

abstract class BaseMapFragment() : Fragment(),
    LocationUpdateListener {
    private lateinit var binding: FragmentMapBinding
    protected lateinit var eventLongClick: String
    var isFollowing = true
    protected var isFullMapShown = true
    protected var previousCameraLocation: Location? = null
    protected var previousCameraZoom = 1.0f
    var map: GoogleMap? = null
    protected abstract val adapter: HeaderBaseAdapter?

    protected var originalMapLayoutHeight = 0

    protected val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if(permissions.entries.any {
                it.value == true
            }) {
                RefreshController.shouldRefreshLocationPermission = true
                onLocationPermissionGranted(true)
            }
        }

    protected open fun initializeMap() {}

    fun centerMap(latLng: LatLng) {
        val cameraPosition = CameraPosition(latLng, EntLocation.lastCameraPosition.zoom, 0F, 0F)
        centerMap(cameraPosition)
    }

    protected fun centerMapAndZoom(latLng: LatLng, zoom: Float, animated: Boolean) {
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
        EntourageApplication.get().authenticationController.me?.address?.let {
            centerMap(LatLng(it.latitude, it.longitude))
            isFollowing = false
        } ?: run {
            centerMap(EntLocation.lastCameraPosition)
        }
    }

    @SuppressLint("MissingPermission")
    protected fun onMapReady(
        googleMap: GoogleMap,
        onGroundOverlayClickListener: GoogleMap.OnGroundOverlayClickListener?
    ) {
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
        if (onGroundOverlayClickListener != null) {
            googleMap.setOnGroundOverlayClickListener(onGroundOverlayClickListener)
        }
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        previousCameraLocation =
            EntLocation.cameraPositionToLocation(null, EntLocation.lastCameraPosition)
    }

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
                GEOLOCATION_POPUP_GUIDE_BANNER -> AnalyticsEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_FROM_BANNER
                GEOLOCATION_POPUP_BANNER -> AnalyticsEvents.EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER
                else -> AnalyticsEvents.EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER
            }

            AlertDialog.Builder(it)
                .setMessage(messagedId)
                .setPositiveButton(R.string.activate) { _: DialogInterface?, _: Int ->
                    AnalyticsEvents.logEvent(eventName)
                    try {
                        if (LocationUtils.isLocationPermissionGranted()
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

    /*fun displayGeolocationPreferences(forceDisplaySettings: Boolean) {
        activity?.let {
            if (forceDisplaySettings) {
                it.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            } else if (!isLocationPermissionGranted()) {
                showAllowGeolocationDialog(GEOLOCATION_POPUP_BANNER)
            }
        }
    }*/

    open fun onLocationPermissionGranted(isPermissionGranted: Boolean) =
        updateGeolocBanner(isPermissionGranted)

    protected open fun updateGeolocBanner(active: Boolean) {
        adapter?.setGeolocStatusIcon(LocationUtils.isLocationPermissionGranted())
        try {
            map?.isMyLocationEnabled = LocationUtils.isLocationPermissionGranted()
        } catch (ex: SecurityException) {
            Timber.w(ex)
        } catch (ex: Exception) {
            Timber.e(ex)
        }
    }

    protected fun onFollowGeolocation() {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_RECENTERCLICK)
        // Check if geolocation is enabled
        if (!LocationUtils.isLocationPermissionGranted()) {
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
        private const val GEOLOCATION_POPUP_RECENTER = 1
        const val GEOLOCATION_POPUP_BANNER = 2
        private const val GEOLOCATION_POPUP_GUIDE_RECENTER = 3
        private const val GEOLOCATION_POPUP_GUIDE_BANNER = 4
        const val ZOOM_REDRAW_LIMIT = 1.1f
        const val REDRAW_LIMIT = 300
    }
}