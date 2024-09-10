package social.entourage.android.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.AnalyticsEvents

class EntourageFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.wtf("wtf notif" , "received notif")
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_NOTIFICATION_RECEIVED)
        if (remoteMessage.data.isNotEmpty()) {
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

    companion object {
        const val TAG = "EntourageFirebaseMessagingService"
        const val KEY_CTA = "entourage_cta"
    }
}