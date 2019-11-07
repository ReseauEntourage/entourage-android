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
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
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
import social.entourage.android.location.LocationUtils;
import social.entourage.android.tour.choice.ChoiceFragment;
import social.entourage.android.tour.confirmation.ConfirmationFragment;
import social.entourage.android.tour.encounter.CreateEncounterActivity;
import social.entourage.android.tour.TourService;
import social.entourage.android.tour.TourServiceListener;
import social.entourage.android.tour.join.TourJoinRequestFragment;
import social.entourage.android.view.EntourageSnackbar;
import timber.log.Timber;

import static social.entourage.android.tour.TourService.KEY_LOCATION_PROVIDER_DISABLED;

public class MapEntourageWithTourFragment extends MapEntourageFragment implements TourServiceListener {
    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.fragment_map_with_tour";

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
        if (isBound && tourService != null) {
            tourService.unregisterTourServiceListener(MapEntourageWithTourFragment.this);
            doUnbindService();
        }
        super.onDestroy();
    }

    @Override
    public void onLocationStatusUpdated(boolean active) {
        super.onLocationStatusUpdated(active);
        if(shouldShowGPSDialog && !active &&  tourService!=null && tourService.isRunning()) {
            //We always need GPS to be turned on during tour
            shouldShowGPSDialog = false;
            final Intent newIntent = new Intent(this.getContext(), DrawerActivity.class);
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
                && tourService != null
                && tourService.isRunning()
                && tourStopButton != null) {
            tourStopButton.setVisibility(View.VISIBLE);
        }
        return super.onBackPressed();
    }

    @Override
    public void displayEntourageDisclaimer() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTION_CREATE_CLICK);
        // if we have an ongoing tour
        if (getActivity()!=null && isBound && tourService != null && tourService.isRunning()) {
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
        if (tourService != null && tourService.isRunning()&& tourStopButton != null) {
            tourStopButton.setVisibility(View.VISIBLE);
        }
        super.displayEntourageDisclaimer();
    }

    @Override
    protected boolean handleSpecialCasesForFAB() {
        if(!(getActivity() instanceof DrawerActivity)) {
            return true;
        }
        DrawerActivity drawerActivity = (DrawerActivity)getActivity();
        //Handling special cases
        if (!drawerActivity.isGuideShown()) {
            /*User me = EntourageApplication.me(getContext());
            if ((me == null) || !me.isPro()) {
                // Show directly the create entourage disclaimer
                displayEntourageDisclaimer();
                return true;
            } else */if (isBound && tourService != null && tourService.isRunning()) {
                // Show directly the create encounter
                onAddEncounter();
                return true;
            }
        }
        return super.handleSpecialCasesForFAB();
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


    @Override
    public void checkAction(@NonNull String action, Tour actionTour) {
        if (getActivity() != null && isBound) {
            // 1 : Check if should Resume tour
            if (ConfirmationFragment.KEY_RESUME_TOUR.equals(action)) {
                resumeTour(actionTour);
            }
            // 2 : Check if should End tour
            else if (ConfirmationFragment.KEY_END_TOUR.equals(action)) {
                stopFeedItem(actionTour, true);
            }
            // 3 : Check if tour is already paused
            else if (tourService!=null && tourService.isPaused()) {
                launchConfirmationActivity();
            }
            // 4 : Check if should pause tour
            else if (TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                launchConfirmationActivity();
            }
            // 5 : Check if should stop tour
            else if (tourService!=null && TourService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                tourService.endTreatment();
            }
        }
        super.checkAction(action, actionTour);
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
            tourService.updateUserHistory(userId, 1, 500);
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
    @OnClick({R.id.button_start_tour_launcher, R.id.map_longclick_button_start_tour_launcher})
    public void onStartTourLauncher() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_TOUR_CREATE_CLICK);
        if (tourService != null) {
            if (!tourService.isRunning()) {
                // Check if the geolocation is permitted
                if (!LocationUtils.INSTANCE.isLocationEnabled() || !LocationUtils.INSTANCE.isLocationPermissionGranted()) {
                    showAllowGeolocationDialog(GEOLOCATION_POPUP_TOUR);
                    return;
                }
                if (mapOptionsMenu.isOpened()) {
                    mapOptionsMenu.toggle(false);
                }
                mapOptionsMenu.setVisibility(View.GONE);
                mapLongClickView.setVisibility(View.GONE);
                mapLauncherLayout.setVisibility(View.VISIBLE);
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
            launchConfirmationActivity();
        }
    }

    @Optional
    @OnClick({R.id.button_add_tour_encounter, R.id.map_longclick_button_create_encounter})
    public void onAddEncounter() {
        if (getActivity() == null) {
            return;
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_CREATE_ENCOUNTER_CLICK);
        // Hide the create entourage menu ui
        mapLongClickView.setVisibility(View.GONE);
        if (mapOptionsMenu.isOpened()) {
            mapOptionsMenu.toggle(false);
        }

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

    @Override
    protected void updateFloatingMenuOptions() {
        super.updateFloatingMenuOptions();
        if(mapOptionsMenu==null) {
            return;
        }
        View addTourEncounterButton = mapOptionsMenu.findViewById(R.id.button_add_tour_encounter);
        View startTourButton = mapOptionsMenu.findViewById(R.id.button_start_tour_launcher);
        if (tourService != null && tourService.isRunning()) {
            if (addTourEncounterButton != null) addTourEncounterButton.setVisibility(View.INVISIBLE);
            if (startTourButton != null) startTourButton.setVisibility(View.GONE);
            if (tourStopButton != null) tourStopButton.setVisibility(View.VISIBLE);
        } else {
            User me = EntourageApplication.me(getActivity());
            boolean isPro = (me != null && me.isPro());

            if (addTourEncounterButton != null) addTourEncounterButton.setVisibility(View.GONE);
            if (startTourButton != null) startTourButton.setVisibility(isPro ? View.VISIBLE : View.GONE);
            if (tourStopButton != null) tourStopButton.setVisibility(View.GONE);
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
            mapOptionsMenu.setVisibility(View.GONE);
            displayEntourageDisclaimer();
            return;
        }
        //update the visible buttons
        boolean isTourRunning = tourService != null && tourService.isRunning();
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_start_tour_launcher).setVisibility(isTourRunning ? View.INVISIBLE : View.VISIBLE);
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_create_encounter).setVisibility(isTourRunning ? View.VISIBLE : View.GONE);
        super.showLongClickOnMapOptions(latLng);
    }

    // ----------------------------------
    // BUS LISTENERS
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
        mapOptionsMenu.setVisibility(View.VISIBLE);
        if (tourService != null) {
            if (tourStopButton != null) tourStopButton.setVisibility(tourService.isRunning() ? View.VISIBLE : View.GONE);
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

    @Override
    public void onTourCreated(boolean created, @NonNull String tourUUID) {
        if(buttonLaunchTour!=null) {
            buttonLaunchTour.setEnabled(true);
        }
        launcherProgressBar.setVisibility(View.GONE);
        if (getActivity() != null) {
            if (created) {
                isFollowing = true;
                currentTourUUID = tourUUID;
                mapLauncherLayout.setVisibility(View.GONE);
                if (newsfeedListView.getVisibility() == View.VISIBLE) {
                    displayFullMap();
                }
                addTourCard(tourService.getCurrentTour());

                mapOptionsMenu.setVisibility(View.VISIBLE);
                updateFloatingMenuOptions();
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
            if (tourService != null && currentTourUUID != null && currentTourUUID.length() > 0) {
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
        @StringRes int message=0;
        if (closed) {
            refreshFeed();
            if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                if (feedItem.getUUID().equalsIgnoreCase(currentTourUUID)) {
                    if(mapOptionsMenu!=null) {
                        mapOptionsMenu.setVisibility(View.VISIBLE);
                        updateFloatingMenuOptions();
                    }
                    if (tourStopButton != null) {
                        tourStopButton.setVisibility(View.GONE);
                    }
                    currentTourUUID = "";
                } else {
                    tourService.notifyListenersTourResumed();
                }
            }
            if ((tourService != null) && (userHistory)) {
                tourService.updateUserHistory(userId, 1, 1);
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
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                    JoinRequestOkFragment joinRequestOkFragment = JoinRequestOkFragment.newInstance(feedItem);
//                    joinRequestOkFragment.show(fragmentManager, JoinRequestOkFragment.TAG);
                TourJoinRequestFragment tourJoinRequestFragment = TourJoinRequestFragment.newInstance(feedItem);
                tourJoinRequestFragment.show(fragmentManager, TourJoinRequestFragment.TAG);
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
            mapOptionsMenu.setVisibility(View.VISIBLE);
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
            if (tourService != null && currentTourUUID != null && currentTourUUID.length() > 0) {
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
        return tourService != null ? tourService.getCurrentTour() : null;
    }

    private void startTour(String type) {
        if (tourService != null && !tourService.isRunning()) {
            color = getTrackColor(false, type, new Date());
            tourService.beginTreatment(type);
        }
    }

    private void pauseTour() {
        if (tourService != null && tourService.isRunning()) {
            tourService.pauseTreatment();
        }
    }

    private void resumeTour(Tour tour) {
        if (tourService != null) {
            if (tour != null && tourService.getCurrentTourId().equalsIgnoreCase(tour.getUUID()) && tourService.isRunning()) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_RESTART_TOUR);
                tourService.resumeTreatment();
            }
        }
    }

    private void launchConfirmationActivity() {
        pauseTour();

        ConfirmationFragment confirmationFragment = ConfirmationFragment.newInstance(getCurrentTour());
        confirmationFragment.show(getFragmentManager(), ConfirmationFragment.TAG);
    }

    private void addEncounter(Encounter encounter) {
        tourService.addEncounter(encounter);
    }

    private void updateEncounter(Encounter encounter) {
        tourService.updateEncounter(encounter);
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
            if (newsfeedAdapter.findCard(tour) != null) {
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

        List<Encounter> encounters = tourService.getCurrentTour().getEncounters();
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
    public void onMapTabChanged(Events.OnMapTabSelected event) {
        super.onMapTabChanged(event);
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
        if (tourService != null) {
            if (content.isTourRelated() && newsfeedAdapter!=null) {
                TimestampedObject timestampedObject = newsfeedAdapter.findCard(TimestampedObject.TOUR_CARD, content.getJoinableId());
                if (timestampedObject != null) {
                    TourUser user = new TourUser();
                    user.setUserId(userId);
                    user.setStatus(status);
                    tourService.notifyListenersUserStatusChanged(user, (FeedItem) timestampedObject);
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
            tourService.updateUserHistory(userId, 1, 500);
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
                Intent intent = new Intent(getActivity(), TourService.class);
                getActivity().startService(intent);
                getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            } catch(IllegalStateException e) {
                Timber.e(e);
            }
        }
    }

    private void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
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
            tourService = ((TourService.LocalBinder) service).getService();
            if(tourService==null) {
                Timber.e("Tour service not found");
                return;
            }
            tourService.registerTourServiceListener(MapEntourageWithTourFragment.this);
            tourService.registerNewsFeedListener(MapEntourageWithTourFragment.this);

            if (tourService.isRunning()) {
                updateFloatingMenuOptions();

                currentTourUUID = tourService.getCurrentTourId();
                //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);

                addCurrentTourEncounters();
            }

            tourService.updateNewsfeed(pagination, selectedTab);
            if (userHistory) {
                tourService.updateUserHistory(userId, 1, 500);
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregisterTourServiceListener(MapEntourageWithTourFragment.this);
            tourService.unregisterNewsFeedListener(MapEntourageWithTourFragment.this);
            tourService = null;
        }
    }

}
