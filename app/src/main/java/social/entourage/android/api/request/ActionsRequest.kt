package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.new_v8.models.Action


class MyActionsListWrapper(@field:SerializedName("actions") val allActions: MutableList<Action>)
class DemandsListWrapper(@field:SerializedName("solicitations") val allActions: MutableList<Action>)
class ContribsListWrapper(@field:SerializedName("contributions") val allActions: MutableList<Action>)

class CreateActionWrapper(@field:SerializedName("action") val action: Action)
class ActionWrapper(@field:SerializedName("action") val action: Action)


interface ActionsRequest {
    @GET("contributions")
    fun getAllActionsContrib(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("sections[]") sections: String?,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Call<ContribsListWrapper>

    @GET("solicitations")
    fun getAllActionsDemand(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("sections[]") sections: String?,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Call<DemandsListWrapper>

    @GET("users/me/actions")
    fun getMyActions(
        @Query("page") page: Int,
        @Query("per") per: Int,
    ): Call<MyActionsListWrapper>

    @GET("contributions/{id}")
    fun getContribution(@Path("id") contribId: Int): Call<ActionWrapper>

    @GET("solicitations/{id}")
    fun getDemand(@Path("id") demandId: Int): Call<ActionWrapper>

    //Report actions
    @POST("contributions/{action_id}/report")
    fun reportContribution(
        @Path("action_id") actionId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

    @POST("solicitations/{action_id}/report")
    fun reportDemand(
        @Path("action_id") actionId: Int,
        @Body reportWrapper: ReportWrapper
    ): Call<ResponseBody>

}