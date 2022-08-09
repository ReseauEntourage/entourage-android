package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.Image
import social.entourage.android.new_v8.events.create.CreateEvent
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.models.Group


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
        @Path("event_id") groupId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>
}