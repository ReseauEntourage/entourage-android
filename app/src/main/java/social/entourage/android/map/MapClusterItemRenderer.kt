package social.entourage.android.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * Created by Mihai Ionescu on 17/09/2018.
 */
class MapClusterItemRenderer(private val context: Context, map: GoogleMap?, clusterManager: ClusterManager<MapClusterItem?>?) : DefaultClusterRenderer<MapClusterItem?>(context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: MapClusterItem?, markerOptions: MarkerOptions) {
        item?.applyMarkerOptions(context, markerOptions)
    }

}