package social.entourage.android.base.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import social.entourage.android.base.location.LocationUtils.isLocationPermissionGranted
import java.util.concurrent.TimeUnit.SECONDS

class LocationProvider(
    context: Context
) {

    companion object {
        const val DURATION_INTERVAL_PUBLIC = 30L
        const val DURATION_FAST_INTERVAL_PUBLIC = 1L
    }

    private val context: Context = context.applicationContext

    //private val googleApiClient: GoogleApiClient
    private var locationListener: LocationListener? = null
    private var lastKnownLocation: Location? = null
        set(value) {
            locationListener?.onLocationResult(LocationResult.create(listOf(value)))
        }

    val locationRequest: LocationRequest
        get() = createLocationRequestForPublicUsage()

    init {
        lastKnownLocation?.let { locationListener?.onLocationChanged(it) }
        requestLocationUpdates()
    }

    fun start() {
        requestLocationUpdates()
    }

    fun stop() {
        removeLocationUpdates()
    }

    fun setLocationListener(listener: LocationListener) {
        locationListener = listener
    }

    @SuppressLint("MissingPermission")
    fun requestLastKnownLocation() {
        if (isLocationPermissionGranted()) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
                lastKnownLocation = it
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (isLocationPermissionGranted()) {
            //TODO add a looper here
            locationListener?.let {
                LocationServices.getFusedLocationProviderClient(context)
                    .requestLocationUpdates(locationRequest, it, null)
            }
        }
    }

    private fun removeLocationUpdates() {
        if (isLocationPermissionGranted() && locationListener != null) {
            LocationServices.getFusedLocationProviderClient(context)
                .removeLocationUpdates(locationListener!!)
        }
    }

    private fun createLocationRequestForPublicUsage(): LocationRequest {
        return LocationRequest.create()
            .setInterval(SECONDS.toMillis(DURATION_INTERVAL_PUBLIC))
            .setFastestInterval(SECONDS.toMillis(DURATION_FAST_INTERVAL_PUBLIC))
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    }
}
