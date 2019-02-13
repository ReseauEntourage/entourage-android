package social.entourage.android.guide;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import social.entourage.android.api.model.map.Category;
import social.entourage.android.api.model.map.Poi;
import social.entourage.android.api.model.tape.EntouragePoiRequest;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.base.EntourageLinkMovementMethod;
import social.entourage.android.guide.filter.GuideFilterFragment;
import social.entourage.android.guide.poi.ReadPoiFragment;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.Utils;

import static social.entourage.android.EntourageEvents.EVENT_OPEN_GUIDE_FROM_TAB;

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

    @BindView(R.id.map_fab_menu)
    FloatingActionMenu guideOptionsMenu;

    @BindView(R.id.fragment_guide_empty_list_popup)
    View emptyListPopup;

    @BindView(R.id.fragment_guide_empty_list_popup_text)
    TextView emptyListTextView;

    @BindView(R.id.fragment_guide_info_popup)
    View infoPopup;

    @BindView(R.id.fragment_guide_longclick)
    RelativeLayout guideLongClickView;

    @BindView(R.id.guide_longclick_buttons)
    RelativeLayout guideLongClickButtonsView;

    @BindView(R.id.fragment_map_display_toggle)
    Button guideDisplayToggle;

    @BindView(R.id.fragment_guide_main_layout)
    RelativeLayout layoutMain;

    @BindView(R.id.fragment_guide_pois_view)
    RecyclerView poisListView;

    PoisAdapter poisAdapter;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (toReturn == null) {
            toReturn = inflater.inflate(R.layout.fragment_guide_map, container, false);
        }
        ButterKnife.bind(this, toReturn);
        return toReturn;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());

        poisMap = new TreeMap<>();
        previousCameraLocation = EntourageLocation.cameraPositionToLocation(null, EntourageLocation.getInstance().getLastCameraPosition());
        initializeEmptyListPopup();
        initializeMap();
        initializeFloatingMenu();
        initializePOIList();
        initializeTopNavigationBar();
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerGuideMapComponent.builder()
                .entourageComponent(entourageComponent)
                .guideMapModule(new GuideMapModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (presenter != null) {
            presenter.start();
            if (map != null) {
                presenter.updatePoisNearby(map);
            }
        }
        showInfoPopup();
        EntourageEvents.logEvent(EVENT_OPEN_GUIDE_FROM_TAB);

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        BusProvider.getInstance().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        if (guideOptionsMenu != null && guideOptionsMenu.isOpened()) {
            guideOptionsMenu.toggle(true);
            return true;
        }
        if (guideLongClickView.getVisibility() == View.VISIBLE) {
            guideLongClickView.setVisibility(View.GONE);
            guideOptionsMenu.setVisibility(View.VISIBLE);
            return true;
        }
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

    @OnClick(R.id.fragment_map_filter_button)
    void onShowFilter() {
        GuideFilterFragment guideFilterFragment = new GuideFilterFragment();
        guideFilterFragment.show(getFragmentManager(), GuideFilterFragment.TAG);
    }

    @OnClick(R.id.fragment_map_display_toggle)
    public void onDisplayToggle() {
        if (!isFullMapShown) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_MAP_VIEW);
        }
        else {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_LIST_VIEW);
        }
        togglePOIList();
    }

    // ----------------------------------
    // BUS EVENTS
    // ----------------------------------

    @Subscribe
    public void onSolidarityGuideFilterChanged(Events.OnSolidarityGuideFilterChanged event) {
        if (presenter != null) {
            if(clusterManager!=null) {
                clusterManager.clearItems();
            }
            poisMap.clear();
            if(poisAdapter!=null) {
                poisAdapter.removeAll();
            }
            presenter.updatePoisNearby(map);
        }
    }

    @Subscribe
    public void onPoiViewRequested(EntouragePoiRequest.OnPoiViewRequestedEvent event) {
        if (event == null || event.getPoi() == null) return;
        EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_POI_VIEW);
        showPoiDetails(event.getPoi());
    }

    @Subscribe
    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        if (event != null && event.isPermissionGranted()&& map != null) {
            try {
                map.setMyLocationEnabled(true);
            } catch (SecurityException ignored) {
            }
        }
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
                }
                if (poisAdapter != null) {
                    List<TimestampedObject> timestampedObjectList = new ArrayList<TimestampedObject>(poiCollection);
                    int previousPoiCount = poisAdapter.getDataItemCount();
                    poisAdapter.addItems(timestampedObjectList);
                    if (previousPoiCount == 0) {
                        poisListView.scrollToPosition(0);
                    }
                }
            } else {
                if (!isInfoPopupVisible()) {
                    showEmptyListPopup();
                }
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

    private void initializeFloatingMenu() {
        guideOptionsMenu.setClosedOnTouchOutside(true);
        guideOptionsMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(final boolean opened) {
                if (opened) {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_PLUS_CLICK);
                    proposePOI();
                }
            }
        });
    }

    private void initializeMap() {
        originalMapLayoutHeight = (int) getResources().getDimension(R.dimen.solidarity_guide_map_height);

        if (onMapReadyCallback == null) {
            onMapReadyCallback = new OnMapReadyCallback() {
                @Override
                public void onMapReady(final GoogleMap googleMap) {
                    if (map != null) return;
                    if (getActivity()== null) return;
                    map = googleMap;
                    clusterManager = new ClusterManager<>(getActivity(), map);
                    poiRenderer = new PoiRenderer(getActivity(), map, clusterManager);
                    clusterManager.setRenderer(poiRenderer);
                    clusterManager.setOnClusterItemClickListener(new OnEntourageMarkerClickListener());
                    map.setOnMarkerClickListener(clusterManager);
                    if ((PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                            || (PermissionChecker.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                        map.setMyLocationEnabled(true);
                    }
                    map.getUiSettings().setMyLocationButtonEnabled(false);
                    map.getUiSettings().setMapToolbarEnabled(false);
                    initializeMapZoom();
                    map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
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

                    map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                        @Override
                        public void onMapLongClick(final LatLng latLng) {
                            if (getActivity() != null) {
                                EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_LONGPRESS);
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

    @OnClick(R.id.guide_longclick_button_poi_propose)
    public void proposePOI() {
        // Close the overlays
        onBackPressed();
        // Open the link to propose a POI
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_PROPOSE_POI);
            ((DrawerActivity)getActivity()).showWebViewForLinkId(Constants.PROPOSE_POI_ID);
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
                        EntourageEvents.logEvent(EntourageEvents.EVENT_FEED_ACTIVATE_GEOLOC_RECENTER);

                        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_LOCATION);
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
        String proposePOIUrl = "";
        if (getActivity() != null && getActivity() instanceof DrawerActivity) {
            proposePOIUrl = ((DrawerActivity)getActivity()).getLink(Constants.PROPOSE_POI_ID);
        }
        emptyListTextView.setMovementMethod(EntourageLinkMovementMethod.getInstance());
        emptyListTextView.setText(Utils.fromHtml(getString(R.string.map_poi_empty_popup, proposePOIUrl)));
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

    @OnClick({R.id.fragment_guide_info_popup_close, R.id.fragment_guide_info_popup})
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

    private boolean isInfoPopupVisible() {
        return infoPopup.getVisibility() == View.VISIBLE;
    }

    // ----------------------------------
    // LONG PRESS OVERLAY
    // ----------------------------------

    private void showLongClickOnMapOptions(LatLng latLng) {
        //hide the FAB menu
        guideOptionsMenu.setVisibility(View.GONE);
        //get the click point
        Point clickPoint = map.getProjection().toScreenLocation(latLng);
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
    // FAB HANDLING
    // ----------------------------------

    @OnClick(R.id.button_poi_propose)
    protected void onPOIProposeClicked() {
        proposePOI();
    }

    // ----------------------------------
    // POI List
    // ----------------------------------

    private void initializePOIList() {
        if (poisAdapter == null) {
            poisListView.setLayoutManager(new LinearLayoutManager(getContext()));
            poisAdapter = new PoisAdapter();
            poisAdapter.setOnMapReadyCallback(onMapReadyCallback);
            poisAdapter.setOnFollowButtonClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    GuideMapEntourageFragment.this.onFollowGeolocation();
                }
            });
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

        guideDisplayToggle.setText(R.string.map_top_navigation_full_map);

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

        guideDisplayToggle.setText(R.string.map_top_navigation_list);

        hideInfoPopup();
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
    // Top Navigation bar
    // ----------------------------------

    private void initializeTopNavigationBar() {
        // Guide starts in full map mode, adjust the text accordingly
        guideDisplayToggle.setText(R.string.map_top_navigation_full_map);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements ClusterManager.OnClusterItemClickListener<Poi> {
        @Override
        public boolean onClusterItemClick(Poi poi) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_POI_VIEW);
            saveCameraPosition();
            showPoiDetails(poi);
            return true;
        }
    }
}
