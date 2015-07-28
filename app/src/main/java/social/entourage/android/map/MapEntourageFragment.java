package social.entourage.android.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.BackPressable;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.Constants;
import social.entourage.android.map.encounter.CreateEncounterActivity;
import social.entourage.android.map.tour.TourService;

public class MapEntourageFragment extends Fragment implements BackPressable, TourService.TourServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final int FOLLOWING_LIMIT = 5;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private SupportMapFragment mapFragment;

    private boolean isBetterLocationUpdated;

    private LatLng previousCoordinates;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound = true;
    private boolean isFollowing = true;
    private int color;

    @InjectView(R.id.fragment_map_pin)
    View mapPin;

    @InjectView(R.id.fragment_map_follow_button)
    View centerButton;

    @InjectView(R.id.button_start_tour_launcher)
    Button buttonStartLauncher;

    @InjectView(R.id.layout_map_launcher)
    View mapLauncherLayout;

    @InjectView(R.id.launcher_tour_transport_mode)
    RadioGroup radioGroupTransportMode;

    @InjectView(R.id.launcher_tour_type)
    RadioGroup radioGroupType;

    @InjectView(R.id.layout_map_tour)
    View layoutMapTour;

    @InjectView(R.id.layout_map_confirmation)
    View layoutMapConfirmation;

    @InjectView(R.id.confirmation_encounters)
    TextView encountersView;

    @InjectView(R.id.confirmation_distance)
    TextView distanceView;

    @InjectView(R.id.confirmation_duration)
    TextView durationView;

    @InjectView(R.id.confirmation_resume_button)
    Button resumeButton;

    @InjectView(R.id.confirmation_end_button)
    Button endButton;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.inject(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().setMyLocationEnabled(true);
            mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
        }
        mapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Location newLocation = cameraPositionToLocation("new", EntourageLocation.getInstance().getLastCameraPosition());
                Location prevLocation = cameraPositionToLocation("previous", cameraPosition);
                if (newLocation.distanceTo(prevLocation) >= FOLLOWING_LIMIT) {
                    isFollowing = false;
                }
            }
        });
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
                Encounter encounter = (Encounter) data.getExtras().getSerializable(Constants.KEY_ENCOUNTER);
                addEncounter(encounter);

                LatLng latLng = new LatLng(data.getExtras().getDouble(Constants.KEY_LATITUDE), data.getExtras().getDouble(Constants.KEY_LONGITUDE));
                /**
                 * HERE : update the request to get all the encounters
                 *        related to the current tour
                 */
                presenter.retrieveMapObjects(latLng);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeLocationService();
        doBindService();
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.start();
    }

    @Override
    public boolean onBackPressed() {
        if (mapLauncherLayout.getVisibility() == View.VISIBLE) {
            mapLauncherLayout.setVisibility(View.GONE);
            buttonStartLauncher.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tourService != null) {
            tourService.unregister(MapEntourageFragment.this);
            doUnbindService();
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void onNotificationAction(String action) {
        if (TourService.NOTIFICATION_PAUSE.equals(action)) {
            showConfirmation();
        }
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
        BitmapDescriptor encounterIcon = BitmapDescriptorFactory.fromResource(R.drawable.encounter);

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

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    void doBindService() {
        Intent intent = new Intent(getActivity(), TourService.class);
        getActivity().startService(intent);
        getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    void doUnbindService() {
        if (isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------

    @Override
    public void onTourUpdated(Tour tour) {
        if (!tour.getCoordinates().isEmpty()) {
            drawLocation(tour.getCoordinates().get(tour.getCoordinates().size() - 1));
        }
    }

    @Override
    public void onTourResumed(Tour tour) {
        drawResumedTour(tour);
    }

    @Override
    public void onLocationUpdated(LatLng location) {
        centerMap(location);
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------

    @OnClick(R.id.fragment_map_follow_button)
    void onFollowGeolocation() {
        isFollowing = true;
    }

    @OnClick(R.id.button_start_tour_launcher)
    void onStartTourLauncher() {
        if (!tourService.isRunning()) {
            buttonStartLauncher.setVisibility(View.GONE);
            mapLauncherLayout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.launcher_tour_go)
    void onStartNewTour() {
        TourTransportMode tourTransportMode = TourTransportMode.findByRessourceId(radioGroupTransportMode.getCheckedRadioButtonId());
        TourType tourType = TourType.findByRessourceId(radioGroupType.getCheckedRadioButtonId());
        mapLauncherLayout.setVisibility(View.GONE);
        layoutMapTour.setVisibility(View.VISIBLE);
        startTour(tourTransportMode.getName(), tourType.getName());
    }


    @OnClick(R.id.tour_stop_button)
    public void onStopTour() {
        pauseTour();
        showConfirmation();
    }

    @OnClick(R.id.tour_add_encounter_button)
    public void onAddEncounter() {
        Intent intent = new Intent(getActivity(), CreateEncounterActivity.class);
        saveCameraPosition();
        Bundle args = new Bundle();
        args.putLong(Constants.KEY_TOUR_ID, getTourId());
        args.putDouble(Constants.KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
        args.putDouble(Constants.KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);
        intent.putExtras(args);
        startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
    }

    @OnClick(R.id.confirmation_resume_button)
    public void onResumeTour() {
        layoutMapConfirmation.setVisibility(View.GONE);
        layoutMapTour.setVisibility(View.VISIBLE);
        resumeTour();
    }

    @OnClick(R.id.confirmation_end_button)
    public void onEndTour() {
        layoutMapConfirmation.setVisibility(View.GONE);
        stopTour();
    }


    // ----------------------------------
    // PRIVATE METHODS (lifecycle)
    // ----------------------------------

    private Location cameraPositionToLocation(String provider, CameraPosition cameraPosition) {
        Location location = new Location(provider);
        location.setLatitude(cameraPosition.target.latitude);
        location.setLongitude(cameraPosition.target.longitude);
        return location;
    }

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.UPDATE_TIMER_MILLIS, Constants.DISTANCE_BETWEEN_UPDATES_METERS, new CustomLocationListener());
    }

    private void showConfirmation() {
        layoutMapTour.setVisibility(View.GONE);
        Tour currentTour = getCurrentTour();
        encountersView.setText(getString(R.string.tour_end_encounters, currentTour.getEncounters().size()));
        distanceView.setText(getString(R.string.tour_end_distance, currentTour.getDistance()));
        distanceView.setText(getString(R.string.tour_end_distance, String.format("%.1f", currentTour.getDistance() / 1000)));
        durationView.setText(getString(R.string.tour_end_duration, currentTour.getDuration()));
        layoutMapConfirmation.setVisibility(View.VISIBLE);
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

    private void startTour(String transportMode, String type) {
        changeTrackColor(type);
        if (tourService != null) {
            if (!tourService.isRunning()) {
                mapPin.setVisibility(View.VISIBLE);
                tourService.beginTreatment(transportMode, type);
            }
        }
    }

    private void pauseTour() {
        if (tourService != null) {
            if (tourService.isRunning()) tourService.pauseTreatment();
        }
    }

    private void resumeTour() {
        if (tourService.isRunning()) {
            tourService.resumeTreatment();
            buttonStartLauncher.setVisibility(View.GONE);
            mapPin.setVisibility(View.VISIBLE);
        }
    }

    private void stopTour() {
        if (tourService.isRunning()) {
            tourService.endTreatment();
            previousCoordinates = null;
            clearMap();
            mapPin.setVisibility(View.GONE);
            buttonStartLauncher.setVisibility(View.VISIBLE);
        }
    }

    private Tour getCurrentTour() {
        return tourService.getCurrentTour();
    }

    private long getTourId() {
        return tourService.getTourId();
    }

    private void addEncounter(Encounter encounter) {
        tourService.addEncounter(encounter);
    }

    // ----------------------------------
    // PRIVATE METHODS (views)
    // ----------------------------------

    private void changeTrackColor(String type) {
        if (TourType.SOCIAL.getName().equals(type)) {
            color = Color.GREEN;
        } else if (TourType.FOOD.getName().equals(type)) {
            color = Color.BLUE;
        } else if (TourType.OTHER.getName().equals(type)) {
            color = Color.RED;
        } else {
            color = Color.GRAY;
        }
    }

    private void clearMap() {
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().clear();
        }
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
            EntourageLocation.getInstance().saveCameraPosition(mapFragment.getMap().getCameraPosition());
        }
    }

    private void drawLocation(LatLng location) {
        if (previousCoordinates != null) {
            PolylineOptions line = new PolylineOptions();
            line.add(previousCoordinates, location);
            line.width(15).color(color);
            mapFragment.getMap().addPolyline(line);
        }
        previousCoordinates = location;
    }

    private void drawResumedTour(Tour tour) {
        changeTrackColor(tour.getTourType());
        if (!tour.getCoordinates().isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            line.width(15).color(color);
            for (LatLng location : tour.getCoordinates()) {
                line.add(location);
            }
            mapFragment.getMap().addPolyline(line);
            previousCoordinates = tour.getCoordinates().get(tour.getCoordinates().size() - 1);
        }

    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            Location bestLocation = EntourageLocation.getInstance().getLocation();
            boolean shouldCenterMap = false;
            if (bestLocation == null || (location.getAccuracy() > 0.0 && bestLocation.getAccuracy() == 0.0)) {
                EntourageLocation.getInstance().saveLocation(location);
                isBetterLocationUpdated = true;
                shouldCenterMap = true;
            }

            if (isBetterLocationUpdated) {
                isBetterLocationUpdated = false;
                LatLng latLng = EntourageLocation.getInstance().getLatLng();
                presenter.retrieveMapObjects(latLng);
                if (shouldCenterMap) {
                    centerMap(latLng);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    private class ServiceConnection implements android.content.ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            tourService = ((TourService.LocalBinder)service).getService();
            tourService.register(MapEntourageFragment.this);

            boolean isRunning = tourService != null && tourService.isRunning();
            if (isRunning) {
                layoutMapTour.setVisibility(View.VISIBLE);
                buttonStartLauncher.setVisibility(View.GONE);
                mapPin.setVisibility(View.VISIBLE);
            }

            Intent intent = getActivity().getIntent();
            if (intent.getBooleanExtra(TourService.NOTIFICATION_PAUSE, false)) {
                onNotificationAction(TourService.NOTIFICATION_PAUSE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(MapEntourageFragment.this);
            tourService = null;
        }
    }
}
