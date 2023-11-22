package social.entourage.android.actions

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_action_category_item.view.*
import social.entourage.android.R
import social.entourage.android.api.model.ActionSection
import social.entourage.android.api.model.EventUtils

class ActionCategoriesFiltersListAdapter(
    private var catfiltersList: List<ActionSection>
) : RecyclerView.Adapter<ActionCategoriesFiltersListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(category: ActionSection) {
            if (category.isSelected) binding.title.setTypeface(
                binding.title.typeface,
                android.graphics.Typeface.BOLD
            )
            binding.title.text = EventUtils.showTagTranslated(binding.context,category.title!!)
            Log.wtf("wtf", "subtitle: ${category.subtitle!!}")
            binding.subtitle.text = EventUtils.showSubTagTranslated(binding.context, category.title!!)
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
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_action_category_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(catfiltersList[position])
    }

    override fun getItemCount(): Int {
        return catfiltersList.size
    }
}