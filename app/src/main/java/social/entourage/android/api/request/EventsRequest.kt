package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.api.model.Image


class EventsImagesResponse(@field:SerializedName("entourage_images") val eventImages: ArrayList<Image>)


interface EventsRequest {
    @GET("entourage_images")
    fun getEventsImages(): Call<EventsImagesResponse>
}