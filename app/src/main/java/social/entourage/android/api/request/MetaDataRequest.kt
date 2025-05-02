package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import social.entourage.android.api.model.Tags
import social.entourage.android.api.model.ReactionType

class MetaDataResponse(val tags: Tags,val reactions: MutableList<ReactionType>)

interface MetaDataRequest {
    @GET("home/metadata")
    fun getMetaData(): Call<MetaDataResponse>
}