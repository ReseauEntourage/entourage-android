package social.entourage.android.guide;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.ButterKnife;
import social.entourage.android.Constants;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.guide.poi.ReadPoiActivity;

public class GuideMapEntourageFragment extends Fragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final int REDRAW_LIMIT = 300;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    GuideMapPresenter presenter;

    private SupportMapFragment mapFragment;
    private Location previousCameraLocation;
    private ClusterManager<Poi> clusterManager;
    private Map<Long, Poi> poisMap;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_map, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
        poisMap = new TreeMap<>();
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());

        if (mapFragment.getMap() != null) {
            clusterManager = new ClusterManager(getActivity(), mapFragment.getMap());
            clusterManager.setRenderer(new PoiRenderer(getActivity(), mapFragment.getMap(), clusterManager));
            clusterManager.setOnClusterItemClickListener(new OnEntourageMarkerClickListener());
            mapFragment.getMap().setOnMarkerClickListener(clusterManager);
            mapFragment.getMap().setMyLocationEnabled(true);
            mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);
            mapFragment.getMap().getUiSettings().setMapToolbarEnabled(false);
            mapFragment.getMap().setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition cameraPosition) {
                    clusterManager.onCameraChange(cameraPosition);
                    EntourageLocation.getInstance().saveCurrentCameraPosition(cameraPosition);
                    Location newLocation = EntourageLocation.cameraPositionToLocation(null, cameraPosition);
                    if (newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT) {
                        previousCameraLocation = newLocation;
                        presenter.updatePoisNearby();
                    }
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

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void putPoiOnMap(Collection<Poi> pois) {
        if (getActivity() != null) {
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
