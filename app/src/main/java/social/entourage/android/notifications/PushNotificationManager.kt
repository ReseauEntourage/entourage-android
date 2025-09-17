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
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.preference.PreferenceManager
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.notifications.NotificationActionReceiver.Companion.ACTION_CLICKED
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.welcome.*
import timber.log.Timber

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
    private var pushNotifications = HashMap<String, MutableList<PushNotificationMessage>>()
    // ------------------------------------
    // INTERNAL PUSH NOTIFICATIONS HANDLING
    // ------------------------------------
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
     * Removes the notifications for a feed item, updating the groups if necessary
     * @param feedItem the feed item from which to remove the notifications
     * @return the number of push notifications that were removed
     */
    /*@Synchronized
    fun removePushNotificationsForFeedItem(feedItem: FeedItem): Int {
        val feedItemId = feedItem.id
        val feedType = feedItem.type
        var nbNotifsFound = 0
        val newPushNotifications =  HashMap<String, MutableList<PushNotificationMessage>>()
        for(key in pushNotifications.keys) {
            pushNotifications[key]?.let { messageList->
                var messageListChanged = false
                val newPushNotificationMessageList = ArrayList<PushNotificationMessage>()
                for(message in messageList) {
                    val content = message.content
                    if (content != null && content.joinableId == feedItemId) {
                        if((TimestampedObject.ENTOURAGE_CARD == feedType && content.isEntourageRelated)){
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
                    newPushNotificationMessageList.add(message)
                }
                if (!messageListChanged
                        || updateNotificationGroup(key, newPushNotificationMessageList)
                        ||newPushNotificationMessageList.isNotEmpty()) {
                    // list not empty we keep it
                    newPushNotifications[key] = newPushNotificationMessageList
                }
            }
        }
        pushNotifications = newPushNotifications
        return nbNotifsFound
    }*/

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
     * @param feedItem the feed item
     * @param userId not used
     * @param pushType the required type
     * @return the number of push notifications that were removed
     */
    /*@Synchronized
    fun removePushNotification(feedItem: FeedItem, userId: Int, pushType: String): Int {
        return removePushNotification(feedItem.id, feedItem.type, userId, pushType)
    }*/

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
                        ||newPushNotificationMessageList.isNotEmpty()) {
                    newPushNotifications[key] = newPushNotificationMessageList
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



        val clickedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CLICKED
            putExtra("notification_content", Gson().toJson(pushNotificationMessage))
        }


        val dismissedIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_DISMISSED
            putExtra("notification_content", Gson().toJson(pushNotificationMessage)) // ou n'importe quelle autre information que vous voulez passer
        }

        val requestCode = pushNotificationMessage.pushNotificationId
        val clickedPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, clickedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissedPendingIntent = PendingIntent.getBroadcast(
            context, requestCode, dismissedIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

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
                .setStyle(NotificationCompat.BigTextStyle())
                .setContentIntent(createMessagePendingIntent(pushNotificationMessage, context))
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_entourage_logo_two_colors))
                .setContentTitle(pushNotificationMessage.getContentTitleForCount(count, context))
                .setContentText(pushNotificationMessage.getContentTextForCount(count, context))
                .setColor(ResourcesCompat.getColor(context.resources,R.color.accent,null))
                .setDeleteIntent(dismissedPendingIntent) // Ajoutez le PendingIntent pour l'action de suppression

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

            // Configure the notification channel.
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
        NotificationManagerCompat.from(context).notify(0, notification)
    }

    /**
     * Updates a group of notifications. If the group is empty, it removes the notification
     * @param key the key of the group
     * @param pushNotificationMessageList the message list
     * @return true if the group was updated, false if it was removed
     */
    private fun updateNotificationGroup(key: String, pushNotificationMessageList: List<PushNotificationMessage>): Boolean {
        // get the visible messages count
        var isGroupEmpty = true
        for (message in pushNotificationMessageList) {
            if (message.isVisible) {
                isGroupEmpty = false
                break
            }
        }
        if (isGroupEmpty) {
            // no more messages, cancel the notification
            var notificationTag: String? = null
            val notificationId: Int
            val separator = key.indexOf(PushNotificationMessage.HASH_SEPARATOR)
            if (separator > 0) {
                notificationTag = key.substring(0, separator)
                notificationId = key.substring(separator + 1).toInt()
            } else {
                notificationId = key.toInt()
            }
            NotificationManagerCompat.from(EntourageApplication.get()).cancel(notificationTag, notificationId)
            return false
        } else {
            displayPushNotification(pushNotificationMessageList.last(), EntourageApplication.get())
        }
        return true
    }



    /**
     * Creates the pending intent to be used when creating the OS notification
     * @param pushNotificationMessage the pushNotificationMessage
     * @param context the content
     * @return the [PendingIntent]
     */
    private fun createMessagePendingIntent(pushNotificationMessage: PushNotificationMessage, context: Context): PendingIntent {
        val args = Bundle()
        args.putSerializable(PUSH_MESSAGE, pushNotificationMessage)
        val messageType: String = pushNotificationMessage.content?.type ?:""

        if(pushNotificationMessage.content?.extra?.stage == "h1"){
            val intent = Intent(context, WelcomeOneActivity::class.java)
            intent.putExtra("notification_content", Gson().toJson(pushNotificationMessage.content))
            return PendingIntent.getActivity(context, pushNotificationMessage.pushNotificationId, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        if(pushNotificationMessage.content?.extra?.stage == "j2"){
            val intent = Intent(context, WelcomeTwoActivity::class.java)
            intent.putExtra("notification_content", Gson().toJson(pushNotificationMessage.content))
            intent.putExtra("notification_content_boolean", true)
            return PendingIntent.getActivity(context, pushNotificationMessage.pushNotificationId, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        if(pushNotificationMessage.content?.extra?.stage == "j5"){
            val intent = Intent(context, WelcomeThreeActivity::class.java)
            intent.putExtra("notification_content", Gson().toJson(pushNotificationMessage.content))
            return PendingIntent.getActivity(context, pushNotificationMessage.pushNotificationId, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        if(pushNotificationMessage.content?.extra?.stage == "j8"){
            val intent = Intent(context, WelcomeFourActivity::class.java)
            intent.putExtra("notification_content", Gson().toJson(pushNotificationMessage.content))
            return PendingIntent.getActivity(context, pushNotificationMessage.pushNotificationId, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        if(pushNotificationMessage.content?.extra?.stage == "j11"){
            val intent = Intent(context, WelcomeFiveActivity::class.java)
            intent.putExtra("notification_content", Gson().toJson(pushNotificationMessage.content))
            return PendingIntent.getActivity(context, pushNotificationMessage.pushNotificationId, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        val instance = pushNotificationMessage.content?.extra?.instance
        val tracking = pushNotificationMessage.content?.extra?.tracking
        Timber.wtf("wtf instance = $instance")
        Timber.wtf("wtf tracking = $tracking")
        val isDiscussionTracking = tracking in listOf(
            "public_chat_message_on_create",
            "post_on_create_to_outing",
            "post_on_create",
            "comment_on_create_to_outing",
            "comment_on_create",
            "chat_message_on_mention",
            "reaction_on_create",
            "survey_response_on_create"
        )
        if ((instance == "outings" || instance == "outing") && isDiscussionTracking) {
            DetailConversationActivity.isSmallTalkMode = false
            val intent = Intent(context, DetailConversationActivity::class.java).apply {
                putExtras(
                    bundleOf(
                        Const.ID to pushNotificationMessage.content?.joinableId?.toInt(), // ou .toLong() selon ton implémentation
                        Const.SHOULD_OPEN_KEYBOARD to false,
                        Const.IS_CONVERSATION_1TO1 to true, // à adapter selon besoin
                        Const.IS_MEMBER to true,
                        Const.IS_CONVERSATION to true,
                        Const.HAS_TO_SHOW_MESSAGE to true, // à adapter selon besoin
                        "notification_content" to Gson().toJson(pushNotificationMessage.content)
                    )
                )
            }

            return PendingIntent.getActivity(
                context,
                pushNotificationMessage.pushNotificationId,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        }

        val messageIntent = Intent(context, MainActivity::class.java)
        when (messageType) {
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST ->                 // because of the grouping, we need an intent that is specific for each entourage
                messageIntent.data = Uri.parse("entourage-notif://" + pushNotificationMessage.pushNotificationTag)
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE,
            PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED,
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION,
            PushNotificationContent.TYPE_INVITATION_STATUS -> {
            }
            else -> Timber.i("Notif has no pending intent")
        }
        messageIntent.action = messageType
        messageIntent.putExtras(args)
        messageIntent.putExtra("notification_content", Gson().toJson(pushNotificationMessage.content))
        return PendingIntent.getActivity(context, pushNotificationMessage.pushNotificationId, messageIntent, PendingIntent.FLAG_IMMUTABLE)
    }

    // ----------------------------------
    // STATIC METHODS
    // ----------------------------------
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
        sharedPreferences.edit().putInt(PREFERENCE_LAST_NOTIFICATION_ID, id).apply()
        return id
    }

    private fun doTracking(notificationContent:String){
        try {
            val pushNotif = Gson().fromJson(notificationContent, PushNotificationContent::class.java)
            val stage = pushNotif.extra?.stage
            if(stage.equals("h1")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay1)}
            if(stage.equals("j2")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay2)}
            if(stage.equals("j5")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay5)}
            if(stage.equals("j!")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay8)}
            if(stage.equals("j11")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__OfferHelp__WDay11)}
            val tracking = pushNotif.extra?.tracking
            if(tracking != null) {
                if(tracking.equals("join_request_on_create")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberEvent)}
                if(tracking.equals("outing_on_update")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__ModifiedEvent)}
                if(tracking.equals("outing_on_create")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PostEvent)}
                if(tracking.equals("post_on_create_to_neighborhood")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PostGroup)}
                if(tracking.equals("comment_on_create_to_neighborhood")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CommentGroup)}
                if(tracking.equals("comment_on_create_to_outing")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CommentEvent)}
                if(tracking.equals("outing_on_add_to_neighborhood")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__EventInGroup)}
                if(tracking.equals("contribution_on_create")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__Contribution)}
                if(tracking.equals("solicitation_on_create")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__Demand)}
                if(tracking.equals("private_chat_message_on_create")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__PrivateMessage)}
                if(tracking.equals("join_request_on_create_to_neighborhood")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberGroup)}
                if(tracking.equals("join_request_on_create_to_outing")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__MemberEvent)}
                if(tracking.equals("outing_on_cancel")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationClicked__CanceledEvent)}
                if(tracking.equals("post_on_create_to_outing")){AnalyticsEvents.logEvent(AnalyticsEvents.NotificationReceived__PostEvent)}
                if(tracking.equals("public_chat_message_on_create")){AnalyticsEvents.logEvent("UNDEFINED_PUSH_TRACKING")}
            }
        }catch (e:Exception){

        }
    }
}