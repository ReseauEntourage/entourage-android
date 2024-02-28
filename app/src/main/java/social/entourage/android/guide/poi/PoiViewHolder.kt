package social.entourage.android.guide.poi

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.fragment.app.findFragment
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.databinding.LayoutPoiCardBinding // Assurez-vous que ceci correspond au nom généré.
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class PoiViewHolder(private val binding: LayoutPoiCardBinding) : BaseCardViewHolder(binding.root) {
    private var poi: Poi? = null
    var showCallButton: Boolean = true

    init {
        bindFields()
    }

    override fun bindFields() {
        binding.root.setOnClickListener { view ->
            poi?.let { poi -> (view.findFragment() as? PoiListFragment)?.showPoiDetails(poi, true) }
        }
        binding.poiCardCallButton.setOnClickListener {
            poi?.phone?.let {phone ->
                val context = binding.root.context
                AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GUIDE_CALLPOI)
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Timber.e(e)
                }
            }
        }
    }

    override fun populate(data: TimestampedObject) {
        if (data is Poi) {
            populatePoi(data)
        }
    }

    private fun populatePoi(newPoi: Poi) {
        this.poi = newPoi
        binding.poiCardTitle.text = newPoi.name ?: ""
        binding.poiCardAddress.text = newPoi.address ?: ""
        binding.poiCardDistance.text = LocationPoint(newPoi.latitude, newPoi.longitude).distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
        binding.poiCardCallButton.visibility = if (!showCallButton || newPoi.phone.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_poi_card
    }
}
