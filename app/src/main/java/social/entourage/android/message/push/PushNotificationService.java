package social.entourage.android.message.push;

import android.app.IntentService;
import android.content.Intent;

import social.entourage.android.api.model.Message;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

public class PushNotificationService extends IntentService {

    public PushNotificationService() {
        super("PushNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Message message = PushNotificationManager.getMessageFromIntent(intent, getApplicationContext());
        PushNotificationManager.getInstance().handlePushNotification(message, this);

        BusProvider.getInstance().post(new Events.OnPushNotificationReceived(message));

        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

}
