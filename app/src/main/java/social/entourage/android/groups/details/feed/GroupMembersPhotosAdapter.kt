package social.entourage.android.groups.details.feed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import social.entourage.android.R
import social.entourage.android.api.model.GroupMember
import social.entourage.android.databinding.NewPhotoItemSmallBinding
import social.entourage.android.tools.utils.Const.LIMIT_PHOTOS_MEMBERS

class GroupMembersPhotosAdapter(
    var membersList: List<GroupMember>,
) : RecyclerView.Adapter<GroupMembersPhotosAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: NewPhotoItemSmallBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewPhotoItemSmallBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(membersList[position]) {
                this.avatarUrl?.let {
                    Glide.with(binding.image.context)
                        .load(it)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.image)
                } ?: kotlin.run {
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.image)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return membersList.size.coerceAtMost(LIMIT_PHOTOS_MEMBERS)
    }
}