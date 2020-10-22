package social.entourage.android.api.request

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import social.entourage.android.api.model.guide.Category
import social.entourage.android.api.model.guide.Poi

class PoiResponse (var pois: List<Poi>, var categories: List<Category>)
class PoiDetailResponse (var poi: Poi)

interface PoiRequest {
    @GET("pois.json")
    fun retrievePoisNearby(@Query("latitude") latitude: Double,
                           @Query("longitude") longitude: Double,
                           @Query("distance") distance: Double,
                           @Query("category_ids") categoryIDs: String?,
                           @Query("partners_filters") partnersFilters: String?,
                           @Query("v")version:String): Call<PoiResponse>

    @GET("pois/{poi_uuid}")
    fun getPoiDetail(@Path("poi_uuid") poiUuid: String): Call<PoiDetailResponse>
}