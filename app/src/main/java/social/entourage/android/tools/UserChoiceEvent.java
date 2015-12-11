package social.entourage.android.tools;

public class UserChoiceEvent {

    private boolean userToursOnly;

    public UserChoiceEvent(boolean userToursOnly) {
        this.userToursOnly = userToursOnly;
    }

    public boolean isUserToursOnly() {
        return userToursOnly;
    }
}
