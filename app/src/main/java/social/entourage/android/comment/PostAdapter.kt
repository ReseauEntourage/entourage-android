package social.entourage.android.comment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.databinding.NewLayoutPostBinding
import social.entourage.android.tools.setHyperlinkClickable
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class PostAdapter(
    var context:Context,
    var postsList: List<Post>,
    var onClick: (Post, Boolean) -> Unit,
    var onReport: (Int,Int) -> Unit,
    var onClickImage: (imageUrl:String, postId:Int) -> Unit,
    ) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {

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
                    onClick(postsList[position], true)
                }
                binding.postCommentsNumberLayout.setOnClickListener {
                    onClick(postsList[position], false)
                }
                binding.name.text = user?.displayName
                content?.let {
                    binding.postMessage.visibility = View.VISIBLE
                    binding.postMessage.text = it
                    binding.postMessage.setHyperlinkClickable()

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
                    binding.photoPost.visibility = View.VISIBLE
                    Glide.with(holder.itemView.context)
                        .load(avatarURL)
                        .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                        .placeholder(R.drawable.new_group_illu)
                        .error(R.drawable.new_group_illu)
                        .into(binding.photoPost)
                    binding.photoPost.setOnClickListener {
                        this.id?.let { it1 -> onClickImage(imageUrl, it1) }
                    }
                } ?: run {
                    binding.photoPost.visibility = View.GONE
                }


                this.user?.avatarURLAsString?.let { avatarURL ->
                    Glide.with(holder.itemView.context)
                        .load(avatarURL)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.image)
                } ?: run {
                    Glide.with(holder.itemView.context)
                        .load(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(binding.image)
                }

                binding.tvAmbassador.visibility = View.VISIBLE
                var tagsString = ""
                if (this.user?.isAdmin() == true){
                    tagsString = tagsString + context.getString(R.string.admin) + " •"
                }else if(this.user?.isAmbassador() == true){
                    tagsString = tagsString + context.getString(R.string.ambassador) + " •"
                }else if(this.user?.partner != null ){
                    tagsString = tagsString + this.user?.partner!!.name

                }
                if(tagsString.isEmpty()){
                    binding.tvAmbassador.visibility = View.GONE
                }else{
                    binding.tvAmbassador.visibility = View.VISIBLE
                    if(tagsString.last().toString() == "•"){
                        tagsString = tagsString.removeSuffix("•")
                    }
                    binding.tvAmbassador.text = tagsString
                }

                binding.name.setOnClickListener {
                    showUserDetail(binding.name.context,this.user?.userId)
                }

                binding.image.setOnClickListener {
                    showUserDetail(binding.image.context,this.user?.userId)
                }

                binding.btnReportPost.setOnClickListener {
                    val userId = postsList[position].user?.id?.toInt()
                    if (userId != null){
                        postsList[position].id?.let { it1 -> onReport(it1,userId) }
                    }
                }
                if(status == "deleted"){
                    binding.postMessage.text = context.getText(R.string.deleted)
                    binding.postMessage.setTextColor(context.getColor(R.color.new_light_grey))
                    binding.postMessage.visibility = View.VISIBLE
                    binding.postComment.visibility = View.GONE
                    binding.btnReportPost.visibility = View.GONE
                }else{
                    binding.postMessage.text = content
                    binding.postMessage.setTextColor(context.getColor(R.color.black))
                    binding.postMessage.visibility = View.VISIBLE
                    binding.postComment.visibility = View.VISIBLE
                    binding.btnReportPost.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showUserDetail(context:Context,userId:Int?) {
        (context as? Activity)?.startActivityForResult(
            Intent(context, UserProfileActivity::class.java).putExtra(
                Const.USER_ID,
                userId
            ), 0
        )
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
}