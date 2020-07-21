package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import social.entourage.android.api.model.EntourageRequestDate
import social.entourage.android.api.model.feed.NewsfeedItem

/**
 * Created by mihaiionescu on 05/05/16.
 */
class NewsfeedItemResponse(@field:SerializedName("feeds") var newsfeedItems: List<NewsfeedItem>)

interface NewsfeedRequest {
    @GET("feeds")
    fun retrieveFeed(
            @Query("before") before: EntourageRequestDate?,
            @Query("longitude") longitude: Double,
            @Query("latitude") latitude: Double,
            @Query("distance") distance: Int,
            @Query("per") itemsPerPage: Int,
            @Query("types") types: String?,
            @Query("show_my_entourages_only") onlyMyEntourages: Boolean,
            @Query("time_range") timeFrame: Int,
            @Query("show_my_partner_only") onlyMyPartnerEntourages: Boolean,
            @Query("announcements") announcementsVersion: String?,
            @Query("show_past_events") showPastEvents: Boolean,
            @Query("partners_only") isPartnersOnly: Boolean
    ): Call<NewsfeedItemResponse>

    @GET("feeds")
    fun retrieveFeedByPage(
            @Query("page") page: Int,
            @Query("per") per: Int,
            @Query("longitude") longitude: Double,
            @Query("latitude") latitude: Double,
            @Query("tour_types") tourTypes: String?,
            @Query("show_tours") showTours: Boolean,
            @Query("show_my_entourages_only") onlyMyEntourages: Boolean,
            @Query("entourage_types") entourageTypes: String?,
            @Query("time_range") timeFrame: Int
    ): Call<NewsfeedItemResponse>

    @GET("myfeeds")
    fun retrieveMyFeeds(
            @Query("page") page: Int,
            @Query("per") per: Int,
            @Query("entourage_types") entourageTypes: String?,
            @Query("tour_types") tourTypes: String?,
            @Query("status") status: String?,
            @Query("created_by_me") createdByMe: Boolean,
            @Query("show_my_partner_only") showMyPartnerOnly: Boolean,
            @Query("accepted_invitation") acceptedInvitation: Boolean
    ): Call<NewsfeedItemResponse>

    @GET("feeds/outings")
    fun retrieveOutings(
            @Query("longitude") longitude: Double,
            @Query("latitude") latitude: Double,
            @Query("starting_after") startingAfterOutingUUID: String?
    ): Call<NewsfeedItemResponse>
}