package social.entourage.android.new_v8.events.details.feed

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.NewAboutEventGroupItemBinding
import social.entourage.android.new_v8.groups.details.feed.FeedActivity
import social.entourage.android.new_v8.models.GroupEvent
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.underline


class AboutEventGroupListAdapter(
    var groupsList: List<GroupEvent>,
) : RecyclerView.Adapter<AboutEventGroupListAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewAboutEventGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewAboutEventGroupItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.root.setOnClickListener {
            holder.binding.groupName.context.startActivity(
                Intent(holder.binding.groupName.context, FeedActivity::class.java).putExtra(
                    Const.GROUP_ID,
                    groupsList[position].id
                )
            )
        }
        groupsList[position].name?.let { holder.binding.groupName.underline(it) }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}