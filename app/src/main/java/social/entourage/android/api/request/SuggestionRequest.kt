package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import social.entourage.android.api.model.SuggestionResponse

interface SuggestionRequest {

    @GET("suggestions")
    fun getSuggestions(): Call<SuggestionResponse>

    @PUT("suggestions/{id}")
    fun updateSuggestion(
        @Path("id") id: Int,
        @Query("action") action: String
    ): Call<Void>
}
