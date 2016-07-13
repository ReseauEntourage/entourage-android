package social.entourage.android.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by mihaiionescu on 12/07/16.
 */
public class Invitation {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String INVITE_BY_SMS = "SMS";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    private int id;

    @SerializedName("invitation_mode")
    private String invitationMode;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("inviter_id")
    @Expose(serialize = false, deserialize = true)
    private int inviterId;

    @SerializedName("entourage_id")
    @Expose(serialize = false, deserialize = true)
    private int entourageId;

    @SerializedName("accepted")
    @Expose(serialize = false, deserialize = true)
    private boolean accepted;

    // ----------------------------------
    // CONSTRUCTOR
    // ----------------------------------

    public Invitation(String invitationMode, String phoneNumber) {
        this.invitationMode = invitationMode;
        this.phoneNumber = phoneNumber;
    }

    // ----------------------------------
    // GETTERS & SETTERS
    // ----------------------------------


    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }

    public int getEntourageId() {
        return entourageId;
    }

    public void setEntourageId(final int entourageId) {
        this.entourageId = entourageId;
    }

    public int getId() {
        return id;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public String getInvitationMode() {
        return invitationMode;
    }

    public void setInvitationMode(final String invitationMode) {
        this.invitationMode = invitationMode;
    }

    public int getInviterId() {
        return inviterId;
    }

    public void setInviterId(final int inviterId) {
        this.inviterId = inviterId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class InvitationWrapper {

        @SerializedName("invite")
        private Invitation invitation;

        public InvitationWrapper(Invitation invitation) {
            this.invitation = invitation;
        }

        public Invitation getInvitation() {
            return invitation;
        }

        public void setInvitation(final Invitation invitation) {
            this.invitation = invitation;
        }
    }

    public static class InvitationsWrapper {

        @SerializedName("invitations")
        private List<Invitation> invitations;

        public List<Invitation> getInvitations() {
            return invitations;
        }

        public void setInvitations(final List<Invitation> invitations) {
            this.invitations = invitations;
        }
    }

}
