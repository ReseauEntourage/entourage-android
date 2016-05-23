package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import social.entourage.android.api.model.Newsfeed;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public interface NewsfeedRequest {

    @GET("feeds")
    Call<Newsfeed.NewsfeedWrapper> retrieveFeed(
            @Query("page") int page,
            @Query("per") int per,
            @Query("longitude") double longitude,
            @Query("latitude") double latitude,
            @Query("tour_types") String tourTypes,
            @Query("show_tours") boolean showTours,
            @Query("show_my_entourages_only") boolean onlyMyEntourages,
            @Query("entourage_types") String entourageTypes,
            @Query("time_frame") int timeFrame
    );
}
