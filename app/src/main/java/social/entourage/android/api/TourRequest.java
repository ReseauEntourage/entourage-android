package social.entourage.android.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import social.entourage.android.api.model.TourWrapper;
import social.entourage.android.api.model.map.TourPoint;

public interface TourRequest {

    @POST("/tours.json")
    void tour( @Body TourWrapper tourWrapper, Callback<TourWrapper> callback);

    @PUT("/tours/{id}.json")
    void closeTour(@Path("id") long tourId, @Body TourWrapper tourWrapper, Callback<TourWrapper> callback);

    @GET("/tours/{id}.json")
    void retrieveTourById(@Path("id") long tourId, Callback<TourWrapper> callback);

    //@POST("/tours/{tour_id}/tour_points.json")
    //void tourPoint( @Path("tour_id") long tourId, @Body TourPoint point, Callback<TourWrapper> callback);

    @POST("/tours/{tour_id}/tour_points.json")
    void tourPoints( @Path("tour_id") long tourId, @Body List<TourPoint> points, Callback<TourWrapper> callback);

    @GET("/tours/blabla.json")
    void retrieveCloseTours();
}