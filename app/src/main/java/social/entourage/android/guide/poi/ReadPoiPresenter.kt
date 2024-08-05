package social.entourage.android.guide.poi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.request.PoiDetailResponse
import social.entourage.android.api.request.PoiRequest
import social.entourage.android.base.map.OnAddressClickListener
import timber.log.Timber

/**
 * Presenter controlling the ReadPoiFragment
 * @see ReadPoiFragment
 */
class ReadPoiPresenter(private val fragment: ReadPoiFragment) {
    private val poiRequest: PoiRequest
        get() = EntourageApplication.get().apiModule.poiRequest

    fun getPoiDetail(poiUuid: String) {
        val call = poiRequest.getPoiDetail(poiUuid)
        Log.wtf("wtf" , "getPoiDetail"  + poiUuid)
        call.enqueue(object : Callback<PoiDetailResponse> {
            override fun onResponse(call: Call<PoiDetailResponse>, response: Response<PoiDetailResponse>) {
                response.body()?.poi?.let { poi ->
                    if (response.isSuccessful) {
                        poi.isSoliguide = poi.source == "soliguide"
                        displayPoi(poi)
                        return
                    }
                }
                fragment.noData()
            }

            override fun onFailure(call: Call<PoiDetailResponse>, t: Throwable) {
                fragment.noData()
            }
        })
    }

    fun displayPoi(poi: Poi) {
        if(!fragment.isAdded) return
        fragment.activity?.let { activity->
            val listenerAddress = poi.address?.let { OnAddressClickListener(activity, it) }
            val listenerPhone = poi.phone?.let { OnPhoneClickListener(it) }
            fragment.onDisplayedPoi(poi, listenerAddress, listenerPhone)
        }
    }

    private fun dial(phone: Uri) {
        if (fragment.context != null) {
            val intent = Intent(Intent.ACTION_DIAL).apply { data = phone }
            try {
                fragment.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                Timber.e(e)
            }
        }
    }

    inner class OnPhoneClickListener(private val phone: String) : View.OnClickListener {
        override fun onClick(v: View) {
            val uri = Uri.parse("tel:$phone")
            dial(uri)
        }

    }

}