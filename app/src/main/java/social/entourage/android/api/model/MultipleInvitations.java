package social.entourage.android.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by mihaiionescu on 08/08/16.
 */
public class MultipleInvitations {

    @SerializedName("mode")
    private String invitationMode;

    @SerializedName("phone_numbers")
    private ArrayList<String> phoneNumbers = new ArrayList<>();

    public MultipleInvitations(String invitationMode) {
        this.invitationMode = invitationMode;
    }

    public void addPhoneNumber(String phoneNumber) {
        phoneNumbers.add(phoneNumber);
    }

    // ----------------------------------
    // WRAPPERS
    // ----------------------------------

    public static class MultipleInvitationsWrapper {

        @SerializedName("invite")
        private MultipleInvitations invitations;

        public MultipleInvitationsWrapper(MultipleInvitations invitations) {
            this.invitations = invitations;
        }

        public MultipleInvitations getInvitations() {
            return invitations;
        }

        public void setInvitations(final MultipleInvitations invitations) {
            this.invitations = invitations;
        }

    }

    public static class MultipleInvitationsResponse {

        @SerializedName("successfull_numbers")
        private ArrayList<String> successfullNumbers;

        @SerializedName("failed_numbers")
        private ArrayList<String> failedNumbers;

        public ArrayList<String> getSuccessfullNumbers() {
            return successfullNumbers;
        }

        public ArrayList<String> getFailedNumbers() {
            return failedNumbers;
        }
    }

}
