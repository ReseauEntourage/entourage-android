package social.entourage.android.new_v8.events.create

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_profile_edit_interest_item.view.*
import social.entourage.android.R
import social.entourage.android.new_v8.models.Group

interface OnItemCheckListener {
    fun onItemCheck(item: Group)
    fun onItemUncheck(item: Group)
}

class ChooseGroupEventListAdapter(
    var groupsList: List<Group>,
    var onItemClick: OnItemCheckListener
) : RecyclerView.Adapter<ChooseGroupEventListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_choose_group_event_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            if (groupsList[position].isSelected) binding.title.setTypeface(
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