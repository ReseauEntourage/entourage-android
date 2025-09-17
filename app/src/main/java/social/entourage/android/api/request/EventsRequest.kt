package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.events.create.CreateEvent
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.Image
import social.entourage.android.api.model.CompleteReactionsResponse
import social.entourage.android.api.model.ReactionWrapper
import social.entourage.android.events.JoinRoleBody

class EventsImagesResponse(@field:SerializedName("entourage_images") val eventImages: ArrayList<Image>)
class EventsListWrapper(@field:SerializedName("outings") val allEvents: MutableList<Events>)

class CreateEventWrapper(@field:SerializedName("outing") val event: CreateEvent)
class EventWrapper(@field:SerializedName("outing") val event: Events)

interface EventsRequest {
    @GET("entourage_images")
    fun getEventsImages(): Call<EventsImagesResponse>

    @GET("users/{user_id}/outings")
    fun getMyEvents(
        @Path("user_id") userId: Int,
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Call<EventsListWrapper>
    @GET("users/{user_id}/outings")
    fun getMyEventsWithFilter(
        @Path("user_id") userId: Int,
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("interest_list") interests: String,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("period") period: String
    ): Call<EventsListWrapper>
    @GET("outings")
    fun getAllEvents(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("period") period:String
    ): Call<EventsListWrapper>
    @GET("outings")
    fun getAllEventsWithFilter(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("interest_list") interests: String,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("period") period:String
    ): Call<EventsListWrapper>

    @POST("outings")
    fun createEvent(@Body event: CreateEventWrapper): Call<EventWrapper>

    @POST("outings/{event_id}/report")
    fun reportEvent(
        @Path("event_id") eventId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

    @POST("outings/{event_id}/chat_messages/{post_id}/report")
    fun reportEventPost(
        @Path("event_id") groupId: Int,
        @Path("post_id") postId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

    @DELETE("outings/{event_id}/chat_messages/{post_id}")
    fun deleteEventPost(
        @Path("event_id") groupId: Int,
        @Path("post_id") postId: Int
    ): Call<ResponseBody>
    @GET("outings/{event_id}/users")
    fun getMembersSearch(
        @Path("event_id") eventId: Int,
        @Query("query") searchQuery: String
    ): Call<MembersWrapper>


    @DELETE("outings/{event_id}/chat_messages/{post_id}/reactions")
    fun deleteReactionAnEventPost(
        @Path("event_id") groupId: Int,
        @Path("post_id") postId: Int
    ): Call<ResponseBody>



    @GET("outings/{id}")
    fun getEvent(@Path("id") eventId: String): Call<EventWrapper>

    @GET("outings/smalltalk")
    fun getEventSmallTalk(): Call<EventWrapper>

    @POST("outings/{event_id}/users")
    fun participate(
        @Path("event_id") eventId: Int
    ): Call<EntourageUserResponse>

    @POST("outings/{event_id}/users")
    fun joinAsOrganizer(
        @Path("event_id") eventId: Int,
        @Body body: JoinRoleBody
    ): Call<EntourageUserResponse>


    @DELETE("outings/{event_id}/users")
    fun leaveEvent(
        @Path("event_id") eventId: Int
    ): Call<EntourageUserResponse>

    @GET("outings/{event_id}/users")
    fun getMembers(
        @Path("event_id") eventId: Int
    ): Call<MembersWrapper>

    @DELETE("outings/{event_id}")
    fun cancelEvent(
        @Path("event_id") eventId: Int
    ): Call<EventWrapper>

    @PUT("outings/{event_id}")
    fun updateEvent(
        @Path("event_id") eventId: Int,
        @Body event: ArrayMap<String, Any>
    ): Call<EventWrapper>

    @GET("outings/{event_id}/chat_messages")
    fun getEventPosts(
        @Path("event_id") eventId: Int,
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<PostListWrapper>

    @POST("outings/{event_id}/chat_messages/presigned_upload")
    fun prepareAddPost(
        @Path("event_id") eventId: Int,
        @Body params: RequestContent
    ): Call<PrepareAddPostResponse>

    @GET("outings/{event_id}/chat_messages/{post_id}/comments")
    fun getPostComments(
        @Path("event_id") eventId: Int,
        @Path("post_id") postId: Int,
    ): Call<PostListWrapper>

    @POST("outings/{event_id}/chat_messages")
    fun addPost(
        @Path("event_id") eventId: Int,
        @Body params: ArrayMap<String, Any>
    ): Call<PostWrapper>

    @PUT("outings/{event_id}")
    fun updateEvent(
        @Path("event_id") eventId: Int,
        @Body event: CreateEventWrapper
    ): Call<EventWrapper>

    @PUT("outings/{event_id}/batch_update")
    fun updateEventSiblings(
        @Path("event_id") eventId: Int,
        @Body event: CreateEventWrapper
    ): Call<EventWrapper>

    @GET("outings/{event_id}/chat_messages/{post_id}")
    fun getPostDetail(
        @Path("event_id") eventId: Int,
        @Path("post_id") postId: Int,
        @Query("image_size") size:String
    ): Call<PostWrapper>

    @GET("outings/{outing_id}/chat_messages/{post_id}/details")
    fun getReactionEventPost(
        @Path("outing_id") eventId: Int,
        @Path("post_id") postId: Int,
    ): Call<ResponseBody>

    @GET("outings/{outing_id}/chat_messages/{post_id}/reactions/users")
    fun getDetailsReactionEventPost(
        @Path("outing_id") eventId: Int,
        @Path("post_id") postId: Int,
    ): Call<CompleteReactionsResponse>

    @POST("outings/{outing_id}/chat_messages/{post_id}/reactions")
    fun postReactionEventPost(
        @Path("outing_id") eventId: Int,
        @Path("post_id") postId: Int,
        @Body reactionWrapper: ReactionWrapper
    ): Call<ResponseBody>

    @POST("outings/{outing_id}/users/confirm")
    fun confirmParticipation(
        @Path("outing_id") eventId: Int
    ): Call<ResponseBody>
    @GET("outings")
    fun getAllEventsWithSearchQuery(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<EventsListWrapper>

    @GET("users/{user_id}/outings")
    fun getMyEventsWithSearchQuery(
        @Path("user_id") userId: Int,
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<EventsListWrapper>

}