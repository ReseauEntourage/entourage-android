package social.entourage.android.map;

import android.support.v4.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.Constants;
import social.entourage.android.EntourageEvents;
import social.entourage.android.api.InvitationRequest;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.map.encounter.EncounterDisclaimerFragment;
import social.entourage.android.map.entourage.create.CreateEntourageFragment;
import social.entourage.android.map.entourage.EntourageDisclaimerFragment;
import social.entourage.android.map.tour.information.TourInformationFragment;
import social.entourage.android.tools.BusProvider;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

/**
 * Presenter controlling the MapEntourageFragment
 *
 * @see MapEntourageFragment
 */
public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapEntourageFragment fragment;
    private final AuthenticationController authenticationController;
    private final InvitationRequest invitationRequest;

    private OnEntourageMarkerClickListener onClickListener;
    private OnEntourageGroundOverlayClickListener onGroundOverlayClickListener;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public MapPresenter(final MapEntourageFragment fragment,
                        final AuthenticationController authenticationController,
                        final InvitationRequest invitationRequest) {
        this.fragment = fragment;
        this.authenticationController = authenticationController;
        this.invitationRequest = invitationRequest;
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

    public boolean isShowNoEntouragesPopup() {
        return authenticationController.isShowNoEntouragesPopup();
    }

    public void setShowNoEntouragesPopup(boolean show) {
        authenticationController.setShowNoEntouragesPopup(show);
    }

    public void loadEncounterOnMap(Encounter encounter) {
        fragment.putEncounterOnMap(encounter, onClickListener);
    }

    public void openFeedItem(FeedItem feedItem, long invitationId, int feedRank) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            TourInformationFragment tourInformationFragment = TourInformationFragment.newInstance(feedItem, invitationId, feedRank);
            tourInformationFragment.show(fragmentManager, TourInformationFragment.TAG);
        }
    }

    public void openFeedItem(String feedItemUUID, int feedItemType, long invitationId) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            TourInformationFragment tourInformationFragment = TourInformationFragment.newInstance(feedItemUUID, feedItemType, invitationId);
            tourInformationFragment.show(fragmentManager, TourInformationFragment.TAG);
        }
    }

    public void openFeedItem(String feedItemShareURL, int feedItemType) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            TourInformationFragment tourInformationFragment = TourInformationFragment.newInstance(feedItemShareURL, feedItemType);
            tourInformationFragment.show(fragmentManager, TourInformationFragment.TAG);
        }
    }

    public void createEntourage(LatLng location, String groupType) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            CreateEntourageFragment entourageFragment = CreateEntourageFragment.newInstance(location, groupType);
            entourageFragment.show(fragmentManager, CreateEntourageFragment.TAG);
        }
    }

    public void displayEntourageDisclaimer(String groupType) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            EntourageDisclaimerFragment fragment = EntourageDisclaimerFragment.newInstance(groupType);
            fragment.show(fragmentManager, EntourageDisclaimerFragment.TAG);
        }
    }

    public boolean shouldDisplayEncounterDisclaimer() {
        return authenticationController != null && authenticationController.isShowEncounterDisclaimer();
    }

    public void  setDisplayEncounterDisclaimer(boolean displayEncounterDisclaimer) {
        if (authenticationController != null) {
            authenticationController.setShowEncounterDisclaimer(displayEncounterDisclaimer);
        }
    }

    public void displayEncounterDisclaimer() {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            EncounterDisclaimerFragment fragment = EncounterDisclaimerFragment.newInstance();
            fragment.show(fragmentManager, EncounterDisclaimerFragment.TAG);
        }
    }

    public void getMyPendingInvitations() {
        Call<Invitation.InvitationsWrapper> call = invitationRequest.retrieveUserInvitationsWithStatus(Invitation.STATUS_PENDING);
        call.enqueue(new Callback<Invitation.InvitationsWrapper>() {
            @Override
            public void onResponse(final Call<Invitation.InvitationsWrapper> call, final Response<Invitation.InvitationsWrapper> response) {
                if (response.isSuccessful()) {
                    fragment.onInvitationsReceived(response.body().getInvitations());
                } else {
                    fragment.onInvitationsReceived(null);
                }
            }

            @Override
            public void onFailure(final Call<Invitation.InvitationsWrapper> call, final Throwable t) {
                fragment.onInvitationsReceived(null);
            }
        });
    }

    public void acceptInvitation(long invitationId) {
        Call<Invitation.InvitationWrapper> call = invitationRequest.acceptInvitation(invitationId);
        call.enqueue(new Callback<Invitation.InvitationWrapper>() {
            @Override
            public void onResponse(final Call<Invitation.InvitationWrapper> call, final Response<Invitation.InvitationWrapper> response) {
            }

            @Override
            public void onFailure(final Call<Invitation.InvitationWrapper> call, final Throwable t) {
            }
        });
    }

    public void handleLocationPermission() {
        fragment.checkPermission(getUserLocationAccess());
    }

    protected String getUserLocationAccess() {
        User user = authenticationController.getUser();
        return user != null ? user.getLocationAccessString() : ACCESS_COARSE_LOCATION;
    }

    public void resetUserOnboardingFlag() {
        User me = authenticationController.getUser();
        if (me != null) {
            me.setOnboardingUser(false);
            authenticationController.saveUser(me);
        }
    }

    public void saveMapFilter() {
        if (authenticationController != null) {
            authenticationController.saveMapFilter();
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
        final Map<Marker, Encounter> encounterMarkerHashMap = new HashMap<>();
        final Map<Marker, FeedItem> markerFeedItemHashMap = new HashMap<>();

        public void addEncounterMarker(Marker marker, Encounter encounter) {
            encounterMarkerHashMap.put(marker, encounter);
        }

        public Marker removeEncounterMarker(long encounterId) {
            Marker marker = null;
            for (Marker key :encounterMarkerHashMap.keySet()) {
                Encounter encounter = encounterMarkerHashMap.get(key);
                if (encounter.getId() == encounterId) {
                    marker = key;
                    encounterMarkerHashMap.remove(key);
                    break;
                }
            }
            return marker;
        }

        public void addTourMarker(Marker marker, FeedItem feedItem) {
            markerFeedItemHashMap.put(marker, feedItem);
        }

        public void clear() {
            encounterMarkerHashMap.clear();
            markerFeedItemHashMap.clear();
        }

        @Override
        public boolean onMarkerClick(Marker marker) {
            LatLng markerPosition = marker.getPosition();
            if (encounterMarkerHashMap.get(marker) != null) {
                openEncounter(encounterMarkerHashMap.get(marker));
            } else if (markerFeedItemHashMap.get(marker) != null) {
                FeedItem feedItem = markerFeedItemHashMap.get(marker);
                if (FeedItem.TOUR_CARD == feedItem.getType()) {
                    openFeedItem(feedItem, 0, 0);
                }
                else {
                    if (fragment != null) {
                        fragment.handleHeatzoneClick(markerPosition);
                    }
                }
            }
            return false;
        }
    }

    public class OnEntourageGroundOverlayClickListener implements GoogleMap.OnGroundOverlayClickListener {

        final Map<LatLng, FeedItem> entourageMarkerHashMap = new HashMap<>();

        public void addEntourageGroundOverlay(LatLng markerPosition, FeedItem feedItem) {
            entourageMarkerHashMap.put(markerPosition, feedItem);
        }

        public void clear() {
            entourageMarkerHashMap.clear();
        }

        @Override
        public void onGroundOverlayClick(final GroundOverlay groundOverlay) {
            LatLng markerPosition = groundOverlay.getPosition();
            if (entourageMarkerHashMap.get(markerPosition) != null) {
                EntourageEvents.logEvent(Constants.EVENT_FEED_HEATZONECLICK);
                if (fragment != null) {
                    fragment.handleHeatzoneClick(markerPosition);
                }
            }
        }
    }

}
