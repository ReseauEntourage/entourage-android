package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.new_v8.models.Conversation
import social.entourage.android.new_v8.models.UserBlocked

/**
 * Created by - on 15/11/2022.
 */

class DiscussionsListWrapper(@field:SerializedName("conversations") val allConversations: MutableList<Conversation>)
class DiscussionDetailWrapper(@field:SerializedName("conversation") val conversation: Conversation)
class UserBlockedWrapper(@field:SerializedName("user_blocked_user") val blockedUser: UserBlocked)
class UsersBlockedWrapper(@field:SerializedName("user_blocked_users") val blockedUsers: MutableList<UserBlocked>)

interface DiscussionsRequest {
    @GET("conversations")
    fun getAllConversations(@Query("page") page: Int,
                            @Query("per") per: Int): Call<DiscussionsListWrapper>

    @GET("conversations/{convId}")
    fun getDetailConversation(@Path("convId") conversationId: Int): Call<DiscussionDetailWrapper>

    @GET("conversations/{convId}/chat_messages")
    fun getMessagesFor(@Path("convId") conversationId: Int,
                       @Query("page") page: Int,
                       @Query("per") per: Int): Call<PostListWrapper>

    @POST("conversations/{conv_id}/chat_messages")
    fun addPost(
        @Path("conv_id") convId: Int,
        @Body params: ArrayMap<String, Any>
    ): Call<PostWrapper>

    @POST("conversations")
    fun createOrGetConversation(
        @Body params: ArrayMap<String, Any>
    ): Call<DiscussionDetailWrapper>

    //Report conversation
    @POST("conversations/{conversation_id}/report")
    fun reportConversation(
        @Path("conversation_id") conversationId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

    @DELETE("conversations/{conversation_id}/users")
    fun leaveConversation(
        @Path("conversation_id") conversationId: Int
    ): Call<ResponseBody>

    //Block user
    @POST("user_blocked_users")
    fun blockUser(
        @Query("id") userId: Int,
    ): Call<UserBlockedWrapper>

    @GET("user_blocked_users")
    fun getBlockedUsers(): Call<UsersBlockedWrapper>

    @HTTP(method = "DELETE", path = "user_blocked_users", hasBody = true)
    fun unblockUsers(
        @Body params: ArrayMap<String, Any>
    ) : Call<ResponseBody>
}