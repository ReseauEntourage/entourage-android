package social.entourage.android.message.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.Message;
import social.entourage.android.api.model.PushNotificationContent;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.map.tour.join.received.TourJoinRequestReceivedActivity;
import social.entourage.android.message.MessageActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Singleton that handles the push notifications
 * Created by Mihai Ionescu on 24/10/2017.
 */

public class PushNotificationManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int CHAT_MESSAGE_NOTIFICATION_ID = 2;
    private static final int JOIN_REQUEST_NOTIFICATION_ID = 3;
    private static final int MIN_NOTIFICATION_ID = 4;
    private static final String PREFERENCE_LAST_NOTIFICATION_ID = "PREFERENCE_LAST_NOTIFICATION_ID";

    private static final String TOUR_TAG = "tour-";
    private static final String ENTOURAGE_TAG = "entourage-";

    public static final String PUSH_MESSAGE = "social.entourage.android.PUSH_MESSAGE";

    public static final String KEY_SENDER = "sender";
    public static final String KEY_OBJECT = "object";
    public static final String KEY_CONTENT = "content";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private HashMap<String, List<Message>> pushNotifications = new HashMap<>();

    // ----------------------------------
    // SINGLETON
    // ----------------------------------

    private static final PushNotificationManager ourInstance = new PushNotificationManager();

    public static PushNotificationManager getInstance() {
        return ourInstance;
    }

    private PushNotificationManager() {
    }

    // ------------------------------------
    // INTERNAL PUSH NOTIFICATIONS HANDLING
    // ------------------------------------

    public void handlePushNotification(Message message, Context context) {
        PushNotificationContent content = message.getContent();
        EntourageApplication application = EntourageApplication.get();
        if (content == null || application == null) return;
        if (PushNotificationContent.TYPE_JOIN_REQUEST_CANCELED.equals(content.getType())) {
            // Remove the related join request push notification
            if (content.isTourRelated()) {
                application.removePushNotification(content.getJoinableId(), TimestampedObject.TOUR_CARD, content.getUserId(), PushNotificationContent.TYPE_NEW_JOIN_REQUEST);
            }
            else if (content.isEntourageRelated()) {
                application.removePushNotification(content.getJoinableId(), TimestampedObject.ENTOURAGE_CARD, content.getUserId(), PushNotificationContent.TYPE_NEW_JOIN_REQUEST);
            }
        } else {
            application.addPushNotification(message);
        }

        // Display all notifications except the join_request_canceled
        if (content == null || !PushNotificationContent.TYPE_JOIN_REQUEST_CANCELED.equals(content.getType())) {
            displayPushNotification(message, context);
        }
    }

    public synchronized void addPushNotification(Message message) {
        List<Message> messageList = pushNotifications.get(message.getHash());
        if (messageList == null) {
            messageList = new ArrayList<>();
            pushNotifications.put(message.getHash(), messageList);
        }
        messageList.add(message);
    }

    public synchronized int removePushNotificationsForFeedItem(FeedItem feedItem) {
        long feedItemId = feedItem.getId();
        int feedType = feedItem.getType();
        int count = 0;
        for (String key : pushNotifications.keySet()) {
            List<Message> messageList = pushNotifications.get(key);
            boolean messageListChanged = false;
            if (messageList != null) {
                Iterator<Message> messageIterator = messageList.iterator();
                while (messageIterator.hasNext()) {
                    Message message = messageIterator.next();
                    if (message == null) {
                        continue;
                    }
                    PushNotificationContent content = message.getContent();
                    if (content != null && content.getJoinableId() == feedItemId) {
                        if (FeedItem.TOUR_CARD == feedType && content.isTourRelated()) {
                            if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(content.getType())) {
                                // Don't delete the join requests push, just hide them
                                message.setVisible(false);
                            } else {
                                messageIterator.remove();
                            }
                            count++;
                            messageListChanged = true;
                            continue;
                        }
                        if (FeedItem.ENTOURAGE_CARD == feedType && content.isEntourageRelated()) {
                            if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(content.getType())) {
                                // Don't delete the join requests push, just hide them
                                message.setVisible(false);
                            } else {
                                messageIterator.remove();
                            }
                            count++;
                            messageListChanged = true;
                        }
                    }
                }
            }
            if (messageListChanged) {
                // refresh the android notifications
                updateNotificationGroup(key, messageList);
            }
        }
        return count;
    }

    public synchronized int removePushNotification(Message message) {
        if (message == null) {
            return 0;
        }
        int count = 0;
        for (String key : pushNotifications.keySet()) {
            List<Message> messageList = pushNotifications.get(key);
            if (messageList != null && messageList.size() > 0) {
                Iterator<Message> messageIterator = messageList.iterator();
                while (messageIterator.hasNext()) {
                    Message msg = messageIterator.next();
                    if (msg.getHash().equals(message.getHash())) {
                        messageIterator.remove();
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public synchronized int removePushNotification(FeedItem feedItem, int userId, String pushType) {
        if (feedItem == null) {
            return 0;
        }
        long feedId = feedItem.getId();
        int feedType = feedItem.getType();

        return removePushNotification(feedId, feedType, userId, pushType);
    }

    public synchronized int removePushNotification(long feedId, int feedType, int userId, String pushType) {
        // Sanity checks
        if (pushType == null) {
            return 0;
        }

        EntourageApplication application = EntourageApplication.get();
        if (application == null) {
            return 0;
        }

        // get the notification manager
        NotificationManager notificationManager = (NotificationManager) application.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return 0;
        }

        int count = 0;
        // search for a push notification that matches our parameters
        for (String key : pushNotifications.keySet()) {
            List<Message> messageList = pushNotifications.get(key);
            boolean messageListChanged = false;
            if (messageList != null) {
                Iterator<Message> messageIterator = messageList.iterator();
                while (messageIterator.hasNext()) {
                    Message message = messageIterator.next();
                    if (message == null) {
                        continue;
                    }
                    PushNotificationContent content = message.getContent();
                    if (content != null && content.getJoinableId() == feedId && content.getType() != null && content.getType().equals(pushType)) {
                        if (FeedItem.TOUR_CARD == feedType && content.isTourRelated()) {
                            // remove the notification from our internal list
                            messageIterator.remove();
                            messageListChanged = true;
                            if (message.isVisible()) {
                                application.updateFeedItemsStorage(message, false);
                                count++;
                            }
                            break;
                        }
                        if (FeedItem.ENTOURAGE_CARD == feedType && content.isEntourageRelated()) {
                            messageIterator.remove();
                            messageListChanged = true;
                            if (message.isVisible()) {
                                application.updateFeedItemsStorage(message, false);
                                count++;
                            }
                            break;
                        }
                    }
                }
            }
            if (messageListChanged) {
                // refresh the android notifications
                updateNotificationGroup(key, messageList);
            }
        }
        return count;
    }

    public synchronized void removeAllPushNotifications() {
        // get the notification manager
        NotificationManager notificationManager = (NotificationManager) EntourageApplication.get().getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }
        // cancel all the notifications
        for (String key : pushNotifications.keySet()) {
            List<Message> messageList = pushNotifications.get(key);
            if (messageList != null && messageList.size() > 0) {
                Message message = messageList.get(0);
                if (message != null) {
                    notificationManager.cancel(message.getPushNotificationTag(), message.getPushNotificationId());
                }
            }
        }
        // remove all the notifications from our internal list
        pushNotifications.clear();
    }

    // ----------------------------------
    // PUSH NOTIFICATION UI HANDLING
    // ----------------------------------

    private void displayPushNotification(Message message, Context context) {
        List<Message> messageList = pushNotifications.get(message.getHash());
        int count = messageList.size();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_notification_small);
        builder.setContentIntent(createMessagePendingIntent(message, context));
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_entourage));
        builder.setContentTitle(message.getContentTitleForCount(count, context));
        builder.setContentText(message.getContentTextForCount(count, context));
        builder.setSubText(message.getMessage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            PushNotificationContent content = message.getContent();
            if (content != null && PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(content.getType())) {
                RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(), R.layout.push_notification);
                String notificationText = message.getContentTextForCount(count, context);
                remoteViews.setTextViewText(R.id.push_notification_text, notificationText);
                builder.setContent(remoteViews);
            }
        }

        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_LIGHTS;
        notification.flags = Notification.FLAG_AUTO_CANCEL | Notification.FLAG_SHOW_LIGHTS;
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(message.getPushNotificationTag(), message.getPushNotificationId(), notification);
    }

    private void updateNotificationGroup(String key, List<Message> messageList) {
        if (key == null || messageList == null) return;
        if (messageList.size() == 0) {
            // no more messages, cancel the notification
            String notificationTag = null;
            int notificationId = 0;
            int separator = key.indexOf('-');
            if (separator > 0) {
                notificationTag = key.substring(0, separator);
                notificationId = Integer.parseInt(key.substring(separator+1));
            } else {
                notificationId = Integer.parseInt(key);
            }
            NotificationManager notificationManager = (NotificationManager) EntourageApplication.get().getSystemService(NOTIFICATION_SERVICE);
            notificationManager.cancel(notificationTag, notificationId);
        } else {
            Message message = messageList.get(messageList.size()-1);
            displayPushNotification(message, EntourageApplication.get());
        }
    }

    private PendingIntent createMessagePendingIntent(Message message, Context context) {
        Bundle args = new Bundle();
        args.putSerializable(PUSH_MESSAGE, message);
        Intent messageIntent;
        String messageType = "";
        if (message.getContent() != null) {
            messageType = message.getContent().getType();
        }
        if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(messageType)) {
            messageIntent = new Intent(context, DrawerActivity.class);
        }
        else if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(messageType) || PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED.equals(messageType)) {
            messageIntent = new Intent(context, DrawerActivity.class);
        }
        else if (PushNotificationContent.TYPE_ENTOURAGE_INVITATION.equals(messageType) || PushNotificationContent.TYPE_INVITATION_STATUS.equals(messageType)) {
            messageIntent = new Intent(context, DrawerActivity.class);
        }
        else {
            messageIntent = new Intent(context, MessageActivity.class);
        }
        if (messageType != null) {
            messageIntent.setAction(messageType);
        }
        messageIntent.putExtras(args);
        return PendingIntent.getActivity(context, message.getPushNotificationId(), messageIntent, 0);
    }

    // ----------------------------------
    // STATIC METHODS
    // ----------------------------------

    @Nullable
    public static Message getMessageFromIntent(Intent intent, Context context) {
        Bundle args = intent.getExtras();
        if (args == null) return null;
        Log.d("notification", KEY_SENDER+"= "+args.getString(KEY_SENDER)+"; "+KEY_OBJECT+"= "+args.getString(KEY_OBJECT)+"; "+KEY_CONTENT+"= "+args.getString(KEY_CONTENT));
        Message message = new Message(args.getString(KEY_SENDER), args.getString(KEY_OBJECT), args.getString(KEY_CONTENT), 0);
        message.setPushNotificationId(getNotificationId(context, message));
        message.setPushNotificationTag(getNotificationTag(message));
        return message;
    }

    private static int getNotificationId(Context context, Message message) {
        if (message != null) {
            PushNotificationContent content = message.getContent();
            if (content != null) {
                String notificationType = content.getType();
                if (notificationType != null) {
                    if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE.equals(notificationType)) {
                        return CHAT_MESSAGE_NOTIFICATION_ID;
                    }
                    if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(notificationType)) {
                        return JOIN_REQUEST_NOTIFICATION_ID;
                    }
                }
            }
        }
        return getNextNotificationId(context);
    }

    private static int getNextNotificationId(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int id = sharedPreferences.getInt(PREFERENCE_LAST_NOTIFICATION_ID, MIN_NOTIFICATION_ID - 1) + 1;
        if (id == Integer.MAX_VALUE) {
            id = MIN_NOTIFICATION_ID;
        }
        sharedPreferences.edit().putInt(PREFERENCE_LAST_NOTIFICATION_ID, id).apply();
        return id;
    }

    private static String getNotificationTag(Message message) {
        if (message != null) {
            PushNotificationContent content = message.getContent();
            if (content != null) {
                if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST.equals(content.getType())) {
                    if (content.isTourRelated()) {
                        return TOUR_TAG + String.valueOf(content.getJoinableId());
                    }
                    if (content.isEntourageRelated()) {
                        return ENTOURAGE_TAG + String.valueOf(content.getJoinableId());
                    }
                }
            }
        }
        return null;
    }

}
