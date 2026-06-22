package social.entourage.android.badges

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.ItemBadgeListBinding
import social.entourage.android.databinding.ItemBadgeSectionHeaderBinding
import java.text.SimpleDateFormat
import java.util.Locale

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

        fun formatDate(iso8601: String?): String {
            if (iso8601.isNullOrEmpty()) return ""
            return try {
                val inputFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
                val outputFmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = inputFmt.parse(iso8601) ?: return ""
                outputFmt.format(date)
            } catch (e: Exception) {
                iso8601.substringBefore("T")
            }
        }
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

            when {
                progress.isObtained -> {
                    // Green card
                    binding.cardBadgeItem.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(ctx, android.R.color.white))
                    binding.tvBadgeEmoji.background =
                        androidx.core.content.ContextCompat.getDrawable(ctx, social.entourage.android.R.drawable.bg_circle_light_orange)
                    val formattedDate = formatDate(progress.obtainedDate)
                    binding.tvBadgeSubtitle.text = if (formattedDate.isNotEmpty()) {
                        ctx.getString(social.entourage.android.R.string.badge_obtained_on, formattedDate)
                    } else {
                        ctx.getString(social.entourage.android.R.string.badge_obtained_on, "")
                    }
                    binding.tvBadgeSubtitle.setTextColor(
                        androidx.core.content.ContextCompat.getColor(ctx, social.entourage.android.R.color.grey))
                    binding.progressBar.progress = 100
                    binding.progressBar.progressDrawable =
                        androidx.core.content.ContextCompat.getDrawable(ctx, social.entourage.android.R.drawable.badge_progress_bar_green)
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.tvBadgeProgressLabel.visibility = android.view.View.GONE
                }
                progress.progress > 0 -> {
                    // In-progress: white card
                    binding.cardBadgeItem.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(ctx, android.R.color.white))
                    binding.tvBadgeEmoji.background =
                        androidx.core.content.ContextCompat.getDrawable(ctx, social.entourage.android.R.drawable.bg_circle_light_orange)
                    binding.tvBadgeSubtitle.text = ctx.getString(def.descriptionShortRes)
                    binding.tvBadgeSubtitle.setTextColor(
                        androidx.core.content.ContextCompat.getColor(ctx, social.entourage.android.R.color.grey))
                    val pct = if (progress.maxProgress > 0) (progress.progress * 100 / progress.maxProgress) else 0
                    binding.progressBar.progress = pct
                    binding.progressBar.progressDrawable =
                        androidx.core.content.ContextCompat.getDrawable(ctx, social.entourage.android.R.drawable.badge_progress_bar)
                    binding.progressBar.visibility = android.view.View.VISIBLE
                    binding.tvBadgeProgressLabel.text = "${progress.progress}/${progress.maxProgress}"
                    binding.tvBadgeProgressLabel.visibility = android.view.View.VISIBLE
                }
                else -> {
                    // Not started: light gray card, all text black
                    binding.cardBadgeItem.setCardBackgroundColor(
                        androidx.core.content.ContextCompat.getColor(ctx, social.entourage.android.R.color.primary_light))
                    binding.tvBadgeEmoji.background =
                        androidx.core.content.ContextCompat.getDrawable(ctx, social.entourage.android.R.drawable.bg_circle_light)
                    binding.tvBadgeSubtitle.text = ctx.getString(def.descriptionShortRes)
                    binding.tvBadgeSubtitle.setTextColor(
                        androidx.core.content.ContextCompat.getColor(ctx, social.entourage.android.R.color.black))
                    binding.progressBar.visibility = android.view.View.GONE
                    binding.tvBadgeProgressLabel.text = "0/${progress.maxProgress}"
                    binding.tvBadgeProgressLabel.setTextColor(
                        androidx.core.content.ContextCompat.getColor(ctx, social.entourage.android.R.color.black))
                    binding.tvBadgeProgressLabel.visibility = android.view.View.VISIBLE
                }
            }

            binding.root.setOnClickListener { onClick(progress) }
        }
    }
}
