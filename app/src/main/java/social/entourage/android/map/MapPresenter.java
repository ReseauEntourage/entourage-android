package social.entourage.android.map;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import social.entourage.android.encounter.EncounterActivity;
import social.entourage.android.common.Constants;
import social.entourage.android.api.MapResponse;
import social.entourage.android.api.MapService;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.encounter.ReadEncounterActivity;

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
        Intent intent = new Intent(activity, ReadEncounterActivity.class);
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
