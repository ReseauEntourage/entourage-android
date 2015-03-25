package com.octo.entourage.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import com.octo.entourage.model.Encounter;
import com.octo.entourage.model.Poi;

/**
 * Created by RPR on 25/03/15.
 */
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
}
