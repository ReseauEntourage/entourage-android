package social.entourage.android.service

import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Vibrator
import com.google.android.gms.maps.model.LatLng
import com.squareup.otto.Subscribe
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.model.EntourageRequestDate
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Encounter
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.request.*
import social.entourage.android.api.tape.EncounterTaskResult
import social.entourage.android.api.tape.Events.OnBetterLocationEvent
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.location.EntLocation.currentLocation
import social.entourage.android.base.location.LocationProvider
import social.entourage.android.base.location.LocationProvider.UserType
import social.entourage.android.base.newsfeed.NewsfeedPagination
import social.entourage.android.base.newsfeed.NewsfeedTabItem
import social.entourage.android.tools.EntBus
import social.entourage.android.tour.TourFilter
import social.entourage.android.tour.encounter.CreateEncounterPresenter.EncounterUploadTask
import timber.log.Timber
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Manager is like a presenter but for a service
 * controlling the EntourageService
 *
 * @see EntService
 */
class TourServiceManager(
        entService: EntService,
        authenticationController: AuthenticationController,
        private val tourRequest: TourRequest,
        private val encounterRequest: EncounterRequest,
        newsfeedRequest: NewsfeedRequest,
        entourageRequest: EntourageRequest,
        locationProvider: LocationProvider)
    : EntServiceManager(
        entService,
        authenticationController,
        newsfeedRequest,
        entourageRequest,
        locationProvider) {
    var tour: Tour? = null
        private set
    private var tourUUID: String? = null
    private var isTourClosing: Boolean
    private var pointsNeededForNextRequest = 1
    private val pointsToSend: MutableList<LocationPoint>
    val pointsToDraw: MutableList<LocationPoint>
    private var previousLocation: Location? = null
    private var timerFinish: Timer? = null

    fun getTourUUID(): String {
        return tour?.uuid ?:  tourUUID ?: ""
    }

    fun setTourDuration(duration: String?) {
        tour?.duration = duration
    }

    fun startTour(type: String) {
        tour = Tour(type)
        sendTour()
    }

    fun finishTour() {
        isTourClosing = true
        updateTourCoordinates()
    }

    fun finishTour(tour: Tour) {
        closeTour(tour)
    }

    val isRunning: Boolean
        get() = tour != null

    fun addEncounter(encounter: Encounter) {
        tour?.addEncounter(encounter)
    }

    fun updateEncounter(encounter: Encounter) {
        tour?.updateEncounter(encounter)
    }

    fun updateTourCoordinates() {
        if (pointsToSend.isEmpty()) {
            if (isTourClosing) {
                closeTour()
                isTourClosing = false
            }
            return
        }
        tour?.let {
            val tourPointWrapper = TourPointWrapper(ArrayList(pointsToSend), it.distance)
            tourRequest.addTourPoints(getTourUUID(), tourPointWrapper)
                    .enqueue(object : Callback<ResponseBody?> {
                        override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                            if (response.isSuccessful) {
                                pointsToSend.removeAll(tourPointWrapper.tourPoints)
                                if (isTourClosing) {
                                    closeTour()
                                }
                            } else {
                                if (isTourClosing) {
                                    entService.notifyListenersFeedItemClosed(false, it)
                                }
                            }
                            isTourClosing = false
                        }

                        override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                            if (isTourClosing) {
                                entService.notifyListenersFeedItemClosed(false, it)
                                isTourClosing = false
                            }
                            Timber.e(t)
                        }
                    })
        }
    }

    @Subscribe
    override fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        super.onLocationPermissionGranted(event)
    }

    @Subscribe
    fun encounterToSend(task: EncounterUploadTask) {
        val encounter = task.encounter
        if (encounter.id > 0) {
            // edited encounter
            updateEncounterToServer(encounter)
        } else {
            // new encounter
            sendEncounter(encounter)
        }
    }

    fun freezeTour(tour: Tour) {
        tour.uuid?.let { uuid ->
            tour.status = Tour.STATUS_FREEZED
            tourRequest.closeTour(uuid, TourWrapper(tour)).enqueue(object : Callback<TourResponse> {
                override fun onResponse(call: Call<TourResponse>, response: Response<TourResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.tour?.let { entService.notifyListenersFeedItemClosed(true, it) }
                    } else {
                        entService.notifyListenersFeedItemClosed(false, tour)
                    }
                }

                override fun onFailure(call: Call<TourResponse>, t: Throwable) {
                    Timber.e(t)
                    entService.notifyListenersFeedItemClosed(false, tour)
                }
            })
        }
    }

    fun retrieveToursByUserId(userId: Int, page: Int, per: Int) {
        tourRequest.retrieveToursByUserId(userId, page, per).enqueue(object : Callback<TourListResponse> {
            override fun onResponse(call: Call<TourListResponse>, response: Response<TourListResponse>) {
                if (response.isSuccessful) {
                    response.body()?.tours?.let { tours -> entService.notifyListenersUserToursFound(tours) }
                }
            }

            override fun onFailure(call: Call<TourListResponse>, t: Throwable) {
                Timber.e(t)
            }
        })
    }

    private fun sendEncounter(encounter: Encounter) {
        if (isNetworkConnected) {
            encounterRequest.create(encounter.tourId, EncounterWrapper(encounter)).enqueue(object : Callback<EncounterResponse?> {
                override fun onResponse(call: Call<EncounterResponse?>, response: Response<EncounterResponse?>) {
                    if (response.isSuccessful) {
                        response.body()?.encounter?.let { EntBus.post(EncounterTaskResult(true, it, EncounterTaskResult.OperationType.ENCOUNTER_ADD)) }
                    }
                }

                override fun onFailure(call: Call<EncounterResponse?>, t: Throwable) {
                    EntBus.post(EncounterTaskResult(false, encounter, EncounterTaskResult.OperationType.ENCOUNTER_ADD))
                }
            })
        } else {
            EntBus.post(EncounterTaskResult(false, encounter, EncounterTaskResult.OperationType.ENCOUNTER_ADD))
        }
    }

    private fun updateEncounterToServer(encounter: Encounter) {
        if (isNetworkConnected) {
            encounterRequest.update(encounter.id, EncounterWrapper(encounter))
                    .enqueue(object : Callback<EncounterResponse?> {
                        override fun onResponse(call: Call<EncounterResponse?>, response: Response<EncounterResponse?>) {
                            EntBus.post(EncounterTaskResult(response.isSuccessful, encounter, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE))
                        }

                        override fun onFailure(call: Call<EncounterResponse?>, t: Throwable) {
                            EntBus.post(EncounterTaskResult(false, encounter, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE))
                        }
                    })
        } else {
            Timber.tag("tape:").d("no network")
            EntBus.post(EncounterTaskResult(false, encounter, EncounterTaskResult.OperationType.ENCOUNTER_UPDATE))
        }
    }

    fun requestToJoinTour(tour: Tour) {
        if (isNetworkConnected) {
            tour.uuid?.let {uuid ->
                tourRequest.requestToJoinTour(uuid).enqueue(object : Callback<EntourageUserResponse?> {
                    override fun onResponse(call: Call<EntourageUserResponse?>, response: Response<EntourageUserResponse?>) {
                        if (response.isSuccessful) {
                            response.body()?.user?.let { user -> entService.notifyListenersUserStatusChanged(user, tour) }
                        }
                    }

                    override fun onFailure(call: Call<EntourageUserResponse?>, t: Throwable) {
                        Timber.e(t)
                    }
                })
            }
        }
    }

    fun removeUserFromTour(tour: Tour, userId: Int) {
        if (isNetworkConnected) {
            tour.uuid?.let { uuid ->
                tourRequest.removeUserFromTour(uuid, userId).enqueue(object : Callback<EntourageUserResponse> {
                    override fun onResponse(call: Call<EntourageUserResponse>, response: Response<EntourageUserResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.user?.let { entService.notifyListenersUserStatusChanged(it, tour) }
                        }
                    }

                    override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                        Timber.e(t)
                    }
                })
            }
        }
    }

    private fun sendTour() {
        tour?.let {
            tourRequest.createTour(TourWrapper(it)).enqueue(object : Callback<TourResponse> {
                override fun onResponse(call: Call<TourResponse>, response: Response<TourResponse>) {
                    if (response.isSuccessful) {
                        val currentLocation = currentLocation
                        if (currentLocation != null) {
                            val latLng = LatLng(currentLocation.latitude, currentLocation.longitude)
                            EntBus.post(OnBetterLocationEvent(latLng))
                        }
                        initializeTimerFinishTask()
                        response.body()?.tour?.let { createdTour ->
                            tourUUID = createdTour.uuid
                            tour = createdTour
                            entService.notifyListenersTourCreated(true, createdTour.uuid ?: "")
                        }
                        locationProvider.requestLastKnownLocation()
                    } else {
                        tour = null
                        entService.notifyListenersTourCreated(false, "")
                    }
                }

                override fun onFailure(call: Call<TourResponse>, t: Throwable) {
                    Timber.e(t)
                    tour = null
                    entService.notifyListenersTourCreated(false, "")
                }
            })
        }
    }

    private fun closeTour() {
        tour?.let {
            it.status = FeedItem.STATUS_CLOSED
            it.setEndTime(Date())
            tourRequest.closeTour(getTourUUID(), TourWrapper(it))
                    .enqueue(object : Callback<TourResponse> {
                        override fun onResponse(call: Call<TourResponse>, response: Response<TourResponse>) {
                            if (response.isSuccessful) {
                                tour = null
                                pointsToSend.clear()
                                pointsToDraw.clear()
                                cancelFinishTimer()
                                response.body()?.let { body ->
                                    entService.notifyListenersFeedItemClosed(true, body.tour)
                                }
                                locationProvider.setUserType(UserType.PUBLIC)
                                authenticationController.saveTour(null)
                            } else {
                                entService.notifyListenersFeedItemClosed(false, it)
                            }
                        }

                        override fun onFailure(call: Call<TourResponse>, t: Throwable) {
                            Timber.e(t)
                            entService.notifyListenersFeedItemClosed(false, it)
                        }
                    })
        }
    }

    private fun closeTour(tour: Tour) {
        tour.status = FeedItem.STATUS_CLOSED
        tour.setEndTime(Date())
        val closingTourUUID = tour.uuid ?: return
        tourRequest.closeTour(closingTourUUID, TourWrapper(tour)).enqueue(object : Callback<TourResponse?> {
            override fun onResponse(call: Call<TourResponse?>, response: Response<TourResponse?>) {
                if (response.isSuccessful) {
                    response.body()?.tour?.let {
                        entService.notifyListenersFeedItemClosed(true, it)
                        if (tour.uuid.equals(tourUUID, ignoreCase = true)) {
                            authenticationController.saveTour(null)
                        }
                        return
                    }
                }
                entService.notifyListenersFeedItemClosed(false, tour)
            }

            override fun onFailure(call: Call<TourResponse?>, t: Throwable) {
                Timber.e(t)
                entService.notifyListenersFeedItemClosed(false, tour)
            }
        })
    }

    private fun onLocationChanged(location: Location, point: LocationPoint) {
        pointsToDraw.add(point)
        pointsToSend.add(point)
        if (pointsToSend.size >= 3) {
            val a = pointsToSend[pointsToSend.size - 3]
            val b = pointsToSend[pointsToSend.size - 2]
            val c = pointsToSend[pointsToSend.size - 1]
            if (distanceToLine(a, b, c) < ALIGNMENT_PRECISION) {
                pointsToSend.remove(b)
            }
        }
        pointsNeededForNextRequest--
        tour?.addCoordinate(LocationPoint(location.latitude, location.longitude))
        if (previousLocation != null) {
            tour?.updateDistance(location.distanceTo(previousLocation))
        }
        previousLocation = location
        if (isWebServiceUpdateNeeded) {
            pointsNeededForNextRequest = POINT_PER_REQUEST
            updateTourCoordinates()
        }
        entService.notifyListenersTourUpdated(LatLng(location.latitude, location.longitude))
        authenticationController.saveTour(tour)
    }

    override fun createNewsfeedWrapperCall(location: LatLng, pagination: NewsfeedPagination, selectedTab: NewsfeedTabItem): Call<NewsfeedItemResponse>? {
        return when (selectedTab) {
            NewsfeedTabItem.TOUR_TAB -> {
                newsfeedRequest.retrieveFeed(
                        EntourageRequestDate(pagination.beforeDate),
                        location.longitude,
                        location.latitude,
                        pagination.distance,
                        pagination.itemsPerPage,
                        TourFilter.getTypes(),
                        false,
                        TourFilter.getTimeFrame(),
                        false,
                        null,
                        showPastEvents = false,
                        isPartnersOnly = false
                )
            }
            else -> super.createNewsfeedWrapperCall(location, pagination, selectedTab)
        }
    }

    override fun updateLocation(location: Location) {
        super.updateLocation(location)

        if (tour != null && !entService.isPaused) {
            val point = LocationPoint(location.latitude, location.longitude, location.accuracy)
            onLocationChanged(location, point)
        }
    }

    private val isWebServiceUpdateNeeded: Boolean
        get() = pointsNeededForNextRequest <= 0

    private fun distanceToLine(startPoint: LocationPoint, middlePoint: LocationPoint, endPoint: LocationPoint): Double {
        val scalarProduct = (middlePoint.latitude - startPoint.latitude) * (endPoint.latitude - startPoint.latitude) + (middlePoint.longitude - startPoint.longitude) * (endPoint.longitude - startPoint.longitude)
        val distanceProjection = scalarProduct / sqrt((endPoint.latitude - startPoint.latitude).pow(2.0) + (endPoint.longitude - startPoint.longitude).pow(2.0))
        val distanceToMiddle = sqrt((middlePoint.latitude - startPoint.latitude).pow(2.0) + (middlePoint.longitude - startPoint.longitude).pow(2.0))
        return sqrt(distanceToMiddle.pow(2.0) - distanceProjection.pow(2.0))
    }

    private fun initializeTimerFinishTask() {
        val duration = 1000 * 60 * 60 * 5.toLong()
        timerFinish = Timer().apply {
            this.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    timeOut()
                }
            }, duration, duration)
        }
    }

    private fun timeOut() {
        val vibrator = entService.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibrator.vibrate(VIBRATION_DURATION)
        entService.sendBroadcast(Intent(EntService.KEY_NOTIFICATION_PAUSE_TOUR))
    }

    private fun cancelFinishTimer() {
        timerFinish?.cancel()
        timerFinish = null
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        private const val POINT_PER_REQUEST = 10
        private const val ALIGNMENT_PRECISION = .000001
        private const val VIBRATION_DURATION: Long = 1000
        private const val MAX_DISTANCE_BETWEEN_TWO_POINTS = 200.0 //meters
        private const val MAX_DISTANCE_TO_LINE = .0020
    }

    init {
        pointsToSend = ArrayList()
        pointsToDraw = ArrayList()
        isTourClosing = false
        authenticationController.savedTour?.let { savedTour ->
            authenticationController.me?.let {me ->
                if (savedTour.author?.userID == me.id) {
                    tour = savedTour
                    tourUUID = savedTour.uuid
                    entService.notifyListenersTourCreated(true, getTourUUID())
                    locationProvider.setUserType(UserType.PRO)
                } else {
                    // it's not the user's tour, so remove it from preferences
                    authenticationController.saveTour(null)
                }
            }
        }

    }
}