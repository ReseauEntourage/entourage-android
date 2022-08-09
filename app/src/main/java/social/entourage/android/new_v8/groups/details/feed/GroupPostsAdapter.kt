package social.entourage.android.new_v8.groups.details.feed

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.R
import social.entourage.android.databinding.NewLayoutPostBinding
import social.entourage.android.new_v8.models.Post
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px
import java.text.SimpleDateFormat
import java.util.*

class GroupPostsAdapter(
    var postsList: List<Post>,
    var groupId: Int,
    var isMember: Boolean,
    var groupName: String?
) : RecyclerView.Adapter<GroupPostsAdapter.ViewHolder>() {


    inner class ViewHolder(val binding: NewLayoutPostBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = NewLayoutPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder) {
            with(postsList[position]) {
                binding.postComment.setOnClickListener {
                    openCommentPage(holder.itemView.context, true, this.id, this.user?.id)
                }
                binding.postCommentsNumberLayout.setOnClickListener {
                    openCommentPage(holder.itemView.context, false, this.id, this.user?.id)
                }
                binding.name.text = user?.displayName
                content?.let {
                    binding.postMessage.text = it
                } ?: run {
                    binding.postMessage.visibility = View.GONE
                }
                if (hasComments == false) {
                    binding.postNoComments.visibility = View.VISIBLE
                    binding.postCommentsNumber.visibility = View.GONE
                } else {
                    binding.postCommentsNumber.visibility = View.VISIBLE
                    binding.postNoComments.visibility = View.GONE
                    binding.postCommentsNumber.text = String.format(
                        holder.itemView.context.getString(R.string.posts_comment_number),
                        commentsCount
                    )
                }
                binding.date.text = SimpleDateFormat(
                    itemView.context.getString(R.string.post_date),
                    Locale.FRANCE
                ).format(
                    createdTime
                )
                this.imageUrl?.let { avatarURL ->
                    Glide.with(holder.itemView.context)
                        .load(Uri.parse(avatarURL))
                        .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                        .placeholder(R.drawable.new_group_illu)
                        .into(binding.photoPost)
                } ?: run {
                    binding.photoPost.visibility = View.GONE
                }
                this.user?.avatarURLAsString?.let { avatarURL ->
                    Glide.with(holder.itemView.context)
                        .load(Uri.parse(avatarURL))
                        .placeholder(R.drawable.ic_user_photo_small)
                        .circleCrop()
                        .into(binding.image)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

    private fun openCommentPage(
        context: Context,
        shouldOpenKeyboard: Boolean,
        postId: Int?,
        userId: Long?
    ) {
        context.startActivity(
            Intent(context, CommentsActivity::class.java)
                .putExtra(
                    Const.GROUP_ID,
                    groupId
                )
                .putExtra(Const.POST_ID, postId)
                .putExtra(Const.POST_AUTHOR_ID, userId?.toInt())
                .putExtra(Const.SHOULD_OPEN_KEYBOARD, shouldOpenKeyboard)
                .putExtra(Const.IS_MEMBER, isMember)
                .putExtra(Const.GROUP_NAME, groupName)
        )
    }
}