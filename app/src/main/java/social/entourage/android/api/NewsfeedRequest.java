package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import social.entourage.android.api.model.EntourageDate;
import social.entourage.android.api.model.NewsfeedItem;

/**
 * Created by mihaiionescu on 05/05/16.
 */
public interface NewsfeedRequest {

    @GET("feeds")
    Call<NewsfeedItem.NewsfeedItemWrapper> retrieveFeed(
            @Query("before") EntourageDate before,
            @Query("longitude") double longitude,
            @Query("latitude") double latitude,
            @Query("distance") int distance,
            @Query("per") int itemsPerPage,
            @Query("types") String types,
            @Query("show_my_entourages_only") boolean onlyMyEntourages,
            @Query("time_range") int timeFrame,
            @Query("show_my_partner_only") boolean onlyMyPartnerEntourages,
            @Query("announcements") String announcementsVersion,
            @Query("show_past_events") boolean showPastEvents
    );

    @GET("feeds")
    Call<NewsfeedItem.NewsfeedItemWrapper> retrieveFeedByPage(
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
    Call<NewsfeedItem.NewsfeedItemWrapper> retrieveMyFeeds(
            @Query("page") int page,
            @Query("per") int per,
            @Query("entourage_types") String entourageTypes,
            @Query("tour_types") String tourTypes,
            @Query("status") String status,
            @Query("created_by_me") boolean createdByMe,
            @Query("show_my_partner_only") boolean showMyPartnerOnly,
            @Query("accepted_invitation") boolean acceptedInvitation
    );

    @GET("feeds/outings")
    Call<NewsfeedItem.NewsfeedItemWrapper> retrieveOutings(
            @Query("longitude") double longitude,
            @Query("latitude") double latitude,
            @Query("starting_after") String startingAfterOutingUUID
    );
}
