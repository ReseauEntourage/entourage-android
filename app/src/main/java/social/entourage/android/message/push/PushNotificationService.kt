package social.entourage.android.message.push

import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import social.entourage.android.api.tape.Events.OnPushNotificationReceived
import social.entourage.android.tools.BusProvider

object PushNotificationService : JobIntentService() {
    override fun onHandleWork(intent: Intent) {
        val message = PushNotificationManager.getMessageFromIntent(intent, applicationContext) ?: return
        PushNotificationManager.handlePushNotification(message, this)
        BusProvider.getInstance().post(OnPushNotificationReceived(message))
    }
    /**
     * Unique job ID for this service.
     */
    const val JOB_ID = 1000
    fun enqueueWork(context: Context, work: Intent) {
        enqueueWork(context, PushNotificationService::class.java, JOB_ID, work)
    }
}