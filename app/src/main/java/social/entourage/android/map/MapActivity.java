package social.entourage.android.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import butterknife.ButterKnife;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.common.Constants;
import social.entourage.android.encounter.CreateEncounterActivity;
import social.entourage.android.guide.GuideMapActivity;
import social.entourage.android.login.LoginActivity;
import social.entourage.android.tour.TourService;

@SuppressWarnings("WeakerAccess")
public class MapActivity extends EntourageSecuredActivity implements ActionBar.TabListener, MapEntourageFragment.OnTourLaunchListener, MapLauncherFragment.OnTourStartListener, MapTourFragment.OnTourActionListener {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    MapPresenter presenter;

    private Fragment fragment;
    private Fragment launcher;
    private Fragment tour;

    //private Location bestLocation;
    private boolean isBetterLocationUpdated;
    private LocationListener locationListener;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

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
    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerMapComponent.builder()
                .entourageComponent(entourageComponent)
                .mapModule(new MapModule(this))
                .build()
                .inject(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_encounter, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.start();
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

    @Override
    public void onBackPressed() {
        if (launcher != null) {
            getSupportFragmentManager().beginTransaction().remove(launcher).commit();
            launcher = null;
            if (fragment instanceof MapEntourageFragment) {
                MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
                mapEntourageFragment.enableStartButton(true);
            }
        } else {
            super.onBackPressed();
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

    public void centerMap(LatLng latLng) {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.centerMap(latLng);
        }
    }

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
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
    // FRAGMENT INTERFACES METHODS
    // ----------------------------------

    @Override
    public void onTourLaunch() {
        launcher = MapLauncherFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_fragment_container, launcher).commit();
    }

    @Override
    public void onTourResume(boolean isPaused) {
        if (tour == null) {
            tour = MapTourFragment.newInstance();
            ((MapTourFragment)tour).setIsPaused(isPaused);
            getSupportFragmentManager().beginTransaction().add(R.id.layout_fragment_container, tour).commit();
        }
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.enableStartButton(false);
        }
    }

    @Override
    public void onPausedTourResumed() {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.resumeTour();
        }
    }

    @Override
    public void onTourStart(String type1, String type2) {
        if (launcher != null) {
            getSupportFragmentManager().beginTransaction().remove(launcher).commit();
            launcher = null;
        }

        tour = MapTourFragment.newInstance();
        getSupportFragmentManager().beginTransaction().add(R.id.layout_fragment_container, tour).commit();

        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.startTour(type1, type2);
        }
    }

    @Override
    public void onTourPaused() {
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.pauseTour();
        }
    }

    @Override
    public void onTourStopped() {
        getSupportFragmentManager().beginTransaction().remove(tour).commit();
        tour = null;
        if (fragment instanceof MapEntourageFragment) {
            MapEntourageFragment mapEntourageFragment = (MapEntourageFragment) fragment;
            mapEntourageFragment.stopTour();
        }
    }

    @Override
    public void onNotificationAction(String action) {
        if (TourService.NOTIFICATION_PAUSE.equals(action) || TourService.NOTIFICATION_RESUME.equals(action)) {
            ((MapTourFragment)tour).switchPauseButton();
        } else if (TourService.NOTIFICATION_STOP.equals(action)) {
            getSupportFragmentManager().beginTransaction().remove(tour).commit();
            tour = null;
            if (fragment instanceof MapEntourageFragment) {
                ((MapEntourageFragment) fragment).switchView();
            }
        }
    }

    @Override
    public void onNewEncounter() {
        Intent intent = new Intent(this, CreateEncounterActivity.class);
        saveCameraPosition();
        Bundle args = new Bundle();
        args.putDouble(Constants.KEY_LATITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.latitude);
        args.putDouble(Constants.KEY_LONGITUDE, EntourageLocation.getInstance().getLastCameraPosition().target.longitude);
        intent.putExtras(args);
        startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
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