package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import social.entourage.android.api.model.SuggestionsResponse

interface SuggestionsRequest {

    @GET("suggestions")
    fun getSuggestions(
        @Query("page") page: Int,
        @Query("per") per: Int = 10
    ): Call<SuggestionsResponse>
}
