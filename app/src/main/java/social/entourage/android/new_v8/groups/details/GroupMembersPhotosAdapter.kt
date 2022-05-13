package social.entourage.android.new_v8.groups.details

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import social.entourage.android.databinding.NewPhotoItemBinding
import social.entourage.android.new_v8.groups.list.groupPerPage
import social.entourage.android.new_v8.models.GroupMember
import social.entourage.android.new_v8.utils.Const.LIMIT_PHOTOS_MEMBERS
import social.entourage.android.new_v8.utils.px

class GroupMembersPhotosAdapter(
    var membersList: List<GroupMember>,
) : RecyclerView.Adapter<GroupMembersPhotosAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewPhotoItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewPhotoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(membersList[position]) {
                Glide.with(binding.image.context)
                    .load(Uri.parse(this.avatarUrl))
                    .apply(RequestOptions().override(25.px, 25.px))
                    .circleCrop()
                    .into(binding.image)
            }
        }
    }

    override fun getItemCount(): Int {
        return membersList.size.coerceAtMost(LIMIT_PHOTOS_MEMBERS)
    }
}