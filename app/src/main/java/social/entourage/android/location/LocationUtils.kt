package social.entourage.android.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.location.LocationManager
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import social.entourage.android.EntourageApplication

object LocationUtils {

    private val locationManager = EntourageApplication.get().applicationContext.getSystemService(Context.LOCATION_SERVICE) as? LocationManager

    fun isLocationPermissionGranted() = isFineLocationPermissionGranted() && isCoarseLocationPermissionGranted()

    private fun isFineLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    private fun isCoarseLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    fun isLocationEnabled() = isFineLocationEnabled() && isCoarseLocationEnabled()

    private fun isFineLocationEnabled(): Boolean {
        locationManager?.let {
            try {
                return it.isProviderEnabled(LocationManager.GPS_PROVIDER)
            } catch (e: IllegalArgumentException) {
            }
        }
        return false
    }

    private fun isCoarseLocationEnabled(): Boolean {
        locationManager?.let {
            try {
                return it.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } catch (e: IllegalArgumentException) {
            }
        }
        return false
    }
}