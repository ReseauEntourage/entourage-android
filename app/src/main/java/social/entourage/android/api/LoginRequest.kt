package social.entourage.android.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import social.entourage.android.api.model.User
import java.util.*


class LoginResponse(val user: User)

interface LoginRequest {
    @POST("login.json")
    fun login(@Body user: HashMap<String, String>): Call<LoginResponse>
}