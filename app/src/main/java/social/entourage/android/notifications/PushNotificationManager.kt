package social.entourage.android.notifications

import android.annotation.SuppressLint
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
import android.preference.PreferenceManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.welcome.WelcomeFiveActivity
import social.entourage.android.welcome.WelcomeFourActivity
import social.entourage.android.welcome.WelcomeOneActivity
import social.entourage.android.welcome.WelcomeThreeActivity
import social.entourage.android.welcome.WelcomeTwoActivity
import timber.log.Timber
import java.util.*

/**
 * Singleton that handles push notifications
 */
object PushNotificationManager {
    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    const val PUSH_MESSAGE = "social.entourage.android.PUSH_MESSAGE"
    const val KEY_SENDER = "sender"
    const val KEY_OBJECT = "object"
    const val KEY_CONTENT = "content"
    const val PREFERENCE_LAST_NOTIFICATION_ID = "PREFERENCE_LAST_NOTIFICATION_ID"
    private const val MIN_NOTIFICATION_ID = 10000

    const val ACTION_OPEN_NOTIFICATION = "social.entourage.android.ACTION_OPEN_NOTIFICATION"

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var pushNotifications: MutableMap<String, MutableList<PushNotificationMessage>> = HashMap()

    // ----------------------------------
    // INTERNAL PUSH NOTIFICATIONS HANDLING
    // ----------------------------------
    /**
     * Handle a push notification received from the server
     * @param pushNotificationMessage The pushNotificationMessage that we use to build the push notification
     * @param context The context into which to add the push notification
     */
    fun handlePushNotification(pushNotificationMessage: PushNotificationMessage, context: Context) {
        if(pushNotificationMessage.content!= null) {
            addPushNotification(pushNotificationMessage)
            EntourageApplication.get().addPushNotification(pushNotificationMessage)
            // Display all notifications except the join_request_canceled
            displayPushNotification(pushNotificationMessage, context)
        }
        EntourageApplication.get().onPushNotificationReceived(pushNotificationMessage)
    }

    /**
     * Adds a pushNotificationMessage to our internal list, creating the group if necessary
     * @param pushNotificationMessage the pushNotificationMessage to add
     */
    @Synchronized
    fun addPushNotification(pushNotificationMessage: PushNotificationMessage) {
        val messageList = pushNotifications[pushNotificationMessage.hash] ?: ArrayList()
        messageList.add(pushNotificationMessage)
        pushNotifications[pushNotificationMessage.hash] = messageList
    }

    /**
     * Removes a notification from our internal list
     * @param msg The message to remove
     * @return the number of push notifications that were removed
     */
    @Synchronized
    fun removePushNotification(msg: PushNotificationMessage): Int {
        var nbMsgFound = 0
        val newPushNotifications = HashMap<String, MutableList<PushNotificationMessage>>()
        for (key in pushNotifications.keys) {
            pushNotifications[key]?.let { messageList ->
                val newPushNotificationMessageList = ArrayList<PushNotificationMessage>()
                for (message in messageList) {
                    if (message.hash == msg.hash) {
                        nbMsgFound++
                        continue
                    }
                    newPushNotificationMessageList.add(message)
                }
                if (newPushNotificationMessageList.isNotEmpty()) newPushNotifications[key] = newPushNotificationMessageList
            }
        }
        pushNotifications = newPushNotifications
        return nbMsgFound
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
        var count = 0
        // search for a push notification that matches our parameters
        val newPushNotifications = HashMap<String, MutableList<PushNotificationMessage>>()
        for(key in pushNotifications.keys) {
            pushNotifications[key]?.let { oldMessageList ->
                val newPushNotificationMessageList = ArrayList<PushNotificationMessage>()
                var messageListChanged = false
                for (message in oldMessageList) {
                    val content = message.content
                    if (content != null && content.joinableId == feedId && content.type == pushType) {
                        if (TimestampedObject.ENTOURAGE_CARD == feedType && content.isEntourageRelated) {
                            messageListChanged = true
                            if (message.isVisible) {
                                count++
                            }
                            continue
                        }
                    }
                    newPushNotificationMessageList.add(message)
                }
                if (!messageListChanged
                        || updateNotificationGroup(key, newPushNotificationMessageList)
                        || newPushNotificationMessageList.isNotEmpty()) {
                    // list not empty we keep it
                    newPushNotifications[key] = newPushNotificationMessageList
                }
            }
        }
        pushNotifications = newPushNotifications
        return count
    }

    /**
     * Removes all the notifications
     * @return the number of push notifications that were removed
     */
    @Synchronized
    fun removeAllPushNotifications(): Int {
        var count = 0
        for (messageList in pushNotifications.values) {
            count += messageList.size
        }
        pushNotifications.clear()
        NotificationManagerCompat.from(EntourageApplication.get()).cancelAll()
        return count
    }

    /**
     * Updates a group of notifications.
     * Use this if the messages that are grouped need to be updated, for example, for the summmary
     * @param key the key of the group
     * @param pushNotificationMessageList the list of messages in the group
     * @return true if the group was updated
     */
    private fun updateNotificationGroup(key: String, pushNotificationMessageList: List<PushNotificationMessage>): Boolean {
        // Validation
        if (pushNotificationMessageList.isEmpty()) {
            return false
        }
        val count = pushNotificationMessageList.size
        // get the last message
        val lastMessage = pushNotificationMessageList[count - 1]
        // retrieve the context
        val context = EntourageApplication.get()
        // update the notification
        val channelId = context.getString(R.string.app_name)
        val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_entourage_logo_one_color)
                .setStyle(NotificationCompat.BigTextStyle())
                .setContentIntent(createMessagePendingIntent(lastMessage, context))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
                .setContentTitle(lastMessage.getContentTitleForCount(count, context))
                .setContentText(lastMessage.getContentTextForCount(count, context))
                .setColor(ResourcesCompat.getColor(context.resources,R.color.accent,null))
        val notification = builder.build()
        notification.defaults = NotificationCompat.DEFAULT_LIGHTS
        notification.flags = NotificationCompat.FLAG_AUTO_CANCEL or NotificationCompat.FLAG_SHOW_LIGHTS
        NotificationManagerCompat.from(context).notify(lastMessage.pushNotificationTag, lastMessage.pushNotificationId, notification)
        return true
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    /**
     * Creates and displays a OS notification, using tag and id
     * @param pushNotificationMessage the pushNotificationMessage received
     * @param context the context
     */
    @SuppressLint("MissingPermission")
    private fun displayPushNotification(pushNotificationMessage: PushNotificationMessage, context: Context) {
        val pushNotificationMessageList: List<PushNotificationMessage>? = pushNotifications[pushNotificationMessage.hash]
        val count = pushNotificationMessageList?.size ?: 0
        if(count>0) {
            pushNotificationMessageList?.firstOrNull()?.let {pushNotificationMessage.pushNotificationId = it.pushNotificationId}
        }
        val pushNotifString = Gson().toJson(pushNotificationMessage)
        val pushNotif = Gson().fromJson(pushNotifString, PushNotificationContent::class.java)
        pushNotif.extra?.stage.let {
            if(it.equals("h1")){AnalyticsEvents.logEventWithContext(context, AnalyticsEvents.NotificationReceived__OfferHelp__WDay1)}
            if(it.equals("j2")){AnalyticsEvents.logEventWithContext(context, AnalyticsEvents.NotificationReceived__OfferHelp__WDay2)}
            if(it.equals("j5")){AnalyticsEvents.logEventWithContext(context, AnalyticsEvents.NotificationReceived__OfferHelp__WDay5)}
            if(it.equals("j!")){AnalyticsEvents.logEventWithContext(context, AnalyticsEvents.NotificationReceived__OfferHelp__WDay8)}
            if(it.equals("j11")){AnalyticsEvents.logEventWithContext(context, AnalyticsEvents.NotificationReceived__OfferHelp__WDay11)}
        }
        pushNotif.extra?.tracking.let {
            if(it.equals("join_request_on_create")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__MemberEvent)}
            if(it.equals("outing_on_update")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__ModifiedEvent)}
            if(it.equals("outing_on_create")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__PostEvent)}
            if(it.equals("post_on_create_to_neighborhood")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__PostGroup)}
            if(it.equals("comment_on_create_to_neighborhood")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__CommentGroup)}
            if(it.equals("comment_on_create_to_outing")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__CommentEvent)}
            if(it.equals("outing_on_add_to_neighborhood")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__EventInGroup)}
            if(it.equals("contribution_on_create")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__Contribution)}
            if(it.equals("solicitation_on_create")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__Demand)}
            if(it.equals("private_chat_message_on_create")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__PrivateMessage)}
            if(it.equals("join_request_on_create_to_neighborhood")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__MemberGroup)}
            if(it.equals("join_request_on_create_to_outing")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__MemberEvent)}
            if(it.equals("outing_on_cancel")){
                AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__CanceledEvent)}
        }


        // CLICKED Intent
        val clickedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CLICKED
            putExtra("notification_content", Gson().toJson(pushNotificationMessage))
        }

        // DISMISSED Intent
        val dismissedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISMISSED
            putExtra("notification_content", Gson().toJson(pushNotificationMessage))
        }

        val requestCode = pushNotificationMessage.pushNotificationId

        // Note: dismissedPendingIntent is used, but clickedPendingIntent is NOT used in setContentIntent below.
        // Instead, createMessagePendingIntent is used.
        val dismissedPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, dismissedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = context.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = channelId
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_entourage_logo_one_color)
                .setStyle(NotificationCompat.BigTextStyle())
                .setContentIntent(createMessagePendingIntent(pushNotificationMessage, context))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
                .setContentTitle(pushNotificationMessage.getContentTitleForCount(count, context))
                .setContentText(pushNotificationMessage.getContentTextForCount(count, context))
                .setColor(ResourcesCompat.getColor(context.resources,R.color.accent,null))
                .setDeleteIntent(dismissedPendingIntent)

        val notification = builder.build()
        notification.defaults = NotificationCompat.DEFAULT_LIGHTS
        notification.flags = NotificationCompat.FLAG_AUTO_CANCEL or NotificationCompat.FLAG_SHOW_LIGHTS
        Timber.d("TAG = %s , ID = %d", pushNotificationMessage.pushNotificationTag, pushNotificationMessage.pushNotificationId)
        NotificationManagerCompat.from(context).notify(pushNotificationMessage.pushNotificationTag, pushNotificationMessage.pushNotificationId, notification)
    }

    @SuppressLint("MissingPermission")
    fun displayFCMPushNotification(fcmCTA:String, fcmTitle: String?, fcmBody: String?, context: Context) {
        val channelId = context.getString(R.string.app_name)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.description = channelId
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
        }
        val ctaIntent = Intent(Intent.ACTION_VIEW, Uri.parse(fcmCTA))
        val builder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_entourage_logo_one_color)
                .setContentIntent(PendingIntent.getActivity(context, 0, ctaIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
                .setContentTitle(fcmTitle)
                .setContentText(fcmBody)
                .setColor(ResourcesCompat.getColor(context.resources,R.color.accent,null))

        val notification = builder.build()
        notification.defaults = NotificationCompat.DEFAULT_LIGHTS
        notification.flags = NotificationCompat.FLAG_AUTO_CANCEL or NotificationCompat.FLAG_SHOW_LIGHTS
        NotificationManagerCompat.from(context).notify("FCM", 0, notification)
    }

    /**
     * Creates the pending intent to be used when creating the OS notification
     * REFACTORED: Now always returns an Intent to MainActivity with ACTION_OPEN_NOTIFICATION.
     * @param pushNotificationMessage the pushNotificationMessage
     * @param context the content
     * @return the [PendingIntent]
     */
    private fun createMessagePendingIntent(pushNotificationMessage: PushNotificationMessage, context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = ACTION_OPEN_NOTIFICATION

        // Pass the raw content as JSON
        intent.putExtra("notification_content", Gson().toJson(pushNotificationMessage.content))

        return PendingIntent.getActivity(
            context,
            pushNotificationMessage.pushNotificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Creates a [PushNotificationMessage] from the remoteMessage received from the firebase messaging server
     * @param remoteMessage remoteMessage from FirebaseMessaging
     * @return the message
     */
    fun getPushNotificationMessageFromRemoteMessage(remoteMessage: RemoteMessage, context: Context): PushNotificationMessage? {
        val msg = remoteMessage.data
        //first checking if content json is present (not here for firebase notification
        val content = msg[KEY_CONTENT] ?: return null
        val sender = msg[KEY_SENDER] ?: return null
        val pushNotificationMessage = PushNotificationMessage(sender, msg[KEY_OBJECT], content, 0, null)
        pushNotificationMessage.pushNotificationId = getNotificationId(context)
        pushNotificationMessage.pushNotificationTag = pushNotificationMessage.content?.notificationTag ?: ""
        return pushNotificationMessage
    }

    /**
     * Returns a unique notification id for the pushNotificationMessage.<br></br>
     * @param context the context
     * @param pushNotificationMessage the pushNotificationMessage
     * @return the notification id
     */
    private fun getNotificationId(context: Context): Int {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        var id = sharedPreferences.getInt(PREFERENCE_LAST_NOTIFICATION_ID, MIN_NOTIFICATION_ID - 1) + 1
        if (id == Int.MAX_VALUE) {
            id = MIN_NOTIFICATION_ID
        }
        sharedPreferences.edit { putInt(PREFERENCE_LAST_NOTIFICATION_ID, id) }
        return id
    }
}
