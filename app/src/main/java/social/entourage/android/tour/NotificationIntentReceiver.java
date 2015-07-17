package social.entourage.android.tour;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import social.entourage.android.R;

/**
 * Created by NTE on 17/07/15.
 */
public class NotificationIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent redirectIntent = new Intent();
        redirectIntent.setAction(intent.getAction());
        context.sendBroadcast(redirectIntent);
    }
}
