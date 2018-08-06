package social.entourage.android.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.EntourageEvents;
import social.entourage.android.EntourageLocation;
import social.entourage.android.EntourageSecuredActivity;
import social.entourage.android.R;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.EntourageDialogFragment;
import social.entourage.android.tools.BusProvider;

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
    private static final String KEY_USE_GOOGLE_PLACES_ONLY = "social.entourage.android.KEY_USE_GOOGLE_PLACES_ONLY";

    private static final float LOCATION_MOVE_DELTA = 50; //meters

    private static final float LOCATION_SEARCH_RADIUS = 0.18f; // 20 kilometers in lat/long degrees

    private static final int PERMISSIONS_REQUEST_LOCATION = 1;

    // ----------------------------------
    // Attributes
    // ----------------------------------

    private LatLng originalLocation;
    private String originalAddress;
    private boolean useGooglePlacesOnly = false;

    private LatLng location;
    private Place selectedPlace;

    private OnFragmentInteractionListener mListener;

    View toReturn;

    @BindView(R.id.entourage_location_address)
    TextView addressTextView;

    SupportMapFragment mapFragment;
    GoogleMap map;

    SupportPlaceAutocompleteFragment autocompleteFragment = null;

    private GeocoderAddressTask geocoderAddressTask = null;

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
     * @param address The initial address as string
     * @param listener The listener
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

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param location The initial location.
     * @param address The initial address as string
     * @param useGooglePlacesOnly true if the fragment will use only Google Places to fetch an address
     * @param listener The listener
     * @return A new instance of fragment LocationFragment.
     */

    public static LocationFragment newInstance(LatLng location, String address, boolean useGooglePlacesOnly, OnFragmentInteractionListener listener) {
        LocationFragment fragment = new LocationFragment();
        fragment.mListener = listener;
        Bundle args = new Bundle();
        args.putParcelable(KEY_ENTOURAGE_LOCATION, location);
        args.putString(KEY_ENTOURAGE_ADDRESS, address);
        args.putBoolean(KEY_USE_GOOGLE_PLACES_ONLY, useGooglePlacesOnly);
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
            useGooglePlacesOnly = getArguments().getBoolean(KEY_USE_GOOGLE_PLACES_ONLY, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        toReturn = inflater.inflate(R.layout.fragment_entourage_location, container, false);
        ButterKnife.bind(this, toReturn);

        toReturn.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    toReturn.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    toReturn.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                int h = toReturn.getHeight();
                if (getDialog() != null) {
                    Window window = getDialog().getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams layoutParams = window.getAttributes();
                        layoutParams.height = h;
                        window.setAttributes(layoutParams);
                    }
                }
            }
        });

        return toReturn;
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
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            EntourageSecuredActivity activity = (EntourageSecuredActivity) getActivity();
            if (activity != null && map != null) {
                for (int index = 0; index < permissions.length; index++) {
                    if (permissions[index].equalsIgnoreCase(activity.getUserLocationAccess()) && grantResults[index] == PackageManager.PERMISSION_GRANTED) {
                        try {
                            map.setMyLocationEnabled(true);
                        } catch (SecurityException ex) {
                            Log.d("LOCATION", ex.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
            mListener.onEntourageLocationChosen(location, addressTextView.getText().toString(), selectedPlace);
        }
        dismiss();
    }

    @OnClick(R.id.entourage_location_current_position)
    protected void onCurrentLocationClicked() {
        if (map != null) {
            EntourageSecuredActivity activity = (EntourageSecuredActivity) getActivity();
            if (activity != null) {
                if (activity.isGeolocationGranted()) {
                    Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
                    if (currentLocation != null) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                        map.moveCamera(cameraUpdate);
                    }
                } else {
                    requestPermissions(new String[]{activity.getUserLocationAccess()}, PERMISSIONS_REQUEST_LOCATION);
                }
            }
        }
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

        initializePlaces();
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
                        location = cameraPosition.target;
                        pin.setPosition(location);

                        if (geocoderAddressTask != null) {
                            geocoderAddressTask.cancel(true);
                        }
                        if (!useGooglePlacesOnly) {
                            geocoderAddressTask = new GeocoderAddressTask();
                            geocoderAddressTask.execute(location);
                        }

                        if (autocompleteFragment != null) {
                            //autocompleteFragment.setBoundsBias(LatLngBounds.builder().include(location).build());
                            autocompleteFragment.setBoundsBias(new LatLngBounds(
                                    new LatLng(location.latitude - LOCATION_SEARCH_RADIUS, location.longitude - LOCATION_SEARCH_RADIUS),
                                    new LatLng(location.latitude + LOCATION_SEARCH_RADIUS, location.longitude + LOCATION_SEARCH_RADIUS)
                            ));
                        }
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

    private void initializePlaces() {

        if (autocompleteFragment == null) {
            autocompleteFragment = new SupportPlaceAutocompleteFragment();
        }
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.entourage_location_places, autocompleteFragment).commit();

//        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
//                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ESTABLISHMENT)
//                .build();
//        autocompleteFragment.setFilter(typeFilter);

        if (originalLocation != null) {
            //autocompleteFragment.setBoundsBias(LatLngBounds.builder().include(originalLocation).build());
            autocompleteFragment.setBoundsBias(new LatLngBounds(
                    new LatLng(originalLocation.latitude - LOCATION_SEARCH_RADIUS, originalLocation.longitude - LOCATION_SEARCH_RADIUS),
                    new LatLng(originalLocation.latitude + LOCATION_SEARCH_RADIUS, originalLocation.longitude + LOCATION_SEARCH_RADIUS)
            ));
        } else {
            autocompleteFragment.setBoundsBias(new LatLngBounds(new LatLng(42, -5), new LatLng(51, 9)));
        }

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (place.getLatLng() != null) {
                    if (map != null) {
                        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(place.getLatLng());
                        map.moveCamera(cameraUpdate);
                        pin.setPosition(place.getLatLng());
                    }
                    addressTextView.setText(place.getAddress());
                }
                selectedPlace = place;
            }

            @Override
            public void onError(Status status) {
                if (getActivity() != null) {
                    Toast.makeText(getActivity(), R.string.entourage_location_address_not_found, Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    public interface OnFragmentInteractionListener {
        void onEntourageLocationChosen(LatLng location, String address, Place place);
    }
}
