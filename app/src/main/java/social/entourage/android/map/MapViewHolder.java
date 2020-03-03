package social.entourage.android.map;

import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;

/**
 * Created by mihaiionescu on 27/06/2017.
 */

public class MapViewHolder extends BaseCardViewHolder {

    private FloatingActionButton mGeolocRecenterButton;
    private EntourageMapView mMapView;

    public MapViewHolder(View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        mGeolocRecenterButton = itemView.findViewById(R.id.layout_feed_map_card_recenter_button);
        mMapView = itemView.findViewById(R.id.layout_feed_map_card_mapview);
        //Force the map to full height, even if the view holder is smaller
        RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams)mMapView.getLayoutParams();
        DisplayMetrics displayMetrics = itemView.getContext().getResources().getDisplayMetrics();
        layout.height = displayMetrics.heightPixels;
        //Move the map up, so that the center of the map is visible
        int itemViewHeight = itemView.getLayoutParams().height;
        if (itemViewHeight > 0) {
            layout.topMargin = (itemViewHeight - layout.height) / 2;
        }
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

    public void setFollowButtonOnClickListener(View.OnClickListener listener) {
        if (mGeolocRecenterButton != null) {
            mGeolocRecenterButton.setOnClickListener(listener);
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

    public void setGeolocStatusIcon(boolean active) {
        mGeolocRecenterButton.setVisibility(active?View.VISIBLE:View.INVISIBLE);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mGeolocRecenterButton.setImageDrawable(AppCompatResources.getDrawable(this.mGeolocRecenterButton.getContext(), active?R.drawable.ic_my_location:R.drawable.ic_my_location_off));
        } else {
            mGeolocRecenterButton.setSelected(active);
        }
    }

    public void setHeight(int height) {
        itemView.getLayoutParams().height = height;

        //Move the map so that the center is visible
        RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams)mMapView.getLayoutParams();
        layout.topMargin = (height - layout.height)/2;
        mMapView.setLayoutParams(layout);

        itemView.forceLayout();
    }

}
