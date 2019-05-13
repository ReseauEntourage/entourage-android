package social.entourage.android.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import social.entourage.android.EntourageApplication

object LocationPermissionUtils {
    fun isGeolocationPermissionGranted(): Boolean {
        return checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
                && checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
    }
}