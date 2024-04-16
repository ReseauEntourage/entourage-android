package social.entourage.android.enhanced_onboarding.fragments

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.LayoutItemOnboardingInterestsBinding
import social.entourage.android.enhanced_onboarding.InterestForAdapter

class OnboardingInterestsAdapter(private val context: Context, var interests: List<InterestForAdapter>, private val onInterestClicked: (InterestForAdapter) -> Unit) :
    RecyclerView.Adapter<OnboardingInterestsAdapter.InterestViewHolder>() {

    inner class InterestViewHolder(val binding: LayoutItemOnboardingInterestsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(interest: InterestForAdapter) {
            binding.ivInterestIcon.setImageResource(interest.icon)
            binding.tvInterestTitle.text = interest.title
            updateBackground(interest.isSelected)

            binding.root.setOnClickListener {
                onInterestClicked(interest)
            }
        }

        private fun updateBackground(isSelected: Boolean) {
            val backgroundResource = if (isSelected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
            binding.root.setBackgroundResource(backgroundResource)
            binding.ivInterestCheck.setImageResource(if (isSelected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck)
            binding.ivInterestCheck.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
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



