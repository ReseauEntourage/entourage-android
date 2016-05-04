package social.entourage.android.guide;

import android.util.Log;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.MapResponse;

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

    public void updatePoisNearby() {
        retrievePoisNearby();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void retrievePoisNearby() {
        CameraPosition currentPosition = EntourageLocation.getInstance().getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            float zoom = currentPosition.zoom;
            float distance = 40000f / (float) Math.pow(2f, zoom) / 2.5f;
            distance = Math.max(1, distance);
            Call<MapResponse> call = mapRequest.retrievePoisNearby(location.latitude, location.longitude, distance);
            call.enqueue(new Callback<MapResponse>() {
                @Override
                public void onResponse(Call<MapResponse> call, Response<MapResponse> response) {
                    if (response.isSuccess()) {
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
