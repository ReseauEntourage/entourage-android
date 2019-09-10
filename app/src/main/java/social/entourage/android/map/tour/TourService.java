package social.entourage.android.map.tour;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import javax.inject.Inject;

import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.TimestampedObject;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.location.LocationUpdateListener;
import social.entourage.android.map.MapTabItem;
import social.entourage.android.newsfeed.NewsfeedPagination;
import social.entourage.android.tools.log.CrashlyticsNewsFeedLogger;
import social.entourage.android.tools.log.LoggerNewsFeedLogger;

/**
 * Background service handling location updates
 * and tours request
 */
public class TourService extends Service {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int NOTIFICATION_ID = 1;
    public static final String KEY_NOTIFICATION_PAUSE_TOUR = "social.entourage.android.KEY_NOTIFICATION_PAUSE_TOUR";
    public static final String KEY_NOTIFICATION_STOP_TOUR = "social.entourage.android.KEY_NOTIFICATION_STOP_TOUR";
    public static final String KEY_LOCATION_PROVIDER_DISABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_DISABLED";
    public static final String KEY_LOCATION_PROVIDER_ENABLED = "social.entourage.android.KEY_LOCATION_PROVIDER_ENABLED";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final IBinder binder = new LocalBinder();

    @Inject
    AuthenticationController authenticationController;
    @Inject
    TourRequest tourRequest;
    @Inject
    EncounterRequest encounterRequest;
    @Inject
    NewsfeedRequest newsfeedRequest;
    @Inject
    EntourageRequest entourageRequest;

    private TourServiceManager tourServiceManager;

    private final List<TourServiceListener> tourServiceListeners = new ArrayList<>();
    private final List<NewsFeedListener> newsFeedListeners = new ArrayList<>();
    private final List<LocationUpdateListener> locationUpdateListeners = new ArrayList<>();

    private final CrashlyticsNewsFeedLogger crashlyticsListener = new CrashlyticsNewsFeedLogger();
    private final LoggerNewsFeedLogger loggerListener = new LoggerNewsFeedLogger();

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews notificationRemoteView;
    private long timeBase;
    private Chronometer chronometer;

    private boolean isPaused;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            if (KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                final Intent newIntent = new Intent(context, DrawerActivity.class);
                newIntent.setAction(action);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            } else if (KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                final Intent newIntent = new Intent(context, DrawerActivity.class);
                newIntent.setAction(action);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            } else if (KEY_LOCATION_PROVIDER_DISABLED.equals(action)) {
                notifyListenersGpsStatusChanged(false);
                if (isRunning()) {
                    final Intent newIntent = new Intent(context, DrawerActivity.class);
                    newIntent.setAction(action);
                    newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(newIntent);
                }
            } else if (KEY_LOCATION_PROVIDER_ENABLED.equals(action)) {
                notifyListenersGpsStatusChanged(true);
            }
        }
    };

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------

    public class LocalBinder extends Binder {
        public TourService getService() {
            return TourService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EntourageApplication.get(this).getEntourageComponent().inject(this);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        tourServiceManager = TourServiceManager.newInstance(
                this,
                tourRequest,
                authenticationController,
                encounterRequest,
                newsfeedRequest,
                entourageRequest);

        isPaused = false;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(KEY_NOTIFICATION_PAUSE_TOUR);
        filter.addAction(KEY_NOTIFICATION_STOP_TOUR);
        filter.addAction(KEY_LOCATION_PROVIDER_DISABLED);
        filter.addAction(KEY_LOCATION_PROVIDER_ENABLED);
        registerReceiver(receiver, filter);

        registerNewsFeedListener(crashlyticsListener);
        registerNewsFeedListener(loggerListener);
    }

    @Override
    public void onDestroy() {
        unregisterNewsFeedListener(loggerListener);
        unregisterNewsFeedListener(crashlyticsListener);
        endTreatment();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(final Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        removeNotification();
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getCurrentTour() {
        return tourServiceManager.getTour();
    }

    public String getCurrentTourId() {
        return tourServiceManager.getTourUUID();
    }

    // ----------------------------------
    // METHODS
    // ----------------------------------

    private PendingIntent createPendingIntent(final String action) {
        final Intent intent = new Intent();
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    private void showNotification(final int action) {
        if (notification == null) {
            createNotification();
        }
        configureRemoteView(action);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }

    private void createNotification() {
        final Intent notificationIntent = new Intent(this, DrawerActivity.class);
        final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.tour_record)
                .setContentTitle(getString(R.string.local_service_running))
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        final PendingIntent pauseTourIntent = createPendingIntent(KEY_NOTIFICATION_PAUSE_TOUR);
        final PendingIntent stopTourIntent = createPendingIntent(KEY_NOTIFICATION_STOP_TOUR);
        notificationRemoteView = new RemoteViews(getPackageName(), R.layout.notification_tour_service_small);
        notificationRemoteView.setOnClickPendingIntent(R.id.notification_tour_pause_button, pauseTourIntent);
        notificationRemoteView.setOnClickPendingIntent(R.id.notification_tour_stop_button, stopTourIntent);
        builder = builder.setContent(notificationRemoteView);
        //notification.bigContentView = notificationRemoteView;
        notification = builder.build();
    }

    private void configureRemoteView(final int action) {
            if(chronometer==null) {
                chronometer = new Chronometer(this);
            }
        switch (action) {
            case 0:
                timeBase = 0;
                notificationRemoteView.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime(), null, true);
                chronometer.start();
                break;
            case 1:
                timeBase = chronometer.getBase() - SystemClock.elapsedRealtime();
                notificationRemoteView.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, false);
                break;
            case 2:
                notificationRemoteView.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, true);
                chronometer.setBase(SystemClock.elapsedRealtime() + timeBase);
                break;
            default:
                break;
        }
    }

    private void startNotification() {
        if(chronometer==null) {
            chronometer = new Chronometer(this);
        }
        showNotification(0);
    }

    private void pauseNotification() {
        showNotification(1);
    }

    private void resumeNotification() {
        showNotification(2);
    }

    private void removeNotification() {
        if (chronometer != null) {
            chronometer.stop();
        }
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void stopService() {
        tourServiceManager.stopLocationService();
        tourServiceManager.unregisterFromBus();
        stopSelf();
    }

    public boolean updateNewsfeed(final NewsfeedPagination pagination, final MapTabItem selectedTab) {
        if (pagination.isLoading && !pagination.isRefreshing) {
            return false;
        }
        pagination.isLoading = true;
        tourServiceManager.retrieveNewsFeed(pagination, selectedTab);
        return true;
    }

    public void cancelNewsFeedUpdate() {
        tourServiceManager.cancelNewsFeedRetrieval();
    }

    public void updateUserHistory(final int userId, final int page, final int per) {
        tourServiceManager.retrieveToursByUserId(userId, page, per);
    }

    public void updateOngoingTour() {
        if (!isRunning()) {
            return;
        }
        tourServiceManager.updateTourCoordinates();
    }

    public void beginTreatment(final String type) {
        if (!isRunning()) {
            tourServiceManager.startTour(type);
        }
    }

    public void pauseTreatment() {
        if (!isRunning() || isPaused) {
            return;
        }

        if(chronometer==null) {
            chronometer = new Chronometer(this);
        }

        final Date duration = new Date(SystemClock.elapsedRealtime() - chronometer.getBase());
        final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);

        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        if (tourServiceManager != null) {
            tourServiceManager.setTourDuration(dateFormat.format(duration));
        }
        pauseNotification();
        isPaused = true;
    }

    public void resumeTreatment() {
        if (isRunning()) {
            if (isPaused) {
                resumeNotification();
                isPaused = false;
            }
        }
    }

    public void endTreatment() {
        if (isRunning()) {
            tourServiceManager.finishTour();
        }
    }

    public void stopFeedItem(final FeedItem feedItem, final boolean success) {
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            tourServiceManager.finishTour((Tour) feedItem);
        } else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            tourServiceManager.closeEntourage((Entourage) feedItem, success);
        }
    }

    public void freezeTour(final Tour tour) {
        tourServiceManager.freezeTour(tour);
    }

    public void requestToJoinTour(final Tour tour) {
        tourServiceManager.requestToJoinTour(tour);
    }

    public void removeUserFromFeedItem(final FeedItem feedItem, final int userId) {
        if (feedItem.getType() == TimestampedObject.TOUR_CARD) {
            tourServiceManager.removeUserFromTour((Tour) feedItem, userId);
        } else if (feedItem.getType() == TimestampedObject.ENTOURAGE_CARD) {
            tourServiceManager.removeUserFromEntourage((Entourage) feedItem, userId);
        }
    }

    public void requestToJoinEntourage(final Entourage entourage) {
        tourServiceManager.requestToJoinEntourage(entourage);
    }

    public void registerNewsFeedListener(final NewsFeedListener listener) {
        newsFeedListeners.add(listener);
    }

    void unregisterNewsFeedListener(final NewsFeedListener listener) {
        newsFeedListeners.remove(listener);
    }

    public void registerLocationUpdateListener(final LocationUpdateListener listener) {
        locationUpdateListeners.add(listener);
    }

    public void unregisterLocationUpdateListener(final LocationUpdateListener listener) {
        locationUpdateListeners.remove(listener);
    }

    public void registerTourServiceListener(final TourServiceListener listener) {
        tourServiceListeners.add(listener);
        if (tourServiceManager.isRunning()) {
            listener.onTourResumed(tourServiceManager.getPointsToDraw(), tourServiceManager.getTour().getTourType(), tourServiceManager.getTour().getStartTime());
        }
    }

    public void unregisterTourServiceListener(final TourServiceListener listener) {
        tourServiceListeners.remove(listener);
        if (!isRunning() && tourServiceListeners.size() == 0) {
            stopService();
        }
    }

    public boolean isRunning() {
        return tourServiceManager.isRunning();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void addEncounter(final Encounter encounter) {
        tourServiceManager.addEncounter(encounter);
    }

    public void updateEncounter(final Encounter encounter) {
        tourServiceManager.updateEncounter(encounter);
    }

    void notifyListenersTourCreated(final boolean created, final String uuid) {
        if (created) {
            startNotification();
        }
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onTourCreated(created, uuid);
        }
    }

    public void notifyListenersTourResumed() {
        if (!tourServiceManager.isRunning()) {
            return;
        }
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onTourResumed(tourServiceManager.getPointsToDraw(), tourServiceManager.getTour().getTourType(), tourServiceManager.getTour().getStartTime());
        }
    }

    void notifyListenersFeedItemClosed(final boolean closed, final FeedItem feedItem) {
        if (closed && feedItem.getType() == TimestampedObject.TOUR_CARD) {
            final Tour ongoingTour = tourServiceManager.getTour();
            if (ongoingTour != null) {
                if (ongoingTour.getId() == feedItem.getId()) {
                    removeNotification();
                    isPaused = false;
                }
            } else {
                removeNotification();
                isPaused = false;
            }
        }
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onFeedItemClosed(closed, feedItem);
        }
    }

    void notifyListenersTourUpdated(final LatLng newPoint) {
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onTourUpdated(newPoint);
        }
    }

    public void notifyListenersPosition(final LatLng location) {
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onLocationUpdated(location);
        }
        for (final LocationUpdateListener listener : locationUpdateListeners) {
            listener.onLocationUpdated(location);
        }
    }

    void notifyListenersUserToursFound(final List<Tour> tours) {
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onRetrieveToursByUserId(tours);
        }
    }

    private void notifyListenersGpsStatusChanged(final boolean active) {
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onLocationStatusUpdated(active);
        }
        for (final LocationUpdateListener listener : locationUpdateListeners) {
            listener.onLocationStatusUpdated(active);
        }
    }

    public void notifyListenersUserStatusChanged(final TourUser user, final FeedItem feedItem) {
        if(user==null || feedItem==null) {
            return;
        }
        for (final TourServiceListener listener : tourServiceListeners) {
            listener.onUserStatusChanged(user, feedItem);
        }
    }

    void notifyListenersNetworkException() {
        for (final NewsFeedListener listener : newsFeedListeners) {
            listener.onNetworkException();
        }
    }

    void notifyListenersCurrentPositionNotRetrieved() {
        for (final NewsFeedListener listener : newsFeedListeners) {
            listener.onCurrentPositionNotRetrieved();
        }
    }

    void notifyListenersServerException(final Throwable throwable) {
        for (final NewsFeedListener listener : newsFeedListeners) {
            listener.onServerException(throwable);
        }
    }

    void notifyListenersTechnicalException(final Throwable throwable) {
        for (final NewsFeedListener listener : newsFeedListeners) {
            listener.onTechnicalException(throwable);
        }
    }

    void notifyListenersNewsFeedReceived(final List<Newsfeed> newsFeeds) {
        for (final NewsFeedListener listener : newsFeedListeners) {
            listener.onNewsFeedReceived(newsFeeds);
        }
    }
}
