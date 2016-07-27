package social.entourage.android.guide;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.flurry.android.FlurryAgent;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Category;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.guide.poi.ReadPoiActivity;

public class GuideMapEntourageFragment extends Fragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.fragment_guide";

    public static final float ZOOM_REDRAW_LIMIT = 1.1f;
    public static final int REDRAW_LIMIT = 300;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    GuideMapPresenter presenter;

    private View toReturn;

    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Location previousCameraLocation;
    private float previousCameraZoom = 1.0f;
    private ClusterManager<Poi> clusterManager;
    private Map<Long, Poi> poisMap;
    private PoiRenderer poiRenderer;
    private boolean isMapLoaded = false;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_guide_map, container, false);
        }
        ButterKnife.bind(this, toReturn);
        FlurryAgent.logEvent(Constants.EVENT_OPEN_GUIDE_FROM_MENU);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        poisMap = new TreeMap<>();
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());

        if (!isMapLoaded) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    isMapLoaded = true;
                    clusterManager = new ClusterManager<>(getActivity(), googleMap);
                    poiRenderer = new PoiRenderer(getActivity(), googleMap, clusterManager);
                    clusterManager.setRenderer(poiRenderer);
                    clusterManager.setOnClusterItemClickListener(new OnEntourageMarkerClickListener());
                    googleMap.setOnMarkerClickListener(clusterManager);
                    if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        googleMap.setMyLocationEnabled(true);
                    }
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                    googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                        @Override
                        public void onCameraChange(CameraPosition cameraPosition) {
                            clusterManager.onCameraChange(cameraPosition);
                            EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition);
                            Location newLocation = EntourageLocation.cameraPositionToLocation(null, cameraPosition);
                            float newZoom = cameraPosition.zoom;
                            if (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT) {
                                previousCameraZoom = newZoom;
                                previousCameraLocation = newLocation;
                                presenter.updatePoisNearby();
                            }
                        }
                    });
                    map = googleMap;
                }
            });
        }
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerGuideMapComponent.builder()
                .entourageComponent(entourageComponent)
                .guideMapModule(new GuideMapModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setTitle(R.string.activity_display_guide_title);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.start();
    }

    @OnClick(R.id.fragment_guide_follow_button)
    void onFollowGeolocation() {
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null && map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            map.moveCamera(cameraUpdate);
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void putPoiOnMap(List<Category> categories, Collection<Poi> pois) {
        if (getActivity() != null) {
            poiRenderer.setCategories(categories);
            if (mapFragment.getMap() != null) {
                clusterManager.addItems(removeRedundantPois(pois));
                clusterManager.cluster();
            }
        }
    }

    public void initializeMapZoom() {
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    private void centerMap(CameraPosition cameraPosition) {
        if(mapFragment!= null && mapFragment.getMap() != null) {
            mapFragment.getMap().moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    public void saveCameraPosition() {
        if(mapFragment!= null && mapFragment.getMap() != null) {
            EntourageLocation.getInstance().saveLastCameraPosition(mapFragment.getMap().getCameraPosition());
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private Collection<Poi> removeRedundantPois(Collection<Poi> pois) {
        Iterator iterator = pois.iterator();
        while (iterator.hasNext()) {
            Poi poi = (Poi) iterator.next();
            if (!poisMap.containsKey(poi.getId())) {
                poisMap.put(poi.getId(), poi);
            } else {
                iterator.remove();
            }
        }
        return pois;
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements ClusterManager.OnClusterItemClickListener<Poi> {
        @Override
        public boolean onClusterItemClick(Poi poi) {
            saveCameraPosition();
            Intent intent = new Intent(getActivity(), ReadPoiActivity.class);
            Bundle extras = new Bundle();
            extras.putSerializable(ReadPoiActivity.BUNDLE_KEY_POI, poi);
            intent.putExtras(extras);
            startActivity(intent);
            return false;
        }
    }
}
