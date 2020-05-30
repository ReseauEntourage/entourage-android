package social.entourage.android.map

import android.os.Build
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.layout_feed_map_card.view.*
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder

/**
 * Created by mihaiionescu on 27/06/2017.
 */
class MapViewHolder(view: View) : BaseCardViewHolder(view) {
    override fun bindFields() {
        itemView.layout_feed_map_card_mapview?.layoutParams?.height = itemView.layoutParams.height
        //Inform the map that it needs to start
        itemView.layout_feed_map_card_mapview?.onCreate(null)
    }

    fun populate() {
        itemView.layout_feed_map_card_mapview?.onResume()
    }

    override fun populate(data: TimestampedObject) {
        itemView.layout_feed_map_card_mapview?.onResume()
    }

    fun setMapReadyCallback(callback: OnMapReadyCallback?) {
        itemView.layout_feed_map_card_mapview?.getMapAsync(callback)
    }

    fun setFollowButtonOnClickListener(listener: View.OnClickListener?) {
        itemView.layout_feed_map_card_recenter_button?.setOnClickListener(listener)
    }

    fun displayGeolocStatusIcon(visible: Boolean) {
        itemView.layout_feed_map_card_recenter_button?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    fun setGeolocStatusIcon(active: Boolean) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            itemView.layout_feed_map_card_recenter_button?.setImageDrawable(AppCompatResources.getDrawable(itemView.context, if (active) R.drawable.ic_my_location else R.drawable.ic_my_location_off))
        } else {
            itemView.layout_feed_map_card_recenter_button?.isSelected = active
        }
    }

    fun setHeight(height: Int) {
        itemView.layoutParams.height = height
        itemView.layout_feed_map_card_mapview?.layoutParams?.height = height
        itemView.forceLayout()
    }

    companion object {
        @JvmStatic
        val layoutResource: Int
            get() = R.layout.layout_feed_map_card
    }
}