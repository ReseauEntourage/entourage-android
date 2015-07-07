package social.entourage.android.tour;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;
import social.entourage.android.EntourageApplication;
import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.map.MapActivity;

/**
 * Created by NTE on 06/07/15.
 */
public class TourService extends Service {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final IBinder binder = new LocalBinder();

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private ObjectGraph activityGraph;

    @Inject
    TourServiceManager tourServiceManager;

    private List<TourServiceListener> listeners;

    private NotificationManager notificationManager;

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
        ObjectGraph applicationGraph = EntourageApplication.get(this).getApplicationGraph();
        activityGraph = applicationGraph.plus(Arrays.<Object>asList(new TourModule(this)).toArray());
        activityGraph.inject(this);

        listeners =  new ArrayList<>();

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received Start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        activityGraph = null;
        //endTreatment();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private void showNotification() {
        CharSequence text = getText(R.string.local_service_started);
        Notification notification = new Notification(R.drawable.maraude_record, text, System.currentTimeMillis());
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MapActivity.class), 0);
        notification.setLatestEventInfo(this, getText(R.string.local_service_label), text, contentIntent);
        notificationManager.notify(R.string.local_service_started, notification);
    }

    private void removeNotification() {
        notificationManager.cancel(R.string.local_service_started);
    }

    // ----------------------------------
    // METHODS
    // ----------------------------------

    public void beginTreatment() {
        if (!isRunning()) {
            tourServiceManager.startTour();
            showNotification();
            Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
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

    public void notifyListeners(Tour tour) {
        for (TourServiceListener listener : listeners) listener.onTourUpdated(tour);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    public interface TourServiceListener {
        void onTourUpdated(Tour tour);
    }
}
