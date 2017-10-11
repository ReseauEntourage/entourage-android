package social.entourage.android.guide;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import social.entourage.android.BackPressable;
import social.entourage.android.Constants;
import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Category;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.guide.filter.GuideFilterFragment;
import social.entourage.android.guide.poi.ReadPoiFragment;
import social.entourage.android.map.tour.TourService;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.Utils;

public class GuideMapEntourageFragment extends Fragment implements BackPressable {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.fragment_guide";

    public static final float ZOOM_REDRAW_LIMIT = 1.1f;
    public static final int REDRAW_LIMIT = 300;

    private static final int PERMISSIONS_REQUEST_LOCATION = 1;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    GuideMapPresenter presenter;

    TourService tourService;

    private View toReturn;

    private OnMapReadyCallback onMapReadyCallback;
    private GoogleMap map;
    private boolean isFullMapShown = true;
    private Location previousCameraLocation;
    private float previousCameraZoom = 1.0f;
    private ClusterManager<Poi> clusterManager;
    private Map<Long, Poi> poisMap;
    private PoiRenderer poiRenderer;

    private int originalMapLayoutHeight;

    private Location previousEmptyListPopupLocation = null;

    FloatingActionMenu mapOptionsMenu;

    @BindView(R.id.fragment_guide_empty_list_popup)
    View emptyListPopup;

    @BindView(R.id.fragment_guide_empty_list_popup_text)
    TextView emptyListTextView;

    @BindView(R.id.fragment_guide_info_popup)
    View infoPopup;

    @BindView(R.id.fragment_guide_longclick)
    RelativeLayout guideLongClickView;

    @BindView(R.id.map_longclick_buttons)
    RelativeLayout guideLongClickButtonsView;

    @BindView(R.id.fragment_guide_display_toggle)
    ToggleButton guideDisplayToggle;

    @BindView(R.id.fragment_guide_main_layout)
    RelativeLayout layoutMain;

    @BindView(R.id.fragment_guide_pois_view)
    RecyclerView poisListView;

    PoisAdapter poisAdapter;

    private LatLng longTapCoordinates;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_guide_map, container, false);
        }
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        mapOptionsMenu = ((DrawerActivity)getActivity()).mapOptionsMenu;
        poisMap = new TreeMap<>();
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
        initializeEmptyListPopup();

        originalMapLayoutHeight = (int) getResources().getDimension(R.dimen.solidarity_guide_map_height);

        if (onMapReadyCallback == null) {
            onMapReadyCallback = new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (map != null) return;
                    map = googleMap;
                    clusterManager = new ClusterManager<>(getActivity(), map);
                    poiRenderer = new PoiRenderer(getActivity(), map, clusterManager);
                    clusterManager.setRenderer(poiRenderer);
                    clusterManager.setOnClusterItemClickListener(new OnEntourageMarkerClickListener());
                    googleMap.setOnMarkerClickListener(clusterManager);
                    if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        map.setMyLocationEnabled(true);
                    }
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                    map.getUiSettings().setMapToolbarEnabled(false);
                    initializeMapZoom();
                    googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                        @Override
                        public void onCameraIdle() {
                            CameraPosition position = map.getCameraPosition();
                            EntourageLocation.getInstance().saveCurrentCameraPosition(position);
                            Location newLocation = EntourageLocation.cameraPositionToLocation(null, position);
                            float newZoom = position.zoom;
                            if (newZoom / previousCameraZoom >= ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= REDRAW_LIMIT) {
                                previousCameraZoom = newZoom;
                                previousCameraLocation = newLocation;
                                presenter.updatePoisNearby(map);
                            }
                        }
                    });

                    googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(final LatLng latLng) {
                            if (getActivity() != null) {
                                showLongClickOnMapOptions(latLng);
                            }
                        }
                    });

                    if (presenter != null) {
                        presenter.updatePoisNearby(map);
                    }
                }
            };
        }

        initializePOIList();
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

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        BusProvider.getInstance().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        if (mapOptionsMenu != null && mapOptionsMenu.isOpened()) {
            mapOptionsMenu.toggle(true);
            return true;
        }
        if (guideLongClickView.getVisibility() == View.VISIBLE) {
            guideLongClickView.setVisibility(View.GONE);
            return true;
        }
        // Switch to map view
        onHideClicked();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            for (int index = 0; index < permissions.length; index++) {
                if (permissions[index].equalsIgnoreCase(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                    //checkPermission();
                    BusProvider.getInstance().post(new Events.OnLocationPermissionGranted(false));
                } else {
                    BusProvider.getInstance().post(new Events.OnLocationPermissionGranted(true));
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @OnClick(R.id.fragment_guide_follow_button)
    void onFollowGeolocation() {
        // Check if geolocation is permitted
        if (!isGeolocationPermitted()) {
            showAllowGeolocationDialog();
            return;
        }
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null && map != null) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            map.moveCamera(cameraUpdate);
        }
    }

    @OnClick(R.id.fragment_guide_filter_button)
    void onShowFilter() {
        GuideFilterFragment guideFilterFragment = new GuideFilterFragment();
        guideFilterFragment.show(getFragmentManager(), GuideFilterFragment.TAG);
    }

    @OnClick(R.id.fragment_guide_hide_button)
    void onHideClicked() {
        EntourageEvents.logEvent(Constants.EVENT_GUIDE_X_CLICK);
        DrawerActivity activity = (DrawerActivity) getActivity();
        if (activity == null) return;
        activity.onPOILauncherClicked();
    }

    @OnClick(R.id.fragment_guide_display_toggle)
    public void onDisplayToggle() {
        if (guideDisplayToggle.isChecked()) {
            EntourageEvents.logEvent(Constants.EVENT_GUIDE_MAP_VIEW);
        }
        else {
            EntourageEvents.logEvent(Constants.EVENT_GUIDE_LIST_VIEW);
        }
        togglePOIList();
    }

    // ----------------------------------
    // BUS EVENTS
    // ----------------------------------

    @Subscribe
    public void onSolidarityGuideFilterChanged(Events.OnSolidarityGuideFilterChanged event) {
        if (presenter != null) {
            clusterManager.clearItems();
            poisMap.clear();
            poisAdapter.removeAll();
            presenter.updatePoisNearby(map);
        }
    }

    @Subscribe
    public void onPoiViewRequested(Events.OnPoiViewRequestedEvent event) {
        if (event == null || event.getPoi() == null) return;
        EntourageEvents.logEvent(Constants.EVENT_GUIDE_POI_VIEW);
        showPoiDetails(event.getPoi());
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void putPoiOnMap(List<Category> categories, List<Poi> pois) {
        if (getActivity() != null) {
            if (categories != null) {
                poiRenderer.setCategories(categories);
            }
            clearOldPois();
            if (pois != null && pois.size() > 0) {
                List<Poi> poiCollection = removeRedundantPois(pois);
                if (map != null) {
                    clusterManager.addItems(poiCollection);
                    clusterManager.cluster();
                    hideEmptyListPopup();
                    showInfoPopup();
                }
                if (poisAdapter != null) {
                    List<TimestampedObject> timestampedObjectList = new ArrayList<>();
                    timestampedObjectList.addAll(poiCollection);
                    int previousPoiCount = poisAdapter.getDataItemCount();
                    poisAdapter.addItems(timestampedObjectList);
                    if (previousPoiCount == 0) {
                        poisListView.scrollToPosition(0);
                    }
                }
            } else {
                showEmptyListPopup();
                if (poisAdapter != null && poisAdapter.getDataItemCount() == 0) {
                    hidePOIList(false);
                }
            }
        }
    }

    private void clearOldPois() {
        poisMap.clear();
        if (map != null && clusterManager != null) {
            clusterManager.clearItems();
        }
        if (poisAdapter != null) {
            poisAdapter.removeAll();
        }
    }

    public void initializeMapZoom() {
        centerMap(EntourageLocation.getInstance().getLastCameraPosition());
    }

    private void centerMap(CameraPosition cameraPosition) {
        if(map != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            saveCameraPosition();
        }
    }

    public void saveCameraPosition() {
        if(map != null) {
            EntourageLocation.getInstance().saveLastCameraPosition(map.getCameraPosition());
        }
    }

    //@OnClick(R.id.guide_longclick_button_poi_propose)
    public void proposePOI() {
        // Close the overlays
        onBackPressed();
        // Open the link to propose a POI
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.POI_PROPOSE_URL));
        try {
            startActivity(browserIntent);
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(getActivity(), R.string.no_browser_error, Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private List<Poi> removeRedundantPois(List<Poi> pois) {
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

    private void showPoiDetails(Poi poi) {
        ReadPoiFragment readPoiFragment = ReadPoiFragment.newInstance(poi);
        readPoiFragment.show(getFragmentManager(), ReadPoiFragment.TAG);
    }

    // ----------------------------------
    // GEOLOCATION PERMISSIONS HANDLING
    // ----------------------------------

    private boolean isGeolocationPermitted() {
        return (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void showAllowGeolocationDialog() {
        @StringRes int messagedId = R.string.map_error_geolocation_disabled_recenter;
        new AlertDialog.Builder(getActivity())
                .setMessage(messagedId)
                .setPositiveButton(R.string.activate, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EntourageEvents.logEvent(Constants.EVENT_FEED_ACTIVATE_GEOLOC_RECENTER);

                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
                        } else {
                            // User selected "Never ask again", so show the settings page
                            displayGeolocationPreferences();
                        }
                    }
                })
                .setNegativeButton(R.string.map_permission_refuse, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int i) {
                    }
                })
                .show();
    }

    private void displayGeolocationPreferences() {
        if (getActivity() != null) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------

    private void initializeEmptyListPopup() {
        emptyListTextView.setMovementMethod(LinkMovementMethod.getInstance());
        emptyListTextView.setText(Utils.fromHtml(getString(R.string.map_poi_empty_popup, Constants.POI_PROPOSE_URL)));
    }

    @OnClick(R.id.fragment_guide_empty_list_popup)
    protected void onEmptyListPopupClose() {
        AuthenticationController authenticationController = EntourageApplication.get(getContext()).getEntourageComponent().getAuthenticationController();
        if (authenticationController != null) {
            authenticationController.setShowNoPOIsPopup(false);
        }
        hideEmptyListPopup();
    }

    private void showEmptyListPopup() {
        if (previousEmptyListPopupLocation == null) {
            previousEmptyListPopupLocation = EntourageLocation.getInstance().getCurrentLocation();
        } else {
            // Show the popup only we moved from the last position we show it
            Location currentLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getCurrentCameraPosition());
            if (previousEmptyListPopupLocation.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                return;
            }
            previousEmptyListPopupLocation = currentLocation;
        }
        AuthenticationController authenticationController = EntourageApplication.get(getContext()).getEntourageComponent().getAuthenticationController();
        if (authenticationController != null && !authenticationController.isShowNoPOIsPopup()) {
            return;
        }
        emptyListPopup.setVisibility(View.VISIBLE);
    }

    private void hideEmptyListPopup() {
        emptyListPopup.setVisibility(View.GONE);
    }

    // ----------------------------------
    // INFO POPUP
    // ----------------------------------

    @OnClick(R.id.fragment_guide_info_popup_close)
    protected void onInfoPopupClose() {
        AuthenticationController authenticationController = EntourageApplication.get(getContext()).getEntourageComponent().getAuthenticationController();
        if (authenticationController != null) {
            authenticationController.setShowInfoPOIsPopup(false);
        }
        hideInfoPopup();
    }

    private void showInfoPopup() {
        AuthenticationController authenticationController = EntourageApplication.get(getContext()).getEntourageComponent().getAuthenticationController();
        if (authenticationController != null && !authenticationController.isShowInfoPOIsPopup()) {
            return;
        }
        infoPopup.setVisibility(View.VISIBLE);
    }

    private void hideInfoPopup() {
        infoPopup.setVisibility(View.GONE);
    }

    // ----------------------------------
    // LONG PRESS OVERLAY
    // ----------------------------------

    private void showLongClickOnMapOptions(LatLng latLng) {
        //save the tap coordinates
        longTapCoordinates = latLng;
        //for public user, start the create entourage funnel directly
        User me = EntourageApplication.me(getActivity());
        boolean isPro = (me != null && me.isPro());
        if (!isPro) {
            //displayEntourageDisclaimer();
            return;
        }
        //get the click point
        Point clickPoint = map.getProjection().toScreenLocation(latLng);
        //update the visible buttons
        boolean isTourRunning = tourService != null && tourService.isRunning();
        guideLongClickButtonsView.findViewById(R.id.map_longclick_button_start_tour_launcher).setVisibility(isTourRunning ? View.INVISIBLE : (isPro ? View.VISIBLE : View.GONE));
        guideLongClickButtonsView.findViewById(R.id.map_longclick_button_create_encounter).setVisibility(isTourRunning ? View.VISIBLE : View.GONE);
        guideLongClickButtonsView.requestLayout();
        //adjust the buttons holder layout
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenSize = new Point();
        display.getSize(screenSize);
        guideLongClickButtonsView.measure(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        int bW = guideLongClickButtonsView.getMeasuredWidth();
        int bH = guideLongClickButtonsView.getMeasuredHeight();
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) guideLongClickButtonsView.getLayoutParams();
        int marginLeft = clickPoint.x - bW/2;
        if (marginLeft + bW > screenSize.x) {
            marginLeft -= bW/2;
        }
        if (marginLeft < 0) marginLeft = 0;
        int marginTop = clickPoint.y - bH /2;
        if (marginTop < 0) marginTop = clickPoint.y;
        lp.setMargins(marginLeft, marginTop, 0, 0);
        guideLongClickButtonsView.setLayoutParams(lp);
        //show the view
        guideLongClickView.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.fragment_guide_longclick)
    protected void hideLongClickView() {
        onBackPressed();
    }

    // ----------------------------------
    // POI List
    // ----------------------------------

    private void initializePOIList() {
        if (poisAdapter == null) {
            poisListView.setLayoutManager(new LinearLayoutManager(getContext()));
            poisAdapter = new PoisAdapter();
            poisAdapter.setOnMapReadyCallback(onMapReadyCallback);
            poisListView.setAdapter(poisAdapter);
        }
    }

    public void togglePOIList() {
        if (poisListView == null) {
            return;
        }
        if (isFullMapShown) {
            showPOIList(true);
        } else {
            hidePOIList(true);
        }
    }

    private void hidePOIList(boolean animated) {
        if (isFullMapShown) {
            return;
        }
        isFullMapShown = true;

        guideDisplayToggle.setChecked(true);

        ensureMapVisible();

        final int targetHeight = layoutMain.getMeasuredHeight();
        if (animated) {
            ValueAnimator anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    poisAdapter.setMapHeight(val);
                    poisListView.getLayoutManager().requestLayout();
                }

            });
            anim.start();
        } else {
            poisAdapter.setMapHeight(targetHeight);
            poisListView.getLayoutManager().requestLayout();
        }

    }

    private void showPOIList(boolean animated) {
        if (layoutMain == null || poisListView == null || guideDisplayToggle == null) {
            return;
        }

        if (!isFullMapShown) {
            return;
        }
        isFullMapShown = false;

        guideDisplayToggle.setChecked(false);

        hideEmptyListPopup();

        if (animated) {
            ValueAnimator anim = ValueAnimator.ofInt(layoutMain.getMeasuredHeight(), originalMapLayoutHeight);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    int val = (Integer) valueAnimator.getAnimatedValue();
                    poisAdapter.setMapHeight(val);
                    poisListView.getLayoutManager().requestLayout();
                }

            });
            anim.start();
        } else {
            poisAdapter.setMapHeight(originalMapLayoutHeight);
            poisListView.getLayoutManager().requestLayout();
        }
    }

    public void ensureMapVisible() {
        poisListView.scrollToPosition(0);
    }

    // ----------------------------------
    // INNER CLASS
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements ClusterManager.OnClusterItemClickListener<Poi> {
        @Override
        public boolean onClusterItemClick(Poi poi) {
            EntourageEvents.logEvent(Constants.EVENT_GUIDE_POI_VIEW);
            saveCameraPosition();
            showPoiDetails(poi);
            return true;
        }
    }
}
