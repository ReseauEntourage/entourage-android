package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.SharingEntourage

/**
 * Created by Jr on 22/12/2021.
 */

class ConversationsResponse(@field:SerializedName("entourages")val messages: List<BaseEntourage>)

interface ConversationsRequest {
    @GET("conversations/private")
    fun retrieveMessagesOne2One(@Query("page") page: Int,
                                @Query("per") per: Int): Call<ConversationsResponse>
    @GET("conversations/group")
    fun retrieveMessagesGroup(@Query("page") page: Int,
                              @Query("per") per: Int): Call<ConversationsResponse>

}