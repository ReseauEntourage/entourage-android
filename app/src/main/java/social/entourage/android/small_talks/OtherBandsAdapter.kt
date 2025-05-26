package social.entourage.android.small_talks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import social.entourage.android.R
import social.entourage.android.api.model.User
import social.entourage.android.api.model.UserSmallTalkRequestWithMatchData
import social.entourage.android.databinding.ItemOtherBandBinding



class OtherBandsAdapter(
    private val items: List<UserSmallTalkRequestWithMatchData>,
    private val onJoinClicked: (UserSmallTalkRequestWithMatchData) -> Unit
) : RecyclerView.Adapter<OtherBandsAdapter.BandViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BandViewHolder {
        val binding = ItemOtherBandBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BandViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BandViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class BandViewHolder(private val binding: ItemOtherBandBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: UserSmallTalkRequestWithMatchData) {
            val context = binding.root.context

            // 1. Afficher les avatars (max 5)
            val urls = request.users.mapNotNull { it.avatarURL }.distinct().take(5)
            renderAvatars(urls)

            // 2. Afficher les noms
            val names = request.users.mapNotNull { it.displayName }.distinct()
            binding.textMembers.text = context.getString(
                R.string.small_talk_other_band_with,
                names.joinToString(" et ")
            )

            // 3. Texte explicatif basé sur le critère manquant (priorité définie)
            binding.textDescription.text = when {
                !request.hasMatchedLocality -> context.getString(R.string.small_talk_other_band_different_location)
                !request.hasMatchedInterest -> context.getString(R.string.small_talk_other_band_different_interests)
                !request.hasMatchedGender -> context.getString(R.string.small_talk_other_band_different_mixity)
                !request.hasMatchedFormat -> context.getString(R.string.small_talk_other_band_duo)
                else -> context.getString(R.string.small_talk_other_band_group)
            }

            // 4. Clic pour rejoindre ou forcer le match
            binding.buttonJoin.setOnClickListener {
                onJoinClicked(request)
            }
        }

        private fun renderAvatars(urls: List<String>) {
            val imageViews = listOf(
                binding.avatar1, binding.avatar2, binding.avatar3, binding.avatar4, binding.avatar5
            )
            imageViews.forEach { it.visibility = View.GONE }
            urls.forEachIndexed { index, url ->
                imageViews[index].apply {
                    visibility = View.VISIBLE
                    Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .transform(CircleCrop())
                        .into(this)
                }
            }
        }
    }
}
