package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.models.Summary


class SummaryResponse(@field:SerializedName("user") val summary: Summary)

interface SummaryRequest {
    @GET("home/summary")
    fun getSummary(): Call<SummaryResponse>
}