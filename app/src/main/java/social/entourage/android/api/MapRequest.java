package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

public interface MapRequest {

    @Headers({"Accept: application/json"})
    @GET("/map.json")
    void map(  @Query("token") String token,
               @Query("limit") int limit,
               @Query("distance") double distance,
               @Query("latitude") double latitude,
               @Query("longitude") double longitude,
               Callback<MapResponse> callback);
}

