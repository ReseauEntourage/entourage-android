package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import social.entourage.android.api.model.TourPointWrapper;
import social.entourage.android.api.model.TourWrapper;
import social.entourage.android.api.model.ToursWrapper;

public interface TourRequest {

    @POST("/tours.json")
    void tour( @Body TourWrapper tourWrapper, Callback<TourWrapper> callback);

    @POST("/tours/{tour_id}/tour_points.json")
    void tourPoints( @Path("tour_id") long tourId, @Body TourPointWrapper points, Callback<TourWrapper> callback);

    @PUT("/tours/{id}.json")
    void closeTour(@Path("id") long tourId, @Body TourWrapper tourWrapper, Callback<TourWrapper> callback);

    @GET("/tours.json")
    void retrieveToursNearby(@Query("limit") int limit,
                             @Query("type") String type,
                             @Query("vehicle_type") String vehicleType,
                             @Query("latitude") double latitude,
                             @Query("longitude") double longitude,
                             @Query("distance") double distance,
                             Callback<ToursWrapper> callback);
}