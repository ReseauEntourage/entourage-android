package social.entourage.android.profile.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import kotlinx.android.synthetic.main.new_item_blocker_user.view.*
import social.entourage.android.R
import social.entourage.android.api.model.UserBlockedUser

interface OnItemCheckListener {
    fun onItemCheck(position: Int)
}

class UnblockUsersListAdapter(
    var blockUsersList: List<UserBlockedUser>,
    var onItemClick: OnItemCheckListener,
) : RecyclerView.Adapter<UnblockUsersListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(userBlockedUser: UserBlockedUser, position: Int) {

            if (userBlockedUser.isChecked) {
                binding.title.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD)
            }
            else {
                binding.title.setTypeface(
                    null,
                    android.graphics.Typeface.NORMAL)
            }

            binding.title.text = userBlockedUser.blockedUser.displayName
            binding.checkBox.isChecked =  userBlockedUser.isChecked

            userBlockedUser.blockedUser.avatarUrl?.let {
                Glide.with(binding.icon.context)
                    .load(it)
                    .error(R.drawable.placeholder_user)
                    .transform(CenterCrop(), CircleCrop())
                    .into(binding.icon)
            } ?: run {
                Glide.with(binding.icon.context)
                    .load(R.drawable.placeholder_user)
                    .transform(CenterCrop(), CircleCrop())
                    .into(binding.icon)
            }

            binding.layoutB.setOnClickListener {
                onItemClick.onItemCheck(position)
                updateCheckUser()
            }
        }

        private fun updateCheckUser() {
            binding.checkBox.isChecked = !binding.checkBox.isChecked
            if ( binding.checkBox.isChecked) {
                binding.title.setTypeface(
                    null,
                    android.graphics.Typeface.BOLD)
            }
            else {
                binding.title.setTypeface(
                    null,
                    android.graphics.Typeface.NORMAL)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.new_item_blocker_user, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(blockUsersList[position],position)
    }

    override fun getItemCount(): Int {
        return blockUsersList.size
    }
}