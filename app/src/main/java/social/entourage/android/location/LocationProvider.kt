package social.entourage.android.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import timber.log.Timber
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

class LocationProvider(context: Context,
                       private var userType: UserType = UserType.PUBLIC)
    : GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    companion object {
        const val DURATION_INTERVAL_PUBLIC = 5L
        const val DURATION_INTERVAL_PRO = 20L
        const val DURATION_FAST_INTERVAL_PUBLIC = 1L
        const val DURATION_FAST_INTERVAL_PRO = 10L
    }

    enum class UserType {
        PUBLIC, PRO
    }

    private val context: Context = context.applicationContext
    private val googleApiClient: GoogleApiClient
    private var locationListener: LocationListener? = null
    private var lastKnownLocation: Location? = null
        set(value) {
            locationListener?.onLocationResult(LocationResult.create(listOf(value)))
        }

    private val locationRequest: LocationRequest
        get() = if (UserType.PRO == userType) {
            createLocationRequestForProUsage()
        } else {
            createLocationRequestForPublicUsage()
        }

    init {
        googleApiClient = initializeGoogleApiClient(context.applicationContext)
    }

    private fun initializeGoogleApiClient(context: Context): GoogleApiClient {
        return GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    fun start() {
        if (!googleApiClient.isConnected) {
            googleApiClient.connect()
        } else {
            requestLocationUpdates()
        }
    }

    fun stop() {
        if (googleApiClient.isConnected) {
            googleApiClient.disconnect()
            removeLocationUpdates()
        }
    }

    fun setLocationListener(listener: LocationListener) {
        locationListener = listener
    }

    fun setUserType(newUserType: UserType) {
        if (userType != newUserType) {
            userType = newUserType
            requestLocationUpdates()
        }
    }

    override fun onConnected(bundle: Bundle?) {
        lastKnownLocation?.let { locationListener?.onLocationChanged(it) }
        requestLocationUpdates()
    }

    override fun onConnectionSuspended(i: Int) {
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Timber.e("Cannot connect to Google API Client $connectionResult")
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
        if (isLocationPermissionGranted() && googleApiClient.isConnected) {
            LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(locationRequest, locationListener, null)
        }
    }

    private fun removeLocationUpdates() {
        if (isLocationPermissionGranted() && googleApiClient.isConnected) {
            LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(locationListener)
        }
    }

    private fun createLocationRequestForPublicUsage(): LocationRequest {
        return LocationRequest.create()
                .setInterval(MINUTES.toMillis(DURATION_INTERVAL_PUBLIC))
                .setFastestInterval(MINUTES.toMillis(DURATION_FAST_INTERVAL_PUBLIC))
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    }

    private fun createLocationRequestForProUsage(): LocationRequest {
        return LocationRequest.create()
                .setInterval(SECONDS.toMillis(DURATION_INTERVAL_PRO))
                .setFastestInterval(SECONDS.toMillis(DURATION_FAST_INTERVAL_PRO))
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }
}
