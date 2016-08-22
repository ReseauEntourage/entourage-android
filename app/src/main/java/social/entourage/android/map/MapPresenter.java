package social.entourage.android.map;

import android.support.v4.app.FragmentManager;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import social.entourage.android.api.MapRequest;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.map.encounter.EncounterDisclaimerFragment;
import social.entourage.android.map.entourage.CreateEntourageFragment;
import social.entourage.android.map.entourage.EntourageDisclaimerFragment;
import social.entourage.android.map.tour.information.TourInformationFragment;
import social.entourage.android.tools.BusProvider;

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
    private OnEntourageGroundOverlayClickListener onGroundOverlayClickListener;

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

    public OnEntourageGroundOverlayClickListener getOnGroundOverlayClickListener() {
        return onGroundOverlayClickListener;
    }

    public void start() {
        onClickListener = new OnEntourageMarkerClickListener();
        onGroundOverlayClickListener = new OnEntourageGroundOverlayClickListener();
    }

    public void incrementUserToursCount() {
        authenticationController.incrementUserToursCount();
    }

    public void loadEncounterOnMap(Encounter encounter) {
        fragment.putEncounterOnMap(encounter, onClickListener);
    }

    public void openFeedItem(FeedItem feedItem, long invitationId) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            TourInformationFragment tourInformationFragment = TourInformationFragment.newInstance(feedItem, invitationId);
            tourInformationFragment.show(fragmentManager, TourInformationFragment.TAG);
        }
    }

    public void openFeedItem(long feedItemId, int feedItemType, long invitationId) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            TourInformationFragment tourInformationFragment = TourInformationFragment.newInstance(feedItemId, feedItemType, invitationId);
            tourInformationFragment.show(fragmentManager, TourInformationFragment.TAG);
        }
    }

    public void createEntourage(String entourageType, LatLng location) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            CreateEntourageFragment entourageFragment = CreateEntourageFragment.newInstance(entourageType, location);
            entourageFragment.show(fragmentManager, CreateEntourageFragment.TAG);
        }
    }

    public void displayEntourageDisclaimer(String entourageType) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            EntourageDisclaimerFragment fragment = EntourageDisclaimerFragment.newInstance(entourageType);
            fragment.show(fragmentManager, EntourageDisclaimerFragment.TAG);
        }
    }

    public void displayEncounterDisclaimer() {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            EncounterDisclaimerFragment fragment = EncounterDisclaimerFragment.newInstance();
            fragment.show(fragmentManager, EncounterDisclaimerFragment.TAG);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void openEncounter(Encounter encounter) {
        if (fragment.getActivity() != null) {
            fragment.saveCameraPosition();
            BusProvider.getInstance().post(new Events.OnTourEncounterViewRequestedEvent(encounter));
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

        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng markerPosition = marker.getPosition();
            if (encounterMarkerHashMap.get(markerPosition) != null){
                openEncounter(encounterMarkerHashMap.get(markerPosition));
            }
            else if (tourMarkerHashMap.get(markerPosition) != null){
                openFeedItem(tourMarkerHashMap.get(markerPosition), 0);
            }
            return false;
        }
    }

    public class OnEntourageGroundOverlayClickListener implements GoogleMap.OnGroundOverlayClickListener {

        final Map<LatLng, Entourage> entourageMarkerHashMap = new HashMap<>();

        public void addEntourageGroundOverlay(LatLng markerPosition, Entourage entourage) {
            entourageMarkerHashMap.put(markerPosition, entourage);
        }

        @Override
        public void onGroundOverlayClick(final GroundOverlay groundOverlay) {
            LatLng markerPosition = groundOverlay.getPosition();
            if (entourageMarkerHashMap.get(markerPosition) != null) {
                //TODO Show the entourage details
                Log.d("Entourage GroundOverlay", "click");
            }
        }
    }

}
