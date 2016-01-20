package social.entourage.android.map.tour;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.Constants;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadTask;
import social.entourage.android.api.tape.EncounterTaskResult;
import social.entourage.android.tools.BusProvider;

import static android.support.v4.content.PermissionChecker.checkSelfPermission;

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
    private final EncounterRequest encounterRequest;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private Tour tour;
    private Location previousLocation;
    private long tourId;
    private int pointsNeededForNextRequest;
    private List<TourPoint> pointsToSend;
    private List<TourPoint> pointsToDraw;
    private Timer timerFinish;
    private ConnectivityManager connectivityManager;

    public TourServiceManager(final TourService tourService, final TourRequest tourRequest, final EncounterRequest encounterRequest) {
        Log.i("TourServiceManager", "constructor");
        this.tourService = tourService;
        this.tourRequest = tourRequest;
        this.encounterRequest = encounterRequest;
        this.pointsNeededForNextRequest = 1;
        this.pointsToSend = new ArrayList<>();
        this.pointsToDraw = new ArrayList<>();
        this.connectivityManager = (ConnectivityManager) this.tourService.getSystemService(Context.CONNECTIVITY_SERVICE);
        BusProvider.getInstance().register(this);
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

    public List<TourPoint> getPointsToDraw() {
        return pointsToDraw;
    }

    public void setTourDuration(String duration) {
        tour.setDuration(duration);
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void startTour(String transportMode, String type) {
        tour = new Tour(transportMode, type);
        sendTour();
    }

    public void finishTour() {
        addLastTourPoint();
        closeTour();
    }

    public boolean isRunning() {
        return tour != null;
    }

    public void addEncounter(Encounter encounter) {
        tour.addEncounter(encounter);
    }

    public void unregisterFromBus() {
        BusProvider.getInstance().unregister(this);
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    private void initializeLocationService() {
        LocationManager locationManager = (LocationManager) tourService.getSystemService(Context.LOCATION_SERVICE);
        CustomLocationListener locationListener = new CustomLocationListener();
        if (checkSelfPermission(tourService, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.UPDATE_TIMER_MILLIS,
                Constants.DISTANCE_BETWEEN_UPDATES_METERS, locationListener);
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
        tourService.sendBroadcast(new Intent(TourService.KEY_NOTIFICATION_PAUSE_TOUR));
    }

    private void addLastTourPoint() {
        Location currentLocation = EntourageLocation.getInstance().getCurrentLocation();
        if (currentLocation != null) {
            TourPoint lastPoint = new TourPoint(currentLocation.getLatitude(), currentLocation.getLongitude(), new Date());
            pointsToSend.add(lastPoint);
        }
        updateTourCoordinates();
    }

    private void sendTour() {
        Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        tourRequest.tour(tourWrapper, new Callback<Tour.TourWrapper>() {
            @Override
            public void success(Tour.TourWrapper tourWrapper, Response response) {
                initializeTimerFinishTask();
                tourId = tourWrapper.getTour().getId();
                tour.setId(tourId);
                tourService.notifyListenersTourCreated(true, tourId);

            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
                tour = null;
                tourService.notifyListenersTourCreated(false, -1);
            }
        });
    }

    private void closeTour() {
        tour.setTourStatus(Tour.TOUR_CLOSED);
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        tourRequest.closeTour(tourId, tourWrapper, new Callback<Tour.TourWrapper>() {
            @Override
            public void success(Tour.TourWrapper tourWrapper, Response response) {
                Log.d("Success", tourWrapper.toString());
                tourService.notifyListenersTourClosed(true);
                tour = null;
                pointsToSend.clear();
                pointsToDraw.clear();
                cancelFinishTimer();
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
                tourService.notifyListenersTourClosed(false);
            }
        });
    }

    private void updateTourCoordinates() {
        final TourPoint.TourPointWrapper tourPointWrapper = new TourPoint.TourPointWrapper();
        tourPointWrapper.setTourPoints(new ArrayList<>(pointsToSend));
        tourPointWrapper.setDistance(tour.getDistance());
        tourRequest.tourPoints(tourId, tourPointWrapper, new Callback<Tour.TourWrapper>() {
            @Override
            public void success(Tour.TourWrapper tourWrapper, Response response) {
                pointsToSend.removeAll(tourPointWrapper.getTourPoints());
                Log.v(this.getClass().getSimpleName(), tourWrapper.toString());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(this.getClass().getSimpleName(), error.toString());
            }
        });
    }

    protected void retrieveToursNearbyLarge() {
        CameraPosition currentPosition = EntourageLocation.getInstance().getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            float zoom = currentPosition.zoom;
            float distance = 40000f / (float) Math.pow(2f, zoom) / 2.5f;
            tourRequest.retrieveToursNearby(10, null, null, location.latitude, location.longitude, distance, new Callback<Tour.ToursWrapper>() {
                @Override
                public void success(Tour.ToursWrapper toursWrapper, Response response) {
                    tourService.notifyListenersToursNearby(toursWrapper.getTours());
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("Error", error.toString());
                }
            });
        }
    }

    protected void retrieveToursNearbySmall(LatLng point) {
        if (point != null) {
            tourRequest.retrieveToursNearby(5, null, null, point.latitude, point.longitude, 0.04, new Callback<Tour.ToursWrapper>() {
                @Override
                public void success(Tour.ToursWrapper toursWrapper, Response response) {
                    Map<Long, Tour> toursMap = new HashMap<>();
                    for (Tour tour : toursWrapper.getTours()) {
                        toursMap.put(tour.getId(), tour);
                    }
                    tourService.notifyListenersToursFound(toursMap);
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e("Error", error.toString());
                }
            });
        }
    }

    protected void retrieveToursByUserId(int userId, int page, int per) {
        tourRequest.retrieveToursByUserId(userId, page, per, new Callback<Tour.ToursWrapper>() {
            @Override
            public void success(Tour.ToursWrapper toursWrapper, Response response) {
                tourService.notifyListenersUserToursFound(toursWrapper.getTours());
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("Error", error.toString());
            }
        });
    }

    protected void sendEncounter(final Encounter encounter) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Encounter.EncounterWrapper encounterWrapper = new Encounter.EncounterWrapper();
            encounterWrapper.setEncounter(encounter);
            encounterRequest.create(encounter.getTourId(), encounterWrapper, new Callback<EncounterResponse>() {
                @Override
                public void success(EncounterResponse encounterResponse, Response response) {
                    Log.d("tape:", "success");
                    BusProvider.getInstance().post(new EncounterTaskResult(true, encounter));
                    //Toast.makeText(tourService, "envoyé", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.d("tape:", "failure");
                    BusProvider.getInstance().post(new EncounterTaskResult(false, null));
                    //Toast.makeText(tourService, "erreur", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.d("tape:", "no network");
            BusProvider.getInstance().post(new EncounterTaskResult(false, null));
            //Toast.makeText(tourService, "pas de réseau", Toast.LENGTH_SHORT).show();
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    public void cancelFinishTimer() {
        if (timerFinish != null) {
            timerFinish.cancel();
            timerFinish = null;
        }
    }

    private void onLocationChanged(Location location, TourPoint point) {
        pointsToDraw.add(point);
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
        tourService.notifyListenersTourUpdated(new LatLng(location.getLatitude(), location.getLongitude()));
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
    // BUS LISTENERS
    // ----------------------------------

    @Subscribe
    public void encounterToSend(EncounterUploadTask task) {
        sendEncounter(task.getEncounter());
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
