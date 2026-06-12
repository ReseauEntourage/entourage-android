package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import social.entourage.android.api.model.NextStepResponse
import social.entourage.android.api.model.OnboardingQuestionsResponse

interface NextStepRequest {

    @GET("next_step")
    fun getNextStep(): Call<NextStepResponse>

    @PATCH("next_step/{id}/complete")
    fun completeNextStep(@Path("id") id: Int): Call<ResponseBody>

    @PATCH("next_step/{id}/dismiss")
    fun dismissNextStep(@Path("id") id: Int): Call<ResponseBody>

    @POST("next_step/tap_push")
    fun tapPush(): Call<ResponseBody>

    @GET("users/me/onboarding_questions")
    fun getOnboardingQuestions(): Call<OnboardingQuestionsResponse>

    @PATCH("users/me/onboarding_preferences")
    fun updateOnboardingPreferences(@Body preferences: Map<String, Any>): Call<ResponseBody>
}
