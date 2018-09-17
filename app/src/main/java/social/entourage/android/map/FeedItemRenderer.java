package social.entourage.android.map;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.tools.Utils;

/**
 * Created by Mihai Ionescu on 17/09/2018.
 */
public class FeedItemRenderer extends DefaultClusterRenderer<FeedItem> {

    private Context context;

    public FeedItemRenderer(final Context context, final GoogleMap map, final ClusterManager<FeedItem> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(final FeedItem item, final MarkerOptions markerOptions) {
        Drawable drawable = context.getResources().getDrawable(item.getHeatmapResourceId());
        BitmapDescriptor icon = Utils.getBitmapDescriptorFromDrawable(drawable, Entourage.getMarkerSize(context), Entourage.getMarkerSize(context));
        markerOptions.icon(icon);
    }
}
