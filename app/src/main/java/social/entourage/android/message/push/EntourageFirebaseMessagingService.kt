package social.entourage.android.message.push

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import social.entourage.android.tools.log.EntourageEvents
import social.entourage.android.api.tape.Events.OnGCMTokenObtainedEvent
import social.entourage.android.api.tape.Events.OnPushNotificationReceived
import social.entourage.android.tools.EntBus

class EntourageFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(registrationId: String) {
        super.onNewToken(registrationId)
        EntBus.register(this)
        EntBus.post(OnGCMTokenObtainedEvent(registrationId))
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_RECEIVED)
        if (remoteMessage.data.isNotEmpty()) {
            //we always provide some extra data in our push notif
            remoteMessage.data[KEY_CTA]?.let { cta ->
                EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_FCM_RECEIVED)
                remoteMessage.notification?.let {handleFCM(cta, it) }
                //nothing to do right now
            } ?: run  {
                //entourage own notif, need to check the message to see what to do right now
                EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_ENTOURAGE_RECEIVED)
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
        EntBus.post(OnPushNotificationReceived(message))
    }

    companion object {
        const val TAG = "EntourageFirebaseMessagingService"
        const val KEY_CTA = "entourage_cta"
    }
}