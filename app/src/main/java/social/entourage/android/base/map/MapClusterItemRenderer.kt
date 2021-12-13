package social.entourage.android.base.map

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer

/**
 * Created by Mihai Ionescu on 17/09/2018.
 */
class MapClusterItemRenderer(private val context: Context, map: GoogleMap?, clusterManager: ClusterManager<ClusterItem>?) : DefaultClusterRenderer<ClusterItem>(context, map, clusterManager) {
    override fun onBeforeClusterItemRendered(item: ClusterItem, markerOptions: MarkerOptions) {
        if(item is MapClusterItem) item.applyMarkerOptions(context, markerOptions)
    }
}