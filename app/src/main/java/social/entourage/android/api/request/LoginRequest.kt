package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import social.entourage.android.api.model.User

class LoginResponse(val user: User)

class LoginWrapper(@field:SerializedName("phone") var phone: String, @field:SerializedName("sms_code") var smsCode: String)

interface LoginRequest {
    @POST("login.json")
    fun login(@Body userCredentials: LoginWrapper): Call<LoginResponse>
}