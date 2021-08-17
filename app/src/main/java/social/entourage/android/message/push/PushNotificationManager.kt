package social.entourage.android.message.push

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
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.RemoteMessage
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
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

    private const val MIN_NOTIFICATION_ID = 40
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
        val application = EntourageApplication.get()
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
        val newPushNotifications =  HashMap<String, MutableList<Message>>()
        for(key in pushNotifications.keys) {
            pushNotifications[key]?.let {messageList->
                var messageListChanged = false
                val newMessageList = ArrayList<Message>()
                for(message in messageList) {
                    val content = message.content
                    if (content != null && content.joinableId == feedItemId) {
                        if((TimestampedObject.TOUR_CARD == feedType && content.isTourRelated)
                                || (TimestampedObject.ENTOURAGE_CARD == feedType && content.isEntourageRelated)){
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
                if (!messageListChanged
                        ||updateNotificationGroup(key, newMessageList)
                        ||newMessageList.isNotEmpty()) {
                    // list not empty we keep it
                    newPushNotifications[key] = newMessageList
                }
            }
        }
        pushNotifications = newPushNotifications
        return nbNotifsFound
    }

    /**
     * Removes a notification from our internal list
     * @param msg The message to remove
     * @return the number of push notifications that were removed
     */
    @Synchronized
    fun removePushNotification(msg: Message): Int {
        var nbMsgFound = 0
        val newPushNotifications = HashMap<String, MutableList<Message>>()
        for (key in pushNotifications.keys) {
            pushNotifications[key]?.let { messageList ->
                val newMessageList = ArrayList<Message>()
                for (message in messageList) {
                    if (message.hash == msg.hash) {
                        nbMsgFound++
                        continue
                    }
                    newMessageList.add(message)
                }
                if (newMessageList.isNotEmpty()) newPushNotifications[key] = newMessageList
            }
        }
        pushNotifications = newPushNotifications
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
        val application = EntourageApplication.get()
        var count = 0
        // search for a push notification that matches our parameters
        val newPushNotifications = HashMap<String, MutableList<Message>>()
        for(key in pushNotifications.keys) {
            pushNotifications[key]?.let { oldMessageList ->
                val newMessageList = ArrayList<Message>()
                var messageListChanged = false
                for (message in oldMessageList) {
                    val content = message.content
                    if (content != null && content.joinableId == feedId && content.type == pushType) {
                        if (TimestampedObject.TOUR_CARD == feedType && content.isTourRelated
                                || TimestampedObject.ENTOURAGE_CARD == feedType && content.isEntourageRelated) {
                            messageListChanged = true
                            if (message.isVisible) {
                                application.storeNewPushNotification(message, false)
                                count++
                            }
                            continue
                        }
                    }
                    newMessageList.add(message)
                }
                if (!messageListChanged
                        ||updateNotificationGroup(key, newMessageList)
                        ||newMessageList.isNotEmpty()) {
                    newPushNotifications[key] = newMessageList
                }
            }
        }
        pushNotifications = newPushNotifications
        return count
    }

    /**
     * Removes all the notifications, both from OS and our internal list
     */
    @Synchronized
    fun removeAllPushNotifications() {
        // cancel all the notifications
        for (messageList in pushNotifications.values) {
            if (messageList.isEmpty()) continue
            NotificationManagerCompat.from(EntourageApplication.get()).cancel(messageList[0].pushNotificationTag,  messageList[0].pushNotificationId)
        }
        // remove all the notifications from our internal list
        pushNotifications.clear()
        NotificationManagerCompat.from(EntourageApplication.get()).cancelAll()
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
        val count = messageList?.size ?: 0
        if(count>0) {
            messageList?.first()?.let {message.pushNotificationId = it.pushNotificationId}
        }

        val channelId = context.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)

            // Configure the notification channel.
            notificationChannel.description = channelId
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_entourage_logo_one_color)
                .setContentIntent(createMessagePendingIntent(message, context))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
                .setContentTitle(message.getContentTitleForCount(count, context))
                .setContentText(message.getContentTextForCount(count, context))

        val notification = builder.build()
        notification.defaults = NotificationCompat.DEFAULT_LIGHTS
        notification.flags = NotificationCompat.FLAG_AUTO_CANCEL or NotificationCompat.FLAG_SHOW_LIGHTS
        Timber.d("TAG = %s , ID = %d", message.pushNotificationTag, message.pushNotificationId)
        NotificationManagerCompat.from(context).notify(message.pushNotificationTag, message.pushNotificationId, notification)
    }

    fun displayFCMPushNotification(fcmCTA:String, fcmTitle: String?, fcmBody: String?, context: Context) {
        val channelId = context.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)

            // Configure the notification channel.
            notificationChannel.description = channelId
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
        }
        val ctaIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fcmCTA))
        val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_entourage_logo_one_color)
                .setContentIntent(PendingIntent.getActivity(context, 0, ctaIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
                .setContentTitle(fcmTitle)
                .setContentText(fcmBody)
        val notification = builder.build()
        notification.defaults = NotificationCompat.DEFAULT_LIGHTS
        notification.flags = NotificationCompat.FLAG_AUTO_CANCEL or NotificationCompat.FLAG_SHOW_LIGHTS
        NotificationManagerCompat.from(context).notify(0, notification)
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
            NotificationManagerCompat.from(EntourageApplication.get()).cancel(notificationTag, notificationId)
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
        //first checking if content json is present (not here for firebase notification
        val content = msg[KEY_CONTENT] ?: return null
        val sender = msg[KEY_SENDER] ?: return null
        val message = Message(sender, msg[KEY_OBJECT], content, 0, null)
        message.pushNotificationId = getNotificationId(context, message)
        message.pushNotificationTag = message.content?.notificationTag ?: ""
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
        val content = args.getString(KEY_CONTENT) ?: return null
        val sender = args.getString(KEY_SENDER) ?: return null
        val message = Message(sender, args.getString(KEY_OBJECT), content, 0, null)
        message.pushNotificationId = getNotificationId(context, message)
        message.pushNotificationTag = message.content?.notificationTag
        return message
    }

    /**
     * Returns a unique notification id for the message.<br></br>
     * @param context the context
     * @param message the message
     * @return the notification id
     */
    private fun getNotificationId(context: Context, message: Message): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var id = sharedPreferences.getInt(PREFERENCE_LAST_NOTIFICATION_ID, MIN_NOTIFICATION_ID - 1) + 1
        if (id == Int.MAX_VALUE) {
            id = MIN_NOTIFICATION_ID
        }
        sharedPreferences.edit().putInt(PREFERENCE_LAST_NOTIFICATION_ID, id).apply()
        return id
    }
}