package social.entourage.android.base.location

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Handler
import android.os.Looper
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
        return LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, SECONDS.toMillis(DURATION_INTERVAL_PUBLIC))
            .setMinUpdateIntervalMillis(SECONDS.toMillis(DURATION_FAST_INTERVAL_PUBLIC))
            .build()
    }

    fun isLocationPermissionGranted() = isFineLocationPermissionGranted() || isCoarseLocationPermissionGranted()

    private fun isFineLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
    private fun isCoarseLocationPermissionGranted() = checkSelfPermission(EntourageApplication.get().applicationContext, ACCESS_COARSE_LOCATION) == PERMISSION_GRANTED

    /**
     * Reverse-geocodes [latitude]/[longitude] into a list of addresses, using the async
     * [Geocoder] API on API 33+ (the synchronous overload is deprecated there) and falling
     * back to the blocking call below. [onResult] is always delivered on the main thread.
     */
    fun Geocoder.getFromLocationCompat(
        latitude: Double,
        longitude: Double,
        maxResults: Int,
        onResult: (List<Address>?) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getFromLocation(latitude, longitude, maxResults) { addresses ->
                Handler(Looper.getMainLooper()).post { onResult(addresses) }
            }
        } else {
            @Suppress("DEPRECATION")
            onResult(getFromLocation(latitude, longitude, maxResults))
        }
    }

    /**
     * Geocodes [locationName] into a list of addresses, using the async [Geocoder] API on
     * API 33+ (the synchronous overload is deprecated there) and falling back to the blocking
     * call below. [onResult] is always delivered on the main thread.
     */
    fun Geocoder.getFromLocationNameCompat(
        locationName: String,
        maxResults: Int,
        onResult: (List<Address>?) -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getFromLocationName(locationName, maxResults) { addresses ->
                Handler(Looper.getMainLooper()).post { onResult(addresses) }
            }
        } else {
            @Suppress("DEPRECATION")
            onResult(getFromLocationName(locationName, maxResults))
        }
    }
}