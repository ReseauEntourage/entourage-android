package social.entourage.android.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.ChatMessage.ChatMessagesWrapper
import social.entourage.android.api.model.EntourageUser.EntourageUserWrapper
import social.entourage.android.api.model.EntourageUser.EntourageUsersWrapper
import social.entourage.android.api.model.LocationPoint.TourPointWrapper
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourJoinMessage.TourJoinMessageWrapper
import java.util.*

class TourWrapper {
    lateinit var tour: Tour
}

class ToursWrapper {
    lateinit var tours: List<Tour>
}

interface TourRequest {
    @POST("tours.json")
    fun tour(
            @Body tourWrapper: TourWrapper
    ): Call<TourWrapper>

    @POST("tours/{tour_id}/tour_points.json")
    fun tourPoints(
            @Path("tour_id") tourUUID: String,
            @Body points: TourPointWrapper
    ): Call<TourWrapper>

    @PUT("tours/{id}.json")
    fun closeTour(
            @Path("id") tourUUID: String,
            @Body tourWrapper: TourWrapper?
    ): Call<TourWrapper>

    @GET("tours.json")
    fun retrieveToursNearby(
            @Query("limit") limit: Int,
            @Query("type") type: String?,
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("distance") distance: Double,
            @Query("status") status: String?
    ): Call<ToursWrapper>

    @GET("users/{user_id}/tours.json")
    fun retrieveToursByUserId(
            @Path("user_id") userId: Int,
            @Query("page") page: Int,
            @Query("per") per: Int
    ): Call<ToursWrapper>

    @GET("users/{user_id}/tours.json")
    fun retrieveToursByUserIdAndStatus(
            @Path("user_id") userId: Int,
            @Query("page") page: Int,
            @Query("per") per: Int,
            @Query("status") status: String
    ): Call<ToursWrapper>

    @GET("users/{user_id}/tours.json")
    fun retrieveToursByUserIdAndPoint(
            @Path("user_id") userId: Int,
            @Query("page") page: Int,
            @Query("per") per: Int,
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("distance") distance: Double
    ): Call<ToursWrapper>

    @GET("tours/{tour_id}")
    fun retrieveTourById(
            @Path("tour_id") tourUUID: String
    ): Call<TourWrapper>

    @GET("tours/{tour_id}/users.json")
    fun retrieveTourUsers(
            @Path("tour_id") tourUUID: String
    ): Call<EntourageUsersWrapper>

    @GET("tours/{tour_id}/users.json")
    fun retrieveTourUsers(
            @Path("tour_id") tourUUID: String,
            @Query("page") page: Int,
            @Query("per") per: Int
    ): Call<EntourageUsersWrapper>

    @POST("tours/{tour_id}/chat_messages.json")
    fun chatMessage(
            @Path("tour_id") tourUUID: String,
            @Body message: ChatMessageWrapper
    ): Call<ChatMessageWrapper>

    @GET("tours/{tour_id}/chat_messages.json")
    fun retrieveTourMessages(
            @Path("tour_id") tourUUID: String
    ): Call<ChatMessagesWrapper>

    @GET("tours/{tour_id}/chat_messages.json")
    fun retrieveTourMessages(
            @Path("tour_id") tourUUID: String,
            @Query("before") pagination: Date?
    ): Call<ChatMessagesWrapper>

    @GET("tours/{tour_id}/encounters.json")
    fun retrieveTourEncounters(
            @Path("tour_id") tourUUID: String
    ): Call<EncounterListWrapper>

    @POST("tours/{tour_id}/users")
    fun requestToJoinTour(
            @Path("tour_id") tourUUID: String
    ): Call<EntourageUserWrapper>

    @PUT("tours/{tour_id}/users/{user_id}")
    fun updateJoinTourMessage(
            @Path("tour_id") tourUUID: String,
            @Path("user_id") userId: Int,
            @Body messageWrapper: TourJoinMessageWrapper
    ): Call<EntourageUserWrapper>

    @PUT("tours/{tour_id}/users/{user_id}")
    fun updateUserTourStatus(
            @Path("tour_id") tourUUID: String,
            @Path("user_id") userId: Int,
            @Body user: HashMap<String, Any>
    ): Call<ResponseBody>

    @DELETE("tours/{tour_id}/users/{user_id}")
    fun removeUserFromTour(
            @Path("tour_id") tourUUID: String,
            @Path("user_id") userId: Int
    ): Call<EntourageUserWrapper>
}