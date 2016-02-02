package social.entourage.android.api.tape.event;

public class GCMTokenObtainedEvent {

    private String registrationId;

    public GCMTokenObtainedEvent(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getRegistrationId() {
        return registrationId;
    }
}
