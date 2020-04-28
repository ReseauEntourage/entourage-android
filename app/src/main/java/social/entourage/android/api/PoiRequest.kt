package social.entourage.android.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PoiRequest {
    @GET("pois.json")
    fun retrievePoisNearby(@Query("latitude") latitude: Double,
                           @Query("longitude") longitude: Double,
                           @Query("distance") distance: Double,
                           @Query("category_ids") categoryIDs: String?): Call<PoiResponse?>?
}