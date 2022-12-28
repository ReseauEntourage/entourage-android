package social.entourage.android.groups.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.NewGroupsInterestsImageItemBinding
import social.entourage.android.api.model.Interest

class GroupsInterestsListAdapter(
    var interestsList: List<String>,
) : RecyclerView.Adapter<GroupsInterestsListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewGroupsInterestsImageItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewGroupsInterestsImageItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.image.setImageResource(Interest.getIconFromId(interestsList[position]))
    }

    override fun getItemCount(): Int {
        return interestsList.size
    }
}