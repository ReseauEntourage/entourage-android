package social.entourage.android.enhanced_onboarding.fragments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutItemOnboardingInterestsBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter

class OnboardingInterestsAdapter(
    private val isFromInterest: Boolean,
    private val onInterestClicked: (InterestForAdapter) -> Unit
) : ListAdapter<InterestForAdapter, OnboardingInterestsAdapter.InterestViewHolder>(InterestDiffCallback()) {

    var isFromInterestLocal = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var forceSingleSelectionForSmallTalk: Boolean = false

    inner class InterestViewHolder(val binding: LayoutItemOnboardingInterestsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(interest: InterestForAdapter) {
            if(interest.icon == 999){
                binding.ivInterestIcon.visibility = View.GONE
            }else{
                binding.ivInterestIcon.visibility = View.VISIBLE
                binding.ivInterestIcon.setImageResource(interest.icon)
            }

            if (isFromInterest || isFromInterestLocal) {
                binding.tvInterestTitle.text = interest.title
                binding.tvInterestTitle.visibility = View.VISIBLE
                binding.tvInterestTitleFromRight.visibility = View.GONE
                binding.tvInterestSubTitleFromRight.visibility = View.GONE
            } else {
                binding.tvInterestTitleFromRight.text = interest.title
                binding.tvInterestSubTitleFromRight.text = interest.subtitle
                binding.tvInterestTitle.visibility = View.GONE
                binding.tvInterestTitleFromRight.visibility = View.VISIBLE
                binding.tvInterestSubTitleFromRight.visibility =
                    if (interest.subtitle.isEmpty()) View.GONE else View.VISIBLE
            }

            updateSelectionUI(interest.isSelected)

            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    handleItemClick(position)
                }
            }
        }

        private fun updateSelectionUI(isSelected: Boolean) {
            val background = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
            val checkIcon = if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck
            binding.root.setBackgroundResource(background)
            binding.ivInterestCheck.setImageResource(checkIcon)
        }

        private fun handleItemClick(position: Int) {
            val updatedList = currentList.mapIndexed { index, item ->
                if (forceSingleSelectionForSmallTalk && !isFromInterest) {
                    // Sélection unique
                    item.copy(isSelected = index == position)
                } else if (index == position) {
                    // Toggle sélection multiple
                    item.copy(isSelected = !item.isSelected)
                } else {
                    item
                }
            }
            submitList(updatedList)
            onInterestClicked(updatedList[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemOnboardingInterestsBinding.inflate(inflater, parent, false)
        return InterestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class InterestDiffCallback : DiffUtil.ItemCallback<InterestForAdapter>() {
    override fun areItemsTheSame(oldItem: InterestForAdapter, newItem: InterestForAdapter): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: InterestForAdapter, newItem: InterestForAdapter): Boolean {
        return oldItem.isSelected == newItem.isSelected
    }
}
