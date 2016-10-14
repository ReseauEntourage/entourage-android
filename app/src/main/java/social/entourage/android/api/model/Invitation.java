package social.entourage.android.api.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

/**
 * Created by mihaiionescu on 12/07/16.
 */
public class Invitation extends TimestampedObject {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    public static final String INVITE_BY_SMS = "SMS";

    private final static String HASH_STRING_HEAD = "Invitation-";

    public static final String STATUS_ACCEPTED = "accepted";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_REJECTED = "rejected";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @SerializedName("id")
    @Expose(serialize = false, deserialize = true)
    private long invitationId;

    @SerializedName("invitation_mode")
    private String invitationMode;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("inviter")
    @Expose(serialize = false, deserialize = true)
    private User inviter;

    @SerializedName("entourage_id")
    @Expose(serialize = false, deserialize = true)
    private int entourageId;

    @SerializedName("status")
    @Expose(serialize = false, deserialize = true)
    private String status;

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

    public int getEntourageId() {
        return entourageId;
    }

    public void setEntourageId(final int entourageId) {
        this.entourageId = entourageId;
    }

    @Override
    public long getId() {
        return invitationId;
    }

    public void setId(final long invitationId) {
        this.invitationId = invitationId;
    }

    public String getInvitationMode() {
        return invitationMode;
    }

    public void setInvitationMode(final String invitationMode) {
        this.invitationMode = invitationMode;
    }

    public User getInviter() {
        return inviter;
    }

    public void setInviter(final User inviter) {
        this.inviter = inviter;
    }

    public String getInviterName() {
        if (inviter == null) return "";
        return inviter.getDisplayName();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public String hashString() {
        return HASH_STRING_HEAD + invitationId;
    }

    @Override
    public int getType() {
        return INVITATION_CARD;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || o.getClass() != this.getClass()) return false;
        return this.invitationId == ((Invitation)o).invitationId;
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
