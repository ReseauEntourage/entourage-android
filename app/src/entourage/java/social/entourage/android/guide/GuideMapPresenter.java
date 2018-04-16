package social.entourage.android.guide;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.MapResponse;
import social.entourage.android.guide.filter.GuideFilter;

/**
 * Presenter controlling the GuideMapEntourageFragment
 * @see GuideMapEntourageFragment
 */
public class GuideMapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final GuideMapEntourageFragment fragment;
    private final MapRequest mapRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public GuideMapPresenter(final GuideMapEntourageFragment fragment, final MapRequest mapRequest) {
        this.fragment = fragment;
        this.mapRequest = mapRequest;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void start() {
        fragment.initializeMapZoom();
        //updatePoisNearby();
    }

    public void updatePoisNearby(GoogleMap map) {
        float distance = 0;
        if (map != null) {
            VisibleRegion region = map.getProjection().getVisibleRegion();
            float[] result = {0};
            Location.distanceBetween(region.farLeft.latitude, region.farLeft.longitude, region.nearLeft.latitude, region.nearLeft.longitude, result);
            distance = result[0] / 1000.0f;
        }
        retrievePoisNearby(distance);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void retrievePoisNearby(float distance) {
        CameraPosition currentPosition = EntourageLocation.getInstance().getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            distance = Math.max(1, distance);
            GuideFilter filter = GuideFilter.getInstance();
            Call<MapResponse> call = mapRequest.retrievePoisNearby(location.latitude, location.longitude, distance, filter.getRequestedCategories());
            call.enqueue(new Callback<MapResponse>() {
                @Override
                public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                    if (response.isSuccessful()) {
                        fragment.putPoiOnMap(response.body().getCategories(), response.body().getPois());
                    }
                }

                @Override
                public void onFailure(Call<MapResponse> call, Throwable t) {
                    Log.d("GuideMapEntourageFrag", "Impossible to retrieve POIs", t);
                }
            });
        }
    }
}
