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
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.RemoteViews;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;

import social.entourage.android.DrawerActivity;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;

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
    public static final String KEY_GPS_DISABLED = "social.entourage.android.KEY_GPS_DISABLED";
    public static final String KEY_GPS_ENABLED = "social.entourage.android.KEY_GPS_ENABLED";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final IBinder binder = new LocalBinder();

    @Inject
    TourRequest tourRequest;
    @Inject
    EncounterRequest encounterRequest;
    @Inject
    NewsfeedRequest newsfeedRequest;

    private TourServiceManager tourServiceManager;

    private List<TourServiceListener> listeners;

    private NotificationManager notificationManager;
    private Notification notification;
    private RemoteViews notificationRemoteView;
    private long timeBase;
    private Chronometer chronometer;

    private boolean isPaused;

    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (KEY_NOTIFICATION_PAUSE_TOUR.equals(action)) {
                Intent newIntent = new Intent(context, DrawerActivity.class);
                newIntent.setAction(action);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            }
            else if (KEY_NOTIFICATION_STOP_TOUR.equals(action)) {
                Intent newIntent = new Intent(context, DrawerActivity.class);
                newIntent.setAction(action);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            }
            else if (KEY_GPS_DISABLED.equals(action)) {
                notifyListenersGpsStatusChanged(false);
                Intent newIntent = new Intent(context, DrawerActivity.class);
                newIntent.setAction(action);
                newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(newIntent);
            }
            else if (KEY_GPS_ENABLED.equals(action)) {
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

        tourServiceManager = new TourServiceManager(this, tourRequest, encounterRequest, newsfeedRequest);

        listeners =  new ArrayList<>();
        isPaused = false;

        IntentFilter filter = new IntentFilter();
        filter.addAction(KEY_NOTIFICATION_PAUSE_TOUR);
        filter.addAction(KEY_NOTIFICATION_STOP_TOUR);
        filter.addAction(KEY_GPS_DISABLED);
        filter.addAction(KEY_GPS_ENABLED);
        registerReceiver(receiver, filter);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        endTreatment();
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        removeNotification();
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getCurrentTour() {
        return tourServiceManager.getTour();
    }

    public long getCurrentTourId() {
        return tourServiceManager.getTourId();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        return PendingIntent.getBroadcast(this, 0, intent, 0);
    }

    private void showNotification(int action) {
        if (notification == null) {
            createNotification();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            configureRemoteView(action);
        }
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private void createNotification() {
        final Intent notificationIntent = new Intent(this, DrawerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.tour_record)
                .setContentTitle(getString(R.string.local_service_running))
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContentTitle(getString(R.string.local_service_running)).setSmallIcon(R.drawable.tour_record);
        } else {
            PendingIntent pauseTourIntent = createPendingIntent(KEY_NOTIFICATION_PAUSE_TOUR);
            PendingIntent stopTourIntent = createPendingIntent(KEY_NOTIFICATION_STOP_TOUR);
            notificationRemoteView = new RemoteViews(getPackageName(), R.layout.notification_tour_service_small);
            notificationRemoteView.setOnClickPendingIntent(R.id.notification_tour_pause_button, pauseTourIntent);
            notificationRemoteView.setOnClickPendingIntent(R.id.notification_tour_stop_button, stopTourIntent);
            builder = builder.setContent(notificationRemoteView);
            //notification.bigContentView = notificationRemoteView;
        }
        notification = builder.build();
    }

    private void configureRemoteView(int action) {
        switch (action) {
            case 0 :
                timeBase = 0;
                notificationRemoteView.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime(), null, true);
                chronometer.start();
                break;
            case 1 :
                timeBase = chronometer.getBase() - SystemClock.elapsedRealtime();
                notificationRemoteView.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, false);
                break;
            case 2 :
                notificationRemoteView.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, true);
                chronometer.setBase(SystemClock.elapsedRealtime() + timeBase);
                break;
            default :
                break;
        }
    }

    private void startNotification() {
        chronometer = new Chronometer(this);
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
        chronometer = null;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void stopService() {
        tourServiceManager.stopLocationService();
        tourServiceManager.unregisterFromBus();
        stopSelf();
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void updateNearbyTours() {
        tourServiceManager.retrieveToursNearbyLarge();
    }

    public void updateNewsfeed() {
        //TODO Add location and proper pagination
        tourServiceManager.retrieveNewsfeed(1, 10);
    }

    public void updateUserHistory(int userId, int page, int per) {
        tourServiceManager.retrieveToursByUserId(userId, page, per);
    }

    public void searchToursFromPoint(LatLng point, boolean isUserHistory, int userId, int page, int per) {
        tourServiceManager.retrieveToursNearbySmall(point, isUserHistory, userId, page, per);
    }

    public void updateOngoingTour() {
        if (!isRunning()) return;
        tourServiceManager.updateTourCoordinates();
    }

    public void beginTreatment(String transportMode, String type) {
        if (!isRunning()) {
            tourServiceManager.startTour(transportMode, type);
        }
    }

    public void pauseTreatment() {
        if (isRunning()) {
            if (!isPaused) {
                Date duration = new Date(SystemClock.elapsedRealtime() - chronometer.getBase());
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.US);
                dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                tourServiceManager.setTourDuration(dateFormat.format(duration));
                pauseNotification();
                isPaused = true;
            }
        }
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

    public void stopOtherTour(Tour tour) {
        tourServiceManager.finishTour(tour);
    }

    public void freezeTour(Tour tour) {
        tourServiceManager.freezeTour(tour);
    }

    public void requestToJoinTour(Tour tour) {
        tourServiceManager.requestToJoinTour(tour);
    }

    public void removeUserFromTour(Tour tour, int userId) {
        tourServiceManager.removeUserFromTour(tour, userId);
    }

    public void register(TourServiceListener listener) {
        listeners.add(listener);
        if (tourServiceManager.isRunning()) {
            listener.onTourResumed(tourServiceManager.getPointsToDraw(), tourServiceManager.getTour().getTourType(), tourServiceManager.getTour().getStartTime());
        }
    }

    public void unregister(TourServiceListener listener) {
        listeners.remove(listener);
        if (!isRunning() && listeners.size() == 0) {
            stopService();
        }
    }

    public boolean isRunning() {
        return tourServiceManager.isRunning();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void addEncounter(Encounter encounter) {
        tourServiceManager.addEncounter(encounter);
    }

    public boolean isLocationInTour(LatLng latLng) {
        return tourServiceManager.isLocationInTour(latLng);
    }

    public void notifyListenersTourCreated(boolean created, long id) {
        if (created) {
            startNotification();
        }
        for (TourServiceListener listener : listeners) {
            listener.onTourCreated(created, id);
        }
    }

    public void notifyListenersTourResumed() {
        if (!tourServiceManager.isRunning()) return;
        for (TourServiceListener listener : listeners) {
            listener.onTourResumed(tourServiceManager.getPointsToDraw(), tourServiceManager.getTour().getTourType(), tourServiceManager.getTour().getStartTime());
        }
    }

    public void notifyListenersTourClosed(boolean closed, Tour tour) {
        if (closed) {
            removeNotification();
            isPaused = false;
        }
        for (TourServiceListener listener : listeners) {
            listener.onTourClosed(closed, tour);
        }
    }

    public void notifyListenersTourUpdated(LatLng newPoint) {
        for (TourServiceListener listener : listeners) {
            listener.onTourUpdated(newPoint);
        }
    }

    public void notifyListenersPosition(LatLng location) {
        for (TourServiceListener listener : listeners) {
            listener.onLocationUpdated(location);
        }
    }

    public void notifyListenersToursNearby(List<Tour> tours) {
        for (TourServiceListener listener : listeners) {
            listener.onRetrieveToursNearby(tours);
        }
    }

    public void notifyListenersUserToursFound(List<Tour> tours) {
        for (TourServiceListener listener : listeners) {
            listener.onRetrieveToursByUserId(tours);
        }
    }

    public void notifyListenersUserToursFoundFromPoint(Map<Long, Tour> tours) {
        for (TourServiceListener listener : listeners) {
            listener.onUserToursFound(tours);
        }
    }

    public void notifyListenersToursFound(Map<Long, Tour> tours) {
        for (TourServiceListener listener : listeners) {
            listener.onToursFound(tours);
        }
    }

    public void notifyListenersGpsStatusChanged(boolean active) {
        for (TourServiceListener listener : listeners) {
            listener.onGpsStatusChanged(active);
        }
    }

    public void notifyListenersUserStatusChanged(TourUser user, Tour tour) {
        for (TourServiceListener listener : listeners) {
            listener.onUserStatusChanged(user, tour);
        }
    }

    public void notifyListenersNewsfeed(List<Newsfeed> newsfeedList) {
        for (TourServiceListener listener : listeners) {
            listener.onRetrieveNewsfeed(newsfeedList);
        }
    }

    // ----------------------------------
    // INNER INTERFACES
    // ----------------------------------

    public interface TourServiceListener {
        void onTourCreated(boolean created, long tourId);
        void onTourUpdated(LatLng newPoint);
        void onTourResumed(List<TourPoint> pointsToDraw, String tourType, Date startDate);
        void onLocationUpdated(LatLng location);
        void onRetrieveToursNearby(List<Tour> tours);
        void onRetrieveToursByUserId(List<Tour> tours);
        void onUserToursFound(Map<Long, Tour> tours);
        void onToursFound(Map<Long, Tour> tours);
        void onTourClosed(boolean closed, Tour tour);
        void onGpsStatusChanged(boolean active);
        void onUserStatusChanged(TourUser user, Tour tour);
        void onRetrieveNewsfeed(List<Newsfeed> newsfeedList);
    }
}
