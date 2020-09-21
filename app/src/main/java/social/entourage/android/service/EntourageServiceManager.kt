package social.entourage.android.service

import android.content.Context
import android.location.Location
import android.net.ConnectivityManager
import com.google.android.gms.maps.model.LatLng
import com.squareup.otto.Subscribe
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.Constants
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.BaseEntourage.EntourageCloseOutcome
import social.entourage.android.api.model.BaseEntourage.EntourageJoinInfo
import social.entourage.android.api.model.EntourageRequestDate
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.request.*
import social.entourage.android.api.tape.Events
import social.entourage.android.api.tape.Events.OnBetterLocationEvent
import social.entourage.android.api.tape.Events.OnLocationPermissionGranted
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.location.EntourageLocation
import social.entourage.android.location.EntourageLocation.currentCameraPosition
import social.entourage.android.location.EntourageLocation.currentLocation
import social.entourage.android.location.EntourageLocation.latLng
import social.entourage.android.location.LocationListener
import social.entourage.android.location.LocationProvider
import social.entourage.android.location.LocationProvider.UserType
import social.entourage.android.map.filter.MapFilterFactory.mapFilter
import social.entourage.android.newsfeed.NewsfeedPagination
import social.entourage.android.newsfeed.NewsfeedTabItem
import social.entourage.android.tools.BusProvider.instance
import timber.log.Timber
import java.util.*

/**
 * Manager is like a presenter but for a service
 * controlling the EntourageService
 *
 * @see EntourageService
 */
open class EntourageServiceManager(
        val entourageService: EntourageService,
        val authenticationController: AuthenticationController,
        val newsfeedRequest: NewsfeedRequest,
        private val entourageRequest: EntourageRequest,
        val locationProvider: LocationProvider) {


    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var currentNewsFeedCall: Call<NewsfeedItemResponse>? = null
    private var isBetterLocationUpdated = false
    private val connectivityManager: ConnectivityManager = entourageService.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // ----------------------------------
    // GETTERS AND SETTERS
    // ----------------------------------
    val isNetworkConnected : Boolean
        get() = connectivityManager.activeNetworkInfo?.isConnected == true

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun stopLocationService() {
        locationProvider.stop()
    }

    fun unregisterFromBus() {
        try {
            instance.unregister(this)
        } catch (e: IllegalArgumentException) {
            Timber.d("No need to unregister")
        }
    }

    private fun resetCurrentNewsfeedCall() {
        currentNewsFeedCall = null
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    @Subscribe
    open fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        if (event.isPermissionGranted) {
            locationProvider.start()
        }
    }

    fun retrieveNewsFeed(pagination: NewsfeedPagination, selectedTab: NewsfeedTabItem) {
        if (!isNetworkConnected) {
            entourageService.notifyListenersNetworkException()
            return
        }
        currentNewsFeedCall = createNewsfeedWrapperCall(currentCameraPosition.target, pagination, selectedTab)
        currentNewsFeedCall?.enqueue(NewsFeedCallback(this, entourageService))
    }

    fun cancelNewsFeedRetrieval() {
        currentNewsFeedCall?.let { call -> if (!call.isCanceled) call.cancel() }
    }

    fun closeEntourage(entourage: BaseEntourage, success: Boolean) {
        val oldStatus = entourage.status
        entourage.status = FeedItem.STATUS_CLOSED
        entourage.setEndTime(Date())
        entourage.outcome = EntourageCloseOutcome(success)
        entourageRequest.closeEntourage(entourage.uuid!!, EntourageWrapper(entourage)).enqueue(object : Callback<EntourageResponse> {
            override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                if (response.isSuccessful) {
                    response.body()?.entourage?.let {
                        entourageService.notifyListenersFeedItemClosed(true, it)
                        return
                    }
                }
                entourage.status = oldStatus
                entourageService.notifyListenersFeedItemClosed(false, entourage)
            }

            override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                Timber.e(t)
                entourage.status = oldStatus
                entourageService.notifyListenersFeedItemClosed(false, entourage)
            }
        })
    }

    fun reopenEntourage(entourage: BaseEntourage) {
        val oldStatus = entourage.status
        entourage.status = FeedItem.STATUS_OPEN
        entourageRequest.editEntourage(entourage.uuid!!, EntourageWrapper(entourage)).enqueue(object : Callback<EntourageResponse> {
            override fun onResponse(call: Call<EntourageResponse>, response: Response<EntourageResponse>) {
                if (response.isSuccessful) {
                    response.body()?.entourage?.let {
                       entourageService.notifyListenersFeedItemClosed(true, it)
                        return
                    }
                }
                entourage.status = oldStatus
                entourageService.notifyListenersFeedItemClosed(false, entourage)
            }

            override fun onFailure(call: Call<EntourageResponse>, t: Throwable) {
                Timber.e(t)
                entourage.status = oldStatus
                entourageService.notifyListenersFeedItemClosed(false, entourage)
            }
        })
    }

    fun requestToJoinEntourage(entourage: BaseEntourage) {
        if (isNetworkConnected) {
            entourageRequest.requestToJoinEntourage(entourage.uuid!!, EntourageJoinInfo(entourage.distanceToCurrentLocation()))
                    .enqueue(object : Callback<EntourageUserResponse> {
                override fun onResponse(call: Call<EntourageUserResponse>, response: Response<EntourageUserResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.user?.let { user -> entourageService.notifyListenersUserStatusChanged(user, entourage) }
                    }
                }

                override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                    Timber.e(t)
                }
            })
        }
    }

    fun removeUserFromEntourage(entourage: BaseEntourage, userId: Int) {
        if (isNetworkConnected) {
            entourage.uuid?.let {
                entourageRequest.removeUserFromEntourage(it, userId).enqueue(object : Callback<EntourageUserResponse> {
                    override fun onResponse(call: Call<EntourageUserResponse>, response: Response<EntourageUserResponse>) {
                        if (response.isSuccessful) {
                            response.body()?.user?.let { user-> entourageService.notifyListenersUserStatusChanged(user, entourage)}
                        }
                    }

                    override fun onFailure(call: Call<EntourageUserResponse>, t: Throwable) {
                        Timber.e(t)
                    }
                })
            }
        }
    }

    open fun createNewsfeedWrapperCall(location: LatLng, pagination: NewsfeedPagination, selectedTab: NewsfeedTabItem): Call<NewsfeedItemResponse>? {
        return when (selectedTab) {
            NewsfeedTabItem.ALL_TAB -> {
                pagination.beforeDate
                newsfeedRequest.retrieveFeed(
                        EntourageRequestDate(pagination.beforeDate),
                        location.longitude,
                        location.latitude,
                        pagination.distance,
                        pagination.itemsPerPage,
                        mapFilter.getTypes(),
                        false,
                        mapFilter.getTimeFrame(),
                        false,
                        Constants.ANNOUNCEMENTS_VERSION,
                        mapFilter.showPastEvents(),
                        mapFilter.isShowPartnersOnly
                )
            }
            NewsfeedTabItem.EVENTS_TAB -> newsfeedRequest.retrieveOutings(
                    location.longitude,
                    location.latitude,
                    pagination.lastFeedItemUUID
            )
            else -> null
        }
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    open fun updateLocation(location: Location) {
        currentLocation = location
        val bestLocation = EntourageLocation.location
        var shouldCenterMap = false
        if (bestLocation == null || (location.accuracy > 0.0 && bestLocation.accuracy.toDouble() == 0.0)) {
            EntourageLocation.location = location
            isBetterLocationUpdated = true
            shouldCenterMap = true
        }
        if (isBetterLocationUpdated) {
            isBetterLocationUpdated = false
            if (shouldCenterMap) {
                latLng?.let {instance.post(OnBetterLocationEvent(it))                 }
            }
        }

        entourageService.notifyListenersPosition(LatLng(location.latitude, location.longitude))
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    internal class NewsFeedCallback(private val manager: EntourageServiceManager, private val service: EntourageService) : Callback<NewsfeedItemResponse?> {
        override fun onResponse(call: Call<NewsfeedItemResponse?>, response: Response<NewsfeedItemResponse?>) {
            manager.resetCurrentNewsfeedCall()
            if (call.isCanceled) {
                return
            }
            if (response.isSuccessful) {
                response.body()?.unreadCount?.let {
                    instance.post(Events.OnUnreadCountUpdate(it))
                }
                response.body()?.newsfeedItems?.let {
                    service.notifyListenersNewsFeedReceived(it)
                    return
                }
            }
            service.notifyListenersServerException(Throwable(getErrorMessage(call, response)))
        }

        override fun onFailure(call: Call<NewsfeedItemResponse?>, t: Throwable) {
            manager.resetCurrentNewsfeedCall()
            if (!call.isCanceled) {
                service.notifyListenersTechnicalException(t)
            }
        }

        private fun getErrorMessage(call: Call<NewsfeedItemResponse?>, response: Response<NewsfeedItemResponse?>): String {
            val errorBody = getErrorBody(response)
            var errorMessage = "Response code = " + response.code()
            errorMessage += " ( " + call.request().toString() + ")"
            if (errorBody.isNotEmpty()) {
                errorMessage += " : $errorBody"
            }
            return errorMessage
        }

        private fun getErrorBody(response: Response<NewsfeedItemResponse?>): String {
            return response.errorBody()?.string() ?: ""
        }
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        fun newInstance(entourageService: EntourageService,
                        tourRequest: TourRequest,
                        authenticationController: AuthenticationController,
                        encounterRequest: EncounterRequest,
                        newsfeedRequest: NewsfeedRequest,
                        entourageRequest: EntourageRequest): EntourageServiceManager {
            val provider = LocationProvider(entourageService, if (authenticationController.me?.isPro == true) UserType.PRO else UserType.PUBLIC)
            val mgr = if (authenticationController.me?.isPro == true) TourServiceManager(
                        entourageService,
                        authenticationController,
                        tourRequest,
                        encounterRequest,
                        newsfeedRequest,
                        entourageRequest,
                        provider)
                else EntourageServiceManager(
                    entourageService,
                    authenticationController,
                    newsfeedRequest,
                    entourageRequest,
                    provider)
            provider.setLocationListener(LocationListener(mgr, entourageService))
            provider.start()
            instance.register(mgr)
            return mgr
        }
    }
}