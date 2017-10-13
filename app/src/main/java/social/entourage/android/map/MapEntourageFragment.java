package social.entourage.android.map;

import android.Manifest;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
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
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnCheckIntentActionEvent;
import social.entourage.android.api.tape.Events.OnEncounterCreated;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.api.tape.Events.OnUserChoiceEvent;
import social.entourage.android.base.EntourageToast;
import social.entourage.android.carousel.CarouselFragment;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationActivity;
import social.entourage.android.map.encounter.CreateEncounterActivity;
import social.entourage.android.map.entourage.minicards.EntourageMiniCardsView;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.map.filter.MapFilterFactory;
import social.entourage.android.map.filter.MapFilterFragment;
import social.entourage.android.map.permissions.NoLocationPermissionFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.information.TourInformationFragment;
import social.entourage.android.map.tour.join.TourJoinRequestFragment;
import social.entourage.android.newsfeed.NewsfeedAdapter;
import social.entourage.android.newsfeed.NewsfeedBottomViewHolder;
import social.entourage.android.newsfeed.NewsfeedPagination;
import social.entourage.android.tools.BusProvider;

import static social.entourage.android.Constants.EVENT_SCREEN_06_1;
import static social.entourage.android.Constants.EVENT_SCREEN_06_2;

public class MapEntourageFragment extends Fragment implements BackPressable, TourService.TourServiceListener, TourService.NewsFeedListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.fragment_map";

    public static final float ZOOM_REDRAW_LIMIT = 1.1f;
    private static final int REDRAW_LIMIT = 300;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MAX_TOUR_HEADS_DISPLAYED = 10;

    private static final long REFRESH_TOURS_INTERVAL = 60000; //1 minute in ms

    private static final int MAX_SCROLL_DELTA_Y = 20;

    // Constants used to track the source call of the geolocation popup
    private static final int GEOLOCATION_POPUP_TOUR = 0;
    private static final int GEOLOCATION_POPUP_RECENTER = 1;

    // Radius of the circle where to search for entourages when user taps a heatzone
    private static final int HEATZONE_SEARCH_RADIUS = (int)Entourage.HEATMAP_SIZE / 2; // meters

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private int userId;
    private boolean userHistory;

    private View toReturn;

    private OnMapReadyCallback onMapReadyCallback;
    private GoogleMap map;

    private LatLng previousCoordinates;
    private Location previousCameraLocation;
    private LatLng longTapCoordinates;
    private float previousCameraZoom = 1.0f;

    private Location previousEmptyListPopupLocation = null;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private ProgressDialog loaderStop;
    private ProgressDialog loaderSearchTours;
    private boolean isBound;
    private boolean isMapLoaded;
    private boolean isFollowing = true;

    private long currentTourId = -1;
    private int color;
    private int displayedTourHeads = 0;

    private List<Polyline> currentTourLines;
    private Map<Long, Polyline> drawnToursMap;
    private Map<Long, Polyline> drawnUserHistory;
    private Map<String, Object> markersMap;
    private Map<Long, Tour> retrievedHistory;
    private boolean initialNewsfeedLoaded = false;
    private BitmapDescriptor heatmapIcon;

    private int originalMapLayoutHeight;
    private boolean isFullMapShown = false;

    private int isRequestingToJoin = 0;

    private boolean isStopped = false;

    @BindView(R.id.fragment_map_gps_layout)
    LinearLayout gpsLayout;

    @BindView(R.id.fragment_map_follow_button)
    View centerButton;

    @BindView(R.id.fragment_map_filter_button)
    View filterButton;

    @BindView(R.id.layout_map_launcher)
    View mapLauncherLayout;

    @BindView(R.id.launcher_tour_go)
    ImageView buttonLaunchTour;

    @BindView(R.id.launcher_tour_type)
    RadioGroup radioGroupType;

    @BindView(R.id.launcher_tour_progressBar)
    ProgressBar launcherProgressBar;

    @BindView(R.id.fragment_map_tours_view)
    RecyclerView newsfeedListView;

    NewsfeedAdapter newsfeedAdapter;

    @BindView(R.id.fragment_map_main_layout)
    RelativeLayout layoutMain;

    @BindView(R.id.fragment_map_display_toggle)
    ToggleButton mapDisplayToggle;

    @BindView(R.id.layout_map_longclick)
    RelativeLayout mapLongClickView;

    @BindView(R.id.map_longclick_buttons)
    RelativeLayout mapLongClickButtonsView;

    @BindView(R.id.tour_stop_button)
    FloatingActionButton tourStopButton;

    FloatingActionMenu mapOptionsMenu;

    @BindView(R.id.fragment_map_new_entourages_button)
    Button newEntouragesButton;

    @BindView(R.id.fragment_map_empty_list)
    TextView emptyListTextView;

    @BindView(R.id.fragment_map_empty_list_popup)
    View emptyListPopup;

    @BindView(R.id.fragment_map_show_guide)
    View showGuideView;

    @BindView(R.id.fragment_map_entourage_mini_cards)
    EntourageMiniCardsView miniCardsView;

    Timer refreshToursTimer;
    TimerTask refreshToursTimerTask;
    final Handler refreshToursHandler = new Handler();

    //pagination
    private NewsfeedPagination pagination = new NewsfeedPagination();

    private OnScrollListener scrollListener = new OnScrollListener();

    //original fab bottom paddings
    private int mapOptionsMenuPaddingBottom;
    // Delta on Y to move the FABs when bottom view is displayed
    private int FAB_BOTTOM_DELTA;
    // FAB Animator
    ValueAnimator fabAnimatorUp;
    ValueAnimator fabAnimatorDown;

    // keeps tracks of the attached fragments
    MapEntourageFragmentLifecycleCallbacks fragmentLifecycleCallbacks;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isBound) {
            doBindService();
        }

        BusProvider.getInstance().register(this);

        currentTourLines = new ArrayList<>();
        drawnToursMap = new TreeMap<>();
        drawnUserHistory = new TreeMap<>();
        markersMap = new TreeMap<>();
        retrievedHistory = new TreeMap<>();

        EntourageEvents.logEvent(Constants.EVENT_OPEN_TOURS_FROM_MENU);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_map, container, false);
        }
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter == null) {
            setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
            presenter.start();
        }
        if (fragmentLifecycleCallbacks == null) {
            fragmentLifecycleCallbacks = new MapEntourageFragmentLifecycleCallbacks();
            getFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
        }
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeMap();
        initializeFloatingMenu();
        initializeToursListView();
        initializeInvitations();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMapComponent.builder()
            .entourageComponent(entourageComponent)
            .mapModule(new MapModule(this))
            .build()
            .inject(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CREATE_ENCOUNTER) {
            if (resultCode == Constants.RESULT_CREATE_ENCOUNTER_OK) {
                Encounter encounter = (Encounter) data.getExtras().getSerializable(CreateEncounterActivity.BUNDLE_KEY_ENCOUNTER);
                addEncounter(encounter);
                presenter.loadEncounterOnMap(encounter);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (int index = 0; index < permissions.length; index++) {
                if (permissions[index].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    //checkPermission();
                    EntourageEvents.logEvent(Constants.EVENT_GEOLOCATION_POPUP_REFUSE);
                    BusProvider.getInstance().post(new OnLocationPermissionGranted(false));
                } else {
                    EntourageEvents.logEvent(Constants.EVENT_GEOLOCATION_POPUP_ACCEPT);
                    BusProvider.getInstance().post(new OnLocationPermissionGranted(true));
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.handleLocationPermission();
        newsfeedListView.addOnScrollListener(scrollListener);
        isStopped = false;
    }

    @Override
    public void onStop() {
        super.onStop();

        newsfeedListView.removeOnScrollListener(scrollListener);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        isStopped = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.activity_tours_title);
            if (isMapLoaded) {
                BusProvider.getInstance().post(new OnCheckIntentActionEvent());
//                if (newsfeedAdapter != null) {
//                    newsfeedAdapter.notifyItemChanged(0);
//                }
            }
        }
        timerStart();
    }

    @Override
    public void onPause() {
        super.onPause();

        timerStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
        if (isBound && tourService != null) {
            tourService.unregisterTourServiceListener(MapEntourageFragment.this);
            doUnbindService();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mapLauncherLayout.getVisibility() == View.VISIBLE) {
            mapLauncherLayout.setVisibility(View.GONE);
            mapOptionsMenu.setVisibility(View.VISIBLE);
            return true;
        }
        if (mapOptionsMenu.isOpened()) {
            mapOptionsMenu.toggle(true);
            return true;
        }
        if (mapLongClickView.getVisibility() == View.VISIBLE) {
            mapLongClickView.setVisibility(View.GONE);
            mapOptionsMenu.setVisibility(View.VISIBLE);
            if (tourService != null && tourService.isRunning()) {
                tourStopButton.setVisibility(View.VISIBLE);
            }
            return true;
        }
        //before closing the fragment, send the cached tour points to server (if applicable)
        if (tourService != null) {
            tourService.updateOngoingTour();
        }
        return false;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void onNotificationExtras(int id, boolean choice) {
        userId = id;
        userHistory = choice;
    }

    public void setOnMarkerClickListener(MapPresenter.OnEntourageMarkerClickListener onMarkerClickListener) {
        if (map != null) {
            map.setOnMarkerClickListener(onMarkerClickListener);
        }
    }

    public void putEncounterOnMap(Encounter encounter,
                                  MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        if (map == null) {
            // The map is not yet initialized or the google play services are outdated on the phone
            return;
        }
        double encounterLatitude = encounter.getLatitude();
        double encounterLongitude = encounter.getLongitude();
        LatLng encounterPosition = new LatLng(encounterLatitude, encounterLongitude);
        BitmapDescriptor encounterIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_encounter);

        MarkerOptions markerOptions = new MarkerOptions().position(encounterPosition)
            .icon(encounterIcon);
        Marker marker = map.addMarker(markerOptions);
        onClickListener.addEncounterMarker(marker, encounter);
    }

    public void initializeMapZoom() {
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    public void displayChosenFeedItem(long feedItemId, int feedItemType) {
        displayChosenFeedItem(feedItemId, feedItemType, 0);
    }

    public void displayChosenFeedItem(long feedItemId, int feedItemType, long invitationId) {
        //display the feed item
        FeedItem feedItem = (FeedItem) newsfeedAdapter.findCard(feedItemType, feedItemId);
        if (feedItem != null) {
            displayChosenFeedItem(feedItem, invitationId);
        } else {
            if (presenter != null) {
                EntourageEvents.logEvent(Constants.EVENT_FEED_OPEN_ENTOURAGE);
                presenter.openFeedItem(feedItemId, feedItemType, invitationId);
            }
        }
    }

    public void displayChosenFeedItem(FeedItem feedItem, int feedRank) {
        displayChosenFeedItem(feedItem, 0, feedRank);
    }

    public void displayChosenFeedItem(FeedItem feedItem, long invitationId) {
        displayChosenFeedItem(feedItem, invitationId, 0);
    }

    public void displayChosenFeedItem(FeedItem feedItem, long invitationId, int feedRank) {
        // decrease the badge count
        EntourageApplication application = EntourageApplication.get(getContext());
        if (application != null) {
            application.removePushNotificationsForFeedItem(feedItem);
        }
        //check if we are not already displaying the tour
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        TourInformationFragment tourInformationFragment = (TourInformationFragment) fragmentManager.findFragmentByTag(TourInformationFragment.TAG);
        if (tourInformationFragment != null && tourInformationFragment.getFeedItemType() == feedItem.getType() && tourInformationFragment.getFeedItemId() == feedItem.getId()) {
            //TODO refresh the tour info screen
            return;
        }
        if (presenter != null) {
            EntourageEvents.logEvent(Constants.EVENT_FEED_OPEN_ENTOURAGE);
            presenter.openFeedItem(feedItem, invitationId, feedRank);
        }
    }

    public void act(TimestampedObject timestampedObject) {
        if (tourService != null) {
            EntourageEvents.logEvent(Constants.EVENT_FEED_OPEN_CONTACT);
            isRequestingToJoin++;
            if (timestampedObject.getType() == TimestampedObject.TOUR_CARD) {
                tourService.requestToJoinTour((Tour) timestampedObject);
            } else if (timestampedObject.getType() == TimestampedObject.ENTOURAGE_CARD) {
                tourService.requestToJoinEntourage((Entourage) timestampedObject);
            } else {
                isRequestingToJoin--;
            }
        } else {
            Toast.makeText(getContext(), R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
        }
    }

    public void displayEntouragePopupWhileTour(final String entourageType) {
        // if we have an ongoing tour
        if (isBound && tourService != null && tourService.isRunning()) {
            EntourageEvents.logEvent(Constants.EVENT_ENCOUNTER_POPUP_SHOW);
            // Show the dialog that asks the user if he really wants to create an entourage instead of encounter
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setMessage(Entourage.TYPE_CONTRIBUTION.equals(entourageType) ? R.string.entourage_tour_ongoing_contribution : R.string.entourage_tour_ongoing_demand)
                    .setTitle(R.string.entourage_tour_ongoing_title)
                    .setPositiveButton(R.string.entourage_tour_ongoing_proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            EntourageEvents.logEvent(Constants.EVENT_ENCOUNTER_POPUP_ENCOUNTER);
                            onAddEncounter();
                        }
                    })
                    .setNegativeButton(R.string.next, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            EntourageEvents.logEvent(Constants.EVENT_ENCOUNTER_POPUP_ENTOURAGE);
                            displayEntourageDisclaimer();
                        }
                    });

            builder.show();
        } else {
            displayEntourageDisclaimer();
        }
    }

    public void displayEntourageDisclaimer() {
        if (mapLongClickView == null) {
            // Binder haven't kicked in yet
            return;
        }
        // Hide the create entourage menu ui
        mapLongClickView.setVisibility(View.GONE);
        if (mapOptionsMenu.isOpened()) {
            mapOptionsMenu.toggle(false);
        }
        mapOptionsMenu.setVisibility(View.VISIBLE);
        if (tourService != null && tourService.isRunning()) {
            tourStopButton.setVisibility(View.VISIBLE);
        }
        // Check if we need to show the entourage disclaimer
        User me = EntourageApplication.me(getActivity());
        if (me == null) {
            return;
        }

        /*
        if (me.isEntourageDisclaimerShown()) {
            // Already shown, display the create entourage fragment
            createEntourage(entourageType);
        } else {
            // Show the disclaimer fragment
            if (presenter != null) {
                presenter.displayEntourageDisclaimer(entourageType, me.isPro());
            }
        }
        */

        // MI: EMA-920 Show the disclaimer ever time
        // Show the disclaimer fragment
        if (presenter != null) {
            presenter.displayEntourageDisclaimer(me.isPro());
        }

    }

    public void createEntourage() {
        LatLng location = EntourageLocation.getInstance().getLastCameraPosition().target;
        if (longTapCoordinates != null) {
            location = longTapCoordinates;
            longTapCoordinates = null;
        }
        if (presenter != null) {
            presenter.createEntourage(location);
        }
    }

    public void checkAction(String action, Tour actionTour) {
        if (getActivity() != null && isBound) {
            // 1 : Check if should Resume tour
            if (action != null && ConfirmationActivity.KEY_RESUME_TOUR.equals(action)) {
                resumeTour(actionTour);
            }
            // 2 : Check if should End tour
            else if (action != null && ConfirmationActivity.KEY_END_TOUR.equals(action)) {
                stopFeedItem(actionTour);
            }
            // 3 : Check if tour is already paused
            else if (tourService.isPaused()) {
                launchConfirmationActivity();
            }
            // 4 : Check if should pause tour
            else if (action != null && TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                launchConfirmationActivity();
            }
            // 5 : Check if should stop tour
            else if (action != null && TourService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                tourService.endTreatment();
            }
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onUserChoiceChanged(OnUserChoiceEvent event) {
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
        User user = EntourageApplication.me(getContext());
        if (user == null || newsfeedAdapter == null) return;
        TourAuthor userAsAuthor = user.asTourAuthor();
        List<TimestampedObject> dirtyList = new ArrayList<>();
        // See which cards needs updating
        for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
            if (!(timestampedObject instanceof FeedItem)) continue;
            FeedItem feedItem = (FeedItem)timestampedObject;
            TourAuthor author = feedItem.getAuthor();
            // Skip null author
            if (author == null) continue;
            // Skip not same author id
            if (author.getUserID() != userAsAuthor.getUserID()) continue;
            // Skip if nothing changed
            if (author.isSame(userAsAuthor)) continue;
            // Update the tour author
            userAsAuthor.setUserName(author.getUserName());
            feedItem.setAuthor(userAsAuthor);
            // Mark as dirty
            dirtyList.add(feedItem);
        }
        // Update the dirty cards
        for (TimestampedObject dirty : dirtyList) {
            newsfeedAdapter.updateCard(dirty);
        }
    }

    @Subscribe
    public void onBetterLocation(OnBetterLocationEvent event) {
        if (event.getLocation() != null) {
            centerMap(event.getLocation());
        }
    }

    @Subscribe
    public void onEncounterCreated(OnEncounterCreated event) {
        Encounter encounter = event.getEncounter();
        if (encounter != null) {
            addEncounter(encounter);
            presenter.loadEncounterOnMap(encounter);
        }
        mapOptionsMenu.setVisibility(View.VISIBLE);
        if (tourService != null) {
            tourStopButton.setVisibility(tourService.isRunning() ? View.VISIBLE : View.GONE);
        }
    }

    @Subscribe
    public void onEncounterUpdated(Events.OnEncounterUpdated event) {
        if (event == null || presenter == null) return;
        Encounter updatedEncounter = event.getEncounter();
        if (updatedEncounter == null) return;
        Marker marker = presenter.getOnClickListener().removeEncounterMarker(updatedEncounter.getId());
        if (marker != null) {
            marker.remove();
        }
        updateEncounter(updatedEncounter);
        presenter.loadEncounterOnMap(updatedEncounter);
    }

    @Subscribe
    public void onEntourageCreated(Events.OnEntourageCreated event) {
        Entourage entourage = event.getEntourage();
        if (entourage == null) {
            return;
        }

        // Force the map filtering for entourages as ON
        MapFilter mapFilter = MapFilterFactory.getMapFilter(getContext());
        mapFilter.entourageTypeContribution = true;
        mapFilter.entourageTypeDemand = true;
        if (presenter != null) {
            presenter.saveMapFilter();
        }

        // Update the newsfeed
        clearAll();
        tourService.updateNewsfeed(pagination);

    }

    @Subscribe
    public void onEntourageUpdated(Events.OnEntourageUpdated event) {
        Entourage entourage = event.getEntourage();
        if (entourage == null) {
            return;
        }
        newsfeedAdapter.updateCard(entourage);
    }

    @Subscribe
    public void onMapFilterChanged(Events.OnMapFilterChanged event) {
        // Save the filter
        if (presenter != null) {
            presenter.saveMapFilter();
        }
        // Refresh the newsfeed
        if (tourService != null) {
            clearAll();
            newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS);

            tourService.updateNewsfeed(pagination);
        }
    }

    @Subscribe
    public void onLocationPermissionGranted(OnLocationPermissionGranted event) {
        if (event == null) {
            return;
        }
        if (event.isPermissionGranted()) {
            onLocationProviderStatusChanged(true);
            if (map != null) {
                try {
                    map.setMyLocationEnabled(true);
                } catch (SecurityException ignored) {
                }
            }
        } else {
            onLocationProviderStatusChanged(false);
        }
    }

    @Subscribe
    public void onNewsfeedLoadMoreRequested(Events.OnNewsfeedLoadMoreEvent event) {
        clearAll();
        ensureMapVisible();
        pagination.setNextDistance();

        if (newsfeedAdapter != null) {
            newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE);
        }
        if (tourService != null) {
            tourService.updateNewsfeed(pagination);
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    void doBindService() {
        if (getActivity() != null) {
            User me = EntourageApplication.me(getActivity());
            if (me == null) {
                // Don't start the service
                return;
            }
            Intent intent = new Intent(getActivity(), TourService.class);
            getActivity().startService(intent);
            getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        }
    }

    void doUnbindService() {
        if (getActivity() != null && isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------

    @Override
    public void onTourCreated(boolean created, long tourId) {
        buttonLaunchTour.setEnabled(true);
        launcherProgressBar.setVisibility(View.GONE);
        if (getActivity() != null) {
            if (created) {
                isFollowing = true;
                currentTourId = tourId;
                presenter.incrementUserToursCount();
                mapLauncherLayout.setVisibility(View.GONE);
                if (newsfeedListView.getVisibility() == View.VISIBLE) {
                    hideToursList();
                }
                addTourCard(tourService.getCurrentTour());

                mapOptionsMenu.setVisibility(View.VISIBLE);
                updateFloatingMenuOptions();
                tourStopButton.setVisibility(View.VISIBLE);

                //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);
            } else {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.tour_creation_fail, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onTourUpdated(LatLng newPoint) {
        drawCurrentLocation(newPoint);
    }

    @Override
    public void onTourResumed(List<TourPoint> pointsToDraw, String tourType, Date startDate) {
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
    public void onLocationUpdated(LatLng location) {
        centerMap(location);
    }

    @Override
    public void onRetrieveToursNearby(List<Tour> tours) {
        //check if there are tours to add or update
        int previousToursCount = newsfeedAdapter.getDataItemCount();
        tours = removeRedundantTours(tours, false);
        Collections.sort(tours, new Tour.TourComparatorOldToNew());
        for (Tour tour : tours) {
            if (currentTourId != tour.getId()) {
                //drawNearbyTour(tour, false);
                addTourCard(tour);
            }
        }
        //recreate the map if needed
        if (tours.size() > 0 && map != null) {
            map.clear();
            for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
                if (timestampedObject.getType() == TimestampedObject.TOUR_CARD) {
                    if (currentTourId != timestampedObject.getId()) {
                        drawNearbyTour((Tour) timestampedObject, false);
                    }
                }
            }
            if (tourService != null && currentTourId != -1) {
                PolylineOptions line = new PolylineOptions();
                for (Polyline polyline : currentTourLines) {
                    line.addAll(polyline.getPoints());
                }
                line.zIndex(2f);
                line.width(15);
                line.color(color);
                map.addPolyline(line);

                Tour currentTour = tourService.getCurrentTour();
                if (currentTour != null) {
                    if (currentTour.getEncounters() != null) {
                        for (Encounter encounter : currentTour.getEncounters()) {
                            presenter.loadEncounterOnMap(encounter);
                        }
                    }
                }
            }
        }

        //show the map if no tours
        if (newsfeedAdapter.getDataItemCount() == 0) {
            hideToursList();
        } else if (previousToursCount == 0) {
            showToursList();
        }
        //scroll to latest
        if (newsfeedAdapter.getDataItemCount() > 0) {
            newsfeedListView.scrollToPosition(0);
        }
    }

    @Override
    public void onRetrieveToursByUserId(List<Tour> tours) {
        tours = removeRedundantTours(tours, true);
        tours = removeRecentTours(tours);
        Collections.sort(tours, new Tour.TourComparatorOldToNew());
        for (Tour tour : tours) {
            if (currentTourId != tour.getId()) {
                drawNearbyTour(tour, true);
            }
        }
    }

    @Override
    public void onUserToursFound(Map<Long, Tour> tours) {
    }

    @Override
    public void onToursFound(Map<Long, Tour> tours) {
        if (loaderSearchTours != null) {
            loaderSearchTours.dismiss();
            loaderSearchTours = null;
        }
        if (getActivity() != null) {
            if (tours.isEmpty()) {
                Toast.makeText(getActivity(), tourService.getString(R.string.tour_info_text_nothing_found), Toast.LENGTH_SHORT).show();
            } else {
                if (tours.size() > 1) {
                    List<Tour> tempList = new ArrayList<>();
                    for (Map.Entry<Long, Tour> entry : tours.entrySet()) {
                        tempList.add(entry.getValue());
                    }
                    Tour.Tours toursList = new Tour.Tours(tempList);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    ChoiceFragment choiceFragment = ChoiceFragment.newInstance(toursList);
                    choiceFragment.show(fragmentManager, "fragment_choice");
                } else {
                    TreeMap<Long, Tour> toursTree = new TreeMap<>(tours);
                    presenter.openFeedItem(toursTree.firstEntry().getValue(), 0, 0);
                }
            }
        }
    }

    @Override
    public void onFeedItemClosed(boolean closed, FeedItem feedItem) {
        if (getActivity() != null) {
            if (closed) {

                clearAll();
                newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS);

                if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                    if (feedItem.getId() == currentTourId) {
                        mapOptionsMenu.setVisibility(View.VISIBLE);
                        updateFloatingMenuOptions();
                        tourStopButton.setVisibility(View.GONE);
                        //bottomTitleTextView.setText(R.string.activity_map_title_small);

                        currentTourId = -1;
                    } else {
                        tourService.notifyListenersTourResumed();
                    }
                }

                if (tourService != null) {
                    tourService.updateNewsfeed(pagination);
                    if (userHistory) {
                        tourService.updateUserHistory(userId, 1, 1);
                    }
                }

                if (getActivity() != null) {
                    @StringRes int tourStatusStringId = R.string.local_service_stopped;
                    if (feedItem.isFreezed()) {
                        tourStatusStringId = R.string.tour_freezed;
                        if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
                            tourStatusStringId = R.string.entourage_info_text_close;
                        }
                    }

                    Toast.makeText(getActivity(), tourStatusStringId, Toast.LENGTH_SHORT).show();
                }

            } else {
                if (getActivity() != null) {
                    @StringRes int tourClosedFailedId = R.string.tour_close_fail;
                    if (feedItem.getStatus().equals(FeedItem.STATUS_FREEZED)) {
                        tourClosedFailedId = R.string.tour_freezed;
                    }
                    Toast.makeText(getActivity(), tourClosedFailedId, Toast.LENGTH_SHORT).show();
                }
            }
            if (loaderStop != null) {
                loaderStop.dismiss();
                loaderStop = null;
            }
        }
    }

    @Override
    public void onLocationProviderStatusChanged(boolean active) {
        if (gpsLayout != null) {
            if (active) {
                if (gpsLayout.getVisibility() == View.VISIBLE) {
                    // Move filter and center buttons up
                    gpsLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                gpsLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                gpsLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }

                            int h = gpsLayout.getHeight();

                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)centerButton.getLayoutParams();
                            lp.topMargin -= h;
                            centerButton.setLayoutParams(lp);

                            lp = (RelativeLayout.LayoutParams)filterButton.getLayoutParams();
                            lp.topMargin -= h;
                            filterButton.setLayoutParams(lp);

                            lp = (RelativeLayout.LayoutParams)mapDisplayToggle.getLayoutParams();
                            lp.topMargin -= h;
                            mapDisplayToggle.setLayoutParams(lp);
                        }
                    });
                }
                gpsLayout.setVisibility(View.GONE);
            } else {
                if (gpsLayout.getVisibility() == View.GONE) {
                    // Move filter and center buttons down, so they are not covered by the gps layout
                    gpsLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                gpsLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            } else {
                                gpsLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            }

                            int h = gpsLayout.getHeight();

                            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)centerButton.getLayoutParams();
                            lp.topMargin += h;
                            centerButton.setLayoutParams(lp);

                            lp = (RelativeLayout.LayoutParams)filterButton.getLayoutParams();
                            lp.topMargin += h;
                            filterButton.setLayoutParams(lp);

                            lp = (RelativeLayout.LayoutParams)mapDisplayToggle.getLayoutParams();
                            lp.topMargin += h;
                            mapDisplayToggle.setLayoutParams(lp);
                        }
                    });
                }
                gpsLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onUserStatusChanged(TourUser user, FeedItem feedItem) {
        if (getActivity() == null || getActivity().isFinishing()) return;
        if (user == null) {
            //error changing the status
            if (isRequestingToJoin > 0 && getContext() != null) {
                Toast.makeText(getContext(), R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
            }
        } else {
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
        }
        isRequestingToJoin--;
    }

    @Override
    public void onNetworkException() {
        if (getActivity() != null) {
            Toast.makeText(getActivity(), R.string.network_error, Toast.LENGTH_LONG).show();
        }
        if (pagination.isLoading) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
        }
    }

    @Override
    public void onCurrentPositionNotRetrieved() {
        if (pagination.isLoading) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
        }
    }

    @Override
    public void onServerException(Throwable throwable) {
        if (getActivity() != null) {
            EntourageToast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_LONG).show();
        }
        if (pagination.isLoading) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
        }
    }

    @Override
    public void onTechnicalException(Throwable throwable) {
        if (getActivity() != null) {
            EntourageToast.makeText(getActivity(), R.string.technical_error, Toast.LENGTH_LONG).show();
        }
        if (pagination.isLoading) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
        }
    }

    @Override
    public void onNewsFeedReceived(List<Newsfeed> newsfeeds) {
        if (newsfeedAdapter == null || newsfeeds == null || !isAdded()) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
            return;
        }

        int previousItemCount = newsfeedAdapter.getDataItemCount();

        newsfeeds = removeRedundantNewsfeed(newsfeeds, false);
        //add or update the received newsfeed
        for (Newsfeed newsfeed : newsfeeds) {
            Object newsfeedData = newsfeed.getData();
            if (newsfeedData != null && (newsfeedData instanceof TimestampedObject)) {
                addNewsfeedCard((TimestampedObject) newsfeedData);
            }
        }
        updatePagination(newsfeeds);

        if (map != null && newsfeeds.size() > 0) {
            // redraw the map
            map.clear();
            markersMap.clear();
            drawnToursMap.clear();
            if (presenter != null) {
                presenter.getOnClickListener().clear();
                presenter.getOnGroundOverlayClickListener().clear();
            }
            displayedTourHeads = 0;
            //redraw the whole newsfeed
            for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
                if (timestampedObject.getType() == TimestampedObject.TOUR_CARD) {
                    Tour tour = (Tour) timestampedObject;
                    if (currentTourId == tour.getId()) {
                        continue;
                    }
                    drawNearbyTour(tour, false);
                } else if (timestampedObject.getType() == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage((Entourage) timestampedObject);
                }
            }
            //redraw the current ongoing tour, if any
            if (tourService != null && currentTourId != -1) {
                PolylineOptions line = new PolylineOptions();
                for (Polyline polyline : currentTourLines) {
                    line.addAll(polyline.getPoints());
                }
                line.zIndex(2f);
                line.width(15);
                line.color(color);
                map.addPolyline(line);

                Tour currentTour = tourService.getCurrentTour();
                if (currentTour != null) {
                    if (currentTour.getEncounters() != null) {
                        for (Encounter encounter : currentTour.getEncounters()) {
                            presenter.loadEncounterOnMap(encounter);
                        }
                    }
                }
            }
        }

        // update the bottom view, if not refreshing
        if (!pagination.isRefreshing) {
            showNewsfeedBottomView(newsfeeds.size() < pagination.itemsPerPage);
        }

        if (newsfeedAdapter.getDataItemCount() == 0) {
            if (!pagination.isRefreshing) {
                hideToursList();
            }
        } else {
            if (!initialNewsfeedLoaded) {
                showToursList();
                initialNewsfeedLoaded = true;
            }
            if (!pagination.isRefreshing && previousItemCount == 0) {
                newsfeedListView.scrollToPosition(0);
            }
        }
        pagination.isLoading = false;
        pagination.isRefreshing = false;
    }

    public void checkPermission(final String permission) {
        if (getActivity() == null) {
            return;
        }

        if (PermissionChecker.checkSelfPermission(getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
            BusProvider.getInstance().post(new OnLocationPermissionGranted(true));
            return;
        }

        if (shouldShowRequestPermissionRationale(permission)) {
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.map_permission_title)
                .setMessage(R.string.map_permission_description)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(new String[]{permission}, PERMISSIONS_REQUEST_LOCATION);
                    }
                })
                .setNegativeButton(R.string.map_permission_refuse, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int i) {
                        NoLocationPermissionFragment noLocationPermissionFragment = new NoLocationPermissionFragment();
                        noLocationPermissionFragment.show(getActivity().getSupportFragmentManager(), NoLocationPermissionFragment.TAG);
                        BusProvider.getInstance().post(new OnLocationPermissionGranted(false));
                    }
                })
                .show();
        } else {
            requestPermissions(new String[]{permission}, PERMISSIONS_REQUEST_LOCATION);
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.fragment_map_gps_layout)
    void displayGeolocationPreferences() {
        if (getActivity() != null) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @OnClick(R.id.fragment_map_follow_button)
    void onFollowGeolocation() {
        EntourageEvents.logEvent(Constants.EVENT_FEED_RECENTERCLICK);
        // Check if geolocation is enabled
        if (!isGeolocationGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_RECENTER);
            return;
        }
        isFollowing = true;
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            centerMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
    }

    @OnClick(R.id.launcher_tour_go)
    void onStartNewTour() {
        buttonLaunchTour.setEnabled(false);
        launcherProgressBar.setVisibility(View.VISIBLE);
        TourType tourType = TourType.findByRessourceId(radioGroupType.getCheckedRadioButtonId());
        startTour(tourType.getName());
        if (tourType == TourType.MEDICAL) {
            EntourageEvents.logEvent(Constants.EVENT_TOUR_MEDICAL);
        } else if (tourType == TourType.BARE_HANDS) {
            EntourageEvents.logEvent(Constants.EVENT_TOUR_SOCIAL);
        } else if (tourType == TourType.ALIMENTARY) {
            EntourageEvents.logEvent(Constants.EVENT_TOUR_DISTRIBUTION);
        }
        EntourageEvents.logEvent(Constants.EVENT_START_TOUR);
    }

    @OnClick(R.id.tour_stop_button)
    public void onStartStopConfirmation() {
        EntourageEvents.logEvent(Constants.EVENT_TOUR_SUSPEND);
        pauseTour();
        if (getActivity() != null) {
            launchConfirmationActivity();
        }
    }

    @OnClick(R.id.map_longclick_button_create_encounter)
    public void onAddEncounter() {
        if (getActivity() == null) {
            return;
        }
        // Hide the create entourage menu ui
        mapLongClickView.setVisibility(View.GONE);
        if (mapOptionsMenu.isOpened()) {
            mapOptionsMenu.toggle(false);
        }

        // MI: EMA-920 Show the disclaimer every time
        // Show the disclaimer fragment
        if (presenter != null) {
            presenter.displayEncounterDisclaimer();
        }
    }

    public void addEncounter() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CreateEncounterActivity.class);
            saveCameraPosition();
            Bundle args = new Bundle();
            args.putLong(CreateEncounterActivity.BUNDLE_KEY_TOUR_ID, currentTourId);
            if (longTapCoordinates != null) {
                //if ongoing tour, show only if the point is in the current tour
                if (tourService != null && tourService.isRunning()) {
                    if (!tourService.isLocationInTour(longTapCoordinates)) {
                        longTapCoordinates = null;
                        mapOptionsMenu.setVisibility(View.VISIBLE);
                        tourStopButton.setVisibility(View.VISIBLE);
                        Toast.makeText(getActivity().getApplicationContext(), R.string.tour_encounter_too_far, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
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
        }
    }

    @OnClick(R.id.fragment_map_display_toggle)
    public void onDisplayToggle() {
        if (mapDisplayToggle.isChecked()) {
            EntourageEvents.logEvent(Constants.EVENT_MAP_MAPVIEW_CLICK);
        }
        else {
            EntourageEvents.logEvent(Constants.EVENT_MAP_LISTVIEW_CLICK);
        }
        toggleToursList();
    }

    @OnClick(R.id.map_longclick_button_entourage_action)
    protected void onCreateEntourageAction() {
        displayEntouragePopupWhileTour(Entourage.TYPE_CONTRIBUTION);
    }

    @OnClick(R.id.fragment_map_filter_button)
    protected void onShowFilter() {
        EntourageEvents.logEvent(Constants.EVENT_FEED_FILTERSCLICK);
        User me = EntourageApplication.me(getActivity());
        boolean isPro = (me != null && me.isPro());
        MapFilterFragment mapFilterFragment = MapFilterFragment.newInstance(isPro);
        mapFilterFragment.show(getFragmentManager(), MapFilterFragment.TAG);
    }

    @OnClick(R.id.fragment_map_new_entourages_button)
    protected void onNewEntouragesReceivedButton() {
        newsfeedListView.scrollToPosition(0);
        newEntouragesButton.setVisibility(View.GONE);
    }

    // ----------------------------------
    // Map Options handler
    // ----------------------------------

    private void initializeFloatingMenu() {
        mapOptionsMenu = ((DrawerActivity) getActivity()).mapOptionsMenu;
        mapOptionsMenu.setClosedOnTouchOutside(true);
        mapOptionsMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(final boolean opened) {
                if (opened) {
                    if (getActivity() != null) {
                        if (getActivity() instanceof DrawerActivity) {
                            DrawerActivity activity = (DrawerActivity) getActivity();
                            if (tourService.isRunning()) {
                                EntourageEvents.logEvent(Constants.EVENT_TOUR_PLUS_CLICK);
                            } else if (activity.isGuideShown()) {
                                EntourageEvents.logEvent(Constants.EVENT_GUIDE_PLUS_CLICK);
                            } else {
                                if (isToursListVisible()) {
                                    EntourageEvents.logEvent(Constants.EVENT_FEED_PLUS_CLICK);
                                } else {
                                    EntourageEvents.logEvent(Constants.EVENT_MAP_PLUS_CLICK);
                                }
                            }
                        }
                    }
                }
            }
        });
        mapOptionsMenu.setOnMenuButtonClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (getActivity() == null) {
                    return;
                }
                DrawerActivity drawerActivity = (DrawerActivity)getActivity();
                if (drawerActivity.isGuideShown()) {
                    // Let the FAB do it's normal thing
                    mapOptionsMenu.toggle(mapOptionsMenu.isAnimated());
                } else {
                    User me = EntourageApplication.me(getContext());
                    boolean isPro = (me != null) && me.isPro();
                    if (!isPro) {
                        // Show directly the create entourage disclaimer
                        drawerActivity.onCreateEntourageClicked();
                    } else {
                        // Let the FAB do it's normal thing
                        mapOptionsMenu.toggle(mapOptionsMenu.isAnimated());
                    }
                }
            }
        });

        mapOptionsMenuPaddingBottom = mapOptionsMenu.getPaddingBottom();
        FAB_BOTTOM_DELTA = getResources().getDimensionPixelOffset(R.dimen.newsfeed_fab_bottowm_view_delta);

        updateFloatingMenuOptions();
    }

    private void updateFloatingMenuOptions() {
        if (tourService != null && tourService.isRunning()) {
            mapOptionsMenu.findViewById(R.id.button_add_tour_encounter).setVisibility(View.INVISIBLE);
            mapOptionsMenu.findViewById(R.id.button_start_tour_launcher).setVisibility(View.GONE);
        } else {
            User me = EntourageApplication.me(getActivity());
            boolean isPro = (me != null && me.isPro());

            mapOptionsMenu.findViewById(R.id.button_add_tour_encounter).setVisibility(View.GONE);
            mapOptionsMenu.findViewById(R.id.button_start_tour_launcher).setVisibility(isPro ? View.INVISIBLE : View.GONE);
        }
    }

    @OnClick(R.id.map_longclick_button_start_tour_launcher)
    public void onStartTourLauncher() {
        if (tourService != null) {
            if (!tourService.isRunning()) {
                // Check if the geolocation is permitted
                if (!isGeolocationGranted()) {
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

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------

    private void showLongClickOnMapOptions(LatLng latLng) {
        //only show when map is in full screen and not visible
        if (!isFullMapShown || mapLongClickView.getVisibility() == View.VISIBLE) {
            return;
        }
        //save the tap coordinates
        longTapCoordinates = latLng;
        //hide the FAB menu
        mapOptionsMenu.setVisibility(View.GONE);
        tourStopButton.setVisibility(View.GONE);
        //for public user, start the create entourage funnel directly
        User me = EntourageApplication.me(getActivity());
        boolean isPro = (me != null && me.isPro());
        if (!isPro) {
            displayEntourageDisclaimer();
            return;
        }
        //get the click point
        Point clickPoint = map.getProjection().toScreenLocation(latLng);
        //update the visible buttons
        boolean isTourRunning = tourService != null && tourService.isRunning();
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_start_tour_launcher).setVisibility(isTourRunning ? View.INVISIBLE : (isPro ? View.VISIBLE : View.GONE));
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_create_encounter).setVisibility(isTourRunning ? View.VISIBLE : View.GONE);
        mapLongClickButtonsView.requestLayout();
        //adjust the buttons holder layout
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        mapLongClickButtonsView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int bW = mapLongClickButtonsView.getMeasuredWidth();
        int bH = mapLongClickButtonsView.getMeasuredHeight();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mapLongClickButtonsView.getLayoutParams();
        int marginLeft = clickPoint.x - bW / 2;
        if (marginLeft + bW > screenSize.x) {
            marginLeft -= bW / 2;
        }
        if (marginLeft < 0) {
            marginLeft = 0;
        }
        int marginTop = clickPoint.y - bH * 3/2;
        if (marginTop < 0) {
            marginTop = clickPoint.y;
        }
        lp.setMargins(marginLeft, marginTop, 0, 0);
        mapLongClickButtonsView.setLayoutParams(lp);
        //show the view
        mapLongClickView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.layout_map_longclick)
    void hideLongClickView() {
        onBackPressed();
    }

    // ----------------------------------
    // PRIVATE METHODS (lifecycle)
    // ----------------------------------

    private boolean isGeolocationGranted() {
        return (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void showAllowGeolocationDialog(final int source) {
        @StringRes int messagedId = R.string.map_error_geolocation_disabled_create_entourage;
        switch (source) {
            case GEOLOCATION_POPUP_RECENTER:
                messagedId = R.string.map_error_geolocation_disabled_recenter;
                break;
            case GEOLOCATION_POPUP_TOUR:
            default:
                break;
        }
        new AlertDialog.Builder(getActivity())
            .setMessage(messagedId)
            .setPositiveButton(R.string.activate, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (source) {
                        case GEOLOCATION_POPUP_RECENTER:
                            EntourageEvents.logEvent(Constants.EVENT_FEED_ACTIVATE_GEOLOC_RECENTER);
                            break;
                        case GEOLOCATION_POPUP_TOUR:
                            EntourageEvents.logEvent(Constants.EVENT_FEED_ACTIVATE_GEOLOC_CREATE_TOUR);
                            break;
                        default:
                            break;
                    }

                    if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                    } else {
                        // User selected "Never ask again", so show the settings page
                        displayGeolocationPreferences();
                    }
                }
            })
            .setNegativeButton(R.string.map_permission_refuse, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int i) {
                    if (mapOptionsMenu.isOpened()) {
                        mapOptionsMenu.toggle(false);
                    }
                }
            })
            .show();
    }

    private void initializeMap() {
        if (onMapReadyCallback == null) {
            onMapReadyCallback = new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    map = googleMap;
                    if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        googleMap.setMyLocationEnabled(true);
                    }
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleMap.getUiSettings().setMapToolbarEnabled(false);

                    initializeMapZoom();
                    setOnMarkerClickListener(presenter.getOnClickListener());
                    map.setOnGroundOverlayClickListener(presenter.getOnGroundOverlayClickListener());

                    googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            CameraPosition cameraPosition = map.getCameraPosition();
                            EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition);
                            Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                            Location newLocation = EntourageLocation.cameraPositionToLocation(null, cameraPosition);
                            float newZoom = cameraPosition.zoom;

                            if (tourService != null && (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT)) {
                                if (previousCameraZoom != newZoom) {
                                    if (previousCameraZoom > newZoom) {
                                        EntourageEvents.logEvent(Constants.EVENT_MAP_ZOOM_IN);
                                    } else {
                                        EntourageEvents.logEvent(Constants.EVENT_MAP_ZOOM_OUT);
                                    }
                                }
                                previousCameraZoom = newZoom;
                                previousCameraLocation = newLocation;

                                // check if we need to cancel the current request
                                if (pagination.isLoading) {
                                    tourService.cancelNewsFeedUpdate();
                                }

                                if (newsfeedAdapter != null) {
                                    newsfeedAdapter.removeAll();
                                    newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE);
                                }
                                pagination = new NewsfeedPagination();
                                tourService.updateNewsfeed(pagination);
                                if (userHistory) {
                                    tourService.updateUserHistory(userId, 1, 500);
                                }
                            }

                            if (isFollowing && currentLocation != null) {
                                if (currentLocation.distanceTo(newLocation) > 1) {
                                    isFollowing = false;
                                }
                            }

                            hideEmptyListPopup();
                        }
                    });

                    googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                        @Override
                        public void onMapClick(LatLng latLng) {
                            if (getActivity() != null) {
                                EntourageEvents.logEvent(Constants.EVENT_FEED_MAPCLICK);
                                if (newsfeedListView.getVisibility() == View.VISIBLE) {
                                    hideToursList();
                                }
                                // EMA-341 Disabling the search tour feature
                        /*
                        else {
                            loaderSearchTours = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_search), getActivity().getString(R.string.button_loading), true);
                            loaderSearchTours.setCancelable(true);
                            tourService.searchToursFromPoint(latLng, userHistory, userId, 1, 500);
                        }
                        */
                            }
                        }
                    });

                    googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(final LatLng latLng) {
                            if (getActivity() != null) {
                                EntourageEvents.logEvent(Constants.EVENT_MAP_LONGPRESS);
                                showLongClickOnMapOptions(latLng);
                            }
                        }
                    });

                    googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                        @Override
                        public void onMapLoaded() {
                            isMapLoaded = true;
                            BusProvider.getInstance().post(new OnCheckIntentActionEvent());
                        }
                    });
                }
            };

            originalMapLayoutHeight = getResources().getDimensionPixelOffset(R.dimen.newsfeed_map_height);
        }
    }

    private void initializeToursListView() {
        if (newsfeedAdapter == null) {
            newsfeedListView.setLayoutManager(new LinearLayoutManager(getContext()));
            newsfeedAdapter = new NewsfeedAdapter();
            newsfeedAdapter.setOnMapReadyCallback(onMapReadyCallback);
            newsfeedListView.setAdapter(newsfeedAdapter);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

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

    public void pauseTour(Tour tour) {
        if (tourService != null && tourService.isRunning()) {
            if (tourService.getCurrentTourId() == tour.getId()) {
                tourService.pauseTreatment();
            }
        }
    }

    public void saveOngoingTour() {
        if (tourService != null) {
            tourService.updateOngoingTour();
        }
    }

    private void resumeTour(Tour tour) {
        if (tourService != null) {
            if (tour != null && tourService.getCurrentTourId() == tour.getId() && tourService.isRunning()) {
                EntourageEvents.logEvent(Constants.EVENT_RESTART_TOUR);
                tourService.resumeTreatment();
            }
        }
    }

    public void stopFeedItem(FeedItem feedItem) {
        if (getActivity() != null) {
            if (tourService != null) {
                if (feedItem != null) {
                    if (!tourService.isRunning()) {
                        // Not ongoing tour, just stop the feed item
                        loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                        loaderStop.setCancelable(true);
                        EntourageEvents.logEvent(Constants.EVENT_STOP_TOUR);
                        tourService.stopFeedItem(feedItem);
                    } else {
                        if (feedItem.getType() == TimestampedObject.TOUR_CARD && tourService.getCurrentTourId() == feedItem.getId()) {
                            // ongoing tour
                            loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                            loaderStop.setCancelable(true);
                            tourService.endTreatment();
                            EntourageEvents.logEvent(Constants.EVENT_STOP_TOUR);
                        } else {
                            // Not ongoing tour, just stop the feed item
                            loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                            loaderStop.setCancelable(true);
                            EntourageEvents.logEvent(Constants.EVENT_STOP_TOUR);
                            tourService.stopFeedItem(feedItem);
                        }
                    }
                } else {
                    if (tourService.isRunning()) {
                        loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                        loaderStop.setCancelable(true);
                        tourService.endTreatment();
                        EntourageEvents.logEvent(Constants.EVENT_STOP_TOUR);
                    }
                }
            }
        }
    }

    public void freezeTour(Tour tour) {
        if (getActivity() != null) {
            if (tourService != null) {
                tourService.freezeTour(tour);
            }
        }
    }

    public void userStatusChanged(PushNotificationContent content, String status) {
        if (tourService != null) {
            TimestampedObject timestampedObject = null;
            if (content.isTourRelated()) {
                timestampedObject = newsfeedAdapter.findCard(TimestampedObject.TOUR_CARD, content.getJoinableId());
            } else if (content.isEntourageRelated()) {
                timestampedObject = newsfeedAdapter.findCard(TimestampedObject.ENTOURAGE_CARD, content.getJoinableId());
            }
            if (timestampedObject != null) {
                TourUser user = new TourUser();
                user.setUserId(userId);
                user.setStatus(status);
                tourService.notifyListenersUserStatusChanged(user, (FeedItem) timestampedObject);
            }
        }
    }

    public void removeUserFromNewsfeedCard(FeedItem card, int userId) {
        if (tourService != null) {
            tourService.removeUserFromFeedItem(card, userId);
        }
    }

    private void launchConfirmationActivity() {
        pauseTour();
        //buttonStartLauncher.setVisibility(View.GONE);
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, getCurrentTour());
        Intent confirmationIntent = new Intent(getActivity(), ConfirmationActivity.class);
        confirmationIntent.putExtras(args);
        getActivity().startActivity(confirmationIntent);
    }

    private void addEncounter(Encounter encounter) {
        tourService.addEncounter(encounter);
    }

    private void updateEncounter(Encounter encounter) {
        tourService.updateEncounter(encounter);
    }

    // ----------------------------------
    // PRIVATE METHODS (views)
    // ----------------------------------

    private List<Tour> removeRedundantTours(List<Tour> tours, boolean isHistory) {
        if (tours == null) {
            return null;
        }
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (!isHistory) {
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

    private List<Newsfeed> removeRedundantNewsfeed(List<Newsfeed> newsfeedList, boolean isHistory) {
        if (newsfeedList == null) {
            return null;
        }
        Iterator iteratorNewsfeed = newsfeedList.iterator();
        while (iteratorNewsfeed.hasNext()) {
            Newsfeed newsfeed = (Newsfeed) iteratorNewsfeed.next();
            if (!isHistory) {
                Object card = newsfeed.getData();
                if (card == null || !(card instanceof TimestampedObject)) {
                    iteratorNewsfeed.remove();
                    continue;
                }
                TimestampedObject retrievedCard;
                retrievedCard = newsfeedAdapter.findCard((TimestampedObject) card);
                if (retrievedCard != null) {
                    if (Tour.NEWSFEED_TYPE.equals(newsfeed.getType())) {
                        if (((Tour) retrievedCard).isSame((Tour) card)) {
                            iteratorNewsfeed.remove();
                        }
                    } else if (Entourage.NEWSFEED_TYPE.equals(newsfeed.getType())) {
                        if (((Entourage) retrievedCard).isSame((Entourage) card)) {
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
        return newsfeedList;
    }

    private List<Tour> removeRecentTours(List<Tour> tours) {
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

    public static int getTransparentColor(int color) {
        return Color.argb(200, Color.red(color), Color.green(color), Color.blue(color));
    }

    private void hideUserHistory() {
        for (final Object o : retrievedHistory.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Tour tour = (Tour) pair.getValue();
            Polyline line = drawnUserHistory.get(tour.getId());
            line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
        }
    }

    private void showUserHistory() {
        for (final Object o : drawnUserHistory.entrySet()) {
            Map.Entry pair = (Map.Entry) o;
            Tour tour = retrievedHistory.get(pair.getKey());
            Polyline line = (Polyline) pair.getValue();
            line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
        }
    }

    public static boolean isToday(Date date) {
        if (date == null) {
            return false;
        }
        Date today = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(date);
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
            cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    protected void centerMapAndZoom(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, EntourageLocation.getInstance().getLastCameraPosition().zoom, 0, 0);
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    private void centerMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, EntourageLocation.getInstance().getLastCameraPosition().zoom, 0, 0);
        centerMap(cameraPosition);
    }

    private void centerMap(CameraPosition cameraPosition) {
        if (map != null && isFollowing) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    public void saveCameraPosition() {
        if (map != null) {
            EntourageLocation.getInstance().saveLastCameraPosition(map.getCameraPosition());
        }
    }

    private void addCurrentTourEncounters() {
        List<Encounter> encounters = tourService.getCurrentTour().getEncounters();
        if (!encounters.isEmpty()) {
            for (Encounter encounter : encounters) {
                presenter.loadEncounterOnMap(encounter);
            }
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

    private void drawCurrentTour(List<TourPoint> pointsToDraw, String tourType, Date startDate) {
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
            boolean existingTour = drawnToursMap.containsKey(tour.getId());
            if (isHistory) {
                retrievedHistory.put(tour.getId(), tour);
                drawnUserHistory.put(tour.getId(), map.addPolyline(line));
            } else {
                drawnToursMap.put(tour.getId(), map.addPolyline(line));
                //addTourCard(tour);
            }
            if (tour.getTourStatus() == null) {
                tour.setTourStatus(FeedItem.STATUS_CLOSED);
            }
            if (!existingTour) {
                addTourHead(tour);
            }
        }
    }

    private void drawNearbyEntourage(Entourage entourage) {
        if (map != null && markersMap != null && entourage != null) {
            if (entourage.getLocation() != null) {
                if (markersMap.get(entourage.hashString()) == null) {
                    LatLng position = entourage.getLocation().getLocation();
                    if (heatmapIcon == null) {
                        heatmapIcon = BitmapDescriptorFactory.fromResource(R.drawable.heat_zone);
                    }
                    GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions()
                        .image(heatmapIcon)
                        .position(position, Entourage.HEATMAP_SIZE, Entourage.HEATMAP_SIZE)
                        .clickable(true)
                        .anchor(0.5f, 0.5f);

                    markersMap.put(entourage.hashString(), map.addGroundOverlay(groundOverlayOptions));
                    if (presenter != null) {
                        presenter.getOnGroundOverlayClickListener().addEntourageGroundOverlay(position, entourage);
                    }
                }
            }
        }
    }

    private void addTourCard(Tour tour) {
        if (newsfeedAdapter.findCard(tour) != null) {
            newsfeedAdapter.updateCard(tour);
        } else {
            newsfeedAdapter.addCardInfoBeforeTimestamp(tour);
        }
    }

    private void addNewsfeedCard(TimestampedObject card) {
        if (newsfeedAdapter.findCard(card) != null) {
            newsfeedAdapter.updateCard(card);
        } else {
            // set the badge count
            if (card instanceof FeedItem) {
                EntourageApplication application = EntourageApplication.get(getContext());
                if (application != null) {
                    application.updateBadgeCountForFeedItem((FeedItem) card);
                }
            }
            // add the card
            if (pagination.isRefreshing) {
                newsfeedAdapter.addCardInfoBeforeTimestamp(card);
            } else {
                newsfeedAdapter.addCardInfo(card);
            }
        }
    }

    private void updateNewsfeedJoinStatus(TimestampedObject timestampedObject) {
        newsfeedAdapter.updateCard(timestampedObject);
    }

    private void addTourHead(Tour tour) {
        if (displayedTourHeads >= MAX_TOUR_HEADS_DISPLAYED || map == null) {
            return;
        }
        displayedTourHeads++;
        TourPoint lastPoint = tour.getTourPoints().get(tour.getTourPoints().size() - 1);
        double latitude = lastPoint.getLatitude();
        double longitude = lastPoint.getLongitude();
        LatLng position = new LatLng(latitude, longitude);

        IconGenerator iconGenerator = new IconGenerator(getContext());
        iconGenerator.setTextAppearance(R.style.OngoingTourMarker);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(tour.getOrganizationName()));

        MarkerOptions markerOptions = new MarkerOptions()
            .position(position)
            .icon(icon)
            .anchor(0.5f, 1.0f);

        if (map != null) {
            Marker marker = map.addMarker(markerOptions);
            markersMap.put(tour.hashString(), marker);
            presenter.getOnClickListener().addTourMarker(marker, tour);
        }
    }

    private void clearAll() {
        if (map != null) {
            map.clear();
        }

        currentTourLines.clear();
        drawnToursMap.clear();
        drawnUserHistory.clear();
        if (presenter != null) {
            presenter.getOnClickListener().clear();
            presenter.getOnGroundOverlayClickListener().clear();
        }

        displayedTourHeads = 0;

        newsfeedAdapter.removeAll();

        // check if we need to cancel the current request
        if (pagination.isLoading && tourService != null) {
            tourService.cancelNewsFeedUpdate();
        }

        pagination.reset();

        previousCoordinates = null;
    }

    private void hideToursList() {

        // show the empty list popup if necessary
        if (newsfeedAdapter.getDataItemCount() == 0) {
            showEmptyListPopup();
        }

        if (isFullMapShown) {
            return;
        }
        isFullMapShown = true;
        newEntouragesButton.setVisibility(View.GONE);
        mapDisplayToggle.setChecked(true);
        showGuideView.setVisibility(View.VISIBLE);

        ensureMapVisible();

        final int targetHeight = layoutMain.getMeasuredHeight();
        newsfeedAdapter.setMapHeight(targetHeight);
        ValueAnimator anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                newsfeedAdapter.setMapHeight(val);
                newsfeedListView.getLayoutManager().requestLayout();
            }

        });
        anim.start();

    }

    protected void showToursList() {
        if (newsfeedListView == null || mapDisplayToggle == null) {
            return;
        }

        if (!isFullMapShown) {
            return;
        }
        isFullMapShown = false;
        mapDisplayToggle.setChecked(false);
        showGuideView.setVisibility(View.GONE);

        hideEmptyListPopup();

        ValueAnimator anim = ValueAnimator.ofInt(layoutMain.getMeasuredHeight(), originalMapLayoutHeight);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int val = (Integer) valueAnimator.getAnimatedValue();
                newsfeedAdapter.setMapHeight(val);
                newsfeedListView.getLayoutManager().requestLayout();
            }

        });
        anim.start();
    }

    public void toggleToursList() {
        if (!isFullMapShown) {
            hideToursList();
            EntourageEvents.logEvent(EVENT_SCREEN_06_2);
        } else {
            showToursList();
            EntourageEvents.logEvent(EVENT_SCREEN_06_1);
        }
    }

    public boolean isToursListVisible() {
        return !isFullMapShown;
    }

    public void ensureMapVisible() {
        newsfeedListView.scrollToPosition(0);
    }

    private void updatePagination(List<Newsfeed> newsfeedList) {
        if (newsfeedList == null || newsfeedList.size() == 0) {
            pagination.loadedItems(null, null);
            return;
        }
        Date newestUpdatedDate = null;
        Date oldestUpdateDate = null;
        for (Newsfeed newsfeed : newsfeedList) {
            Object newsfeedData = newsfeed.getData();
            if (newsfeedData != null && (newsfeedData instanceof FeedItem)) {
                FeedItem feedItem = (FeedItem) newsfeedData;
                if (feedItem.getUpdatedTime() != null) {
                    Date feedUpdatedDate = feedItem.getUpdatedTime();
                    if (newestUpdatedDate == null) {
                        newestUpdatedDate = feedUpdatedDate;
                    } else {
                        if (newestUpdatedDate.before(feedUpdatedDate)) {
                            newestUpdatedDate = feedUpdatedDate;
                        }
                    }
                    if (oldestUpdateDate == null) {
                        oldestUpdateDate = feedUpdatedDate;
                    } else {
                        if (oldestUpdateDate.after(feedUpdatedDate)) {
                            oldestUpdateDate = feedUpdatedDate;
                        }
                    }
                }
            }
        }

        pagination.loadedItems(newestUpdatedDate, oldestUpdateDate);
    }

    // ----------------------------------
    // Push handling
    // ----------------------------------

    public void onPushNotificationReceived(Message message) {
        //refresh the newsfeed
        if (tourService != null) {
            pagination.isRefreshing = true;
            tourService.updateNewsfeed(pagination);
        }
        //update the badge count on tour card
        PushNotificationContent content = message.getContent();
        if (content == null) {
            return;
        }
        if (newsfeedAdapter == null) {
            return;
        }
        long joinableId = content.getJoinableId();
        if (content.isTourRelated()) {
            Tour tour = (Tour) newsfeedAdapter.findCard(TimestampedObject.TOUR_CARD, joinableId);
            if (tour == null) {
                return;
            }
            tour.increaseBadgeCount();
            newsfeedAdapter.updateCard(tour);
        } else if (content.isEntourageRelated()) {
            Entourage entourage = (Entourage) newsfeedAdapter.findCard(TimestampedObject.ENTOURAGE_CARD, joinableId);
            if (entourage == null) {
                return;
            }
            entourage.increaseBadgeCount();
            newsfeedAdapter.updateCard(entourage);
        }
    }

    public void onPushNotificationConsumedForFeedItem(FeedItem feedItem) {
        if (newsfeedAdapter == null) {
            return;
        }
        FeedItem feedItemCard = (FeedItem) newsfeedAdapter.findCard(feedItem);
        if (feedItemCard == null) {
            return;
        }
        feedItemCard.setBadgeCount(0);
        newsfeedAdapter.updateCard(feedItemCard);
    }

    // ----------------------------------
    // Refresh tours timer handling
    // ----------------------------------

    private void timerStart() {
        //create the timer
        refreshToursTimer = new Timer();
        //create the task
        refreshToursTimerTask = new TimerTask() {
            @Override
            public void run() {
                refreshToursHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (tourService != null) {
                            if (!isMapLoaded) {
                                return; //Don't refresh till the map is loaded
                            }
                            pagination.isRefreshing = true;
                            tourService.updateNewsfeed(pagination);
                        }
                    }
                });
            }
        };
        //schedule the timer
        refreshToursTimer.schedule(refreshToursTimerTask, 0, REFRESH_TOURS_INTERVAL);
    }

    private void timerStop() {
        if (refreshToursTimer != null) {
            refreshToursTimer.cancel();
            refreshToursTimer = null;
        }
    }

    // ----------------------------------
    // INVITATIONS
    // ----------------------------------

    private void initializeInvitations() {
        // Check if it's a valid user and onboarding
        User me = EntourageApplication.me(getActivity());
        if (me == null || !me.isOnboardingUser()) {
            return;
        }
        // Retrieve the list of invitations
        if (presenter != null) {
            presenter.getMyPendingInvitations();
        }
    }

    protected void onInvitationsReceived(List<Invitation> invitationList) {
        // Get the user
        User me = EntourageApplication.me(getActivity());
        // Check for errors and empty list
        if (invitationList == null || invitationList.size() == 0) {
            // Check if we need to show the carousel
            if (me != null && me.isOnboardingUser()) {
                showCarousel();
                // Reset the onboarding flag
                if (presenter != null) {
                    presenter.resetUserOnboardingFlag();
                }
            }
            return;
        }
        // Check for null presenter
        if (presenter == null) {
            return;
        }
        for (final Invitation invitation : invitationList) {
            presenter.acceptInvitation(invitation.getId());
        }
        // Show the first invitation
        Invitation firstInvitation = invitationList.get(0);
        if (firstInvitation != null) {
            presenter.openFeedItem(firstInvitation.getEntourageId(), FeedItem.ENTOURAGE_CARD, firstInvitation.getId());
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------

    @OnClick(R.id.fragment_map_empty_list_popup_close)
    protected void onEmptyListPopupClose() {
        if (presenter != null) {
            presenter.setShowNoEntouragesPopup(false);
        }
        hideEmptyListPopup();
    }

    private void showEmptyListPopup() {
        if (previousEmptyListPopupLocation == null) {
            previousEmptyListPopupLocation = EntourageLocation.getInstance().getCurrentLocation();
        } else {
            // Show the popup only we moved from the last position we show it
            Location currentLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getCurrentCameraPosition());
            if (previousEmptyListPopupLocation.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                return;
            }
            previousEmptyListPopupLocation = currentLocation;
        }
        // Check if we need to show the popup
        if (presenter != null && !presenter.isShowNoEntouragesPopup()) {
            return;
        }
        emptyListPopup.setVisibility(View.VISIBLE);
    }

    private void hideEmptyListPopup() {
        emptyListPopup.setVisibility(View.GONE);
    }

    // ----------------------------------
    // CAROUSEL
    // ----------------------------------

    private void showCarousel() {
        Handler h = new Handler();
        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Check if the activity is still running
                if (getActivity() == null || getActivity().isFinishing() || isStopped) {
                    return;
                }
                // Check if the map fragment is still on top
                if (fragmentLifecycleCallbacks == null) return;
                Fragment topFragment = fragmentLifecycleCallbacks.getTopFragment();
                if (topFragment != null &&  !(topFragment instanceof MapEntourageFragment) ) {
                    return;
                }
                CarouselFragment carouselFragment = new CarouselFragment();
                try {
                    carouselFragment.show(getFragmentManager(), CarouselFragment.TAG);
                } catch (Exception e) {
                    // This is just to see if we still get the Illegal state exception
                    EntourageEvents.logEvent("CAROUSEL_EXCEPTION");
                }
            }
        }, Constants.CAROUSEL_DELAY_MILLIS);
    }

    // ----------------------------------
    // Newsfeed Bottom View Handling
    // ----------------------------------

    private void showNewsfeedBottomView(boolean show) {
        if (newsfeedAdapter == null) return;
        if (pagination.isNextDistanceAvailable()) {
            // we can increase the distance
            newsfeedAdapter.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE);
        } else {
            if (newsfeedAdapter.getDataItemCount() == 0) {
                // max distance and still no items, show no items info
                newsfeedAdapter.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS);
            } else {
                // max distance and items, show no more items info
                newsfeedAdapter.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_MORE_ITEMS);
            }
        }
    }

    // ----------------------------------
    // Show Guide button on full map screen
    // ----------------------------------

    @OnClick(R.id.fragment_map_show_guide)
    protected void onShowGuideClicked() {
        if (getActivity() == null) return;
        DrawerActivity drawerActivity = (DrawerActivity) getActivity();
        drawerActivity.onPOILauncherClicked();
    }

    public void onGuideWillShow() {
        if (miniCardsView != null) {
            miniCardsView.setVisibility(View.GONE);
        }
    }

    // ----------------------------------
    // Heatzone Tap Handling
    // ----------------------------------

    protected void handleHeatzoneClick(LatLng location) {
        if (isToursListVisible()) {
            centerMapAndZoom(location);
            toggleToursList();
        } else {
            showHeatzoneMiniCardsAtLocation(location);
        }
    }

    protected void showHeatzoneMiniCardsAtLocation(LatLng location) {
        // get the list of entourages near this location
        ArrayList<TimestampedObject> entourageArrayList = new ArrayList<>();
        List<TimestampedObject> feedItemsList = new ArrayList<>();
        feedItemsList.addAll(newsfeedAdapter.getItems());
        for (TimestampedObject feedItem:feedItemsList
             ) {
            if (feedItem.getType() != TimestampedObject.ENTOURAGE_CARD) continue;
            Entourage entourage = (Entourage)feedItem;
            if (entourage.distanceToLocation(location) < HEATZONE_SEARCH_RADIUS) {
                entourageArrayList.add(entourage);
            }
        }
        if (entourageArrayList.size() == 0) {
            return;
        }
        //show the minicards list
        miniCardsView.setEntourages(entourageArrayList);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() != null) {
                tourService = ((TourService.LocalBinder) service).getService();
                tourService.registerTourServiceListener(MapEntourageFragment.this);
                tourService.registerNewsFeedListener(MapEntourageFragment.this);

                boolean isRunning = tourService != null && tourService.isRunning();
                if (isRunning) {
                    updateFloatingMenuOptions();

                    currentTourId = tourService.getCurrentTourId();
                    //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);

                    addCurrentTourEncounters();
                }

                tourService.updateNewsfeed(pagination);
                if (userHistory) {
                    tourService.updateUserHistory(userId, 1, 500);
                }
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregisterTourServiceListener(MapEntourageFragment.this);
            tourService = null;
        }
    }

    private class OnScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(final RecyclerView recyclerView, final int dx, final int dy) {
            if (dy > 0) {
                // Scrolling down
                int visibleItemCount = recyclerView.getChildCount();
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                if (totalItemCount - visibleItemCount <= firstVisibleItem + 2) {
                    EntourageEvents.logEvent(Constants.EVENT_FEED_SCROLL_LIST);
                    if (tourService != null) {
                        tourService.updateNewsfeed(pagination);
                    }
                }
            } else {
                // Scrolling up
             }
        }

        @Override
        public void onScrollStateChanged(final RecyclerView recyclerView, final int newState) {
        }
    }
}
