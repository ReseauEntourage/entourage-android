package social.entourage.android.guide.poi

import android.content.Intent
import android.net.Uri
import android.view.View
import kotlinx.android.synthetic.main.layout_poi_card.view.*
import social.entourage.android.Constants
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.model.LocationPoint
import social.entourage.android.api.tape.EntouragePoiRequest.OnPoiViewRequestedEvent
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.guide.poi.PoiRenderer.CategoryType
import social.entourage.android.tools.BusProvider

/**
 * Point of interest card view holder
 *
 * Created by mihaiionescu on 26/04/2017.
 */
class PoiViewHolder(itemView: View) : BaseCardViewHolder(itemView) {
    private var poi: Poi? = null

    override fun bindFields() {
        itemView.setOnClickListener(View.OnClickListener {
            if (poi == null) return@OnClickListener
            BusProvider.instance.post(OnPoiViewRequestedEvent(poi!!))
        })
        itemView.poi_card_call_button?.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:${poi!!.phone}")
            if (itemView.context != null) {
                if (intent.resolveActivity(itemView.context.packageManager) != null) {
                    itemView.context.startActivity(intent)
                }
            }
        }
    }

    override fun populate(data: TimestampedObject) {
        populatePoi(data as Poi)
    }

    private fun populatePoi(newPoi: Poi) {
        this.poi = newPoi
        itemView.poi_card_title?.text = poi!!.name ?: ""
        itemView.poi_card_type?.text = CategoryType.findCategoryTypeById(poi!!.categoryId).displayName
        itemView.poi_card_address?.text = if (poi!!.address != null) poi!!.address else ""
        itemView.poi_card_distance?.text = LocationPoint(poi!!.latitude, poi!!.longitude).distanceToCurrentLocation(Constants.DISTANCE_MAX_DISPLAY)
        itemView.poi_card_call_button?.visibility = if (poi!!.phone.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.layout_poi_card
    }
}