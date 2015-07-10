package social.entourage.android.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
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

    private SupportMapFragment mapFragment;
    private LatLng prevCoord;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            tourService = ((TourService.LocalBinder)service).getService();
            tourService.register(MapEntourageFragment.this);
            boolean isRunning = tourService != null && tourService.isRunning();
            Button button = (Button) getView().findViewById(R.id.button_start_tour);
            button.setText(isRunning ? R.string.tour_stop : R.string.tour_start);
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

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public static MapEntourageFragment newInstance() {
        MapEntourageFragment fragment = new MapEntourageFragment();
        return fragment;
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
            mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);
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

    @Override
    public void onTourUpdated(Tour tour) {
        if (!tour.getCoordinates().isEmpty())
            drawLocation(tour.getCoordinates().get(tour.getCoordinates().size() - 1));
    }

    @Override
    public void onTourResumed(Tour tour) {
        drawResumedTour(tour);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    /**
     *  Tours not created from this button anymore
     *
    @OnClick(R.id.button_add_encounter)
    public void openCreateEncounter(View view) {
        Activity parent = this.getActivity();
        if (parent != null) {
            Intent intent = new Intent(parent, CreateEncounterActivity.class);

            saveCameraPosition();

            Bundle args = new Bundle();
            args.putDouble(Constants.KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
            args.putDouble(Constants.KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);

            intent.putExtras(args);

            parent.startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
        }
    }
     */

    @OnClick(R.id.button_start_tour)
    public void startNewTour(View view) {
        if (tourService != null) {
            Button button = (Button) view;
            if (tourService.isRunning()) {
                button.setText(R.string.tour_start);
                tourService.endTreatment();
                clearMap();
            } else {
                button.setText(R.string.tour_stop);
                tourService.beginTreatment();
            }
        }
    }

    /** Implémentation de l'affichage des maraudes passées en cours (NTE) */
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

        // ici ajouter les maraudes sauvegardées dynamiquement
        // ...
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
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().clear();
            prevCoord = null;
        }
    }

    public void initializeMapZoom() {
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    public void centerMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, EntourageLocation.getInstance().getLastCameraPosition().zoom,0, 0);
        centerMap(cameraPosition);
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
        if (prevCoord != null) {
            PolylineOptions line = new PolylineOptions();
            line.add(prevCoord, location);
            line.width(15).color(Color.BLUE);
            mapFragment.getMap().addPolyline(line);
        }
        prevCoord = location;
    }

    public void drawResumedTour(Tour tour) {
        if (tour != null && !tour.getCoordinates().isEmpty()) {
            PolylineOptions line = new PolylineOptions();
            line.width(15).color(Color.BLUE);
            for (LatLng location : tour.getCoordinates()) {
                line.add(location);
            }
            mapFragment.getMap().addPolyline(line);
            prevCoord = tour.getCoordinates().get(tour.getCoordinates().size()-1);
        }

    }
}
