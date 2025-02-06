package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserReportWrapper
import social.entourage.android.home.UnreadMessages
import social.entourage.android.user.PrepareAvatarUploadRepository

class UserResponse(val user: User)
class UnreadCountWrapper(@field:SerializedName("user") val unreadMessages: UnreadMessages)

interface UserRequest {
    @PATCH("users/me.json")
    fun updateUser(@Body user: ArrayMap<String, Any>): Call<UserResponse>

    @POST("users/me/presigned_avatar_upload.json")
    fun prepareAvatarUpload(@Body params: PrepareAvatarUploadRepository.Request): Call<PrepareAvatarUploadRepository.Response>

    @PATCH("users/me/code.json")
    fun regenerateSecretCode(@Body userInfo: ArrayMap<String, Any>): Call<UserResponse>

    @GET("users/{user_id}")
    fun getUser(@Path("user_id") userId: String): Call<UserResponse>

    @DELETE("users/me.json")
    fun deleteUser(): Call<UserResponse>

    @POST("users")
    fun registerUser(@Body userInfo: ArrayMap<String, Any>): Call<UserResponse>

    @POST("users/{user_id}/report")
    fun reportUser(@Path("user_id") userId: Int, @Body userReportWrapper: UserReportWrapper): Call<ResponseBody>

    @POST("users/{user_id}/partners")
    fun addPartner(@Path("user_id") userId: Int, @Body partner: PartnerWrapper): Call<PartnerResponse>

    @DELETE("users/{user_id}/partners/{partner_id}")
    fun removePartnerFromUser(@Path("user_id") userId: Int, @Path("partner_id") partnerId: Long): Call<ResponseBody>

    @PUT("users/{user_id}/partners/{partner_id}")
    fun updatePartner(@Path("user_id") userId: Int, @Path("partner_id") partnerId: Long, @Body partner: PartnerWrapper): Call<PartnerResponse>

    @PUT("users/{user_id}")
    fun updateLanguage(@Path("user_id") userId: Int, @Query("user[lang]") lang:String):Call<ResponseBody>

    @POST("users/me/addresses/1")
    fun updatePrimaryAddressLocation(@Body address: ArrayMap<String, Any>): Call<UserResponse>

    @POST("users/me/addresses/2")
    fun updateSecondaryAddressLocation(@Body address: ArrayMap<String, Any>): Call<UserResponse>

    @DELETE("users/me/addresses/2")
    fun deleteSecondaryAddressLocation(): Call<ResponseBody>

    //Onboarding Asso
    @POST("partners/join_request")
    fun updateAssoInfos(@Body asso: ArrayMap<String, Any>): Call<ResponseBody>

    //Get Asso detail
    @GET("partners/{partner_id}")
    fun getPartnerDetail(@Path("partner_id") partnerId: Int): Call<PartnerResponse>

    //Update partner follow
    @POST("users/me/following")
    fun updateUserPartner(@Body isFollowing: ArrayMap<String, Any>): Call<ResponseBody>

    @POST("users/request_phone_change")
    fun changePhone(@Body userInfo: ArrayMap<String, Any>): Call<ResponseBody>

    @GET("users/unread")
    fun getUnreadCountForUser(): Call<UnreadCountWrapper>
}