package social.entourage.android.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import social.entourage.android.common.Constants;
import social.entourage.android.api.MapResponse;
import social.entourage.android.api.MapService;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.encounter.ReadEncounterActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.MapResponse;
import social.entourage.android.api.MapService;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.common.Constants;
import social.entourage.android.encounter.ReadEncounterActivity;
import social.entourage.android.poi.ReadPoiActivity;

public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapActivity activity;
    private final MapService mapService;

    private OnEntourageMarkerClickListener onClickListener;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public MapPresenter(final MapActivity activity, final MapService mapService) {
        this.activity = activity;
        this.mapService = mapService;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void start() {
        onClickListener = new OnEntourageMarkerClickListener();
        activity.initializeMap();
        retrieveMapObjects(MapEntourageFragment.INITIAL_LATITUDE, MapEntourageFragment.INITIAL_LONGITUDE);
        activity.setOnMarkerCLickListener(onClickListener);
    }

    public void retrieveMapObjects(double latitude, double longitude) {
        mapService.map("0cb4507e970462ca0b11320131e96610", 1000, 10, latitude, longitude, new Callback<MapResponse>() {
            @Override
            public void success(MapResponse mapResponse, Response response) {
                loadObjectsOnMap(mapResponse);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("MapActivity", "Impossible to retrieve map objects");
                error.printStackTrace();
            }
        });
    }

    private void loadObjectsOnMap(MapResponse mapResponse) {
        activity.clearMap();
        for (Encounter encounter : mapResponse.getEncounters()) {
            activity.putEncouter(encounter, onClickListener);
        }

        for (Poi poi : mapResponse.getPois()) {
            activity.putPoi(poi, onClickListener);
        }
    }

    public void openEncounter(Encounter encounter) {
        Intent intent = new Intent(activity, ReadEncounterActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(Constants.KEY_ENCOUNTER, encounter);
        intent.putExtras(extras);
        activity.startActivity(intent);
    }

    public void openPointOfInterest(Poi poi) {
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
        Map<LatLng, Encounter> encounterMarkerHashMap = new HashMap<LatLng, Encounter>();
        Map<LatLng, Poi> poiMarkerHashMap = new HashMap<LatLng, Poi>();

        public void addPoiMarker(LatLng markerPosition, Poi poi) {
            poiMarkerHashMap.put(markerPosition, poi);
        }

        public void addEncounterMarker(LatLng markerPosition, Encounter encounter) {
            encounterMarkerHashMap.put(markerPosition, encounter);
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng markerPosition = marker.getPosition();
            if (encounterMarkerHashMap.get(markerPosition) != null){
                openEncounter(encounterMarkerHashMap.get(markerPosition));
            }

            if (poiMarkerHashMap.get(markerPosition) != null){
                openPointOfInterest(poiMarkerHashMap.get(markerPosition));
            }
            return false;
        }
    }
}
