package social.entourage.android.notifications

import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.home.NextStepPresenter
import social.entourage.android.tools.log.AnalyticsEvents

class EntourageFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_NOTIFICATION_RECEIVED)
        if (remoteMessage.data.isNotEmpty()) {
            // Handle next_step push notification type
            if (remoteMessage.data["type"] == KEY_TYPE_NEXT_STEP) {
                handleNextStepPush()
                return
            }

            //we always provide some extra data in our push notif
            remoteMessage.data[KEY_CTA]?.let { cta ->
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_NOTIFICATION_FCM_RECEIVED)
                remoteMessage.notification?.let {handleFCM(cta, it) }
                //nothing to do right now
            } ?: run  {
                //entourage own notif, need to check the message to see what to do right now
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_NOTIFICATION_ENTOURAGE_RECEIVED)
                handleNow(remoteMessage)
            }
        }
    }

    private fun handleFCM(cta: String, fcmMessageNotif: RemoteMessage.Notification) {
        PushNotificationManager.displayFCMPushNotification(
            cta,
            fcmMessageNotif.title,
            fcmMessageNotif.body,
            this
        )
    }

    private fun handleNow(remoteMessage: RemoteMessage) {
        PushNotificationManager.getPushNotificationMessageFromRemoteMessage(remoteMessage, this)?.let { message ->
            PushNotificationManager.handlePushNotification(message, this)
        }
    }

    private fun handleNextStepPush() {
        // Track the push tap (fire and forget)
        NextStepPresenter().tapPush()

        // Route to the home screen (MainActivity)
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(KEY_EXTRA_NEXT_STEP_PUSH, true)
        }
        startActivity(intent)
    }

    companion object {
        const val TAG = "EntourageFirebaseMessagingService"
        const val KEY_CTA = "entourage_cta"
        const val KEY_TYPE_NEXT_STEP = "next_step"
        const val KEY_EXTRA_NEXT_STEP_PUSH = "next_step_push"
    }
}