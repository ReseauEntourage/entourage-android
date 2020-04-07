package social.entourage.android.map

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.ui.IconGenerator
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.Encounter
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.Tour
import social.entourage.android.tools.Utils

/**
 *
 * Created by Mihai Ionescu on 18/09/2018.
 */
class MapClusterItem : ClusterItem {
    var mapItem: TimestampedObject?
        private set

    constructor(feedItem: FeedItem?) {
        mapItem = feedItem
    }

    constructor(encounter: Encounter?) {
        mapItem = encounter
    }

    override fun getPosition(): LatLng? {
        if (mapItem != null) {
            if (mapItem is FeedItem) {
                val feedItem = mapItem as FeedItem
                if (feedItem.type == FeedItem.TOUR_CARD) {
                    val tour = mapItem as Tour
                    if (tour.tourPoints != null) {
                        val lastPoint = tour.tourPoints[tour.tourPoints.size - 1]
                        if (lastPoint != null) {
                            return LatLng(lastPoint.latitude, lastPoint.longitude)
                        }
                    }
                } else if (feedItem.type == FeedItem.ENTOURAGE_CARD) {
                    val location = (feedItem as Entourage).location
                    if (location != null) {
                        return LatLng(location.latitude, location.longitude)
                    }
                }
            } else if (mapItem is Encounter) {
                val encounter = mapItem as Encounter
                return LatLng(encounter.latitude, encounter.longitude)
            }
        }
        return null
    }

    override fun getTitle(): String? {
        return null
    }

    override fun getSnippet(): String? {
        return null
    }

    fun applyMarkerOptions(context: Context?, markerOptions: MarkerOptions) {
        if (mapItem == null) return
        if (mapItem is FeedItem) {
            val feedItem = mapItem as FeedItem
            if (feedItem.type == FeedItem.TOUR_CARD) {
                val iconGenerator = IconGenerator(context)
                iconGenerator.setTextAppearance(R.style.OngoingTourMarker)
                val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon((feedItem as Tour).organizationName))
                markerOptions.icon(icon)
                markerOptions.anchor(0.5f, 1.0f)
            } else if (feedItem.type == FeedItem.ENTOURAGE_CARD) {
                val drawable = AppCompatResources.getDrawable(context!!, (mapItem as Entourage).heatmapResourceId)
                val icon = Utils.getBitmapDescriptorFromDrawable(drawable!!, Entourage.getMarkerSize(context), Entourage.getMarkerSize(context))
                markerOptions.icon(icon)
            }
        } else if (mapItem is Encounter) {
            val encounterIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_encounter)
            markerOptions.icon(encounterIcon)
        }
    }
}