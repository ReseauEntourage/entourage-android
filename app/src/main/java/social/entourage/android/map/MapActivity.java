package social.entourage.android.map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import social.entourage.android.EntourageLocation;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.common.Constants;
import social.entourage.android.encounter.CreateEncounterActivity;
import social.entourage.android.guide.GuideMapActivity;
import social.entourage.android.login.LoginActivity;
import social.entourage.android.tour.TourService;

/**
 * Created by RPR on 25/03/15.
 */
public class MapActivity extends EntourageSecuredActivity implements ActionBar.TabListener, TourService.TourServiceListener {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private Fragment fragment;

    private TourService tourService;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            tourService = ((TourService.LocalBinder)service).getService();
            tourService.register(MapActivity.this);
            invalidateOptionsMenu();
            //Toast.makeText(MapActivity.this, R.string.local_service_connected, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            tourService.unregister(MapActivity.this);
            tourService = null;
            //Toast.makeText(MapActivity.this, R.string.local_service_disconnected, Toast.LENGTH_SHORT).show();
        }
    };
    private boolean isBound = true;

    //private Location bestLocation;
    private boolean isBetterLocationUpdated;
    private LocationListener locationListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    protected List<Object> getScopedModules() {
        return Arrays.<Object>asList(new MapModule(this));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        setTitle(R.string.activity_map_title);
        ButterKnife.inject(this);

        initializeLocationService();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);

        // TODO: Remove depreceated code : move tabs in toolbar
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.addTab(actionBar.newTab().setText(R.string.activity_map_tab_map).setTabListener(this));
        //TODO: display List Tab here
        //actionBar.addTab(actionBar.newTab().setText(R.string.activity_map_tab_liste).setTabListener(this));

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_encounter, menu);
        boolean isRunning = tourService != null && tourService.isRunning();
        MenuItem item = menu.findItem(R.id.action_maraude);
        item.setIcon(isRunning ? R.drawable.tour_stop : R.drawable.tour_record);
        item.setTitle(isRunning ? R.string.stop_tour_title : R.string.start_tour_title);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
        doBindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tourService != null) {
            tourService.unregister(MapActivity.this);
            doUnbindService();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }else if (id == R.id.action_logout) {
            getAuthenticationController().logOutUser();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_guide) {
            saveCameraPosition();
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(locationListener);
            startActivity(new Intent(this, GuideMapActivity.class));
        } else if (id == R.id.action_new_encounter) {
            Intent intent = new Intent(this, CreateEncounterActivity.class);
            saveCameraPosition();
            Bundle args = new Bundle();
            args.putDouble(Constants.KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
            args.putDouble(Constants.KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);
            intent.putExtras(args);
            startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
        } else if (id == R.id.action_maraude) {
            if (tourService != null) {
                if (tourService.isRunning()) {
                    item.setTitle(R.string.start_tour_title);
                    item.setIcon(R.drawable.tour_record);
                    tourService.endTreatment();
                    clearMap();
                } else {
                    item.setTitle(R.string.stop_tour_title);
                    item.setIcon(R.drawable.tour_stop);
                    tourService.beginTreatment();
                }
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CREATE_ENCOUNTER) {
            if (resultCode == Constants.RESULT_CREATE_ENCOUNTER_OK) {
                LatLng latLng = new LatLng(data.getExtras().getDouble(Constants.KEY_LATITUDE),
                        data.getExtras().getDouble(Constants.KEY_LONGITUDE));

                presenter.retrieveMapObjects(latLng);
            }
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        switch (tab.getPosition()) {
            case 0:
                fragment = MapEntourageFragment.newInstance();
                break;
            case 1:
                fragment = ListFragment.newInstance();
                break;
        }
        fragmentTransaction.replace(R.id.layout_fragment_container, fragment);
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        fragmentTransaction.remove(fragment);
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------

    void doBindService() {
        Intent intent = new Intent(MapActivity.this, TourService.class);
        startService(intent);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
        isBound = true;
    }

    void doUnbindService() {
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
    }

    @Override
    public void onTourUpdated(Tour tour) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.drawLocation(tour.getCoordinates().get(tour.getCoordinates().size() - 1));
        }
    }

    @Override
    public void onTourResumed(Tour tour) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.drawResumedTour(tour);
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void putEncouter(Encounter encounter, MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.putEncounterOnMap(encounter, onClickListener);
        }
    }

    public void setOnMarkerCLickListener(MapPresenter.OnEntourageMarkerClickListener onMarkerClickListener) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.setOnMarkerClickListener(onMarkerClickListener);
        }
    }

    public void clearMap() {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.clearMap();
        }
    }

    public void centerMap(LatLng latLng) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.centerMap(latLng);
        }
    }

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_TIMER_MILLIS,
//                DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.UPDATE_TIMER_MILLIS,
                Constants.DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
    }

    public void initializeMap() {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.initializeMapZoom();
        }
    }

    public void saveCameraPosition() {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.saveCameraPosition();
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
            if (bestLocation == null || (location.getAccuracy()>0.0 && bestLocation.getAccuracy()==0.0)) {
                EntourageLocation.getInstance().saveLocation(location);
                bestLocation = location;
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

    /**
     * This Runnable is executed 3s after each map objects
     * If during this time, a new better location has been registered, it will call immediately a
     * new object list, not waiting for a new location from LocationServices.
     */
    private class RequestWaiter implements Runnable {

        private final Location locationUsed;

        public RequestWaiter(final Location locationUsed) {
            this.locationUsed = locationUsed;
        }

        @Override
        public void run() {
            Location bestLocation = EntourageLocation.getInstance().getLocation();
            if (bestLocation.getAccuracy() > locationUsed.getAccuracy()) {
                isBetterLocationUpdated = true;
                locationListener.onLocationChanged(bestLocation);
            }
        }
    }

}