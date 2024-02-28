package social.entourage.android.base.map

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.OnMapReadyCallback
import social.entourage.android.R
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.base.BaseCardViewHolder
import social.entourage.android.databinding.LayoutFeedHeaderMapCardBinding

class MapViewHolder(private val binding: LayoutFeedHeaderMapCardBinding) : BaseCardViewHolder(binding.root) {
    init {
        // Informe la carte qu'elle doit démarrer ici si nécessaire
        // Cela pourrait dépendre de votre logique spécifique
    }

    override fun bindFields() {
        // Ici, vous pouvez initialiser des champs spécifiques si nécessaire
    }

    fun populate() {
        // Appelez cela pour peupler les vues avec des données dynamiques
        binding.layoutFeedMapCardMapview.onResume()
    }

    override fun populate(data: TimestampedObject) {
        // Utilisez cette méthode si vous avez besoin de peupler votre ViewHolder avec des données spécifiques
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
        val layoutParams = binding.root.layoutParams
        layoutParams.height = height
        binding.root.layoutParams = layoutParams

        val mapLayoutParams = binding.layoutFeedMapCardMapview.layoutParams
        mapLayoutParams.height = height
        binding.layoutFeedMapCardMapview.layoutParams = mapLayoutParams
    }

    companion object {
        val layoutResource: Int
            get() = R.layout.layout_feed_header_map_card

        fun create(parent: ViewGroup): MapViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = LayoutFeedHeaderMapCardBinding.inflate(inflater, parent, false)
            return MapViewHolder(binding)
        }
    }
}
