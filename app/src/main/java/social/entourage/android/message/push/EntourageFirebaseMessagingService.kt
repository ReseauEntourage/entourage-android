package social.entourage.android.message.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import social.entourage.android.EntourageApplication
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.EntBus

class EntourageFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
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
        PushNotificationManager.displayFCMPushNotification(cta, fcmMessageNotif.title, fcmMessageNotif.body, this)
    }

    private fun handleNow(remoteMessage: RemoteMessage) {
        val message = PushNotificationManager.getMessageFromRemoteMessage(remoteMessage, this) ?: return
        PushNotificationManager.handlePushNotification(message, this)
        EntourageApplication.get().onPushNotificationReceived(message)
    }

    companion object {
        const val TAG = "EntourageFirebaseMessagingService"
        const val KEY_CTA = "entourage_cta"
    }
}