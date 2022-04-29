package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.GroupImage
import social.entourage.android.new_v8.models.Group


class GroupImagesResponse(@field:SerializedName("neighborhood_images") val groupImages: ArrayList<GroupImage>)
class GroupWrapper(@field:SerializedName("neighborhood") val group: Group)

interface GroupRequest {
    @GET("neighborhood_images")
    fun getGroupImages(): Call<GroupImagesResponse>

    @POST("neighborhoods")
    fun createGroup(@Body groupInfo: GroupWrapper): Call<Group>

    @GET("neighborhoods/{id}")
    fun getGroup(@Path("id") groupId: Int): Call<GroupWrapper>

    @PATCH("neighborhoods/{id}")
    fun updateGroup(@Path("id") groupId: Int,
                    @Body user: ArrayMap<String, Any>): Call<GroupWrapper>
}