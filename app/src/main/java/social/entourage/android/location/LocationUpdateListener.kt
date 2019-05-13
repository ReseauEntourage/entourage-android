package social.entourage.android.location

import com.google.android.gms.maps.model.LatLng

interface LocationUpdateListener {

    fun onLocationStatusUpdated(active: Boolean)

    fun onLocationUpdated(location: LatLng)
}