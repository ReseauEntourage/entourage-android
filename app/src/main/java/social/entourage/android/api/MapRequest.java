package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

public interface MapRequest {

    @GET("/pois.json")
    void retrievePoisNearby(@Query("latitude") double latitude,
                            @Query("longitude") double longitude,
                            @Query("distance") double distance,
                            Callback<MapResponse> callback);
}

