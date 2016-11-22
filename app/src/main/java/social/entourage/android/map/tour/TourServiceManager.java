package social.entourage.android.map.tour;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.Constants;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.EntourageDate;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.EncounterTaskResult;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadTask;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.tools.BusProvider;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.LOCATION_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static android.support.v4.content.PermissionChecker.checkSelfPermission;

/**
 * Manager is like a presenter but for a service
 * controlling the TourService
 *
 * @see TourService
 */
public class TourServiceManager {

    // ----------------------------------
    // CONSTANTS
    // ----------------------------------

    private static final int POINT_PER_REQUEST = 10;
    private static final double ALIGNMENT_PRECISION = .000001;
    private static final long VIBRATION_DURATION = 1000;
    private static final double RETRIEVE_TOURS_DISTANCE = 0.04;
    private static final double MAX_DISTANCE_BETWEEN_TWO_POINTS = 50; //meters
    private static final double MAX_DISTANCE_TO_LINE = .0005;

    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private final TourService tourService;
    private final TourRequest tourRequest;
    private final EncounterRequest encounterRequest;
    private final NewsfeedRequest newsfeedRequest;
    private final EntourageRequest entourageRequest;
    private final ConnectivityManager connectivityManager;
    private final EntourageLocation entourageLocation;

    private Tour tour;
    private Location previousLocation;
    private long tourId;
    private int pointsNeededForNextRequest;
    private List<TourPoint> pointsToSend;
    private List<TourPoint> pointsToDraw;
    private Timer timerFinish;
    private boolean isTourClosing;

    Call<Newsfeed.NewsfeedWrapper> retrieveNewsfeedCall; // current call to retrieve newsfeed

    private LocationManager locationManager;
    private CustomLocationListener locationListener;
    private boolean isBetterLocationUpdated;

    TourServiceManager(final TourService tourService,
                       final TourRequest tourRequest,
                       final EncounterRequest encounterRequest,
                       final NewsfeedRequest newsfeedRequest,
                       final EntourageRequest entourageRequest,
                       final ConnectivityManager connectivityManager,
                       final EntourageLocation entourageLocation) {
        this.tourService = tourService;
        this.tourRequest = tourRequest;
        this.encounterRequest = encounterRequest;
        this.newsfeedRequest = newsfeedRequest;
        this.entourageRequest = entourageRequest;
        this.pointsNeededForNextRequest = 1;
        this.pointsToSend = new ArrayList<>();
        this.pointsToDraw = new ArrayList<>();
        this.isTourClosing = false;
        this.connectivityManager = connectivityManager;
        this.entourageLocation = entourageLocation;
    }

    public static TourServiceManager newInstance(final TourService tourService,
                                                 final TourRequest tourRequest,
                                                 final EncounterRequest encounterRequest,
                                                 final NewsfeedRequest newsfeedRequest,
                                                 final EntourageRequest entourageRequest) {
        Log.i("TourServiceManager", "newInstance");
        ConnectivityManager connectivityManager = (ConnectivityManager) tourService.getSystemService(CONNECTIVITY_SERVICE);
        EntourageLocation entourageLocation = EntourageLocation.getInstance();
        TourServiceManager tourServiceManager = new TourServiceManager(
            tourService,
            tourRequest,
            encounterRequest,
            newsfeedRequest,
            entourageRequest,
            connectivityManager,
            entourageLocation);
        BusProvider.getInstance().register(tourServiceManager);
        tourServiceManager.initializeLocationService();
        return tourServiceManager;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getTour() {
        return this.tour;
    }

    public long getTourId() {
        if (this.tour != null) {
            return this.tour.getId();
        }
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

    public void stopLocationService() {
        if (checkPermission()) {
            if (locationListener != null) {
                locationManager.removeUpdates(locationListener);
                locationManager = null;
            }
        }
    }

    public void startTour(String type) {
        tour = new Tour(type);
        sendTour();
    }

    public void finishTour() {
        //addLastTourPoint();
        //closeTour();

        isTourClosing = true;
        updateTourCoordinates();
    }

    public void finishTour(Tour tour) {
        closeTour(tour);
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

    public void updateTourCoordinates() {
        if (pointsToSend.isEmpty()) {
            if (isTourClosing) {
                closeTour();
                isTourClosing = false;
            }
            return;
        }
        final TourPoint.TourPointWrapper tourPointWrapper = new TourPoint.TourPointWrapper();
        tourPointWrapper.setTourPoints(new ArrayList<>(pointsToSend));
        tourPointWrapper.setDistance(tour.getDistance());
        Call<Tour.TourWrapper> call = tourRequest.tourPoints(tourId, tourPointWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    pointsToSend.removeAll(tourPointWrapper.getTourPoints());
                    if (isTourClosing) {
                        closeTour();
                    }
                } else {
                    if (isTourClosing) {
                        tourService.notifyListenersFeedItemClosed(false, tour);
                    }
                }
                isTourClosing = false;
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                if (isTourClosing) {
                    tourService.notifyListenersFeedItemClosed(false, tour);
                }
                isTourClosing = false;
                Log.e(this.getClass().getSimpleName(), t.getLocalizedMessage());
            }
        });
    }

    public void cancelFinishTimer() {
        if (timerFinish != null) {
            timerFinish.cancel();
            timerFinish = null;
        }
    }

    public boolean isLocationInTour(LatLng latLng) {
        if (tour == null) {
            return false;
        }
        List<TourPoint> tourPoints = tour.getTourPoints();
        if (tourPoints.isEmpty()) {
            return false;
        }
        if (tourPoints.size() == 1) {
            TourPoint p = tourPoints.get(0);
            float[] distance = {0};
            Location.distanceBetween(p.getLatitude(), p.getLongitude(), latLng.latitude, latLng.longitude, distance);
            if (distance[0] <= MAX_DISTANCE_BETWEEN_TWO_POINTS) {
                return true;
            }
        } else {
            TourPoint targetPoint = new TourPoint(latLng.latitude, latLng.longitude);
            for (int i = 1; i < tourPoints.size(); i++) {
                TourPoint p1 = tourPoints.get(i - 1);
                TourPoint p2 = tourPoints.get(i);
                double d = distanceToLine(p1, targetPoint, p2);
                if (d <= MAX_DISTANCE_TO_LINE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Subscribe
    public void onLocationPermissionGranted(OnLocationPermissionGranted event) {
        if (locationListener == null && event.isPermissionGranted()) {
            initializeLocationService();
        }
    }

    @Subscribe
    public void encounterToSend(EncounterUploadTask task) {
        sendEncounter(task.getEncounter());
    }

    protected void freezeTour(final Tour tour) {
        if (tour == null) {
            return;
        }
        tour.setTourStatus(FeedItem.STATUS_FREEZED);
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        Call<Tour.TourWrapper> call = tourRequest.closeTour(tour.getId(), tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    Log.d("Success", response.body().getTour().toString());
                    tourService.notifyListenersFeedItemClosed(true, response.body().getTour());
                } else {
                    tourService.notifyListenersFeedItemClosed(false, tour);
                }
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
                tourService.notifyListenersFeedItemClosed(false, tour);
            }
        });
    }

    protected void retrieveToursNearbyLarge() {
        CameraPosition currentPosition = entourageLocation.getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            float zoom = currentPosition.zoom;
            //float distance = 40000f / (float) Math.pow(2f, zoom) / 2.5f;
            float distance = 10; //kilometers
            Call<Tour.ToursWrapper> call = tourRequest.retrieveToursNearby(10, null, null, location.latitude, location.longitude, distance);
            call.enqueue(new Callback<Tour.ToursWrapper>() {
                @Override
                public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                    if (response.isSuccess()) {
                        tourService.notifyListenersToursNearby(response.body().getTours());
                    }
                }

                @Override
                public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                    Log.e("Error", t.getLocalizedMessage());
                }
            });
        }
    }

    protected void retrieveToursNearbySmall(final LatLng point, final boolean isUserHistory, final int userId, final int page, final int per) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final ExecutorService executorService = Executors.newFixedThreadPool((isUserHistory ? 2 : 1));
                    final Future<List<Tour>> futureCloseTours;
                    final Future<List<Tour>> futureCloseUserTours;
                    final List<Tour> closeTours;
                    final List<Tour> closeUserTours;

                    futureCloseTours = executorService.submit(
                        new Callable<List<Tour>>() {
                            @Override
                            public List<Tour> call() throws Exception {
                                return tourRequest.retrieveToursNearby(5, null, null, point.latitude, point.longitude, RETRIEVE_TOURS_DISTANCE)
                                    .execute().body().getTours();
                            }
                        }
                    );
                    closeTours = futureCloseTours.get();
                    //if (isUserHistory) {
                    futureCloseUserTours = executorService.submit(
                        new Callable<List<Tour>>() {
                            @Override
                            public List<Tour> call() throws Exception {
                                return tourRequest.retrieveToursByUserIdAndPoint(userId, page, per, point.latitude, point.longitude, RETRIEVE_TOURS_DISTANCE)
                                    .execute().body().getTours();
                            }
                        }
                    );
                    closeUserTours = futureCloseUserTours.get();
                    //}
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            Map<Long, Tour> toursMap = new HashMap<>();
                            for (Tour tour : closeTours) {
                                toursMap.put(tour.getId(), tour);
                            }
                            //if (isUserHistory) {
                            for (Tour tour : closeUserTours) {
                                toursMap.put(tour.getId(), tour);
                            }
                            //}
                            tourService.notifyListenersToursFound(toursMap);
                        }
                    });
                } catch (InterruptedException | ExecutionException e) {
                    Log.e(TourServiceManager.class.getSimpleName(), "", e);
                }
            }
        }).start();
    }

    protected void retrieveToursByUserId(int userId, int page, int per) {
        Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserId(userId, page, per);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                if (response.isSuccess()) {
                    tourService.notifyListenersUserToursFound(response.body().getTours());
                }
            }

            @Override
            public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
            }
        });
    }

    protected void retrieveToursByUserIdAndPoint(int userId, int page, int per, LatLng point) {
        Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserIdAndPoint(userId, page, per, point.latitude, point.longitude, RETRIEVE_TOURS_DISTANCE);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                if (response.isSuccess()) {
                    Map<Long, Tour> toursMap = new HashMap<>();
                    for (Tour tour : response.body().getTours()) {
                        toursMap.put(tour.getId(), tour);
                    }
                    tourService.notifyListenersUserToursFoundFromPoint(toursMap);
                }
            }

            @Override
            public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
            }
        });
    }

    protected void retrieveNewsFeed(Date beforeDate) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            tourService.notifyListenersNetworkException();
            return;
        }

        CameraPosition currentPosition = entourageLocation.getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            MapFilter mapFilter = MapFilter.getInstance();
            retrieveNewsfeedCall = createNewsfeedWrapperCall(beforeDate, location, mapFilter);
            retrieveNewsfeedCall.enqueue(new NewsFeedCallback(this));
        } else {
            tourService.notifyListenersCurrentPositionNotRetrieved();
        }
    }

    protected void cancelNewsFeedRetrieval() {
        if (retrieveNewsfeedCall == null || retrieveNewsfeedCall.isCanceled()) {
            return;
        }
        retrieveNewsfeedCall.cancel();
    }

    protected void sendEncounter(final Encounter encounter) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Encounter.EncounterWrapper encounterWrapper = new Encounter.EncounterWrapper();
            encounterWrapper.setEncounter(encounter);
            Call<EncounterResponse> call = encounterRequest.create(encounter.getTourId(), encounterWrapper);
            call.enqueue(new Callback<EncounterResponse>() {
                @Override
                public void onResponse(Call<EncounterResponse> call, Response<EncounterResponse> response) {
                    if (response.isSuccess()) {
                        Log.d("tape:", "success");
                        BusProvider.getInstance().post(new EncounterTaskResult(true, encounter));
                    }
                }

                @Override
                public void onFailure(Call<EncounterResponse> call, Throwable t) {
                    Log.d("tape:", "failure");
                    BusProvider.getInstance().post(new EncounterTaskResult(false, null));
                }
            });
        } else {
            Log.d("tape:", "no network");
            BusProvider.getInstance().post(new EncounterTaskResult(false, null));
            //Toast.makeText(tourService, "pas de réseau", Toast.LENGTH_SHORT).show();
        }
    }

    protected void requestToJoinTour(final Tour tour) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Call<TourUser.TourUserWrapper> call = tourRequest.requestToJoinTour(tour.getId());
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccess()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), tour);
                    } else {
                        tourService.notifyListenersUserStatusChanged(null, tour);
                    }
                }

                @Override
                public void onFailure(final Call<TourUser.TourUserWrapper> call, final Throwable t) {
                    tourService.notifyListenersUserStatusChanged(null, tour);
                }
            });
        } else {
            tourService.notifyListenersUserStatusChanged(null, tour);
        }
    }

    protected void removeUserFromTour(final Tour tour, int userId) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Call<TourUser.TourUserWrapper> call = tourRequest.removeUserFromTour(tour.getId(), userId);
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccess()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), tour);
                    } else {
                        tourService.notifyListenersUserStatusChanged(null, tour);
                    }
                }

                @Override
                public void onFailure(final Call call, final Throwable t) {
                    tourService.notifyListenersUserStatusChanged(null, tour);
                }
            });
        } else {
            tourService.notifyListenersUserStatusChanged(null, tour);
        }
    }

    protected void closeEntourage(final Entourage entourage) {
        final String oldStatus = entourage.getStatus();
        entourage.setStatus(FeedItem.STATUS_CLOSED);
        entourage.setEndTime(new Date());
        final Entourage.EntourageWrapper entourageWrapper = new Entourage.EntourageWrapper();
        entourageWrapper.setEntourage(entourage);
        Call<Entourage.EntourageWrapper> call = entourageRequest.closeEntourage(entourage.getId(), entourageWrapper);
        call.enqueue(new Callback<Entourage.EntourageWrapper>() {
            @Override
            public void onResponse(Call<Entourage.EntourageWrapper> call, Response<Entourage.EntourageWrapper> response) {
                if (response.isSuccess()) {
                    Log.d("Success", response.body().getEntourage().toString());
                    tourService.notifyListenersFeedItemClosed(true, response.body().getEntourage());
                } else {
                    entourage.setStatus(oldStatus);
                    tourService.notifyListenersFeedItemClosed(false, entourage);
                }
            }

            @Override
            public void onFailure(Call<Entourage.EntourageWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
                entourage.setStatus(oldStatus);
                tourService.notifyListenersFeedItemClosed(false, entourage);
            }
        });
    }

    protected void requestToJoinEntourage(final Entourage entourage) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Call<TourUser.TourUserWrapper> call = entourageRequest.requestToJoinEntourage(entourage.getId());
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccess()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), entourage);
                    } else {
                        tourService.notifyListenersUserStatusChanged(null, entourage);
                    }
                }

                @Override
                public void onFailure(final Call<TourUser.TourUserWrapper> call, final Throwable t) {
                    tourService.notifyListenersUserStatusChanged(null, tour);
                }
            });
        } else {
            tourService.notifyListenersUserStatusChanged(null, tour);
        }
    }

    protected void removeUserFromEntourage(final Entourage entourage, int userId) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Call<TourUser.TourUserWrapper> call = entourageRequest.removeUserFromEntourage(entourage.getId(), userId);
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccess()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), entourage);
                    } else {
                        tourService.notifyListenersUserStatusChanged(null, entourage);
                    }
                }

                @Override
                public void onFailure(final Call call, final Throwable t) {
                    tourService.notifyListenersUserStatusChanged(null, entourage);
                }
            });
        } else {
            tourService.notifyListenersUserStatusChanged(null, entourage);
        }
    }

    private Call<Newsfeed.NewsfeedWrapper> createNewsfeedWrapperCall(Date beforeDate, LatLng location, MapFilter mapFilter) {
        return newsfeedRequest.retrieveFeed(
            (beforeDate == null ? null : new EntourageDate(beforeDate)),
            location.longitude,
            location.latitude,
            mapFilter.getTourTypes(),
            mapFilter.showTours,
            mapFilter.onlyMyEntourages,
            mapFilter.getEntourageTypes(),
            mapFilter.timeframe
        );
    }

    private boolean checkPermission() {
        return (checkSelfPermission(tourService, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            checkSelfPermission(tourService, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void initializeLocationService() {
        if (checkPermission()) {
            locationManager = (LocationManager) tourService.getSystemService(LOCATION_SERVICE);
            locationListener = new CustomLocationListener();
            updateLocationServiceFrequency();
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if (lastKnownLocation != null) {
                entourageLocation.setInitialLocation(lastKnownLocation);
            }
        }
    }

    private void updateLocationServiceFrequency() {
        if (checkPermission()) {
            long minTime = Constants.UPDATE_TIMER_MILLIS_OFF_TOUR;
            float minDistance = Constants.DISTANCE_BETWEEN_UPDATES_METERS_OFF_TOUR;
            if (tour != null) {
                minTime = Constants.UPDATE_TIMER_MILLIS_ON_TOUR;
                minDistance = Constants.DISTANCE_BETWEEN_UPDATES_METERS_ON_TOUR;
            }
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener);
            } catch (Exception ex) {
                Log.d("Entourage", "No GPS Provider");
            }
        }
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
        Vibrator vibrator = (Vibrator) tourService.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATION_DURATION);
        tourService.sendBroadcast(new Intent(TourService.KEY_NOTIFICATION_PAUSE_TOUR));
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    private void sendTour() {
        Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        Call<Tour.TourWrapper> call = tourRequest.tour(tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    //initializeLocationService();
                    Location currentLocation = entourageLocation.getCurrentLocation();
                    if (currentLocation != null) {
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        BusProvider.getInstance().post(new OnBetterLocationEvent(latLng));
                    }
                    updateLocationServiceFrequency();
                    initializeTimerFinishTask();
                    tourId = response.body().getTour().getId();
                    //tour.setId(tourId);
                    tour = response.body().getTour();
                    tourService.notifyListenersTourCreated(true, tourId);

                    if (checkPermission()) {
                        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location == null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        }
                        if (location != null) {
                            TourPoint point = new TourPoint(location.getLatitude(), location.getLongitude());
                            tour.addCoordinate(point);
                            pointsToDraw.add(point);
                            pointsToSend.add(point);
                            previousLocation = location;
                            updateTourCoordinates();
                            tourService.notifyListenersTourUpdated(new LatLng(location.getLatitude(), location.getLongitude()));
                        } else {
                            Log.e(this.getClass().getSimpleName(), "no location provided");
                        }
                    }
                } else {
                    tour = null;
                    tourService.notifyListenersTourCreated(false, -1);
                }
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
                tour = null;
                tourService.notifyListenersTourCreated(false, -1);
            }
        });
    }

    private void closeTour() {
        tour.setTourStatus(FeedItem.STATUS_CLOSED);
        tour.setEndTime(new Date());
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        Call<Tour.TourWrapper> call = tourRequest.closeTour(tourId, tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    Log.d("Success", response.body().getTour().toString());
                    tour = null;
                    pointsToSend.clear();
                    pointsToDraw.clear();
                    cancelFinishTimer();
                    updateLocationServiceFrequency();
                    tourService.notifyListenersFeedItemClosed(true, response.body().getTour());
                } else {
                    tourService.notifyListenersFeedItemClosed(false, tour);
                }
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
                tourService.notifyListenersFeedItemClosed(false, tour);
            }
        });
    }

    private void closeTour(final Tour tour) {
        tour.setTourStatus(FeedItem.STATUS_CLOSED);
        tour.setEndTime(new Date());
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        Call<Tour.TourWrapper> call = tourRequest.closeTour(tour.getId(), tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(Call<Tour.TourWrapper> call, Response<Tour.TourWrapper> response) {
                if (response.isSuccess()) {
                    Log.d("Success", response.body().getTour().toString());
                    tourService.notifyListenersFeedItemClosed(true, response.body().getTour());
                } else {
                    tourService.notifyListenersFeedItemClosed(false, tour);
                }
            }

            @Override
            public void onFailure(Call<Tour.TourWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
                tourService.notifyListenersFeedItemClosed(false, tour);
            }
        });
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

        tour.addCoordinate(new TourPoint(location.getLatitude(), location.getLongitude()));
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

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

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

    static class NewsFeedCallback implements Callback<Newsfeed.NewsfeedWrapper> {
        private static final String EMPTY_STRING = "";
        private final TourServiceManager manager;

        NewsFeedCallback(TourServiceManager manager) {
            this.manager = manager;
        }

        @Override
        public void onResponse(Call<Newsfeed.NewsfeedWrapper> call, Response<Newsfeed.NewsfeedWrapper> response) {
            if (!call.isCanceled()) {
                if (response.isSuccess()) {
                    List<Newsfeed> newsFeedList = response.body().getNewsfeed();
                    if (newsFeedList == null) {
                        manager.tourService.notifyListenersTechnicalException(new Throwable("Null newsfeed list"));
                    } else {
                        manager.tourService.notifyListenersNewsFeedReceived(newsFeedList);
                    }
                } else {
                    manager.tourService.notifyListenersServerException(new Throwable(getErrorMessage(response)));
                }
            }
            manager.retrieveNewsfeedCall = null;
        }

        @Override
        public void onFailure(Call<Newsfeed.NewsfeedWrapper> call, Throwable t) {
            if (!call.isCanceled()) {
                manager.tourService.notifyListenersTechnicalException(t);
            }
            manager.retrieveNewsfeedCall = null;
        }

        private String getErrorMessage(Response<Newsfeed.NewsfeedWrapper> response) {
            String errorBody = getErrorBody(response);
            String errorMessage = "Response code = " + response.code();
            if (!errorBody.isEmpty()) {
                errorMessage += " : " + errorBody;
            }
            return errorMessage;
        }

        @NonNull
        private String getErrorBody(Response<Newsfeed.NewsfeedWrapper> response) {
            try {
                return response.errorBody().string();
            } catch (IOException ignored) {
                return EMPTY_STRING;
            }
        }
    }

    private class CustomLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            updateLocation(location);
            tourService.notifyListenersPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            if (tour != null && !tourService.isPaused()) {
                TourPoint point = new TourPoint(location.getLatitude(), location.getLongitude());
                TourServiceManager.this.onLocationChanged(location, point);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Intent intent = new Intent();
            intent.setAction(TourService.KEY_GPS_ENABLED);
            TourServiceManager.this.tourService.sendBroadcast(intent);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Intent intent = new Intent();
            intent.setAction(TourService.KEY_GPS_DISABLED);
            TourServiceManager.this.tourService.sendBroadcast(intent);
        }

        private void updateLocation(Location location) {
            entourageLocation.saveCurrentLocation(location);
            Location bestLocation = entourageLocation.getLocation();
            boolean shouldCenterMap = false;
            if (bestLocation == null || (location.getAccuracy() > 0.0 && bestLocation.getAccuracy() == 0.0)) {
                entourageLocation.saveLocation(location);
                isBetterLocationUpdated = true;
                shouldCenterMap = true;
            }

            if (isBetterLocationUpdated) {
                isBetterLocationUpdated = false;
                LatLng latLng = entourageLocation.getLatLng();
                if (shouldCenterMap) {
                    BusProvider.getInstance().post(new OnBetterLocationEvent(latLng));
                }
            }
        }
    }
}
