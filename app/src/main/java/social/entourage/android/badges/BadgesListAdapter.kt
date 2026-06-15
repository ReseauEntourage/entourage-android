package social.entourage.android.badges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.ItemBadgeListBinding
import social.entourage.android.databinding.ItemBadgeSectionHeaderBinding

sealed class BadgeListItem {
    data class Header(val title: String) : BadgeListItem()
    data class BadgeItem(val progress: UserBadgeProgress) : BadgeListItem()
}

class BadgesListAdapter(
    private val items: List<BadgeListItem>,
    private val onBadgeClick: (UserBadgeProgress) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_BADGE = 1
    }

    override fun getItemViewType(position: Int) = when (items[position]) {
        is BadgeListItem.Header -> TYPE_HEADER
        is BadgeListItem.BadgeItem -> TYPE_BADGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(ItemBadgeSectionHeaderBinding.inflate(inflater, parent, false))
            else -> BadgeViewHolder(ItemBadgeListBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is BadgeListItem.Header -> (holder as HeaderViewHolder).bind(item.title)
            is BadgeListItem.BadgeItem -> (holder as BadgeViewHolder).bind(item.progress, onBadgeClick)
        }
    }

    override fun getItemCount() = items.size

    class HeaderViewHolder(private val binding: ItemBadgeSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.tvSectionHeader.text = title
        }
    }

    class BadgeViewHolder(private val binding: ItemBadgeListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(progress: UserBadgeProgress, onClick: (UserBadgeProgress) -> Unit) {
            val ctx = binding.root.context
            val def = progress.definition

            binding.tvBadgeEmoji.text = def.emoji
            binding.tvBadgeTitle.text = ctx.getString(def.titleRes)

            if (progress.isObtained) {
                val dateText = progress.obtainedDate ?: ""
                binding.tvBadgeSubtitle.text = if (dateText.isNotEmpty()) {
                    ctx.getString(social.entourage.android.R.string.badge_obtained_on, dateText)
                } else {
                    ctx.getString(social.entourage.android.R.string.badge_obtained_on, "")
                }
                binding.progressBar.progress = 100
                binding.tvBadgeProgressLabel.visibility = android.view.View.GONE
            } else {
                binding.tvBadgeSubtitle.text = ctx.getString(def.descriptionShortRes)
                val pct = if (def.maxProgress > 0) (progress.progress * 100 / def.maxProgress) else 0
                binding.progressBar.progress = pct
                binding.tvBadgeProgressLabel.text = "${progress.progress}/${def.maxProgress}"
                binding.tvBadgeProgressLabel.visibility = android.view.View.VISIBLE
            }

            // Color the progress bar green when obtained
            if (progress.isObtained) {
                binding.progressBar.progressDrawable =
                    androidx.core.content.ContextCompat.getDrawable(ctx, social.entourage.android.R.drawable.badge_progress_bar_green)
            } else {
                binding.progressBar.progressDrawable =
                    androidx.core.content.ContextCompat.getDrawable(ctx, social.entourage.android.R.drawable.badge_progress_bar)
            }

            binding.root.setOnClickListener { onClick(progress) }
        }
    }
}
