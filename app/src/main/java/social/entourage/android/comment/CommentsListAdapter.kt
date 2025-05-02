package social.entourage.android.comment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.VectorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import io.github.armcha.autolink.MODE_URL
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.databinding.LayoutCommentDetailPostTopBinding
import social.entourage.android.databinding.LayoutCommentItemDateBinding
import social.entourage.android.databinding.LayoutCommentItemLeftBinding
import social.entourage.android.databinding.LayoutCommentItemRightBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.tools.setHyperlinkClickable
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import social.entourage.android.user.UserProfileActivity
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
    fun onCommentReport(commentId: Int?, isForEvent:Boolean, isMe:Boolean, commentLang:String)
    fun onShowWeb(url:String)
}

class CommentsListAdapter(
    var context: Context,
    private var commentsList: List<Post>,
    private var postAuthorId: Int,
    var isOne2One:Boolean,
    var isConversation:Boolean,
    private var currentParentPost:Post?,
    var onItemClick: OnItemClickListener,
) : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    var isForEvent:Boolean = false

    fun initiateList(){
        val translatedByDefault = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ).getBoolean("translatedByDefault", true)
        if (translatedByDefault) {
            commentsList.forEach {
                if (it.contentTranslations != null) {
                    translationExceptions.add(it.id!!)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun setForEvent() {
        isForEvent = true
    }
    private val translationExceptions = mutableSetOf<Int>()

    fun translateItem(commentId: Int) {
        if (translationExceptions.contains(commentId)) {
            translationExceptions.remove(commentId)
        } else {
            translationExceptions.add(commentId)
        }
        //Log.wtf("wtf","translationExceptions : $translationExceptions")
        notifyItemChanged(commentsList.indexOfFirst { it.id == commentId } + 1)
    }

    inner class ViewHolder(val binding: ViewBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bindDate(comment: Post) {
            (binding as LayoutCommentItemDateBinding).publicationDay.text = comment.datePostText
        }
        fun bindLeft(comment: Post) {
            val binding = binding as LayoutCommentItemLeftBinding

            if(binding.comment.text != null){
                binding.comment.addAutoLinkMode(MODE_URL)
                binding.comment.onAutoLinkClick { item ->
                    onItemClick.onShowWeb(item.originalText)
                }
            }

            val isMe = comment.user?.userId == EntourageApplication.get().me()?.id

            if(comment.status == "deleted"){
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_comment_deleted)
                val vectorDrawable = DrawableCompat.wrap(drawable!!) as VectorDrawable
                val width = 12
                val height = (width * vectorDrawable.intrinsicHeight) / vectorDrawable.intrinsicWidth
                val scaledDrawable = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(scaledDrawable)
                vectorDrawable.setBounds(8, 0, canvas.width - 8, canvas.height)
                vectorDrawable.draw(canvas)
                val grayDrawable = vectorDrawable.mutate()
                grayDrawable.setColorFilter(context.getColor(R.color.grey_deleted_icon), PorterDuff.Mode.SRC_IN)
                grayDrawable.setBounds(8, 0, scaledDrawable.width - 8, scaledDrawable.height)
                binding.comment.setCompoundDrawablesWithIntrinsicBounds(grayDrawable, null, null, null)
                binding.comment.compoundDrawablePadding = 16
                if(isOne2One){
                    binding.comment.text = context.getString(R.string.deleted_message)
                }else{
                    binding.comment.text = context.getString(R.string.deleted_comment)
                }
                binding.comment.background = context.getDrawable(R.drawable.new_comment_background_grey)
                binding.comment.setTextColor(context.getColor(R.color.grey_deleted_icon))
            } else {
                var contentToShow = comment.content
                val sharedPrefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
                val isTranslated = if (translationExceptions.contains(comment.id)) {
                    !isTranslatedByDefault
                } else {
                    isTranslatedByDefault
                }
                if(comment.contentTranslations != null){
                    contentToShow = if (isTranslated) {
                        comment.contentTranslations.translation ?: comment.content
                    } else {
                        comment.contentTranslations.original ?: comment.content
                    }
                }

                binding.comment.text = contentToShow

                if(isMe){
                    binding.comment.background = context.getDrawable(R.drawable.new_comment_background_orange)
                }else{
                    binding.comment.background = context.getDrawable(R.drawable.new_comment_background_beige)
                }
                binding.comment.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                binding.comment.setTextColor(context.getColor(R.color.black))
            }
            binding.report.setOnClickListener {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                DataLanguageStock.updateContentToCopy(comment.content ?: "")
                onItemClick.onCommentReport(comment.id, isForEvent, isMe,commentLang)
            }
            //here
            if(comment.status != "deleted" ){
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                binding.comment.setOnLongClickListener {
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent, isMe,commentLang)
                    return@setOnLongClickListener true
                }
            }

            comment.createdTime?.let {
                binding.informationLayout.visibility = View.VISIBLE
                binding.error.visibility = View.GONE
                val locale = LanguageManager.getLocaleFromPreferences(context)
                if (isConversation) {
                    binding.publicationDate.text = SimpleDateFormat("HH'h'mm",
                        locale
                    ).format(it)
                }
                else {
                    binding.publicationDate.text = "le ${SimpleDateFormat(
                        binding.root.context.getString(R.string.comments_date),
                        locale
                    ).format(it)}"
                }

            } ?: run {
                binding.informationLayout.visibility = View.GONE
                binding.error.visibility = View.VISIBLE
                binding.error.setOnClickListener {
                    onItemClick.onItemClick(comment)
                }
            }

            comment.user?.let {
                binding.authorName.text = if (isOne2One || isMe) "" else comment.user?.displayName
                comment.user?.avatarURLAsString?.let {
                    Glide.with(binding.root.context)
                        .load(it)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.root.context)
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
                binding.authorName.setTextColor(binding.authorName.context.resources.getColor(R.color.light_orange))
                binding.publicationDate.setTextColor(binding.publicationDate.context.resources.getColor(R.color.light_orange))
            }
        }
        fun bindRight(comment: Post) {
            val binding = binding as LayoutCommentItemRightBinding

            if(binding.comment.text != null){
                binding.comment.addAutoLinkMode(MODE_URL)
                binding.comment.onAutoLinkClick { item ->
                    onItemClick.onShowWeb(item.originalText)
                }
            }

            val isMe = comment.user?.userId == EntourageApplication.get().me()?.id

            if(comment.status == "deleted"){

                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_comment_deleted)
                val vectorDrawable = DrawableCompat.wrap(drawable!!) as VectorDrawable
                val width = 12
                val height = (width * vectorDrawable.intrinsicHeight) / vectorDrawable.intrinsicWidth
                val scaledDrawable = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(scaledDrawable)
                vectorDrawable.setBounds(8, 0, canvas.width - 8, canvas.height)
                vectorDrawable.draw(canvas)
                val grayDrawable = vectorDrawable.mutate()
                grayDrawable.setColorFilter(context.getColor(R.color.grey_deleted_icon), PorterDuff.Mode.SRC_IN)
                grayDrawable.setBounds(8, 0, scaledDrawable.width - 8, scaledDrawable.height)
                binding.comment.setCompoundDrawablesWithIntrinsicBounds(grayDrawable, null, null, null)
                binding.comment.compoundDrawablePadding = 16
                if(isOne2One){
                    binding.comment.text = context.getString(R.string.deleted_message)
                }else{
                    binding.comment.text = context.getString(R.string.deleted_comment)
                }
                binding.comment.background = context.getDrawable(R.drawable.new_comment_background_grey)
                binding.comment.setTextColor(context.getColor(R.color.grey_deleted_icon))
            } else {
                var contentToShow = comment.content
                val sharedPrefs = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
                val isTranslated = if (translationExceptions.contains(comment.id)) {
                    !isTranslatedByDefault
                } else {
                    isTranslatedByDefault
                }
                if(comment.contentTranslations != null){
                    contentToShow = if (isTranslated) {
                        comment.contentTranslations.translation ?: comment.content
                    } else {
                        comment.contentTranslations.original ?: comment.content
                    }
                }

                binding.comment.text = contentToShow

                if(isMe){
                    binding.comment.background = context.getDrawable(R.drawable.new_comment_background_orange)
                }else{
                    binding.comment.background = context.getDrawable(R.drawable.new_comment_background_beige)
                }
                binding.comment.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                binding.comment.setTextColor(context.getColor(R.color.black))
            }
            binding.report.setOnClickListener {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                DataLanguageStock.updateContentToCopy(comment.content ?: "")
                onItemClick.onCommentReport(comment.id, isForEvent, isMe,commentLang)
            }
            //here
            if(comment.status != "deleted" ){
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                binding.comment.setOnLongClickListener {
                    DataLanguageStock.updateContentToCopy(comment.content ?: "")
                    onItemClick.onCommentReport(comment.id, isForEvent, isMe,commentLang)
                    return@setOnLongClickListener true
                }
            }

            comment.createdTime?.let {
                binding.informationLayout.visibility = View.VISIBLE
                binding.error.visibility = View.GONE
                if (isConversation) {
                    var locale = LanguageManager.getLocaleFromPreferences(context)
                    binding.publicationDate.text = SimpleDateFormat("HH'h'mm",
                        locale
                    ).format(it)
                }
                else {
                    var locale = LanguageManager.getLocaleFromPreferences(context)
                    binding.publicationDate.text = "le ${SimpleDateFormat(
                        binding.root.context.getString(R.string.comments_date),
                        locale
                    ).format(it)}"
                }

            } ?: run {
                binding.informationLayout.visibility = View.GONE
                binding.error.visibility = View.VISIBLE
                binding.error.setOnClickListener {
                    onItemClick.onItemClick(comment)
                }
            }

            comment.user?.let {
                binding.authorName.text = if (isOne2One || isMe) "" else comment.user?.displayName
                comment.user?.avatarURLAsString?.let {
                    Glide.with(binding.root.context)
                        .load(it)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .apply(RequestOptions().override(25.px, 25.px))
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(binding.root.context)
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
                binding.authorName.setTextColor(binding.authorName.context.resources.getColor(R.color.light_orange))
                binding.publicationDate.setTextColor(binding.publicationDate.context.resources.getColor(R.color.light_orange))
            }
        }
        fun bindDetails(comment: Post) {
            val binding = binding as LayoutCommentDetailPostTopBinding
            //TODO: parse Detail post
            binding.commentPost.text = comment.content
            if(comment.status.equals("deleted")){
                binding.commentPost.text = context.getString(R.string.deleted_publi)
            }
            binding.commentPost.setHyperlinkClickable()
            comment.createdTime?.let {
                val locale = LanguageManager.getLocaleFromPreferences(context)
                binding.publicationDatePost.text = "le ${SimpleDateFormat("dd.MM.yyyy",
                    locale
                ).format(it)}"
            }

            comment.imageUrl?.let { avatarURL ->
                binding.photoPost.visibility = View.VISIBLE
                Glide.with( binding.photoPost.context)
                    .load(avatarURL)
                    .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                    .placeholder(R.drawable.new_group_illu)
                    .error(R.drawable.new_group_illu)
                    .into(binding.photoPost)
            } ?: run {
                binding.photoPost.visibility = View.GONE
            }

            binding.authorNamePost.text = comment.user?.displayName ?: "-"

            comment.user?.avatarURLAsString?.let { avatarURL ->
                Glide.with(binding.imagePost.context)
                    .load(avatarURL)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.imagePost)
            } ?: run {
                Glide.with(binding.imagePost.context)
                    .load(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(binding.imagePost)
            }
        }
    }

    fun updateData(currentParentPost: Post?) {
        this.currentParentPost = currentParentPost
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = when (viewType) {
            CommentsTypes.TYPE_DETAIL.code -> LayoutCommentDetailPostTopBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            CommentsTypes.TYPE_LEFT.code -> LayoutCommentItemLeftBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )

            CommentsTypes.TYPE_RIGHT.code -> LayoutCommentItemRightBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ), parent, false
            )

            else -> LayoutCommentItemDateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        }

        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = if (position == 0 && hasCurrentPost()) currentParentPost 
            else commentsList[if(hasCurrentPost()) (position - 1) else position]
        if(comment == null) return

        when(getItemViewType(position)) {
            CommentsTypes.TYPE_DETAIL.code -> holder.bindDetails(comment)
            CommentsTypes.TYPE_LEFT.code -> holder.bindLeft(comment)
            CommentsTypes.TYPE_RIGHT.code -> holder.bindRight(comment)
            CommentsTypes.TYPE_DATE.code -> holder.bindDate(comment)
        }
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
        } else {
            CommentsTypes.TYPE_LEFT.code
        }
    }
}