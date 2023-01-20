package social.entourage.android.base.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import social.entourage.android.EntourageApplication

object LocationUtils {

    fun isLocationPermissionGranted() = isFineLocationPermissionGranted() || isCoarseLocationPermissionGranted()

    private fun isFineLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    private fun isCoarseLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
}