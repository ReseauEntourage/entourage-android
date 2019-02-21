package social.entourage.android.message.push;

import android.content.Intent;
import android.content.SharedPreferences;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mixpanel.android.mpmetrics.GCMReceiver;

import java.util.Map;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

public class EntourageFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "EntourageFirebaseMessagingService";

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
            Timber.e("Cannot read new push token. Clearing existing one");
            EntourageApplication.get().getMixpanel().getPeople().clearPushRegistrationId();
        }
        BusProvider.getInstance().register(this);
        BusProvider.getInstance().post(new Events.OnGCMTokenObtainedEvent(registrationId));
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
             if(remoteMessage.getData().containsKey("mp_message")) {
                    Map<String, String> mp_message = remoteMessage.getData();
                    //mp_message now contains the notification's text
                    GCMReceiver mixpanelGCMReceiver = new GCMReceiver();
                    Intent fakeIntent = new Intent("com.google.android.c2dm.intent.RECEIVE");
                    for (String key : mp_message.keySet()) {
                        String value = mp_message.get(key);
                        fakeIntent.putExtra(key, value);
                    }
                    mixpanelGCMReceiver.onReceive(this, fakeIntent);
            } else {
                handleNow(remoteMessage);
            }
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Timber.d("Message Notification Body: %s", remoteMessage.getNotification().getBody());
            //TODO handle this
        }
    }

    private void handleNow(RemoteMessage remoteMessage) {
        Message message = PushNotificationManager.getMessageFromRemoteMessage(remoteMessage, this);
        PushNotificationManager.getInstance().handlePushNotification(message, this);
        BusProvider.getInstance().post(new Events.OnPushNotificationReceived(message));
    }
}