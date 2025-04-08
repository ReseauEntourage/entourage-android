package social.entourage.android.discussions.members

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.GroupMember
import social.entourage.android.databinding.NewGroupMemberItemBinding
import social.entourage.android.groups.details.members.OnItemShowListener
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const

class MembersConversationListAdapter(
    private var membersList: List<GroupMember>,
    private var userCreatorId:Int?,
    private var onItemShowListener: OnItemShowListener
) : RecyclerView.Adapter<MembersConversationListAdapter.ViewHolder>() {

    fun updateCreatorId(userCreatorId:Int?) {
        this.userCreatorId = userCreatorId
        notifyDataSetChanged()
    }


    inner class ViewHolder(val binding: NewGroupMemberItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewGroupMemberItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(membersList[position]) {

                val isMe = EntourageApplication.get().me()?.id == id
                binding.contact.visibility = if (isMe) View.INVISIBLE else View.VISIBLE
                binding.name.text = displayName
                binding.reaction.root.visibility = View.GONE
                if (this.id == userCreatorId) {
                    binding.ambassador.text = "Admin"
                    binding.ambassador.visibility = View.VISIBLE
                }
                else {
                    binding.ambassador.visibility = View.GONE
                }


                avatarUrl?.let { avatarURL ->
                    Glide.with(holder.itemView.context)
                        .load(avatarURL)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.picture)
                } ?: kotlin.run {
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.picture)
                }

                binding.layout.setOnClickListener { view ->
                    ProfileFullActivity.isMe = false
                    ProfileFullActivity.userId = this.id.toString()
                    view.context.startActivity(
                        Intent(view.context, ProfileFullActivity::class.java).putExtra(
                            Const.USER_ID,
                            this.id
                        )
                    )
                }
                binding.contact.setOnClickListener {
                    id?.let { id->
                        onItemShowListener.onShowConversation(id)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return membersList.size
    }
}