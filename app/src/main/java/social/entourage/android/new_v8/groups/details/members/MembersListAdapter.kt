package social.entourage.android.new_v8.groups.details.members


import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.databinding.NewGroupMemberItemBinding
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const

class MembersListAdapter(
    private var membersList: List<EntourageUser>
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
                communityRoles?.let {
                    if (it.contains(Const.AMBASSADOR)) binding.ambassador.visibility = View.VISIBLE
                }
                partner?.let {
                    binding.partner.visibility = View.VISIBLE
                    binding.partner.text = it.name
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

                binding.layout.setOnClickListener {
                    binding.picture.context.startActivity(
                        Intent(binding.picture.context, UserProfileActivity::class.java).putExtra(
                            Const.USER_ID,
                            this.userId
                        )
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return membersList.size
    }
}