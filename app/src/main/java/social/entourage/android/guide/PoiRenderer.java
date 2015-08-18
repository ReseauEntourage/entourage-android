package social.entourage.android.guide;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import social.entourage.android.api.model.map.Poi;

public class PoiRenderer extends DefaultClusterRenderer<Poi> {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final String POI_DRAWABLE_NAME_PREFIX = "poi_category_";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final Context context;

    // ----------------------------------
    // CONSTRUCTORS
    // ----------------------------------

    public PoiRenderer(Context context, GoogleMap map, ClusterManager<Poi> clusterManager) {
        super(context, map, clusterManager);
        this.context = context;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    @Override
    protected void onBeforeClusterItemRendered(Poi poi, MarkerOptions markerOptions) {
        int poiIconResId = context.getResources().getIdentifier(POI_DRAWABLE_NAME_PREFIX + poi.getCategoryId(), "drawable", context.getPackageName());
        BitmapDescriptor poiIcon = BitmapDescriptorFactory.fromResource(poiIconResId);
        markerOptions.icon(poiIcon);
    }
}
