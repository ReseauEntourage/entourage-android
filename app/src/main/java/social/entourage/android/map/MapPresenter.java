package social.entourage.android.map;

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
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.MapResponse;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;
import social.entourage.android.encounter.ReadEncounterActivity;

/**
 * Presenter controlling the MapActivity
 * @see MapActivity
 */
public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapEntourageFragment fragment;
    private final MapRequest mapRequest;

    private OnEntourageMarkerClickListener onClickListener;
    private boolean isStarted = false;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public MapPresenter(final MapEntourageFragment fragment, final MapRequest mapRequest) {
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
        //retrieveMapObjects(EntourageLocation.getInstance().getLastCameraPosition().target);
        fragment.setOnMarkerClickListener(onClickListener);
    }

    public void retrieveMapObjects(LatLng latLng) {
        if(isStarted) {
            /**
             * HERE : update the request to get all the encounters
             *        related to the current tour
             */
            mapRequest.map(Constants.TOKEN, 0, 0, latLng.latitude, latLng.longitude, new Callback<MapResponse>() {
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
    }

    private void loadObjectsOnMap(MapResponse mapResponse) {
        for (Encounter encounter : mapResponse.getEncounters()) {
            fragment.putEncounterOnMap(encounter, onClickListener);
        }
    }

    private void openEncounter(Encounter encounter) {
        fragment.saveCameraPosition();
        Intent intent = new Intent(fragment.getActivity(), ReadEncounterActivity.class);
        Bundle extras = new Bundle();
        extras.putSerializable(Constants.KEY_ENCOUNTER, encounter);
        intent.putExtras(extras);
        fragment.getActivity().startActivity(intent);
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        final Map<LatLng, Encounter> encounterMarkerHashMap = new HashMap<>();

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
