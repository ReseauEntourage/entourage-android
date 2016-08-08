package social.entourage.android.api;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import social.entourage.android.api.model.Invitation;

/**
 * Created by mihaiionescu on 08/08/16.
 */
public interface InvitationRequest {

    @PUT("invitation/{invitation_id}")
    Call<Invitation.InvitationWrapper> acceptInvitation(
            @Path("invitation_id") int invitationId
    );

    @DELETE("invitation/{invitation_id}")
    Call<Invitation.InvitationWrapper> refuseInvitation(
            @Path("invitation_id") int invitationId
    );

    @GET("invitations")
    Call<Invitation.InvitationsWrapper> retrieveUserInvitations();

}
