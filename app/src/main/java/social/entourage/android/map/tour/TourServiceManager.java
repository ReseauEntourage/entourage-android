package social.entourage.android.map.tour;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.otto.Subscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.EntourageDate;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.EncounterTaskResult;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.map.encounter.CreateEncounterPresenter.EncounterUploadTask;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.map.filter.MapFilterFactory;
import social.entourage.android.map.tour.FusedLocationProvider.ProviderStatusListener;
import social.entourage.android.map.tour.FusedLocationProvider.UserType;
import social.entourage.android.tools.BusProvider;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static social.entourage.android.map.tour.FusedLocationProvider.UserType.PRO;
import static social.entourage.android.map.tour.FusedLocationProvider.UserType.PUBLIC;

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
    private static final double MAX_DISTANCE_BETWEEN_TWO_POINTS = 200; //meters
    private static final double MAX_DISTANCE_TO_LINE = .0020;

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
    private final FusedLocationProvider provider;

    private Tour tour;
    private Location previousLocation;
    private long tourId;
    private int pointsNeededForNextRequest;
    private List<TourPoint> pointsToSend;
    private List<TourPoint> pointsToDraw;
    private Timer timerFinish;
    private boolean isTourClosing;
    private Call<Newsfeed.NewsfeedWrapper> currentNewsFeedCall;

    private boolean isBetterLocationUpdated;

    TourServiceManager(final TourService tourService,
                       final TourRequest tourRequest,
                       final EncounterRequest encounterRequest,
                       final NewsfeedRequest newsfeedRequest,
                       final EntourageRequest entourageRequest,
                       final ConnectivityManager connectivityManager,
                       final EntourageLocation entourageLocation,
                       final FusedLocationProvider provider) {
        this.tourService = tourService;
        this.tourRequest = tourRequest;
        this.encounterRequest = encounterRequest;
        this.newsfeedRequest = newsfeedRequest;
        this.entourageRequest = entourageRequest;
        this.provider = provider;
        this.pointsNeededForNextRequest = 1;
        this.pointsToSend = new ArrayList<>();
        this.pointsToDraw = new ArrayList<>();
        this.isTourClosing = false;
        this.connectivityManager = connectivityManager;
        this.entourageLocation = entourageLocation;
    }

    public static TourServiceManager newInstance(final TourService tourService,
                                                 final TourRequest tourRequest,
                                                 final AuthenticationController controller,
                                                 final EncounterRequest encounterRequest,
                                                 final NewsfeedRequest newsfeedRequest,
                                                 final EntourageRequest entourageRequest) {
        Log.i("TourServiceManager", "newInstance");
        ConnectivityManager connectivityManager = (ConnectivityManager) tourService.getSystemService(CONNECTIVITY_SERVICE);
        EntourageLocation entourageLocation = EntourageLocation.getInstance();
        User user = controller.getUser();
        UserType type = user != null && user.isPro() ? PRO : PUBLIC;
        FusedLocationProvider provider = new FusedLocationProvider(tourService, type);
        TourServiceManager tourServiceManager = new TourServiceManager(
            tourService,
            tourRequest,
            encounterRequest,
            newsfeedRequest,
            entourageRequest,
            connectivityManager,
            entourageLocation,
            provider);
        provider.setLocationListener(new FusedLocationListener(tourServiceManager));
        provider.setStatusListener(new FusedLocationStatusListener(tourService));
        provider.start();
        BusProvider.getInstance().register(tourServiceManager);
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
        provider.stop();
    }

    public void startTour(String type) {
        tour = new Tour(type);
        sendTour();
    }

    public void finishTour() {
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

    public void updateEncounter(Encounter encounter) {
        tour.updateEncounter(encounter);
    }

    public void unregisterFromBus() {
        BusProvider.getInstance().unregister(this);
    }

    void resetCurrentNewsfeedCall() {
        currentNewsFeedCall = null;
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
                if (response.isSuccessful()) {
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

    @SuppressWarnings("unused")
    @Subscribe
    public void onLocationPermissionGranted(OnLocationPermissionGranted event) {
        if (event.isPermissionGranted()) {
            provider.start();
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void encounterToSend(EncounterUploadTask task) {
        Encounter encounter = task.getEncounter();
        if (encounter == null) {
            return;
        }
        if (encounter.getId() > 0) {
            // edited encounter
            updateEncounterToServer(encounter);
        } else {
            // new encounter
            sendEncounter(encounter);
        }
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
                if (response.isSuccessful()) {
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

    protected void retrieveToursByUserId(int userId, int page, int per) {
        Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserId(userId, page, per);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(Call<Tour.ToursWrapper> call, Response<Tour.ToursWrapper> response) {
                if (response.isSuccessful()) {
                    tourService.notifyListenersUserToursFound(response.body().getTours());
                }
            }

            @Override
            public void onFailure(Call<Tour.ToursWrapper> call, Throwable t) {
                Log.e("Error", t.getLocalizedMessage());
            }
        });
    }

    protected void retrieveNewsFeed(Date beforeDate, int distance, int itemsPerPage, Context context) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            tourService.notifyListenersNetworkException();
            return;
        }

        CameraPosition currentPosition = entourageLocation.getCurrentCameraPosition();
        if (currentPosition != null) {
            LatLng location = currentPosition.target;
            MapFilter mapFilter = MapFilterFactory.getMapFilter(context);
            currentNewsFeedCall = createNewsfeedWrapperCall(beforeDate, location, distance, itemsPerPage, mapFilter);
            currentNewsFeedCall.enqueue(new NewsFeedCallback(this, tourService));
        } else {
            tourService.notifyListenersCurrentPositionNotRetrieved();
        }
    }

    protected void cancelNewsFeedRetrieval() {
        if (currentNewsFeedCall == null || currentNewsFeedCall.isCanceled()) {
            return;
        }
        currentNewsFeedCall.cancel();
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
                    if (response.isSuccessful()) {
                        Log.d("tape:", "success");
                        BusProvider.getInstance().post(new EncounterTaskResult(true, response.body().getEncounter(), EncounterTaskResult.OperationType.ENCOUNTER_ADD));
                    }
                }

                @Override
                public void onFailure(Call<EncounterResponse> call, Throwable t) {
                    Log.d("tape:", "failure");
                    BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_ADD));
                }
            });
        } else {
            Log.d("tape:", "no network");
            BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_ADD));
        }
    }

    protected void updateEncounterToServer(final Encounter encounter) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Encounter.EncounterWrapper encounterWrapper = new Encounter.EncounterWrapper();
            encounterWrapper.setEncounter(encounter);
            Call<EncounterResponse> call = encounterRequest.update(encounter.getId(), encounterWrapper);
            call.enqueue(new Callback<EncounterResponse>() {
                @Override
                public void onResponse(Call<EncounterResponse> call, Response<EncounterResponse> response) {
                    if (response.isSuccessful()) {
                        Log.d("tape:", "success");
                        BusProvider.getInstance().post(new EncounterTaskResult(true, encounter, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                    }
                    else {
                        Log.d("tape:", "not successful");
                        BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                    }
                }

                @Override
                public void onFailure(Call<EncounterResponse> call, Throwable t) {
                    Log.d("tape:", "failure");
                    BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                }
            });
        } else {
            Log.d("tape:", "no network");
            BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
        }
    }

    protected void requestToJoinTour(final Tour tour) {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            Call<TourUser.TourUserWrapper> call = tourRequest.requestToJoinTour(tour.getId());
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccessful()) {
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
                    if (response.isSuccessful()) {
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
                if (response.isSuccessful()) {
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
            Entourage.EntourageJoinInfo joinInfo = new Entourage.EntourageJoinInfo(entourage.distanceToCurrentLocation());
            Call<TourUser.TourUserWrapper> call = entourageRequest.requestToJoinEntourage(entourage.getId(), joinInfo);
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(final Call<TourUser.TourUserWrapper> call, final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccessful()) {
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
                    if (response.isSuccessful()) {
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

    private Call<Newsfeed.NewsfeedWrapper> createNewsfeedWrapperCall(Date beforeDate, LatLng location, int distance, int itemsPerPage, MapFilter mapFilter) {
        return newsfeedRequest.retrieveFeed(
                (beforeDate == null ? null : new EntourageDate(beforeDate)),
                location.longitude,
                location.latitude,
                distance,
                itemsPerPage,
                mapFilter.getTourTypes(),
                mapFilter.showTours,
                mapFilter.onlyMyEntourages,
                mapFilter.getEntourageTypes(),
                mapFilter.timeframe,
                mapFilter.onlyMyOrganisationEntourages
        );
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
                if (response.isSuccessful()) {
                    Location currentLocation = entourageLocation.getCurrentLocation();
                    if (currentLocation != null) {
                        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        BusProvider.getInstance().post(new OnBetterLocationEvent(latLng));
                    }
                    initializeTimerFinishTask();
                    tourId = response.body().getTour().getId();
                    tour = response.body().getTour();
                    tourService.notifyListenersTourCreated(true, tourId);

                    Location location = provider.getLastKnownLocation();
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
                if (response.isSuccessful()) {
                    Log.d("Success", response.body().getTour().toString());
                    tour = null;
                    pointsToSend.clear();
                    pointsToDraw.clear();
                    cancelFinishTimer();
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
                if (response.isSuccessful()) {
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
        private final TourService service;

        NewsFeedCallback(TourServiceManager manager, TourService service) {
            this.manager = manager;
            this.service = service;
        }

        @Override
        public void onResponse(Call<Newsfeed.NewsfeedWrapper> call, Response<Newsfeed.NewsfeedWrapper> response) {
            manager.resetCurrentNewsfeedCall();
            if (call.isCanceled()) {
                return;
            }
            if (response.isSuccessful()) {
                List<Newsfeed> newsFeedList = response.body().getNewsfeed();
                if (newsFeedList == null) {
                    service.notifyListenersTechnicalException(new Throwable("Null newsfeed list"));
                } else {
                    service.notifyListenersNewsFeedReceived(newsFeedList);
                }
            } else {
                service.notifyListenersServerException(new Throwable(getErrorMessage(response)));
            }
        }

        @Override
        public void onFailure(Call<Newsfeed.NewsfeedWrapper> call, Throwable t) {
            manager.resetCurrentNewsfeedCall();
            if (!call.isCanceled()) {
                service.notifyListenersTechnicalException(t);
            }
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
                if (response.errorBody() != null) {
                    return response.errorBody().string();
                } else {
                    return EMPTY_STRING;
                }
            } catch (IOException ignored) {
                return EMPTY_STRING;
            }
        }
    }

    private static class FusedLocationListener implements LocationListener {
        private final TourServiceManager manager;

        private FusedLocationListener(TourServiceManager manager) {
            this.manager = manager;
        }

        @Override
        public void onLocationChanged(Location location) {
            if (manager.entourageLocation.getCurrentLocation() == null) {
                manager.entourageLocation.setInitialLocation(location);
            }

            manager.updateLocation(location);
            manager.tourService.notifyListenersPosition(new LatLng(location.getLatitude(), location.getLongitude()));

            if (manager.tour != null && !manager.tourService.isPaused()) {
                TourPoint point = new TourPoint(location.getLatitude(), location.getLongitude());
                manager.onLocationChanged(location, point);
            }
        }
    }

    private static class FusedLocationStatusListener implements ProviderStatusListener {
        private final Context context;

        private FusedLocationStatusListener(Context context) {
            this.context = context;
        }

        @Override
        public void onProviderEnabled() {
            context.sendBroadcast(new Intent(TourService.KEY_LOCATION_PROVIDER_ENABLED));
        }

        @Override
        public void onProviderDisabled() {
            context.sendBroadcast(new Intent(TourService.KEY_LOCATION_PROVIDER_DISABLED));
        }
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
            if (shouldCenterMap) {
                BusProvider.getInstance().post(new OnBetterLocationEvent(entourageLocation.getLatLng()));
            }
        }
    }
}
