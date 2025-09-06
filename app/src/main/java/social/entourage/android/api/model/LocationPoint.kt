package social.entourage.android.api.model

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.Date

class LocationPoint : Serializable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    var latitude: Double
    var longitude: Double
    var accuracy: Float

    @SerializedName("passing_time")
    private var passingTime: Date

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------
    constructor(latitude: Double, longitude: Double) {
        this.latitude = latitude
        this.longitude = longitude
        accuracy = 0.0f
        passingTime = Date()
    }

    constructor(latitude: Double, longitude: Double, accuracy: Float) {
        this.latitude = latitude
        this.longitude = longitude
        this.accuracy = accuracy
        passingTime = Date()
    }

    val location: LatLng
        get() = LatLng(latitude, longitude)

    // ----------------------------------
    // HELPERS
    // ----------------------------------
    fun distanceTo(otherPoint: LocationPoint?): Float {
        if (otherPoint == null) return 0f
        val result = floatArrayOf(0f)
        Location.distanceBetween(latitude, longitude, otherPoint.latitude, otherPoint.longitude, result)
        return result[0]
    }

    fun distanceToCurrentLocation(maxDistanceToShow: Float, currentLocation: LocationPoint?): String {
        currentLocation?.let {currentLocation ->
            val distance = distanceTo(currentLocation)
            if (distance < maxDistanceToShow) {
                return if (distance < 1000.0f) {
                    String.format("%.0f m", distance)
                } else if (distance < 10000.0f) {
                    String.format("%.1f km", distance / 1000.0f)
                } else {
                    String.format("%.0f km", distance / 1000.0f)
                }
            }
        }
        return ""
    }

    companion object {
        private const val serialVersionUID = -5620241951845606404L
    }
}