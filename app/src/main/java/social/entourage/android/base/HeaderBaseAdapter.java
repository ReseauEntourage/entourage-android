package social.entourage.android.base;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.OnMapReadyCallback;

import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.map.MapViewHolder;

public class HeaderBaseAdapter extends EntourageBaseAdapter {

    protected MapViewHolder mapViewHolder;
    private OnMapReadyCallback onMapReadyCallback;
    private View.OnClickListener onFollowButtonClickListener;

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (position == 0) {//header position
            if(mapViewHolder == null && holder instanceof MapViewHolder) {
                mapViewHolder = (MapViewHolder)holder;
            }
            //we populate with  no data
            mapViewHolder.populate();
            return;
        }
        super.onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(final int position) {
        if (position == 0) {
            return TimestampedObject.TOP_VIEW;
        }
        return super.getItemViewType(position);
    }

    public void setMapHeight(int height) {
        if (mapViewHolder != null) {
            mapViewHolder.setHeight(height);
        }
    }

    public void setGeolocStatusIcon(boolean visible) {
        if (mapViewHolder != null) {
            mapViewHolder.setGeolocStatusIcon(visible);
        }
    }

    public void displayGeolocStatusIcon(boolean active) {
        if (mapViewHolder != null) {
            mapViewHolder.displayGeolocStatusIcon(active);
        }
    }

    @Override
    protected int getPositionOffset() {
        return 1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {

        RecyclerView.ViewHolder cardViewHolder = super.onCreateViewHolder(parent,viewType);
        if (cardViewHolder instanceof MapViewHolder && viewType == TimestampedObject.TOP_VIEW) {
            mapViewHolder = (MapViewHolder)cardViewHolder;
            mapViewHolder.setMapReadyCallback(onMapReadyCallback);
            mapViewHolder.setFollowButtonOnClickListener(onFollowButtonClickListener);
            mapViewHolder.setGeolocStatusIcon(LocationUtils.INSTANCE.isLocationEnabled() && LocationUtils.INSTANCE.isLocationPermissionGranted());
        }

        return cardViewHolder;
    }

    public void setOnMapReadyCallback(final OnMapReadyCallback onMapReadyCallback) {
        this.onMapReadyCallback = onMapReadyCallback;
    }

    public void setOnFollowButtonClickListener(final View.OnClickListener onFollowButtonClickListener) {
        this.onFollowButtonClickListener = onFollowButtonClickListener;
    }

}