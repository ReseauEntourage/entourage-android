package social.entourage.android.api.request

import androidx.collection.ArrayMap
import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.models.ActionCancel


class MyActionsListWrapper(@field:SerializedName("actions") val allActions: MutableList<Action>)
class DemandsListWrapper(@field:SerializedName("solicitations") val allActions: MutableList<Action>)
class ContribsListWrapper(@field:SerializedName("contributions") val allActions: MutableList<Action>)

class ContribWrapper(@field:SerializedName("contribution") val action: Action)
class DemandWrapper(@field:SerializedName("solicitation") val action: Action)

class ContribCancelWrapper(@field:SerializedName("contribution") val action: ActionCancel)
class DemandCancelWrapper(@field:SerializedName("solicitation") val action: ActionCancel)


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
    fun getContribution(@Path("id") contribId: Int): Call<ContribWrapper>

    @GET("solicitations/{id}")
    fun getDemand(@Path("id") demandId: Int): Call<DemandWrapper>

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

    //create action
    @POST("contributions")
    fun createActionContrib(@Body action: ContribWrapper): Call<ContribWrapper>

    @POST("solicitations")
    fun createActionDemand(@Body action: DemandWrapper): Call<DemandWrapper>

    //Prepare images
    @POST("contributions/presigned_upload")
    fun prepareAddImage(@Body params: RequestContent): Call<PrepareAddPostResponse>

    //cancel action
    @HTTP(method = "DELETE", path = "contributions/{action_id}", hasBody = true)
    fun cancelContribution(
        @Path("action_id") contribId: Int,
        @Body params: ContribCancelWrapper
    ): Call<ContribWrapper>

    @HTTP(method = "DELETE", path = "solicitations/{action_id}", hasBody = true)
    fun cancelDemand(
        @Path("action_id") demandId: Int,
        @Body params: DemandCancelWrapper
    ): Call<DemandWrapper>

}