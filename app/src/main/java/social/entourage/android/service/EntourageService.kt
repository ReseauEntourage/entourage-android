package social.entourage.android.service


import android.app.Notification
import androidx.core.app.NotificationManagerCompat
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.widget.Chronometer
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.google.android.gms.maps.model.LatLng
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.request.EncounterRequest
import social.entourage.android.api.request.EntourageRequest
import social.entourage.android.api.request.NewsfeedRequest
import social.entourage.android.api.request.TourRequest
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.location.LocationUpdateListener
import social.entourage.android.newsfeed.NewsFeedListener
import social.entourage.android.newsfeed.NewsfeedPagination
import social.entourage.android.newsfeed.NewsfeedTabItem
import social.entourage.android.tools.Utils.getDateStringFromSeconds
import social.entourage.android.tools.log.CrashlyticsNewsFeedLogger
import social.entourage.android.tools.log.LoggerNewsFeedLogger
import java.util.*
import javax.inject.Inject

/**
 * Background service handling location updates
 * and tours request
 */
class EntourageService : Service() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private val binder: IBinder = LocalBinder()

    @Inject lateinit var authenticationController: AuthenticationController
    @Inject lateinit var tourRequest: TourRequest
    @Inject lateinit var encounterRequest: EncounterRequest
    @Inject lateinit var newsfeedRequest: NewsfeedRequest
    @Inject lateinit var entourageRequest: EntourageRequest

    private lateinit var entourageServiceManager: EntourageServiceManager
    val tourServiceManager:  TourServiceManager?
        get() = (entourageServiceManager as? TourServiceManager)

    private val apiListeners: MutableList<ApiConnectionListener> = ArrayList()
    private val locationUpdateListeners: MutableList<LocationUpdateListener> = ArrayList()
    private val crashlyticsListener = CrashlyticsNewsFeedLogger()
    private val loggerListener = LoggerNewsFeedLogger()
    //private lateinit var notificationManager: NotificationManagerCompat
    private var notification: Notification? = null
    private var notificationRemoteView: RemoteViews? = null
    private var timeBase: Long = 0
    private lateinit var chronometer: Chronometer

    var isPaused = false
        private set
    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                KEY_NOTIFICATION_PAUSE_TOUR -> {
                    val newIntent = Intent(context, MainActivity::class.java)
                    newIntent.action = intent.action
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(newIntent)
                }
                KEY_NOTIFICATION_STOP_TOUR -> {
                    val newIntent = Intent(context, MainActivity::class.java)
                    newIntent.action = intent.action
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(newIntent)
                }
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
        val service: EntourageService
            get() = this@EntourageService
    }

    override fun onCreate() {
        super.onCreate()
        EntourageApplication[this].entourageComponent.inject(this)
        //notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        chronometer = Chronometer(this)
        entourageServiceManager = EntourageServiceManager.newInstance(
                this,
                tourRequest,
                authenticationController,
                encounterRequest,
                newsfeedRequest,
                entourageRequest)
        isPaused = false
        val filter = IntentFilter()
        filter.addAction(KEY_NOTIFICATION_PAUSE_TOUR)
        filter.addAction(KEY_NOTIFICATION_STOP_TOUR)
        filter.addAction(KEY_LOCATION_PROVIDER_DISABLED)
        filter.addAction(KEY_LOCATION_PROVIDER_ENABLED)
        registerReceiver(receiver, filter)
        registerApiListener(crashlyticsListener)
        registerApiListener(loggerListener)
    }

    override fun onDestroy() {
        unregisterApiListener(loggerListener)
        unregisterApiListener(crashlyticsListener)
        endTreatment()
        unregisterReceiver(receiver)
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        removeNotification()
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------
    val currentTour: Tour?
        get() = tourServiceManager?.tour
    val currentTourId: String
        get() = tourServiceManager?.getTourUUID() ?: ""

    // ----------------------------------
    // METHODS
    // ----------------------------------
    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent()
        intent.action = action
        return PendingIntent.getBroadcast(this, 0, intent, 0)
    }

    private fun showNotification(action: Int) {
        if (notification == null) {
            createNotification()
        }
        configureRemoteView(action)
        notification?.let {NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, it)}
    }

    private fun createNotification() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return
        val notificationIntent = Intent(this, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        var builder = NotificationCompat.Builder(this, getString(R.string.local_service_notification_title))
                .setSmallIcon(R.drawable.ic_baseline_play_arrow_24)
                .setContentTitle(getString(R.string.local_service_running))
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
        val pauseTourIntent = createPendingIntent(KEY_NOTIFICATION_PAUSE_TOUR)
        val stopTourIntent = createPendingIntent(KEY_NOTIFICATION_STOP_TOUR)
        notificationRemoteView = RemoteViews(packageName, R.layout.layout_notification_tour_service_small).apply {
            this.setOnClickPendingIntent(R.id.notification_tour_pause_button, pauseTourIntent)
            this.setOnClickPendingIntent(R.id.notification_tour_stop_button, stopTourIntent)
        }
        builder = builder.setContent(notificationRemoteView)
        //notification.bigContentView = notificationRemoteView;
        notification = builder.build()
    }

    private fun configureRemoteView(action: Int) {
        when (action) {
            ACTION_START -> {
                timeBase = 0
                notificationRemoteView?.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime(), null, true)
                chronometer.start()
            }
            ACTION_PAUSE -> {
                timeBase = chronometer.base - SystemClock.elapsedRealtime()
                notificationRemoteView?.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, false)
            }
            ACTION_RESUME -> {
                notificationRemoteView?.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, true)
                chronometer.base = SystemClock.elapsedRealtime() + timeBase
            }
            else -> {
            }
        }
    }

    private fun startNotification() {
        showNotification(ACTION_START)
    }

    private fun pauseNotification() {
        showNotification(ACTION_PAUSE)
    }

    private fun resumeNotification() {
        showNotification(ACTION_RESUME)
    }

    private fun removeNotification() {
        chronometer.stop()
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID)
    }

    private fun stopService() {
        entourageServiceManager.stopLocationService()
        entourageServiceManager.unregisterFromBus()
        stopSelf()
    }

    fun updateNewsfeed(pagination: NewsfeedPagination, selectedTab: NewsfeedTabItem): Boolean {
        if (pagination.isLoading && !pagination.isRefreshing) {
            return false
        }
        pagination.isLoading = true
        entourageServiceManager.retrieveNewsFeed(pagination, selectedTab)
        return true
    }

    fun cancelNewsFeedUpdate() {
        entourageServiceManager.cancelNewsFeedRetrieval()
    }

    fun updateUserHistory(userId: Int, page: Int, per: Int) {
        tourServiceManager?.retrieveToursByUserId(userId, page, per)
    }

    fun updateOngoingTour() {
        if (isRunning) {
            tourServiceManager?.updateTourCoordinates()
        }
    }

    fun beginTreatment(type: String) {
        if (!isRunning) {
            tourServiceManager?.startTour(type)
        }
    }

    fun pauseTreatment() {
        if (!isRunning || isPaused) {
            return
        }
        tourServiceManager?.setTourDuration(getDateStringFromSeconds(SystemClock.elapsedRealtime() - chronometer.base))
        pauseNotification()
        isPaused = true
    }

    fun resumeTreatment() {
        if (isRunning) {
            if (isPaused) {
                resumeNotification()
                isPaused = false
            }
        }
    }

    fun endTreatment() {
        if (isRunning) {
            tourServiceManager?.finishTour()
        }
    }

    fun stopFeedItem(feedItem: FeedItem, success: Boolean) {
        if (feedItem.type == TimestampedObject.TOUR_CARD) {
            tourServiceManager?.finishTour(feedItem as Tour)
        } else if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            entourageServiceManager.closeEntourage(feedItem as BaseEntourage, success)
        }
    }

    fun reopenFeedItem(feedItem: FeedItem) {
        if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            entourageServiceManager.reopenEntourage(feedItem as BaseEntourage)
        }
    }

    fun freezeTour(tour: Tour) {
        tourServiceManager?.freezeTour(tour)
    }

    fun requestToJoinTour(tour: Tour) {
        tourServiceManager?.requestToJoinTour(tour)
    }

    fun removeUserFromFeedItem(feedItem: FeedItem, userId: Int) {
        if (feedItem.type == TimestampedObject.TOUR_CARD) {
            tourServiceManager?.removeUserFromTour(feedItem as Tour, userId)
        } else if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            entourageServiceManager.removeUserFromEntourage(feedItem as BaseEntourage, userId)
        }
    }

    fun requestToJoinEntourage(entourage: BaseEntourage) {
        entourageServiceManager.requestToJoinEntourage(entourage)
    }

    fun registerApiListener(listener: ApiConnectionListener) {
        apiListeners.add(listener)
    }

    fun unregisterApiListener(listener: ApiConnectionListener) {
        apiListeners.remove(listener)
    }

    fun registerServiceListener(listener: LocationUpdateListener) {
        locationUpdateListeners.add(listener)
        if (tourServiceManager?.isRunning == true && listener is TourServiceListener) {
            currentTour?.let { listener.onTourResumed(tourServiceManager!!.pointsToDraw, it.tourType, it.getStartTime()) }
        }
    }

    fun unregisterServiceListener(listener: LocationUpdateListener) {
        locationUpdateListeners.remove(listener)
        if (!isRunning && locationUpdateListeners.size == 0) {
            stopService()
        }
    }

    val isRunning: Boolean
        get() = tourServiceManager?.isRunning == true

    fun addEncounter(encounter: Encounter) {
        tourServiceManager?.addEncounter(encounter)
    }

    fun updateEncounter(encounter: Encounter) {
        tourServiceManager?.updateEncounter(encounter)
    }

    fun notifyListenersTourCreated(created: Boolean, uuid: String) {
        if (created) {
            startNotification()
        }
        locationUpdateListeners.filterIsInstance<TourServiceListener>().forEach { listener ->
            listener.onTourCreated(created, uuid)
        }
    }

    fun notifyListenersTourResumed() {
        tourServiceManager?.let { tourMgr->
            if (tourMgr.isRunning) {
                currentTour?.let {
                    locationUpdateListeners.filterIsInstance<TourServiceListener>().forEach { listener ->
                        listener.onTourResumed(tourMgr.pointsToDraw, it.tourType, it.getStartTime())
                    }
                }
            }
        }
    }

    fun notifyListenersFeedItemClosed(closed: Boolean, feedItem: FeedItem) {
        if (closed && feedItem.type == TimestampedObject.TOUR_CARD) {
            currentTour?.let { ongoingTour ->
                if (ongoingTour.id == feedItem.id) {
                    removeNotification()
                    isPaused = false
                }
            } ?: run {
                removeNotification()
                isPaused = false
            }
        }
        locationUpdateListeners.filterIsInstance<EntourageServiceListener>().forEach { listener ->
            listener.onFeedItemClosed(closed, feedItem)
        }
    }

    fun notifyListenersTourUpdated(newPoint: LatLng) {
        locationUpdateListeners.filterIsInstance<TourServiceListener>().forEach {
            it.onTourUpdated(newPoint)
        }
    }

    fun notifyListenersPosition(location: LatLng) {
        locationUpdateListeners.forEach { listener -> listener.onLocationUpdated(location) }
    }

    fun notifyListenersUserToursFound(tours: List<Tour>) {
        locationUpdateListeners.filterIsInstance<TourServiceListener>().forEach {
            it.onRetrieveToursByUserId(tours)
        }
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
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = 0
        const val ACTION_PAUSE = 1
        const val ACTION_RESUME = 2
        const val KEY_NOTIFICATION_PAUSE_TOUR = "social.entourage.android.KEY_NOTIFICATION_PAUSE_TOUR"
        const val KEY_NOTIFICATION_STOP_TOUR = "social.entourage.android.KEY_NOTIFICATION_STOP_TOUR"
        const val KEY_LOCATION_PROVIDER_DISABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_DISABLED"
        const val KEY_LOCATION_PROVIDER_ENABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_ENABLED"
    }
}