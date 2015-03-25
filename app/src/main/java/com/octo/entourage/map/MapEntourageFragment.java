package com.octo.entourage.map;

import com.google.android.gms.maps.SupportMapFragment;

import com.octo.entourage.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by RPR on 25/03/15.
 */
public class MapEntourageFragment extends Fragment {


    private Fragment searchMapFragment;

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
        searchMapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.id.fragment_map);
    }
}
