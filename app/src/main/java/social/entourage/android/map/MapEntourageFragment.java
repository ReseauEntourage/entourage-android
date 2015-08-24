package social.entourage.android.map;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.TourTransportMode;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.map.choice.ChoiceFragment;
import social.entourage.android.map.confirmation.ConfirmationActivity;
import social.entourage.android.map.encounter.CreateEncounterActivity;
import social.entourage.android.map.tour.TourService;

public class MapEntourageFragment extends Fragment implements BackPressable, TourService.TourServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final int REDRAW_LIMIT = 300;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private SupportMapFragment mapFragment;

    private boolean isBetterLocationUpdated;

    private LatLng previousCoordinates;
    private Location previousCameraLocation;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection();
    private boolean isBound = true;
    private boolean isFollowing = true;

    private int color;

    private int selected;

    private long currentTourId;
    private List<Polyline> currentTourLines;
    private Map<Long, Polyline> drawnToursMap;
    private Map<Long, Marker> markersMap;

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

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View toReturn = inflater.inflate(R.layout.fragment_map, container, false);
        ButterKnife.inject(this, toReturn);

        currentTourLines = new ArrayList<>();
        drawnToursMap = new HashMap<>();
        markersMap = new HashMap<>();

        previousCameraLocation = cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        initializeMap();
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
                presenter.loadEncounterOnMap(encounter);
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
    public void onPause() {
        if (tourService != null) {
            tourService.unregister(MapEntourageFragment.this);
            doUnbindService();
        }
        super.onPause();
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

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void onNotificationAction(String action) {
        if (ConfirmationActivity.KEY_RESUME_TOUR.equals(action)) {
            resumeTour();
        }
        else if (ConfirmationActivity.KEY_END_TOUR.equals(action)) {
            stopTour();
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

    public void displayChosenTour(Tour tour) {
        presenter.openTour(tour);
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    void doBindService() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), TourService.class);
            getActivity().startService(intent);
            getActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE);
            isBound = true;
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
    public void onTourCreated(long tourId) {
        currentTourId = tourId;
    }

    @Override
    public void onTourUpdated(Tour tour) {
        if (!tour.getTourPoints().isEmpty()) {
            drawCurrentLocation(tour.getTourPoints().get(tour.getTourPoints().size() - 1).getLocation());
        }
    }

    @Override
    public void onTourResumed(Tour tour) {
        if (!tour.getTourPoints().isEmpty()) {
            drawCurrentTour(tour);
            previousCoordinates = tour.getTourPoints().get(tour.getTourPoints().size() - 1).getLocation();
        }
    }

    @Override
    public void onLocationUpdated(LatLng location) {
        centerMap(location);
    }

    @Override
    public void onRetrieveToursNearby(List<Tour> tours) {
        removeDeprecatedTours(tours);
        for (Tour tour : removeRedundantTours(tours)) {
            drawNearbyTour(tour);
        }
    }

    @Override
    public void onToursFound(final Map<Long, Tour> tours) {
        if (getActivity() != null) {
            if (tours.isEmpty()){
                Toast.makeText(getActivity(), tourService.getString(R.string.tour_info_text_nothing_found), Toast.LENGTH_SHORT).show();
            } else {
                if (tours.size() > 1) {
                    //createTourSelectionDialog(tours);
                    List<Tour> tempList = new ArrayList<>();
                    for (Map.Entry<Long, Tour> entry : tours.entrySet()) {
                        tempList.add(entry.getValue());
                    }
                    Tour.Tours toursList = new Tour.Tours(tempList);
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                    ChoiceFragment choiceFragment = ChoiceFragment.newInstance(toursList);
                    choiceFragment.show(fragmentManager, "fragment_choice");
                } else {
                    for (Map.Entry<Long, Tour> tour : tours.entrySet()) {
                        presenter.openTour(tour.getValue());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onToursCountUpdated() {
        presenter.incrementUserToursCount();
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
        isFollowing = true;
        TourTransportMode tourTransportMode = TourTransportMode.findByRessourceId(radioGroupTransportMode.getCheckedRadioButtonId());
        TourType tourType = TourType.findByRessourceId(radioGroupType.getCheckedRadioButtonId());
        mapLauncherLayout.setVisibility(View.GONE);
        layoutMapTour.setVisibility(View.VISIBLE);
        startTour(tourTransportMode.getName(), tourType.getName());
    }

    @OnClick(R.id.tour_stop_button)
    public void onStopTour() {
        pauseTour();
        layoutMapTour.setVisibility(View.GONE);
        if (getActivity() != null) {
            launchConfirmationActivity();
        }
    }

    @OnClick(R.id.tour_add_encounter_button)
    public void onAddEncounter() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), CreateEncounterActivity.class);
            saveCameraPosition();
            Bundle args = new Bundle();
            args.putLong(Constants.KEY_TOUR_ID, getTourId());
            args.putDouble(Constants.KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
            args.putDouble(Constants.KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);
            intent.putExtras(args);
            startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
        }
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
        if (getActivity() != null) {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS, Constants.DISTANCE_BETWEEN_UPDATES_METERS, new CustomLocationListener());
        }
    }

    private void initializeMap() {
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().setMyLocationEnabled(true);
            mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
        }
        mapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition);
                Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                Location newLocation = cameraPositionToLocation(null, cameraPosition);

                if (newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT) {
                    previousCameraLocation = newLocation;
                    tourService.updateNearbyTours();
                }

                if (isFollowing && currentLocation != null) {
                    if (currentLocation.distanceTo(newLocation) > 1) {
                        isFollowing = false;
                    }
                }
            }
        });
        mapFragment.getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                tourService.searchToursFromPoint(latLng);
            }
        });
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------

    private void startTour(String transportMode, String type) {
        color = getTrackColor(type, new Date());
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
            layoutMapTour.setVisibility(View.VISIBLE);
        }
    }

    private void stopTour() {
        for (Polyline line : currentTourLines) {
            line.remove();
        }
        currentTourLines.clear();
        if (tourService.isRunning()) {
            tourService.endTreatment();
            previousCoordinates = null;
            clearMap();
            mapPin.setVisibility(View.GONE);
            layoutMapTour.setVisibility(View.GONE);
            buttonStartLauncher.setVisibility(View.VISIBLE);
        }
    }

    private void launchConfirmationActivity() {
        Bundle args = new Bundle();
        args.putSerializable(Tour.KEY_TOUR, getCurrentTour());
        Intent confirmationIntent = new Intent(getActivity(), ConfirmationActivity.class);
        confirmationIntent.putExtras(args);
        getActivity().startActivity(confirmationIntent);
    }

    private Tour getCurrentTour() {
        return tourService != null ? tourService.getCurrentTour() : null;
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

    private void removeDeprecatedTours(List<Tour> tours) {
        boolean found;
        Iterator iteratorLines = drawnToursMap.entrySet().iterator();
        while (iteratorLines.hasNext()) {
            found = false;
            Map.Entry pair = (Map.Entry) iteratorLines.next();
            for (Tour tour : tours) {
                if (pair.getKey() == tour.getId()) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                drawnToursMap.get(pair.getKey()).remove();
                iteratorLines.remove();
                markersMap.get(pair.getKey()).remove();
                markersMap.remove(pair.getKey());
                presenter.getOnClickListener().removeMarker((long)pair.getKey());
            }
        }
    }

    private List<Tour> removeRedundantTours(List<Tour> tours) {
        Iterator iteratorTours = tours.iterator();
        while (iteratorTours.hasNext()) {
            Tour tour = (Tour) iteratorTours.next();
            if (drawnToursMap.containsKey(tour.getId())) {
                iteratorTours.remove();
            }
        }
        return tours;
    }

    private int getTrackColor(String type, Date date) {
        int color = Color.GRAY;
        if (TourType.SOCIAL.getName().equals(type)) {
            color = Color.GREEN;
        }
        else if (TourType.FOOD.getName().equals(type)) {
            color = Color.BLUE;
        }
        else if (TourType.OTHER.getName().equals(type)) {
            color = Color.RED;
        }
        if (!isToday(date)) {
            color = getTransparentColor(color);
        }
        return color;
    }

    private int getTransparentColor(int color) {
        return Color.argb(100, Color.red(color), Color.green(color), Color.blue(color));
    }

    private boolean isToday(Date date) {
        Date today = new Date();
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(date);
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR));
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
            EntourageLocation.getInstance().saveLastCameraPosition(mapFragment.getMap().getCameraPosition());
        }
    }

    private void drawCurrentLocation(LatLng location) {
        if (previousCoordinates != null) {
            PolylineOptions line = new PolylineOptions();
            line.add(previousCoordinates, location);
            line.width(15).color(color);
            currentTourLines.add(mapFragment.getMap().addPolyline(line));
        }
        previousCoordinates = location;
    }

    private void drawCurrentTour(Tour tour) {
        if (tour != null && !tour.getTourPoints().isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            line.width(15).color(getTrackColor(tour.getTourType(), tour.getTourPoints().get(0).getPassingTime()));
            for (TourPoint tourPoint : tour.getTourPoints()) {
                line.add(tourPoint.getLocation());
            }
            currentTourLines.add(mapFragment.getMap().addPolyline(line));
        }
    }

    private void drawNearbyTour(Tour tour) {
        if (tour != null && !tour.getTourPoints().isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            line.width(15).color(getTrackColor(tour.getTourType(), tour.getTourPoints().get(0).getPassingTime()));
            for (TourPoint tourPoint : tour.getTourPoints()) {
                line.add(tourPoint.getLocation());
            }
            drawnToursMap.put(tour.getId(), mapFragment.getMap().addPolyline(line));
            if (tour.getTourStatus() == null) {
                tour.setTourStatus(Tour.TOUR_CLOSED);
            }
            addTourHead(tour);
        }
    }

    private void addTourHead(Tour tour) {
        TourPoint lastPoint = tour.getTourPoints().get(tour.getTourPoints().size() - 1);
        double latitude = lastPoint.getLatitude();
        double longitude = lastPoint.getLongitude();
        LatLng position = new LatLng(latitude, longitude);

        BitmapDescriptor icon = null;
        if (tour.getTourVehicleType().equals(TourTransportMode.FEET.getName())) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_feet);
        }
        else if (tour.getTourVehicleType().equals(TourTransportMode.CAR.getName())) {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_car);
        }

        MarkerOptions markerOptions = new MarkerOptions().position(position).icon(icon);

        if (mapFragment.getMap() != null) {
            markersMap.put(tour.getId(), mapFragment.getMap().addMarker(markerOptions));
            presenter.getOnClickListener().addTourMarker(position, tour);
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {

            EntourageLocation.getInstance().saveCurrentLocation(location);

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
            if (getActivity() != null) {
                tourService = ((TourService.LocalBinder) service).getService();
                tourService.register(MapEntourageFragment.this);
                tourService.updateNearbyTours();

                boolean isRunning = tourService != null && tourService.isRunning();
                if (isRunning) {
                    buttonStartLauncher.setVisibility(View.GONE);
                    if (tourService.isPaused()) {
                        layoutMapTour.setVisibility(View.GONE);
                        launchConfirmationActivity();
                    } else {
                        mapPin.setVisibility(View.VISIBLE);
                        layoutMapTour.setVisibility(View.VISIBLE);
                    }
                }

                Intent intent = getActivity().getIntent();
                if (intent.getBooleanExtra(TourService.NOTIFICATION_PAUSE, false)) {
                    onNotificationAction(TourService.NOTIFICATION_PAUSE);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(MapEntourageFragment.this);
            tourService = null;
        }
    }
}
