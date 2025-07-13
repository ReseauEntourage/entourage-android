package social.entourage.android.guide

import android.location.Location
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.guide.ClusterPoiResponse
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.request.PoiRequest
import social.entourage.android.api.request.PoiResponse
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.base.location.EntLocation
import social.entourage.android.guide.filter.GuideFilter
import timber.log.Timber
import java.net.UnknownHostException
import java.util.TreeMap

/**
 * Presenter controlling the GuideMapFragment
 * @see GuideMapFragment
 */
class GuideMapPresenter (private val fragment: GuideMapFragment) {
    private val authenticationController: AuthenticationController
        get() = EntourageApplication.get().authenticationController
    private val poiRequest: PoiRequest
        get() = EntourageApplication.get().apiModule.poiRequest

    private var previousEmptyListPopupLocation: Location? = null
    private var poisMap: MutableMap<String, Poi> = TreeMap()

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun start() {
        //updatePoisNearby();
    }

    fun updatePoisNearby(map: GoogleMap?) {
        val distance: Float
        if (map != null) {
            val region = map.projection.visibleRegion
            val result = floatArrayOf(0f)
            Location.distanceBetween(region.farLeft.latitude, region.farLeft.longitude, region.nearLeft.latitude, region.nearLeft.longitude, result)
            distance = result[0] / 1000.0f
            retrievePoisNearby(map.cameraPosition, distance)
        } else {
            Timber.w("no map available for updating Guide")
        }
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun retrievePoisNearby(currentPosition: CameraPosition, mapDistance: Float) {
        val location = currentPosition.target
        val distance: Double = mapDistance./*coerceAtMost(1f).*/toDouble()
        val call = poiRequest.retrievePoisNearby(location.latitude, location.longitude, distance, GuideFilter.instance.requestedCategories,GuideFilter.instance.requestedPartnerFilters,"2")
        call.enqueue(object : Callback<PoiResponse> {
            override fun onResponse(call: Call<PoiResponse>, response: Response<PoiResponse>) {
                response.body()?.let {
                    if (response.isSuccessful) {
                        fragment.putPoiOnMap(it.pois)

                    }
                }
            }

            override fun onFailure(call: Call<PoiResponse>, t: Throwable) {
                Timber.e(t, "Impossible to retrieve POIs")
                if (t is UnknownHostException) fragment.showErrorMessage()
            }
        })
    }

    fun updatePreviousEmptyListPopupLocation(cameraPosition: CameraPosition) {
        val currentLocation = EntLocation.cameraPositionToLocation(null, cameraPosition)
        previousEmptyListPopupLocation?.let {
            // Show the popup only we moved from the last position we show it
            if (it.distanceTo(currentLocation) < Constants.EMPTY_POPUP_DISPLAY_LIMIT) {
                return
            }
        }
        previousEmptyListPopupLocation = currentLocation
    }

    fun clear() {
        poisMap.clear()
    }

    fun removeRedundantPois(pois: List<Poi>): List<Poi> {
        val newPois: MutableList<Poi> = ArrayList()
        for(poi in pois) {
            if (!poisMap.containsKey(poi.uuid)) {
                poisMap[poi.uuid] = poi
                newPois.add(poi)
            }
        }
        return newPois
    }
    fun updatePoisAndClusters(map: GoogleMap?) {
        if (map != null) {
            val region = map.projection.visibleRegion
            val result = floatArrayOf(0f)
            Location.distanceBetween(
                region.farLeft.latitude, region.farLeft.longitude,
                region.nearLeft.latitude, region.nearLeft.longitude, result
            )
            val distance = result[0] / 1000.0f
            retrieveClustersAndPois(map.cameraPosition, distance)
        } else {
            Timber.w("no map available for updating Guide")
        }
    }

    private fun retrieveClustersAndPois(currentPosition: CameraPosition, mapDistance: Float) {
        val location = currentPosition.target
        val distance: Double = mapDistance.toDouble() / 2

        val call = poiRequest.retrieveClustersAndPois(location.latitude, location.longitude, distance, GuideFilter.instance.requestedCategories,GuideFilter.instance.requestedPartnerFilters)
        call.enqueue(object : Callback<ClusterPoiResponse> {
            override fun onResponse(call: Call<ClusterPoiResponse>, response: Response<ClusterPoiResponse>) {
                response.body()?.let {
                    if (response.isSuccessful) {
                        fragment.clearMap()
                        fragment.putClustersAndPoisOnMap(it.clusters)
                    }
                }
            }

            override fun onFailure(call: Call<ClusterPoiResponse>, t: Throwable) {
                Timber.e(t, "Impossible to retrieve clusters and POIs")
                fragment.showErrorMessage()
            }
        })
    }

    var isShowNoPOIsPopup: Boolean
        get() = authenticationController.isShowNoPOIsPopup
        set(shouldShowNoPOIsPopup) {authenticationController.isShowNoPOIsPopup = shouldShowNoPOIsPopup}

    var isShowInfoPOIsPopup: Boolean
        get() = authenticationController.isShowInfoPOIsPopup
        set(shouldShowInfoPOIsPopup) {authenticationController.isShowInfoPOIsPopup = shouldShowInfoPOIsPopup}
}