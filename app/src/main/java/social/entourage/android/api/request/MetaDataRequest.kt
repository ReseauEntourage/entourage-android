package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.ReactionType
import java.io.Serializable

class MetaDataResponse(val tags: Tags,val reactions: MutableList<ReactionType>, val interests:MutableList<userConfig>, val involvements:MutableList<userConfig>, val concerns:MutableList<userConfig>,)

class userConfig(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,

) : Serializable

interface MetaDataRequest {
    @GET("home/metadata")
    fun getMetaData(): Call<MetaDataResponse>
}