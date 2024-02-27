package social.entourage.android.events.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.NewChooseGroupEventItemBinding

interface OnItemCheckListener {
    fun onItemCheck(item: Group)
    fun onItemUncheck(item: Group)
}

class ChooseGroupEventListAdapter(
    var groupsList: List<Group>,
    var onItemClick: OnItemCheckListener
) : RecyclerView.Adapter<ChooseGroupEventListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewChooseGroupEventItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewChooseGroupEventItemBinding.inflate(
            LayoutInflater.from(parent.context),  parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            if (groupsList[position].isSelected) holder.binding.title.setTypeface(
                binding.title.typeface,
                android.graphics.Typeface.BOLD
            )
            binding.title.text = groupsList[position].name
            binding.checkBox.isChecked = groupsList[position].isSelected
            binding.layout.setOnClickListener {
                if (groupsList[position].isSelected) {
                    onItemClick.onItemUncheck(groupsList[position])
                    binding.title.typeface =
                        android.graphics.Typeface.create(
                            binding.title.typeface,
                            android.graphics.Typeface.NORMAL
                        )
                } else {
                    onItemClick.onItemCheck(groupsList[position])
                    binding.title.setTypeface(
                        binding.title.typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
                groupsList[position].isSelected = !(groupsList[position].isSelected)
                binding.checkBox.isChecked = !binding.checkBox.isChecked
            }
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}