package social.entourage.android.tour;

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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.map.MapActivity;

/**
 * Background service for handling location modification in a tour like in "RunKeeper" app
 */
//TODO : remove the notification when the app is killed from the recent apps list (doesn't work)
public class TourService extends Service {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final IBinder binder = new LocalBinder();
    private final int NOTIFICATION_ID = 1;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    @Inject
    TourServiceManager tourServiceManager;

    private List<TourServiceListener> listeners;

    NotificationCompat.Builder builder;

    private NotificationManager notificationManager;
    private RemoteViews notification;
    private long timeBase;
    private Chronometer chronometer;

    private boolean isPaused;

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
        setupComponent(EntourageApplication.get(this).getEntourageComponent());
        listeners =  new ArrayList<>();
        isPaused = false;

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MapActivity.class), 0);

        builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.tour_record)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    protected void setupComponent(EntourageComponent entourageComponent) {
        DaggerTourComponent.builder()
                .entourageComponent(entourageComponent)
                .tourModule(new TourModule(this))
                .build()
                .inject(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received Start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        endTreatment();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void showNotification(int action) {

        Intent intentPause = new Intent(this, NotificationIntentReceiver.class)
                .setAction(getString(R.string.notification_stop_intent));
        Intent intentStop = new Intent(this, NotificationIntentReceiver.class)
                .setAction(getString(R.string.notification_pause_intent));

        PendingIntent buttonPauseIntent = PendingIntent.getActivity(this, 0, intentPause, 0)
                .getBroadcast(this, 0, intentPause, 0);
        PendingIntent buttonStopIntent = PendingIntent.getActivity(this, 0, intentStop, 0)
                .getBroadcast(this, 0, intentStop, 0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContentTitle(getString(R.string.local_service_running))
                    .setSmallIcon(R.drawable.tour_record);
        } else {
            notification = new RemoteViews(getPackageName(), R.layout.notification_tour_service);
            notification.setOnClickPendingIntent(R.id.notification_tour_pause_button, buttonPauseIntent);
            notification.setOnClickPendingIntent(R.id.notification_tour_stop_button, buttonStopIntent);
            switch (action) {
                case 0 :
                    timeBase = 0;
                    notification.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime(), null, true);
                    chronometer.start();
                    break;
                case 1 :
                    notificationManager.cancel(NOTIFICATION_ID);
                    timeBase = chronometer.getBase() - SystemClock.elapsedRealtime();
                    notification.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, false);
                    break;
                case 2 :
                    notificationManager.cancel(NOTIFICATION_ID);
                    notification.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime() + timeBase, null, true);
                    chronometer.setBase(SystemClock.elapsedRealtime() + timeBase);
                    break;
                default :
                    break;
            }
            builder = builder.setContent(notification);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
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
        chronometer.stop();
        chronometer = null;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void beginTreatment(String type1, String type2) {
        if (!isRunning()) {
            tourServiceManager.startTour(type1, type2);
            startNotification();
            //Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseTreatment() {
        if (isRunning()) {
            if (!isPaused) {
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
            removeNotification();
            if (listeners.size() == 0) stopSelf();
            Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
        }
    }

    public void register(TourServiceListener listener) {
        listeners.add(listener);
        if (tourServiceManager.isRunning()) {
            listener.onTourResumed(tourServiceManager.getTour());
        }
    }

    public void unregister(TourServiceListener listener) {
        listeners.remove(listener);
        if (!isRunning() && listeners.size() == 0) {
            stopSelf();
        }
    }

    public boolean isRunning() {
        return tourServiceManager.isRunning();
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void notifyListeners(Tour tour) {
        for (TourServiceListener listener : listeners) listener.onTourUpdated(tour);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface TourServiceListener {
        void onTourUpdated(Tour tour);
        void onTourResumed(Tour tour);
    }
}
