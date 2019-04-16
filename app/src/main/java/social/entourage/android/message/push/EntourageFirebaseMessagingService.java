package social.entourage.android.message.push;


import android.content.SharedPreferences;
import com.google.firebase.messaging.RemoteMessage;
import com.mixpanel.android.mpmetrics.MixpanelFCMMessagingService;

import java.util.Map;

import social.entourage.android.EntourageApplication;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

public class EntourageFirebaseMessagingService extends MixpanelFCMMessagingService {

    public static final String TAG = "EntourageFirebaseMessagingService";

    @Override
    public void onNewToken(String registrationId) {
        super.onNewToken(registrationId);
        if (registrationId != null) {
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
            //we always provide some extra data in our push notif
            if(remoteMessage.getData().containsKey(PushNotificationManager.KEY_MIXPANEL)) {
                super.onMessageReceived(remoteMessage);
            } else if(remoteMessage.getData().containsKey(PushNotificationManager.KEY_CTA)) {
                handleFCM(remoteMessage);
                //nothing to do right now
            } else {
                //entourage own notif, need to check the message to see what to do right now
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