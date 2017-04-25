package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MapRequest {

    @GET("pois.json")
    Call<MapResponse> retrievePoisNearby(@Query("latitude") double latitude,
                                         @Query("longitude") double longitude,
                                         @Query("distance") double distance,
                                         @Query("category_ids") String categoryIDs);
}

