package com.octo.entourage.map;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.octo.entourage.R;
import com.octo.entourage.model.Encounter;
import com.octo.entourage.model.Poi;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by RPR on 25/03/15.
 */
public class MapEntourageFragment extends Fragment {

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
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.fragment_map);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

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
            Marker marker = mapFragment.getMap().addMarker(markerOptions);
            onClickListener.addEncounterMarker(marker, encounter);
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
            onClickListener.addPoiMarker(marker, poi);
        }
    }
}
