package social.entourage.android.profile

// SettingProfileFullAdapter.kt

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.SettingItemSeparatorBinding
import social.entourage.android.databinding.SettingsItemUserSectionBinding

class SettingProfileFullAdapter(
    private val items: List<ProfileSectionItem>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_SEPARATOR = 0
        private const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is ProfileSectionItem.Separator -> VIEW_TYPE_SEPARATOR
            is ProfileSectionItem.Item -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_SEPARATOR -> {
                val binding = SettingItemSeparatorBinding.inflate(inflater, parent, false)
                SeparatorViewHolder(binding)
            }
            VIEW_TYPE_ITEM -> {
                val binding = SettingsItemUserSectionBinding.inflate(inflater, parent, false)
                ItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProfileSectionItem.Separator -> (holder as SeparatorViewHolder).bind(item)
            is ProfileSectionItem.Item -> (holder as ItemViewHolder).bind(item)
        }
    }

    // ViewHolder for Separator
    inner class SeparatorViewHolder(
        private val binding: SettingItemSeparatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(separator: ProfileSectionItem.Separator) {
            binding.tvTitleSeparator.text = separator.title
        }
    }

    // ViewHolder for Item
    inner class ItemViewHolder(
        private val binding: SettingsItemUserSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProfileSectionItem.Item) {
            binding.ivUserSection.setImageResource(item.iconRes)
            binding.tvTitleUserSection.text = item.title
            binding.tvSubtitleUserSection.text = item.subtitle
            binding.ivArrowUserSection.setImageResource(R.drawable.arrow_right_orange) // Assuming this is constant
        }
    }
}
