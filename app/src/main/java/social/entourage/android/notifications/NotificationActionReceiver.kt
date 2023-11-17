package social.entourage.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.gson.Gson
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.tools.log.AnalyticsEvents

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when (intent.action) {
            ACTION_CLICKED -> {
                // Gérer l'action de clic sur la notification
                val notificationContent = intent.getStringExtra("notification_content") // null si l'extra "notification_content" n'existe pas

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
            ACTION_DISMISSED -> {
                // Gérer l'action de swipe pour supprimer la notification
                val notificationContent = intent.getStringExtra("notification_content") // null si l'extra "notification_content" n'existe pas
                try {
                    val pushNotif = Gson().fromJson(notificationContent, PushNotificationContent::class.java)
                    val instance = pushNotif.extra?.instance
                }catch (e:Exception){

                }

            }
        }
    }

    companion object {
        const val ACTION_CLICKED = "ACTION_CLICKED"
        const val ACTION_DISMISSED = "ACTION_DISMISSED"

    }
}