package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import social.entourage.android.api.model.Image
import social.entourage.android.new_v8.models.Events


class EventsImagesResponse(@field:SerializedName("entourage_images") val eventImages: ArrayList<Image>)
class EventsListWrapper(@field:SerializedName("outings") val allEvents: MutableList<Events>)


interface EventsRequest {
    @GET("entourage_images")
    fun getEventsImages(): Call<EventsImagesResponse>


    @GET("users/{user_id}/outings")
    fun getMyEvents(
        @Path("user_id") userId: Int,
    ): Call<EventsListWrapper>

    @GET("outings")
    fun getAllEvents(
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<EventsListWrapper>
}