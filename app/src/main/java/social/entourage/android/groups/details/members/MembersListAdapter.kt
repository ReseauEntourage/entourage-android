package social.entourage.android.groups.details.members

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.ReactionType
import social.entourage.android.databinding.NewGroupMemberItemBinding
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const

interface OnItemShowListener {
    fun onShowConversation(userId: Int)
}

class MembersListAdapter(
    private val context:Context,
    private var membersList: List<EntourageUser>,
    private var reactionList: List<ReactionType>,
    private var onItemShowListener: OnItemShowListener,

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

    fun resetData(membersList: List<EntourageUser>, reactionList: List<ReactionType>) {
        this.membersList = membersList
        this.reactionList = reactionList
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(membersList[position]) {
                if(reactionList.isNotEmpty()) {
                    binding.reaction.layoutItemReactionParent.visibility = View.VISIBLE
                    Glide.with(context)
                        .load(reactionList[position].imageUrl)
                        .into(binding.reaction.image)
                }
                else {
                    binding.reaction.layoutItemReactionParent.visibility = View.GONE
                }
                val isMe = EntourageApplication.get().me()?.id == userId
                binding.contact.visibility = if (isMe) View.INVISIBLE else View.VISIBLE
                if(membersList[position].confirmedAt != null) {
                    binding.name.text = displayName + " - Participation confirmée"
                }
                else {
                    binding.name.text = displayName
                }

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