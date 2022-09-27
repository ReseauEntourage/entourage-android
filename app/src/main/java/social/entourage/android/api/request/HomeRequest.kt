package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.models.Pedago
import social.entourage.android.new_v8.models.Summary


class SummaryResponse(@field:SerializedName("user") val summary: Summary)
class PedagogicResponse(@field:SerializedName("resources") val pedago: MutableList<Pedago>)
class PedagogicSingleResponse(@field:SerializedName("resource") val pedago: Pedago)

interface HomeRequest {
    @GET("home/summary")
    fun getSummary(): Call<SummaryResponse>

    @GET("resources")
    fun getPedagogicalResources(): Call<PedagogicResponse>

    @GET("resources/{id}")
    fun getPedagogicalResource(@Path("id") resourceId: Int): Call<PedagogicSingleResponse>

    @POST("resources/{id}/users")
    fun setPedagogicalContentAsRead(@Path("id") groupId: Int): Call<Boolean>
}