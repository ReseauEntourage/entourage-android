package social.entourage.android.map;

import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

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
        itemView.forceLayout();
    }

}
