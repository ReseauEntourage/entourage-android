package social.entourage.android.location

import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.api.model.map.TourPoint
import social.entourage.android.tour.TourService
import social.entourage.android.tour.TourServiceManager
import timber.log.Timber

class LocationListener(private val manager: TourServiceManager,
                       private val context: Context)
    : LocationListener, LocationCallback() {

    override fun onLocationChanged(location: Location) {
        Timber.d("onLocationChanged")
        onUpdateLocation(location)
    }

    private fun onUpdateLocation(location: Location) {
        if (manager.entourageLocation.currentLocation == null) {
            manager.entourageLocation.initialLocation = location
        }

        manager.updateLocation(location)
        manager.tourService.notifyListenersPosition(LatLng(location.latitude, location.longitude))

        if (manager.tour != null && !manager.tourService.isPaused) {
            val point = TourPoint(location.latitude, location.longitude, location.accuracy)
            manager.onLocationChanged(location, point)
        }
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

    override fun onProviderEnabled(s: String) {
        context.sendBroadcast(Intent(TourService.KEY_LOCATION_PROVIDER_ENABLED))
    }

    override fun onProviderDisabled(s: String) {
        context.sendBroadcast(Intent(TourService.KEY_LOCATION_PROVIDER_DISABLED))
    }

    override fun onLocationResult(result: LocationResult?) {
        super.onLocationResult(result)
        val location = result?.lastLocation ?: return
        onUpdateLocation(location)
    }

    override fun onLocationAvailability(result: LocationAvailability?) {
        super.onLocationAvailability(result)
        val isLocationAvailable = result?.isLocationAvailable == true
        Timber.d("LocationAvailability changed to %s", isLocationAvailable)
        //val intent = if (isLocationAvailable) Intent(TourService.KEY_LOCATION_PROVIDER_ENABLED) else Intent(TourService.KEY_LOCATION_PROVIDER_DISABLED)
        //context.sendBroadcast(intent)
    }
}