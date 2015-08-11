package social.entourage.android.map.tour;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.R;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.TourPointWrapper;
import social.entourage.android.api.model.TourWrapper;
import social.entourage.android.api.model.ToursWrapper;
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

    private static final int POINT_PER_REQUEST = 10;
    private static final double ALIGNMENT_PRECISION = .000001;
    private static final long VIBRATION_DURATION = 1000;

    private final TourService tourService;
    private final TourRequest tourRequest;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private CustomLocationListener locationListener;
    private Tour tour;
    private Location previousLocation;
    private long tourId;
    private int pointsNeededForNextRequest;
    private List<TourPoint> pointsToSend;
    private Timer timerRefresh;
    private Timer timerFinish;

    @Inject
    public TourServiceManager(final TourService tourService, final TourRequest tourRequest) {
        this.tourService = tourService;
        this.tourRequest = tourRequest;
        this.pointsNeededForNextRequest = 1;
        this.pointsToSend = new ArrayList<>();
        initializeLocationService();
        initializeTimerRefreshTask();
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
    // PUBLIC METHODS
    // ----------------------------------

    public void startTour(String transportMode, String type) {
        tour = new Tour();
        tour.setTourVehicleType(transportMode);
        tour.setTourType(type);
        sendTour();
        initializeTimerFinishTask();
    }

    public void finishTour() {
        tour.closeTour();
        closeTour();
        tour = null;
        cancelFinishTimer();
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
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new CustomLocationListener();
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS,
                Constants.DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
    }

    private void initializeTimerRefreshTask() {
        timerRefresh = new Timer();
        timerRefresh.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                CameraPosition currentPosition = EntourageLocation.getInstance().getCurrentCameraPosition();
                if (currentPosition != null) {
                    LatLng location = currentPosition.target;
                    float zoom = currentPosition.zoom;
                    float distance = 40000f / (float) Math.pow(2f, zoom);
                    retrieveToursNearby(5, null, null, new LatLng(location.latitude, location.longitude), distance);
                }
            }
        }, 1000, 5000);
    }

    private void initializeTimerFinishTask() {
        long duration = 1000 * 60 * 60 * 5;
        timerFinish = new Timer();
        timerFinish.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeOut();
            }
        }, duration, duration);
    }

    private void timeOut() {
        Vibrator vibrator = (Vibrator) tourService.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATION_DURATION);
        tourService.sendBroadcast(new Intent(TourService.NOTIFICATION_PAUSE));
    }

    private void sendTour() {
        TourWrapper tourWrapper = new TourWrapper();
        tourWrapper.setTour(tour);
        tourRequest.tour(tourWrapper, new Callback<TourWrapper>() {
            @Override
            public void success(TourWrapper tourWrapper, Response response) {
                tourService.notifyListenersToursCountUpdated();
                tourId = tourWrapper.getTour().getId();
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

    private void updateTourCoordinates() {
        final TourPointWrapper tourPointWrapper = new TourPointWrapper();
        tourPointWrapper.setTourPoints(new ArrayList<>(pointsToSend));
        tourRequest.tourPoints(tourId, tourPointWrapper, new Callback<TourWrapper>() {
            @Override
            public void success(TourWrapper tourWrapper, Response response) {
                pointsToSend.removeAll(tourPointWrapper.getTourPoints());
                Log.v("Success", tourWrapper.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
    }

    protected void retrieveToursNearby(int limit, String transportMode, String type, LatLng location, float distance) {
        tourRequest.retrieveToursNearby(limit, transportMode, type, location.latitude, location.longitude, distance, new Callback<ToursWrapper>() {
            @Override
            public void success(ToursWrapper toursWrapper, Response response) {
                tourService.notifyListenersToursNearby(toursWrapper.getTours());
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

    public void cancelRefreshTimer() {
        if (timerRefresh != null) {
            timerRefresh.cancel();
            timerRefresh = null;
        }
    }

    public void cancelFinishTimer() {
        if (timerFinish != null) {
            timerFinish.cancel();
            timerFinish = null;
        }
    }

    private void onLocationChanged(Location location, TourPoint point) {
        pointsToSend.add(point);
        if (pointsToSend.size() >= 3) {
            TourPoint a = pointsToSend.get(pointsToSend.size() - 3);
            TourPoint b = pointsToSend.get(pointsToSend.size() - 2);
            TourPoint c = pointsToSend.get(pointsToSend.size() - 1);
            if (distanceToLine(a, b, c) < ALIGNMENT_PRECISION) {
                pointsToSend.remove(b);
            }
        }
        pointsNeededForNextRequest--;

        tour.addCoordinate(new TourPoint(location.getLatitude(), location.getLongitude(), new Date()));
        if (previousLocation != null) {
            tour.updateDistance(location.distanceTo(previousLocation));
        }
        previousLocation = location;

        if (isWebServiceUpdateNeeded()) {
            pointsNeededForNextRequest = POINT_PER_REQUEST;
            updateTourCoordinates();
        }
        tourService.notifyListenersTour(tour);
    }

    private boolean isWebServiceUpdateNeeded() {
        return pointsNeededForNextRequest <= 0;
    }

    private double distanceToLine(TourPoint startPoint, TourPoint middlePoint, TourPoint endPoint) {
        double scalarProduct = (middlePoint.getLatitude() - startPoint.getLatitude()) * (endPoint.getLatitude() - startPoint.getLatitude()) + (middlePoint.getLongitude() - startPoint.getLongitude()) * (endPoint.getLongitude() - startPoint.getLongitude());
        double distanceProjection = scalarProduct / Math.sqrt(Math.pow(endPoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(endPoint.getLongitude() - startPoint.getLongitude(), 2));
        double distanceToMiddle = Math.sqrt(Math.pow(middlePoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(middlePoint.getLongitude() - startPoint.getLongitude(), 2));
        return Math.sqrt(Math.pow(distanceToMiddle, 2) - Math.pow(distanceProjection, 2));
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
                TourServiceManager.this.onLocationChanged(location, point);
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
