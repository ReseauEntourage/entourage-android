package social.entourage.android.map

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.ui.IconGenerator
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.tools.Utils

/**
 *
 * Created by Mihai Ionescu on 18/09/2018.
 */
abstract class MapClusterItem : ClusterItem {
    abstract fun applyMarkerOptions(context: Context, markerOptions: MarkerOptions)

    override fun getTitle(): String? { return null }

    override fun getSnippet(): String? { return null }
}

class MapClusterEntourageItem(mapEntourage: BaseEntourage) : MapClusterItem() {
    private var entourage: BaseEntourage = mapEntourage

    override fun getPosition(): LatLng {
        return entourage.location?.let {location -> LatLng(location.latitude, location.longitude)} ?: LatLng(0.0,0.0)
    }

    override fun applyMarkerOptions(context: Context, markerOptions: MarkerOptions) {
        val drawable = AppCompatResources.getDrawable(context, entourage.getHeatmapResourceId()) ?: return
        val icon = Utils.getBitmapDescriptorFromDrawable(drawable, BaseEntourage.getMarkerSize(context), BaseEntourage.getMarkerSize(context))
        markerOptions.icon(icon)
    }
}

class MapClusterTourItem(mapTour: Tour) : MapClusterItem() {
    var tour: Tour = mapTour

    override fun getPosition(): LatLng {
        val lastPoint = tour.tourPoints[tour.tourPoints.size - 1]
        return LatLng(lastPoint.latitude, lastPoint.longitude)
    }

    override fun applyMarkerOptions(context: Context, markerOptions: MarkerOptions) {
        val iconGenerator = IconGenerator(context)
        iconGenerator.setTextAppearance(R.style.OngoingTourMarker)
        val icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(tour.organizationName))
        markerOptions.icon(icon)
        markerOptions.anchor(0.5f, 1.0f)
    }
}

class MapClusterEncounterItem(mapEncounter: Encounter) : MapClusterItem() {
    private var encounter: Encounter = mapEncounter

    override fun getPosition(): LatLng {
        return LatLng(encounter.latitude, encounter.longitude)
    }

    override fun applyMarkerOptions(context: Context, markerOptions: MarkerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_encounter))
    }
}