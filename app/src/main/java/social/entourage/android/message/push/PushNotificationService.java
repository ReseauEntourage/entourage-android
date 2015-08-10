package social.entourage.android.message.push;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.message.MessageActivity;

public class PushNotificationService extends IntentService {

    private static final int NOTIFICATION_ID = 2;

    public static final String PUSH_MESSAGE = "social.entourage.android.PUSH_MESSAGE";

    private static final String KEY_SENDER = "sender";
    private static final String KEY_OBJECT = "object";
    private static final String KEY_CONTENT = "content";

    public PushNotificationService() {
        super("PushNotificationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        displayPushNotification(getMessageFromNotification(intent.getExtras()));
    }

    private void displayPushNotification(Message message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_map);
        builder.setContentIntent(createMessagePendingIntent(message));
        builder.setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_launcher));
        builder.setContentTitle(message.getAuthor());
        builder.setContentText(message.getObject());
        builder.setSubText(message.getContent());

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private PendingIntent createMessagePendingIntent(Message message) {
        Bundle args = new Bundle();
        args.putSerializable(PUSH_MESSAGE, message);
        Intent messageIntent = new Intent(this, MessageActivity.class);
        messageIntent.putExtras(args);

        return PendingIntent.getActivity(this, 0, messageIntent, 0);
    }

    @Nullable
    private Message getMessageFromNotification(Bundle args) {
        return new Message(args.getString(KEY_SENDER), args.getString(KEY_OBJECT), args.getString(KEY_CONTENT));
    }

}
