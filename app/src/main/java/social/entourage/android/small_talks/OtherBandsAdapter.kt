package social.entourage.android.small_talks

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import social.entourage.android.R
import social.entourage.android.api.model.SmallTalk
import social.entourage.android.api.model.UserSmallTalkRequest
import social.entourage.android.databinding.ItemOtherBandBinding

class OtherBandsAdapter(
    private val items: List<UserSmallTalkRequest>,
    private val onJoinClicked: (UserSmallTalkRequest) -> Unit
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

        fun bind(request: UserSmallTalkRequest) {
            val context = binding.root.context
            val smallTalk = request.smallTalk
            val fallbackUser = request.user

            val avatarUrls = smallTalk?.members?.mapNotNull { it.avatarUrl }
                ?: listOfNotNull(fallbackUser?.avatarURL)

            renderAvatars(avatarUrls)

            val names = smallTalk?.members?.mapNotNull { it.displayName }
                ?: listOfNotNull(fallbackUser?.displayName)

            binding.textMembers.text = context.getString(
                R.string.small_talk_other_band_with,
                names.joinToString(" et ")
            )

            // Texte selon les incompatibilités
            binding.textDescription.text = when {
                SmallTalkListOtherBands.matchingLocality && request.matchLocality == false ->
                    context.getString(R.string.small_talk_other_band_different_location)
                //SmallTalkListOtherBands.matchingInterest && request.matchInterest == false ->
                   // context.getString(R.string.small_talk_other_band_different_interests)
                SmallTalkListOtherBands.matchingGender && request.matchGender == false ->
                    context.getString(R.string.small_talk_other_band_different_interests)
                SmallTalkListOtherBands.matchingGroup == "one" && request.matchFormat != "one" ->
                    context.getString(R.string.small_talk_other_band_duo)
                else ->
                    context.getString(R.string.small_talk_other_band_group)
            }

            binding.buttonJoin.setOnClickListener {
                onJoinClicked(request)
            }
        }

        private fun renderAvatars(urls: List<String>) {
            val imageViews = listOf(
                binding.avatar1, binding.avatar2, binding.avatar3, binding.avatar4, binding.avatar5
            )

            imageViews.forEach { it.visibility = View.GONE }

            urls.take(5).forEachIndexed { index, url ->
                val imageView = imageViews[index]
                imageView.visibility = View.VISIBLE
                Glide.with(imageView)
                    .load(url)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .transform(CircleCrop())
                    .into(imageView)
            }
        }
    }
}
