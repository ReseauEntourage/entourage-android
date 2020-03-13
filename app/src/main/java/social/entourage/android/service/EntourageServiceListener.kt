package social.entourage.android.service

import com.google.android.gms.maps.model.LatLng
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.Tour
import social.entourage.android.api.model.map.TourPoint
import social.entourage.android.api.model.map.TourUser
import social.entourage.android.location.LocationUpdateListener
import java.util.*

interface EntourageServiceListener : LocationUpdateListener {

    fun onFeedItemClosed(closed: Boolean, feedItem: FeedItem)

    fun onUserStatusChanged(user: TourUser, feedItem: FeedItem)
}