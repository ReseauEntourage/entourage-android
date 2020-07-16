package social.entourage.android.guide

import android.location.Location
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.PoiRequest
import social.entourage.android.api.PoiResponse
import social.entourage.android.guide.filter.GuideFilter
import timber.log.Timber
import javax.inject.Inject

/**
 * Presenter controlling the GuideMapFragment
 * @see GuideMapFragment
 */
class GuideMapPresenter @Inject constructor(
        private val fragment: GuideMapFragment,
        private val poiRequest: PoiRequest) {

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
        val distance: Double = mapDistance.coerceAtMost(1f).toDouble()
        val call = poiRequest.retrievePoisNearby(location.latitude, location.longitude, distance, GuideFilter.instance.requestedCategories)
        call.enqueue(object : Callback<PoiResponse> {
            override fun onResponse(call: Call<PoiResponse>, response: Response<PoiResponse>) {
                response.body()?.let {
                    if (response.isSuccessful) {
                        fragment.putPoiOnMap(it.categories, it.pois)
                    }
                }
            }

            override fun onFailure(call: Call<PoiResponse>, t: Throwable) {
                Timber.e(t, "Impossible to retrieve POIs")
            }
        })
    }
}