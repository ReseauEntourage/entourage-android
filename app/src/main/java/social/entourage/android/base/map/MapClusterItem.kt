package social.entourage.android.base.map

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.tools.UtilsV7

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
        val icon = UtilsV7.getBitmapDescriptorFromDrawable(drawable, BaseEntourage.getMarkerSize(context), BaseEntourage.getMarkerSize(context))
        markerOptions.icon(icon)
    }
}