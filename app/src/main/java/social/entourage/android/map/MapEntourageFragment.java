package social.entourage.android.map;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.TourType;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.tour.TourService;

/**
 * Created by RPR on 25/03/15.
 */
public class MapEntourageFragment extends Fragment implements TourService.TourServiceListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String POI_DRAWABLE_NAME_PREFIX = "poi_category_";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private OnTourLaunchListener callback;

    private SupportMapFragment mapFragment;
    private LatLng previousCoordinates;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            tourService = ((TourService.LocalBinder)service).getService();
            tourService.register(MapEntourageFragment.this);
            boolean isRunning = tourService != null && tourService.isRunning();
            if (isRunning) {
                callback.onTourResume(tourService.isPaused());
            }
            //Toast.makeText(MapActivity.this, R.string.local_service_connected, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(MapEntourageFragment.this);
            tourService = null;
            //Toast.makeText(MapActivity.this, R.string.local_service_disconnected, Toast.LENGTH_SHORT).show();
        }
    };
    private boolean isBound = true;
    private boolean isFollowing = true;
    private int color;

    @InjectView(R.id.fragment_map_pin)
    View mapPin;

    @InjectView(R.id.fragment_map_follow_button)
    View centerButton;

    @InjectView(R.id.button_start_tour_launcher)
    Button buttonStartLauncher;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public static MapEntourageFragment newInstance() {
        return new MapEntourageFragment();
    }

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
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().setMyLocationEnabled(true);
            mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(false);
        }
        mapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Location newLocation = cameraPositionToLocation("new",  EntourageLocation.getInstance().getLastCameraPosition());
                Location prevLocation = cameraPositionToLocation("previous", cameraPosition);
                if (newLocation.distanceTo(prevLocation) >= 5) {
                    isFollowing = false;
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            callback = (OnTourLaunchListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnTourLaunchListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        doBindService();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tourService != null) {
            tourService.unregister(MapEntourageFragment.this);
            doUnbindService();
        }
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

    @Override
    public void onNotificationAction(String action) {
        callback.onNotificationAction(action);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private Location cameraPositionToLocation(String provider, CameraPosition cameraPosition) {
        Location location = new Location(provider);
        location.setLatitude(cameraPosition.target.latitude);
        location.setLongitude(cameraPosition.target.longitude);
        return location;
    }

    // ----------------------------------
    // PUBLIC METHODS (tours)
    // ----------------------------------

    @OnClick(R.id.fragment_map_follow_button)
    public void followGeolocation(View view) {
        isFollowing = true;
    }

    @OnClick(R.id.button_start_tour_launcher)
    public void startTourLauncher(View view) {
        if (!tourService.isRunning()) {
            enableStartButton(false);
            callback.onTourLaunch();
        }
    }

    @OnClick(R.id.button_show_tours)
    public void showToursList(View view) {
        PopupMenu popupMenu = new PopupMenu(this.getActivity(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_popup_maraudes, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return false;
            }
        });
        popupMenu.show();

        // here add past tours dynamically
        // ...
    }

    public void startTour(String type1, String type2) {
        changeTrackColor(type2);
        if (tourService != null) {
            if (!tourService.isRunning()) {
                enableMapPin(true);
                tourService.beginTreatment(type1, type2);
            }
        }
    }

    public void pauseTour() {
        if (tourService != null) {
            if (tourService.isRunning()) tourService.pauseTreatment();
        }
    }

    public void resumeTour() {
        if (tourService.isRunning()) {
            tourService.resumeTreatment();
            enableStartButton(false);
            enableMapPin(true);
        }
    }

    public void stopTour() {
        if (tourService.isRunning()) {
            tourService.endTreatment();
            previousCoordinates = null;
            clearMap();
            enableMapPin(false);
            enableStartButton(true);
        }
    }

    public Tour getCurrentTour() {
        return tourService.getCurrentTour();
    }

    public long getTourId() {
        return tourService.getTourId();
    }

    public void addEncounter(Encounter encounter) {
        tourService.addEncounter(encounter);
    }

    // ----------------------------------
    // PUBLIC METHODS (views)
    // ----------------------------------

    public void changeTrackColor(String type) {
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

    public void enableStartButton(boolean enable) {
        if (enable) {
            buttonStartLauncher.setClickable(true);
            buttonStartLauncher.setVisibility(View.VISIBLE);
        } else {
            buttonStartLauncher.setClickable(false);
            buttonStartLauncher.setVisibility(View.INVISIBLE);
        }
    }

    public void enableMapPin(boolean enable) {
        if (enable) {
            mapPin.setVisibility(View.VISIBLE);
        } else {
            mapPin.setVisibility(View.INVISIBLE);
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
        BitmapDescriptor encounterIcon = BitmapDescriptorFactory.fromResource(R.drawable.rencontre);

        MarkerOptions markerOptions = new MarkerOptions().position(encounterPosition)
                                                         .icon(encounterIcon);

        if (mapFragment.getMap() != null) {
            mapFragment.getMap().addMarker(markerOptions);
            onClickListener.addEncounterMarker(encounterPosition, encounter);
        }
    }

    public void clearMap() {
        if (mapFragment.getMap() != null) mapFragment.getMap().clear();
    }

    public void initializeMapZoom() {
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    public void centerMap(LatLng latLng) {
        if (isFollowing) {
            CameraPosition cameraPosition = new CameraPosition(latLng, EntourageLocation.getInstance().getLastCameraPosition().zoom, 0, 0);
            centerMap(cameraPosition);
        }
    }

    private void centerMap(CameraPosition cameraPosition) {
        if(mapFragment!= null && mapFragment.getMap() != null) {
            mapFragment.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    public void saveCameraPosition() {
        if(mapFragment!= null && mapFragment.getMap() != null) {
            EntourageLocation.getInstance().saveCameraPosition(mapFragment.getMap().getCameraPosition());
        }
    }

    public void drawLocation(LatLng location) {
        if (previousCoordinates != null) {
            PolylineOptions line = new PolylineOptions();
            line.add(previousCoordinates, location);
            line.width(15).color(color);
            mapFragment.getMap().addPolyline(line);
        }
        previousCoordinates = location;
    }

    public void drawResumedTour(Tour tour) {
        changeTrackColor(tour.getTourType());
        if (tour != null && !tour.getCoordinates().isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            line.width(15).color(color);
            for (LatLng location : tour.getCoordinates()) {
                line.add(location);
            }
            mapFragment.getMap().addPolyline(line);
            previousCoordinates = tour.getCoordinates().get(tour.getCoordinates().size()-1);
        }

    }

    // ----------------------------------
    // INTERFACES
    // ----------------------------------

    public interface OnTourLaunchListener {
        void onTourLaunch();
        void onTourResume(boolean isPaused);
        void onNotificationAction(String action);
    }
}
