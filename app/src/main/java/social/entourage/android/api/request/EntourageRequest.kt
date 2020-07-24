package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.EntourageJoinInfo
import social.entourage.android.api.model.ChatMessage.ChatMessageListResponse
import social.entourage.android.api.model.ChatMessage.ChatMessageWrapper
import social.entourage.android.api.model.ChatMessage.ChatMessageResponse
import social.entourage.android.api.model.EntourageReport.EntourageReportWrapper
import social.entourage.android.api.model.EntourageUser.EntourageUserListResponse
import social.entourage.android.api.model.EntourageUser.EntourageUserResponse
import social.entourage.android.api.model.MultipleInvitations
import java.util.*

class EntourageWrapper (var entourage: BaseEntourage)
class EntourageResponse (var entourage: BaseEntourage)

class MultipleInvitationsWrapper(@field:SerializedName("invite") var invitations: MultipleInvitations)

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

    @GET("entourages/{entourage_id}/chat_messages.json")
    fun retrieveEntourageMessages(
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
}