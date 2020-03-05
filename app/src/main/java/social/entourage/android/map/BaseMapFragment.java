package social.entourage.android.map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.jetbrains.annotations.NotNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BackPressable;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageEvents;
import social.entourage.android.location.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.User;
import social.entourage.android.api.tape.Events;
import social.entourage.android.base.HeaderBaseAdapter;
import social.entourage.android.location.LocationUpdateListener;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public abstract class BaseMapFragment extends Fragment implements BackPressable, LocationUpdateListener {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.base_fragment_map";

    // Constants used to track the source call of the geolocation popup
    static final int GEOLOCATION_POPUP_TOUR = 0;
    private static final int GEOLOCATION_POPUP_RECENTER = 1;
    private static final int GEOLOCATION_POPUP_BANNER = 2;
    private static final int GEOLOCATION_POPUP_GUIDE_RECENTER = 3;
    private static final int GEOLOCATION_POPUP_GUIDE_BANNER = 4;

    static final int PERMISSIONS_REQUEST_LOCATION = 1;

    protected static final float ZOOM_REDRAW_LIMIT = 1.1f;
    protected static final int REDRAW_LIMIT = 300;

    protected int layout;
    protected String eventLongClick;
    boolean isFollowing = true;
    protected boolean isFullMapShown = true;
    protected Location previousCameraLocation;
    protected float previousCameraZoom = 1.0f;
    protected GoogleMap map;
    protected ClusterManager mapClusterManager;
    protected DefaultClusterRenderer mapClusterItemRenderer;
    protected int originalMapLayoutHeight;
    private View toReturn;

    @BindView(R.id.fragment_map_longclick)
    protected RelativeLayout mapLongClickView;

    @BindView(R.id.map_longclick_buttons)
    RelativeLayout mapLongClickButtonsView;

    @Override
    public boolean onBackPressed() {
        return false;
    }

    public BaseMapFragment(int layout) {
        this.layout = layout;
    }

    protected void initializeMap() {
    }

    void centerMap(LatLng latLng) {
        CameraPosition cameraPosition = new CameraPosition(latLng, EntourageLocation.getInstance().getLastCameraPosition().zoom, 0, 0);
        centerMap(cameraPosition);
    }

    protected void centerMapAndZoom(LatLng latLng, float zoom, boolean animated) {
        CameraPosition cameraPosition = new CameraPosition(latLng, zoom, 0, 0);
        if (map != null) {
            if (animated) {
                map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 300, null);
            } else {
                map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
            saveCameraPosition();
        }
    }

    private void centerMap(CameraPosition cameraPosition) {
        if (map != null && isFollowing) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    protected void saveCameraPosition() {
    }

    public void initializeMapZoom() {
        User me = EntourageApplication.get().getEntourageComponent().getAuthenticationController().getUser();
        if (me != null) {
            User.Address address = me.getAddress();
            if (address != null) {
                centerMap(new LatLng(address.getLatitude(), address.getLongitude()));
                isFollowing = false;
                return;
            }
        }
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (int index = 0; index < permissions.length; index++) {
                if (permissions[index].equalsIgnoreCase(ACCESS_FINE_LOCATION)) {
                    BusProvider.getInstance().post(new Events.OnLocationPermissionGranted(grantResults[index] == PackageManager.PERMISSION_GRANTED));
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("MissingPermission")
    protected void onMapReady(GoogleMap googleMap, ClusterManager.OnClusterItemClickListener onClickListener, GoogleMap.OnGroundOverlayClickListener onGroundOverlayClickListener) {
        map = googleMap;//we forced the settng of the map anyway
        if (getActivity() == null) {
            Timber.e("No activity found");
            return;
        }
        map.setMyLocationEnabled(LocationUtils.INSTANCE.isLocationPermissionGranted());

        //mylocation is handled in MapViewHolder
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_styles_json));

        mapClusterManager = new ClusterManager<>(getActivity(), map);
        mapClusterItemRenderer = getRenderer();
        mapClusterManager.setRenderer(mapClusterItemRenderer);
        mapClusterManager.setOnClusterItemClickListener(onClickListener);

        initializeMapZoom();
        map.setOnMarkerClickListener(mapClusterManager);
        if (onGroundOverlayClickListener != null) {
            map.setOnGroundOverlayClickListener(onGroundOverlayClickListener);
        }

        map.setOnMapLongClickListener(latLng -> {
            //only show when map is in full screen and not visible
            if (!isFullMapShown || mapLongClickView.getVisibility() == View.VISIBLE) {
                return;
            }
            if (getActivity() != null) {
                EntourageEvents.logEvent(eventLongClick);
                showLongClickOnMapOptions(latLng);
            }
        });
    }

    protected DefaultClusterRenderer getRenderer() {
        return null;
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (toReturn == null) {
            toReturn = inflater.inflate(layout, container, false);
        }
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------

    protected void showLongClickOnMapOptions(LatLng latLng) {
        //get the click point
        Point clickPoint = map.getProjection().toScreenLocation(latLng);
        //adjust the buttons holder layout
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        mapLongClickButtonsView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int bW = mapLongClickButtonsView.getMeasuredWidth();
        int bH = mapLongClickButtonsView.getMeasuredHeight();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mapLongClickButtonsView.getLayoutParams();
        int marginLeft = clickPoint.x - bW / 2;
        if (marginLeft + bW > screenSize.x) {
            marginLeft -= bW / 2;
        }
        if (marginLeft < 0) {
            marginLeft = 0;
        }
        int marginTop = clickPoint.y - bH / 2;
        if (marginTop < 0) {
            marginTop = clickPoint.y;
        }
        lp.setMargins(marginLeft, marginTop, 0, 0);
        mapLongClickButtonsView.setLayoutParams(lp);
        //show the view
        mapLongClickView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.fragment_map_longclick)
    void hideLongClickView() {
        onBackPressed();
    }

    void showAllowGeolocationDialog(final int source) {
        if (getActivity() == null) {
            return;
        }

        @StringRes int messagedId = R.string.map_error_geolocation_disabled_use_entourage;
        String eventName = EntourageEvents.EVENT_FEED_ACTIVATE_GEOLOC_FROM_BANNER;
        switch (source) {
            case GEOLOCATION_POPUP_RECENTER:
                messagedId = R.string.map_error_geolocation_disabled_recenter;
                eventName = EntourageEvents.EVENT_FEED_ACTIVATE_GEOLOC_RECENTER;
                break;
            case GEOLOCATION_POPUP_GUIDE_RECENTER:
                messagedId = R.string.map_error_geolocation_disabled_recenter;
                eventName = EntourageEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_RECENTER;
                break;
            case GEOLOCATION_POPUP_TOUR:
                messagedId = R.string.map_error_geolocation_disabled_create_tour;
                eventName = EntourageEvents.EVENT_FEED_ACTIVATE_GEOLOC_CREATE_TOUR;
                break;
            case GEOLOCATION_POPUP_GUIDE_BANNER:
                eventName = EntourageEvents.EVENT_GUIDE_ACTIVATE_GEOLOC_FROM_BANNER;
                break;
            case GEOLOCATION_POPUP_BANNER:
            default:
                break;
        }
        String finalEventName = eventName; // needs to be final for later functions
        new AlertDialog.Builder(getActivity())
                .setMessage(messagedId)
                .setPositiveButton(R.string.activate, (dialogInterface, i) -> {
                    EntourageEvents.logEvent(finalEventName);

                    try {
                        if (LocationUtils.INSTANCE.isLocationEnabled() || shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                        } else {
                            // User selected "Never ask again", so show the settings page
                            displayGeolocationPreferences(true);
                        }
                    } catch(IllegalStateException e) {
                        Timber.w(e);
                    }
                })
                .setNegativeButton(R.string.map_permission_refuse, null)
                .show();
    }

    void displayGeolocationPreferences(boolean forceDisplaySettings) {
        if (forceDisplaySettings || !LocationUtils.INSTANCE.isLocationEnabled()) {
            if (getActivity() != null) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        } else if (!LocationUtils.INSTANCE.isLocationPermissionGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_BANNER);
        }
    }

    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        updateGeolocBanner(event != null && event.isPermissionGranted());
    }

    private void updateGeolocBanner(boolean active) {
        TextView gpsLayout = getView()!=null?getView().findViewById(R.id.fragment_map_gps):null;
        if (gpsLayout != null) {
            boolean visibility = true;
            if (LocationUtils.INSTANCE.isLocationEnabled() && LocationUtils.INSTANCE.isLocationPermissionGranted()) {
                visibility = false;
            }
            //we force it because we don't need geoloc when Action zone is set
            User me = EntourageApplication.me(getActivity());
            if ((me != null) && !me.isPro() && (me.getAddress() != null)) {
                visibility = false;
            }

            gpsLayout.setText(LocationUtils.INSTANCE.isLocationEnabled()? getString(R.string.map_gps_no_permission):getString(R.string.map_gps_unavailable));
            gpsLayout.setVisibility(visibility? View.VISIBLE : View.GONE);

            View recenterButton = getView().findViewById(R.id.layout_feed_map_card_recenter_button);
            if(recenterButton!=null) recenterButton.setVisibility(visibility?View.INVISIBLE:View.VISIBLE);

        }

        if(getAdapter()!=null) {
            getAdapter().setGeolocStatusIcon(LocationUtils.INSTANCE.isLocationEnabled() && LocationUtils.INSTANCE.isLocationPermissionGranted());
        }

        if (map != null) {
            try {
                map.setMyLocationEnabled(LocationUtils.INSTANCE.isLocationEnabled());
            } catch (SecurityException ignored) {
            }
        }
    }

    abstract protected HeaderBaseAdapter getAdapter();

    protected void onFollowGeolocation() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_RECENTERCLICK);
        // Check if geolocation is enabled
        if (!LocationUtils.INSTANCE.isLocationEnabled() || !LocationUtils.INSTANCE.isLocationPermissionGranted()) {
            showAllowGeolocationDialog(GEOLOCATION_POPUP_RECENTER);
            return;
        }
        isFollowing = true;
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            centerMap(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        }
    }

    @Override
    public void onLocationUpdated(@NotNull LatLng location) {}

    @Override
    public void onLocationStatusUpdated(boolean active) {
        updateGeolocBanner(active);
    }
}
