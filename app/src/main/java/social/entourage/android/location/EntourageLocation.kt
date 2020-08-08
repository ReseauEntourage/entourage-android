package social.entourage.android.location

import android.location.Location
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.EntourageApplication.Companion.get

object EntourageLocation {
    @JvmStatic
    var location: Location? = null
    @JvmStatic
    var currentLocation: Location? = null
    var initialLocation: Location? = null
    var lastCameraPosition: CameraPosition
    @JvmStatic
    var currentCameraPosition: CameraPosition

    /*fun setInitialLocation(initialLocation: Location) {
        this.initialLocation = initialLocation
        //saveCurrentLocation(initialLocation)
        //currentCameraPosition = CameraPosition(LatLng(initialLocation.latitude, initialLocation.longitude), INITIAL_CAMERA_FACTOR, 0F, 0F)
        //lastCameraPosition = currentCameraPosition
    }*/

    @JvmStatic
    val latLng: LatLng?
        get() = location?.let {LatLng(it.latitude, it.longitude)}

    val currentLatLng: LatLng?
        get() = currentLocation?.let { LatLng(it.latitude, it.longitude) }

    private const val INITIAL_LATITUDE = 48.841636
    private const val INITIAL_LONGITUDE = 2.335899
    const val INITIAL_CAMERA_FACTOR = 15f
    const val INITIAL_CAMERA_FACTOR_ENTOURAGE_VIEW = 14f

    fun cameraPositionToLocation(provider: String?, cameraPosition: CameraPosition): Location {
        val location = Location(provider)
        location.latitude = cameraPosition.target.latitude
        location.longitude = cameraPosition.target.longitude
        return location
    }

    init {
        val address = get().entourageComponent.authenticationController.me?.address
        val lat = LatLng(address?.latitude ?: INITIAL_LATITUDE, address?.longitude ?: INITIAL_LONGITUDE)
        lastCameraPosition = CameraPosition(lat, INITIAL_CAMERA_FACTOR, 0.0F, 0.0F)
        currentCameraPosition = CameraPosition(lat, INITIAL_CAMERA_FACTOR, 0.0F, 0.0F)
    }
}