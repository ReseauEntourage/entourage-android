package social.entourage.android.api.tape

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.IBinder
import com.squareup.otto.Subscribe
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.tape.EncounterTaskResult.OperationType
import social.entourage.android.api.tape.Events.OnConnectionChangedEvent
import social.entourage.android.tools.EntBus
import social.entourage.android.tour.encounter.EncounterUploadCallback
import javax.inject.Inject

class EncounterTapeService : Service(), EncounterUploadCallback {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @JvmField
    @Inject
    var queue: EncounterTapeTaskQueue? = null
    private var connected = false

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate() {
        super.onCreate()
        get(this).entourageComponent.inject(this)
        connected = isConnected()
        EntBus.register(this)
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (queue != null) {
            executeNext()
        } else {
            stopService()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    // ----------------------------------
    // METHODS
    // ----------------------------------
    private fun isConnected(): Boolean {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    private fun stopService() {
        EntBus.unregister(this)
        stopSelf()
    }

    private fun executeNext() {
        if (connected) {
            if (running) {
                return
            }
            queue?.peek()?.let {task ->
                running = true
                task.execute(this)
            } ?: run {
                stopService()
            }
        }
    }

    override fun onSuccess(encounter: Encounter?, operationType: OperationType?) {
        running = false
        queue?.remove()
        executeNext()
    }

    override fun onFailure(encounter: Encounter?, operationType: OperationType?) {
        running = false
        executeNext()
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun onConnectionChanged(event: OnConnectionChangedEvent) {
        connected = event.isConnected
        if (connected) executeNext()
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    class ConnectionChangeReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            EntBus.register(this)
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            EntBus.post(OnConnectionChangedEvent(connectivityManager.activeNetworkInfo?.isConnected == true))
            EntBus.unregister(this)
        }
    }

    companion object {
        private var running = false
    }
}