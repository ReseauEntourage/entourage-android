package social.entourage.android.map;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Poi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import social.entourage.android.common.Constants;
import social.entourage.android.encounter.CreateEncounterActivity;

/**
 * Created by RPR on 25/03/15.
 */
public class MapEntourageFragment extends Fragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------


    private static final double INITIAL_LATITUDE = 48.841636;
    private static final double INITIAL_LONGITUDE = 2.335899;
    private static final float INITIAL_CAMERA_FACTOR = 15;
    //private static final float DEFAULT_ZOOM_FACTOR = 17;

    private static CameraPosition lastCameraPosition;
    public static CameraPosition getLastCameraPosition() {
        if(lastCameraPosition == null) {
            lastCameraPosition = new CameraPosition(new LatLng(INITIAL_LATITUDE, INITIAL_LONGITUDE), INITIAL_CAMERA_FACTOR,0, 0);
        }
        return lastCameraPosition;
    };

    public static void resetCameraPosition(LatLng latLng) {
        lastCameraPosition = new CameraPosition(latLng, INITIAL_CAMERA_FACTOR,0, 0);
    }

    public static final String POI_DRAWABLE_NAME_PREFIX = "poi_category_";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private SupportMapFragment mapFragment;

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

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------


    @OnClick(R.id.button_add_encounter)
    public void openCreateEncounter(View view) {
        Activity parent = this.getActivity();
        if (parent != null) {
            Intent intent = new Intent(parent, CreateEncounterActivity.class);

            saveCameraPosition();

            Bundle args = new Bundle();
            args.putDouble(Constants.KEY_LATITUDE, lastCameraPosition.target.latitude);
            args.putDouble(Constants.KEY_LONGITUDE, lastCameraPosition.target.longitude);

            intent.putExtras(args);

            parent.startActivityForResult(intent, Constants.REQUEST_CREATE_ENCOUNTER);
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

    public void putPoiOnMap(Poi poi, MapPresenter.OnEntourageMarkerClickListener onClickListener) {
        double poiLatitude = poi.getLatitude();
        double poiLongitude = poi.getLongitude();
        LatLng poiPosition = new LatLng(poiLatitude, poiLongitude);

        int poiIconRessourceId = getActivity().getResources().getIdentifier(POI_DRAWABLE_NAME_PREFIX + poi.getCategoryId(), "drawable", getActivity().getPackageName());
        BitmapDescriptor poiIcon = BitmapDescriptorFactory.fromResource(poiIconRessourceId);

        MarkerOptions markerOptions = new MarkerOptions().position(poiPosition)
                                                         .icon(poiIcon);

        if (mapFragment.getMap() != null) {
            Marker marker = mapFragment.getMap().addMarker(markerOptions);
            onClickListener.addPoiMarker(poiPosition, poi);
        }
    }

    public void clearMap() {
        if (mapFragment.getMap() != null) {
            mapFragment.getMap().clear();
        }
    }

    public void initializeMapZoom() {
        centerMap(getLastCameraPosition());
    }

    public void centerMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, getLastCameraPosition().zoom,0, 0);
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
            lastCameraPosition = mapFragment.getMap().getCameraPosition();
        }
    }
}
