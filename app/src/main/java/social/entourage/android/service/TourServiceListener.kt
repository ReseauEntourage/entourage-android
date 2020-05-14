package social.entourage.android.service

import com.google.android.gms.maps.model.LatLng
import org.jetbrains.annotations.NotNull
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.LocationPoint
import java.util.*

interface TourServiceListener : EntourageServiceListener {

    fun onTourCreated(created: Boolean, tourUUID: String)

    fun onTourUpdated(newPoint: LatLng)

    fun onTourResumed(pointsToDraw: List<LocationPoint>, tourType: String, startDate: Date)

    @NotNull
    fun onRetrieveToursByUserId(tours: List<Tour>)
}