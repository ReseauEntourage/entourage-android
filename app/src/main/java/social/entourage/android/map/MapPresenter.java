package social.entourage.android.map;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.MapResponse;
import social.entourage.android.api.MapService;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.common.Constants;
import social.entourage.android.encounter.ReadEncounterActivity;

public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapActivity activity;
    private final MapService mapService;

    private OnEntourageMarkerClickListener onClickListener;
    private boolean isStarted = false;

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
        isStarted = true;
        activity.initializeMap();
        retrieveMapObjects(EntourageLocation.getInstance().getLastCameraPosition().target);
        activity.setOnMarkerCLickListener(onClickListener);
    }

    public void retrieveTours() {

    }

    public void retrieveMapObjects(LatLng latLng) {
        if(isStarted) {
            mapService.map("0cb4507e970462ca0b11320131e96610", 0, 0, latLng.latitude, latLng.longitude, new Callback<MapResponse>() {
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
        } else {
            //Map is not initialized so we just store the proper camera location
            //EntourageLocation.getInstance().resetCameraPosition(latLng);
        }
    }

    private void loadObjectsOnMap(MapResponse mapResponse) {
        //activity.clearMap();
        for (Encounter encounter : mapResponse.getEncounters()) {
            activity.putEncouter(encounter, onClickListener);
        }

        /*for (Poi poi : mapResponse.getPois()) {
            activity.putPoi(poi, onClickListener);
        }*/
    }

    public void openEncounter(Encounter encounter) {
        activity.saveCameraPosition();
        Intent intent = new Intent(activity, ReadEncounterActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(Constants.KEY_ENCOUNTER, encounter);
        intent.putExtras(extras);
        activity.startActivity(intent);
    }

    /*
    public void getCurrentAddress(Location location) {
        Intent intent = new Intent(activity, FetchAddressIntentService.class);
        intent.putExtra(Constants.RECEIVER, receiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, location);
        activity.startService(intent);
    }
    */

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        Map<LatLng, Encounter> encounterMarkerHashMap = new HashMap<LatLng, Encounter>();

        public void addEncounterMarker(LatLng markerPosition, Encounter encounter) {
            encounterMarkerHashMap.put(markerPosition, encounter);
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng markerPosition = marker.getPosition();
            if (encounterMarkerHashMap.get(markerPosition) != null){
                openEncounter(encounterMarkerHashMap.get(markerPosition));
            }
            return false;
        }
    }

}
