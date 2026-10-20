package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import social.entourage.android.api.model.User
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable as KSerializable

@KSerializable
class LoginResponse(val user: User)

@KSerializable
class LoginWrapper(@SerialName("phone") var phone: String = "", @SerialName("sms_code") var smsCode: String = "")

interface LoginRequest {
    @POST("login.json")
    fun login(@Body userCredentials: LoginWrapper): Call<LoginResponse>
}