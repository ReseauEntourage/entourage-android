package social.entourage.android.map;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.Toast;

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
import social.entourage.android.Constants;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.map.encounter.ReadEncounterActivity;
import social.entourage.android.map.tour.TourInformationFragment;

/**
 * Presenter controlling the MapEntourageFragment
 * @see MapEntourageFragment
 */
public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapEntourageFragment fragment;
    private final MapRequest mapRequest;
    private final AuthenticationController authenticationController;

    private OnEntourageMarkerClickListener onClickListener;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public MapPresenter(final MapEntourageFragment fragment,
                        final MapRequest mapRequest,
                        final AuthenticationController authenticationController) {
        this.fragment = fragment;
        this.mapRequest = mapRequest;
        this.authenticationController = authenticationController;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------


    public OnEntourageMarkerClickListener getOnClickListener() {
        return onClickListener;
    }

    public void start() {
        onClickListener = new OnEntourageMarkerClickListener();
        fragment.initializeMapZoom();
        fragment.setOnMarkerClickListener(onClickListener);
    }

    public void incrementUserToursCount() {
        authenticationController.incrementUserToursCount();
    }

    public void loadEncounterOnMap(Encounter encounter) {
        fragment.putEncounterOnMap(encounter, onClickListener);
    }

    public void openTour(Tour tour) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            TourInformationFragment tourInformationFragment = TourInformationFragment.newInstance(tour);
            tourInformationFragment.show(fragmentManager, "fragment_tour_information");
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void openEncounter(Encounter encounter) {
        if (fragment.getActivity() != null) {
            fragment.saveCameraPosition();
            Intent intent = new Intent(fragment.getActivity(), ReadEncounterActivity.class);
            Bundle extras = new Bundle();
            extras.putSerializable(ReadEncounterActivity.BUNDLE_KEY_ENCOUNTER, encounter);
            intent.putExtras(extras);
            fragment.getActivity().startActivity(intent);
        }
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements GoogleMap.OnMarkerClickListener {
        final Map<LatLng, Encounter> encounterMarkerHashMap = new HashMap<>();
        final Map<LatLng, Tour> tourMarkerHashMap = new HashMap<>();

        public void addEncounterMarker(LatLng markerPosition, Encounter encounter) {
            encounterMarkerHashMap.put(markerPosition, encounter);
        }

        public void addTourMarker(LatLng markerPosition, Tour tour) {
            tourMarkerHashMap.put(markerPosition, tour);
        }

        public void removeMarker(long tourId) {
            tourMarkerHashMap.remove(tourId);
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng markerPosition = marker.getPosition();
            if (encounterMarkerHashMap.get(markerPosition) != null){
                openEncounter(encounterMarkerHashMap.get(markerPosition));
            }
            if (tourMarkerHashMap.get(markerPosition) != null){
                openTour(tourMarkerHashMap.get(markerPosition));
            }
            return false;
        }
    }

}
