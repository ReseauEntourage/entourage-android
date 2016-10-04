package social.entourage.android.map.entourage;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;

/**
 * Fragment to choose the location of an entourage
 * Activities that contain this fragment must implement the
 * {@link EntourageLocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EntourageLocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class EntourageLocationFragment extends DialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.entourage.location";

    private static final String KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION";
    private static final String KEY_ENTOURAGE_ADDRESS = "social.entourage.android.KEY_ENTOURAGE_ADDRESS";

    private static final float LOCATION_MOVE_DELTA = 50; //meters

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private LatLng originalLocation;
    private String originalAddress;

    private LatLng location;

    private OnFragmentInteractionListener mListener;

    @Bind(R.id.entourage_location_search)
    EditText searchEditText;

    @Bind(R.id.entourage_location_address)
    TextView addressTextView;

    SupportMapFragment mapFragment;
    GoogleMap map;

    private GeocoderLocationTask geocoderAddressTask = null;

    private Marker pin;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public EntourageLocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location The initial location.
     * @return A new instance of fragment EntourageLocationFragment.
     */

    public static EntourageLocationFragment newInstance(LatLng location, String address, OnFragmentInteractionListener listener) {
        EntourageLocationFragment fragment = new EntourageLocationFragment();
        fragment.mListener = listener;
        Bundle args = new Bundle();
        args.putParcelable(KEY_ENTOURAGE_LOCATION, location);
        args.putString(KEY_ENTOURAGE_ADDRESS, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            originalLocation = getArguments().getParcelable(KEY_ENTOURAGE_LOCATION);
            if (originalLocation != null) {
                location = new LatLng(originalLocation.latitude, originalLocation.longitude);
            }
            originalAddress = getArguments().getString(KEY_ENTOURAGE_ADDRESS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entourage_location, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeView();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().getAttributes().windowAnimations = R.style.CustomDialogFragmentSlide;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background)));
    }

    // ----------------------------------
    // INTERFACE CALLBACKS
    // ----------------------------------

    @OnClick(R.id.entourage_location_back_button)
    protected void onBackClicked() {
        if (mListener != null) {
            mListener.onEntourageLocationChoosen(location, addressTextView.getText().toString());
        }
        dismiss();
    }

    @OnClick(R.id.entourage_location_current_position)
    protected void onCurrentLocationClicked() {
        if (map != null) {
            boolean cameraMoved = false;
            Location location = EntourageLocation.getInstance().getCurrentLocation();
            if (location != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                map.moveCamera(cameraUpdate);
                cameraMoved = true;
            }
            else if (originalLocation != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(originalLocation);
                map.moveCamera(cameraUpdate);
                cameraMoved = true;
            }
            if (cameraMoved) {
                if (originalAddress != null) {
                    addressTextView.setText(originalAddress);
                }
            }
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {
        // Initialize map
        if (mapFragment == null) {
            GoogleMapOptions googleMapOptions = new GoogleMapOptions();
            googleMapOptions.zOrderOnTop(true);
            mapFragment = SupportMapFragment.newInstance(googleMapOptions);
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.entourage_location_map_layout, mapFragment).commit();

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    googleMap.setMyLocationEnabled(true);
                }
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                if (originalLocation != null) {
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(originalLocation, 15);
                    googleMap.moveCamera(cameraUpdate);

                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(originalLocation)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange));
                    pin = googleMap.addMarker(markerOptions);
                } else {
                    MarkerOptions markerOptions = new MarkerOptions()
                            .position(googleMap.getCameraPosition().target)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_pin_orange));
                    pin = googleMap.addMarker(markerOptions);
                }

                googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                    @Override
                    public void onCameraChange(final CameraPosition cameraPosition) {
                        float[] results = new float[1];
                        Location.distanceBetween(location.latitude, location.longitude, cameraPosition.target.latitude, cameraPosition.target.longitude, results);
                        if (results[0] >= LOCATION_MOVE_DELTA) {
                            location = cameraPosition.target;
                            GeocoderAddressTask geocoderAddressTask = new GeocoderAddressTask();
                            geocoderAddressTask.execute(location);

                            pin.setPosition(location);
                        }
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(final LatLng latLng) {
                        location = latLng;

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                        map.moveCamera(cameraUpdate);

                        GeocoderAddressTask geocoderAddressTask = new GeocoderAddressTask();
                        geocoderAddressTask.execute(location);

                        pin.setPosition(location);
                    }
                });

                map = googleMap;
            }
        });

        // Initialize address
        if (originalAddress != null) {
            addressTextView.setText(originalAddress);
        }

        // Initialize the search field
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                if (event == null) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        String search = v.getText().toString().trim();
                        doSearchAddress(search);
                    }
                }
                else if (event.getKeyCode() == KeyEvent.ACTION_DOWN) {
                    String search = v.getText().toString().trim();
                    doSearchAddress(search);
                }
                return false;
            }
        });
    }

    private void doSearchAddress(String search) {
        if (search != null && search.length() > 0) {
            if (geocoderAddressTask != null) {
                geocoderAddressTask.cancel(true);
                geocoderAddressTask = null;
            }
            geocoderAddressTask = new GeocoderLocationTask();
            geocoderAddressTask.execute(search);
        }
    }

    private void onAddressFound(Address address, AsyncTask asyncTask) {
        if (geocoderAddressTask != asyncTask) return;
        if (address == null) {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), R.string.entourage_location_address_not_found, Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (address.hasLatitude() && address.hasLongitude()) {
            if (map != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(address.getLatitude(), address.getLongitude()));
                map.moveCamera(cameraUpdate);
            }
            if (address.getMaxAddressLineIndex() > 0) {
                String addressLine = address.getAddressLine(0);
                addressTextView.setText(addressLine);
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class GeocoderAddressTask extends AsyncTask<LatLng, Void, String> {

        @Override
        protected String doInBackground(final LatLng... params) {
            try {
                if (getActivity() == null) {
                    return null;
                }
                Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                LatLng location = params[0];
                List<Address> addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1);
                String addressLine = "";
                if (addresses != null && addresses.size() > 0) {
                    Address address = addresses.get(0);
                    if (address.getMaxAddressLineIndex() >= 0) {
                        addressLine = addresses.get(0).getAddressLine(0);
                    }
                }
                return addressLine;
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final String address) {
            if (address == null) return;
            EntourageLocationFragment.this.addressTextView.setText(address);
        }
    }

    private class GeocoderLocationTask extends AsyncTask<String, Void, Address> {

        @Override
        protected Address doInBackground(final String... params) {
            try {
                if (getActivity() == null) {
                    return null;
                }
                Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                String address = params[0];
                List<Address> addresses = geoCoder.getFromLocationName(address, 1);
                if (addresses.size() > 0) {
                    return addresses.get(0);
                }
                return null;
            }
            catch (IOException e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final Address address) {
            onAddressFound(address, this);
        }
    }

    public interface OnFragmentInteractionListener {
        void onEntourageLocationChoosen(LatLng location, String address);
    }
}
