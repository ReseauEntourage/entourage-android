package social.entourage.android.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.location.Location
import android.os.Bundle
import androidx.core.content.PermissionChecker.checkSelfPermission
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import timber.log.Timber
import java.util.concurrent.TimeUnit.MINUTES
import java.util.concurrent.TimeUnit.SECONDS

class LocationProvider(context: Context,
                       private var userType: UserType = UserType.PUBLIC) {

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

    private fun removeLocationUpdates() {
        if (geolocationPermissionIsGranted() && googleApiClient.isConnected) {
            LocationServices.getFusedLocationProviderClient(context).removeLocationUpdates(locationListener)
        }
    }

    private fun initializeGoogleApiClient(context: Context): GoogleApiClient {
        return GoogleApiClient.Builder(context)
                .addConnectionCallbacks(FusedLocationConnectionCallbacks(this))
                .addOnConnectionFailedListener(FusedLocationConnectionFailedListener())
                .addApi(LocationServices.API)
                .build()
    }

    private fun onFusedLocationConnected() {
        lastKnownLocation?.let { locationListener?.onLocationChanged(it) }
        requestLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    fun requestLastKnownLocation() {
        if (geolocationPermissionIsGranted()) {
            LocationServices.getFusedLocationProviderClient(context).lastLocation.addOnSuccessListener {
                lastKnownLocation = it
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        if (geolocationPermissionIsGranted() && googleApiClient.isConnected) {
            LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(locationRequest, locationListener, null)
        }
    }

    private fun createLocationRequestForPublicUsage(): LocationRequest {
        return LocationRequest.create()
                .setInterval(MINUTES.toMillis(5))
                .setFastestInterval(MINUTES.toMillis(1))
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
    }

    private fun createLocationRequestForProUsage(): LocationRequest {
        return LocationRequest.create()
                .setInterval(SECONDS.toMillis(20))
                .setFastestInterval(SECONDS.toMillis(10))
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
    }

    private fun geolocationPermissionIsGranted(): Boolean {
        return checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                && checkSelfPermission(context, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
    }

    enum class UserType {
        PUBLIC, PRO
    }

    private class FusedLocationConnectionCallbacks(private val provider: LocationProvider) : GoogleApiClient.ConnectionCallbacks {

        override fun onConnected(bundle: Bundle?) {
            provider.onFusedLocationConnected()
        }

        override fun onConnectionSuspended(i: Int) {
        }
    }

    private class FusedLocationConnectionFailedListener : GoogleApiClient.OnConnectionFailedListener {
        override fun onConnectionFailed(connectionResult: ConnectionResult) {
            Timber.e("Cannot connect to Google API Client $connectionResult")
        }
    }
}
