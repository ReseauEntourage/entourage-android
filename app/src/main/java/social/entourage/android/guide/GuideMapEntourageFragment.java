package social.entourage.android.guide;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.maps.android.clustering.ClusterManager;

import java.util.Collection;

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
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    GuideMapPresenter presenter;

    private SupportMapFragment mapFragment;
    private ClusterManager<Poi> clusterManager;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_guide_map, container, false);
        ButterKnife.inject(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);

        if (mapFragment.getMap() != null) {
            clusterManager = new ClusterManager(getActivity(), mapFragment.getMap());
            clusterManager.setRenderer(new PoiRenderer(getActivity(), mapFragment.getMap(), clusterManager));
            mapFragment.getMap().setOnCameraChangeListener(clusterManager);
            mapFragment.getMap().setOnMarkerClickListener(clusterManager);
            mapFragment.getMap().setMyLocationEnabled(true);
            mapFragment.getMap().getUiSettings().setMyLocationButtonEnabled(true);
            clusterManager.setOnClusterItemClickListener(new OnEntourageMarkerClickListener());
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
                clusterManager.addItems(pois);
            }
        }
    }

    public void clearMap() {
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().clear();
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
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements ClusterManager.OnClusterItemClickListener<Poi> {
        @Override
        public boolean onClusterItemClick(Poi poi) {
            saveCameraPosition();
            Intent intent = new Intent(getActivity(), ReadPoiActivity.class);
            Bundle extras = new Bundle();
            extras.putSerializable(Constants.KEY_POI, poi);
            intent.putExtras(extras);
            startActivity(intent);
            return false;
        }
    }


}
