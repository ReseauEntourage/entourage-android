package social.entourage.android.service

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.squareup.otto.Subscribe
import social.entourage.android.api.tape.Events.OnBetterLocationEvent
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationListener
import social.entourage.android.base.location.LocationProvider
import social.entourage.android.tools.EntBus
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

    fun unregisterFromBus() {
        try {
            EntBus.unregister(this)
        } catch (e: IllegalArgumentException) {
            Timber.d("No need to unregister")
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    @Subscribe
    fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        if (event.isPermissionGranted) {
            locationProvider.start()
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun updateLocation(location: Location) {
        EntLocation.currentLocation = location
        val bestLocation = EntLocation.location
        var shouldCenterMap = false
        if (bestLocation == null || (location.accuracy > 0.0 && bestLocation.accuracy.toDouble() == 0.0)) {
            EntLocation.location = location
            isBetterLocationUpdated = true
            shouldCenterMap = true
        }
        if (isBetterLocationUpdated) {
            isBetterLocationUpdated = false
            if (shouldCenterMap) {
                EntLocation.latLng?.let { EntBus.post(OnBetterLocationEvent(it))}
            }
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
            EntBus.register(mgr)
            return mgr
        }
    }
}