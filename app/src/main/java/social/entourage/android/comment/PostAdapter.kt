package social.entourage.android.comment

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.notification.Reaction
import social.entourage.android.api.model.notification.ReactionType
import social.entourage.android.databinding.NewLayoutPostBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.setHyperlinkClickable
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat
import java.util.*

interface ReactionInterface{
    fun onReactionClicked(postId: Post, reactionId: Int)
    fun seeMemberReaction(post: Post)
    fun deleteReaction(post: Post)
}
class PostAdapter(
    var context:Context,
    var reactionCallback: ReactionInterface,
    var postsList: List<Post>,
    var onClick: (Post, Boolean) -> Unit,
    var onReport: (Int,Int) -> Unit,
    var onClickImage: (imageUrl:String, postId:Int) -> Unit,
    ) : RecyclerView.Adapter<PostAdapter.ViewHolder>() {
    private val translationExceptions = mutableSetOf<Int>()

    fun initiateList(){
        val translatedByDefault = context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE
        ).getBoolean("translatedByDefault", false)
        if (translatedByDefault) {
            postsList.forEach {
                if (it.contentTranslations != null) {
                    translationExceptions.add(it.id!!)
                }
            }
        }
        notifyDataSetChanged()
    }

    fun translateItem(postId: Int) {
        if (translationExceptions.contains(postId)) {
            translationExceptions.remove(postId)
        } else {
            translationExceptions.add(postId)
        }
        notifyItemChanged(postsList.indexOfFirst { it.id == postId })
    }
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
            val meId = EntourageApplication.get().me()?.id
            var isPouceOrange = postsList[position].postId ?: 0
            if(isPouceOrange != 0){
                binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_orange))
            }
            binding.tvTitleILike.setText(context.getText(R.string.text_title_i_like))
            binding.tvIComment.setText(context.getText(R.string.text_title_comment))

            binding.layoutPostParent.setOnClickListener {
                binding.layoutReactions.visibility = View.GONE
            }


            // Ajouter un listener pour l'appui long sur le bouton "j'aime"
            binding.btnILike.setOnLongClickListener {
                binding.layoutReactions.visibility = if (binding.layoutReactions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                true
            }

            with(postsList[position]) {

                if (this.reactionId != 0) {
                    // Si le pouce est orange, le changer en gris
                    binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_orange))
                } else {
                    // Si le pouce est gris, le changer en orange
                    binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_grey))
                }

                binding.btnILike.setOnClickListener {
                    val firstReactionType = MainActivity.reactionsList?.firstOrNull()
                    if(firstReactionType != null){
                        binding.layoutReactions.visibility =  View.GONE
                        handleReactionClick(this, firstReactionType)
                    }
                }
                val reactionImageViews = listOf(
                    binding.ivReactOne,
                    binding.ivReactTwo,
                    binding.ivReactThree,
                    binding.ivReactFour,
                    binding.ivReactFive
                )
                val reactionTypes = MainActivity.reactionsList
                reactionTypes?.let { types ->
                    types.take(5).forEachIndexed { index, reactionType ->
                        Glide.with(context).load(reactionType.imageUrl).into(reactionImageViews[index])
                        reactionImageViews[index].setOnClickListener {
                            handleReactionClick(this, reactionType)
                            binding.layoutReactions.visibility =  View.GONE
                        }
                    }
                }

                val reactionViews = listOf(
                    binding.reaction1,
                    binding.reaction2,
                    binding.reaction3,
                    binding.reaction4,
                    binding.reaction5
                )
                val reactionsLayouts = arrayOf(
                    binding.reaction1.layoutItemReactionParent,
                    binding.reaction2.layoutItemReactionParent,
                    binding.reaction3.layoutItemReactionParent,
                    binding.reaction4.layoutItemReactionParent,
                    binding.reaction5.layoutItemReactionParent
                )

                // Cache tous les layouts de réaction
                reactionsLayouts.forEach { it.visibility = View.GONE }

                // Afficher les réactions disponibles pour ce post
                reactions?.forEachIndexed { index, reaction ->
                    if (index < reactionsLayouts.size) {
                        val reactionType = reactionTypes?.find { it.id == reaction.reactionId }
                        reactionType?.let {
                            Glide.with(context)
                                .load(it.imageUrl)
                                .into(reactionViews[index].image)
                            reactionsLayouts[index].visibility = View.VISIBLE
                        }
                    }
                }



                // Cache toutes les vues de réaction par défaut
                reactionViews.forEach { it.layoutItemReactionParent.visibility = View.GONE }
                // Calcule le nombre total de réactions pour ce post
                val totalReactionsCount = reactions?.sumOf { it.reactionsCount } ?: 0
                binding.numberReaction.text = totalReactionsCount.toString()
                if (totalReactionsCount > 0) {
                    binding.numberReaction.visibility = View.VISIBLE
                } else {
                    binding.numberReaction.visibility = View.GONE
                }

                // Affiche les réactions en fonction des ReactionType disponibles
                reactions?.forEachIndexed { index, reaction ->
                    if (index < reactionViews.size) {
                        // Récupère le ReactionType correspondant à l'ID de la réaction
                        val reactionType = MainActivity.reactionsList?.find { it.id == reaction.reactionId }
                        reactionType?.let {
                            Glide.with(context)
                                .load(it.imageUrl)
                                .into(reactionViews[index].image) // Assurez-vous que votre layout_item_reaction a un ImageView avec un id `image`
                            reactionViews[index].layoutItemReactionParent.visibility = View.VISIBLE
                        }
                    }
                }
                binding.btnIComment.setOnClickListener {
                    onClick(this, false)
                }

                binding.postCommentsNumberLayout.setOnClickListener {
                    reactionCallback.seeMemberReaction(this)
                }

                binding.postMessage.setOnLongClickListener {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Clic_CopyPaste_LongClic)
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText(context.getString(R.string.copied_text), binding.postMessage.text)
                    clipboard.setPrimaryClip(clip)
                    // Afficher un petit message de confirmation, si vous voulez
                    Toast.makeText(context, context.getString(R.string.copied_text), Toast.LENGTH_SHORT).show()
                    true // Retourne true pour indiquer que l'événement a été géré
                }
                if(DataLanguageStock.userLanguage == this.contentTranslations?.fromLang || (this.user?.id == meId?.toLong()) && (this.content == "")){
                    binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
                }else{
                    binding.postTranslationButton.layoutCsTranslate.visibility = View.VISIBLE
                }
                if(this.contentTranslations == null ){
                    binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
                }
                // Déterminer si ce post spécifique doit être traduit ou non
                val isTranslated = !translationExceptions.contains(id)
                // Configurer le bouton de traduction
                val text = if (isTranslated) {
                    context.getString(R.string.layout_translate_title_translation)
                } else {
                    context.getString(R.string.layout_translate_title_original)
                }
                val titleButton = SpannableString(text)
                titleButton.setSpan(UnderlineSpan(), 0, text.length, 0)
                binding.postTranslationButton.tvTranslate.text = titleButton
                binding.postTranslationButton.layoutCsTranslate.setOnClickListener {
                    translateItem(id ?: this.id!!)
                }
                binding.imageViewComments.setOnClickListener {
                    onClick(this, false)
                }
                binding.postNoComments.setOnClickListener {
                    onClick(this, false)
                }
                binding.postCommentsNumber.setOnClickListener {
                    onClick(this, false)
                }

                // Configurer le contenu du post en fonction de la traduction
                var contentToShow = content
                if (contentTranslations != null) {
                    contentToShow = if (isTranslated) contentTranslations.translation else contentTranslations.original
                }
                binding.postMessage.text = contentToShow
                content?.let {
                    binding.postMessage.visibility = View.VISIBLE
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
                    if(commentsCount != null && commentsCount!! > 1){
                        binding.postCommentsNumber.text = String.format(
                            holder.itemView.context.getString(R.string.posts_comment_number),
                            commentsCount
                        )
                    }else{
                        binding.postCommentsNumber.text = String.format(
                            holder.itemView.context.getString(R.string.posts_comment_number_singular),
                            commentsCount
                        )
                    }
                }
                var locale = LanguageManager.getLocaleFromPreferences(context)
                binding.date.text = SimpleDateFormat(
                    itemView.context.getString(R.string.post_date),
                    locale
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
                binding.name.text = user?.displayName
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
                    binding.postMessage.text = context.getText(R.string.deleted_publi)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.postMessage.setTextColor(context.getColor(R.color.deleted_grey))
                    }
                    binding.postMessage.visibility = View.VISIBLE
                    /*binding.postComment.visibility = View.GONE*/
                    binding.btnReportPost.visibility = View.GONE
                }else{

                    val isTranslated = !translationExceptions.contains(id)
                    var contentToShow = content
                    if(contentTranslations != null){
                        if(isTranslated){
                            contentToShow = contentTranslations.original
                        }else{
                            contentToShow = contentTranslations.translation
                        }
                    }
                    binding.postMessage.text = contentToShow
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        binding.postMessage.setTextColor(context.getColor(R.color.black))
                    }
                    binding.postMessage.visibility = View.VISIBLE
                    binding.btnReportPost.visibility = View.VISIBLE
                }
            }
        }
    }
    private fun handleReactionClick(post: Post, reactionType: ReactionType) {
        val currentUserReactionId = post.reactionId
        val isSameReaction = currentUserReactionId == reactionType.id

        if (currentUserReactionId != 0) {
            val existingReaction = post.reactions?.find { it.reactionId == currentUserReactionId }

            if (isSameReaction) {
                // Supprimer la réaction actuelle
                existingReaction?.let {
                    it.reactionsCount--
                    if (it.reactionsCount <= 0) {
                        post.reactions?.remove(it)
                    }
                    post.reactionId = 0
                    reactionCallback.deleteReaction(post)
                    notifyItemChanged(postsList.indexOf(post))
                }
            } else {
                // Remplacer la réaction
                existingReaction?.let {
                    it.reactionsCount--
                    if (it.reactionsCount <= 0) {
                        post.reactions?.remove(it)
                    }
                }
                post.reactionId = reactionType.id
                reactionCallback.deleteReaction(post)

                // Ajouter un délai avant d'ajouter la nouvelle réaction
                Handler(Looper.getMainLooper()).postDelayed({
                    addOrUpdateReaction(post, reactionType)
                    reactionCallback.onReactionClicked(post, reactionType.id)
                    notifyItemChanged(postsList.indexOf(post))
                }, 500) // Délai de 500 millisecondes
            }
        } else {
            // Première réaction de l'utilisateur
            addOrUpdateReaction(post, reactionType)
            post.reactionId = reactionType.id
            reactionCallback.onReactionClicked(post, reactionType.id)
        }

        if (!isSameReaction) {
            notifyItemChanged(postsList.indexOf(post))
        }
    }


    private fun addOrUpdateReaction(post: Post, reactionType: ReactionType) {
        val existingReaction = post.reactions?.find { it.reactionId == reactionType.id }
        if (existingReaction != null) {
            // Mettre à jour la réaction existante
            existingReaction.reactionsCount++
        } else {
            // Ajouter une nouvelle réaction
            val newReaction = Reaction().apply {
                reactionId = reactionType.id
                reactionsCount = 1
            }
            post.reactions?.add(newReaction)
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