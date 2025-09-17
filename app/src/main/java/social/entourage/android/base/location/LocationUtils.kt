package social.entourage.android.base.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import social.entourage.android.EntourageApplication
import java.util.concurrent.TimeUnit.SECONDS

object LocationUtils {

    const val DURATION_INTERVAL_PUBLIC = 30L
    const val DURATION_FAST_INTERVAL_PUBLIC = 5L

    fun createLocationRequest(): LocationRequest {
        return LocationRequest.create()
            .setInterval(SECONDS.toMillis(DURATION_INTERVAL_PUBLIC))
            .setFastestInterval(SECONDS.toMillis(DURATION_FAST_INTERVAL_PUBLIC))
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
    }

    fun isLocationPermissionGranted() = isFineLocationPermissionGranted() || isCoarseLocationPermissionGranted()

    private fun isFineLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    private fun isCoarseLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED
}