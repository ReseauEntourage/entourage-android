package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import social.entourage.android.api.HomeTourArea

/**
 * Created by Jr (MJ-DEVS) on 4/29/21.
 */
interface TourAreaRequest {

    @GET("tour_areas")
    fun getTourAreas(): Call<TourAreasResponse>

    @POST("tour_areas/{tour_area_id}/request")
    fun sendTourAreaRequest(
            @Path("tour_area_id") tourAreaId: Int
    ): Call<ResponseBody>

}

class TourAreasResponse(@field:SerializedName("tour_areas") var tourAreas: List<HomeTourArea>)