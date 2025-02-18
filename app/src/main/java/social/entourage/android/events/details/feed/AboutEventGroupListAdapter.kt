package social.entourage.android.events.details.feed

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.databinding.NewAboutEventGroupItemBinding
import social.entourage.android.api.model.GroupEvent
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.underline

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
        if (position >= 0 && position < groupsList.size) {
            holder.binding.root.setOnClickListener { view ->
                val intent = Intent(view.context, EventFeedActivity::class.java)
                intent.putExtra(Const.GROUP_ID, groupsList[position].id)
                view.context.startActivity(intent)
            }

            groupsList[position].name?.let { holder.binding.groupName.underline(it) }
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}