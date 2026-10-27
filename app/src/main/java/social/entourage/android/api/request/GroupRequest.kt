package social.entourage.android.api.request

import androidx.collection.ArrayMap
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Group
import social.entourage.android.api.model.Image
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.CompleteReactionsResponse
import social.entourage.android.api.model.ReactionWrapper
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class GroupImagesResponse(@SerialName("neighborhood_images") val groupImages: ArrayList<Image> = ArrayList())
@KSerializable
class GroupWrapper(@SerialName("neighborhood") val group: Group)
@KSerializable
class GroupsListWrapper(@SerialName("neighborhoods") val allGroups: MutableList<Group> = mutableListOf())
@KSerializable
class RequestContent internal constructor(private val content_type: String = "")

@KSerializable
class Report(var message: String = "", var signals: MutableList<String> = mutableListOf())
@KSerializable
class ReportWrapper(
    @SerialName("report") var Report: Report
)

@KSerializable
class PrepareAddPostResponse(
    @SerialName("upload_key") var uploadKey: String = "",
    @SerialName("presigned_url") val presignedUrl: String = ""
) {
    override fun toString(): String {
        return "PrepareAddPostResponse(avatarKey='$uploadKey', presignedUrl='$presignedUrl')"
    }
}

@KSerializable
class MembersWrapper(@SerialName("users") val users: MutableList<EntourageUser> = mutableListOf())
@KSerializable
class PostListWrapper(@SerialName("chat_messages") val posts: MutableList<Post> = mutableListOf())
@KSerializable
class PostWrapper(@SerialName("chat_message") val post: Post)

interface GroupRequest {
    @GET("neighborhood_images")
    fun getGroupImages(): Call<GroupImagesResponse>

    @POST("neighborhoods")
    fun createGroup(@Body groupInfo: GroupWrapper): Call<GroupWrapper>

    @GET("neighborhoods/{id}")
    fun getGroup(@Path("id") groupId: Int): Call<GroupWrapper>

    @GET("neighborhoods/{id}")
    fun getGroupWithStringId(@Path("id") groupId: String): Call<GroupWrapper>

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

    @GET("neighborhoods/national")
    fun getNationalGroups(): Call<GroupsListWrapper>

    @GET("neighborhoods/{neighborhood_id}/users")
    fun searchGroupMembers(
        @Path("neighborhood_id") groupId: Int,
        @Query("query") query: String
    ): Call<MembersWrapper>

    @GET("neighborhoods")
    fun getAllGroupswithFilter(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("interest_list") interests: String,
        @Query("travel_distance") radius: Int,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?
    ): Call<GroupsListWrapper>


    @GET("users/{user_id}/neighborhoods")
    fun getMyGroupswithFilter(
        @Path("user_id") userId: Int,
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("interest_list") interests: String,
        @Query("travel_distance") radius: Int,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?
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

    @GET("users/me/neighborhoods/default")
    fun getDefautGroups(

    ): Call<GroupWrapper>

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

    // social.entourage.android.api.request.GroupRequest
    @GET("neighborhoods/{neighborhood_id}/users")
    fun getMembers(
        @Path("neighborhood_id") groupId: Int,
        @Query("page") page: Int? = null,
        @Query("per")  per:  Int? = null
    ): Call<MembersWrapper>

    // image_size=high : le backend le prend en compte sur la liste (EN-9671) pour servir la variante
    // pleine largeur des images des publications
    @GET("neighborhoods/{neighborhood_id}/chat_messages?image_size=high")
    fun getGroupPosts(
        @Path("neighborhood_id") groupId: Int,
        @Query("page") page: Int,
        @Query("per") per: Int,
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

    @DELETE("neighborhoods/{group_id}/chat_messages/{post_id}")
    fun deletePost(
        @Path("group_id") groupId: Int,
        @Path("post_id") postId: Int
    ): Call<ResponseBody>

    // Même ressource chat_message que conversations/{id}/chat_messages/{id} (cf.
    // DiscussionsRequest.updateMessage) : à confirmer en recette avant release, ce PATCH
    // n'ayant pas été testé contre le back pour les posts/commentaires de groupe.
    @PATCH("neighborhoods/{neighborhood_id}/chat_messages/{post_id}")
    fun updatePost(
        @Path("neighborhood_id") groupId: Int,
        @Path("post_id") postId: Int,
        @Body params: ArrayMap<String, Any>
    ): Call<PostWrapper>
    @GET("neighborhoods/{neighborhood_id}/outings")
    fun getGroupEvents(
        @Path("neighborhood_id") groupId: Int,
    ): Call<EventsListWrapper>

    @GET("neighborhoods/{neighborhood_id}/chat_messages/{post_id}/reactions")
    fun getReactionGroupPost(
        @Path("neighborhood_id") groupId: Int,
        @Path("post_id") postId: Int,
    ): Call<ResponseBody>

    @GET("neighborhoods/{neighborhood_id}/chat_messages/{post_id}/reactions/users")
    fun getDetailsReactionGroupPost(
        @Path("neighborhood_id") groupId: Int,
        @Path("post_id") postId: Int,
    ): Call<CompleteReactionsResponse>

    @POST("neighborhoods/{neighborhood_id}/chat_messages/{post_id}/reactions")
    fun postReactionGroupPost(
        @Path("neighborhood_id") groupId: Int,
        @Path("post_id") postId: Int,
        @Body reactionWrapper: ReactionWrapper
    ): Call<ResponseBody>

    @DELETE("neighborhoods/{neighborhood_id}/chat_messages/{post_id}/reactions")
    fun deleteReactionGroupPost(
        @Path("neighborhood_id") groupId: Int,
        @Path("post_id") postId: Int,
    ): Call<ResponseBody>



    @GET("neighborhoods/{neighborhood_id}/chat_messages/{post_id}")
    fun getPostDetail(
        @Path("neighborhood_id") groupId: Int,
        @Path("post_id") postId: Int,
        @Query("image_size") size:String,
    ): Call<PostWrapper>

    @GET("neighborhoods")
    fun getAllGroupsWithSearchQuery(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<GroupsListWrapper>

    @GET("users/{user_id}/neighborhoods")
    fun getMyGroupsWithSearchQuery(
        @Path("user_id") userId: Int,
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<GroupsListWrapper>
}