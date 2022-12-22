package social.entourage.android.old_v7.base.location

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import social.entourage.android.base.location.EntLocation
import social.entourage.android.old_v7.service.EntServiceManager_v7
import social.entourage.android.old_v7.service.EntService_v7
import timber.log.Timber

class LocationListener_v7(private val manager: EntServiceManager_v7,
                          private val context: Context)
    : LocationListener, LocationCallback() {

    override fun onLocationChanged(location: Location) {
        Timber.d("onLocationChanged")
        onUpdateLocation(location)
    }

    private fun onUpdateLocation(location: Location) {
        if (EntLocation.currentLocation == null) {
            EntLocation.initialLocation = location
        }

        manager.updateLocation(location)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

    override fun onProviderEnabled(s: String) {
        context.sendBroadcast(Intent(EntService_v7.KEY_LOCATION_PROVIDER_ENABLED))
    }

    override fun onProviderDisabled(s: String) {
        context.sendBroadcast(Intent(EntService_v7.KEY_LOCATION_PROVIDER_DISABLED))
    }

    override fun onLocationResult(result: LocationResult) {
        super.onLocationResult(result)
        result.lastLocation?.let { onUpdateLocation(it) }
    }

    override fun onLocationAvailability(result: LocationAvailability) {
        super.onLocationAvailability(result)
        val isLocationAvailable = result.isLocationAvailable
        Timber.d("LocationAvailability changed to %s", isLocationAvailable)
        val intent = Intent(if(result.isLocationAvailable) EntService_v7.KEY_LOCATION_PROVIDER_ENABLED else EntService_v7.KEY_LOCATION_PROVIDER_DISABLED)
        context.sendBroadcast(intent)
    }
}