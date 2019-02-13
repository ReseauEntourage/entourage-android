package social.entourage.android.message.push;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import social.entourage.android.api.model.Message;
import social.entourage.android.api.tape.Events;
import social.entourage.android.tools.BusProvider;

public class PushNotificationService extends JobIntentService {

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, PushNotificationService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull final Intent intent) {
        Message message = PushNotificationManager.getMessageFromIntent(intent, getApplicationContext());
        PushNotificationManager.getInstance().handlePushNotification(message, this);

        BusProvider.getInstance().post(new Events.OnPushNotificationReceived(message));
    }

}
