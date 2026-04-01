package social.entourage.android.onboarding.onboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.ItemOnboardingProfileChoiceBinding

enum class ProfileChoiceType { ENTOUR, BE_ENTOUR, ASSO }

data class ProfileChoice(
    val type: ProfileChoiceType,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    @param:DrawableRes val iconRes: Int,
    var selected: Boolean
)

class ProfileChoiceAdapter(
    private var items: List<ProfileChoice>,
    private val onClick: (ProfileChoice) -> Unit
) : RecyclerView.Adapter<ProfileChoiceAdapter.VH>() {

    inner class VH(val binding: ItemOnboardingProfileChoiceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProfileChoice) {
            binding.tvInterestTitleFromRight.setText(item.titleRes)
            binding.tvInterestSubTitleFromRight.setText(item.subtitleRes)
            binding.ivInterestIcon.setImageResource(item.iconRes)

            // check / uncheck + bordure
            binding.ivInterestCheck.setImageResource(
                if (item.selected) R.drawable.ic_onboarding_check else R.drawable.ic_onboarding_uncheck
            )
            binding.view.setBackgroundResource(
                if (item.selected) R.drawable.shape_border_orange else R.drawable.shape_grey_border
            )

            binding.view.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOnboardingProfileChoiceBinding.inflate(inflater, parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    /** Sélection exclusive */
    fun updateSelection(isEntour: Boolean, isBeEntour: Boolean, isAsso: Boolean) {
        items = items.map {
            when (it.type) {
                ProfileChoiceType.ENTOUR -> it.copy(selected = isEntour)
                ProfileChoiceType.BE_ENTOUR -> it.copy(selected = isBeEntour)
                ProfileChoiceType.ASSO -> it.copy(selected = isAsso)
            }
        }
        notifyDataSetChanged()
    }
}
