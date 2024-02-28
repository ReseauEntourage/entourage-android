package social.entourage.android.base.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.OnMapReadyCallback
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.databinding.LayoutFeedHeaderMapCardBinding

/**
 * Created by mihaiionescu on 27/06/2017.
 */
class MapViewHolder(val binding: LayoutFeedHeaderMapCardBinding) : BaseCardViewHolder(binding.root) {
    override fun bindFields() {
        binding.layoutFeedMapCardMapview.layoutParams?.height = binding.root.layoutParams.height
        //Inform the map that it needs to start
        binding.layoutFeedMapCardMapview.onCreate(null)
    }

    fun populate() {
        binding.layoutFeedMapCardMapview.onResume()
    }

    override fun populate(data: TimestampedObject) {
        binding.layoutFeedMapCardMapview.onResume()
    }

    fun setMapReadyCallback(callback: OnMapReadyCallback) {
        binding.layoutFeedMapCardMapview.getMapAsync(callback)
    }

    fun setFollowButtonOnClickListener(listener: View.OnClickListener?) {
        binding.layoutFeedMapCardRecenterButton.setOnClickListener(listener)
    }

    fun displayGeolocStatusIcon(visible: Boolean) {
        binding.layoutFeedMapCardRecenterButton.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    fun setGeolocStatusIcon(active: Boolean) {
        binding.layoutFeedMapCardRecenterButton.isSelected = active
    }

    fun setHeight(height: Int) {
        binding.root.layoutParams.height = height
        binding.layoutFeedMapCardMapview.layoutParams?.height = height
        binding.root.forceLayout()
    }
}
