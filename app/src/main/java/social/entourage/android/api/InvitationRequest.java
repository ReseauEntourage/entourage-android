package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import social.entourage.android.api.model.Invitation;

/**
 * Created by mihaiionescu on 08/08/16.
 */
public interface InvitationRequest {

    @PUT("invitations/{invitation_id}")
    Call<Invitation.InvitationWrapper> acceptInvitation(
            @Path("invitation_id") long invitationId
    );

    @DELETE("invitations/{invitation_id}")
    Call<Invitation.InvitationWrapper> refuseInvitation(
            @Path("invitation_id") long invitationId
    );

    @GET("invitations")
    Call<Invitation.InvitationsWrapper> retrieveUserInvitations();

    @GET("invitations")
    Call<Invitation.InvitationsWrapper> retrieveUserInvitationsWithStatus(
            @Query("status") String status
    );

}
