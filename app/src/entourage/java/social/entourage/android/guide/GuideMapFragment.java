package social.entourage.android.guide;

import android.animation.ValueAnimator;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import social.entourage.android.Constants;
import social.entourage.android.MainActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.EntourageEvents;
import social.entourage.android.location.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Category;
import social.entourage.android.api.model.guide.Poi;
import social.entourage.android.api.model.tape.EntouragePoiRequest;
import social.entourage.android.api.tape.Events;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.base.EntourageLinkMovementMethod;
import social.entourage.android.base.HeaderBaseAdapter;
import social.entourage.android.guide.filter.GuideFilterFragment;
import social.entourage.android.guide.poi.ReadPoiFragment;
import social.entourage.android.location.LocationUtils;
import social.entourage.android.map.BaseMapFragment;
import social.entourage.android.tools.BusProvider;
import social.entourage.android.tools.Utils;
import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class GuideMapFragment extends BaseMapFragment {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String TAG = "social.entourage.android.fragment_guide";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    GuideMapPresenter presenter;

    private OnMapReadyCallback onMapReadyCallback;
    private Map<Long, Poi> poisMap;

    private Location previousEmptyListPopupLocation = null;
    private PoisAdapter poisAdapter;

    @BindView(R.id.fragment_guide_empty_list_popup)
    View emptyListPopup;

    @BindView(R.id.fragment_guide_empty_list_popup_text)
    TextView emptyListTextView;

    @BindView(R.id.fragment_guide_info_popup)
    View infoPopup;

    @BindView(R.id.fragment_map_display_toggle)
    FloatingActionButton guideDisplayToggle;

    @BindView(R.id.fragment_guide_main_layout)
    RelativeLayout layoutMain;

    @BindView(R.id.fragment_guide_pois_view)
    RecyclerView poisListView;

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public GuideMapFragment() {
        super(R.layout.fragment_guide_map);
        eventLongClick = EntourageEvents.EVENT_GUIDE_LONGPRESS;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (presenter == null) {
            setupComponent(EntourageApplication.get(getActivity()).getEntourageComponent());
        }
        poisMap = new TreeMap<>();
        initializeEmptyListPopup();
        initializeMap();
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
        EntourageEvents.logEvent(EntourageEvents.EVENT_OPEN_GUIDE_FROM_TAB);

        BusProvider.getInstance().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        boolean isLocationGranted = LocationUtils.INSTANCE.isLocationPermissionGranted();
        BusProvider.getInstance().post(new Events.OnLocationPermissionGranted(isLocationGranted));
    }

    @Override
    public void onStop() {
        super.onStop();

        BusProvider.getInstance().unregister(this);
    }

    @Override
    public boolean onBackPressed() {
        if (mapLongClickView.getVisibility() == View.VISIBLE) {
            mapLongClickView.setVisibility(View.GONE);
            //fabProposePOI.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    @OnClick(R.id.fragment_map_filter_button)
    void onShowFilter() {
        if (getFragmentManager() == null) {
            return;
        }
        GuideFilterFragment guideFilterFragment = new GuideFilterFragment();
        guideFilterFragment.show(getFragmentManager(), GuideFilterFragment.TAG);
    }

    @OnClick(R.id.fragment_map_display_toggle)
    void onDisplayToggle() {
        if (!isFullMapShown) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_MAP_VIEW);
        } else {
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
            if (mapClusterManager != null) {
                mapClusterManager.clearItems();
            }
            poisMap.clear();
            if (poisAdapter != null) {
                poisAdapter.removeAll();
            }
            presenter.updatePoisNearby(map);
        }
    }

    @Subscribe
    public void onPoiViewRequested(EntouragePoiRequest.OnPoiViewRequestedEvent event) {
        if (event == null || event.getPoi() == null) {
            return;
        }
        EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_POI_VIEW);
        showPoiDetails(event.getPoi());
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    void putPoiOnMap(List<Category> categories, List<Poi> pois) {
        if (getActivity() != null) {
            if (categories != null && mapClusterItemRenderer instanceof PoiRenderer) {
                ((PoiRenderer) mapClusterItemRenderer).setCategories(categories);
            }
            clearOldPois();
            if (pois != null && pois.size() > 0) {
                List<Poi> poiCollection = removeRedundantPois(pois);
                if (map !=null && mapClusterManager != null) {
                    mapClusterManager.addItems(poiCollection);
                    mapClusterManager.cluster();
                    hideEmptyListPopup();
                }
                if (poisAdapter != null) {
                    List<TimestampedObject> timestampedObjectList = new ArrayList<>(poiCollection);
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

    @Override
    protected void initializeMap() {
        originalMapLayoutHeight = (int) getResources().getDimension(R.dimen.solidarity_guide_map_height);
        if (onMapReadyCallback == null) {
            onMapReadyCallback = this::onMapReady;
        }
    }

    @Subscribe
    @Override
    public void onLocationPermissionGranted(Events.OnLocationPermissionGranted event) {
        super.onLocationPermissionGranted(event);
    }

    @Override
    protected DefaultClusterRenderer getRenderer() {
        return new PoiRenderer(getActivity(), map, mapClusterManager);
    }

    @OnClick(R.id.guide_longclick_button_poi_propose)
    void proposePOI() {
        // Close the overlays
        onBackPressed();
        // Open the link to propose a POI
        if (getActivity() instanceof MainActivity) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_PROPOSE_POI);
            ((MainActivity) getActivity()).showWebViewForLinkId(Constants.PROPOSE_POI_ID);
        }
    }

    @Override
    protected HeaderBaseAdapter getAdapter() {
        return poisAdapter;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private void clearOldPois() {
        poisMap.clear();
        if (map != null && mapClusterManager != null) {
            mapClusterManager.clearItems();
        }
        if (poisAdapter != null) {
            poisAdapter.removeAll();
        }
    }

    private void onMapReady(GoogleMap googleMap) {
        super.onMapReady(googleMap,
                new OnEntourageMarkerClickListener(),
                null
        );

        map.setOnCameraIdleListener(() -> {
            CameraPosition position = map.getCameraPosition();
            Location newLocation = EntourageLocation.cameraPositionToLocation(null, position);
            float newZoom = position.zoom;
            if (newZoom / previousCameraZoom >= BaseMapFragment.ZOOM_REDRAW_LIMIT || newLocation.distanceTo(previousCameraLocation) >= BaseMapFragment.REDRAW_LIMIT) {
                previousCameraZoom = newZoom;
                previousCameraLocation = newLocation;
                presenter.updatePoisNearby(map);
            }
        });

        if (presenter != null) {
            presenter.updatePoisNearby(map);
        }
    }

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
        if (readPoiFragment != null && getFragmentManager() != null) {
            readPoiFragment.show(getFragmentManager(), ReadPoiFragment.TAG);
        }
    }

    // ----------------------------------
    // EMPTY LIST POPUP
    // ----------------------------------

    private void initializeEmptyListPopup() {
        String proposePOIUrl = "";
        if (getActivity() != null && getActivity() instanceof MainActivity) {
            proposePOIUrl = ((MainActivity) getActivity()).getLink(Constants.PROPOSE_POI_ID);
        }
        emptyListTextView.setMovementMethod(EntourageLinkMovementMethod.getInstance());
        emptyListTextView.setText(Utils.fromHtml(getString(R.string.map_poi_empty_popup, proposePOIUrl)));
    }

    @OnClick(R.id.fragment_guide_empty_list_popup)
    void onEmptyListPopupClose() {
        AuthenticationController authenticationController = EntourageApplication.get(getContext()).getEntourageComponent().getAuthenticationController();
        //TODO add an "never display" button
        if (authenticationController != null) {
            authenticationController.setShowNoPOIsPopup(false);
        }
        hideEmptyListPopup();
    }

    private void showEmptyListPopup() {
        if(map!=null) {
            if (previousEmptyListPopupLocation == null) {
                previousEmptyListPopupLocation = EntourageLocation.cameraPositionToLocation(null, map.getCameraPosition());
            } else {
                // Show the popup only we moved from the last position we show it
                Location currentLocation = EntourageLocation.cameraPositionToLocation(null, map.getCameraPosition());
                if (previousEmptyListPopupLocation.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                    return;
                }
                previousEmptyListPopupLocation = currentLocation;
            }
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
    void onInfoPopupClose() {
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
    // FAB HANDLING
    // ----------------------------------

    @OnClick(R.id.button_poi_propose)
    void onPOIProposeClicked() {
        EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_PLUS_CLICK);
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
            poisAdapter.setOnFollowButtonClickListener(v -> onFollowGeolocation());
            poisListView.setAdapter(poisAdapter);
        }
    }

    private void togglePOIList() {
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

        guideDisplayToggle.setImageDrawable(AppCompatResources.getDrawable(getContext(),R.drawable.ic_list_white_24dp));

        ensureMapVisible();

        final int targetHeight = layoutMain.getMeasuredHeight();
        if (animated) {
            ValueAnimator anim = ValueAnimator.ofInt(originalMapLayoutHeight, targetHeight);
            anim.addUpdateListener(this::onAnimationUpdate);
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

        guideDisplayToggle.setImageDrawable(AppCompatResources.getDrawable(getContext(),R.drawable.ic_map_white_24dp));

        hideInfoPopup();
        hideEmptyListPopup();

        if (animated) {
            ValueAnimator anim = ValueAnimator.ofInt(layoutMain.getMeasuredHeight(), originalMapLayoutHeight);
            anim.addUpdateListener(this::onAnimationUpdate);
            anim.start();
        } else {
            poisAdapter.setMapHeight(originalMapLayoutHeight);
            poisListView.getLayoutManager().requestLayout();
        }
    }

    private void ensureMapVisible() {
        poisListView.scrollToPosition(0);
    }

    // ----------------------------------
    // Top Navigation bar
    // ----------------------------------

    private void initializeTopNavigationBar() {
        // Guide starts in full map mode, adjust the text accordingly
        if(getContext()== null) return;
        guideDisplayToggle.setImageDrawable(AppCompatResources.getDrawable(getContext(),R.drawable.ic_list_white_24dp));

        new MaterialTapTargetPrompt.Builder(getActivity())
                .setTarget(R.id.fragment_map_filter_button)
                .setPrimaryText("Filtrer les POI")
                .setSecondaryText("Clique ici pour voir les filtres actifs")
                .setPromptStateChangeListener((prompt, state) -> {
                    if (state == MaterialTapTargetPrompt.STATE_NON_FOCAL_PRESSED)
                    {
                        new MaterialTapTargetPrompt.Builder(getActivity())
                                .setTarget(R.id.button_poi_propose)
                                .setPrimaryText("Proposer un POI")
                                .setSecondaryText("Clique ici pour envoyer les infos")
                                .setBackgroundColour(ContextCompat.getColor(getContext(), R.color.accent))
                                .show();
                    }
                })
                .setBackgroundColour(ContextCompat.getColor(getContext(), R.color.accent))
                .show();
    }

    private void onAnimationUpdate(ValueAnimator valueAnimator) {
        int val = (Integer) valueAnimator.getAnimatedValue();
        poisAdapter.setMapHeight(val);
        if(poisListView.getLayoutManager()!=null) {
            poisListView.getLayoutManager().requestLayout();
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public class OnEntourageMarkerClickListener implements ClusterManager.OnClusterItemClickListener<Poi> {
        @Override
        public boolean onClusterItemClick(Poi poi) {
            EntourageEvents.logEvent(EntourageEvents.EVENT_GUIDE_POI_VIEW);
            showPoiDetails(poi);
            return true;
        }
    }
}
