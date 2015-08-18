package social.entourage.android.guide;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.MapRequest;
import social.entourage.android.api.MapResponse;
import social.entourage.android.Constants;

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

    private boolean isStarted = false;

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
        isStarted = true;
        fragment.initializeMapZoom();
        retrieveMapObjects(EntourageLocation.getInstance().getLastCameraPosition().target);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void retrieveMapObjects(LatLng latLng) {
        if(isStarted) {
            mapRequest.map(Constants.TOKEN, 1000, 10, latLng.latitude, latLng.longitude, new Callback<MapResponse>() {
                @Override
                public void success(MapResponse mapResponse, Response response) {
                    loadObjectsOnMap(mapResponse);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("GuideMapActivity", "Impossible to retrieve map objects", error);
                }
            });
        }
    }

    private void loadObjectsOnMap(MapResponse mapResponse) {
        fragment.clearMap();
        fragment.putPoiOnMap(mapResponse.getPois());
    }
}
