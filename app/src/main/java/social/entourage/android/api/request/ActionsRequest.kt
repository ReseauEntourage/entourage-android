package social.entourage.android.api.request

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionCancel

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
        @Query("section_list") sections: String?,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,

    ): Call<DemandsListWrapper>
    @GET("solicitations")
    fun getAllActionsDemandWithoutMine(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("section_list") sections: String?,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
        @Query("exclude_memberships") excludeMemberships : Boolean?,

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

    //Update
    @PUT("contributions/{action_id}")
    fun updateActionContrib( @Path("action_id") actionId: Int,
                             @Body action: ContribWrapper): Call<ContribWrapper>

    @PUT("solicitations/{action_id}")
    fun updateActionDemand( @Path("action_id") actionId: Int,
                            @Body action: DemandWrapper): Call<DemandWrapper>

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

    @GET("contributions")
    fun getAllActionsContribWithFilter(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("section_list") sections: String?,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Call<ContribsListWrapper>

    @GET("solicitations")
    fun getAllActionsDemandWithFilter(
        @Query("page") page: Int,
        @Query("per") per: Int,
        @Query("section_list") sections: String?,
        @Query("travel_distance") travelDistance: Int?,
        @Query("latitude") latitude: Double?,
        @Query("longitude") longitude: Double?,
    ): Call<DemandsListWrapper>
    @GET("contributions")
    fun getAllContribsWithSearchQuery(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<ContribsListWrapper>

    @GET("solicitations")
    fun getAllDemandsWithSearchQuery(
        @Query("q") query: String,
        @Query("page") page: Int,
        @Query("per") per: Int
    ): Call<DemandsListWrapper>
}