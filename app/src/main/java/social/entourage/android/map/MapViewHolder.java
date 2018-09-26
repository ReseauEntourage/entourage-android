package social.entourage.android.map;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.OnMapReadyCallback;

import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.base.BaseCardViewHolder;

/**
 * Created by mihaiionescu on 27/06/2017.
 */

public class MapViewHolder extends BaseCardViewHolder {

    private ImageButton mFollowButton;
    private EntourageMapView mMapView;
    private View mTabView;

    public MapViewHolder(View view) {
        super(view);
    }

    @Override
    protected void bindFields() {
        mFollowButton = itemView.findViewById(R.id.layout_feed_map_card_follow_button);
        mMapView = itemView.findViewById(R.id.layout_feed_map_card_mapview);
        mTabView = itemView.findViewById(R.id.layout_feed_map_card_tab);
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
        if (mFollowButton != null) {
            mFollowButton.setOnClickListener(listener);
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
        RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams)mMapView.getLayoutParams();
        layout.topMargin = (height - layout.height)/2;
        mMapView.setLayoutParams(layout);

        itemView.forceLayout();
    }

    public void setTabVisibility(int visibility) {
        if (mTabView != null) mTabView.setVisibility(visibility);
    }

    public void setSelectedTab(MapTabItem selectedTab) {
        if (mTabView != null && mTabView instanceof IMapTabView) {
            ((IMapTabView)mTabView).setSelectedTab(selectedTab);
        }
    }

}
