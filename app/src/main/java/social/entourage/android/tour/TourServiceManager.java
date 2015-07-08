package social.entourage.android.tour;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import social.entourage.android.R;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.common.Constants;

/**
 * Created by NTE on 06/07/15.
 */
@Singleton
public class TourServiceManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final TourService tourService;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private CustomLocationListener locationListener;
    private Tour tour;

    @Inject
    public TourServiceManager(final TourService tourService) {
        this.tourService = tourService;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getTour() {
        return this.tour;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS,
                Constants.DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
    }

    private void sendTour() {
        System.out.println("----- envoi de la maraude au webservice -----");
        // TODO : send the tour to the webservice

        /**
         * TEST OF THE TOUR CONTENT
         */
                for (LatLng coord : tour.getCoordinates())
                    System.out.println("+ " + coord.latitude + ", " + coord.longitude);

                DateFormat dateFormat = new SimpleDateFormat("HH'h'mm");
                for (Date time : tour.getSteps().keySet())
                    System.out.println(dateFormat.format(time) + " " + tour.getSteps().get(time));
        /**
         * END OF THE TEST
         */
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startTour() {
        tour = new Tour();
        initializeLocationService();
        addStep(new Date(), tourService.getString(R.string.tour_started));
    }

    public void finishTour() {
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        addStep(new Date(), tourService.getString(R.string.tour_stopped));
        sendTour();
        tour = null;
    }

    public void addStep(Date time, String step) {
        tour.updateSteps(time, step);
    }

    public boolean isRunning() {
        return tour != null;
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            //System.out.println("NOUVELLE POSITION : " + location.getLatitude() + ", " + location.getLongitude());
            if (tour != null) {
                tour.updateCoordinates(new LatLng(location.getLatitude(), location.getLongitude()));
                tourService.notifyListeners(tour);
            }
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
