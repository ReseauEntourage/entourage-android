package social.entourage.android.tour;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.common.Constants;
import social.entourage.android.map.MapActivity;

/**
 * Created by NTE on 06/07/15.
 * Service Local de récupération des coordonnées géographique en continu
 * pour le Run-Tracking.
 */
public class TourService extends Service {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final IBinder binder = new LocalBinder();

    /* passées dans la classe CONSTANTS
    private static final long UPDATE_TIMER_MILLIS = 1000;
    private static final float DISTANCE_BETWEEN_UPDATES_METERS = 10;
    */

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private NotificationManager notificationManager;
    private int NOTIFICATION = R.string.local_service_started;
    private CustomLocationListener locationListener;
    private Tour tour;

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
        tour = new Tour();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        initializeLocationService();
        Toast.makeText(this, R.string.local_service_started, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("LocalService", "Received Start id " + startId + ": " + intent);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        notificationManager.cancel(NOTIFICATION);
        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
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
        notificationManager.notify(NOTIFICATION, notification);
    }

    // ----------------------------------
    // METHODS
    // ----------------------------------

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS,
                Constants.DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
    }

    public void sendMaraude() {
        // traitement (ex : envoi au webservice)

        // puis arrêt du listener et du service
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        stopSelf();
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            System.out.println("NOUVELLE POSITION : " + location.getLatitude() + ", " + location.getLongitude());
            tour.updateCoordinates(new LatLng(location.getLatitude(), location.getLongitude()));
            // envoi des informations à l'activité pour que le fragment affiche la progression de la tour
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
}
