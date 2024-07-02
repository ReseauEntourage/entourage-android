package social.entourage.android.main_filter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.ItemMainFilterBinding

class MainFilterAdapter(
    private val context: Context,
    private var items: List<MainFilterInterestForAdapter>,
    private val onItemClicked: (MainFilterInterestForAdapter) -> Unit
) : RecyclerView.Adapter<MainFilterAdapter.FilterViewHolder>() {

    inner class FilterViewHolder(val binding: ItemMainFilterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MainFilterInterestForAdapter) {
            binding.tvInterestTitleFromRight.text = item.title
            binding.tvInterestSubTitleFromRight.text = item.subtitle
            updateBackground(item.isSelected)

            binding.root.setOnClickListener {
                item.isSelected = !item.isSelected
                updateBackground(item.isSelected)
                onItemClicked(item)
            }
        }

        private fun updateBackground(isSelected: Boolean) {
            val backgroundResource = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
            binding.root.setBackgroundResource(backgroundResource)
            binding.ivInterestCheck.setImageResource(if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemMainFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount() = items.size

    fun resetItems(newItems: List<MainFilterInterestForAdapter>) {
        items = newItems
        notifyDataSetChanged()
    }
}
