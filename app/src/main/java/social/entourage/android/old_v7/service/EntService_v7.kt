package social.entourage.android.old_v7.service

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.EntourageApplication
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.request.NewsfeedRequest
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.location.LocationUpdateListener
import social.entourage.android.old_v7.base.newsfeed.NewsFeedListener
import social.entourage.android.old_v7.base.newsfeed.NewsfeedPagination
import social.entourage.android.old_v7.base.newsfeed.NewsfeedTabItem
import social.entourage.android.tools.log.LoggerNewsFeedLogger

/**
 * Background service handling location updates request
 */
class EntService_v7 : Service() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private val binder: IBinder = LocalBinder()

     private val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController
     private val newsfeedRequest: NewsfeedRequest
         get() = EntourageApplication.get().apiModule.newsfeedRequest
     private val entourageRequest: EntourageRequest
         get() = EntourageApplication.get().apiModule.entourageRequest

    private lateinit var entServiceManager: EntServiceManager_v7

    private val apiListeners: MutableList<ApiConnectionListener> = ArrayList()
    private val locationUpdateListeners: MutableList<LocationUpdateListener> = ArrayList()
    private val loggerListener = LoggerNewsFeedLogger()

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                KEY_LOCATION_PROVIDER_DISABLED -> {
                    notifyListenersGpsStatusChanged(false)
                    /* TODO: fix this so it won't start multiple intents
                        if (isRunning()) {
                        final Intent newIntent = new Intent(context, MainActivity.class);
                        newIntent.setAction(action);
                        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(newIntent);
                    }*/
                }
                KEY_LOCATION_PROVIDER_ENABLED -> {
                    notifyListenersGpsStatusChanged(true)
                }
            }
        }
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    inner class LocalBinder : Binder() {
        val service: EntService_v7
            get() = this@EntService_v7
    }

    override fun onCreate() {
        super.onCreate()
        entServiceManager = EntServiceManager_v7.newInstance(
            this,
            authenticationController,
            newsfeedRequest,
            entourageRequest
        )
        val filter = IntentFilter()
        filter.addAction(KEY_LOCATION_PROVIDER_DISABLED)
        filter.addAction(KEY_LOCATION_PROVIDER_ENABLED)
        registerReceiver(receiver, filter)
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
        entServiceManager.unregisterFromBus()
        stopSelf()
    }

    fun updateNewsfeed(pagination: NewsfeedPagination, selectedTab: NewsfeedTabItem): Boolean {
        if (pagination.isLoading && !pagination.isRefreshing) {
            return false
        }
        pagination.isLoading = true
        entServiceManager.retrieveNewsFeed(pagination, selectedTab)
        return true
    }

    fun updateHomefeed(pagination: NewsfeedPagination): Boolean {
        if (pagination.isLoading && !pagination.isRefreshing) {
            return false
        }
        pagination.isLoading = true
        entServiceManager.retrieveHomeFeed()
        return true
    }

    fun cancelNewsFeedUpdate() {
        entServiceManager.cancelNewsFeedRetrieval()
    }

    fun stopFeedItem(feedItem: FeedItem, success: Boolean, comment:String?) {
        if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            entServiceManager.closeEntourage(feedItem as BaseEntourage, success,comment)
        }
    }

    fun reopenFeedItem(feedItem: FeedItem) {
        if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            entServiceManager.reopenEntourage(feedItem as BaseEntourage)
        }
    }

    fun removeUserFromFeedItem(feedItem: FeedItem, userId: Int) {
        if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            entServiceManager.removeUserFromEntourage(feedItem as BaseEntourage, userId)
        }
    }

    fun requestToJoinEntourage(entourage: BaseEntourage) {
        entServiceManager.requestToJoinEntourage(entourage)
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

    fun notifyListenersFeedItemClosed(closed: Boolean, feedItem: FeedItem) {
        locationUpdateListeners.filterIsInstance<EntourageServiceListener>().forEach { listener ->
            listener.onFeedItemClosed(closed, feedItem)
        }
    }

    fun notifyListenersPosition(location: LatLng) {
        locationUpdateListeners.forEach { listener -> listener.onLocationUpdated(location) }
    }

    private fun notifyListenersGpsStatusChanged(active: Boolean) {
        locationUpdateListeners.forEach { listener -> listener.onLocationStatusUpdated(active) }
    }

    fun notifyListenersUserStatusChanged(user: EntourageUser, feedItem: FeedItem) {
       locationUpdateListeners.filterIsInstance<EntourageServiceListener>().forEach {
            it.onUserStatusChanged(user, feedItem)
        }
    }

    fun notifyListenersNetworkException() {
        apiListeners.forEach { listener -> listener.onNetworkException() }
    }

    fun notifyListenersServerException(throwable: Throwable) {
        apiListeners.forEach { listener -> listener.onServerException(throwable) }
    }

    fun notifyListenersTechnicalException(throwable: Throwable) {
        apiListeners.forEach { listener -> listener.onTechnicalException(throwable) }
    }

    fun notifyListenersNewsFeedReceived(newsFeeds: List<NewsfeedItem>) {
        apiListeners.filterIsInstance<NewsFeedListener>().forEach {
            it.onNewsFeedReceived(newsFeeds)
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val KEY_LOCATION_PROVIDER_DISABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_DISABLED"
        const val KEY_LOCATION_PROVIDER_ENABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_ENABLED"
    }
}