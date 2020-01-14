package social.entourage.android.map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageEvents;
import social.entourage.android.api.InvitationRequest;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.entourage.category.EntourageCategory;
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment;
import social.entourage.android.entourage.create.CreateEntourageFragment;
import social.entourage.android.entourage.EntourageDisclaimerFragment;
import social.entourage.android.entourage.information.EntourageInformationFragment;
import social.entourage.android.tools.BusProvider;

/**
 * Presenter controlling the MapFragment
 *
 * @see MapFragment
 */
public class MapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final MapFragment fragment;
    private final AuthenticationController authenticationController;
    private final InvitationRequest invitationRequest;

    private OnEntourageMarkerClickListener onClickListener;
    private OnEntourageGroundOverlayClickListener onGroundOverlayClickListener;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public MapPresenter(final MapFragment fragment,
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

    public void openFeedItem(FeedItem feedItem, long invitationId, int feedRank) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            EntourageInformationFragment entourageInformationFragment = EntourageInformationFragment.newInstance(feedItem, invitationId, feedRank);
            entourageInformationFragment.show(fragmentManager, EntourageInformationFragment.TAG);
        }
    }

    public void openFeedItem(String feedItemUUID, int feedItemType, long invitationId) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            EntourageInformationFragment entourageInformationFragment = EntourageInformationFragment.newInstance(feedItemUUID, feedItemType, invitationId);
            entourageInformationFragment.show(fragmentManager, EntourageInformationFragment.TAG);
        }
    }

    public void openFeedItem(String feedItemShareURL, int feedItemType) {
        if (fragment.getActivity() != null) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            EntourageInformationFragment entourageInformationFragment = EntourageInformationFragment.newInstance(feedItemShareURL, feedItemType);
            entourageInformationFragment.show(fragmentManager, EntourageInformationFragment.TAG);
        }
    }

    public void createEntourage(LatLng location, @NonNull String groupType, @Nullable EntourageCategory category) {
        if (fragment != null && fragment.getActivity() != null && !fragment.isStateSaved()) {
            FragmentManager fragmentManager = fragment.getActivity().getSupportFragmentManager();
            CreateEntourageFragment entourageFragment = CreateEntourageFragment.newInstance(location, groupType, category);
            entourageFragment.show(fragmentManager, CreateEntourageFragment.TAG);
        }
    }

    public void displayEntourageDisclaimer(String groupType) {
        if (fragment != null && fragment.getActivity() != null && !fragment.isStateSaved()) {
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
            public void onResponse(@NonNull final Call<Invitation.InvitationsWrapper> call, @NonNull final Response<Invitation.InvitationsWrapper> response) {
                if (response.isSuccessful()) {
                    fragment.onInvitationsReceived(response.body().getInvitations());
                } else {
                    fragment.onInvitationsReceived(null);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Invitation.InvitationsWrapper> call, @NonNull final Throwable t) {
                fragment.onInvitationsReceived(null);
            }
        });
    }

    public void acceptInvitation(long invitationId) {
        Call<Invitation.InvitationWrapper> call = invitationRequest.acceptInvitation(invitationId);
        call.enqueue(new Callback<Invitation.InvitationWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Invitation.InvitationWrapper> call, @NonNull final Response<Invitation.InvitationWrapper> response) {
            }

            @Override
            public void onFailure(@NonNull final Call<Invitation.InvitationWrapper> call, @NonNull final Throwable t) {
            }
        });
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

    public class OnEntourageMarkerClickListener implements ClusterManager.OnClusterItemClickListener<MapClusterItem> {
        final Map<MapClusterItem, Encounter> encounterMarkerHashMap = new HashMap<>();

        public void addEncounterMapClusterItem(MapClusterItem mapClusterItem, Encounter encounter) {
            encounterMarkerHashMap.put(mapClusterItem, encounter);
        }

        public MapClusterItem getEncounterMapClusterItem(long encounterId) {
            MapClusterItem mapClusterItem = null;
            for (MapClusterItem key :encounterMarkerHashMap.keySet()) {
                Encounter encounter = encounterMarkerHashMap.get(key);
                if (encounter.getId() == encounterId) {
                    mapClusterItem = key;
                    break;
                }
            }
            return mapClusterItem;
        }

        public MapClusterItem removeEncounterMapClusterItem(long encounterId) {
            MapClusterItem mapClusterItem = getEncounterMapClusterItem(encounterId);
            if (mapClusterItem != null) {
                    encounterMarkerHashMap.remove(mapClusterItem);
            }
            return mapClusterItem;
        }

        public void clear() {
            encounterMarkerHashMap.clear();
        }

        @Override
        public boolean onClusterItemClick(final MapClusterItem mapClusterItem) {
            if (encounterMarkerHashMap.get(mapClusterItem) != null) {
                openEncounter(encounterMarkerHashMap.get(mapClusterItem));
            } else {
                Object mapItem = mapClusterItem.getMapItem();
                if (mapItem != null) {
                    if (mapItem instanceof FeedItem) {
                        FeedItem feedItem = (FeedItem)mapItem;
                        if (FeedItem.TOUR_CARD == feedItem.getType()) {
                            openFeedItem(feedItem, 0, 0);
                        }
                        else {
                            if (fragment != null) {
                                fragment.handleHeatzoneClick(mapClusterItem.getPosition());
                            }
                        }
                    }
                }
            }
            return true;
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
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_HEATZONECLICK);
                if (fragment != null) {
                    fragment.handleHeatzoneClick(markerPosition);
                }
            }
        }
    }

}
