package social.entourage.android.home.pedago

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.NewFilterItemLightBinding
import social.entourage.android.api.model.Category

interface OnItemClickListener {
    fun onItemClick(filter: Category, position: Int)
}

class FilterAdapter(
    private var filtersList: List<Category>,
    private var onItemClickListener: OnItemClickListener,
    private var selectedFilterPosition: Int
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    private var selectedItemPos = selectedFilterPosition
    private var lastItemSelectedPos = selectedFilterPosition

    inner class ViewHolder(val binding: NewFilterItemLightBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun defaultBg() {
            binding.root.background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.new_bg_unselected_filter
                )
            TextViewCompat.setTextAppearance(
                binding.label,
                R.style.unselected_filter
            )
        }

        fun selectedBg() {
            binding.root.background =
                AppCompatResources.getDrawable(
                    itemView.context,
                    R.drawable.new_bg_selected_filter
                )
            TextViewCompat.setTextAppearance(
                binding.label,
                R.style.selected_filter
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewFilterItemLightBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.label.text = holder.itemView.context.getString(filtersList[position].id)
        holder.binding.root.setOnClickListener {
            selectedItemPos = position
            lastItemSelectedPos = if (lastItemSelectedPos == -1) selectedItemPos
            else {
                notifyItemChanged(lastItemSelectedPos)
                selectedItemPos
            }
            notifyItemChanged(selectedItemPos)
            onItemClickListener.onItemClick(filtersList[position], position)
        }
        if (position == selectedItemPos)
            holder.selectedBg()
        else
            holder.defaultBg()
    }

    override fun getItemCount(): Int {
        return filtersList.size
    }
}