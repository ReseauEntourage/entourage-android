package social.entourage.android.guide.poi

import android.content.Intent
import android.net.Uri
import android.view.View
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.map.OnAddressClickListener

/**
 * Presenter controlling the ReadPoiFragment
 * @see ReadPoiFragment
 */
class ReadPoiPresenter(private val fragment: ReadPoiFragment) {
    fun displayPoi(poi: Poi) {
        var listenerAddress: OnAddressClickListener? = null
        var listenerPhone: OnPhoneClickListener? = null
        if (poi.address != null) {
            listenerAddress = OnAddressClickListener(fragment.requireActivity(), poi.address!!)
        }
        if (poi.phone != null) {
            listenerPhone = OnPhoneClickListener(poi.phone!!)
        }
        fragment.onDisplayedPoi(poi, listenerAddress, listenerPhone)
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