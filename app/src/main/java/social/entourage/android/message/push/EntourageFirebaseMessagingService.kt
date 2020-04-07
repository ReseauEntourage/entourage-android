package social.entourage.android.message.push

import com.google.firebase.messaging.RemoteMessage
import com.mixpanel.android.mpmetrics.MixpanelFCMMessagingService
import social.entourage.android.EntourageApplication
import social.entourage.android.EntourageEvents
import social.entourage.android.api.tape.Events.OnGCMTokenObtainedEvent
import social.entourage.android.api.tape.Events.OnPushNotificationReceived
import social.entourage.android.tools.BusProvider

class EntourageFirebaseMessagingService : MixpanelFCMMessagingService() {
    override fun onNewToken(registrationId: String) {
        super.onNewToken(registrationId)
        EntourageApplication.get().mixpanel.people.pushRegistrationId = registrationId
        BusProvider.getInstance().register(this)
        BusProvider.getInstance().post(OnGCMTokenObtainedEvent(registrationId))
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_RECEIVED)
        if (remoteMessage.data.isNotEmpty()) {
            //we always provide some extra data in our push notif
            when {
                remoteMessage.data.containsKey(PushNotificationManager.KEY_MIXPANEL) -> {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_MIXPANEL_RECEIVED)
                    super.onMessageReceived(remoteMessage)
                }
                remoteMessage.data.containsKey(PushNotificationManager.KEY_CTA) -> {
                    EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_FCM_RECEIVED)
                    handleFCM(remoteMessage)
                    //nothing to do right now
                }
                else -> {
                    //entourage own notif, need to check the message to see what to do right now
                    EntourageEvents.logEvent(EntourageEvents.EVENT_NOTIFICATION_ENTOURAGE_RECEIVED)
                    handleNow(remoteMessage)
                }
            }
        }
    }

    private fun handleFCM(remoteMessage: RemoteMessage) {
        PushNotificationManager.displayPushNotification(remoteMessage, this)
    }

    private fun handleNow(remoteMessage: RemoteMessage) {
        val message = PushNotificationManager.getMessageFromRemoteMessage(remoteMessage, this) ?: return
        PushNotificationManager.handlePushNotification(message, this)
        BusProvider.getInstance().post(OnPushNotificationReceived(message))
    }

    companion object {
        const val TAG = "EntourageFirebaseMessagingService"
    }
}