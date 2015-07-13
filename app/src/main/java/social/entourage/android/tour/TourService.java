package social.entourage.android.tour;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
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

    private NotificationManager notificationManager;
    private RemoteViews notification;

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

    private void showNotification() {

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MapActivity.class), 0);

        PendingIntent buttonIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, TourService.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.tour_record)
                .setTicker(getString(R.string.tour_started))
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notification = new RemoteViews(getPackageName(), R.layout.notification_tour_service);
            notification.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime(), null, true);
            notification.setOnClickPendingIntent(R.id.notification_tour_stop_button, buttonIntent);
            builder = builder.setContent(notification);
        } else {
            builder = builder.setContentTitle(getString(R.string.local_service_running))
                    .setSmallIcon(R.drawable.tour_record);
        }

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void removeNotification() {
        notificationManager.cancel(NOTIFICATION_ID);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void beginTreatment(String type1, String type2) {
        if (!isRunning()) {
            tourServiceManager.startTour(type1, type2);
            showNotification();
            //Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
        }
    }

    public void pauseTreatment() {
        if (isRunning()) {
            if (!isPaused) {
                //notification.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime(), null, false);
                isPaused = true;
            }
        }
    }

    public void resumeTreatment() {
        if (isRunning()) {
            if (isPaused) {
                //notification.setChronometer(R.id.notification_tour_chronometer, SystemClock.elapsedRealtime(), null, true);
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
