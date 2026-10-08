package social.entourage.android.events.create

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.ItemChooseGroupEventBinding

interface OnItemCheckListener {
    fun onItemCheck(item: Group)
    fun onItemUncheck(item: Group)
}

class ChooseGroupEventListAdapter(
    var groupsList: List<Group>,
    var onItemClick: OnItemCheckListener
) : RecyclerView.Adapter<ChooseGroupEventListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemChooseGroupEventBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemChooseGroupEventBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groupsList[position]
        with(holder.binding) {
            title.text = group.name
            check.isSelected = group.isSelected
            layout.setOnClickListener {
                if (group.isSelected) onItemClick.onItemUncheck(group) else onItemClick.onItemCheck(group)
                group.isSelected = !group.isSelected
                check.isSelected = group.isSelected
            }
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}
