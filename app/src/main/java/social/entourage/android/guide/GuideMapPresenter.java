package social.entourage.android.guide;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.MapResponse;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.common.Constants;
import social.entourage.android.poi.ReadPoiActivity;

public class GuideMapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final GuideMapActivity activity;
    private final MapRequest mapRequest;

    private OnEntourageMarkerClickListener onClickListener;
    private boolean isStarted = false;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public GuideMapPresenter(final GuideMapActivity activity, final MapRequest mapRequest) {
        this.activity = activity;
        this.mapRequest = mapRequest;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void start() {
        onClickListener = new OnEntourageMarkerClickListener();
        isStarted = true;
        activity.initializeMap();
        retrieveMapObjects(EntourageLocation.getInstance().getLastCameraPosition().target);
        activity.setOnMarkerCLickListener(onClickListener);
    }

    public void retrieveMapObjects(LatLng latLng) {
        if(isStarted) {
            mapRequest.map("0cb4507e970462ca0b11320131e96610", 1000, 10, latLng.latitude, latLng.longitude, new Callback<MapResponse>() {
                @Override
                public void success(MapResponse mapResponse, Response response) {
                    loadObjectsOnMap(mapResponse);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("GuideMapActivity", "Impossible to retrieve map objects");
                    error.printStackTrace();
                }
            });
        } else {
            //Map is not initialized so we just store the proper camera location
            //EntourageLocation.getInstance().resetCameraPosition(latLng);
        }
    }

    private void loadObjectsOnMap(MapResponse mapResponse) {
        activity.clearMap();
        for (Poi poi : mapResponse.getPois()) {
            activity.putPoi(poi, onClickListener);
        }
    }

    public void openPointOfInterest(Poi poi) {
        activity.saveCameraPosition();
        Intent intent = new Intent(activity, ReadPoiActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(Constants.KEY_POI, poi);
        intent.putExtras(extras);
        activity.startActivity(intent);
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        Map<LatLng, Poi> poiMarkerHashMap = new HashMap<LatLng, Poi>();

        public void addPoiMarker(LatLng markerPosition, Poi poi) {
            poiMarkerHashMap.put(markerPosition, poi);
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng markerPosition = marker.getPosition();
            if (poiMarkerHashMap.get(markerPosition) != null){
                openPointOfInterest(poiMarkerHashMap.get(markerPosition));
            }
            return false;
        }
    }
}
