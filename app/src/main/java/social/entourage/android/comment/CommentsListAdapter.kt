package social.entourage.android.groups.details.feed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.github.armcha.autolink.MODE_URL
import kotlinx.android.synthetic.main.new_comment_detail_post_top.view.*
import kotlinx.android.synthetic.main.new_comment_item_date.view.*
import kotlinx.android.synthetic.main.new_comment_item_left.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat
import java.util.*

enum class CommentsTypes(val code: Int) {
    TYPE_LEFT(0),
    TYPE_RIGHT(1),
    TYPE_DATE(2),
    TYPE_DETAIL(3)
}

interface OnItemClickListener {
    fun onItemClick(comment: Post)
    fun onCommentReport(commentId: Int?, isForEvent:Boolean)
    fun onShowWeb(url:String)
}

class CommentsListAdapter(
    var commentsList: List<Post>,
    var postAuthorId: Int,
    var isOne2One:Boolean,
    var isConversation:Boolean,
    var currentParentPost:Post?,
    var onItemClick: OnItemClickListener,
) : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    var isForEvent:Boolean = false

    fun setForEvent(){
        isForEvent = true
    }

    inner class ViewHolder(val binding: View) :
        RecyclerView.ViewHolder(binding) {
        @SuppressLint("SetTextI18n")
        fun bind(comment: Post, isDetailPost: Boolean) {

            if (isDetailPost) {
                //TODO: parse Detail post
                binding.comment_post.text = comment.content

                comment.createdTime?.let {
                    binding.publication_date_post.text = "le ${SimpleDateFormat("dd.MM.yyyy",
                        Locale.FRANCE
                    ).format(it)}"
                }

                comment.imageUrl?.let { avatarURL ->
                    binding.photo_post.visibility = View.VISIBLE
                    Glide.with( binding.photo_post.context)
                        .load(avatarURL)
                        .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                        .placeholder(R.drawable.new_group_illu)
                        .error(R.drawable.new_group_illu)
                        .into(binding.photo_post)
                } ?: run {
                    binding.photo_post.visibility = View.GONE
                }

                binding.author_name_post.text = comment.user?.displayName ?: "-"

                comment.user?.avatarURLAsString?.let { avatarURL ->
                    Glide.with(binding.image_post.context)
                        .load(avatarURL)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.image_post)
                } ?: run {
                    Glide.with(binding.image_post.context)
                        .load(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.image_post)
                }

                return
            }

            if (comment.isDatePostOnly) {
                binding.publication_day.text = comment.datePostText
                return
            }

            binding.comment.addAutoLinkMode(
                MODE_URL
            )

            binding.comment.onAutoLinkClick { item ->
                onItemClick.onShowWeb(item.originalText)
            }

            binding.comment.text = comment.content
            binding.report.setOnClickListener {
                onItemClick.onCommentReport(comment.id, isForEvent)
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
                    binding.publication_date.text = "le ${SimpleDateFormat(
                        binding.context.getString(R.string.comments_date),
                        Locale.FRANCE
                    ).format(it)}"
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

    fun updateDatas(currentParentPost: Post?) {
        this.currentParentPost = currentParentPost
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            CommentsTypes.TYPE_DETAIL.code -> R.layout.new_comment_detail_post_top
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
        if (position == 0 && hasCurrentPost()) {
            holder.bind(currentParentPost!!, true)
            return
        }
        val realPos = if(hasCurrentPost()) position - 1 else position
        holder.bind(commentsList[realPos], false)
    }

    private fun hasCurrentPost() : Boolean {
        return currentParentPost != null
    }

    override fun getItemCount(): Int {
        val addOne = if (hasCurrentPost()) 1 else 0
        return commentsList.size + addOne
    }

    override fun getItemViewType(position: Int): Int {

        if (position == 0 && hasCurrentPost()) return CommentsTypes.TYPE_DETAIL.code

        val realPos = if(hasCurrentPost()) position - 1 else position

        if (commentsList[realPos].isDatePostOnly) return CommentsTypes.TYPE_DATE.code

        return if (commentsList[realPos].user?.id?.toInt() == postAuthorId) {
            CommentsTypes.TYPE_RIGHT.code
        } else CommentsTypes.TYPE_LEFT.code
    }
}