package social.entourage.android.service;

import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Vibrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import social.entourage.android.Constants;
import social.entourage.android.location.EntourageLocation;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.EntourageRequestDate;
import social.entourage.android.api.model.feed.NewsfeedItem;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.BaseEntourage;
import social.entourage.android.api.model.tour.Encounter;
import social.entourage.android.api.model.feed.FeedItem;
import social.entourage.android.api.model.tour.Tour;
import social.entourage.android.api.model.feed.FeedItemAuthor;
import social.entourage.android.api.model.LocationPoint;
import social.entourage.android.api.model.EntourageUser;
import social.entourage.android.api.tape.EncounterTaskResult;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.location.LocationListener;
import social.entourage.android.location.LocationProvider;
import social.entourage.android.location.LocationProvider.UserType;
import social.entourage.android.newsfeed.NewsfeedTabItem;
import social.entourage.android.tour.TourFilter;
import social.entourage.android.tour.encounter.CreateEncounterPresenter.EncounterUploadTask;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.map.filter.MapFilterFactory;
import social.entourage.android.newsfeed.NewsfeedPagination;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Manager is like a presenter but for a service
 * controlling the EntourageService
 *
 * @see EntourageService
 */
public class EntourageServiceManager {

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

    public final EntourageService entourageService;
    private final TourRequest tourRequest;
    private final AuthenticationController authenticationController;
    private final EncounterRequest encounterRequest;
    private final NewsfeedRequest newsfeedRequest;
    private final EntourageRequest entourageRequest;
    private final ConnectivityManager connectivityManager;
    private final LocationProvider locationProvider;
    public final EntourageLocation entourageLocation;

    private Tour currentTour;
    private Location previousLocation;
    private String tourUUID;
    private int pointsNeededForNextRequest;
    private final List<LocationPoint> pointsToSend;
    private final List<LocationPoint> pointsToDraw;
    private Timer timerFinish;
    private boolean isTourClosing;
    private Call<NewsfeedItem.NewsfeedItemWrapper> currentNewsFeedCall;

    private boolean isBetterLocationUpdated;

    private EntourageServiceManager(final EntourageService entourageService,
                                    final AuthenticationController authenticationController,
                                    final TourRequest tourRequest,
                                    final EncounterRequest encounterRequest,
                                    final NewsfeedRequest newsfeedRequest,
                                    final EntourageRequest entourageRequest,
                                    final ConnectivityManager connectivityManager,
                                    final EntourageLocation entourageLocation,
                                    final LocationProvider locationProvider) {
        this.entourageService = entourageService;
        this.authenticationController = authenticationController;
        this.tourRequest = tourRequest;
        this.encounterRequest = encounterRequest;
        this.newsfeedRequest = newsfeedRequest;
        this.entourageRequest = entourageRequest;
        this.locationProvider = locationProvider;
        pointsNeededForNextRequest = 1;
        pointsToSend = new ArrayList<>();
        pointsToDraw = new ArrayList<>();
        isTourClosing = false;
        this.connectivityManager = connectivityManager;
        this.entourageLocation = entourageLocation;
    }

    public static EntourageServiceManager newInstance(final EntourageService entourageService,
                                                      final TourRequest tourRequest,
                                                      final AuthenticationController controller,
                                                      final EncounterRequest encounterRequest,
                                                      final NewsfeedRequest newsfeedRequest,
                                                      final EntourageRequest entourageRequest) {
        Timber.d("newInstance");
        final ConnectivityManager connectivityManager = (ConnectivityManager) entourageService.getSystemService(CONNECTIVITY_SERVICE);
        final EntourageLocation entourageLocation = EntourageLocation.getInstance();
        final User user = controller.getUser();
        final UserType type = user != null && user.isPro() ? UserType.PRO : UserType.PUBLIC;
        final LocationProvider provider = new LocationProvider(entourageService, type);
        final EntourageServiceManager entourageServiceManager = new EntourageServiceManager(
                entourageService,
                controller,
                tourRequest,
                encounterRequest,
                newsfeedRequest,
                entourageRequest,
                connectivityManager,
                entourageLocation,
                provider);

        provider.setLocationListener(new LocationListener(entourageServiceManager, entourageService));
        provider.start();
        final Tour savedTour = controller.getSavedTour();
        if (savedTour != null && user != null) {
            final FeedItemAuthor author = savedTour.getAuthor();
            if (author == null || author.getUserID() != user.getId()) {
                // it's not the user's tour, so remove it from preferences
                controller.saveTour(null);
            } else {
                entourageServiceManager.currentTour = savedTour;
                entourageServiceManager.tourUUID = savedTour.getUuid();
                entourageService.notifyListenersTourCreated(true, savedTour.getUuid());
                provider.setUserType(UserType.PRO);
            }
        }
        BusProvider.INSTANCE.getInstance().register(entourageServiceManager);
        return entourageServiceManager;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getTour() {
        return currentTour;
    }

    String getTourUUID() {
        if (currentTour != null) {
            return currentTour.getUuid();
        }
        return tourUUID == null ? "" : tourUUID;
    }

    List<LocationPoint> getPointsToDraw() {
        return pointsToDraw;
    }

    void setTourDuration(final String duration) {
        currentTour.duration = duration;
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    void stopLocationService() {
        locationProvider.stop();
    }

    void startTour(final String type) {
        currentTour = new Tour(type);
        sendTour();
    }

    void finishTour() {
        isTourClosing = true;
        updateTourCoordinates();
    }

    void finishTour(final Tour tour) {
        closeTour(tour);
    }

    boolean isRunning() {
        return currentTour != null;
    }

    void addEncounter(final Encounter encounter) {
        if (currentTour != null) {
            currentTour.addEncounter(encounter);
        }
    }

    void updateEncounter(final Encounter encounter) {
        if (currentTour != null) {
            currentTour.updateEncounter(encounter);
        }
    }

    void unregisterFromBus() {
        try {
            BusProvider.INSTANCE.getInstance().unregister(this);
        } catch (final IllegalArgumentException e) {
            Timber.d("No need to unregister");
        }

    }

    private void resetCurrentNewsfeedCall() {
        currentNewsFeedCall = null;
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------

    void updateTourCoordinates() {
        if (pointsToSend.isEmpty()) {
            if (isTourClosing) {
                closeTour();
                isTourClosing = false;
            }
            return;
        }
        final LocationPoint.TourPointWrapper tourPointWrapper = new LocationPoint.TourPointWrapper();
        tourPointWrapper.setTourPoints(new ArrayList<>(pointsToSend));
        tourPointWrapper.setDistance(currentTour.distance);
        final Call<Tour.TourWrapper> call = tourRequest.tourPoints(tourUUID, tourPointWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    pointsToSend.removeAll(tourPointWrapper.getTourPoints());
                    if (isTourClosing) {
                        closeTour();
                    }
                } else {
                    if (isTourClosing) {
                        entourageService.notifyListenersFeedItemClosed(false, currentTour);
                    }
                }
                isTourClosing = false;
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                if (isTourClosing) {
                    entourageService.notifyListenersFeedItemClosed(false, currentTour);
                }
                isTourClosing = false;
                Timber.e(t);
            }
        });
    }

    private void cancelFinishTimer() {
        if (timerFinish != null) {
            timerFinish.cancel();
            timerFinish = null;
        }
    }

    boolean isLocationInTour(final LatLng latLng) {
        if (currentTour == null) {
            return false;
        }
        final List<LocationPoint> tourPoints = currentTour.getTourPoints();
        if (tourPoints.isEmpty()) {
            return false;
        }
        if (tourPoints.size() == 1) {
            final LocationPoint p = tourPoints.get(0);
            final float[] distance = {0};
            Location.distanceBetween(p.getLatitude(), p.getLongitude(), latLng.latitude, latLng.longitude, distance);
            return distance[0] <= MAX_DISTANCE_BETWEEN_TWO_POINTS;
        } else {
            final LocationPoint targetPoint = new LocationPoint(latLng.latitude, latLng.longitude);
            for (int i = 1; i < tourPoints.size(); i++) {
                final LocationPoint p1 = tourPoints.get(i - 1);
                final LocationPoint p2 = tourPoints.get(i);
                final double d = distanceToLine(p1, targetPoint, p2);
                if (d <= MAX_DISTANCE_TO_LINE) {
                    return true;
                }
            }
        }
        return false;
    }

    @Subscribe
    public void onLocationPermissionGranted(final OnLocationPermissionGranted event) {
        if (event.isPermissionGranted()) {
            locationProvider.start();
        }
    }

    @Subscribe
    public void encounterToSend(final EncounterUploadTask task) {
        final Encounter encounter = task.getEncounter();
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

    void freezeTour(final Tour tour) {
        if (tour == null) {
            return;
        }
        tour.status = FeedItem.STATUS_FREEZED;
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.tour = tour;
        final Call<Tour.TourWrapper> call = tourRequest.closeTour(tour.getUuid(), tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.body().tour.toString());
                    entourageService.notifyListenersFeedItemClosed(true, response.body().tour);
                } else {
                    entourageService.notifyListenersFeedItemClosed(false, tour);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                entourageService.notifyListenersFeedItemClosed(false, tour);
            }
        });
    }

    void retrieveToursByUserId(final int userId, final int page, final int per) {
        final Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserId(userId, page, per);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.ToursWrapper> call, @NonNull final Response<Tour.ToursWrapper> response) {
                if (response.isSuccessful()) {
                    entourageService.notifyListenersUserToursFound(response.body().tours);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.ToursWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
            }
        });
    }

    void retrieveNewsFeed(final NewsfeedPagination pagination, final NewsfeedTabItem selectedTab) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            entourageService.notifyListenersNetworkException();
            return;
        }

        final CameraPosition currentPosition = entourageLocation.getCurrentCameraPosition();
        if (currentPosition != null) {
            final LatLng location = currentPosition.target;
            currentNewsFeedCall = createNewsfeedWrapperCall(location, pagination, selectedTab);
            if (currentNewsFeedCall == null) {
                //fail graciously
                entourageService.notifyListenersNewsFeedReceived(null);
                return;
            }
            currentNewsFeedCall.enqueue(new NewsFeedCallback(this, entourageService));
        } else {
            entourageService.notifyListenersCurrentPositionNotRetrieved();
        }
    }

    void cancelNewsFeedRetrieval() {
        if (currentNewsFeedCall == null || currentNewsFeedCall.isCanceled()) {
            return;
        }
        currentNewsFeedCall.cancel();
    }

    private void sendEncounter(final Encounter encounter) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Encounter.EncounterWrapper encounterWrapper = new Encounter.EncounterWrapper();
            encounterWrapper.setEncounter(encounter);
            final Call<EncounterResponse> call = encounterRequest.create(encounter.getTourId(), encounterWrapper);
            call.enqueue(new Callback<EncounterResponse>() {
                @Override
                public void onResponse(@NonNull final Call<EncounterResponse> call, @NonNull final Response<EncounterResponse> response) {
                    if (response.isSuccessful() && response.body()!=null) {
                        Timber.tag("tape:").d("success");
                        BusProvider.INSTANCE.getInstance().post(new EncounterTaskResult(true, response.body().getEncounter(), EncounterTaskResult.OperationType.ENCOUNTER_ADD));
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<EncounterResponse> call, @NonNull final Throwable t) {
                    Timber.tag("tape:").e("failure");
                    BusProvider.INSTANCE.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_ADD));
                }
            });
        } else {
            Timber.tag("tape:").d("no network");
            BusProvider.INSTANCE.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_ADD));
        }
    }

    private void updateEncounterToServer(final Encounter encounter) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Encounter.EncounterWrapper encounterWrapper = new Encounter.EncounterWrapper();
            encounterWrapper.setEncounter(encounter);
            final Call<EncounterResponse> call = encounterRequest.update(encounter.getId(), encounterWrapper);
            call.enqueue(new Callback<EncounterResponse>() {
                @Override
                public void onResponse(@NonNull final Call<EncounterResponse> call, @NonNull final Response<EncounterResponse> response) {
                    if (response.isSuccessful()) {
                        Timber.tag("tape:").d("success");
                        BusProvider.INSTANCE.getInstance().post(new EncounterTaskResult(true, encounter, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                    } else {
                        Timber.tag("tape:").d("not successful");
                        BusProvider.INSTANCE.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<EncounterResponse> call, @NonNull final Throwable t) {
                    Timber.tag("tape:").d("failure");
                    BusProvider.INSTANCE.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                }
            });
        } else {
            Timber.tag("tape:").d("no network");
            BusProvider.INSTANCE.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
        }
    }

    void requestToJoinTour(final Tour tour) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Call<EntourageUser.EntourageUserWrapper> call = tourRequest.requestToJoinTour(tour.getUuid());
            call.enqueue(new Callback<EntourageUser.EntourageUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<EntourageUser.EntourageUserWrapper> call, @NonNull final Response<EntourageUser.EntourageUserWrapper> response) {
                    if (response.isSuccessful() && response.body()!=null) {
                        entourageService.notifyListenersUserStatusChanged(response.body().getUser(), tour);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<EntourageUser.EntourageUserWrapper> call, @NonNull final Throwable t) {
                    Timber.e(t);
                }
            });
        }
    }

    void removeUserFromTour(final Tour tour, final int userId) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Call<EntourageUser.EntourageUserWrapper> call = tourRequest.removeUserFromTour(tour.getUuid(), userId);
            call.enqueue(new Callback<EntourageUser.EntourageUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<EntourageUser.EntourageUserWrapper> call, @NonNull final Response<EntourageUser.EntourageUserWrapper> response) {
                    if (response.isSuccessful()) {
                        entourageService.notifyListenersUserStatusChanged(response.body().getUser(), tour);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull final Throwable t) {
                    Timber.e(t);
                }
            });
        }
    }

    void closeEntourage(final BaseEntourage entourage, final boolean success) {
        final String oldStatus = entourage.status;
        entourage.status = FeedItem.STATUS_CLOSED;
        entourage.setEndTime(new Date());
        entourage.outcome = new BaseEntourage.EntourageCloseOutcome(success);
        final BaseEntourage.EntourageWrapper entourageWrapper = new BaseEntourage.EntourageWrapper();
        entourageWrapper.entourage = entourage;
        final Call<BaseEntourage.EntourageWrapper> call = entourageRequest.closeEntourage(entourage.getUuid(), entourageWrapper);
        call.enqueue(new Callback<BaseEntourage.EntourageWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<BaseEntourage.EntourageWrapper> call, @NonNull final Response<BaseEntourage.EntourageWrapper> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.body().entourage.toString());
                    entourageService.notifyListenersFeedItemClosed(true, response.body().entourage);
                } else {
                    entourage.status = oldStatus;
                    entourageService.notifyListenersFeedItemClosed(false, entourage);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<BaseEntourage.EntourageWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                entourage.status = oldStatus;
                entourageService.notifyListenersFeedItemClosed(false, entourage);
            }
        });
    }

    void requestToJoinEntourage(final BaseEntourage entourage) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final BaseEntourage.EntourageJoinInfo joinInfo = new BaseEntourage.EntourageJoinInfo(entourage.distanceToCurrentLocation());
            final Call<EntourageUser.EntourageUserWrapper> call = entourageRequest.requestToJoinEntourage(entourage.getUuid(), joinInfo);
            call.enqueue(new Callback<EntourageUser.EntourageUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<EntourageUser.EntourageUserWrapper> call, @NonNull final Response<EntourageUser.EntourageUserWrapper> response) {
                    if (response.isSuccessful()) {
                        entourageService.notifyListenersUserStatusChanged(response.body().getUser(), entourage);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<EntourageUser.EntourageUserWrapper> call, @NonNull final Throwable t) {
                    Timber.e(t);
                }
            });
        }
    }

    void removeUserFromEntourage(final BaseEntourage entourage, final int userId) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Call<EntourageUser.EntourageUserWrapper> call = entourageRequest.removeUserFromEntourage(entourage.getUuid(), userId);
            call.enqueue(new Callback<EntourageUser.EntourageUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<EntourageUser.EntourageUserWrapper> call, @NonNull final Response<EntourageUser.EntourageUserWrapper> response) {
                    if (response.isSuccessful()) {
                        entourageService.notifyListenersUserStatusChanged(response.body().getUser(), entourage);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull final Throwable t) {
                    Timber.e(t);
                }
            });
        }
    }

    private @Nullable
    Call<NewsfeedItem.NewsfeedItemWrapper> createNewsfeedWrapperCall(final LatLng location, final NewsfeedPagination pagination, final NewsfeedTabItem selectedTab) {
        switch (selectedTab) {
            case ALL_TAB: {
                final MapFilter mapFilter = MapFilterFactory.getMapFilter();
                pagination.getBeforeDate();
                return newsfeedRequest.retrieveFeed(
                        new EntourageRequestDate(pagination.getBeforeDate()),
                        location.longitude,
                        location.latitude,
                        pagination.distance,
                        pagination.itemsPerPage,
                        mapFilter.getTypes(),
                        false,
                        mapFilter.getTimeFrame(),
                        false,
                        Constants.ANNOUNCEMENTS_VERSION,
                        mapFilter.showPastEvents()
                );
            }
            case TOUR_TAB:
                pagination.getBeforeDate();
                return newsfeedRequest.retrieveFeed(
                        new EntourageRequestDate(pagination.getBeforeDate()),
                        location.longitude,
                        location.latitude,
                        pagination.distance,
                        pagination.itemsPerPage,
                        TourFilter.getTypes(),
                        false,
                        TourFilter.getTimeFrame(),
                        false,
                        null,
                        true
                );
            case EVENTS_TAB:
                return newsfeedRequest.retrieveOutings(
                        location.longitude,
                        location.latitude,
                        pagination.getLastFeedItemUUID()
                );
        }
        return null;
    }

    private void initializeTimerFinishTask() {
        final long duration = 1000 * 60 * 60 * 5;
        timerFinish = new Timer();
        timerFinish.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeOut();
            }
        }, duration, duration);
    }

    private void timeOut() {
        final Vibrator vibrator = (Vibrator) entourageService.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATION_DURATION);
        entourageService.sendBroadcast(new Intent(EntourageService.KEY_NOTIFICATION_PAUSE_TOUR));
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    private void sendTour() {
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.tour = currentTour;
        final Call<Tour.TourWrapper> call = tourRequest.tour(tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    final Location currentLocation = entourageLocation.getCurrentLocation();
                    if (currentLocation != null) {
                        final LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        BusProvider.INSTANCE.getInstance().post(new OnBetterLocationEvent(latLng));
                    }
                    initializeTimerFinishTask();
                    tourUUID = response.body().tour.getUuid();
                    currentTour = response.body().tour;
                    entourageService.notifyListenersTourCreated(true, tourUUID);

                    locationProvider.requestLastKnownLocation();
                } else {
                    currentTour = null;
                    entourageService.notifyListenersTourCreated(false, "");
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                currentTour = null;
                entourageService.notifyListenersTourCreated(false, "");
            }
        });
    }

    private void closeTour() {
        currentTour.status = FeedItem.STATUS_CLOSED;
        currentTour.setEndTime(new Date());
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.tour = currentTour;
        final Call<Tour.TourWrapper> call = tourRequest.closeTour(tourUUID, tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.body().tour.toString());
                    currentTour = null;
                    pointsToSend.clear();
                    pointsToDraw.clear();
                    cancelFinishTimer();
                    entourageService.notifyListenersFeedItemClosed(true, response.body().tour);
                    locationProvider.setUserType(UserType.PUBLIC);
                    authenticationController.saveTour(currentTour);
                } else {
                    entourageService.notifyListenersFeedItemClosed(false, currentTour);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                entourageService.notifyListenersFeedItemClosed(false, currentTour);
            }
        });
    }

    private void closeTour(final Tour tour) {
        tour.status = FeedItem.STATUS_CLOSED;
        tour.setEndTime(new Date());
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.tour = tour;
        final Call<Tour.TourWrapper> call = tourRequest.closeTour(tour.getUuid(), tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    entourageService.notifyListenersFeedItemClosed(true, response.body().tour);
                    if (tour.getUuid().equalsIgnoreCase(tourUUID)) {
                        authenticationController.saveTour(null);
                    }
                } else {
                    entourageService.notifyListenersFeedItemClosed(false, tour);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                entourageService.notifyListenersFeedItemClosed(false, tour);
            }
        });
    }

    public void onLocationChanged(final Location location, final LocationPoint point) {
        pointsToDraw.add(point);
        pointsToSend.add(point);
        if (pointsToSend.size() >= 3) {
            final LocationPoint a = pointsToSend.get(pointsToSend.size() - 3);
            final LocationPoint b = pointsToSend.get(pointsToSend.size() - 2);
            final LocationPoint c = pointsToSend.get(pointsToSend.size() - 1);
            if (distanceToLine(a, b, c) < ALIGNMENT_PRECISION) {
                pointsToSend.remove(b);
            }
        }
        pointsNeededForNextRequest--;

        currentTour.addCoordinate(new LocationPoint(location.getLatitude(), location.getLongitude()));
        if (previousLocation != null) {
            currentTour.updateDistance(location.distanceTo(previousLocation));
        }
        previousLocation = location;

        if (isWebServiceUpdateNeeded()) {
            pointsNeededForNextRequest = POINT_PER_REQUEST;
            updateTourCoordinates();
        }

        entourageService.notifyListenersTourUpdated(new LatLng(location.getLatitude(), location.getLongitude()));
        authenticationController.saveTour(currentTour);
    }

    public void updateLocation(final Location location) {
        entourageLocation.saveCurrentLocation(location);
        final Location bestLocation = entourageLocation.getLocation();
        boolean shouldCenterMap = false;
        if (bestLocation == null || (location.getAccuracy() > 0.0 && bestLocation.getAccuracy() == 0.0)) {
            entourageLocation.saveLocation(location);
            isBetterLocationUpdated = true;
            shouldCenterMap = true;
        }

        if (isBetterLocationUpdated) {
            isBetterLocationUpdated = false;
            if (shouldCenterMap) {
                BusProvider.INSTANCE.getInstance().post(new OnBetterLocationEvent(entourageLocation.getLatLng()));
            }
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    private boolean isWebServiceUpdateNeeded() {
        return pointsNeededForNextRequest <= 0;
    }

    private double distanceToLine(final LocationPoint startPoint, final LocationPoint middlePoint, final LocationPoint endPoint) {
        final double scalarProduct = (middlePoint.getLatitude() - startPoint.getLatitude()) * (endPoint.getLatitude() - startPoint.getLatitude()) + (middlePoint.getLongitude() - startPoint.getLongitude()) * (endPoint.getLongitude() - startPoint.getLongitude());
        final double distanceProjection = scalarProduct / Math.sqrt(Math.pow(endPoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(endPoint.getLongitude() - startPoint.getLongitude(), 2));
        final double distanceToMiddle = Math.sqrt(Math.pow(middlePoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(middlePoint.getLongitude() - startPoint.getLongitude(), 2));
        return Math.sqrt(Math.pow(distanceToMiddle, 2) - Math.pow(distanceProjection, 2));
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    static class NewsFeedCallback implements Callback<NewsfeedItem.NewsfeedItemWrapper> {
        private static final String EMPTY_STRING = "";
        private final EntourageServiceManager manager;
        private final EntourageService service;

        NewsFeedCallback(final EntourageServiceManager manager, final EntourageService service) {
            this.manager = manager;
            this.service = service;
        }

        @Override
        public void onResponse(@NonNull final Call<NewsfeedItem.NewsfeedItemWrapper> call, @NonNull final Response<NewsfeedItem.NewsfeedItemWrapper> response) {
            manager.resetCurrentNewsfeedCall();
            if (call.isCanceled()) {
                return;
            }
            if (response.isSuccessful()) {
                final List<NewsfeedItem> newsFeedList = response.body().newsfeedItems;
                if (newsFeedList == null) {
                    service.notifyListenersTechnicalException(new Throwable("Null newsfeed list"));
                } else {
                    service.notifyListenersNewsFeedReceived(newsFeedList);
                }
            } else {
                service.notifyListenersServerException(new Throwable(getErrorMessage(call, response)));
            }
        }

        @Override
        public void onFailure(@NonNull final Call<NewsfeedItem.NewsfeedItemWrapper> call, @NonNull final Throwable t) {
            manager.resetCurrentNewsfeedCall();
            if (!call.isCanceled()) {
                service.notifyListenersTechnicalException(t);
            }
        }

        @NonNull
        private String getErrorMessage(@NonNull final Call<NewsfeedItem.NewsfeedItemWrapper> call, @NonNull final Response<NewsfeedItem.NewsfeedItemWrapper> response) {
            final String errorBody = getErrorBody(response);
            String errorMessage = "Response code = " + response.code();
            call.request();
            errorMessage += " ( " + call.request().toString() + ")";
            if (!errorBody.isEmpty()) {
                errorMessage += " : " + errorBody;
            }
            return errorMessage;
        }

        @NonNull
        private String getErrorBody(final Response<NewsfeedItem.NewsfeedItemWrapper> response) {
            try {
                if (response.errorBody() != null) {
                    return response.errorBody().string();
                } else {
                    return EMPTY_STRING;
                }
            } catch (final IOException ignored) {
                return EMPTY_STRING;
            }
        }
    }
}
