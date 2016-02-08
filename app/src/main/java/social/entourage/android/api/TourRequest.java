package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;

public interface TourRequest {

    @POST("/tours.json")
    void tour( @Body Tour.TourWrapper tourWrapper, Callback<Tour.TourWrapper> callback);

    @POST("/tours/{tour_id}/tour_points.json")
    void tourPoints( @Path("tour_id") long tourId, @Body TourPoint.TourPointWrapper points, Callback<Tour.TourWrapper> callback);

    @PUT("/tours/{id}.json")
    void closeTour(@Path("id") long tourId, @Body Tour.TourWrapper tourWrapper, Callback<Tour.TourWrapper> callback);

    @GET("/tours.json")
    void retrieveToursNearby(@Query("limit") int limit,
                             @Query("type") String type,
                             @Query("vehicle_type") String vehicleType,
                             @Query("latitude") double latitude,
                             @Query("longitude") double longitude,
                             @Query("distance") double distance,
                             Callback<Tour.ToursWrapper> callback);

    @GET("/users/{user_id}/tours.json")
    void retrieveToursByUserId(@Path("user_id") int userId,
                               @Query("page") int page,
                               @Query("per") int per,
                               Callback<Tour.ToursWrapper> callback);
}