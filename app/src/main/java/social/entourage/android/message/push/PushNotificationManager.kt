package social.entourage.android.message.push

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.RemoteMessage
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.FeedItem
import timber.log.Timber
import java.util.*

/**
 * Singleton that handles the push notifications
 * Created by Mihai Ionescu on 24/10/2017.
 */
object PushNotificationManager {
    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    const val PUSH_MESSAGE = "social.entourage.android.PUSH_MESSAGE"
    const val KEY_CTA = "entourage_cta"
    const val KEY_MIXPANEL = "mp_message"

    private const val CHAT_MESSAGE_NOTIFICATION_ID = 2
    private const val JOIN_REQUEST_NOTIFICATION_ID = 3
    private const val MIN_NOTIFICATION_ID = 4
    private const val PREFERENCE_LAST_NOTIFICATION_ID = "PREFERENCE_LAST_NOTIFICATION_ID"
    private const val KEY_SENDER = "sender"
    private const val KEY_OBJECT = "object"
    private const val KEY_CONTENT = "content"

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var pushNotifications = HashMap<String, MutableList<Message>>()
    // ------------------------------------
    // INTERNAL PUSH NOTIFICATIONS HANDLING
    // ------------------------------------
    /**
     * Handle a push notification received from the server
     * @param message The message that we use to build the push notification
     * @param context The context into which to add the push notification
     */
    fun handlePushNotification(message: Message, context: Context) {
        val content = message.content ?: return
        val application = EntourageApplication.get() ?: return
        if (PushNotificationContent.TYPE_JOIN_REQUEST_CANCELED == content.type) {
            // Remove the related join request push notification
            if (content.isTourRelated) {
                application.removePushNotification(content.joinableId, TimestampedObject.TOUR_CARD, content.userId, PushNotificationContent.TYPE_NEW_JOIN_REQUEST)
            } else if (content.isEntourageRelated) {
                application.removePushNotification(content.joinableId, TimestampedObject.ENTOURAGE_CARD, content.userId, PushNotificationContent.TYPE_NEW_JOIN_REQUEST)
            }
        } else {
            application.addPushNotification(message)
            // Display all notifications except the join_request_canceled
            displayPushNotification(message, context)
        }
    }

    /**
     * Adds a message to our internal list, creating the group if necessary
     * @param message the message to add
     */
    @Synchronized
    fun addPushNotification(message: Message) {
        val messageList = pushNotifications[message.hash] ?: ArrayList()
        messageList.add(message)
        pushNotifications[message.hash] = messageList
    }

    /**
     * Removes the notifications for a feed item, updating the groups if necessary
     * @param feedItem the feed item from which to remove the notifications
     * @return the number of push notifications that were removed
     */
    @Synchronized
    fun removePushNotificationsForFeedItem(feedItem: FeedItem): Int {
        val feedItemId = feedItem.id
        val feedType = feedItem.type
        var nbNotifsFound = 0
        val newPushNotifsMap =  HashMap<String, MutableList<Message>>()
        for(key in pushNotifications.keys) {
            if (pushNotifications[key] == null) continue
            var messageListChanged = false
            val newMessageList = ArrayList<Message>()
            for(message in pushNotifications[key]!!) {
                val content = message.content
                if (content != null && content.joinableId == feedItemId) {
                    if((FeedItem.TOUR_CARD == feedType && content.isTourRelated)
                            || (FeedItem.ENTOURAGE_CARD == feedType && content.isEntourageRelated)){
                        nbNotifsFound++
                        messageListChanged = true
                        if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST == content.type) {
                            // Don't delete the join requests push, just hide them
                            message.isVisible = false
                        } else {
                            continue
                        }
                    }
                }
                newMessageList.add(message)
            }
            if (messageListChanged && !updateNotificationGroup(key, newMessageList)) {
                // no more notifications in the group, remove the key
                continue
            }
            if(newMessageList.isNotEmpty()) newPushNotifsMap[key] = newMessageList
        }
        pushNotifications = newPushNotifsMap
        return nbNotifsFound
    }

    /**
     * Removes a notification from our internal list
     * @param message The message to remove
     * @return the number of push notifications that were removed
     */
    @Synchronized
    fun removePushNotification(message: Message): Int {
        var nbMsgFound = 0
        val newPushNotifMap = HashMap<String, MutableList<Message>>()
        for (key in pushNotifications.keys) {
            if (pushNotifications[key] == null) continue
            val newMessageList = ArrayList<Message>()
            for(msg in pushNotifications[key]!!) {
                if (msg.hash == message.hash) {
                    nbMsgFound++
                    continue
                }
                newMessageList.add(msg)
            }
            if(newMessageList.isNotEmpty()) newPushNotifMap[key] = newMessageList
        }
        pushNotifications = newPushNotifMap
        return nbMsgFound
    }

    /**
     * Removes notifications from a feed that matches the required type (see [PushNotificationContent])
     * @param feedItem the feed item
     * @param userId not used
     * @param pushType the required type
     * @return the number of push notifications that were removed
     */
    @Synchronized
    fun removePushNotification(feedItem: FeedItem, userId: Int, pushType: String): Int {
        return removePushNotification(feedItem.id, feedItem.type, userId, pushType)
    }

    /**
     * Removes notifications from a feed that matches the required type (see [PushNotificationContent])
     * @param feedId feed id
     * @param feedType feed type (see [FeedItem])
     * @param userId not used
     * @param pushType the required type
     * @return the number of push notifications that were removed
     */
    @Synchronized
    fun removePushNotification(feedId: Long, feedType: Int, userId: Int, pushType: String?): Int {
        // Sanity checks
        if (pushType == null) {
            return 0
        }
        val application = EntourageApplication.get() ?: return 0
        var count = 0
        // search for a push notification that matches our parameters
        val newPush = HashMap<String, MutableList<Message>>()
        for(key in pushNotifications.keys) {
            if (pushNotifications[key] == null) continue
            val messageList = ArrayList<Message>()
            var messageListChanged = false
            for(message in pushNotifications[key]!!) {
                val content = message.content
                if (content != null && content.joinableId == feedId && content.type != null && content.type == pushType) {
                    if (FeedItem.TOUR_CARD == feedType && content.isTourRelated
                            || FeedItem.ENTOURAGE_CARD == feedType && content.isEntourageRelated) {
                        messageListChanged = true
                        if (message.isVisible) {
                            application.storeNewPushNotification(message, false)
                            count++
                        }
                        continue
                    }
                }
                messageList.add(message)
            }
            if (messageListChanged) {
                // refresh the android notifications
                if (!updateNotificationGroup(key, messageList)) {
                    // no more notifications in the group, remove the key
                    continue
                }
            }
            if(messageList.isNotEmpty()) newPush[key] = messageList
        }
        pushNotifications = newPush
        return count
    }

    /**
     * Removes all the notifications, both from OS and our internal list
     */
    @Synchronized
    fun removeAllPushNotifications() {
        // get the notification manager
        val notificationManager = EntourageApplication.get().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // cancel all the notifications
        for (messageList in pushNotifications.values) {
            if (messageList.size > 0) {
                notificationManager.cancel( messageList[0].pushNotificationTag,  messageList[0].pushNotificationId)
            }
        }
        // remove all the notifications from our internal list
        pushNotifications.clear()
    }
    // ----------------------------------
    // PUSH NOTIFICATION UI HANDLING
    // ----------------------------------
    /**
     * Creates and displays a OS notification, using tag and id
     * @param message the message received
     * @param context the context
     */
    private fun displayPushNotification(message: Message, context: Context) {
        val messageList: List<Message>? = pushNotifications[message.hash]
        var count = 0
        if (messageList != null) {
            count = messageList.size
        }
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = context.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)

            // Configure the notification channel.
            notificationChannel.description = channelId
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.drawable.ic_entourage_logo_one_color)
        builder.setContentIntent(createMessagePendingIntent(message, context))
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
        builder.setContentTitle(message.getContentTitleForCount(count, context))
        builder.setContentText(message.getContentTextForCount(count, context))
        val notification = builder.build()
        notification.defaults = Notification.DEFAULT_LIGHTS
        notification.flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_SHOW_LIGHTS
        Timber.d("TAG = " + message.pushNotificationTag + " , ID = " + message.pushNotificationId)
        notificationManager.notify(message.pushNotificationTag, message.pushNotificationId, notification)
    }

    fun displayPushNotification(fcmMessage: RemoteMessage, context: Context) {
        val notif = fcmMessage.notification ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = context.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)

            // Configure the notification channel.
            notificationChannel.description = channelId
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, channelId)
        builder.setSmallIcon(R.drawable.ic_entourage_logo_one_color)
        val ctaIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fcmMessage.data[KEY_CTA]))
        builder.setContentIntent(PendingIntent.getActivity(context, 0, ctaIntent, PendingIntent.FLAG_UPDATE_CURRENT))
        builder.setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
        builder.setContentTitle(notif.title)
        builder.setContentText(notif.body)
        val notification = builder.build()
        notification.defaults = Notification.DEFAULT_LIGHTS
        notification.flags = Notification.FLAG_AUTO_CANCEL or Notification.FLAG_SHOW_LIGHTS
        notificationManager.notify(0, notification)
    }

    /**
     * Updates a group of notifications. If the group is empty, it removes the notification
     * @param key the key of the group
     * @param messageList the message list
     * @return true if the group was updated, false if it was removed
     */
    private fun updateNotificationGroup(key: String, messageList: List<Message>): Boolean {
        // get the visible messages count
        var isGroupEmpty = true
        for (message in messageList) {
            if (message.isVisible) {
                isGroupEmpty = false
                break
            }
        }
        if (isGroupEmpty) {
            // no more messages, cancel the notification
            var notificationTag: String? = null
            val notificationId: Int
            val separator = key.indexOf(Message.HASH_SEPARATOR)
            if (separator > 0) {
                notificationTag = key.substring(0, separator)
                notificationId = key.substring(separator + 1).toInt()
            } else {
                notificationId = key.toInt()
            }
            (EntourageApplication.get().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(notificationTag, notificationId)
            return false
        } else {
            displayPushNotification(messageList.last(), EntourageApplication.get())
        }
        return true
    }

    /**
     * Creates the pending intent to be used when creating the OS notification
     * @param message the message
     * @param context the content
     * @return the [PendingIntent]
     */
    private fun createMessagePendingIntent(message: Message, context: Context): PendingIntent {
        val args = Bundle()
        args.putSerializable(PUSH_MESSAGE, message)
        val messageType: String = message.content?.type ?:""
        val messageIntent = Intent(context, MainActivity::class.java)
        when (messageType) {
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST ->                 // because of the grouping, we need an intent that is specific for each entourage
                messageIntent.data = Uri.parse("entourage-notif://" + message.pushNotificationTag)
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE,
            PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED,
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION,
            PushNotificationContent.TYPE_INVITATION_STATUS -> {
            }
            else -> Timber.e("Notif has no pending intent")
        }
        messageIntent.action = messageType
        messageIntent.putExtras(args)
        return PendingIntent.getActivity(context, message.pushNotificationId, messageIntent, 0)
    }

    // ----------------------------------
    // STATIC METHODS
    // ----------------------------------
    /**
     * Creates a [Message] from the remoteMessage received from the firebase messaging server
     * @param remoteMessage remoteMessage from FirebaseMessaging
     * @return the message
     */
    fun getMessageFromRemoteMessage(remoteMessage: RemoteMessage, context: Context): Message? {
        val msg = remoteMessage.data
        //first checking if content json is present (not here for mixpanel or firebase notification
        if ( !msg.containsKey(KEY_CONTENT)) return null
        Timber.d(KEY_SENDER + "= " + msg[KEY_SENDER] + "; " + KEY_OBJECT + "= " + msg[KEY_OBJECT] + "; " + KEY_CONTENT + "= " + msg[KEY_CONTENT])
        val message = Message(msg[KEY_SENDER], msg[KEY_OBJECT], msg[KEY_CONTENT], 0, null)
        message.pushNotificationId = getNotificationId(context, message)
        message.pushNotificationTag = if (message.content != null) message.content.notificationTag else ""
        return message
    }

    /**
     * Creates a [Message] from the Intent received from the server
     * @param intent the intent with the json from the server
     * @param context the context
     * @return the message
     */
    fun getMessageFromIntent(intent: Intent, context: Context): Message? {
        val args = intent.extras ?: return null
        Timber.d(KEY_SENDER + "= " + args.getString(KEY_SENDER) + "; " + KEY_OBJECT + "= " + args.getString(KEY_OBJECT) + "; " + KEY_CONTENT + "= " + args.getString(KEY_CONTENT))
        val message = Message(args.getString(KEY_SENDER), args.getString(KEY_OBJECT), args.getString(KEY_CONTENT), 0, null)
        message.pushNotificationId = getNotificationId(context, message)
        message.pushNotificationTag = message.content.notificationTag
        return message
    }

    /**
     * Returns the notification id for the message.<br></br>
     * The NEW_CHAT_MESSAGE and TYPE_NEW_JOIN_REQUEST messages use a hardcoded id, for grouping purposes. The others use an incremental id
     * @param context the context
     * @param message the message
     * @return the notification id
     */
    private fun getNotificationId(context: Context, message: Message): Int {
        val notificationType = message.content?.type
        if (notificationType != null) {
            if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE == notificationType) {
                return CHAT_MESSAGE_NOTIFICATION_ID
            } else if (PushNotificationContent.TYPE_NEW_JOIN_REQUEST == notificationType) {
                return JOIN_REQUEST_NOTIFICATION_ID
            }
        }
        return getNextNotificationId(context)
    }

    /**
     * Returns the next notification id
     * @param context the context
     * @return the notification id
     */
    private fun getNextNotificationId(context: Context): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var id = sharedPreferences.getInt(PREFERENCE_LAST_NOTIFICATION_ID, MIN_NOTIFICATION_ID - 1) + 1
        if (id == Int.MAX_VALUE) {
            id = MIN_NOTIFICATION_ID
        }
        sharedPreferences.edit().putInt(PREFERENCE_LAST_NOTIFICATION_ID, id).apply()
        return id
    }
}