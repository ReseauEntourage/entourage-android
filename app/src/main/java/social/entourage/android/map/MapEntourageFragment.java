package social.entourage.android.map;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.clustering.ClusterManager;
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
import butterknife.Optional;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.EntourageLocation;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.Invitation;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Announcement;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnEncounterCreated;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.api.tape.Events.OnUserChoiceEvent;
import social.entourage.android.base.EntourageToast;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationFragment;
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
import social.entourage.android.user.edit.UserEditActionZoneFragment;

import static social.entourage.android.Constants.EVENT_FEED_TAB_ALL;
import static social.entourage.android.Constants.EVENT_FEED_TAB_EVENTS;
import static social.entourage.android.Constants.EVENT_SCREEN_06_1;
import static social.entourage.android.Constants.EVENT_SCREEN_06_2;

public class MapEntourageFragment extends Fragment implements BackPressable, TourService.TourServiceListener, TourService.NewsFeedListener, UserEditActionZoneFragment.FragmentListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.fragment_map";

    public static final float ZOOM_REDRAW_LIMIT = 1.1f;
    private static final int REDRAW_LIMIT = 300;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MAX_TOUR_HEADS_DISPLAYED = 10;

    private static final long DELAY_REFRESH_TOURS_INTERVAL = 3000; // 3 seconds delay when starting the timer to refresh the feed
    private static final long REFRESH_TOURS_INTERVAL = 60000; //1 minute in ms

    // Constants used to track the source call of the geolocation popup
    private static final int GEOLOCATION_POPUP_TOUR = 0;
    private static final int GEOLOCATION_POPUP_RECENTER = 1;

    // Radius of the circle where to search for entourages when user taps a heatzone
    private static final int HEATZONE_SEARCH_RADIUS = (int)Entourage.HEATMAP_SIZE / 2; // meters

    // Zoom in level when taping a heatzone
    private static final float ZOOM_HEATZONE = 15.7f;

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

    private String currentTourUUID = "";
    private int color;
    private int displayedTourHeads = 0;

    private List<Polyline> currentTourLines;
    private List<Polyline> drawnToursMap;
    private Map<Long, Polyline> drawnUserHistory;
    private Map<String, Object> markersMap;
    private Map<Long, Tour> retrievedHistory;
    private ClusterManager<MapClusterItem> mapClusterManager;
    private MapClusterItemRenderer mapClusterItemRenderer;
    private boolean initialNewsfeedLoaded = false;

    private int originalMapLayoutHeight;
    private boolean isFullMapShown = false;

    private int isRequestingToJoin = 0;

    private boolean isStopped = false;

    @BindView(R.id.fragment_map_gps_layout)
    LinearLayout gpsLayout;

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
    Button mapDisplayToggle;

    @BindView(R.id.layout_map_longclick)
    RelativeLayout mapLongClickView;

    @BindView(R.id.map_longclick_buttons)
    RelativeLayout mapLongClickButtonsView;

    @Nullable @BindView(R.id.tour_stop_button)
    FloatingActionButton tourStopButton;

    @BindView(R.id.map_fab_menu)
    FloatingActionMenu mapOptionsMenu;

    @BindView(R.id.fragment_map_new_entourages_button)
    Button newEntouragesButton;

    @BindView(R.id.fragment_map_empty_list)
    TextView emptyListTextView;

    @BindView(R.id.fragment_map_empty_list_popup)
    View emptyListPopup;

    @BindView(R.id.fragment_map_entourage_mini_cards)
    EntourageMiniCardsView miniCardsView;

    @BindView(R.id.fragment_map_filter_button)
    View filterButton;

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

    // requested entourage group type
    String entourageGroupType = null;

    // current selected tab
    MapTabItem selectedTab = MapTabItem.ALL_TAB;

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
        drawnToursMap = new ArrayList<>();
        drawnUserHistory = new TreeMap<>();
        markersMap = new TreeMap<>();
        retrievedHistory = new TreeMap<>();

        EntourageEvents.logEvent(Constants.EVENT_OPEN_TOURS_FROM_MENU);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_map, container, false);
        }
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter == null) {
            setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
            presenter.start();
        }
        if (fragmentLifecycleCallbacks == null) {
            fragmentLifecycleCallbacks = new MapEntourageFragmentLifecycleCallbacks();
            getFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
        }

        if (getActivity() != null) {
            ((DrawerActivity)getActivity()).showEditActionZoneFragment();
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
            if (presenter != null) {
                for (int index = 0; index < permissions.length; index++) {
                    if (permissions[index].equalsIgnoreCase(presenter.getUserLocationAccess()) && grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        //checkPermission();
                        EntourageEvents.logEvent(Constants.EVENT_GEOLOCATION_POPUP_REFUSE);
                        BusProvider.getInstance().post(new OnLocationPermissionGranted(false));
                    } else {
                        EntourageEvents.logEvent(Constants.EVENT_GEOLOCATION_POPUP_ACCEPT);
                        BusProvider.getInstance().post(new OnLocationPermissionGranted(true));
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        //presenter.handleLocationPermission();
        if (!isGeolocationGranted()) {
            if (getActivity() != null) {
                ((DrawerActivity)getActivity()).showEditActionZoneFragment(true, this);
            }
        }
        newsfeedListView.addOnScrollListener(scrollListener);
        isStopped = false;
    }

    @Override
    public void onStop() {
        super.onStop();

        newsfeedListView.removeOnScrollListener(scrollListener);
    }

    @Override
    public void onSaveInstanceState(@NonNull final Bundle outState) {
        super.onSaveInstanceState(outState);
        isStopped = true;
    }

    @Override
    public void onResume() {
        super.onResume();

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
        if (mapLauncherLayout != null && mapLauncherLayout.getVisibility() == View.VISIBLE) {
            hideTourLauncher();
            return true;
        }
        if (mapOptionsMenu != null && mapOptionsMenu.isOpened()) {
            mapOptionsMenu.toggle(true);
            return true;
        }
        if (mapLongClickView != null && mapLongClickView.getVisibility() == View.VISIBLE) {
            mapLongClickView.setVisibility(View.GONE);
            if (mapOptionsMenu != null) mapOptionsMenu.setVisibility(View.VISIBLE);
            if (tourService != null && tourService.isRunning()) {
                if (tourStopButton != null) tourStopButton.setVisibility(View.VISIBLE);
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

    public void dismissAllDialogs() {
        if (fragmentLifecycleCallbacks != null) {
            fragmentLifecycleCallbacks.dismissAllDialogs();
        }
    }

    public void putEncounterOnMap(Encounter encounter,
                                  MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        if (map == null) {
            // The map is not yet initialized or the google play services are outdated on the phone
            return;
        }
        MapClusterItem mapClusterItem = presenter.getOnClickListener().getEncounterMapClusterItem(encounter.getId());
        if (mapClusterItem != null) {
            return;
        }
        mapClusterItem = new MapClusterItem(encounter);
        onClickListener.addEncounterMapClusterItem(mapClusterItem, encounter);
        mapClusterManager.addItem(mapClusterItem);
    }

    public void displayChosenFeedItem(String feedItemUUID, int feedItemType) {
        displayChosenFeedItem(feedItemUUID, feedItemType, 0);
    }

    public void displayChosenFeedItem(String feedItemUUID, int feedItemType, long invitationId) {
        //display the feed item
        if (newsfeedAdapter != null) {
            FeedItem feedItem = (FeedItem) newsfeedAdapter.findCard(feedItemType, feedItemUUID);
            if (feedItem != null) {
                displayChosenFeedItem(feedItem, invitationId);
                return;
            }
        }
        if (presenter != null) {
            EntourageEvents.logEvent(Constants.EVENT_FEED_OPEN_ENTOURAGE);
            presenter.openFeedItem(feedItemUUID, feedItemType, invitationId);
        }
    }

    public void displayChosenFeedItem(FeedItem feedItem, int feedRank) {
        displayChosenFeedItem(feedItem, 0, feedRank);
    }

    public void displayChosenFeedItem(FeedItem feedItem, long invitationId) {
        displayChosenFeedItem(feedItem, invitationId, 0);
    }

    public void displayChosenFeedItem(FeedItem feedItem, long invitationId, int feedRank) {
        if (getContext() == null || isStateSaved()) return;
        // decrease the badge count
        EntourageApplication application = EntourageApplication.get(getContext());
        if (application != null) {
            application.removePushNotificationsForFeedItem(feedItem);
        }
        //check if we are not already displaying the tour
        if (getActivity() != null) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            if (fragmentManager != null) {
                TourInformationFragment tourInformationFragment = (TourInformationFragment) fragmentManager.findFragmentByTag(TourInformationFragment.TAG);
                if (tourInformationFragment != null
                        && tourInformationFragment.getFeedItemType() == feedItem.getType()
                        && tourInformationFragment.getFeedItemId() != null
                        && tourInformationFragment.getFeedItemId().equalsIgnoreCase(feedItem.getUUID())
                        ) {
                    //TODO refresh the tour info screen
                    return;
                }
            }
        }
        if (presenter != null) {
            EntourageEvents.logEvent(Constants.EVENT_FEED_OPEN_ENTOURAGE);
            presenter.openFeedItem(feedItem, invitationId, feedRank);
        }
    }

    public void displayChosenFeedItemFromShareURL(String feedItemShareURL, int feedItemType) {
        //display the feed item
        if (presenter != null) {
            EntourageEvents.logEvent(Constants.EVENT_FEED_OPEN_ENTOURAGE);
            presenter.openFeedItem(feedItemShareURL, feedItemType);
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

    public void displayEntouragePopupWhileTour() {
        EntourageEvents.logEvent(Constants.EVENT_FEED_ACTION_CREATE_CLICK);
        // if we have an ongoing tour
        if (isBound && tourService != null && tourService.isRunning()) {
            EntourageEvents.logEvent(Constants.EVENT_ENCOUNTER_POPUP_SHOW);
            // Show the dialog that asks the user if he really wants to create an entourage instead of encounter
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder
                    .setMessage(R.string.entourage_tour_ongoing_description)
                    .setTitle(R.string.entourage_tour_ongoing_title)
                    .setPositiveButton(R.string.entourage_tour_ongoing_proceed, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            EntourageEvents.logEvent(Constants.EVENT_ENCOUNTER_POPUP_ENCOUNTER);
                            onAddEncounter();
                        }
                    })
                    .setNegativeButton(R.string.entourage_tour_ongoing_action, new DialogInterface.OnClickListener() {
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
            if (tourStopButton != null) tourStopButton.setVisibility(View.VISIBLE);
        }
        // Check if we need to show the entourage disclaimer
        if (Configuration.getInstance().showEntourageDisclaimer()) {
            if (presenter != null) {
                presenter.displayEntourageDisclaimer(entourageGroupType);
            }
        } else {
            if (getActivity() != null) {
                ((DrawerActivity) getActivity()).onEntourageDisclaimerAccepted(null);
            }
        }

    }

    public void createEntourage() {
        LatLng location = EntourageLocation.getInstance().getLastCameraPosition().target;
        if (!Entourage.TYPE_OUTING.equalsIgnoreCase(entourageGroupType)) {
            // For demand/contribution, by default select the action zone location, if set
            User me = EntourageApplication.me();
            if (me != null) {
                User.Address address = me.getAddress();
                if (address != null) {
                    location = new LatLng(address.getLatitude(), address.getLongitude());
                }
            }
        }
        if (longTapCoordinates != null) {
            location = longTapCoordinates;
            longTapCoordinates = null;
        }
        if (presenter != null) {
            presenter.createEntourage(location, entourageGroupType);
        }
    }

    public void checkAction(String action, Tour actionTour) {
        if (getActivity() != null && isBound) {
            // 1 : Check if should Resume tour
            if (action != null && ConfirmationFragment.KEY_RESUME_TOUR.equals(action)) {
                resumeTour(actionTour);
            }
            // 2 : Check if should End tour
            else if (action != null && ConfirmationFragment.KEY_END_TOUR.equals(action)) {
                stopFeedItem(actionTour, true);
            }
            // 3 : Check if tour is already paused
            else if (tourService!=null && tourService.isPaused()) {
                launchConfirmationActivity();
            }
            // 4 : Check if should pause tour
            else if (action != null && TourService.KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                launchConfirmationActivity();
            }
            // 5 : Check if should stop tour
            else if (tourService!=null && action != null && TourService.KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
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
        presenter.loadEncounterOnMap(updatedEncounter);
    }

    @Subscribe
    public void onEntourageCreated(Events.OnEntourageCreated event) {
        Entourage entourage = event.getEntourage();
        if (entourage == null) {
            return;
        }

        // Force the map filtering for entourages as ON
        MapFilter mapFilter = MapFilterFactory.getMapFilter();
        mapFilter.entourageCreated();
        if (presenter != null) {
            presenter.saveMapFilter();
        }

        // Update the newsfeed
        clearAll();
        tourService.updateNewsfeed(pagination, selectedTab);

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
            newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab);

            tourService.updateNewsfeed(pagination, selectedTab);
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
        switch (selectedTab) {
            case ALL_TAB:
                clearAll();
                ensureMapVisible();
                pagination.setNextDistance();

                if (newsfeedAdapter != null) {
                    newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab);
                }
                if (tourService != null) {
                    tourService.updateNewsfeed(pagination, selectedTab);
                }
                break;
            case EVENTS_TAB:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_events_url)));
                try {
                    startActivity(browserIntent);
                } catch (ActivityNotFoundException ex) {
                    Toast.makeText(getContext(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Subscribe
    public void onMapTabChanged(Events.OnMapTabSelected event) {
        selectedTab = event.getSelectedTab();

        EntourageEvents.logEvent(selectedTab == MapTabItem.ALL_TAB ? EVENT_FEED_TAB_ALL : EVENT_FEED_TAB_EVENTS);

        filterButton.setVisibility(selectedTab == MapTabItem.ALL_TAB ? View.VISIBLE : View.GONE);

        resetFeed();

        if (newsfeedAdapter != null) {
            newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab);
        }
        if (tourService != null) {
            tourService.updateNewsfeed(pagination, selectedTab);
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
    public void onTourCreated(boolean created,String tourUUID) {
        buttonLaunchTour.setEnabled(true);
        launcherProgressBar.setVisibility(View.GONE);
        if (getActivity() != null) {
            if (created) {
                isFollowing = true;
                currentTourUUID = tourUUID;
                presenter.incrementUserToursCount();
                mapLauncherLayout.setVisibility(View.GONE);
                if (newsfeedListView.getVisibility() == View.VISIBLE) {
                    hideToursList();
                }
                addTourCard(tourService.getCurrentTour());

                mapOptionsMenu.setVisibility(View.VISIBLE);
                updateFloatingMenuOptions();
                if (tourStopButton != null) tourStopButton.setVisibility(View.VISIBLE);

                if (presenter != null) {
                    presenter.setDisplayEncounterDisclaimer(true);
                }
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
            if (!tour.getUUID().equalsIgnoreCase(currentTourUUID)) {
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
        if (getActivity() != null && feedItem != null) {
            if (closed) {

                clearAll();
                newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab);

                if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                    if (feedItem.getUUID().equalsIgnoreCase(currentTourUUID)) {
                        mapOptionsMenu.setVisibility(View.VISIBLE);
                        updateFloatingMenuOptions();
                        if (tourStopButton != null) tourStopButton.setVisibility(View.GONE);
                        //bottomTitleTextView.setText(R.string.activity_map_title_small);

                        currentTourUUID = "";
                    } else {
                        tourService.notifyListenersTourResumed();
                    }
                }

                if (tourService != null) {
                    tourService.updateNewsfeed(pagination, selectedTab);
                    if (userHistory) {
                        tourService.updateUserHistory(userId, 1, 1);
                    }
                }

                if (getActivity() != null) {
                    Toast.makeText(getActivity(), feedItem.getClosedToastMessage(), Toast.LENGTH_SHORT).show();
                }

            } else {
                if (getActivity() != null) {
                    @StringRes int tourClosedFailedId = R.string.tour_close_fail;
                    if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
                        if (feedItem.getStatus()!=null && feedItem.getStatus().equals(FeedItem.STATUS_FREEZED)) {
                            tourClosedFailedId = R.string.tour_freezed;
                        }
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
        if (gpsLayout == null) {
            return;
        }
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

                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layoutMain.getLayoutParams();
                        lp.topMargin -= h;
                        layoutMain.setLayoutParams(lp);
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

                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layoutMain.getLayoutParams();
                        lp.topMargin += h;
                        layoutMain.setLayoutParams(lp);
                    }
                });
            }
            gpsLayout.setVisibility(View.VISIBLE);
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
//            map.clear();
//            markersMap.clear();
//            drawnToursMap.clear();
//            mapClusterManager.clearItems();
//            if (presenter != null) {
//                presenter.getOnClickListener().clear();
//                presenter.getOnGroundOverlayClickListener().clear();
//            }
//            displayedTourHeads = 0;

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
            showNewsfeedBottomView(selectedTab == MapTabItem.ALL_TAB ? newsfeeds.size() < pagination.itemsPerPage : newsfeedAdapter.getDataItemCount() == 0);
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

        // Check if the user allowed geolocation from screen 04.2 (login funnel)
        boolean geolocationAllowedByUser = EntourageApplication.get().getSharedPreferences().getBoolean(EntourageApplication.KEY_GEOLOCATION_ENABLED, true);
        if (!geolocationAllowedByUser) {
            BusProvider.getInstance().post(new OnLocationPermissionGranted(false));
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

//    @Optional
//    @OnClick(R.id.fragment_map_follow_button)
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

    @Optional
    @OnClick({R.id.button_start_tour_launcher, R.id.map_longclick_button_start_tour_launcher})
    public void onStartTourLauncher() {
        EntourageEvents.logEvent(Constants.EVENT_FEED_TOUR_CREATE_CLICK);
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

    @Optional @OnClick(R.id.tour_stop_button)
    public void onStartStopConfirmation() {
        EntourageEvents.logEvent(Constants.EVENT_TOUR_SUSPEND);
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
        EntourageEvents.logEvent(Constants.EVENT_CREATE_ENCOUNTER_CLICK);
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

    @OnClick(R.id.fragment_map_display_toggle)
    public void onDisplayToggle() {
        if (!isFullMapShown) {
            EntourageEvents.logEvent(Constants.EVENT_MAP_MAPVIEW_CLICK);
            if (selectedTab == MapTabItem.EVENTS_TAB && newsfeedAdapter != null) {
                newsfeedAdapter.setSelectedTab(MapTabItem.ALL_TAB);
                onMapTabChanged(new Events.OnMapTabSelected(MapTabItem.ALL_TAB));
            }
        }
        else {
            EntourageEvents.logEvent(Constants.EVENT_MAP_LISTVIEW_CLICK);
        }
        toggleToursList();
    }

    @OnClick({R.id.button_create_entourage, R.id.map_longclick_button_entourage_action})
    protected void onCreateEntourageAction() {
        entourageGroupType = null;
        displayEntouragePopupWhileTour();
    }

    @Optional
    @OnClick({R.id.button_create_outing})
    protected void onCreateOuting() {
        entourageGroupType = Entourage.TYPE_OUTING;
        displayEntouragePopupWhileTour();
    }

    @OnClick(R.id.fragment_map_filter_button)
    public void onShowFilter() {
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

    @OnClick(R.id.launcher_tour_outer_view)
    protected void hideTourLauncher() {
        if (mapLauncherLayout.getVisibility() == View.VISIBLE) {
            mapLauncherLayout.setVisibility(View.GONE);
            mapOptionsMenu.setVisibility(View.VISIBLE);
        }
    }

    // ----------------------------------
    // Map Options handler
    // ----------------------------------

    private void initializeFloatingMenu() {
        mapOptionsMenu.setClosedOnTouchOutside(true);
        mapOptionsMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(final boolean opened) {
                if (opened) {
                    if (getActivity() != null) {
                        if (getActivity() instanceof DrawerActivity) {
                            DrawerActivity activity = (DrawerActivity) getActivity();
                            if (activity.isGuideShown()) {
                                EntourageEvents.logEvent(Constants.EVENT_GUIDE_PLUS_CLICK);
                            } else if (tourService.isRunning()) {
                                EntourageEvents.logEvent(Constants.EVENT_TOUR_PLUS_CLICK);
                            } else  if (isToursListVisible()) {
                                EntourageEvents.logEvent(Constants.EVENT_FEED_PLUS_CLICK);
                            } else {
                                EntourageEvents.logEvent(Constants.EVENT_MAP_PLUS_CLICK);
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
                //Handling special cases
                if (!drawerActivity.isGuideShown()) {
                    User me = EntourageApplication.me(getContext());
                    boolean isPro = (me != null) && me.isPro();
                    if (!isPro && !Configuration.getInstance().showMapFABMenu()) {
                        // Show directly the create entourage disclaimer
                        displayEntouragePopupWhileTour();
                        return;
                    } else if (isBound && tourService != null && tourService.isRunning()) {
                        // Show directly the create encounter
                        onAddEncounter();
                        return;
                    }
                }
                // Let the FAB do it's normal thing
                mapOptionsMenu.toggle(mapOptionsMenu.isAnimated());
            }
        });

        mapOptionsMenuPaddingBottom = mapOptionsMenu.getPaddingBottom();
        FAB_BOTTOM_DELTA = getResources().getDimensionPixelOffset(R.dimen.newsfeed_fab_bottowm_view_delta);

        updateFloatingMenuOptions();
    }

    private void updateFloatingMenuOptions() {
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
            if (startTourButton != null) startTourButton.setVisibility(isPro ? View.INVISIBLE : View.GONE);
            if (tourStopButton != null) tourStopButton.setVisibility(View.GONE);
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
        if (tourStopButton != null) tourStopButton.setVisibility(View.GONE);
        //for public user, start the create entourage funnel directly
        User me = EntourageApplication.me(getActivity());
        boolean isPro = (me != null && me.isPro());
        if (!isPro  || !Configuration.getInstance().showMapFABMenu()) {
            displayEntourageDisclaimer();
            return;
        }
        //get the click point
        Point clickPoint = map.getProjection().toScreenLocation(latLng);
        //update the visible buttons
        boolean isTourRunning = tourService != null && tourService.isRunning();
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_start_tour_launcher).setVisibility(isTourRunning ? View.INVISIBLE : View.VISIBLE);
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
        bH += getResources().getDimensionPixelOffset(R.dimen.newsfeed_longpress_delta_y);
        int marginTop = clickPoint.y - bH;
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
        EntourageSecuredActivity activity = (EntourageSecuredActivity) getActivity();
        if (activity == null) return false;
        return activity.isGeolocationGranted();
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
                    String accessLocation = Manifest.permission.ACCESS_FINE_LOCATION;
                    switch (source) {
                        case GEOLOCATION_POPUP_RECENTER:
                            EntourageEvents.logEvent(Constants.EVENT_FEED_ACTIVATE_GEOLOC_RECENTER);
                            accessLocation = Manifest.permission.ACCESS_COARSE_LOCATION;
                            break;
                        case GEOLOCATION_POPUP_TOUR:
                            EntourageEvents.logEvent(Constants.EVENT_FEED_ACTIVATE_GEOLOC_CREATE_TOUR);
                            break;
                        default:
                            break;
                    }

                    /*
                    if (shouldShowRequestPermissionRationale(accessLocation)) {
                        requestPermissions(new String[]{accessLocation}, PERMISSIONS_REQUEST_LOCATION);
                    } else {
                        // User selected "Never ask again", so show the settings page
                        displayGeolocationPreferences();
                    }
                    */
                    requestPermissions(new String[]{accessLocation}, PERMISSIONS_REQUEST_LOCATION);

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
                    if (getActivity() == null) return;
                    map = googleMap;
                    if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        googleMap.setMyLocationEnabled(true);
                    }
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleMap.getUiSettings().setMapToolbarEnabled(false);

                    mapClusterManager = new ClusterManager<>(getActivity(), map);
                    mapClusterItemRenderer = new MapClusterItemRenderer(getActivity(), map, mapClusterManager);
                    mapClusterManager.setRenderer(mapClusterItemRenderer);
                    mapClusterManager.setOnClusterItemClickListener(presenter.getOnClickListener());

                    initializeMapZoom();
                    map.setOnMarkerClickListener(mapClusterManager);
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
                                    newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab);
                                }
                                pagination = new NewsfeedPagination();
                                tourService.updateNewsfeed(pagination, selectedTab);
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
                                hideTourLauncher();
                                if (isFullMapShown) {
                                    // Hide the minicards if visible
                                    if (miniCardsView.getVisibility() == View.VISIBLE) {
                                        miniCardsView.setVisibility(View.INVISIBLE);
                                    }
                                } else {
                                    toggleToursList();
                                }
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
                        }
                    });
                }
            };

            originalMapLayoutHeight = getResources().getDimensionPixelOffset(R.dimen.newsfeed_map_height);
        }
    }

    public void initializeMapZoom() {
        User me = EntourageApplication.get().getEntourageComponent().getAuthenticationController().getUser();
        if (me != null) {
            User.Address address = me.getAddress();
            if (address != null) {
                centerMap(new LatLng(address.getLatitude(), address.getLongitude()));
                isFollowing = false;
                return;
            }
        }
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    private void initializeToursListView() {
        if (newsfeedAdapter == null) {
            newsfeedListView.setLayoutManager(new LinearLayoutManager(getContext()));
            newsfeedAdapter = new NewsfeedAdapter();
            newsfeedAdapter.setOnMapReadyCallback(onMapReadyCallback);
            newsfeedAdapter.setOnFollowButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    MapEntourageFragment.this.onFollowGeolocation();
                }
            });
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
            if (tourService.getCurrentTourId().equalsIgnoreCase(tour.getUUID())) {
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
            if (tour != null && tourService.getCurrentTourId().equalsIgnoreCase(tour.getUUID()) && tourService.isRunning()) {
                EntourageEvents.logEvent(Constants.EVENT_RESTART_TOUR);
                tourService.resumeTreatment();
            }
        }
    }

    public void stopFeedItem(FeedItem feedItem, boolean success) {
        if (getActivity() != null) {
            if (tourService != null) {
                if (feedItem != null) {
                    if (!tourService.isRunning()) {
                        // Not ongoing tour, just stop the feed item
                        loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(feedItem.getClosingLoaderMessage()), getActivity().getString(R.string.button_loading), true);
                        loaderStop.setCancelable(true);
                        EntourageEvents.logEvent(Constants.EVENT_STOP_TOUR);
                        tourService.stopFeedItem(feedItem, success);
                    } else {
                        if (feedItem.getType() == TimestampedObject.TOUR_CARD && tourService.getCurrentTourId().equalsIgnoreCase(feedItem.getUUID())) {
                            // ongoing tour
                            loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                            loaderStop.setCancelable(true);
                            tourService.endTreatment();
                            EntourageEvents.logEvent(Constants.EVENT_STOP_TOUR);
                        } else {
                            // Not ongoing tour, just stop the feed item
                            loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(feedItem.getClosingLoaderMessage()), getActivity().getString(R.string.button_loading), true);
                            loaderStop.setCancelable(true);
                            EntourageEvents.logEvent(Constants.EVENT_STOP_TOUR);
                            tourService.stopFeedItem(feedItem, success);
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

        ConfirmationFragment confirmationFragment = ConfirmationFragment.newInstance(getCurrentTour());
        confirmationFragment.show(getFragmentManager(), ConfirmationFragment.TAG);
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
            if(newsfeed==null) {
                continue;
            }
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
                    } else if (Announcement.NEWSFEED_TYPE.equals(newsfeed.getType())) {
                        iteratorNewsfeed.remove();
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
        for (final Map.Entry<Long, Polyline> pair: drawnUserHistory.entrySet()) {
            Tour tour = retrievedHistory.get(pair.getKey());
            Polyline line = pair.getValue();
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
        CameraPosition cameraPosition = EntourageLocation.getInstance().getLastCameraPosition();
        if (cameraPosition != null) {
            centerMapAndZoom(latLng, cameraPosition.zoom, false);
        }
    }

    protected void centerMapAndZoom(LatLng latLng, float zoom, boolean animated) {
        CameraPosition cameraPosition = new CameraPosition(latLng, zoom, 0, 0);
        if (map != null) {
            if (animated) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 300, null);
            } else {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
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

    private void drawNearbyEntourage(FeedItem feedItem) {
        if (map != null && markersMap != null && feedItem != null) {
            if (feedItem.getStartPoint() != null) {
                if (markersMap.get(feedItem.hashString()) == null) {
                    LatLng position = feedItem.getStartPoint().getLocation();
                    if (feedItem.showHeatmapAsOverlay()) {
                        BitmapDescriptor heatmapIcon = BitmapDescriptorFactory.fromResource(feedItem.getHeatmapResourceId());
                        GroundOverlayOptions groundOverlayOptions = new GroundOverlayOptions()
                                .image(heatmapIcon)
                                .position(position, Entourage.HEATMAP_SIZE, Entourage.HEATMAP_SIZE)
                                .clickable(true)
                                .anchor(0.5f, 0.5f);

                        markersMap.put(feedItem.hashString(), map.addGroundOverlay(groundOverlayOptions));
                        if (presenter != null) {
                            presenter.getOnGroundOverlayClickListener().addEntourageGroundOverlay(position, feedItem);
                        }
                    } else {
                        // add marker
                        /*
                        Drawable drawable = getResources().getDrawable(feedItem.getHeatmapResourceId());
                        BitmapDescriptor icon = Utils.getBitmapDescriptorFromDrawable(drawable, Entourage.getMarkerSize(getContext()), Entourage.getMarkerSize(getContext()));
                        MarkerOptions markerOptions = new MarkerOptions()
                                    .icon(icon)
                                    .position(position)
                                    .draggable(false)
                                    .anchor(0.5f, 0.5f);
                        Marker marker = map.addMarker(markerOptions);
                        if (presenter != null) {
                            presenter.getOnClickListener().addTourMarker(marker, feedItem);
                        }
                        */
                        MapClusterItem mapClusterItem = new MapClusterItem(feedItem);
                        markersMap.put(feedItem.hashString(), mapClusterItem);
                        mapClusterManager.addItem(mapClusterItem);
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

    private void clearAll() {
        if (map != null) {
            map.clear();
        }

        if (mapClusterManager != null) mapClusterManager.clearItems();
        markersMap.clear();
        currentTourLines.clear();
        drawnToursMap.clear();
        drawnUserHistory.clear();
        if (presenter != null) {
            presenter.getOnClickListener().clear();
            presenter.getOnGroundOverlayClickListener().clear();
        }

        displayedTourHeads = 0;

        resetFeed();

        previousCoordinates = null;
    }

    private void resetFeed() {
        newsfeedAdapter.removeAll();

        // check if we need to cancel the current request
        if (pagination.isLoading && tourService != null) {
            tourService.cancelNewsFeedUpdate();
        }

        pagination.reset();
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
        mapDisplayToggle.setText(R.string.map_top_navigation_full_map);

        ensureMapVisible();

        newsfeedAdapter.setTabVisibility(View.GONE);
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
        mapDisplayToggle.setText(R.string.map_top_navigation_list);
       miniCardsView.setVisibility(View.INVISIBLE);

        hideEmptyListPopup();

        newsfeedAdapter.setTabVisibility(View.VISIBLE);
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
        switch (selectedTab) {
            case ALL_TAB:
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
                break;
            case EVENTS_TAB:
                int position = newsfeedAdapter.getItemCount();
                while (position >= 0) {
                    TimestampedObject card = newsfeedAdapter.getCardAt(position);
                    if (card instanceof FeedItem) {
                        pagination.setLastFeedItemUUID(((FeedItem)card).getUUID());
                        break;
                    }
                    position--;
                }
                break;
        }
    }

    // ----------------------------------
    // Push handling
    // ----------------------------------

    public void onPushNotificationReceived(Message message) {
        //refresh the newsfeed
        if (tourService != null) {
            pagination.isRefreshing = true;
            tourService.updateNewsfeed(pagination, selectedTab);
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
                            if (selectedTab == MapTabItem.ALL_TAB) {
                                pagination.isRefreshing = true;
                                tourService.updateNewsfeed(pagination, selectedTab);
                            }
                        }
                    }
                });
            }
        };
        //schedule the timer
        refreshToursTimer.schedule(refreshToursTimerTask, DELAY_REFRESH_TOURS_INTERVAL, REFRESH_TOURS_INTERVAL);
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
            presenter.openFeedItem(firstInvitation.getEntourageUUID(), FeedItem.ENTOURAGE_CARD, firstInvitation.getId());
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
                if (topFragment == null) {
                    return;
                }
                ((DrawerActivity)getActivity()).showTutorial();
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
            newsfeedAdapter.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab);
        } else {
            if (newsfeedAdapter.getDataItemCount() == 0) {
                // max distance and still no items, show no items info
                newsfeedAdapter.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_ITEMS, selectedTab);
            } else {
                // max distance and items, show no more items info
                newsfeedAdapter.showBottomView(show, NewsfeedBottomViewHolder.CONTENT_TYPE_NO_MORE_ITEMS, selectedTab);
            }
        }
    }

    // ----------------------------------
    // Heatzone Tap Handling
    // ----------------------------------

    protected void handleHeatzoneClick(LatLng location) {
        hideTourLauncher();
        if (isToursListVisible()) {
            centerMapAndZoom(location, ZOOM_HEATZONE, true);
            toggleToursList();
        } else {
            showHeatzoneMiniCardsAtLocation(location);
        }
    }

    protected void showHeatzoneMiniCardsAtLocation(LatLng location) {
        // get the list of entourages near this location
        ArrayList<TimestampedObject> entourageArrayList = new ArrayList<>();
        List<TimestampedObject> feedItemsList = new ArrayList<>(newsfeedAdapter.getItems());
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
        //zoom in the heatzone
        if (map != null) {
            CameraPosition cameraPosition = new CameraPosition(location, ZOOM_HEATZONE, 0, 0);
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    // ----------------------------------
    // UserEditActionZoneFragment.FragmentListener
    // ----------------------------------

    @Override
    public void onUserEditActionZoneFragmentDismiss() {

    }

    @Override
    public void onUserEditActionZoneFragmentAddressSaved() {
        User me = EntourageApplication.me();
        if (me != null) {
            User.Address address = me.getAddress();
            if (address != null) {
                centerMap(new LatLng(address.getLatitude(), address.getLongitude()));
            }
        }
    }

    @Override
    public void onUserEditActionZoneFragmentIgnore() {
        onUserEditActionZoneFragmentAddressSaved();
        if (presenter != null) {
            presenter.handleLocationPermission();
        }
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

                    currentTourUUID = tourService.getCurrentTourId();
                    //bottomTitleTextView.setText(R.string.tour_info_text_ongoing);

                    addCurrentTourEncounters();
                }

                tourService.updateNewsfeed(pagination, selectedTab);
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
        public void onScrolled(@NonNull final RecyclerView recyclerView, final int dx, final int dy) {
            if (dy > 0) {
                // Scrolling down
                int visibleItemCount = recyclerView.getChildCount();
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int firstVisibleItem = linearLayoutManager.findFirstVisibleItemPosition();
                int totalItemCount = linearLayoutManager.getItemCount();
                if (totalItemCount - visibleItemCount <= firstVisibleItem + 2) {
                    if ((tourService != null)&&(tourService.updateNewsfeed(pagination, selectedTab))) {
                            //if update returns false no need to log this...
                        EntourageEvents.logEvent(Constants.EVENT_FEED_SCROLL_LIST);
                    }
                }
            } else {
                // Scrolling up
             }
        }

        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
        }
    }
}
