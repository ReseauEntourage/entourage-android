package social.entourage.android.service

import com.google.android.gms.maps.model.LatLng
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.Tour
import social.entourage.android.api.model.map.TourPoint
import social.entourage.android.api.model.map.TourUser
import social.entourage.android.location.LocationUpdateListener
import java.util.*

interface TourServiceListener : EntourageServiceListener {

    fun onTourCreated(created: Boolean, tourUUID: String)

    fun onTourUpdated(newPoint: LatLng)

    fun onTourResumed(pointsToDraw: List<TourPoint>, tourType: String, startDate: Date)

    fun onRetrieveToursNearby(tours: List<Tour>)

    fun onRetrieveToursByUserId(tours: List<Tour>)

    fun onUserToursFound(tours: Map<Long, Tour>)

    fun onToursFound(tours: Map<Long, Tour>)
}