package social.entourage.android.new_v8.groups.details.feed

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.new_comment_item_left.view.*
import social.entourage.android.R
import social.entourage.android.new_v8.models.Post
import java.text.SimpleDateFormat
import java.util.*

enum class CommentsTypes(val code: Int) {
    TYPE_LEFT(0),
    TYPE_RIGHT(1),
}

class CommentsListAdapter(
    var commentsList: List<Post>,
    var postAuthorId: Int
) : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(comment: Post) {
            binding.comment.text = comment.content
            binding.author_name.text = comment.user.displayName
            binding.publication_date.text =
                SimpleDateFormat(
                    binding.context.getString(R.string.comments_date),
                    Locale.FRANCE
                ).format(comment.createdTime)
            Glide.with(binding.context)
                .load(Uri.parse(comment.user.avatarURLAsString))
                .placeholder(R.drawable.ic_user_photo_small)
                .circleCrop()
                .into(binding.image)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            CommentsTypes.TYPE_LEFT.code -> R.layout.new_comment_item_left
            else -> R.layout.new_comment_item_right
        }
        val view = LayoutInflater
            .from(parent.context)
            .inflate(layout, parent, false)

        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(commentsList[position])
    }

    override fun getItemCount(): Int {
        return commentsList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (commentsList[position].user.id.toInt() == postAuthorId) {
            CommentsTypes.TYPE_RIGHT.code
        } else CommentsTypes.TYPE_LEFT.code
    }
}