package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.Image
import social.entourage.android.new_v8.events.create.CreateEvent
import social.entourage.android.new_v8.models.Events


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
        @Query("per") per: Int
    ): Call<EventsListWrapper>

    @GET("outings")
    fun getAllEvents(
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<EventsListWrapper>

    @POST("outings")
    fun createEvent(@Body event: CreateEventWrapper): Call<EventWrapper>

    @POST("outings/{event_id}/report")
    fun reportEvent(
        @Path("event_id") eventId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

    @GET("outings/{id}")
    fun getEvent(@Path("id") eventId: Int): Call<EventWrapper>

    @POST("outings/{event_id}/users")
    fun participate(
        @Path("event_id") eventId: Int
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
        @Path("event_id") eventId: Int
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
}