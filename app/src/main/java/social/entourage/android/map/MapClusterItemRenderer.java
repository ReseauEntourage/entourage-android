package social.entourage.android.map;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;


/**
 * Created by Mihai Ionescu on 17/09/2018.
 */
public class MapClusterItemRenderer extends DefaultClusterRenderer<MapClusterItem> {

    private Context context;

    public MapClusterItemRenderer(final Context context, final GoogleMap map, final ClusterManager<MapClusterItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(final MapClusterItem item, final MarkerOptions markerOptions) {
        if (item != null) {
            item.applyMarkerOptions(context, markerOptions);
        }
    }
}
