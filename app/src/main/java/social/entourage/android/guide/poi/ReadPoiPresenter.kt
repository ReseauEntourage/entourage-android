package social.entourage.android.guide.poi

import android.content.Intent
import android.net.Uri
import android.view.View
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.request.PoiDetailResponse
import social.entourage.android.api.request.PoiRequest
import social.entourage.android.map.OnAddressClickListener
import javax.inject.Inject

/**
 * Presenter controlling the ReadPoiFragment
 * @see ReadPoiFragment
 */
class ReadPoiPresenter @Inject constructor(private val fragment: ReadPoiFragment, private val poiRequest: PoiRequest) {

    fun getPoiDetail(poiUuid: String) {
        val call = poiRequest.getPoiDetail(poiUuid)
        call.enqueue(object : Callback<PoiDetailResponse> {
            override fun onResponse(call: Call<PoiDetailResponse>, response: Response<PoiDetailResponse>) {
                response.body()?.let {
                    if (response.isSuccessful) {
                        it.poi.isSoliguide = it.poi.source.equals("soliguide")
                        displayPoi(it.poi)
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
        fragment.activity?.let { activity->
            val listenerAddress = poi.address?.let { OnAddressClickListener(activity, it) }
            val listenerPhone = poi.phone?.let { OnPhoneClickListener(it) }
            fragment.onDisplayedPoi(poi, listenerAddress, listenerPhone)
        }
    }

    private fun dial(phone: Uri) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = phone
        if (fragment.context != null) {
            if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
                fragment.startActivity(intent)
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