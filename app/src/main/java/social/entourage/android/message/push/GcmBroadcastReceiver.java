package social.entourage.android.message.push;

import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

public class GcmBroadcastReceiver extends FirebaseMessagingService {

    public static final String TAG = "FcmBroadcastReceiver";

    @Override
    public void onNewToken(String registrationId) {
        super.onNewToken(registrationId);
        if (registrationId != null) {
            final SharedPreferences sharedPreferences = EntourageApplication.get().getSharedPreferences();
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(EntourageApplication.KEY_REGISTRATION_ID, registrationId);
            editor.apply();
            EntourageApplication.get().getMixpanel().getPeople().setPushRegistrationId(registrationId);
        } else {//TODO Check unregistration
            EntourageApplication.get().getMixpanel().getPeople().clearPushRegistrationId();
        }
        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new Events.OnGCMTokenObtainedEvent(registrationId));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Timber.tag(TAG).d("From: %s", remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Timber.tag(TAG).d("Message data payload: %s", remoteMessage.getData());
            //if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(gcm.getMessageType(intent)) && extras != null && !extras.isEmpty()) {
            //    if (intent.getExtras().containsKey("mp_message")) {
            if(remoteMessage.getData().containsKey("mp_message")) {
                    String mp_message = remoteMessage.getData().get("mp_message");
                    //mp_message now contains the notification's text
                    //GCMReceiver mixpanelGCMReceiver = new GCMReceiver();
                    //mixpanelGCMReceiver.onReceive(context, intent);
            } else {
                handleNow(remoteMessage);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.tag(TAG).d("Message Notification Body: %s", remoteMessage.getNotification().getBody());
        }
    }

    private void handleNow(RemoteMessage remoteMessage) {
        Message message = PushNotificationManager.getMessageFromRemoteMessage(remoteMessage, EntourageApplication.get());
        PushNotificationManager.getInstance().handlePushNotification(message, this);
        BusProvider.getInstance().post(new Events.OnPushNotificationReceived(message));
    }
}