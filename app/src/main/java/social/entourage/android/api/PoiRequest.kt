package social.entourage.android.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import social.entourage.android.api.model.guide.Category
import social.entourage.android.api.model.guide.Poi

class PoiResponse {
    var pois: List<Poi>? = null
    var categories: List<Category>? = null
}

interface PoiRequest {
    @GET("pois.json")
    fun retrievePoisNearby(@Query("latitude") latitude: Double,
                           @Query("longitude") longitude: Double,
                           @Query("distance") distance: Double,
                           @Query("category_ids") categoryIDs: String?): Call<PoiResponse>
}