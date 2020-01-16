package social.entourage.android.guide;

import android.location.Location;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.VisibleRegion;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.api.PoiRequest;
import social.entourage.android.api.PoiResponse;
import social.entourage.android.guide.filter.GuideFilter;
import timber.log.Timber;

/**
 * Presenter controlling the GuideMapFragment
 * @see GuideMapFragment
 */
public class GuideMapPresenter {

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final GuideMapFragment fragment;
    private final PoiRequest poiRequest;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    @Inject
    public GuideMapPresenter(final GuideMapFragment fragment, final PoiRequest poiRequest) {
        this.fragment = fragment;
        this.poiRequest = poiRequest;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void start() {
        //updatePoisNearby();
    }

    public void updatePoisNearby(GoogleMap map) {
        float distance;
        if (map != null) {
            VisibleRegion region = map.getProjection().getVisibleRegion();
            float[] result = {0};
            Location.distanceBetween(region.farLeft.latitude, region.farLeft.longitude, region.nearLeft.latitude, region.nearLeft.longitude, result);
            distance = result[0] / 1000.0f;
            retrievePoisNearby(map.getCameraPosition(), distance);
        } else {
            Timber.w("no map available for updating Guide");
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void retrievePoisNearby(CameraPosition currentPosition, float distance) {
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            distance = Math.max(1, distance);
            GuideFilter filter = GuideFilter.getInstance();
            Call<PoiResponse> call = poiRequest.retrievePoisNearby(location.latitude, location.longitude, distance, filter.getRequestedCategories());
            call.enqueue(new Callback<PoiResponse>() {
                @Override
                public void onResponse(@NonNull Call<PoiResponse> call, @NonNull Response<PoiResponse> response) {
                    if (response.isSuccessful()) {
                        fragment.putPoiOnMap(response.body().getCategories(), response.body().getPois());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<PoiResponse> call, @NonNull Throwable t) {
                    Timber.e(t, "Impossible to retrieve POIs");
                }
            });
        }
    }
}
