package social.entourage.android.enhanced_onboarding.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutItemOnboardingInterestsBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter

class OnboardingInterestsAdapter(private val context: Context, val isFromInterest :Boolean, var interests: List<InterestForAdapter>, private val onInterestClicked: (InterestForAdapter) -> Unit) :
    RecyclerView.Adapter<OnboardingInterestsAdapter.InterestViewHolder>() {

    inner class InterestViewHolder(val binding: LayoutItemOnboardingInterestsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(interest: InterestForAdapter) {
            binding.ivInterestIcon.setImageResource(interest.icon)
            if(isFromInterest){
                binding.tvInterestTitle.text = interest.title
                binding.tvInterestTitleFromRight.visibility = View.GONE
                binding.tvInterestTitle.visibility = View.VISIBLE
                binding.tvInterestSubTitleFromRight.visibility = View.GONE
            }else{
                binding.tvInterestTitleFromRight.text = interest.title
                binding.tvInterestSubTitleFromRight.text = interest.subtitle
                binding.tvInterestTitle.visibility = View.GONE
                binding.tvInterestTitleFromRight.visibility = View.VISIBLE
                binding.tvInterestSubTitleFromRight.visibility = View.VISIBLE
            }
            if(binding.tvInterestSubTitleFromRight.text.isEmpty()){
                binding.tvInterestSubTitleFromRight.visibility = View.GONE
            }
            updateBackground(interest.isSelected)
            binding.root.setOnClickListener {
                onInterestClicked(interest)
            }
        }

        private fun updateBackground(isSelected: Boolean) {
            val backgroundResource = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
            binding.root.setBackgroundResource(backgroundResource)
            binding.ivInterestCheck.setImageResource(if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InterestViewHolder {
        val binding = LayoutItemOnboardingInterestsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InterestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InterestViewHolder, position: Int) {
        holder.bind(interests[position])
    }

    override fun getItemCount() = interests.size
}



