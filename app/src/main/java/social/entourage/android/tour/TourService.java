package social.entourage.android.tour;

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
import android.view.View;
import android.widget.Chronometer;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.inject.Inject;

import social.entourage.android.EntourageApplication;
import social.entourage.android.EntourageComponent;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.common.Constants;
import social.entourage.android.map.MapActivity;

/**
 * Background service for handling location modification in a tour like in "RunKeeper" app
 */
//TODO : remove the notification when the app is killed from the recent apps list (doesn't work)
//TODO : update the notification (buttons) - no link with the views
public class TourService extends Service {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int NOTIFICATION_ID = 1;
    public static final String NOTIFICATION_PAUSE = "social.entourage.android.NOTIFICATION_PAUSE";

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final IBinder binder = new LocalBinder();

    @Inject
    TourServiceManager tourServiceManager;

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
            if (NOTIFICATION_PAUSE.equals(intent.getAction())) {
                if (!isPaused) {
                    pauseTreatment();
                    Intent pauseIntent = new Intent(context, MapActivity.class);
                    pauseIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    pauseIntent.putExtra(NOTIFICATION_PAUSE, true);
                    startActivity(pauseIntent);
                }
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
        setupComponent(EntourageApplication.get(this).getEntourageComponent());
        listeners =  new ArrayList<>();
        isPaused = false;

        IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFICATION_PAUSE);
        registerReceiver(receiver, filter);

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
        unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getCurrentTour() {
        return tourServiceManager.getTour();
    }

    public long getTourId() {
        return tourServiceManager.getTourId();
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private PendingIntent createPendingIntent(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        return pendingIntent.getBroadcast(this, 0, intent, 0);
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
        final Intent notificationIntent = new Intent(this, MapActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.tour_record)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContentTitle(getString(R.string.local_service_running)).setSmallIcon(R.drawable.tour_record);
        } else {
            PendingIntent pauseTourIntent = createPendingIntent(NOTIFICATION_PAUSE);
            notificationRemoteView = new RemoteViews(getPackageName(), R.layout.notification_tour_service);
            notificationRemoteView.setOnClickPendingIntent(R.id.notification_tour_stop_button, pauseTourIntent);
            builder = builder.setContent(notificationRemoteView);
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
        chronometer.stop();
        chronometer = null;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void beginTreatment(String transportMode, String type) {
        if (!isRunning()) {
            tourServiceManager.startTour(transportMode, type);
            startNotification();
        }
    }

    public void pauseTreatment() {
        if (isRunning()) {
            if (!isPaused) {
                Date duration = new Date(SystemClock.elapsedRealtime() - chronometer.getBase());
                SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
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
            removeNotification();
            isPaused = false;
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

    public void addEncounter(Encounter encounter) {
        tourServiceManager.addEncounter(encounter);
    }

    public void notifyListenersTour(Tour tour) {
        for (TourServiceListener listener : listeners) {
            listener.onTourUpdated(tour);
        }
    }

    public void notifyListenersPosition(LatLng location) {
        for (TourServiceListener listener : listeners) {
            listener.onLocationUpdated(location);
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface TourServiceListener {
        void onTourUpdated(Tour tour);
        void onTourResumed(Tour tour);
        void onLocationUpdated(LatLng location);
    }
}
