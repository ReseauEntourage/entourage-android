package social.entourage.android.api.request

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.ChatMessage.ChatMessageResponse
import social.entourage.android.api.model.ChatMessage.ChatMessageListResponse
import social.entourage.android.api.model.EntourageUser.EntourageUserResponse
import social.entourage.android.api.model.EntourageUser.EntourageUserListResponse
import social.entourage.android.api.model.LocationPoint.TourPointWrapper
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.model.tour.TourJoinMessage.TourJoinMessageWrapper
import java.util.*

class TourResponse(var tour: Tour)

class TourWrapper(var tour: Tour)

class TourListResponse(var tours: List<Tour>)

interface TourRequest {
    @POST("tours.json")
    fun createTour(
            @Body tourWrapper: TourWrapper
    ): Call<TourResponse>

    @POST("tours/{tour_id}/tour_points.json")
    fun addTourPoints(
            @Path("tour_id") tourUUID: String,
            @Body points: TourPointWrapper
    ): Call<ResponseBody>

    @PUT("tours/{id}.json")
    fun closeTour(
            @Path("id") tourUUID: String,
            @Body tourWrapper: TourWrapper
    ): Call<TourResponse>

    @GET("tours.json")
    fun retrieveToursNearby(
            @Query("limit") limit: Int,
            @Query("type") type: String?,
            @Query("latitude") latitude: Double,
            @Query("longitude") longitude: Double,
            @Query("distance") distance: Double,
            @Query("status") status: String?
    ): Call<TourListResponse>

    @GET("users/{user_id}/tours.json")
    fun retrieveToursByUserId(
            @Path("user_id") userId: Int,
            @Query("page") page: Int,
            @Query("per") per: Int
    ): Call<TourListResponse>

    @GET("tours/{tour_id}")
    fun retrieveTourById(
            @Path("tour_id") tourUUID: String
    ): Call<TourResponse>

    @GET("tours/{tour_id}/users.json")
    fun retrieveTourUsers(
            @Path("tour_id") tourUUID: String
    ): Call<EntourageUserListResponse>

    @POST("tours/{tour_id}/chat_messages.json")
    fun addChatMessage(
            @Path("tour_id") tourUUID: String,
            @Body message: ChatMessageWrapper
    ): Call<ChatMessageResponse>

    @GET("tours/{tour_id}/chat_messages.json")
    fun retrieveTourMessages(
            @Path("tour_id") tourUUID: String,
            @Query("before") pagination: Date?
    ): Call<ChatMessageListResponse>

    @GET("tours/{tour_id}/encounters.json")
    fun retrieveTourEncounters(
            @Path("tour_id") tourUUID: String
    ): Call<EncounterListResponse>

    @POST("tours/{tour_id}/users")
    fun requestToJoinTour(
            @Path("tour_id") tourUUID: String
    ): Call<EntourageUserResponse>

    @PUT("tours/{tour_id}/users/{user_id}")
    fun updateJoinTourMessage(
            @Path("tour_id") tourUUID: String,
            @Path("user_id") userId: Int,
            @Body messageWrapper: TourJoinMessageWrapper
    ): Call<ResponseBody>

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
    ): Call<EntourageUserResponse>
}