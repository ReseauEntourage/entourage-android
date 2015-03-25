package com.octo.entourage.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.octo.entourage.model.Encounter;
import com.octo.entourage.model.Poi;
import android.content.Intent;
import android.os.Bundle;
import com.octo.entourage.encounter.EncounterActivity;
import com.octo.entourage.model.Constants;

import org.joda.time.DateTime;

public class MapPresenter {
    private final MapActivity activity;

    public MapPresenter(final MapActivity activity) {
        this.activity = activity;
    }


    public void start() {
        Encounter encounter = new Encounter();
        encounter.setLatitude(42);
        encounter.setLongitude(2);

        Poi poi = new Poi();
        poi.setLatitude(43);
        poi.setLongitude(2);
        poi.setCategoryId(4);

        GoogleMap.OnMarkerClickListener onClickListener = new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return false;
            }
        };

        activity.setOnMarkerCLickListener(onClickListener);
        activity.putEncouter(encounter);
        activity.putPoi(poi);
    }

    public void openEncounter() {
        Encounter encounter = new Encounter();
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
}
