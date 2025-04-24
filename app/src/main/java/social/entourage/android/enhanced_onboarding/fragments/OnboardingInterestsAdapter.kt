package social.entourage.android.enhanced_onboarding.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutItemOnboardingInterestsBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter

class OnboardingInterestsAdapter(
    private val context: Context,
    private val isFromInterest: Boolean,
    var interests: List<InterestForAdapter>,
    private val onInterestClicked: (InterestForAdapter) -> Unit
) : RecyclerView.Adapter<OnboardingInterestsAdapter.InterestViewHolder>() {

    var forceSingleSelectionForSmallTalk: Boolean = false
    inner class InterestViewHolder(val binding: LayoutItemOnboardingInterestsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(interest: InterestForAdapter) {
            binding.ivInterestIcon.setImageResource(interest.icon)

            if (isFromInterest) {
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
                if (forceSingleSelectionForSmallTalk && !isFromInterest) {
                    // Sélection unique (activée uniquement dans SmallTalk sauf pour la grille finale)
                    val previousSelected = interests.indexOfFirst { it.isSelected }
                    val currentIndex = adapterPosition

                    if (previousSelected != currentIndex) {
                        interests.forEach { it.isSelected = false }
                        interests[currentIndex].isSelected = true
                        notifyItemChanged(previousSelected)
                        notifyItemChanged(currentIndex)
                    }
                } else {
                    // Multi-sélection classique
                    interest.isSelected = !interest.isSelected
                    notifyItemChanged(adapterPosition)
                }

                onInterestClicked(interest)
            }
        }

        private fun updateSelectionUI(isSelected: Boolean) {
            val background = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
            val checkIcon = if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck
            binding.root.setBackgroundResource(background)
            binding.ivInterestCheck.setImageResource(checkIcon)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LayoutItemOnboardingInterestsBinding.inflate(inflater, parent, false)
        return InterestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        holder.bind(interests[position])
    }

    override fun getItemCount(): Int = interests.size
}
