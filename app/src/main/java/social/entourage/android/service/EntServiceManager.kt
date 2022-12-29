package social.entourage.android.service

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.RefreshController
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationListener
import social.entourage.android.base.location.LocationProvider
import timber.log.Timber

/**
 * Manager is like a presenter but for a service
 * controlling the EntourageService
 *
 * @see EntService
 */
class EntServiceManager(
    val entService: EntService,
    val authenticationController: AuthenticationController,
    private val locationProvider: LocationProvider
) {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var isBetterLocationUpdated = false

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun stopLocationService() {
        locationProvider.stop()
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    fun onLocationPermissionGranted(isPermissionGranted: Boolean) {
        if (isPermissionGranted) {
            locationProvider.start()
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun updateLocation(location: Location) {
        EntLocation.currentLocation = location
        val bestLocation = EntLocation.location
        if (bestLocation == null || (location.accuracy > 0.0 && bestLocation.accuracy.toDouble() == 0.0)) {
            EntLocation.location = location
            isBetterLocationUpdated = true
        }

        entService.notifyListenersPosition(LatLng(location.latitude, location.longitude))
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        fun newInstance(entService: EntService,
                        authenticationController: AuthenticationController): EntServiceManager {
            val provider = LocationProvider(entService)
            val mgr = EntServiceManager(
                    entService,
                    authenticationController,
                    provider)
            provider.locationListener = LocationListener(mgr, entService)
            provider.start()
            return mgr
        }
    }
}