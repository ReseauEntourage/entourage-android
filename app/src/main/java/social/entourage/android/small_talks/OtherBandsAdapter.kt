package social.entourage.android.small_talks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import social.entourage.android.R
import social.entourage.android.databinding.ItemOtherBandBinding

class OtherBandsAdapter(
    private val items: List<OtherBand>,
    private val onJoinClicked: (OtherBand) -> Unit
) : RecyclerView.Adapter<OtherBandsAdapter.OtherBandViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OtherBandViewHolder {
        val binding = ItemOtherBandBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OtherBandViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OtherBandViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class OtherBandViewHolder(private val binding: ItemOtherBandBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: OtherBand) {
            val context = binding.root.context

            // 📝 Description selon le type
            binding.textDescription.text = when (item.type) {
                OtherBandType.DIFFERENT_LOCATION -> context.getString(R.string.small_talk_other_band_different_location)
                OtherBandType.DIFFERENT_INTERESTS -> context.getString(R.string.small_talk_other_band_different_interests)
                OtherBandType.DUO -> context.getString(R.string.small_talk_other_band_duo)
                OtherBandType.GROUP_OF_THREE_PLUS -> context.getString(R.string.small_talk_other_band_group)
            }

            // 👥 Membres
            binding.textMembers.text = context.getString(
                R.string.small_talk_other_band_with,
                item.members.joinToString(" et ")
            )

            // 📸 Avatars
            renderAvatars(item.memberAvatarUrls)

            // 🔘 Bouton
            binding.buttonJoin.setOnClickListener {
                onJoinClicked(item)
            }
        }

        private fun renderAvatars(urls: List<String>) {
            val imageViews = listOf(
                binding.avatar1,
                binding.avatar2,
                binding.avatar3,
                binding.avatar4,
                binding.avatar5
            )

            // Masquer tous les avatars au départ
            imageViews.forEach { it.visibility = View.GONE }

            // Afficher et remplir ceux nécessaires
            urls.take(5).forEachIndexed { index, url ->
                val imageView = imageViews[index]
                imageView.visibility = View.VISIBLE

                Glide.with(imageView)
                    .load(url.takeIf { it.isNotBlank() })
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .transform(CircleCrop())
                    .into(imageView)
            }
        }
    }
}
