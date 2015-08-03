package social.entourage.android.map.tour;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.TourPointWrapper;
import social.entourage.android.api.model.TourWrapper;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.Constants;

/**
 * Manager is like a presenter but for a service
 * controlling the TourService
 * @see TourService
 */
public class TourServiceManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private final TourService tourService;
    private final TourRequest tourRequest;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private CustomLocationListener locationListener;
    private Tour tour;
    private Location previousLocation;
    private long tourId;

    @Inject
    public TourServiceManager(final TourService tourService, final TourRequest tourRequest) {
        this.tourService = tourService;
        this.tourRequest = tourRequest;
        initializeLocationService();
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getTour() {
        return this.tour;
    }

    public long getTourId() {
        return tourId;
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
        TourWrapper tourWrapper = new TourWrapper();
        tourWrapper.setTour(tour);
        tourRequest.tour(tourWrapper, new Callback<TourWrapper>() {
            @Override
            public void success(TourWrapper tourWrapper, Response response) {
                tourId = tourWrapper.getTour().getId();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
    }

    private void retrieveTour(long id) {
        tourRequest.retrieveTourById(id, new Callback<TourWrapper>() {
            @Override
            public void success(TourWrapper tourWrapper, Response response) {
                Log.v("Success", tourWrapper.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
    }

    private void closeTour() {
        final TourWrapper tourWrapper = new TourWrapper();
        tourWrapper.setTour(tour);
        tourRequest.closeTour(tourId, tourWrapper, new Callback<TourWrapper>() {
            @Override
            public void success(TourWrapper tourWrapper, Response response) {
                Log.d("Success", tourWrapper.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
    }

    private void updateTourCoordinates(TourPoint point) {
        List<TourPoint> points = new ArrayList<>();
        points.add(point);
        final TourPointWrapper tourPointWrapper = new TourPointWrapper();
        tourPointWrapper.setTourPoints(points);
        tourRequest.tourPoints(tourId, tourPointWrapper, new Callback<TourWrapper>() {
            @Override
            public void success(TourWrapper tourWrapper, Response response) {
                Log.v("Success", tourWrapper.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startTour(String transportMode, String type) {
        tour = new Tour();
        tour.setTourVehiculeType(transportMode); // feet, car
        tour.setTourType(type); // social, other, food
        sendTour();
    }

    public void finishTour() {
        tour.closeTour();
        closeTour();
        tour = null;
    }

    public boolean isRunning() {
        return tour != null;
    }

    public void setTourDuration(String duration) {
        tour.setDuration(duration);
    }

    public void addEncounter(Encounter encounter) {
        tour.addEncounter(encounter);
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            tourService.notifyListenersPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            if (tour != null && !tourService.isPaused()) {
                TourPoint point = new TourPoint(location.getLatitude(), location.getLongitude(), new Date());
                updateTourCoordinates(point);
                tour.addCoordinate(new LatLng(location.getLatitude(), location.getLongitude()));
                tourService.notifyListenersTour(tour);
                if (previousLocation != null) {
                    tour.updateDistance(location.distanceTo(previousLocation));
                }
                previousLocation = location;
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
