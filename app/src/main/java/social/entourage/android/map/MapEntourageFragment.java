package social.entourage.android.map;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
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
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnCheckIntentActionEvent;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.api.tape.Events.OnUserChoiceEvent;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationActivity;
import social.entourage.android.map.encounter.CreateEncounterActivity;
import social.entourage.android.map.permissions.NoLocationPermissionFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.map.tour.join.TourJoinRequestFragment;
import social.entourage.android.map.tour.ToursAdapter;
import social.entourage.android.tools.BusProvider;

public class MapEntourageFragment extends Fragment implements BackPressable, TourService.TourServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final float ZOOM_REDRAW_LIMIT = 1.1f;
    private static final int REDRAW_LIMIT = 300;
    private static final int PERMISSIONS_REQUEST_LOCATION = 1;
    private static final int MAX_TOUR_HEADS_DISPLAYED = 10;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private int userId;
    private boolean userHistory;

    private View toReturn;

    private SupportMapFragment mapFragment;

    private LatLng previousCoordinates;
    private Location previousCameraLocation;
    private LatLng longTapCoordinates;
    private float previousCameraZoom = 1.0f;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private ProgressDialog loaderStop;
    private ProgressDialog loaderSearchTours;
    private boolean isBound;
    private boolean isMapLoaded;
    private boolean isFollowing = true;

    private long currentTourId;
    private int color;
    private int displayedTourHeads = 0;

    private List<Polyline> currentTourLines;
    private Map<Long, Polyline> drawnToursMap;
    private Map<Long, Polyline> drawnUserHistory;
    private Map<Long, Marker> markersMap;
    private Map<Long, Tour> retrievedTours;
    private Map<Long, Tour> retrievedHistory;

    private LayoutInflater inflater;

    private float originalMapLayoutWeight;

    private boolean isRequestingToJoin = false;

    @Bind(R.id.fragment_map_pin)
    View mapPin;

    @Bind(R.id.fragment_map_gps_layout)
    LinearLayout gpsLayout;

    @Bind(R.id.fragment_map_follow_button)
    View centerButton;

    @Bind(R.id.layout_map_launcher)
    View mapLauncherLayout;

    @Bind(R.id.launcher_tour_go)
    ImageView buttonLaunchTour;

    @Bind(R.id.launcher_tour_transport_mode)
    RadioGroup radioGroupTransportMode;

    @Bind(R.id.launcher_tour_type)
    RadioGroup radioGroupType;

    @Bind(R.id.fragment_map_tours_view)
    RecyclerView toursListView;

    ToursAdapter toursAdapter;

    @Bind(R.id.layout_map)
    FrameLayout layoutMapMain;

    @Bind(R.id.fragment_map_main_layout)
    LinearLayout layoutMain;

    @Bind(R.id.map_display_type)
    RadioGroup mapDisplayTypeRadioGroup;

    @Bind(R.id.layout_map_longclick)
    RelativeLayout mapLongClickView;

    @Bind(R.id.map_longclick_buttons)
    LinearLayout mapLongClickButtonsView;

    @Bind(R.id.tour_stop_button)
    FloatingActionButton tourStopButton;

    FloatingActionMenu mapOptionsMenu;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isBound) {
            doBindService();
        }

        currentTourLines = new ArrayList<>();
        drawnToursMap = new TreeMap<>();
        drawnUserHistory = new TreeMap<>();
        markersMap = new TreeMap<>();
        retrievedTours = new TreeMap<>();
        retrievedHistory = new TreeMap<>();

        checkPermission();
        FlurryAgent.logEvent(Constants.EVENT_OPEN_TOURS_FROM_MENU);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_map, container, false);
        }
        ButterKnife.bind(this, toReturn);
        this.inflater = inflater;
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        presenter.start();
        initializeMap();
        initializeFloatingMenu();
        initializeToursListView();
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
                    checkPermission();
                } else {
                    BusProvider.getInstance().post(new OnLocationPermissionGranted(true));
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onStart() {
        super.onStart();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.activity_tours_title);
            if (isMapLoaded) {
                BusProvider.getInstance().post(new OnCheckIntentActionEvent());
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBound && tourService != null) {
            tourService.unregister(MapEntourageFragment.this);
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
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().setOnMarkerClickListener(onMarkerClickListener);
        }
    }

    public void putEncounterOnMap(Encounter encounter,
                                  MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        double encounterLatitude = encounter.getLatitude();
        double encounterLongitude = encounter.getLongitude();
        LatLng encounterPosition = new LatLng(encounterLatitude, encounterLongitude);
        BitmapDescriptor encounterIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_encounter);

        MarkerOptions markerOptions = new MarkerOptions().position(encounterPosition)
                .icon(encounterIcon);

        if (mapFragment.getMap() != null) {
            mapFragment.getMap().addMarker(markerOptions);
            onClickListener.addEncounterMarker(encounterPosition, encounter);
        }
    }

    public void initializeMapZoom() {
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    public void displayChosenTour(Tour tour) {
        if (presenter != null) {
            presenter.openTour(tour);
        }
    }

    public void displayChosenUser(int userID) {
        Toast.makeText(getContext(), "Show user profile for id="+userID, Toast.LENGTH_SHORT).show();
    }

    public void act(Tour tour) {
        if (tourService != null) {
            isRequestingToJoin = true;
            tourService.requestToJoinTour(tour);
        }
        else {
            Toast.makeText(getContext(), R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
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
                stopTour(actionTour);
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
    public void onBetterLocation(OnBetterLocationEvent event) {
        if (event.getLocation() != null) {
            centerMap(event.getLocation());
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    void doBindService() {
        if (getActivity() != null) {
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
        if (getActivity() != null) {
            if (created) {
                isFollowing = true;
                currentTourId = tourId;
                presenter.incrementUserToursCount();
                mapLauncherLayout.setVisibility(View.GONE);
                if (toursListView.getVisibility() == View.VISIBLE) {
                    hideToursList();
                }
                addTourCell(tourService.getCurrentTour());
                //mapPin.setVisibility(View.VISIBLE);
                mapOptionsMenu.setVisibility(View.VISIBLE);
                updateFloatingMenuOptions();
                tourStopButton.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getActivity(), R.string.tour_creation_fail, Toast.LENGTH_SHORT).show();
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
        tourStopButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLocationUpdated(LatLng location) {
        centerMap(location);
    }

    @Override
    public void onRetrieveToursNearby(List<Tour> tours) {
        tours = removeRedundantTours(tours, false);
        Collections.sort(tours, new Tour.TourComparatorOldToNew());
        for (Tour tour : tours) {
            if (currentTourId != tour.getId()) {
                drawNearbyTour(tour, false);
            }
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
                    presenter.openTour(toursTree.firstEntry().getValue());
                }
            }
        }
    }

    @Override
    public void onTourClosed(boolean closed, Tour tour) {
        if (getActivity() != null) {
            if (closed) {
                mapFragment.getMap().clear();

                currentTourLines.clear();
                drawnToursMap.clear();
                drawnUserHistory.clear();
                retrievedTours.clear();

                displayedTourHeads = 0;

                toursAdapter.removeAllTours();

                previousCoordinates = null;

                //mapPin.setVisibility(View.GONE);
                if (tour.getId() == currentTourId) {
                    mapOptionsMenu.setVisibility(View.VISIBLE);
                    updateFloatingMenuOptions();
                    tourStopButton.setVisibility(View.GONE);

                    currentTourId = -1;
                }
                else {
                    tourService.notifyListenersTourResumed();
                }

                tourService.updateNearbyTours();
                if (userHistory) {
                    tourService.updateUserHistory(userId, 1, 1);
                }

                @StringRes int tourStatusStringId =  R.string.local_service_stopped;
                if (tour.getTourStatus().equals(Tour.TOUR_FREEZED)) {
                    tourStatusStringId = R.string.tour_freezed;
                }

                Toast.makeText(getActivity(), tourStatusStringId, Toast.LENGTH_SHORT).show();

            } else {
                @StringRes int tourClosedFailedId = R.string.tour_close_fail;
                if (tour.getTourStatus().equals(Tour.TOUR_FREEZED)) {
                    tourClosedFailedId = R.string.tour_freezed;
                }
                Toast.makeText(getActivity(), tourClosedFailedId, Toast.LENGTH_SHORT).show();
            }
            if (loaderStop != null) {
                loaderStop.dismiss();
                loaderStop = null;
            }
        }
    }

    @Override
    public void onGpsStatusChanged(boolean active) {
        if (gpsLayout != null) {
            if (active) {
                gpsLayout.setVisibility(View.GONE);
            } else {
                gpsLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onUserStatusChanged(final TourUser user, final Tour tour) {
        if (user == null) {
            //error changing the status
            if (isRequestingToJoin) {
                Toast.makeText(getContext(), R.string.tour_join_request_error, Toast.LENGTH_SHORT).show();
            }
        }
        else {
            tour.setJoinStatus(user.getStatus());
            updateTourCellJoinStatus(tour);

            if (user.getStatus().equals(Tour.JOIN_STATUS_PENDING)) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                TourJoinRequestFragment tourJoinRequestFragment = TourJoinRequestFragment.newInstance(tour);
                tourJoinRequestFragment.show(fragmentManager, TourJoinRequestFragment.TAG);
            }
        }
        isRequestingToJoin = false;
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
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            centerMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
        isFollowing = true;
    }

    @OnClick(R.id.launcher_tour_go)
    void onStartNewTour() {
        buttonLaunchTour.setEnabled(false);
        TourTransportMode tourTransportMode = TourTransportMode.findByRessourceId(radioGroupTransportMode.getCheckedRadioButtonId());
        TourType tourType = TourType.findByRessourceId(radioGroupType.getCheckedRadioButtonId());
        startTour(tourTransportMode.getName(), tourType.getName());
        FlurryAgent.logEvent(Constants.EVENT_START_TOUR);
    }

    @OnClick(R.id.tour_stop_button)
    public void onStartStopConfirmation() {
        pauseTour();
        if (getActivity() != null) {
            launchConfirmationActivity();
        }
    }

    @OnClick(R.id.map_longclick_button_create_encounter)
    public void onAddEncounter() {
        if (getActivity() != null) {
            mapLongClickView.setVisibility(View.GONE);
            if (mapOptionsMenu.isOpened()) {
                mapOptionsMenu.toggle(false);
            }
            Intent intent = new Intent(getActivity(), CreateEncounterActivity.class);
            saveCameraPosition();
            Bundle args = new Bundle();
            args.putLong(CreateEncounterActivity.BUNDLE_KEY_TOUR_ID, currentTourId);
            if (longTapCoordinates != null) {
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, longTapCoordinates.latitude);
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, longTapCoordinates.longitude);
                longTapCoordinates = null;
            }
            else {
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
                args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);
            }
            intent.putExtras(args);
            startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
        }
    }

    @OnClick(R.id.map_display_type_list)
    public void onDisplayTypeList() {
        showToursList();
    }

    // ----------------------------------
    // Map Options handler
    // ----------------------------------

    private void initializeFloatingMenu() {
        mapOptionsMenu = ((DrawerActivity)getActivity()).mapOptionsMenu;
        mapOptionsMenu.setClosedOnTouchOutside(true);
    }

    private void updateFloatingMenuOptions() {
        if (tourService.isRunning()) {
            mapOptionsMenu.findViewById(R.id.button_add_tour_encounter).setVisibility(View.VISIBLE);
            mapOptionsMenu.findViewById(R.id.button_start_tour_launcher).setVisibility(View.GONE);
        }
        else {
            mapOptionsMenu.findViewById(R.id.button_add_tour_encounter).setVisibility(View.GONE);
            mapOptionsMenu.findViewById(R.id.button_start_tour_launcher).setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.map_longclick_button_start_tour_launcher)
    public void onStartTourLauncher() {
        if (!tourService.isRunning()) {
            FlurryAgent.logEvent(Constants.EVENT_OPEN_TOUR_LAUNCHER_FROM_MAP);
            if (mapOptionsMenu.isOpened()) {
                mapOptionsMenu.toggle(false);
            }
            mapOptionsMenu.setVisibility(View.GONE);
            mapLongClickView.setVisibility(View.GONE);
            mapLauncherLayout.setVisibility(View.VISIBLE);
        }
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------

    private void showLongClickOnMapOptions(LatLng latLng) {
        //only show when map is in full screen and not visible
        if (toursListView.getVisibility() == View.VISIBLE || mapLongClickView.getVisibility() == View.VISIBLE) {
            return;
        }
        //if ongoing tour, show only if the point is in the current tour
        if (tourService != null && tourService.isRunning()) {
            if (!tourService.isLocationInTour(latLng)) return;
            longTapCoordinates = latLng;
        }
        //hide the FAB menu
        mapOptionsMenu.setVisibility(View.GONE);
        tourStopButton.setVisibility(View.GONE);
        //get the click point
        Point clickPoint = mapFragment.getMap().getProjection().toScreenLocation(latLng);
        //adjust the buttons holder layout
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        mapLongClickButtonsView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int bW = mapLongClickButtonsView.getMeasuredWidth();
        int bH = mapLongClickButtonsView.getMeasuredHeight();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mapLongClickButtonsView.getLayoutParams();
        int marginLeft = clickPoint.x;
        if (marginLeft + bW > screenSize.x) {
            marginLeft -= bW;
        }
        int marginTop = clickPoint.y - bH;
        if (marginTop < 0) marginTop = clickPoint.y;
        lp.setMargins(marginLeft, marginTop, 0, 0);
        mapLongClickButtonsView.setLayoutParams(lp);
        //update the visible buttons
        boolean isTourRunning = tourService != null && tourService.isRunning();
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_start_tour_launcher).setVisibility(isTourRunning?View.GONE:View.VISIBLE);
        mapLongClickButtonsView.findViewById(R.id.map_longclick_button_create_encounter).setVisibility(isTourRunning?View.VISIBLE:View.GONE);
        //show the view
        mapLongClickView.setVisibility(View.VISIBLE);
    }

    // ----------------------------------
    // PRIVATE METHODS (lifecycle)
    // ----------------------------------

    private void checkPermission() {
        if (getActivity() != null) {
            if (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.map_permission_title)
                            .setMessage(R.string.map_permission_description)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    requestPermissions(new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, PERMISSIONS_REQUEST_LOCATION);
                                }
                            })
                            .setNegativeButton(R.string.map_permission_refuse, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, final int i) {
                                    NoLocationPermissionFragment noLocationPermissionFragment = new NoLocationPermissionFragment();
                                    noLocationPermissionFragment.show(getActivity().getSupportFragmentManager(), "fragment_no_location_permission");
                                }
                            })
                            .show();
                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                }
                return;
            }
        }
    }

    private void initializeMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    googleMap.setMyLocationEnabled(true);
                }
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                initializeMapZoom();
                setOnMarkerClickListener(presenter.getOnClickListener());

                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(CameraPosition cameraPosition) {
                        EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition);
                        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                        Location newLocation = EntourageLocation.cameraPositionToLocation(null, cameraPosition);
                        float newZoom = cameraPosition.zoom;

                        if (tourService != null && (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT)) {
                            previousCameraZoom = newZoom;
                            previousCameraLocation = newLocation;
                            tourService.updateNearbyTours();
                            if (userHistory) {
                                tourService.updateUserHistory(userId, 1, 500);
                            }
                        }

                        if (isFollowing && currentLocation != null) {
                            if (currentLocation.distanceTo(newLocation) > 1) {
                                isFollowing = false;
                            }
                        }
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        if (getActivity() != null) {
                            if (toursListView.getVisibility() == View.VISIBLE) {
                                hideToursList();
                            } else {
                                loaderSearchTours = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_search), getActivity().getString(R.string.button_loading), true);
                                loaderSearchTours.setCancelable(true);
                                tourService.searchToursFromPoint(latLng, userHistory, userId, 1, 500);
                            }
                        }
                    }
                });

                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(final LatLng latLng) {
                        if (getActivity() != null) {
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
        });
    }

    private void initializeToursListView() {
        if (toursAdapter == null) {
            toursListView.setLayoutManager(new LinearLayoutManager(getContext()));
            toursAdapter = new ToursAdapter();
            toursListView.setAdapter(toursAdapter);
        }
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

    private Tour getCurrentTour() {
        return tourService != null ? tourService.getCurrentTour() : null;
    }

    private void startTour(String transportMode, String type) {
        if (tourService != null && !tourService.isRunning()) {
            color = getTrackColor(false, type, new Date());
            tourService.beginTreatment(transportMode, type);
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

    private void resumeTour() {
        if (tourService.isRunning()) {
            tourService.resumeTreatment();
            //buttonStartLauncher.setVisibility(View.GONE);
            //mapPin.setVisibility(View.VISIBLE);
        }
    }

    private void resumeTour(Tour tour) {
        if (tourService != null) {
            if (tour != null && tourService.getCurrentTourId() == tour.getId() && tourService.isRunning()) {
                tourService.resumeTreatment();
            }
        }
    }

    private void stopTour(Tour tour) {
        if (getActivity() != null) {
            if (tourService != null) {
                if (tour != null && tourService.getCurrentTourId() != tour.getId()) {
                    loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                    loaderStop.setCancelable(true);
                    FlurryAgent.logEvent(Constants.EVENT_STOP_TOUR);
                    tourService.stopOtherTour(tour);
                    return;
                }
                else {
                    if (tourService.isRunning()) {
                        loaderStop = ProgressDialog.show(getActivity(), getActivity().getString(R.string.loader_title_tour_finish), getActivity().getString(R.string.button_loading), true);
                        loaderStop.setCancelable(true);
                        tourService.endTreatment();
                        FlurryAgent.logEvent(Constants.EVENT_STOP_TOUR);
                    }
                }
            }
        }
    }

    public void userStatusChanged(long tourId, int userId, String status) {
        if (tourService != null) {
            Tour tour = retrievedTours.get(tourId);
            if (tour != null) {
                TourUser user = new TourUser();
                user.setUserId(userId);
                user.setStatus(status);
                tourService.notifyListenersUserStatusChanged(user, tour);
            }
        }
    }

    public void removeUserFromTour(Tour tour, int userId) {
        if (tourService != null) {
            tourService.removeUserFromTour(tour, userId);
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

    // ----------------------------------
    // PRIVATE METHODS (views)
    // ----------------------------------

    private List<Tour> removeRedundantTours(List<Tour> tours, boolean isHistory) {
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (!isHistory) {
                if (drawnToursMap.containsKey(tour.getId())) {
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

    private List<Tour> removeRecentTours(List<Tour> tours) {
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (retrievedTours.containsValue(tour)) {
                iteratorTours.remove();
            }
        }
        return tours;
    }

    private int getTrackColor(boolean isHistory, String type, Date date) {
        int color = Color.GRAY;
        if (TourType.MEDICAL.getName().equals(type)) {
            color = Color.RED;
        }
        else if (TourType.ALIMENTARY.getName().equals(type)) {
            color = Color.GREEN;
        }
        else if (TourType.BARE_HANDS.getName().equals(type)) {
            color = Color.BLUE;
        }
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
        return Color.argb(100, Color.red(color), Color.green(color), Color.blue(color));
    }

    private void hideUserHistory() {
        Iterator iteratorTours = retrievedHistory.entrySet().iterator();
        while (iteratorTours.hasNext()) {
            Map.Entry pair = (Map.Entry) iteratorTours.next();
            Tour tour = (Tour) pair.getValue();
            Polyline line = drawnUserHistory.get(tour.getId());
            line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
        }
    }

    private void showUserHistory() {
        Iterator iteratorLines = drawnUserHistory.entrySet().iterator();
        while (iteratorLines.hasNext()) {
            Map.Entry pair = (Map.Entry) iteratorLines.next();
            Tour tour = retrievedHistory.get(pair.getKey());
            Polyline line = (Polyline) pair.getValue();
            line.setColor(getTrackColor(true, tour.getTourType(), tour.getStartTime()));
        }
    }

    public static boolean isToday(Date date) {
        if (date == null) return false;
        Date today = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(date);
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
    }

    private void centerMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, EntourageLocation.getInstance().getLastCameraPosition().zoom, 0, 0);
        centerMap(cameraPosition);
    }

    private void centerMap(CameraPosition cameraPosition) {
        if(mapFragment!= null && mapFragment.getMap() != null && isFollowing) {
            mapFragment.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    public void saveCameraPosition() {
        if(mapFragment!= null && mapFragment.getMap() != null) {
            EntourageLocation.getInstance().saveLastCameraPosition(mapFragment.getMap().getCameraPosition());
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
        if (previousCoordinates != null) {
            PolylineOptions line = new PolylineOptions();
            line.add(previousCoordinates, location);
            line.zIndex(2f);
            line.width(15);
            line.color(color);
            currentTourLines.add(mapFragment.getMap().addPolyline(line));
        }
        previousCoordinates = location;
    }

    private void drawCurrentTour(List<TourPoint> pointsToDraw, String tourType, Date startDate) {
        if (!pointsToDraw.isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            color = getTrackColor(true, tourType, startDate);
            line.zIndex(2f);
            line.width(15);
            line.color(color);
            for (TourPoint tourPoint : pointsToDraw) {
                line.add(tourPoint.getLocation());
            }
            currentTourLines.add(mapFragment.getMap().addPolyline(line));
        }
    }

    private void drawNearbyTour(Tour tour, boolean isHistory) {
        if (mapFragment != null && mapFragment.getMap() != null && drawnToursMap != null && drawnUserHistory != null && tour != null && !tour.getTourPoints().isEmpty()) {
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
            DrawerActivity activity = null;
            if (getActivity() instanceof DrawerActivity) {
                activity = (DrawerActivity) getActivity();
                tour.setBadgeCount(activity.getPushNotificationsCountForTour(tour.getId()));
            }
            if (isHistory) {
                retrievedHistory.put(tour.getId(), tour);
                drawnUserHistory.put(tour.getId(), mapFragment.getMap().addPolyline(line));
            } else {
                retrievedTours.put(tour.getId(), tour);
                drawnToursMap.put(tour.getId(), mapFragment.getMap().addPolyline(line));
                addTourCell(tour);
            }
            if (tour.getTourStatus() == null) {
                tour.setTourStatus(Tour.TOUR_CLOSED);
            }
            if (Tour.TOUR_ON_GOING.equalsIgnoreCase(tour.getTourStatus())) {
                addTourHead(tour);
            }
        }
    }

    private void addTourCell(Tour tour) {
        toursAdapter.add(tour);
    }

    private void updateTourCellJoinStatus(Tour tour) {
        toursAdapter.updateTour(tour);
    }

    private void addTourHead(Tour tour) {
        if (displayedTourHeads >= MAX_TOUR_HEADS_DISPLAYED) {
            return;
        }
        displayedTourHeads++;
        TourPoint lastPoint = tour.getTourPoints().get(tour.getTourPoints().size() - 1);
        double latitude = lastPoint.getLatitude();
        double longitude = lastPoint.getLongitude();
        LatLng position = new LatLng(latitude, longitude);

        BitmapDescriptor icon = null;
        /*
        if (tour.getTourVehicleType().equals(TourTransportMode.FEET.getName())) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_feet_active);
        }
        else if (tour.getTourVehicleType().equals(TourTransportMode.CAR.getName())) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_car_active);
        }
        */
        IconGenerator iconGenerator = new IconGenerator(getContext());
        iconGenerator.setTextAppearance(R.style.OngoingTourMarker);
        icon = BitmapDescriptorFactory.fromBitmap(iconGenerator.makeIcon(tour.getOrganizationName()));

        MarkerOptions markerOptions = new MarkerOptions()
                .position(position)
                .icon(icon)
                .anchor(0.5f, 1.0f);

        if (mapFragment.getMap() != null) {
            markersMap.put(tour.getId(), mapFragment.getMap().addMarker(markerOptions));
            presenter.getOnClickListener().addTourMarker(position, tour);
        }
    }

    private void hideToursList() {
        toursListView.setVisibility(View.GONE);

        mapDisplayTypeRadioGroup.check(R.id.map_display_type_carte);
        mapDisplayTypeRadioGroup.setVisibility(View.VISIBLE);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layoutMapMain.getLayoutParams();
        originalMapLayoutWeight = lp.weight;
        lp.weight = layoutMain.getWeightSum();
        layoutMapMain.setLayoutParams(lp);
        layoutMain.forceLayout();
    }

    private void showToursList() {
        toursListView.setVisibility(View.VISIBLE);

        mapDisplayTypeRadioGroup.setVisibility(View.GONE);

        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layoutMapMain.getLayoutParams();
        lp.weight = originalMapLayoutWeight;
        layoutMapMain.setLayoutParams(lp);
    }

    // ----------------------------------
    // Push handling
    // ----------------------------------

    public void onPushNotificationReceived(Message message) {
        //update the badge count on tour card
        PushNotificationContent content = message.getContent();
        if (content == null) return;
        long tourId = message.getContent().getTourId();
        Tour tour = retrievedTours.get(tourId);
        if (tour == null) return;
        tour.increaseBadgeCount();
        toursAdapter.updateTour(tour);
    }

    public void onPushNotificationConsumedForTour(long tourId) {
        Tour tour = retrievedTours.get(tourId);
        if (tour == null) return;
        tour.setBadgeCount(0);
        toursAdapter.updateTour(tour);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (getActivity() != null) {
                tourService = ((TourService.LocalBinder) service).getService();
                tourService.register(MapEntourageFragment.this);

                boolean isRunning = tourService != null && tourService.isRunning();
                if (isRunning) {
                    updateFloatingMenuOptions();

                    currentTourId = tourService.getCurrentTourId();
                    //mapPin.setVisibility(View.VISIBLE);

                    addCurrentTourEncounters();
                }

                tourService.updateNearbyTours();
                if (userHistory) {
                    tourService.updateUserHistory(userId, 1, 500);
                }
            }
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(MapEntourageFragment.this);
            tourService = null;
        }
    }
}
