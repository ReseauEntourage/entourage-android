package social.entourage.android.map;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.maps.OnMapReadyCallback;

import social.entourage.android.EntourageActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;

/**
 * Created by mihaiionescu on 27/06/2017.
 */

public class MapViewHolder extends BaseCardViewHolder {

    public EntourageMapView mMapView;

    public MapViewHolder(View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        mMapView = (EntourageMapView) itemView.findViewById(R.id.layout_feed_map_card_mapview);
        //Force the map to full height, even if the view holder is smaller
        LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams)mMapView.getLayoutParams();
        DisplayMetrics displayMetrics = itemView.getContext().getResources().getDisplayMetrics();
        layout.height = displayMetrics.heightPixels;
        //Move the map up, so that the center of the map is visible
        layout.topMargin = (itemView.getLayoutParams().height - layout.height)/2;
        mMapView.setLayoutParams(layout);
        //Inform the map that it needs to start
        mapViewOnCreate(null);
    }

    @Override
    public void populate(final TimestampedObject data) {
        mapViewOnResume();
    }

    public static int getLayoutResource() {
        return R.layout.layout_feed_map_card;
    }

    public void setMapReadyCallback(OnMapReadyCallback callback) {
        if (mMapView != null) {
            mMapView.getMapAsync(callback);
        }
    }

    public void mapViewOnCreate(Bundle savedInstanceState) {
        if (mMapView != null) {
            mMapView.onCreate(savedInstanceState);
        }
    }
    public void mapViewOnResume() {
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    public void setHeight(int height) {
        itemView.getLayoutParams().height = height;

        //Move the map so that the center is visible
        LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams)mMapView.getLayoutParams();
        layout.topMargin = (height - layout.height)/2;
        mMapView.setLayoutParams(layout);

        itemView.forceLayout();
    }

}
