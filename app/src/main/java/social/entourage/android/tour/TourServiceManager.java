package social.entourage.android.tour;

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
import social.entourage.android.EntourageLocation;
import social.entourage.android.api.EncounterRequest;
import social.entourage.android.api.EncounterResponse;
import social.entourage.android.api.EntourageRequest;
import social.entourage.android.api.NewsfeedRequest;
import social.entourage.android.api.TourRequest;
import social.entourage.android.api.model.EntourageDate;
import social.entourage.android.api.model.Newsfeed;
import social.entourage.android.api.model.User;
import social.entourage.android.api.model.map.BaseEntourage;
import social.entourage.android.api.model.map.Encounter;
import social.entourage.android.api.model.map.Entourage;
import social.entourage.android.api.model.map.FeedItem;
import social.entourage.android.api.model.map.Tour;
import social.entourage.android.api.model.map.TourAuthor;
import social.entourage.android.api.model.map.TourPoint;
import social.entourage.android.api.model.map.TourUser;
import social.entourage.android.api.tape.EncounterTaskResult;
import social.entourage.android.api.tape.Events.OnBetterLocationEvent;
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted;
import social.entourage.android.authentication.AuthenticationController;
import social.entourage.android.location.LocationListener;
import social.entourage.android.location.LocationProvider;
import social.entourage.android.location.LocationProvider.UserType;
import social.entourage.android.map.MapTabItem;
import social.entourage.android.tour.encounter.CreateEncounterPresenter.EncounterUploadTask;
import social.entourage.android.map.filter.MapFilter;
import social.entourage.android.map.filter.MapFilterFactory;
import social.entourage.android.newsfeed.NewsfeedPagination;
import social.entourage.android.tools.BusProvider;
import timber.log.Timber;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;
import static social.entourage.android.location.LocationProvider.UserType.PRO;
import static social.entourage.android.location.LocationProvider.UserType.PUBLIC;

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

    public final TourService tourService;
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
    private final List<TourPoint> pointsToSend;
    private final List<TourPoint> pointsToDraw;
    private Timer timerFinish;
    private boolean isTourClosing;
    private Call<Newsfeed.NewsfeedWrapper> currentNewsFeedCall;

    private boolean isBetterLocationUpdated;

    private TourServiceManager(final TourService tourService,
                               final AuthenticationController authenticationController,
                               final TourRequest tourRequest,
                               final EncounterRequest encounterRequest,
                               final NewsfeedRequest newsfeedRequest,
                               final EntourageRequest entourageRequest,
                               final ConnectivityManager connectivityManager,
                               final EntourageLocation entourageLocation,
                               final LocationProvider locationProvider) {
        this.tourService = tourService;
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

    public static TourServiceManager newInstance(final TourService tourService,
                                                 final TourRequest tourRequest,
                                                 final AuthenticationController controller,
                                                 final EncounterRequest encounterRequest,
                                                 final NewsfeedRequest newsfeedRequest,
                                                 final EntourageRequest entourageRequest) {
        Timber.d("newInstance");
        final ConnectivityManager connectivityManager = (ConnectivityManager) tourService.getSystemService(CONNECTIVITY_SERVICE);
        final EntourageLocation entourageLocation = EntourageLocation.getInstance();
        final User user = controller.getUser();
        final UserType type = user != null && user.isPro() ? PRO : PUBLIC;
        final LocationProvider provider = new LocationProvider(tourService, type);
        final TourServiceManager tourServiceManager = new TourServiceManager(
                tourService,
                controller,
                tourRequest,
                encounterRequest,
                newsfeedRequest,
                entourageRequest,
                connectivityManager,
                entourageLocation,
                provider);

        provider.setLocationListener(new LocationListener(tourServiceManager, tourService));
        provider.start();
        final Tour savedTour = controller.getSavedTour();
        if (savedTour != null && user != null) {
            final TourAuthor author = savedTour.getAuthor();
            if (author == null || author.getUserID() != user.getId()) {
                // it's not the user's tour, so remove it from preferences
                controller.saveTour(null);
            } else {
                tourServiceManager.currentTour = savedTour;
                tourServiceManager.tourUUID = savedTour.getUUID();
                tourService.notifyListenersTourCreated(true, savedTour.getUUID());
                provider.setUserType(UserType.PRO);
            }
        }
        BusProvider.getInstance().register(tourServiceManager);
        return tourServiceManager;
    }

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------

    public Tour getTour() {
        return currentTour;
    }

    String getTourUUID() {
        if (currentTour != null) {
            return currentTour.getUUID();
        }
        return tourUUID == null ? "" : tourUUID;
    }

    List<TourPoint> getPointsToDraw() {
        return pointsToDraw;
    }

    void setTourDuration(final String duration) {
        currentTour.setDuration(duration);
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
            BusProvider.getInstance().unregister(this);
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
        final TourPoint.TourPointWrapper tourPointWrapper = new TourPoint.TourPointWrapper();
        tourPointWrapper.setTourPoints(new ArrayList<>(pointsToSend));
        tourPointWrapper.setDistance(currentTour.getDistance());
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
                        tourService.notifyListenersFeedItemClosed(false, currentTour);
                    }
                }
                isTourClosing = false;
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                if (isTourClosing) {
                    tourService.notifyListenersFeedItemClosed(false, currentTour);
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
        final List<TourPoint> tourPoints = currentTour.getTourPoints();
        if (tourPoints.isEmpty()) {
            return false;
        }
        if (tourPoints.size() == 1) {
            final TourPoint p = tourPoints.get(0);
            final float[] distance = {0};
            Location.distanceBetween(p.getLatitude(), p.getLongitude(), latLng.latitude, latLng.longitude, distance);
            return distance[0] <= MAX_DISTANCE_BETWEEN_TWO_POINTS;
        } else {
            final TourPoint targetPoint = new TourPoint(latLng.latitude, latLng.longitude);
            for (int i = 1; i < tourPoints.size(); i++) {
                final TourPoint p1 = tourPoints.get(i - 1);
                final TourPoint p2 = tourPoints.get(i);
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
        tour.setTourStatus(FeedItem.STATUS_FREEZED);
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        final Call<Tour.TourWrapper> call = tourRequest.closeTour(tour.getUUID(), tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.body().getTour().toString());
                    tourService.notifyListenersFeedItemClosed(true, response.body().getTour());
                } else {
                    tourService.notifyListenersFeedItemClosed(false, tour);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                tourService.notifyListenersFeedItemClosed(false, tour);
            }
        });
    }

    void retrieveToursByUserId(final int userId, final int page, final int per) {
        final Call<Tour.ToursWrapper> call = tourRequest.retrieveToursByUserId(userId, page, per);
        call.enqueue(new Callback<Tour.ToursWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.ToursWrapper> call, @NonNull final Response<Tour.ToursWrapper> response) {
                if (response.isSuccessful()) {
                    tourService.notifyListenersUserToursFound(response.body().getTours());
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.ToursWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
            }
        });
    }

    void retrieveNewsFeed(final NewsfeedPagination pagination, final MapTabItem selectedTab) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo == null || !netInfo.isConnected()) {
            tourService.notifyListenersNetworkException();
            return;
        }

        final CameraPosition currentPosition = entourageLocation.getCurrentCameraPosition();
        if (currentPosition != null) {
            final LatLng location = currentPosition.target;
            final MapFilter mapFilter = MapFilterFactory.getMapFilter();
            if (mapFilter == null) {
                return;
            }
            currentNewsFeedCall = createNewsfeedWrapperCall(location, pagination, mapFilter, selectedTab);
            if (currentNewsFeedCall == null) {
                //fail graciously
                tourService.notifyListenersNewsFeedReceived(null);
                return;
            }
            currentNewsFeedCall.enqueue(new NewsFeedCallback(this, tourService));
        } else {
            tourService.notifyListenersCurrentPositionNotRetrieved();
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
                    if (response.isSuccessful()) {
                        Timber.tag("tape:").d("success");
                        BusProvider.getInstance().post(new EncounterTaskResult(true, response.body().getEncounter(), EncounterTaskResult.OperationType.ENCOUNTER_ADD));
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<EncounterResponse> call, @NonNull final Throwable t) {
                    Timber.tag("tape:").e("failure");
                    BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_ADD));
                }
            });
        } else {
            Timber.tag("tape:").d("no network");
            BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_ADD));
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
                        BusProvider.getInstance().post(new EncounterTaskResult(true, encounter, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                    } else {
                        Timber.tag("tape:").d("not successful");
                        BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<EncounterResponse> call, @NonNull final Throwable t) {
                    Timber.tag("tape:").d("failure");
                    BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
                }
            });
        } else {
            Timber.tag("tape:").d("no network");
            BusProvider.getInstance().post(new EncounterTaskResult(false, null, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE));
        }
    }

    void requestToJoinTour(final Tour tour) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Call<TourUser.TourUserWrapper> call = tourRequest.requestToJoinTour(tour.getUUID());
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccessful()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), tour);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Throwable t) {
                    Timber.e(t);
                }
            });
        }
    }

    void removeUserFromTour(final Tour tour, final int userId) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Call<TourUser.TourUserWrapper> call = tourRequest.removeUserFromTour(tour.getUUID(), userId);
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccessful()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), tour);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call call, @NonNull final Throwable t) {
                    Timber.e(t);
                }
            });
        }
    }

    void closeEntourage(final Entourage entourage, final boolean success) {
        final String oldStatus = entourage.getStatus();
        entourage.setStatus(FeedItem.STATUS_CLOSED);
        entourage.setEndTime(new Date());
        entourage.setOutcome(new BaseEntourage.EntourageCloseOutcome(success));
        final Entourage.EntourageWrapper entourageWrapper = new Entourage.EntourageWrapper();
        entourageWrapper.setEntourage(entourage);
        final Call<Entourage.EntourageWrapper> call = entourageRequest.closeEntourage(entourage.getUUID(), entourageWrapper);
        call.enqueue(new Callback<Entourage.EntourageWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Entourage.EntourageWrapper> call, @NonNull final Response<Entourage.EntourageWrapper> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.body().getEntourage().toString());
                    tourService.notifyListenersFeedItemClosed(true, response.body().getEntourage());
                } else {
                    entourage.setStatus(oldStatus);
                    tourService.notifyListenersFeedItemClosed(false, entourage);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Entourage.EntourageWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                entourage.setStatus(oldStatus);
                tourService.notifyListenersFeedItemClosed(false, entourage);
            }
        });
    }

    void requestToJoinEntourage(final Entourage entourage) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Entourage.EntourageJoinInfo joinInfo = new Entourage.EntourageJoinInfo(entourage.distanceToCurrentLocation());
            final Call<TourUser.TourUserWrapper> call = entourageRequest.requestToJoinEntourage(entourage.getUUID(), joinInfo);
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccessful()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), entourage);
                    }
                }

                @Override
                public void onFailure(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Throwable t) {
                    Timber.e(t);
                }
            });
        }
    }

    void removeUserFromEntourage(final Entourage entourage, final int userId) {
        final NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnected()) {
            final Call<TourUser.TourUserWrapper> call = entourageRequest.removeUserFromEntourage(entourage.getUUID(), userId);
            call.enqueue(new Callback<TourUser.TourUserWrapper>() {
                @Override
                public void onResponse(@NonNull final Call<TourUser.TourUserWrapper> call, @NonNull final Response<TourUser.TourUserWrapper> response) {
                    if (response.isSuccessful()) {
                        tourService.notifyListenersUserStatusChanged(response.body().getUser(), entourage);
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
    Call<Newsfeed.NewsfeedWrapper> createNewsfeedWrapperCall(final LatLng location, final NewsfeedPagination pagination, final MapFilter mapFilter, final MapTabItem selectedTab) {
        switch (selectedTab) {
            case ALL_TAB:
                return newsfeedRequest.retrieveFeed(
                        (pagination.getBeforeDate() == null ? null : new EntourageDate(pagination.getBeforeDate())),
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
        final Vibrator vibrator = (Vibrator) tourService.getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(VIBRATION_DURATION);
        tourService.sendBroadcast(new Intent(TourService.KEY_NOTIFICATION_PAUSE_TOUR));
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------

    private void sendTour() {
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(currentTour);
        final Call<Tour.TourWrapper> call = tourRequest.tour(tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    final Location currentLocation = entourageLocation.getCurrentLocation();
                    if (currentLocation != null) {
                        final LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        BusProvider.getInstance().post(new OnBetterLocationEvent(latLng));
                    }
                    initializeTimerFinishTask();
                    tourUUID = response.body().getTour().getUUID();
                    currentTour = response.body().getTour();
                    tourService.notifyListenersTourCreated(true, tourUUID);

                    locationProvider.requestLastKnownLocation();
                } else {
                    currentTour = null;
                    tourService.notifyListenersTourCreated(false, "");
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                currentTour = null;
                tourService.notifyListenersTourCreated(false, "");
            }
        });
    }

    private void closeTour() {
        currentTour.setTourStatus(FeedItem.STATUS_CLOSED);
        currentTour.setEndTime(new Date());
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(currentTour);
        final Call<Tour.TourWrapper> call = tourRequest.closeTour(tourUUID, tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.body().getTour().toString());
                    currentTour = null;
                    pointsToSend.clear();
                    pointsToDraw.clear();
                    cancelFinishTimer();
                    tourService.notifyListenersFeedItemClosed(true, response.body().getTour());
                    locationProvider.setUserType(UserType.PUBLIC);
                    authenticationController.saveTour(currentTour);
                } else {
                    tourService.notifyListenersFeedItemClosed(false, currentTour);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                tourService.notifyListenersFeedItemClosed(false, currentTour);
            }
        });
    }

    private void closeTour(final Tour tour) {
        tour.setTourStatus(FeedItem.STATUS_CLOSED);
        tour.setEndTime(new Date());
        final Tour.TourWrapper tourWrapper = new Tour.TourWrapper();
        tourWrapper.setTour(tour);
        final Call<Tour.TourWrapper> call = tourRequest.closeTour(tour.getUUID(), tourWrapper);
        call.enqueue(new Callback<Tour.TourWrapper>() {
            @Override
            public void onResponse(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Response<Tour.TourWrapper> response) {
                if (response.isSuccessful()) {
                    Timber.d(response.body().getTour().toString());
                    tourService.notifyListenersFeedItemClosed(true, response.body().getTour());
                    if (tour.getUUID().equalsIgnoreCase(tourUUID)) {
                        authenticationController.saveTour(null);
                    }
                } else {
                    tourService.notifyListenersFeedItemClosed(false, tour);
                }
            }

            @Override
            public void onFailure(@NonNull final Call<Tour.TourWrapper> call, @NonNull final Throwable t) {
                Timber.e(t);
                tourService.notifyListenersFeedItemClosed(false, tour);
            }
        });
    }

    public void onLocationChanged(final Location location, final TourPoint point) {
        pointsToDraw.add(point);
        pointsToSend.add(point);
        if (pointsToSend.size() >= 3) {
            final TourPoint a = pointsToSend.get(pointsToSend.size() - 3);
            final TourPoint b = pointsToSend.get(pointsToSend.size() - 2);
            final TourPoint c = pointsToSend.get(pointsToSend.size() - 1);
            if (distanceToLine(a, b, c) < ALIGNMENT_PRECISION) {
                pointsToSend.remove(b);
            }
        }
        pointsNeededForNextRequest--;

        currentTour.addCoordinate(new TourPoint(location.getLatitude(), location.getLongitude()));
        if (previousLocation != null) {
            currentTour.updateDistance(location.distanceTo(previousLocation));
        }
        previousLocation = location;

        if (isWebServiceUpdateNeeded()) {
            pointsNeededForNextRequest = POINT_PER_REQUEST;
            updateTourCoordinates();
        }

        tourService.notifyListenersTourUpdated(new LatLng(location.getLatitude(), location.getLongitude()));
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
                BusProvider.getInstance().post(new OnBetterLocationEvent(entourageLocation.getLatLng()));
            }
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------

    private boolean isWebServiceUpdateNeeded() {
        return pointsNeededForNextRequest <= 0;
    }

    private double distanceToLine(final TourPoint startPoint, final TourPoint middlePoint, final TourPoint endPoint) {
        final double scalarProduct = (middlePoint.getLatitude() - startPoint.getLatitude()) * (endPoint.getLatitude() - startPoint.getLatitude()) + (middlePoint.getLongitude() - startPoint.getLongitude()) * (endPoint.getLongitude() - startPoint.getLongitude());
        final double distanceProjection = scalarProduct / Math.sqrt(Math.pow(endPoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(endPoint.getLongitude() - startPoint.getLongitude(), 2));
        final double distanceToMiddle = Math.sqrt(Math.pow(middlePoint.getLatitude() - startPoint.getLatitude(), 2) + Math.pow(middlePoint.getLongitude() - startPoint.getLongitude(), 2));
        return Math.sqrt(Math.pow(distanceToMiddle, 2) - Math.pow(distanceProjection, 2));
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------

    static class NewsFeedCallback implements Callback<Newsfeed.NewsfeedWrapper> {
        private static final String EMPTY_STRING = "";
        private final TourServiceManager manager;
        private final TourService service;

        NewsFeedCallback(final TourServiceManager manager, final TourService service) {
            this.manager = manager;
            this.service = service;
        }

        @Override
        public void onResponse(@NonNull final Call<Newsfeed.NewsfeedWrapper> call, @NonNull final Response<Newsfeed.NewsfeedWrapper> response) {
            manager.resetCurrentNewsfeedCall();
            if (call.isCanceled()) {
                return;
            }
            if (response.isSuccessful()) {
                final List<Newsfeed> newsFeedList = response.body().getNewsfeed();
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
        public void onFailure(@NonNull final Call<Newsfeed.NewsfeedWrapper> call, @NonNull final Throwable t) {
            manager.resetCurrentNewsfeedCall();
            if (!call.isCanceled()) {
                service.notifyListenersTechnicalException(t);
            }
        }

        @NonNull
        private String getErrorMessage(@NonNull final Call<Newsfeed.NewsfeedWrapper> call, @NonNull final Response<Newsfeed.NewsfeedWrapper> response) {
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
        private String getErrorBody(final Response<Newsfeed.NewsfeedWrapper> response) {
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
