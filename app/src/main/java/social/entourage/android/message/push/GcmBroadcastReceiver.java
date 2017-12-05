package social.entourage.android.message.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.mixpanel.android.mpmetrics.GCMReceiver;

public class GcmBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(context);
        if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(gcm.getMessageType(intent)) && extras != null && !extras.isEmpty()) {
            if (intent.getExtras().containsKey("mp_message")) {
                String mp_message = intent.getExtras().getString("mp_message");
                //mp_message now contains the notification's text
                GCMReceiver mixpanelGCMReceiver = new GCMReceiver();
                mixpanelGCMReceiver.onReceive(context, intent);
            } else {
                PushNotificationService.enqueueWork(context, intent);
            }
        }
    }

}