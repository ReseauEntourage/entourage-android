package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.GroupImage
import social.entourage.android.api.model.User
import social.entourage.android.new_v8.models.Group


class GroupImagesResponse(@field:SerializedName("neighborhood_images") val groupImages: ArrayList<GroupImage>)
class GroupWrapper(@field:SerializedName("neighborhood") val group: Group)
class GroupsListWrapper(@field:SerializedName("neighborhoods") val allGroups: MutableList<Group>)

interface GroupRequest {
    @GET("neighborhood_images")
    fun getGroupImages(): Call<GroupImagesResponse>

    @POST("neighborhoods")
    fun createGroup(@Body groupInfo: GroupWrapper): Call<Group>

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


    @POST("neighborhoods/{neighborhood_id}/users")
    fun joinGroup(
        @Path("neighborhood_id") userId: Int
    ): Call<EntourageUserResponse>

    @DELETE("neighborhoods/{neighborhood_id}/users")
    fun leaveGroup(
        @Path("neighborhood_id") groupId: Int
    ): Call<EntourageUserResponse>

}