package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.*
import social.entourage.android.api.model.BaseEntourage.EntourageJoinInfo
import social.entourage.android.api.model.EntourageReport.EntourageReportWrapper
import java.util.*

class EntourageWrapper (var entourage: BaseEntourage)
class EntourageResponse (var entourage: BaseEntourage)

class EntouragesResponse (@field:SerializedName("entourages") var entourages: List<BaseEntourage>? = null)

class MultipleInvitationsWrapper(@field:SerializedName("invite") var invitations: MultipleInvitations)

class EntourageUserResponse {
    var user: EntourageUser? = null
}

class EntourageUserListResponse {
    var users: List<EntourageUser>? = null
}

class ChatMessageWrapper (@SerializedName("chat_message") var chatMessage: Post)
class ShareMessageWrapper (@SerializedName("chat_message") var chatMessage: ShareMessage)
class ChatMessageResponse {
    @SerializedName("chat_message")
    var chatMessage: ChatMessage? = null

}

class ChatMessageListResponse {
    @SerializedName("chat_messages")
    var chatMessages: List<ChatMessage>? = null

}

interface EntourageRequest {
    @POST("entourages.json")
    fun createEntourage(
            @Body entourageWrapper: EntourageWrapper
    ): Call<EntourageResponse>

    @PATCH("entourages/{entourage_id}")
    fun editEntourage(
            @Path("entourage_id") entourageUUID: String,
            @Body entourageWrapper: EntourageWrapper
    ): Call<EntourageResponse>

    @GET("entourages/{entourage_id}")
    fun retrieveEntourageById(
            @Path("entourage_id") entourageUUID: String,
            @Query("distance") distance: Int,
            @Query("feed_rank") feedRank: Int
    ): Call<EntourageResponse>

    @GET("entourages/{entourage_id}")
    fun retrieveEntourageByShareURL(
            @Path("entourage_id") entourageShareURL: String
    ): Call<EntourageResponse>

    @PUT("entourages/{id}")
    fun closeEntourage(
            @Path("id") entourageUUID: String,
            @Body entourageWrapper: EntourageWrapper?
    ): Call<EntourageResponse>

    @GET("entourages/{entourage_id}/users")
    fun retrieveEntourageUsers(
            @Path("entourage_id") entourageUUID: String,
            @Query("context") context: String?
    ): Call<EntourageUserListResponse>

    @POST("entourages/{entourage_id}/users")
    fun requestToJoinEntourage(
            @Path("entourage_id") entourageUUID: String,
            @Body info: EntourageJoinInfo
    ): Call<EntourageUserResponse>

    @PUT("entourages/{entourage_id}/users/{user_id}")
    fun updateUserEntourageStatus(
            @Path("entourage_id") entourageUUID: String,
            @Path("user_id") userId: Int,
            @Body info: HashMap<String, Any>
    ): Call<ResponseBody>

    @DELETE("entourages/{entourage_id}/users/{user_id}")
    fun removeUserFromEntourage(
            @Path("entourage_id") entourageUUID: String,
            @Path("user_id") userId: Int
    ): Call<EntourageUserResponse>

    @POST("entourages/{entourage_id}/chat_messages.json")
    fun addChatMessage(
            @Path("entourage_id") entourageUUID: String,
            @Body message: ChatMessageWrapper
    ): Call<ChatMessageResponse>

    @POST("entourages/{entourage_id}/chat_messages.json")
    fun addSharingMessage(
            @Path("entourage_id") entourageUUID: String,
            @Body message: ShareMessageWrapper
    ): Call<ChatMessageResponse>

    @GET("entourages/{entourage_id}/chat_messages.json")
    fun retrieveMessages(
            @Path("entourage_id") entourageUUID: String,
            @Query("before") pagination: Date?
    ): Call<ChatMessageListResponse>

    @POST("entourages/{entourage_id}/invitations")
    fun inviteBySMS(
            @Path("entourage_id") entourageUUID: String,
            @Body invitations: MultipleInvitationsWrapper
    ): Call<ResponseBody>

    @POST("entourages/{entourage_id}/report")
    fun reportEntourage(
            @Path("entourage_id") userId: Int,
            @Body entourageReportWrapper: EntourageReportWrapper
    ): Call<ResponseBody>

    @DELETE("entourages/{entourage_id}/report_prompt")
    fun removeUserReportPrompt(
            @Path("entourage_id") entourageUUID: String
    ): Call<ResponseBody>

    @GET("entourages/search")
    fun searchEntourages(
            @Query("latitude") latitude: Double?,
            @Query("longitude") longitude: Double?,
            @Query("types") types: String?,
            @Query("q") searchTxt: String,
    ): Call<ResponseBody>

    @GET("entourages/owned")
    fun getMyActions(): Call<EntouragesResponse>
}