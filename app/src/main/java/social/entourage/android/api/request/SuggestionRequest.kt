package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import social.entourage.android.api.model.ZuggestionResponse

interface ZuggestionRequest {

    @GET("zuggestions")
    fun getSuggestions(): Call<ZuggestionResponse>

    @PUT("zuggestions/{id}")
    fun updateSuggestion(
        @Path("id") id: Int,
        @Query("action_type") action: String
    ): Call<Void>
}
