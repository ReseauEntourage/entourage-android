package social.entourage.android.api.tape;

public class ConnectionChangedEvent {

    private boolean connected;

    public ConnectionChangedEvent(boolean connected) {
        this.connected = connected;
    }

    public boolean isConnected() {
        return connected;
    }
}
