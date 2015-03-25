package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by RPR on 25/03/15.
 */
public interface MapService {

    @GET("/map.json")
    void map(  @Query("token") String token,
               @Query("limit") int limit,
               @Query("distance") int distance,
               @Query("latitude") double latitude,
               @Query("longitude") double longitude,
               Callback<MapResponse> callback);
}

