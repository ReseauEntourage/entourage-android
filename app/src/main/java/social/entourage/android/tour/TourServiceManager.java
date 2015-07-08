package social.entourage.android.tour;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import javax.inject.Singleton;

import social.entourage.android.api.model.map.Tour;
import social.entourage.android.common.Constants;

/**
 * Created by NTE on 06/07/15.
 */
@Singleton
public class TourServiceManager {

    private final TourService tourService;

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
        System.out.println("ICI : " + this.tour.getCoordinates().get(tour.getCoordinates().size()-1));
        return this.tour;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void sendTour() {
        System.out.println("----- envoi de la maraude au webservice -----");
        /*
        if (tour != null) {
            for (LatLng coord : tour.getCoordinates())
                System.out.println("coord : " + coord.latitude + ", " + coord.longitude);
            // ici envoi
        }
        */
    }

    public void startTour() {
        tour = new Tour();
        initializeLocationService();
    }

    public void finishTour() {
        sendTour();
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        locationManager.removeUpdates(locationListener);
        tour = null;
    }

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS,
                Constants.DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
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
