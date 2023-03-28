package social.entourage.android.comment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
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
import social.entourage.android.tools.setHyperlinkClickable
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
    fun onCommentReport(commentId: Int?, isForEvent:Boolean, isMe:Boolean)
    fun onShowWeb(url:String)
}

class CommentsListAdapter(
    var context: Context,
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
                binding.comment_post.setHyperlinkClickable()
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

            if(binding.comment.text != null){
                binding.comment.addAutoLinkMode(
                    MODE_URL
                )
                binding.comment.onAutoLinkClick { item ->
                    onItemClick.onShowWeb(item.originalText)

                }
            }


            val isMe = comment.user?.userId == EntourageApplication.get().me()?.id

            if(comment.status == "deleted"){

                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_comment_deleted)
                val vectorDrawable = DrawableCompat.wrap(drawable!!) as VectorDrawable
                val width = 15
                val height = (width * vectorDrawable.intrinsicHeight) / vectorDrawable.intrinsicWidth
                val scaledDrawable = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(scaledDrawable)
                vectorDrawable.setBounds(8, 0, canvas.width - 8, canvas.height)
                vectorDrawable.draw(canvas)
                val grayDrawable = vectorDrawable.mutate()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    grayDrawable.setColorFilter(context.getColor(R.color.text_dark), PorterDuff.Mode.SRC_IN)
                }
                grayDrawable.setBounds(8, 0, scaledDrawable.width - 8, scaledDrawable.height)
                binding.comment.setCompoundDrawablesWithIntrinsicBounds(grayDrawable, null, null, null)
                binding.comment.compoundDrawablePadding = 16
                if(isOne2One){
                    binding.comment.text = context.getString(R.string.deleted_message)
                }else{
                    binding.comment.text = context.getString(R.string.deleted_comment)
                }
                binding.comment.background = context.getDrawable(R.drawable.new_comment_background_grey)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.comment.setTextColor(context.getColor(R.color.text_dark))
                }
            }else{
                binding.comment.text = comment.content
                if(isMe){
                    binding.comment.background = context.getDrawable(R.drawable.new_comment_background_orange)
                }else{
                    binding.comment.background = context.getDrawable(R.drawable.new_comment_background_beige)
                }
                binding.comment.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    binding.comment.setTextColor(context.getColor(R.color.black))
                }
            }
            binding.report.setOnClickListener {
                onItemClick.onCommentReport(comment.id, isForEvent, isMe)
            }
            //here
            if(isMe && comment.status != "deleted" ){
                binding.comment.setOnLongClickListener {
                    onItemClick.onCommentReport(comment.id, isForEvent, isMe)
                    return@setOnLongClickListener true
                }
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
                    if(comment.user != null){
                        (view.context as? Activity)?.startActivityForResult(
                            Intent(view.context, UserProfileActivity::class.java).putExtra(
                                Const.USER_ID,
                                comment.user?.userId
                            ), 0
                        )
                    }
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