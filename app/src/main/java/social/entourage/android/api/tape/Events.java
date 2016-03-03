package social.entourage.android.api.tape;

import com.google.android.gms.maps.model.LatLng;

public class Events {

    /**
     * Event triggering the checking of the intent action
     */
    public static class OnCheckIntentActionEvent {

        public OnCheckIntentActionEvent() {}

    }

    /**
     * Event bearing connection state for the offline encounters queue
     */
    public static class OnConnectionChangedEvent {

        private boolean connected;

        public OnConnectionChangedEvent(boolean connected) {
            this.connected = connected;
        }

        public boolean isConnected() {
            return connected;
        }
    }

    /**
     * Event bearing the registration id when obtained from Google Cloud Messaging (for push notifications)
     */
    public static class OnGCMTokenObtainedEvent {

        private String registrationId;

        public OnGCMTokenObtainedEvent(String registrationId) {
            this.registrationId = registrationId;
        }

        public String getRegistrationId() {
            return registrationId;
        }
    }

    /**
     * Event bearing the new location on which to center the map
     */
    public static class OnBetterLocationEvent {

        private LatLng location;

        public OnBetterLocationEvent(LatLng location) {
            this.location = location;
        }

        public LatLng getLocation() {
            return location;
        }
    }

    /**
     * Event bearing the user's choice regarding the displaying of his past tours
     */
    public static class OnUserChoiceEvent {

        private boolean userHistory;

        public OnUserChoiceEvent(boolean userHistory) {
            this.userHistory = userHistory;
        }

        public boolean isUserHistory() {
            return userHistory;
        }
    }

    /**
     * Event signaling that user info is updated
     */
    public static class OnUserInfoUpdatedEvent {

        public OnUserInfoUpdatedEvent() {};
    }

    /**
     * Event triggering the tours service location listener when the permission has been granted
     */
    public static class OnLocationPermissionGranted {

        private boolean isPermissionGranted;

        public OnLocationPermissionGranted(boolean isPermissionGranted) {
            this.isPermissionGranted = isPermissionGranted;
        }

        public boolean isPermissionGranted() {
            return isPermissionGranted;
        }
    }
}
