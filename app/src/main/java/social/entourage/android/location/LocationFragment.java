package social.entourage.android.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.base.EntourageDialogFragment;

/**
 * Fragment to choose the location of an entourage
 * Activities that contain this fragment must implement the
 * {@link LocationFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link LocationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class LocationFragment extends EntourageDialogFragment {

    // ----------------------------------
    // Constants
    // ----------------------------------

    public static final String TAG = "social.entourage.android.entourage.location";

    private static final String KEY_ENTOURAGE_LOCATION = "social.entourage.android.KEY_ENTOURAGE_LOCATION";
    private static final String KEY_ENTOURAGE_ADDRESS = "social.entourage.android.KEY_ENTOURAGE_ADDRESS";

    private static final float LOCATION_MOVE_DELTA = 50; //meters

    private static final float LOCATION_SEARCH_RADIUS = 0.18f; // 20 kilometers in lat/long degrees

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private LatLng originalLocation;
    private String originalAddress;

    private LatLng location;

    private OnFragmentInteractionListener mListener;

    @BindView(R.id.entourage_location_search)
    EditText searchEditText;

    @BindView(R.id.entourage_location_search_cancel_button)
    Button searchCancelButton;

    @BindView(R.id.entourage_location_address)
    TextView addressTextView;

    @BindView(R.id.entourage_location_search_result)
    ListView  searchResultListView;

    LocationSearchAdapter locationSearchAdapter;

    SupportMapFragment mapFragment;
    GoogleMap map;

    private GeocoderLocationTask geocoderLocationTask = null;
    private GeocoderAddressTask geocoderAddressTask = null;

    private boolean moveToLocationFound = false;

    private Marker pin;

    // ----------------------------------
    // Lifecycle
    // ----------------------------------

    public LocationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location The initial location.
     * @return A new instance of fragment LocationFragment.
     */

    public static LocationFragment newInstance(LatLng location, String address, OnFragmentInteractionListener listener) {
        LocationFragment fragment = new LocationFragment();
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

    // ----------------------------------
    // INTERFACE CALLBACKS
    // ----------------------------------

    @OnClick(R.id.title_close_button)
    protected void onBackClicked() {
        dismiss();
    }

    @OnClick(R.id.title_action_button)
    protected void onValidateClicked() {
        if (mListener != null) {
            mListener.onEntourageLocationChosen(location, addressTextView.getText().toString());
        }
        dismiss();
    }

    @OnClick(R.id.entourage_location_current_position)
    protected void onCurrentLocationClicked() {
        if (map != null) {
            boolean cameraMoved = false;
            Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
            if (currentLocation != null) {
                location = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);
                map.moveCamera(cameraUpdate);
                cameraMoved = true;
            }
            else if (originalLocation != null) {
                location = originalLocation;
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

    @OnClick(R.id.entourage_location_search_cancel_button)
    protected void onSearchCancelClicked() {
        searchEditText.setText("");
        searchResultListView.setVisibility(View.GONE);
        locationSearchAdapter.setAddressList(new ArrayList<Address>());
        hideKeyboard();
        moveToLocationFound = false;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeView() {
        // Initialize map
        initializeMap();

        // Initialize address
        if (originalAddress != null) {
            addressTextView.setText(originalAddress);
        }

        // Initialize the search field
        initializeSearch();
    }

    private void initializeMap() {
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
                if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
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

                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        CameraPosition cameraPosition = map.getCameraPosition();
                        float[] results = new float[1];
                        Location.distanceBetween(location.latitude, location.longitude, cameraPosition.target.latitude, cameraPosition.target.longitude, results);
                        location = cameraPosition.target;
                        pin.setPosition(location);

                        if (geocoderAddressTask != null) {
                            geocoderAddressTask.cancel(true);
                        }
                        geocoderAddressTask = new GeocoderAddressTask();
                        geocoderAddressTask.execute(location);
                    }
                });

                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(final LatLng latLng) {
                        location = latLng;

                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
                        map.moveCamera(cameraUpdate);

                        pin.setPosition(location);

                        hideKeyboard();
                    }
                });

                map = googleMap;
            }
        });
    }

    private void initializeSearch() {
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
                if (event == null) {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        String search = v.getText().toString().trim();
                        doSearchAddress(search);
                        moveToLocationFound = true;
                    }
                }
                else if (event.getKeyCode() == KeyEvent.ACTION_DOWN) {
                    String search = v.getText().toString().trim();
                    doSearchAddress(search);
                    moveToLocationFound = true;
                }
                return false;
            }
        });
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(final CharSequence s, final int start, final int count, final int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, final int start, final int before, final int count) {
                searchCancelButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(final Editable s) {
                if (s.length() > 2) {
                    moveToLocationFound = false;
                    doSearchAddress(s.toString());
                }
            }
        });

        // initialize the search result list
        locationSearchAdapter = new LocationSearchAdapter();
        searchResultListView.setAdapter(locationSearchAdapter);
        searchResultListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
                Address address = locationSearchAdapter.getItem(position);
                onAddressFound(address);
            }
        });
    }

    private void doSearchAddress(String search) {
        if (search != null && search.length() > 0) {
            if (geocoderLocationTask != null) {
                geocoderLocationTask.cancel(true);
                geocoderLocationTask = null;
            }
            geocoderLocationTask = new GeocoderLocationTask();
            geocoderLocationTask.execute(search);
        } else {
            locationSearchAdapter.setAddressList(new ArrayList<Address>());
        }
    }

    private void onAddressesFound(List<Address> addresses, AsyncTask asyncTask) {
        if (geocoderLocationTask != asyncTask) return;
        if (addresses == null) {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), R.string.entourage_location_address_not_found, Toast.LENGTH_SHORT).show();
            }
            locationSearchAdapter.setAddressList(new ArrayList<Address>());
            return;
        }
        if (addresses.size() == 1 && moveToLocationFound) {
            Address address = addresses.get(0);
            onAddressFound(address);
        } else {
            searchResultListView.setVisibility(View.VISIBLE);
            locationSearchAdapter.setAddressList(addresses);
        }
    }

    private void onAddressFound(Address address) {
        if (address == null) {
            if (getActivity() != null) {
                Toast.makeText(getActivity(), R.string.entourage_location_address_not_found, Toast.LENGTH_SHORT).show();
            }
            searchResultListView.setVisibility(View.GONE);
            locationSearchAdapter.setAddressList(new ArrayList<Address>());
            return;
        }
        if (address.hasLatitude() && address.hasLongitude()) {
            location = new LatLng(address.getLatitude(), address.getLongitude());
            if (map != null) {
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(location);
                map.moveCamera(cameraUpdate);
                pin.setPosition(location);
            }
            if (address.getMaxAddressLineIndex() > 0) {
                String addressLine = address.getAddressLine(0);
                addressTextView.setText(addressLine);
            }
            hideKeyboard();
        }
        searchResultListView.setVisibility(View.GONE);
        locationSearchAdapter.setAddressList(new ArrayList<Address>());
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
            catch (IOException ignored) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final String address) {
            if (address == null || geocoderAddressTask != this) return;
            LocationFragment.this.addressTextView.setText(address);
            geocoderAddressTask = null;
        }
    }

    private class GeocoderLocationTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(final String... params) {
            try {
                if (getActivity() == null) {
                    return null;
                }
                Geocoder geoCoder = new Geocoder(getActivity(), Locale.getDefault());
                String address = params[0];
                List<Address> addresses = geoCoder.getFromLocationName(address,
                        5,
                        location.latitude - LOCATION_SEARCH_RADIUS,
                        location.longitude - LOCATION_SEARCH_RADIUS,
                        location.latitude + LOCATION_SEARCH_RADIUS,
                        location.longitude + LOCATION_SEARCH_RADIUS);
                return addresses;
            }
            catch (IOException ignored) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(final List<Address> addresses) {
            onAddressesFound(addresses, this);
        }
    }

    public interface OnFragmentInteractionListener {
        void onEntourageLocationChosen(LatLng location, String address);
    }
}
