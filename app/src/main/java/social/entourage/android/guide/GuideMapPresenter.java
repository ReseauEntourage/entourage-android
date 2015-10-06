package social.entourage.android.guide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.MapResponse;
import social.entourage.android.Constants;
import social.entourage.android.guide.poi.ReadPoiActivity;

/**
 * Presenter controlling the GuideMapEntourageFragment
 * @see GuideMapEntourageFragment
 */
public class GuideMapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final GuideMapEntourageFragment fragment;
    private final MapRequest mapRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public GuideMapPresenter(final GuideMapEntourageFragment fragment, final MapRequest mapRequest) {
        this.fragment = fragment;
        this.mapRequest = mapRequest;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void start() {
        fragment.initializeMapZoom();
        updatePoisNearby();
    }

    public void updatePoisNearby() {
        retrievePoisNearby();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void retrievePoisNearby() {
        CameraPosition currentPosition = EntourageLocation.getInstance().getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            float zoom = currentPosition.zoom;
            float distance = 40000f / (float) Math.pow(2f, zoom) / 2.5f;
            distance = Math.max(1, distance);
            mapRequest.retrievePoisNearby(location.latitude, location.longitude, distance, new Callback<MapResponse>() {
                @Override
                public void success(MapResponse mapResponse, Response response) {
                    fragment.putPoiOnMap(mapResponse.getPois());
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("GuideMapEntourageFrag", "Impossible to retrieve POIs", error);
                }
            });
        }
    }
}
