package social.entourage.android.api;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;
import social.entourage.android.api.model.TourResponse;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;

/**
 * Created by NTE on 09/07/15.
 */
public interface TourRequest {

    @POST("/tours.json")
    void tour( @Body Tour tour, Callback<TourResponse> callback);

    @POST("/tours/{tour_id}/tour_points.json")
    void tourPoint( @Path("tour_id") long tourId, @Body TourPoint point, Callback<TourResponse> callback);

    //@POST("/tours/{tour_id}/encounters")
}
