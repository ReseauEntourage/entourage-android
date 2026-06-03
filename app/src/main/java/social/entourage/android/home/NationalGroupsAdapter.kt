package social.entourage.android.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.button.MaterialButton
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.tools.utils.px

class NationalGroupsAdapter(
    groups: List<Group>,
    private val onJoinClick: (Group, Int) -> Unit
) : RecyclerView.Adapter<NationalGroupsAdapter.GroupViewHolder>() {

    private var groups: List<Group> = groups

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_home_welcome_national_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position])
    }

    override fun getItemCount(): Int = groups.size

    inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val groupImage: ImageView = itemView.findViewById(R.id.group_image)
        private val groupName: TextView = itemView.findViewById(R.id.group_name)
        private val memberCount: TextView = itemView.findViewById(R.id.member_count)
        private val joinButton: MaterialButton = itemView.findViewById(R.id.join_button)

        fun bind(group: Group) {
            groupName.text = group.name
            memberCount.text = "${group.members_count ?: 0} membres"

            // Load group image
            if (!group.imageUrl.isNullOrEmpty()) {
                Glide.with(itemView.context)
                    .load(group.imageUrl)
                    .apply(RequestOptions().override(100.px, 100.px))
                    .placeholder(R.drawable.new_placeholder_group)
                    .error(R.drawable.new_placeholder_group)
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(groupImage)
            } else {
                Glide.with(itemView.context)
                    .load(R.drawable.new_placeholder_group)
                    .apply(RequestOptions().override(100.px, 100.px))
                    .transform(CenterCrop(), RoundedCorners(20.px))
                    .into(groupImage)
            }

            // Set button state based on member status
            updateJoinButton(group.member)

            joinButton.setOnClickListener {
                onJoinClick(group, adapterPosition)
            }
        }

        fun updateJoinButton(isMember: Boolean) {
            if (isMember) {
                // Style "rejoint" - bouton secondaire avec tick (comme EnhancedOnboarding)
                joinButton.text = "Rejoint ✓"
                joinButton.setBackgroundColor(itemView.context.getColor(R.color.beige))
                joinButton.setTextColor(itemView.context.getColor(R.color.orange))
                //joinButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0)
                joinButton.compoundDrawablePadding = 8
            } else {
                // Style "rejoindre" - bouton primaire orange
                joinButton.text = "Rejoindre"
                joinButton.setBackgroundColor(itemView.context.getColor(R.color.orange))
                joinButton.setTextColor(itemView.context.getColor(android.R.color.white))
                joinButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }
    }

    fun updateGroupJoinedStatus(groupId: Int, isJoined: Boolean) {
        groups.find { it.id == groupId }?.member = isJoined
        notifyDataSetChanged()
    }

    fun updateGroups(newGroups: List<Group>) {
        // Simply replace the entire list with the new data
        groups = newGroups.toList()
        notifyDataSetChanged()
    }
}