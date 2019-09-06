package social.entourage.android.map;

import android.animation.ValueAnimator;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.squareup.otto.Subscribe;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.Optional;
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
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Announcement;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.base.EntourageToast;
import social.entourage.android.configuration.Configuration;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.map.entourage.minicards.EntourageMiniCardsView;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.map.filter.MapFilterFactory;
import social.entourage.android.map.filter.MapFilterFragment;
import social.entourage.android.map.permissions.NoLocationPermissionFragment;
import social.entourage.android.map.tour.NewsFeedListener;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.information.TourInformationFragment;
import social.entourage.android.newsfeed.NewsfeedAdapter;
import social.entourage.android.newsfeed.NewsfeedBottomViewHolder;
import social.entourage.android.newsfeed.NewsfeedPagination;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.user.edit.UserEditActionZoneFragment;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapEntourageFragment extends BaseMapEntourageFragment implements NewsFeedListener, UserEditActionZoneFragment.FragmentListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.fragment_map";

    private static final long DELAY_REFRESH_TOURS_INTERVAL = 3000; // 3 seconds delay when starting the timer to refresh the feed
    private static final long REFRESH_TOURS_INTERVAL = 60000; //1 minute in ms

    // Radius of the circle where to search for entourages when user taps a heatzone
    private static final int HEATZONE_SEARCH_RADIUS = (int)Entourage.HEATMAP_SIZE / 2; // meters

    // Zoom in level when taping a heatzone
    private static final float ZOOM_HEATZONE = 15.7f;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private OnMapReadyCallback onMapReadyCallback;

    protected int userId;

    protected LatLng longTapCoordinates;

    private Location previousEmptyListPopupLocation = null;

    protected TourService tourService;
    protected ProgressDialog loaderStop;

    protected Map<String, Object> markersMap;
    private boolean initialNewsfeedLoaded = false;

    protected int isRequestingToJoin = 0;

    private boolean isStopped = false;
    private final Handler refreshToursHandler = new Handler();

    @BindView(R.id.fragment_map_tours_view)
    RecyclerView newsfeedListView;

    @BindView(R.id.fragment_map_main_layout)
    RelativeLayout layoutMain;

    @BindView(R.id.fragment_map_display_toggle)
    Button mapDisplayToggle;

    @BindView(R.id.fragment_map_new_entourages_button)
    Button newEntouragesButton;

    @BindView(R.id.fragment_map_empty_list)
    TextView emptyListTextView;

    @BindView(R.id.fragment_map_empty_list_popup)
    View emptyListPopup;

    @BindView(R.id.fragment_map_entourage_mini_cards)
    EntourageMiniCardsView miniCardsView;

    protected NewsfeedAdapter newsfeedAdapter;
    private Timer refreshToursTimer;

    //pagination
    protected NewsfeedPagination pagination = new NewsfeedPagination();

    private OnScrollListener scrollListener = new OnScrollListener();
    // keeps tracks of the attached fragments
    private MapEntourageFragmentLifecycleCallbacks fragmentLifecycleCallbacks;

    // requested entourage group type
    private String entourageGroupType = null;

    // current selected tab
    protected MapTabItem selectedTab = MapTabItem.ALL_TAB;

    public MapEntourageFragment() {
        super(R.layout.fragment_map);
        eventLongClick = EntourageEvents.EVENT_MAP_LONGPRESS;
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        BusProvider.getInstance().register(this);

        markersMap = new TreeMap<>();

        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_TOURS_FROM_MENU);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (presenter == null) {
            setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
            presenter.start();
        }
        if (fragmentLifecycleCallbacks == null && getFragmentManager() != null) {
            fragmentLifecycleCallbacks = new MapEntourageFragmentLifecycleCallbacks();
            getFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
        }

        initializeMap();
        initializeFloatingMenu();
        initializeToursListView();
        initializeInvitations();
        if (getActivity() != null) {
            ((DrawerActivity)getActivity()).showEditActionZoneFragment();
        }
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMapComponent.builder()
            .entourageComponent(entourageComponent)
            .mapModule(new MapModule(this))
            .build()
            .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!LocationUtils.INSTANCE.isLocationEnabled() && !LocationUtils.INSTANCE.isLocationPermissionGranted() && getActivity() != null) {
            ((DrawerActivity) getActivity()).showEditActionZoneFragment(this);
        }
        newsfeedListView.addOnScrollListener(scrollListener);
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_FEED_FROM_TAB);
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

        boolean isLocationGranted = LocationUtils.INSTANCE.isLocationPermissionGranted();
        BusProvider.getInstance().post(new OnLocationPermissionGranted(isLocationGranted));
    }

    @Override
    public void onPause() {
        super.onPause();

        timerStop();
    }

    @Override
    public void onDestroy() {
        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (mapOptionsMenu != null && mapOptionsMenu.isOpened()) {
            mapOptionsMenu.toggle(true);
            return true;
        }
        if (mapLongClickView != null && mapLongClickView.getVisibility() == View.VISIBLE) {
            mapLongClickView.setVisibility(View.GONE);
            if (mapOptionsMenu != null) {
                mapOptionsMenu.setVisibility(View.VISIBLE);
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
    }

    public void dismissAllDialogs() {
        if (fragmentLifecycleCallbacks != null) {
            fragmentLifecycleCallbacks.dismissAllDialogs();
        }
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
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE);
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
        if (presenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE);
            presenter.openFeedItem(feedItem, invitationId, feedRank);
        }
    }

    public void displayChosenFeedItemFromShareURL(String feedItemShareURL, int feedItemType) {
        //display the feed item
        if (presenter != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_ENTOURAGE);
            presenter.openFeedItem(feedItemShareURL, feedItemType);
        }
    }

    public void act(TimestampedObject timestampedObject) {
        if (tourService != null) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_OPEN_CONTACT);
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

    public void checkAction(@NonNull String action, Tour actionTour) {
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void onBetterLocation(OnBetterLocationEvent event) {
        if (event.getLocation() != null) {
            centerMap(event.getLocation());
        }
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

        EntourageEvents.logEvent(selectedTab == MapTabItem.ALL_TAB ? EntourageEvents.EVENT_FEED_TAB_ALL : EntourageEvents.EVENT_FEED_TAB_EVENTS);

        if(getActivity()!= null) {
            View filterButton = getActivity().findViewById(R.id.fragment_map_filter_button);
            if(filterButton!= null) {
                filterButton.setVisibility(selectedTab == MapTabItem.ALL_TAB ? View.VISIBLE : View.GONE);
            }
        }

        resetFeed();

        if (newsfeedAdapter != null) {
            newsfeedAdapter.showBottomView(false, NewsfeedBottomViewHolder.CONTENT_TYPE_LOAD_MORE, selectedTab);
        }
        if (tourService != null) {
            tourService.updateNewsfeed(pagination, selectedTab);
        }
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------

    @Override
    public void onLocationUpdated(@NonNull LatLng location) {
        if(tourService.isRunning()) {
            centerMap(location);
        }
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
    public void onServerException(@NonNull Throwable throwable) {
        if (getActivity() != null) {
            EntourageToast.makeText(getActivity(), R.string.server_error, Toast.LENGTH_LONG).show();
        }
        if (pagination.isLoading) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
        }
    }

    @Override
    public void onTechnicalException(@NonNull Throwable throwable) {
        if (getActivity() != null) {
            EntourageToast.makeText(getActivity(), R.string.technical_error, Toast.LENGTH_LONG).show();
        }
        if (pagination.isLoading) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
        }
    }

    @Override
    public void onNewsFeedReceived(@NotNull List<? extends Newsfeed> newsFeeds) {
        if (newsfeedAdapter == null || !isAdded()) {
            pagination.isLoading = false;
            pagination.isRefreshing = false;
            return;
        }

        int previousItemCount = newsfeedAdapter.getDataItemCount();

        newsFeeds = removeRedundantNewsfeed(newsFeeds, false);
        //add or update the received newsfeed
        for (Newsfeed newsfeed : newsFeeds) {
            Object newsfeedData = newsfeed.getData();
            if ((newsfeedData instanceof TimestampedObject)) {
                addNewsfeedCard((TimestampedObject) newsfeedData);
            }
        }
        updatePagination(newsFeeds);

        redrawWholeNewsfeed(newsFeeds);

        // update the bottom view, if not refreshing
        if (!pagination.isRefreshing) {
            showNewsfeedBottomView(selectedTab == MapTabItem.ALL_TAB ? newsFeeds.size() < pagination.itemsPerPage : newsfeedAdapter.getDataItemCount() == 0);
        }

        if (newsfeedAdapter.getDataItemCount() == 0) {
            if (!pagination.isRefreshing) {
                displayFullMap();
            }
        } else {
            if (!initialNewsfeedLoaded) {
                displayListWithMapHeader();
                initialNewsfeedLoaded = true;
            }
            if (!pagination.isRefreshing && previousItemCount == 0) {
                newsfeedListView.scrollToPosition(0);
            }
        }
        pagination.isLoading = false;
        pagination.isRefreshing = false;
    }

    protected void redrawWholeNewsfeed(@NotNull List<? extends Newsfeed> newsFeeds) {
        if (map != null && newsFeeds.size() > 0) {
            //redraw the whole newsfeed
            for (TimestampedObject timestampedObject : newsfeedAdapter.getItems()) {
                if (timestampedObject.getType() == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage((Entourage) timestampedObject);
                }
            }
            mapClusterManager.cluster();
        }
    }

    private void checkPermission() {
        if (getActivity() == null) {
            return;
        }
        if (LocationUtils.INSTANCE.isLocationPermissionGranted()) {
            BusProvider.getInstance().post(new OnLocationPermissionGranted(true));
            return;
        }

        // Check if the user allowed geolocation from screen 04.2 (login funnel)
        boolean geolocationAllowedByUser = EntourageApplication.get().getSharedPreferences().getBoolean(EntourageApplication.KEY_GEOLOCATION_ENABLED, true);
        if (!geolocationAllowedByUser) {
            BusProvider.getInstance().post(new OnLocationPermissionGranted(false));
            return;
        }

        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            new AlertDialog.Builder(getActivity())
                .setTitle(R.string.map_permission_title)
                .setMessage(R.string.map_permission_description)
                .setPositiveButton(getString(R.string.activate), (dialogInterface, i) ->
                        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION))
                .setNegativeButton(R.string.map_permission_refuse, (dialog, i) -> {
                    NoLocationPermissionFragment noLocationPermissionFragment = new NoLocationPermissionFragment();
                    noLocationPermissionFragment.show(getActivity().getSupportFragmentManager(), NoLocationPermissionFragment.TAG);
                    BusProvider.getInstance().post(new OnLocationPermissionGranted(false));
                })
                .show();
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @OnClick(R.id.fragment_map_display_toggle)
    public void onDisplayToggle() {
        if (!isFullMapShown) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_MAPVIEW_CLICK);
            if (selectedTab == MapTabItem.EVENTS_TAB && newsfeedAdapter != null) {
                newsfeedAdapter.setSelectedTab(MapTabItem.ALL_TAB);
                onMapTabChanged(new Events.OnMapTabSelected(MapTabItem.ALL_TAB));
            }
        }
        else {
            EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_LISTVIEW_CLICK);
        }
        toggleToursList();
    }

    @OnClick({R.id.button_create_entourage, R.id.map_longclick_button_entourage_action})
    protected void onCreateEntourageAction() {
        entourageGroupType = null;
        displayEntourageDisclaimer();
    }

    @Optional
    @OnClick({R.id.button_create_outing})
    protected void onCreateOuting() {
        entourageGroupType = Entourage.TYPE_OUTING;
        displayEntourageDisclaimer();
    }

    @OnClick(R.id.fragment_map_filter_button)
    public void onShowFilter() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_FILTERSCLICK);
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
        mapOptionsMenu.setClosedOnTouchOutside(true);
        mapOptionsMenu.setOnMenuToggleListener(opened -> {
            if (opened) {
                if ((getActivity() != null) &&(getActivity() instanceof DrawerActivity)) {
                    DrawerActivity activity = (DrawerActivity) getActivity();
                    if (activity.isGuideShown()) {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_PLUS_CLICK);
                    } else if (tourService!=null && tourService.isRunning()) {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_TOUR_PLUS_CLICK);
                    } else  if (isToursListVisible()) {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_PLUS_CLICK);
                    } else {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_PLUS_CLICK);
                    }
                }
            }
        });
        mapOptionsMenu.setOnMenuButtonClickListener(v -> {
            if(handleSpecialCasesForFAB())
                return;
            // Let the FAB do it's normal thing
            mapOptionsMenu.toggle(mapOptionsMenu.isAnimated());
        });

        updateFloatingMenuOptions();
    }

    protected boolean handleSpecialCasesForFAB() {

        if(!(getActivity() instanceof DrawerActivity)) {
            return true;
        }
        DrawerActivity drawerActivity = (DrawerActivity)getActivity();
        //Handling special cases
        if (!drawerActivity.isGuideShown()) {
            if (!Configuration.getInstance().showMapFABMenu()) {
                // Show directly the create entourage disclaimer
                displayEntourageDisclaimer();
                return true;
            }
        }
        return false;
    }

    @Subscribe
    @Override
    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        super.onLocationPermissionGranted(event);
    }

    protected void updateFloatingMenuOptions() {
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------

    @Override
    protected  void showLongClickOnMapOptions(LatLng latLng) {
        //save the tap coordinates
        longTapCoordinates = latLng;
        //hide the FAB menu
        if (!Configuration.getInstance().showMapFABMenu()) {
            mapOptionsMenu.setVisibility(View.GONE);
            displayEntourageDisclaimer();
            return;
        }
        //update the visible buttons
        mapLongClickButtonsView.requestLayout();
        super.showLongClickOnMapOptions(latLng);
    }

    // ----------------------------------
    // PRIVATE METHODS (lifecycle)
    // ----------------------------------

    @Override
    protected void initializeMap() {
        originalMapLayoutHeight = getResources().getDimensionPixelOffset(R.dimen.newsfeed_map_height);
        if (onMapReadyCallback == null) {
            onMapReadyCallback = this::onMapReady;
        }
    }

    @Override
    protected void saveCameraPosition() {
        if (map != null) {
            EntourageLocation.getInstance().saveLastCameraPosition(map.getCameraPosition());
        }
    }

    @Override
    protected DefaultClusterRenderer getRenderer() {
        return new MapClusterItemRenderer(getActivity(), map, mapClusterManager);
    }

    protected void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap,
                presenter.getOnClickListener(),
                presenter.getOnGroundOverlayClickListener()
        );

        map.setOnCameraIdleListener(() -> {
            CameraPosition cameraPosition = map.getCameraPosition();
            EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition);
            Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
            Location newLocation = EntourageLocation.cameraPositionToLocation(null, cameraPosition);
            float newZoom = cameraPosition.zoom;

            if (tourService != null && (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT)) {
                if (previousCameraZoom != newZoom) {
                    if (previousCameraZoom > newZoom) {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_ZOOM_IN);
                    } else {
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MAP_ZOOM_OUT);
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
                updateUserHistory();

            }

            if (isFollowing && currentLocation != null) {
                if (currentLocation.distanceTo(newLocation) > 1) {
                    isFollowing = false;
                }
            }

            hideEmptyListPopup();
        });

        map.setOnMapClickListener(latLng -> {
            if (getActivity() != null) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_MAPCLICK);
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
        });
    }

    protected void updateUserHistory() {
    }

    private void initializeToursListView() {
        if (newsfeedAdapter == null) {
            newsfeedListView.setLayoutManager(new LinearLayoutManager(getContext()));
            newsfeedAdapter = new NewsfeedAdapter();
            newsfeedAdapter.setOnMapReadyCallback(onMapReadyCallback);
            newsfeedAdapter.setOnFollowButtonClickListener(v -> onFollowGeolocation());
            newsfeedListView.setAdapter(newsfeedAdapter);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------
    protected void hideTourLauncher() {
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

    public void stopFeedItem(FeedItem feedItem, boolean success) {
        if (getActivity() != null) {
            if (tourService != null) {
                if (feedItem != null) {
                    if (!tourService.isRunning()) {
                        // Not ongoing tour, just stop the feed item
                        loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(feedItem.getClosingLoaderMessage()), getActivity().getString(R.string.button_loading), true);
                        loaderStop.setCancelable(true);
                        EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR);
                        tourService.stopFeedItem(feedItem, success);
                    } else {
                        if (feedItem.getType() == TimestampedObject.TOUR_CARD && tourService.getCurrentTourId().equalsIgnoreCase(feedItem.getUUID())) {
                            // ongoing tour
                            loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                            loaderStop.setCancelable(true);
                            tourService.endTreatment();
                            EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR);
                        } else {
                            // Not ongoing tour, just stop the feed item
                            loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(feedItem.getClosingLoaderMessage()), getActivity().getString(R.string.button_loading), true);
                            loaderStop.setCancelable(true);
                            EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR);
                            tourService.stopFeedItem(feedItem, success);
                        }
                    }
                } else {
                    if (tourService.isRunning()) {
                        loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                        loaderStop.setCancelable(true);
                        tourService.endTreatment();
                        EntourageEvents.logEvent(EntourageEvents.EVENT_STOP_TOUR);
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
            if (content.isEntourageRelated()) {
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

    public static int getTransparentColor(int color) {
        return Color.argb(200, Color.red(color), Color.green(color), Color.blue(color));
    }

    // ----------------------------------
    // PRIVATE METHODS (views)
    // ----------------------------------

    protected List<? extends Newsfeed> removeRedundantNewsfeed(List<? extends Newsfeed> newsFeedList, boolean isHistory) {
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
                retrievedCard = newsfeedAdapter.findCard((TimestampedObject) card);
                if (retrievedCard != null) {
                    if (Entourage.NEWSFEED_TYPE.equals(newsfeed.getType())) {
                        if (((Entourage) retrievedCard).isSame((Entourage) card)) {
                            iteratorNewsfeed.remove();
                        }
                    } else if (Announcement.NEWSFEED_TYPE.equals(newsfeed.getType())) {
                        iteratorNewsfeed.remove();
                    }
                }
            }
        }
        return newsFeedList;
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

    protected void drawNearbyEntourage(FeedItem feedItem) {
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
                        MapClusterItem mapClusterItem = new MapClusterItem(feedItem);
                        markersMap.put(feedItem.hashString(), mapClusterItem);
                        mapClusterManager.addItem(mapClusterItem);
                    }
                }
            }
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

    void clearAll() {
        if (map != null) {
            map.clear();
        }

        if (mapClusterManager != null) mapClusterManager.clearItems();
        markersMap.clear();
        if (presenter != null) {
            presenter.getOnClickListener().clear();
            presenter.getOnGroundOverlayClickListener().clear();
        }

        resetFeed();
    }

    private void resetFeed() {
        if(newsfeedAdapter!=null) {
            newsfeedAdapter.removeAll();
        }

        // check if we need to cancel the current request
        if (pagination.isLoading && tourService != null) {
            tourService.cancelNewsFeedUpdate();
        }

        pagination.reset();
    }

    void displayFullMap() {

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
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            newsfeedAdapter.setMapHeight(val);
            newsfeedListView.getLayoutManager().requestLayout();
        });
        anim.start();
    }

    protected void displayListWithMapHeader() {
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
        anim.addUpdateListener(valueAnimator -> {
            int val = (Integer) valueAnimator.getAnimatedValue();
            newsfeedAdapter.setMapHeight(val);
            newsfeedListView.getLayoutManager().requestLayout();
        });
        anim.start();
    }

    public void toggleToursList() {
        if (!isFullMapShown) {
            displayFullMap();
            EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_06_2);
        } else {
            displayListWithMapHeader();
            EntourageEvents.logEvent(EntourageEvents.EVENT_SCREEN_06_1);
        }
    }

    public boolean isToursListVisible() {
        return !isFullMapShown;
    }

    public void ensureMapVisible() {
        newsfeedListView.scrollToPosition(0);
    }

    private void updatePagination(List<? extends Newsfeed> newsfeedList) {
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
                    if ((newsfeedData instanceof FeedItem)) {
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
        TimerTask refreshToursTimerTask = new TimerTask() {
            @Override
            public void run() {
                refreshToursHandler.post(() -> {
                    if (tourService != null) {
                        if (selectedTab == MapTabItem.ALL_TAB) {
                            pagination.isRefreshing = true;
                            tourService.updateNewsfeed(pagination, selectedTab);
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
        h.postDelayed(() -> {
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
            ((DrawerActivity) getActivity()).showTutorial();
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
        storeActionZoneInfo(false);
    }

    @Override
    public void onUserEditActionZoneFragmentIgnore() {
        storeActionZoneInfo(true);
        checkPermission();
    }

    private void storeActionZoneInfo(final boolean ignoreAddress) {
        AuthenticationController authenticationController = EntourageApplication.get().getEntourageComponent().getAuthenticationController();
        authenticationController.getUserPreferences().setIgnoringActionZone(ignoreAddress);
        authenticationController.saveUserPreferences();
        User me = EntourageApplication.me();
        if (me != null && !ignoreAddress) {
            User.Address address = me.getAddress();
            if (address != null) {
                centerMap(new LatLng(address.getLatitude(), address.getLongitude()));
            }
        }
    }

    public void addEncounter() {
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

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
                        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_SCROLL_LIST);
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(@NonNull final RecyclerView recyclerView, final int newState) {
        }
    }
}
