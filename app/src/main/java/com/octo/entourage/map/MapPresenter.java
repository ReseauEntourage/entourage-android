package com.octo.entourage.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import com.octo.entourage.encounter.EncounterActivity;
import com.octo.entourage.common.Constants;
import com.octo.entourage.api.MapResponse;
import com.octo.entourage.api.MapService;
import com.octo.entourage.api.model.map.Encounter;
import com.octo.entourage.api.model.map.Poi;
import com.octo.entourage.encounter.EncounterActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapActivity activity;
    private final MapService mapService;

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
        Encounter encounter = new Encounter();
        encounter.setLatitude(42);
        encounter.setLongitude(2);

        Poi poi = new Poi();
        poi.setLatitude(43);
        poi.setLongitude(2);
        poi.setCategoryId(4);

        retrieveMapObjects();

        OnEntourageMarkerClickListener onClickListener = new OnEntourageMarkerClickListener();

        activity.setOnMarkerCLickListener(onClickListener);
        activity.putEncouter(encounter, onClickListener);
        activity.putPoi(poi, onClickListener);
    }

    public void retrieveMapObjects() {
        mapService.map("07ee026192ea722e66feb2340a05e3a8", 10, 10, 42.1, 2.1, new Callback<MapResponse>() {
            @Override
            public void success(MapResponse mapResponse, Response response) {
                Log.d("Success", "Success");
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("Failure", "Failure");
            }
        });
    }

    public void openEncounter(Encounter encounter) {
        encounter.setCreationDate(new Date());
        encounter.setId(1);
        encounter.setStreetPersonName("Jean");
        encounter.setUserName("Nico");

        Intent intent = new Intent(activity, EncounterActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(Constants.KEY_ENCOUNTER, encounter);
        intent.putExtras(extras);

        activity.startActivity(intent);
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        Map <Marker, Encounter> encounterMarkerHashMap = new HashMap<Marker, Encounter>();
        Map<Marker, Poi> poiMarkerHashMap = new HashMap<Marker, Poi>();

        public void addPoiMarker(Marker marker, Poi poi) {
            poiMarkerHashMap.put(marker, poi);
        }

        public void addEncounterMarker(Marker marker, Encounter encounter) {
            encounterMarkerHashMap.put(marker, encounter);
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            if (encounterMarkerHashMap.get(marker) != null){
                openEncounter(encounterMarkerHashMap.get(marker));
            }

            if (poiMarkerHashMap.get(marker) != null){
                Log.d("POI", String.valueOf(poiMarkerHashMap.get(marker).getLatitude()));
            }
            return false;
        }
    }
}
