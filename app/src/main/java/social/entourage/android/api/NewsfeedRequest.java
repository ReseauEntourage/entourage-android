package social.entourage.android.api;

import java.util.Date;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import social.entourage.android.api.model.EntourageDate;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.map.Entourage;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public interface NewsfeedRequest {

    @GET("feeds")
    Call<Newsfeed.NewsfeedWrapper> retrieveFeed(
            @Query("before") EntourageDate before,
            @Query("longitude") double longitude,
            @Query("latitude") double latitude,
            @Query("tour_types") String tourTypes,
            @Query("show_tours") boolean showTours,
            @Query("show_my_entourages_only") boolean onlyMyEntourages,
            @Query("entourage_types") String entourageTypes,
            @Query("time_range") int timeFrame
    );

    @GET("feeds")
    Call<Newsfeed.NewsfeedWrapper> retrieveFeedByPage(
            @Query("page") int page,
            @Query("per") int per,
            @Query("longitude") double longitude,
            @Query("latitude") double latitude,
            @Query("tour_types") String tourTypes,
            @Query("show_tours") boolean showTours,
            @Query("show_my_entourages_only") boolean onlyMyEntourages,
            @Query("entourage_types") String entourageTypes,
            @Query("time_range") int timeFrame
    );

    @GET("myfeeds")
    Call<Newsfeed.NewsfeedWrapper> retrieveMyFeeds(
            @Query("page") int page,
            @Query("per") int per,
            @Query("status") String status
    );
}
