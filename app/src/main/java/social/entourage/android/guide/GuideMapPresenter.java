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
import social.entourage.android.Constants;
import social.entourage.android.guide.poi.ReadPoiActivity;

/**
 * Presenter controlling the GuideMapActivity
 * @see GuideMapActivity
 */
public class GuideMapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final GuideMapEntourageFragment fragment;
    private final MapRequest mapRequest;

    private OnEntourageMarkerClickListener onClickListener;
    private boolean isStarted = false;

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
        onClickListener = new OnEntourageMarkerClickListener();
        isStarted = true;
        fragment.initializeMapZoom();
        retrieveMapObjects(EntourageLocation.getInstance().getLastCameraPosition().target);
        fragment.setOnMarkerClickListener(onClickListener);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void retrieveMapObjects(LatLng latLng) {
        if(isStarted) {
            mapRequest.map(Constants.TOKEN, 1000, 10, latLng.latitude, latLng.longitude, new Callback<MapResponse>() {
                @Override
                public void success(MapResponse mapResponse, Response response) {
                    loadObjectsOnMap(mapResponse);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("GuideMapActivity", "Impossible to retrieve map objects", error);
                }
            });
        }
    }

    private void loadObjectsOnMap(MapResponse mapResponse) {
        fragment.clearMap();
        for (Poi poi : mapResponse.getPois()) {
            fragment.putPoiOnMap(poi, onClickListener);
        }
    }

    private void openPointOfInterest(Poi poi) {
        fragment.saveCameraPosition();
        Intent intent = new Intent(fragment.getActivity(), ReadPoiActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(Constants.KEY_POI, poi);
        intent.putExtras(extras);
        fragment.startActivity(intent);
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        final Map<LatLng, Poi> poiMarkerHashMap = new HashMap<>();

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
