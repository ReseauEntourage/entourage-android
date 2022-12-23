package social.entourage.android.new_v8.groups.details.feed

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.new_comment_item_date.view.*
import kotlinx.android.synthetic.main.new_comment_item_left.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.new_v8.models.Post
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.px
import java.text.SimpleDateFormat
import java.util.*

enum class CommentsTypes(val code: Int) {
    TYPE_LEFT(0),
    TYPE_RIGHT(1),
    TYPE_DATE(2),
}

interface OnItemClickListener {
    fun onItemClick(comment: Post)
    fun onCommentReport(commentId: Int?)
}

class CommentsListAdapter(
    var commentsList: List<Post>,
    var postAuthorId: Int,
    var isOne2One:Boolean,
    var isConversation:Boolean,
    var onItemClick: OnItemClickListener
) : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        fun bind(comment: Post) {

            if (comment.isDatePostOnly) {
                binding.publication_day.text = comment.datePostText
                return
            }

            binding.comment.text = comment.content
            binding.report.setOnClickListener {
                onItemClick.onCommentReport(comment.id)
            }
            comment.createdTime?.let {
                binding.information_layout.visibility = View.VISIBLE
                binding.error.visibility = View.GONE
                if (isConversation) {
                    binding.publication_date.text = SimpleDateFormat("HH'h'mm",
                        Locale.FRANCE
                    ).format(it)
                }
                else {
                    binding.publication_date.text = SimpleDateFormat(
                        binding.context.getString(R.string.comments_date),
                        Locale.FRANCE
                    ).format(it)
                }

            } ?: run {
                binding.information_layout.visibility = View.GONE
                binding.error.visibility = View.VISIBLE
                binding.error.setOnClickListener {
                    onItemClick.onItemClick(comment)
                }
            }

            val isMe = comment.user?.userId == EntourageApplication.get().me()?.id

            comment.user?.let {
                binding.author_name.text = if (isOne2One || isMe) "" else comment.user?.displayName
                comment.user?.avatarURLAsString?.let {
                    Glide.with(binding.context)
                        .load(it)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.context)
                        .load(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                }
            }

            if (isMe || isConversation) {
                binding.report.visibility = View.GONE
            }
            else {
                binding.report.visibility = View.VISIBLE
                binding.image.setOnClickListener { view->
                    (view.context as? Activity)?.startActivityForResult(
                        Intent(view.context, UserProfileActivity::class.java).putExtra(
                            Const.USER_ID,
                            comment.user?.userId
                        ), 0
                    )
                }
            }

            if (isConversation) {
                binding.author_name.setTextColor(binding.author_name.context.resources.getColor(R.color.light_orange))
                binding.publication_date.setTextColor(binding.publication_date.context.resources.getColor(R.color.light_orange))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            CommentsTypes.TYPE_LEFT.code -> R.layout.new_comment_item_left
            CommentsTypes.TYPE_RIGHT.code -> R.layout.new_comment_item_right
            else -> R.layout.new_comment_item_date
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

        if (commentsList[position].isDatePostOnly) return CommentsTypes.TYPE_DATE.code

        return if (commentsList[position].user?.id?.toInt() == postAuthorId) {
            CommentsTypes.TYPE_RIGHT.code
        } else CommentsTypes.TYPE_LEFT.code
    }
}