package social.entourage.android.message.push;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import social.entourage.android.EntourageEvents;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

public class EntourageFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "EntourageFirebaseMessagingService";

    @Override
    public void onNewToken(String registrationId) {
        super.onNewToken(registrationId);
        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new Events.OnGCMTokenObtainedEvent(registrationId));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_RECEIVED);
        if (remoteMessage.getData().size() > 0) {
            //we always provide some extra data in our push notif
            if(remoteMessage.getData().containsKey(PushNotificationManager.KEY_CTA)) {
                EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_FCM_RECEIVED);
                handleFCM(remoteMessage);
                //nothing to do right now
            } else {
                //entourage own notif, need to check the message to see what to do right now
                EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_ENTOURAGE_RECEIVED);
                handleNow(remoteMessage);
            }
        }
    }

    private void handleFCM(RemoteMessage remoteMessage) {
        PushNotificationManager.getInstance().displayPushNotification(remoteMessage, this);
    }

    private void handleNow(RemoteMessage remoteMessage) {
        Message message = PushNotificationManager.getMessageFromRemoteMessage(remoteMessage, this);
        PushNotificationManager.getInstance().handlePushNotification(message, this);
        BusProvider.getInstance().post(new Events.OnPushNotificationReceived(message));
    }
}