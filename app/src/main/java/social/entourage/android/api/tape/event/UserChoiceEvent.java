package social.entourage.android.api.tape.event;

public class UserChoiceEvent {

    private boolean userHistory;

    public UserChoiceEvent(boolean userHistory) {
        this.userHistory = userHistory;
    }

    public boolean isUserHistory() {
        return userHistory;
    }
}
