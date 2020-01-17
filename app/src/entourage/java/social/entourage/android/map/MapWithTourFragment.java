package social.entourage.android.map;

import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
import social.entourage.android.Constants;
import social.entourage.android.MainActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.location.EntourageLocation;
import social.entourage.android.PlusFragment;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.api.tape.Events.OnEncounterCreated;
import social.entourage.android.entourage.category.EntourageCategoryManager;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.message.push.PushNotificationManager;
import social.entourage.android.newsfeed.FeedItemOptionsFragment;
import social.entourage.android.service.EntourageService;
import social.entourage.android.tour.choice.ChoiceFragment;
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment;
import social.entourage.android.tour.encounter.CreateEncounterActivity;
import social.entourage.android.service.EntourageServiceListener;
import social.entourage.android.tour.join.TourJoinRequestFragment;
import social.entourage.android.view.EntourageSnackbar;
import timber.log.Timber;

import static social.entourage.android.service.EntourageService.KEY_LOCATION_PROVIDER_DISABLED;

public class MapWithTourFragment extends MapFragment implements EntourageServiceListener {
    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int MAX_TOUR_HEADS_DISPLAYED = 10;

    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound;
    private String currentTourUUID = "";
    private int color;
    private List<Polyline> drawnToursMap;
    private List<Polyline> currentTourLines;
    private LatLng previousCoordinates;
    private ProgressDialog loaderSearchTours;

    private boolean userHistory;
    private Map<Long, Polyline> drawnUserHistory;
    private Map<Long, Tour> retrievedHistory;

    private int displayedTourHeads = 0;

    private boolean shouldShowGPSDialog = true;

    @BindView(R.id.layout_map_launcher)
    View mapLauncherLayout;

    @BindView(R.id.launcher_tour_go)
    ImageView buttonLaunchTour;

    @BindView(R.id.launcher_tour_type)
    RadioGroup radioGroupType;

    @BindView(R.id.launcher_tour_progressBar)
    ProgressBar launcherProgressBar;

    @Nullable
    @BindView(R.id.tour_stop_button)
    FloatingActionButton tourStopButton;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CREATE_ENCOUNTER) {
            if (resultCode == Constants.RESULT_CREATE_ENCOUNTER_OK && data.getExtras()!=null) {
                Encounter encounter = (Encounter) data.getExtras().getSerializable(CreateEncounterActivity.BUNDLE_KEY_ENCOUNTER);
                addEncounter(encounter);
                putEncounterOnMap(encounter);
            }
        }
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        drawnUserHistory = new TreeMap<>();
        drawnToursMap = new ArrayList<>();
        currentTourLines = new ArrayList<>();
        retrievedHistory = new TreeMap<>();
        if (!isBound) {
            doBindService();
        }
    }

    @Override
    public void onDestroy() {
        if (isBound && entourageService != null) {
            entourageService.unregisterServiceListener(MapWithTourFragment.this);
            doUnbindService();
        }
        super.onDestroy();
    }

    @Override
    public void onLocationStatusUpdated(boolean active) {
        super.onLocationStatusUpdated(active);
        if(shouldShowGPSDialog && !active &&  entourageService !=null && entourageService.isRunning()) {
            //We always need GPS to be turned on during tour
            shouldShowGPSDialog = false;
            final Intent newIntent = new Intent(this.getContext(), MainActivity.class);
            newIntent.setAction(KEY_LOCATION_PROVIDER_DISABLED);
            newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(newIntent);
        } else if(!shouldShowGPSDialog && active) {
            shouldShowGPSDialog = true;
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mapLauncherLayout != null && mapLauncherLayout.getVisibility() == View.VISIBLE) {
            hideTourLauncher();
            return true;
        }
        if (mapLongClickView != null
                && mapLongClickView.getVisibility() == View.VISIBLE
                && entourageService != null
                && entourageService.isRunning()
                && tourStopButton != null) {
            tourStopButton.setVisibility(View.VISIBLE);
        }
        return super.onBackPressed();
    }

    @Override
    public void displayEntourageDisclaimer() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTION_CREATE_CLICK);
        // if we have an ongoing tour
        if (getActivity()!=null && isBound && entourageService != null && entourageService.isRunning()) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_ENCOUNTER_POPUP_SHOW);
            // Show the dialog that asks the user if he really wants to create an entourage instead of encounter
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setMessage(R.string.entourage_tour_ongoing_description)
                    .setTitle(R.string.entourage_tour_ongoing_title)
                    .setPositiveButton(R.string.entourage_tour_ongoing_proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            EntourageEvents.logEvent(EntourageEvents.EVENT_ENCOUNTER_POPUP_ENCOUNTER);
                            onAddEncounter();
                        }
                    })
                    .setNegativeButton(R.string.entourage_tour_ongoing_action, (dialog, which) -> {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_ENCOUNTER_POPUP_ENTOURAGE);
                        super.displayEntourageDisclaimer();
                    });

            builder.show();
            return;
        }
        if (entourageService != null && entourageService.isRunning()&& tourStopButton != null) {
            tourStopButton.setVisibility(View.VISIBLE);
        }
        super.displayEntourageDisclaimer();
    }

    private void putEncounterOnMap(Encounter encounter) {
        if (map == null || presenter == null) {
            // The map is not yet initialized or the google play services are outdated on the phone
            return;
        }
        MapClusterItem mapClusterItem = presenter.getOnClickListener().getEncounterMapClusterItem(encounter.getId());
        if (mapClusterItem != null) {
            //the item aalready exists
            return;
        }
        mapClusterItem = new MapClusterItem(encounter);
        presenter.getOnClickListener().addEncounterMapClusterItem(mapClusterItem, encounter);
        mapClusterManager.addItem(mapClusterItem);
    }


    public void checkAction(@NonNull String action) {
        if (getActivity() != null && isBound) {
            // 1 : Check if should Resume tour
            if (TourEndConfirmationFragment.KEY_RESUME_TOUR.equals(action)) {
                resumeTour();
            }
            // 2 : Check if should End tour
            else if (TourEndConfirmationFragment.KEY_END_TOUR.equals(action)) {
                if(entourageService != null && entourageService.isRunning())
                    stopFeedItem(null, true);
            }
            // 3 : Check if tour is already paused
            else if (entourageService !=null && entourageService.isPaused()) {
                launchConfirmationFragment();
            }
            // 4 : Check if should pause tour
            else if (EntourageService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                launchConfirmationFragment();
            }
            // 5 : Check if should stop tour
            else if (entourageService !=null && EntourageService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                entourageService.endTreatment();
            }
            // 6 : start tour
            else if (PlusFragment.KEY_START_TOUR.equals(action)) {
                onStartTourLauncher();
            }
            // 7 : Create action
            else if (PlusFragment.KEY_CREATE_CONTRIBUTION.equals(action)) {
                createAction(EntourageCategoryManager.getInstance().getDefaultCategory(Entourage.TYPE_CONTRIBUTION), Entourage.TYPE_ACTION);
            }
            // 8 : Create action
            else if (PlusFragment.KEY_CREATE_DEMAND.equals(action)) {
                createAction(EntourageCategoryManager.getInstance().getDefaultCategory(Entourage.TYPE_DEMAND), Entourage.TYPE_ACTION);
            }
            // 9 : Create action
            else if (PlusFragment.KEY_CREATE_OUTING.equals(action)) {
                createAction(null, Entourage.TYPE_OUTING);
            }
            // 10 : Create encounter
            else if (PlusFragment.KEY_ADD_ENCOUNTER.equals(action)) {
                onAddEncounter();
            }
        }
    }

    @Override
    public void onNotificationExtras(int id, boolean choice) {
        super.onNotificationExtras(id, choice);
        userHistory = choice;
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onUserChoiceChanged(Events.OnUserChoiceEvent event) {
        userHistory = event.isUserHistory();
        if (userHistory) {
            entourageService.updateUserHistory(userId, 1, 500);
        }
        if (userHistory) {
            showUserHistory();
        } else {
            hideUserHistory();
        }
    }

    @Subscribe
    public void onUserInfoUpdated(Events.OnUserInfoUpdatedEvent event) {
        User me = EntourageApplication.me(getContext());
        if (me == null || newsfeedAdapter == null) return;
        TourAuthor meAsAuthor = me.asTourAuthor();
        List<TimestampedObject> dirtyList = new ArrayList<>();
        // See which cards needs updating
        for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
            if (!(timestampedObject instanceof FeedItem)) continue;
            FeedItem feedItem = (FeedItem)timestampedObject;
            TourAuthor author = feedItem.getAuthor();
            // Skip null author
            if (author == null) continue;
            // Skip not same author id
            if (author.getUserID() != meAsAuthor.getUserID()) continue;
            // Skip if nothing changed
            if (author.isSame(meAsAuthor)) continue;
            // Update the tour author
            meAsAuthor.setUserName(author.getUserName());
            feedItem.setAuthor(meAsAuthor);
            // Mark as dirty
            dirtyList.add(feedItem);
        }
        // Update the dirty cards
        for (TimestampedObject dirty : dirtyList) {
            newsfeedAdapter.updateCard(dirty);
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    @Optional
    @OnClick(R.id.map_longclick_button_start_tour_launcher)
    public void onStartTourLauncher() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_TOUR_CREATE_CLICK);
        if (entourageService != null) {
            if (!entourageService.isRunning()) {
                // Check if the geolocation is permitted
                if (!LocationUtils.INSTANCE.isLocationEnabled() || !LocationUtils.INSTANCE.isLocationPermissionGranted()) {
                    showAllowGeolocationDialog(GEOLOCATION_POPUP_TOUR);
                    return;
                }
                if(mapLongClickView!=null) {
                    mapLongClickView.setVisibility(View.GONE);
                }
                if(mapLauncherLayout!=null) {
                    mapLauncherLayout.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    @OnClick(R.id.launcher_tour_go)
    void onStartNewTour() {
        buttonLaunchTour.setEnabled(false);
        launcherProgressBar.setVisibility(View.VISIBLE);
        TourType tourType = TourType.findByRessourceId(radioGroupType.getCheckedRadioButtonId());
        startTour(tourType.getName());
        if (tourType == TourType.MEDICAL) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_MEDICAL);
        } else if (tourType == TourType.BARE_HANDS) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_SOCIAL);
        } else if (tourType == TourType.ALIMENTARY) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_DISTRIBUTION);
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_START_TOUR);
    }

    @Optional @OnClick(R.id.tour_stop_button)
    public void onStartStopConfirmation() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_SUSPEND);
        pauseTour();
        if (getActivity() != null) {
            launchConfirmationFragment();
        }
    }

    @Optional
    @OnClick(R.id.map_longclick_button_create_encounter)
    public void onAddEncounter() {
        if (getActivity() == null) {
            return;
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_CLICK);
        // Hide the create entourage menu ui
        mapLongClickView.setVisibility(View.GONE);

        // MI: EMA-1669 Show the disclaimer only the first time when a tour was started
        // Show the disclaimer fragment
        if (presenter != null) {
            if (presenter.shouldDisplayEncounterDisclaimer()) {
                presenter.displayEncounterDisclaimer();
            } else {
                addEncounter();
            }
        }
    }

    @Override
    public void addEncounter() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CreateEncounterActivity.class);
            saveCameraPosition();
            Bundle args = new Bundle();
            args.putString(CreateEncounterActivity.BUNDLE_KEY_TOUR_ID, currentTourUUID);
            if (longTapCoordinates != null) {
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, longTapCoordinates.latitude);
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, longTapCoordinates.longitude);
                longTapCoordinates = null;
            } else if (EntourageLocation.getInstance().getCurrentLocation() != null) {
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, EntourageLocation.getInstance().getCurrentLocation().getLatitude());
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, EntourageLocation.getInstance().getCurrentLocation().getLongitude());
            } else {
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);
            }
            intent.putExtras(args);
            //startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
            startActivity(intent);

            // show the disclaimer only once per tour
            if (presenter != null) {
                presenter.setDisplayEncounterDisclaimer(false);
            }
        }
    }

    protected void updateFloatingMenuOptions() {
        if (tourStopButton != null) {
            tourStopButton.setVisibility((entourageService != null && entourageService.isRunning())?View.VISIBLE:View.GONE);
        }
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------

    @Override
    protected  void showLongClickOnMapOptions(LatLng latLng) {
        //save the tap coordinates
        longTapCoordinates = latLng;
        //hide the FAB menu
        if (tourStopButton != null) tourStopButton.setVisibility(View.GONE);
        //for public user, start the create entourage funnel directly
        User me = EntourageApplication.me(getActivity());
        boolean isPro = (me != null && me.isPro());
        if (!isPro) {
            displayEntourageDisclaimer();
            return;
        }
        //update the visible buttons
        boolean isTourRunning = entourageService != null && entourageService.isRunning();
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_start_tour_launcher).setVisibility(isTourRunning ? View.INVISIBLE : View.VISIBLE);
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_create_encounter).setVisibility(isTourRunning ? View.VISIBLE : View.GONE);
        super.showLongClickOnMapOptions(latLng);
    }

    // ----------------------------------
    // BUS LISTENERS : needs to be in final class (not in parent class
    // ----------------------------------
    @Subscribe
    @Override
    public void onMyEntouragesForceRefresh(Events.OnMyEntouragesForceRefresh event) {
        super.onMyEntouragesForceRefresh(event);
    }

    @Subscribe
    @Override
    public void onEntourageCreated(Events.OnEntourageCreated event) {
        super.onEntourageCreated(event);
    }

    @Subscribe
    @Override
    public void onEntourageUpdated(Events.OnEntourageUpdated event) {
        super.onEntourageUpdated(event);
    }

    @Subscribe
    @Override
    public void onNewsfeedLoadMoreRequested(Events.OnNewsfeedLoadMoreEvent event) {
        super.onNewsfeedLoadMoreRequested(event);
    }

    @Subscribe
    @Override
    public void onMapFilterChanged(Events.OnMapFilterChanged event) {
        super.onMapFilterChanged(event);
    }

    @Subscribe
    @Override
    public void onBetterLocation(Events.OnBetterLocationEvent event) {
        super.onBetterLocation(event);
    }

    @Subscribe
    public void onEncounterCreated(OnEncounterCreated event) {
        Encounter encounter = event.getEncounter();
        if (encounter != null) {
            addEncounter(encounter);
            if(presenter!=null) {
                putEncounterOnMap(encounter);
            }
        }
        if (entourageService != null) {
            if (tourStopButton != null) tourStopButton.setVisibility(entourageService.isRunning() ? View.VISIBLE : View.GONE);
        }
    }

    @Subscribe
    public void onEncounterUpdated(Events.OnEncounterUpdated event) {
        if (event == null || presenter == null) return;
        Encounter updatedEncounter = event.getEncounter();
        if (updatedEncounter == null) return;
        MapClusterItem mapClusterItem = presenter.getOnClickListener().removeEncounterMapClusterItem(updatedEncounter.getId());
        if (mapClusterItem != null) {
            mapClusterManager.removeItem(mapClusterItem);
        }
        updateEncounter(updatedEncounter);
        putEncounterOnMap(updatedEncounter);
    }

    @Subscribe
    public void feedItemViewRequested(Events.OnFeedItemInfoViewRequestedEvent event) {
        super.feedItemViewRequested(event);
    }

    @Subscribe
    public void userActRequested(final Events.OnUserActEvent event) {
        super.userActRequested(event);
    }

    @Override
    public void onTourCreated(boolean created, @NonNull String tourUUID) {
        if(buttonLaunchTour!=null) {
            buttonLaunchTour.setEnabled(true);
        }
        if(launcherProgressBar!=null) {
            launcherProgressBar.setVisibility(View.GONE);
        }
        if (getActivity() != null) {
            if (created) {
                isFollowing = true;
                currentTourUUID = tourUUID;
                mapLauncherLayout.setVisibility(View.GONE);
                if (newsfeedListView.getVisibility() == View.VISIBLE) {
                    displayFullMap();
                }
                addTourCard(entourageService.getCurrentTour());

                if (tourStopButton != null) tourStopButton.setVisibility(View.VISIBLE);

                if (presenter != null) {
                    presenter.incrementUserToursCount();
                    presenter.setDisplayEncounterDisclaimer(true);
                }
            } else if(layoutMain!=null){
                EntourageSnackbar.INSTANCE.make(layoutMain, R.string.tour_creation_fail, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onTourUpdated(@NonNull LatLng newPoint) {
        drawCurrentLocation(newPoint);
    }

    @Override
    public void onTourResumed(@NotNull List<? extends TourPoint> pointsToDraw, @NotNull String tourType, @NotNull Date startDate) {
        if (!pointsToDraw.isEmpty()) {
            drawCurrentTour(pointsToDraw, tourType, startDate);
            previousCoordinates = pointsToDraw.get(pointsToDraw.size() - 1).getLocation();

            Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
            centerMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            isFollowing = true;
        }
        if (tourStopButton != null) {
            tourStopButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRetrieveToursNearby(@NotNull List<? extends Tour> tours) {
        if(newsfeedAdapter==null) {
            return;
        }
        //check if there are tours to add or update
        int previousToursCount = newsfeedAdapter.getDataItemCount();
        tours = removeRedundantTours(tours, false);
        Collections.sort(tours, new Tour.TourComparatorOldToNew());
        for (Tour tour : tours) {
            if (!tour.getUUID().equalsIgnoreCase(currentTourUUID)) {
                addTourCard(tour);
            }
        }
        //recreate the map if needed
        if (tours.size() > 0 && map != null) {
            map.clear();
            for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
                if (timestampedObject.getType() == TimestampedObject.TOUR_CARD) {
                    Tour tour = (Tour)timestampedObject;
                    if (!tour.getUUID().equalsIgnoreCase(currentTourUUID)) {
                        drawNearbyTour((Tour) timestampedObject, false);
                    }
                }
            }
            if (entourageService != null && currentTourUUID != null && currentTourUUID.length() > 0) {
                PolylineOptions line = new PolylineOptions();
                for (Polyline polyline : currentTourLines) {
                    line.addAll(polyline.getPoints());
                }
                line.zIndex(2f);
                line.width(15);
                line.color(color);
                map.addPolyline(line);

                addCurrentTourEncounters();
            }
        }

        //show the map if no tours
        if (newsfeedAdapter.getDataItemCount() == 0) {
            displayFullMap();
        } else if (previousToursCount == 0) {
            displayListWithMapHeader();
        }
        //scroll to latest
        if (newsfeedAdapter.getDataItemCount() > 0) {
            newsfeedListView.scrollToPosition(0);
        }
    }

    @Override
    public void onRetrieveToursByUserId(@NotNull List<? extends Tour> tours) {
        tours = removeRedundantTours(tours, true);
        tours = removeRecentTours(tours);
        Collections.sort(tours, new Tour.TourComparatorOldToNew());
        for (Tour tour : tours) {
            if (!tour.getUUID().equalsIgnoreCase(currentTourUUID)) {
                drawNearbyTour(tour, true);
            }
        }
    }

    @Override
    public void onUserToursFound(@NotNull Map<Long, ? extends Tour> tours) {
    }

    @Override
    public void onToursFound(@NotNull Map<Long, ? extends Tour> tours) {
        if (loaderSearchTours != null) {
            loaderSearchTours.dismiss();
            loaderSearchTours = null;
        }
        if(getActivity()==null) {
            return;
        }
        if (tours.isEmpty()) {
            if(layoutMain!=null) {
                EntourageSnackbar.INSTANCE.make(layoutMain, R.string.tour_info_text_nothing_found, Snackbar.LENGTH_SHORT).show();
            }
        } else {
            if (tours.size() > 1) {
                List<Tour> tempList = new ArrayList<>();
                for (Map.Entry<Long, ? extends Tour> entry : tours.entrySet()) {
                    tempList.add(entry.getValue());
                }
                Tour.Tours toursList = new Tour.Tours(tempList);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                ChoiceFragment choiceFragment = ChoiceFragment.newInstance(toursList);
                choiceFragment.show(fragmentManager, "fragment_choice");
            } else if(presenter!=null){
                TreeMap<Long, Tour> toursTree = new TreeMap<>(tours);
                presenter.openFeedItem(toursTree.firstEntry().getValue(), 0, 0);
            }
        }
    }

    @Override
    public void onFeedItemClosed(boolean closed, @NonNull FeedItem feedItem) {
        @StringRes int message;
        if (closed) {
            refreshFeed();
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                if (feedItem.getUUID().equalsIgnoreCase(currentTourUUID)) {
                    updateFloatingMenuOptions();
                    currentTourUUID = "";
                } else {
                    entourageService.notifyListenersTourResumed();
                }
            }
            if ((entourageService != null) && (userHistory)) {
                entourageService.updateUserHistory(userId, 1, 1);
            }
            message = feedItem.getClosedToastMessage();
        } else {
            message = R.string.tour_close_fail;
            if ((feedItem.getType() == TimestampedObject.TOUR_CARD)
                    &&(feedItem.getStatus()!=null
                    && feedItem.getStatus().equals(FeedItem.STATUS_FREEZED))) {
                message = R.string.tour_freezed;
            }
        }
        if(layoutMain!=null){
            EntourageSnackbar.INSTANCE.make(layoutMain, message, Snackbar.LENGTH_SHORT).show();
        }

        if (loaderStop != null) {
            loaderStop.dismiss();
            loaderStop = null;
        }
    }

    @Override
    public void onUserStatusChanged(@NonNull TourUser user, @NonNull FeedItem feedItem) {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (feedItem.getType() == TimestampedObject.TOUR_CARD || feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            feedItem.setJoinStatus(user.getStatus());
            if (user.getStatus().equals(Tour.JOIN_STATUS_PENDING)) {
                try {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                    JoinRequestOkFragment joinRequestOkFragment = JoinRequestOkFragment.newInstance(feedItem);
//                    joinRequestOkFragment.show(fragmentManager, JoinRequestOkFragment.TAG);
                    TourJoinRequestFragment tourJoinRequestFragment = TourJoinRequestFragment.newInstance(feedItem);
                    tourJoinRequestFragment.show(fragmentManager, TourJoinRequestFragment.TAG);
                } catch (IllegalStateException e) {
                    Timber.w(e);
                }
            }
        }
        updateNewsfeedJoinStatus(feedItem);
        isRequestingToJoin--;
    }

    @Override
    @OnClick(R.id.launcher_tour_outer_view)
    protected void hideTourLauncher() {
        if (mapLauncherLayout.getVisibility() == View.VISIBLE) {
            mapLauncherLayout.setVisibility(View.GONE);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

    @Override
    protected void redrawWholeNewsfeed(@NotNull List<? extends Newsfeed> newsFeeds) {
        if (map != null && newsFeeds.size() > 0 && newsfeedAdapter!=null) {
            for (Polyline polyline:drawnToursMap) {
                polyline.remove();
            }
            drawnToursMap.clear();

            //redraw the whole newsfeed
            for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
                if (timestampedObject.getType() == TimestampedObject.TOUR_CARD) {
                    Tour tour = (Tour) timestampedObject;
                    if (tour.getUUID().equalsIgnoreCase(currentTourUUID)) {
                        continue;
                    }
                    drawNearbyTour(tour, false);
                } else if (timestampedObject.getType() == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage((Entourage) timestampedObject);
                }
            }
            mapClusterManager.cluster();
            //redraw the current ongoing tour, if any
            if (entourageService != null && currentTourUUID != null && currentTourUUID.length() > 0) {
                PolylineOptions line = new PolylineOptions();
                for (Polyline polyline : currentTourLines) {
                    line.addAll(polyline.getPoints());
                }
                line.zIndex(2f);
                line.width(15);
                line.color(color);
                drawnToursMap.add(map.addPolyline(line));

                addCurrentTourEncounters();
            }
        }
    }

    private Tour getCurrentTour() {
        return entourageService != null ? entourageService.getCurrentTour() : null;
    }

    private void startTour(String type) {
        if (entourageService != null && !entourageService.isRunning()) {
            color = getTrackColor(false, type, new Date());
            entourageService.beginTreatment(type);
        }
    }

    private void pauseTour() {
        if (entourageService != null && entourageService.isRunning()) {
            entourageService.pauseTreatment();
        }
    }

    private void resumeTour() {
        if (entourageService != null && entourageService.isRunning()) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_RESTART_TOUR);
                entourageService.resumeTreatment();
        }
    }

    private void launchConfirmationFragment() {
        pauseTour();

        TourEndConfirmationFragment tourEndConfirmationFragment = TourEndConfirmationFragment.newInstance(getCurrentTour());
        tourEndConfirmationFragment.show(getFragmentManager(), TourEndConfirmationFragment.TAG);
    }

    private void addEncounter(Encounter encounter) {
        entourageService.addEncounter(encounter);
    }

    private void updateEncounter(Encounter encounter) {
        entourageService.updateEncounter(encounter);
    }

    private List<? extends Tour> removeRedundantTours(List<? extends Tour> tours, boolean isHistory) {
        if (tours == null) {
            return null;
        }
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (!isHistory) {
                if(newsfeedAdapter==null) {
                    break;//no need to continue here
                }
                Tour retrievedTour = (Tour) newsfeedAdapter.findCard(tour);
                if (retrievedTour.isSame(tour)) {
                    iteratorTours.remove();
                }
            } else {
                if (drawnUserHistory.containsKey(tour.getId())) {
                    iteratorTours.remove();
                }
            }
        }
        return tours;
    }

    @Override
    protected List<? extends Newsfeed> removeRedundantNewsfeed(List<? extends Newsfeed> newsFeedList, boolean isHistory) {
        super.removeRedundantNewsfeed(newsFeedList, isHistory);
        if (newsFeedList == null) {
            return null;
        }
        Iterator iteratorNewsfeed = newsFeedList.iterator();
        while (iteratorNewsfeed.hasNext()) {
            Newsfeed newsfeed = (Newsfeed) iteratorNewsfeed.next();
            if(newsfeed==null) {
                continue;
            }
            if (!isHistory) {
                Object card = newsfeed.getData();
                if (!(card instanceof TimestampedObject)) {
                    iteratorNewsfeed.remove();
                    continue;
                }
                TimestampedObject retrievedCard;
                retrievedCard = newsfeedAdapter!=null?newsfeedAdapter.findCard((TimestampedObject) card):null;
                if (retrievedCard != null) {
                    if (Tour.NEWSFEED_TYPE.equals(newsfeed.getType())) {
                        if (((Tour) retrievedCard).isSame((Tour) card)) {
                            iteratorNewsfeed.remove();
                        }
                    }
                }
            } else {
                if (drawnUserHistory.containsKey(newsfeed.getId())) {
                    iteratorNewsfeed.remove();
                }
            }
        }
        return newsFeedList;
    }

    private List<? extends Tour> removeRecentTours(List<? extends Tour> tours) {
        if (tours == null) {
            return null;
        }
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (newsfeedAdapter!=null && newsfeedAdapter.findCard(tour) != null) {
                iteratorTours.remove();
            }
        }
        return tours;
    }

    private void addCurrentTourEncounters() {
        if(presenter==null) {
            Timber.e("MapPresenter not ready");
            return;
        }

        List<Encounter> encounters = entourageService.getCurrentTour().getEncounters();
        if (!encounters.isEmpty()) {
            for (Encounter encounter : encounters) {
                putEncounterOnMap(encounter);
            }
        }
    }

    private void drawCurrentTour(List<? extends TourPoint> pointsToDraw, String tourType, Date startDate) {
        if (map != null && !pointsToDraw.isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            color = getTrackColor(true, tourType, startDate);
            line.zIndex(2f);
            line.width(15);
            line.color(color);
            for (TourPoint tourPoint : pointsToDraw) {
                line.add(tourPoint.getLocation());
            }
            currentTourLines.add(map.addPolyline(line));
        }
    }

    private void drawNearbyTour(Tour tour, boolean isHistory) {
        if (map != null && drawnToursMap != null && drawnUserHistory != null && tour != null && !tour.getTourPoints().isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            if (isToday(tour.getStartTime())) {
                line.zIndex(1f);
            } else {
                line.zIndex(0f);
            }
            line.width(15);
            line.color(getTrackColor(isHistory, tour.getTourType(), tour.getStartTime()));
            for (TourPoint tourPoint : tour.getTourPoints()) {
                line.add(tourPoint.getLocation());
            }
            if (isHistory) {
                retrievedHistory.put(tour.getId(), tour);
                drawnUserHistory.put(tour.getId(), map.addPolyline(line));
            } else {
                drawnToursMap.add(map.addPolyline(line));
                //addTourCard(tour);
            }
            if (tour.getTourStatus() == null) {
                tour.setTourStatus(FeedItem.STATUS_CLOSED);
            }
            addTourHead(tour);
        }
    }

    private void drawCurrentLocation(LatLng location) {
        if (previousCoordinates != null && map != null) {
            PolylineOptions line = new PolylineOptions();
            line.add(previousCoordinates, location);
            line.zIndex(2f);
            line.width(15);
            line.color(color);
            currentTourLines.add(map.addPolyline(line));
        }
        previousCoordinates = location;
    }

    private void addTourCard(Tour tour) {
        if(newsfeedAdapter==null) {
            return;
        }
        if (newsfeedAdapter.findCard(tour) != null) {
            newsfeedAdapter.updateCard(tour);
        } else {
            newsfeedAdapter.addCardInfoBeforeTimestamp(tour);
        }
    }

    private void addTourHead(Tour tour) {
        if (displayedTourHeads >= MAX_TOUR_HEADS_DISPLAYED || map == null || markersMap.containsKey(tour.hashString())) {
            return;
        }
        displayedTourHeads++;
        if (map != null) {
            MapClusterItem mapClusterItem = new MapClusterItem(tour);
            markersMap.put(tour.hashString(), mapClusterItem);
            mapClusterManager.addItem(mapClusterItem);
        }
    }

    @Subscribe
    @Override
    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        super.onLocationPermissionGranted(event);
    }



    @Override
    void clearAll() {
        super.clearAll();

        currentTourLines.clear();
        drawnToursMap.clear();
        drawnUserHistory.clear();
        previousCoordinates = null;

        displayedTourHeads = 0;
    }

    private void updateNewsfeedJoinStatus(TimestampedObject timestampedObject) {
        if(newsfeedAdapter!=null) {
            newsfeedAdapter.updateCard(timestampedObject);
        }
    }

    private void hideUserHistory() {
        for (final Object o : retrievedHistory.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Tour tour = (Tour) pair.getValue();
            Polyline line = drawnUserHistory.get(tour.getId());
            if(line !=null) {
                line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
            }
        }
    }

    private void showUserHistory() {
        for (final Map.Entry<Long, Polyline> pair: drawnUserHistory.entrySet()) {
            Tour tour = retrievedHistory.get(pair.getKey());
            if(tour!=null) {
                Polyline line = pair.getValue();
                line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
            }
        }
    }

    public void userStatusChanged(PushNotificationContent content, String status) {
        super.userStatusChanged(content, status);
        if (entourageService != null) {
            if (content.isTourRelated() && newsfeedAdapter!=null) {
                TimestampedObject timestampedObject = newsfeedAdapter.findCard(TimestampedObject.TOUR_CARD, content.getJoinableId());
                if (timestampedObject != null) {
                    TourUser user = new TourUser();
                    user.setUserId(userId);
                    user.setStatus(status);
                    entourageService.notifyListenersUserStatusChanged(user, (FeedItem) timestampedObject);
                }
            }
        }
    }

    private int getTrackColor(boolean isHistory, String type, Date date) {
        int color = Color.GRAY;
        if (getContext() == null) {
            return color;
        }
        color = ContextCompat.getColor(getContext(), Tour.getTypeColorRes(type));
        if (!isToday(date)) {
            color = getTransparentColor(color);
        }
        if (isHistory) {
            if (!userHistory) {
                return Color.argb(0, Color.red(color), Color.green(color), Color.blue(color));
            } else {
                return Color.argb(255, Color.red(color), Color.green(color), Color.blue(color));
            }
        }
        return color;
    }

    @Override
    protected void updateUserHistory() {
        if (userHistory) {
            entourageService.updateUserHistory(userId, 1, 500);
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    private void doBindService() {
        if (getActivity() != null) {
            User me = EntourageApplication.me(getActivity());
            if (me == null) {
                // Don't start the service
                return;
            }
            try{
                Intent intent = new Intent(getActivity(), EntourageService.class);
                getActivity().startService(intent);
                getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            } catch(IllegalStateException e) {
                Timber.w(e);
            }
        }
    }

    private void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    @Override
    protected void displayFeedItemOptions(FeedItem feedItem) {
        if (getActivity() != null ) {
            FeedItemOptionsFragment feedItemOptionsFragment = FeedItemOptionsFragment.newInstance(feedItem);
            feedItemOptionsFragment.show(getActivity().getSupportFragmentManager(), FeedItemOptionsFragment.TAG);
        }

    }

    @Subscribe
    @Override
    public void feedItemCloseRequested(Events.OnFeedItemCloseRequestEvent event) {
        super.feedItemCloseRequested(event);
    }

    @Subscribe
    public void checkIntentAction(Events.OnCheckIntentActionEvent event) {
        Intent intent = getActivity().getIntent();

        checkAction(intent.getAction());

        Message message = null;
        if (intent.getExtras() != null) {
            message = (Message) intent.getExtras().getSerializable(PushNotificationManager.PUSH_MESSAGE);
        }
        if (message != null) {
            PushNotificationContent content = message.getContent();
            if (content != null) {
                PushNotificationContent.Extra extra = content.extra;
                switch(intent.getAction()) {
                    case PushNotificationContent.TYPE_NEW_CHAT_MESSAGE:
                    case PushNotificationContent.TYPE_NEW_JOIN_REQUEST:
                    case PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED:
                        if (content.isTourRelated()) {
                            displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.TOUR_CARD);
                        } else if (content.isEntourageRelated()) {
                            displayChosenFeedItem(content.getJoinableUUID(), TimestampedObject.ENTOURAGE_CARD);
                        }
                        break;
                    case PushNotificationContent.TYPE_ENTOURAGE_INVITATION:
                        if (extra != null) {
                            displayChosenFeedItem(String.valueOf(extra.entourageId), TimestampedObject.ENTOURAGE_CARD, extra.invitationId);
                        }
                        break;
                    case PushNotificationContent.TYPE_INVITATION_STATUS:
                        if (extra != null && (content.isEntourageRelated() || content.isTourRelated())) {
                            displayChosenFeedItem(content.getJoinableUUID(), content.isTourRelated() ? TimestampedObject.TOUR_CARD : TimestampedObject.ENTOURAGE_CARD);
                        }
                        break;
                }
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() == null) {
                Timber.e("No activity for service");
                return;
            }
            entourageService = ((EntourageService.LocalBinder) service).getService();
            if(entourageService ==null) {
                Timber.e("Service not found");
                return;
            }
            entourageService.registerServiceListener(MapWithTourFragment.this);
            entourageService.registerNewsFeedListener(MapWithTourFragment.this);

            if (entourageService.isRunning()) {
                updateFloatingMenuOptions();

                currentTourUUID = entourageService.getCurrentTourId();
                //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);

                addCurrentTourEncounters();
            }

            entourageService.updateNewsfeed(pagination, selectedTab);
            if (userHistory) {
                entourageService.updateUserHistory(userId, 1, 500);
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            entourageService.unregisterServiceListener(MapWithTourFragment.this);
            entourageService.unregisterNewsFeedListener(MapWithTourFragment.this);
            entourageService = null;
        }
    }

}
