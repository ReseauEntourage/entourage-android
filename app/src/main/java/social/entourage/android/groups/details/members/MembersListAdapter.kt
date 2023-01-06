package social.entourage.android.groups.details.members

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.databinding.NewGroupMemberItemBinding
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const

interface OnItemShowListener {
    fun onShowConversation(userId: Int)
}

class MembersListAdapter(
    private var membersList: List<EntourageUser>,
    private var onItemShowListener: OnItemShowListener
) : RecyclerView.Adapter<MembersListAdapter.ViewHolder>() {

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

                val isMe = EntourageApplication.get().me()?.id == userId
                binding.contact.visibility = if (isMe) View.INVISIBLE else View.VISIBLE

                binding.name.text = displayName

                val roles = getCommunityRoleWithPartnerFormated()

                if (roles != null) {
                    binding.ambassador.visibility = View.VISIBLE
                    binding.ambassador.text = roles
                }
                else {
                    binding.ambassador.visibility = View.GONE
                }

                avatarURLAsString?.let { avatarURL ->
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
                    (view.context as? Activity)?.startActivityForResult(
                        Intent(view.context, UserProfileActivity::class.java).putExtra(
                            Const.USER_ID,
                            this.userId
                    ), 0)
                }

                binding.contact.setOnClickListener {
                    onItemShowListener.onShowConversation(userId)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return membersList.size
    }
}