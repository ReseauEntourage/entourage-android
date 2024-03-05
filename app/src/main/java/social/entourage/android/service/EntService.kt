package social.entourage.android.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.EntourageApplication
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.location.LocationUpdateListener
import social.entourage.android.tools.log.LoggerNewsFeedLogger

/**
 * Background service handling location updates request
 */
class EntService : Service() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private val binder: IBinder = LocalBinder()

     private val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController

    private lateinit var entServiceManager: EntServiceManager

    private val apiListeners: MutableList<ApiConnectionListener> = ArrayList()
    private val locationUpdateListeners: MutableList<LocationUpdateListener> = ArrayList()
    private val loggerListener = LoggerNewsFeedLogger()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                KEY_LOCATION_PROVIDER_DISABLED,
                KEY_LOCATION_PROVIDER_ENABLED -> {
                    notifyListenersGpsStatusChanged()
                }
            }
        }
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    inner class LocalBinder : Binder() {
        val service: EntService
            get() = this@EntService
    }

    override fun onCreate() {
        super.onCreate()
        entServiceManager = EntServiceManager.newInstance(
                this,
                authenticationController)
        val filter = IntentFilter()
        filter.addAction(KEY_LOCATION_PROVIDER_DISABLED)
        filter.addAction(KEY_LOCATION_PROVIDER_ENABLED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(receiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(receiver, filter)
        }
        registerApiListener(loggerListener)
    }

    override fun onDestroy() {
        unregisterApiListener(loggerListener)
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    // ----------------------------------
    // METHODS
    // ----------------------------------
    private fun stopService() {
        entServiceManager.stopLocationService()
        stopSelf()
    }

    fun registerApiListener(listener: ApiConnectionListener) {
        apiListeners.add(listener)
    }

    fun unregisterApiListener(listener: ApiConnectionListener) {
        apiListeners.remove(listener)
    }

    fun registerServiceListener(listener: LocationUpdateListener) {
        locationUpdateListeners.add(listener)
    }

    fun unregisterServiceListener(listener: LocationUpdateListener) {
        locationUpdateListeners.remove(listener)
        if (locationUpdateListeners.size == 0) {
            stopService()
        }
    }

    fun notifyListenersPosition(location: LatLng) {
        locationUpdateListeners.forEach { listener -> listener.onLocationUpdated(location) }
    }

    private fun notifyListenersGpsStatusChanged() {
        locationUpdateListeners.forEach { listener -> listener.onLocationStatusUpdated() }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val KEY_LOCATION_PROVIDER_DISABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_DISABLED"
        const val KEY_LOCATION_PROVIDER_ENABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_ENABLED"
    }
}