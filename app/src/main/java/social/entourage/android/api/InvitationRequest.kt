package social.entourage.android.api

import com.google.gson.annotations.SerializedName
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*
import social.entourage.android.api.model.Invitation

/**
 * Created by mihaiionescu on 08/08/16.
 */

//class InvitationWrapper(@field:SerializedName("invite") var invitation: Invitation)

class InvitationListResponse(@field:SerializedName("invitations") var invitations: List<Invitation>)

interface InvitationRequest {
    @PUT("invitations/{invitation_id}")
    fun acceptInvitation(
            @Path("invitation_id") invitationId: Long
    ): Call<ResponseBody>

    @DELETE("invitations/{invitation_id}")
    fun refuseInvitation(
            @Path("invitation_id") invitationId: Long
    ): Call<ResponseBody>

    @GET("invitations")
    fun retrieveUserInvitationsWithStatus(
            @Query("status") status: String?
    ): Call<InvitationListResponse>
}