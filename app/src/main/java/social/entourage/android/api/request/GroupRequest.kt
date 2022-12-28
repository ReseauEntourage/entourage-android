package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Image
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Post

class GroupImagesResponse(@field:SerializedName("neighborhood_images") val groupImages: ArrayList<Image>)
class GroupWrapper(@field:SerializedName("neighborhood") val group: Group)
class GroupsListWrapper(@field:SerializedName("neighborhoods") val allGroups: MutableList<Group>)
class RequestContent internal constructor(private val content_type: String)

class Report(var message: String, var signals: MutableList<String>)
class ReportWrapper(
    @field:SerializedName("report") var Report: Report
)

class PrepareAddPostResponse(
    @field:SerializedName("upload_key") var uploadKey: String,
    @field:SerializedName("presigned_url") val presignedUrl: String
) {
    override fun toString(): String {
        return "PrepareAddPostResponse(avatarKey='$uploadKey', presignedUrl='$presignedUrl')"
    }
}

class MembersWrapper(@field:SerializedName("users") val users: MutableList<EntourageUser>)
class PostListWrapper(@field:SerializedName("chat_messages") val posts: MutableList<Post>)
class PostWrapper(@field:SerializedName("chat_message") val post: Post)

interface GroupRequest {
    @GET("neighborhood_images")
    fun getGroupImages(): Call<GroupImagesResponse>

    @POST("neighborhoods")
    fun createGroup(@Body groupInfo: GroupWrapper): Call<GroupWrapper>

    @GET("neighborhoods/{id}")
    fun getGroup(@Path("id") groupId: Int): Call<GroupWrapper>

    @PATCH("neighborhoods/{id}")
    fun updateGroup(
        @Path("id") groupId: Int,
        @Body user: ArrayMap<String, Any>
    ): Call<GroupWrapper>

    @GET("neighborhoods")
    fun getAllGroups(
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<GroupsListWrapper>

    @GET("neighborhoods")
    fun getGroupsSearch(
        @Query("q") searchTxt: String,
    ): Call<GroupsListWrapper>

    @GET("users/{user_id}/neighborhoods")
    fun getMyGroups(
        @Path("user_id") userId: Int,
        @Query("page") page: Int,
        @Query("per") per: Int,
    ): Call<GroupsListWrapper>

    @GET("users/{user_id}/neighborhoods")
    fun getMyGroups(
        @Path("user_id") userId: Int
    ): Call<GroupsListWrapper>

    @POST("neighborhoods/{neighborhood_id}/users")
    fun joinGroup(
        @Path("neighborhood_id") userId: Int
    ): Call<EntourageUserResponse>

    @DELETE("neighborhoods/{neighborhood_id}/users")
    fun leaveGroup(
        @Path("neighborhood_id") groupId: Int
    ): Call<EntourageUserResponse>

    @GET("neighborhoods/{neighborhood_id}/users")
    fun getMembers(
        @Path("neighborhood_id") groupId: Int
    ): Call<MembersWrapper>

    @GET("neighborhoods/{neighborhood_id}/chat_messages")
    fun getGroupPosts(
        @Path("neighborhood_id") groupId: Int
    ): Call<PostListWrapper>

    @POST("neighborhoods/{neighborhood_id}/chat_messages/presigned_upload")
    fun prepareAddPost(
        @Path("neighborhood_id") groupId: Int,
        @Body params: RequestContent
    ): Call<PrepareAddPostResponse>

    @POST("neighborhoods/{neighborhood_id}/chat_messages")
    fun addPost(
        @Path("neighborhood_id") groupId: Int,
        @Body params: ArrayMap<String, Any>
    ): Call<PostWrapper>

    @GET("neighborhoods/{neighborhood_id}/chat_messages/{post_id}/comments")
    fun getPostComments(
        @Path("neighborhood_id") groupId: Int,
        @Path("post_id") postId: Int,
    ): Call<PostListWrapper>

    @POST("neighborhoods/{group_id}/report")
    fun reportGroup(
        @Path("group_id") groupId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

    @POST("neighborhoods/{group_id}/chat_messages/{post_id}/report")
    fun reportPost(
        @Path("group_id") groupId: Int,
        @Path("post_id") postId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

    @GET("neighborhoods/{neighborhood_id}/outings")
    fun getGroupEvents(
        @Path("neighborhood_id") groupId: Int,
    ): Call<EventsListWrapper>
}