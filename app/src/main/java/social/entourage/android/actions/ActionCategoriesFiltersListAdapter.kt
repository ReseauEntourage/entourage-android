package social.entourage.android.actions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.EventUtils
import social.entourage.android.databinding.LayoutActionCategoryItemBinding

class ActionCategoriesFiltersListAdapter(
    private var catfiltersList: List<ActionSection>
) : RecyclerView.Adapter<ActionCategoriesFiltersListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutActionCategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(category: ActionSection) {
            if (category.isSelected) binding.title.setTypeface(
                binding.title.typeface,
                android.graphics.Typeface.BOLD
            )
            binding.title.text = EventUtils.showTagTranslated(binding.root.context,category.id!!)
            binding.subtitle.text = EventUtils.showSubTagTranslated(binding.root.context, category.id)
            binding.checkBox.isChecked = category.isSelected
            binding.icon.setImageResource(category.icon)
            binding.layout.setOnClickListener {
                if (category.isSelected) {
                    binding.title.typeface =
                        android.graphics.Typeface.create(
                            binding.title.typeface,
                            android.graphics.Typeface.NORMAL
                        )
                } else {
                    binding.title.setTypeface(
                        binding.title.typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
                category.isSelected = !(category.isSelected)
                binding.checkBox.isChecked = !binding.checkBox.isChecked
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutActionCategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(catfiltersList[position])
    }

    override fun getItemCount(): Int {
        return catfiltersList.size
    }
}