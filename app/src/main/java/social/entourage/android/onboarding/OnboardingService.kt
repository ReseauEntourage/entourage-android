package social.entourage.android.onboarding

import androidx.collection.ArrayMap
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import social.entourage.android.api.UserResponse

/**
 * Created by Jr (MJ-DEVS) on 11/05/2020.
 */
interface OnboardingService {
    @POST("users")
    fun registerUser(@Body userInfo: ArrayMap<String, Any>): Call<UserResponse>
}