package social.entourage.android.base.location

import com.google.android.gms.maps.model.LatLng

interface LocationUpdateListener {

    fun onLocationStatusUpdated()

    fun onLocationUpdated(location: LatLng)
}