package social.entourage.android.new_v8.groups.list

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.R
import social.entourage.android.api.model.MetaData
import social.entourage.android.databinding.NewGroupItemBinding
import social.entourage.android.new_v8.models.Group
import social.entourage.android.new_v8.user.OnItemCheckListener
import social.entourage.android.new_v8.utils.px

interface OnItemCheckListener {
    fun onItemCheck(item: Group)
    fun onItemUncheck(item: Group)
}

class GroupsListAdapter(
    var groupsList: List<Group>,
    var onItemClick: social.entourage.android.new_v8.groups.list.OnItemCheckListener
) : RecyclerView.Adapter<GroupsListAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewGroupItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewGroupItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(groupsList[position]) {
                binding.groupName.text = this.name
                binding.members.text = String.format(
                    holder.itemView.context.getString(R.string.progress_km),
                    this.members?.size
                )
                binding.futureOutgoingEvents.text = String.format(
                    holder.itemView.context.getString(R.string.future_outgoing_events_number),
                    this.futureOutingsCount
                )
                Glide.with(binding.image.context)
                    .load(Uri.parse(this.imageUrl))
                    .apply(RequestOptions().override(90.px, 90.px))
                    .placeholder(R.drawable.new_illu_header_group)
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(binding.image)
            }
        }
    }

    override fun getItemCount(): Int {
        return groupsList.size
    }
}