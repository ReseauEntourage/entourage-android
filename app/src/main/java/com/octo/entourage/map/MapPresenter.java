package com.octo.entourage.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import com.octo.entourage.encounter.EncounterActivity;
import com.octo.entourage.model.Constants;
import com.octo.entourage.model.Encounter;
import com.octo.entourage.model.Poi;

import org.joda.time.DateTime;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapActivity activity;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public MapPresenter(final MapActivity activity) {
        this.activity = activity;
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

        OnEntourageMarkerClickListener onClickListener = new OnEntourageMarkerClickListener();

        activity.setOnMarkerCLickListener(onClickListener);
        activity.putEncouter(encounter, onClickListener);
        activity.putPoi(poi, onClickListener);
    }

    public void openEncounter(Encounter encounter) {
        encounter.setCreationDate(new DateTime());
        encounter.setId(1);
        encounter.setStreetPersonName("Jean");
        encounter.setUserName("Nico");

        Intent intent = new Intent(activity, EncounterActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(Constants.KEY_ENCOUNTER_ID, encounter);
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
