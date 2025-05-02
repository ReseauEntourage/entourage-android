package social.entourage.android.guide.poi

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.OnMapReadyCallback
import social.entourage.android.databinding.LayoutFeedFullMapCardBinding

/**
 * Created by mihaiionescu on 27/06/2017.
 */
class MapViewHolder(val binding: LayoutFeedFullMapCardBinding) : RecyclerView.ViewHolder(binding.root) {
    private fun bindFields() {
        binding.layoutFeedMapCardMapview.layoutParams?.height = binding.root.layoutParams.height
        //Inform the map that it needs to start
        binding.layoutFeedMapCardMapview.onCreate(null)
    }

    fun populate() {
        binding.layoutFeedMapCardMapview.onResume()
    }

    fun setMapReadyCallback(callback: OnMapReadyCallback) {
        binding.layoutFeedMapCardMapview.getMapAsync(callback)
    }

    fun setFollowButtonOnClickListener(listener: View.OnClickListener?) {
        binding.layoutFeedMapCardRecenterButton.setOnClickListener(listener)
    }

    fun setGeolocStatusIcon(active: Boolean) {
        binding.layoutFeedMapCardRecenterButton.isSelected = active
    }

    fun setHeight(height: Int) {
        binding.root.layoutParams.height = height
        binding.layoutFeedMapCardMapview.layoutParams?.height = height
        binding.root.forceLayout()
    }

    init {
        bindFields()
    }
}
