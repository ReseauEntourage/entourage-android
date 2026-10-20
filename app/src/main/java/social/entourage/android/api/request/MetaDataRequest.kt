package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.ReactionType
import java.io.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class MetaDataResponse(
    val tags: Tags,
    val reactions: MutableList<ReactionType> = mutableListOf(),
    val interests: MutableList<userConfig> = mutableListOf(),
    val involvements: MutableList<userConfig> = mutableListOf(),
    val concerns: MutableList<userConfig> = mutableListOf(),
    val orientations: MutableList<userConfig> = mutableListOf())

@KSerializable
class userConfig(
    @SerialName("id")
    val id: Int = 0,
    @SerialName("name")
    val name: String? = null) : Serializable

interface MetaDataRequest {
    @GET("home/metadata")
    fun getMetaData(): Call<MetaDataResponse>
}