package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import social.entourage.android.api.model.GroupImage
import social.entourage.android.new_v8.models.Group


class GroupImagesResponse(@field:SerializedName("neighborhood_images") val groupImages: ArrayList<GroupImage>)
class GroupWrapper(@field:SerializedName("neighborhood") val group: Group)

interface GroupRequest {
    @GET("neighborhood_images")
    fun getGroupImages(): Call<GroupImagesResponse>

    @POST("neighborhoods")
    fun createGroup(@Body groupInfo: GroupWrapper): Call<Group>
}