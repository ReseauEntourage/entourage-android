package social.entourage.android.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_CLICKED -> {
                // Gérer l'action de clic sur la notification
                Log.wtf("wtf", "wtf gone on click ")
            }
            ACTION_DISMISSED -> {
                Log.wtf("wtf" , " wtf gone on swiped")
                // Gérer l'action de swipe pour supprimer la notification
            }
        }
    }

    companion object {
        const val ACTION_CLICKED = "ACTION_CLICKED"
        const val ACTION_DISMISSED = "ACTION_DISMISSED"
    }
}