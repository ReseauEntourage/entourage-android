package social.entourage.android.service

import com.google.android.gms.maps.model.LatLng
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.map.LocationPoint
import java.util.*

interface TourServiceListener : EntourageServiceListener {

    fun onTourCreated(created: Boolean, tourUUID: String)

    fun onTourUpdated(newPoint: LatLng)

    fun onTourResumed(pointsToDraw: List<LocationPoint>, tourType: String, startDate: Date)

    fun onRetrieveToursByUserId(tours: List<Tour>)
}