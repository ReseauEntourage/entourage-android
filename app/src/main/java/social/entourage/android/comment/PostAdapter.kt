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
import android.text.Spanned
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
import kotlinx.android.synthetic.main.layout_question_survey.view.ui_iv_check
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.notification.Reaction
import social.entourage.android.api.model.notification.ReactionType
import social.entourage.android.databinding.NewLayoutPostBinding
import social.entourage.android.databinding.SurveyLayoutBinding
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
    var isMember: Boolean? = false,
    var onClick: (Post, Boolean) -> Unit,
    var onReport: (Int,Int) -> Unit,
    var onClickImage: (imageUrl:String, postId:Int) -> Unit,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val translationExceptions = mutableSetOf<Int>()

    companion object {
        const val TYPE_POST = 0
        const val TYPE_SURVEY = 1
    }


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

    override fun getItemViewType(position: Int): Int {
        val post = postsList[position]
        Log.wtf("wtf", "eho passed survey attribute " + Gson().toJson(post.survey))
        return if (post.survey != null) TYPE_SURVEY else TYPE_POST
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

    inner class SurveyViewHolder(val binding: SurveyLayoutBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SURVEY -> {
                val binding = SurveyLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SurveyViewHolder(binding)
            }
            else -> {
                val binding = NewLayoutPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ViewHolder(binding)
            }
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        with(holder) {
            when (getItemViewType(position)) {
                TYPE_SURVEY -> {
                    val surveyHolder = holder as SurveyViewHolder
                    val post = postsList[position]
                    val user = post.user

                    // Peuple les informations de l'auteur du sondage
                    surveyHolder.binding.name.text = user?.displayName ?: "Nom inconnu"
                    user?.avatarURLAsString?.let { avatarUrl ->
                        Glide.with(context)
                            .load(avatarUrl)
                            .placeholder(R.drawable.placeholder_user) // Mettez une placeholder par défaut
                            .error(R.drawable.placeholder_user) // Image à afficher en cas d'erreur
                            .circleCrop() // Pour arrondir l'image
                            .into(surveyHolder.binding.image)
                    } ?: run {
                        // Dans le cas où il n'y a pas d'URL pour l'avatar, utiliser une image par défaut
                        Glide.with(context)
                            .load(R.drawable.placeholder_user)
                            .circleCrop()
                            .into(surveyHolder.binding.image)
                    }



                    // Peuple la question du sondage si disponible
                    surveyHolder.binding.surveyQuestion.text = post.content ?: "Question non disponible"

                    // Initialisation des choix de sondage
                    val survey = post.survey
                    val totalResponses = survey?.summary?.sum() ?: 0 // Calcul du total des réponses pour calculer les pourcentages
                    survey?.choices?.forEachIndexed { index, choice ->
                        val choiceBinding = when (index) {
                            0 -> surveyHolder.binding.choiceOne
                            1 -> surveyHolder.binding.choiceTwo
                            2 -> surveyHolder.binding.choiceThree
                            3 -> surveyHolder.binding.choiceFour
                            4 -> surveyHolder.binding.choiceFive
                            else -> null
                        }

                        choiceBinding?.let { binding ->
                            binding.parent.visibility = View.VISIBLE
                            binding.tvQuestionSurvey.text = choice
                            // Met à jour la ProgressBar et le nombre de réponses
                            val summary = survey.summary.getOrNull(index) ?: 0
                            val progress = if (totalResponses > 0) (summary * 100 / totalResponses) else 0
                            binding.progressBar3.progress = progress
                            binding.tvNumberAnswer.text = "$summary"

                            // Initialiser avec l'icône non sélectionnée ou en fonction de l'état actuel
                            binding.uiIvCheck.setImageResource(R.drawable.new_bg_unselected_filter)
                            var isSelected = false // Cet état doit être récupéré ou calculé en fonction de tes données

                            // Gestion du clic sur l'icône de vérification
                            binding.parent.setOnClickListener {
                                // Si l'option est actuellement non sélectionnée et va être sélectionnée
                                val previouslySelected = isSelected
                                isSelected = !isSelected
                                val newSummary = if (isSelected) summary + 1 else summary - 1 // Corrige la logique d'ajustement
                                val newTotalResponses = if (previouslySelected) totalResponses - 1 else totalResponses + 1 // Assure que le total des réponses est correctement ajusté

                                // Met à jour l'affichage
                                binding.tvNumberAnswer.text = "$newSummary"
                                val newProgress = if (newTotalResponses > 0) (newSummary * 100 / newTotalResponses) else 0
                                binding.progressBar3.progress = newProgress

                                if (survey.multiple) {
                                    val iconRes = if (isSelected) R.drawable.new_bg_selected_filter else R.drawable.new_bg_unselected_filter
                                    binding.uiIvCheck.setImageResource(iconRes)
                                } else {
                                    // Désélectionne toutes les autres options pour les sondages à choix unique
                                    listOf(surveyHolder.binding.choiceOne, surveyHolder.binding.choiceTwo, surveyHolder.binding.choiceThree, surveyHolder.binding.choiceFour, surveyHolder.binding.choiceFive).forEach {
                                        it.uiIvCheck.setImageResource(R.drawable.new_bg_unselected_filter)
                                    }
                                    // Sélectionne cette option si elle vient d'être sélectionnée
                                    if (isSelected) {
                                        binding.uiIvCheck.setImageResource(R.drawable.new_bg_selected_filter)
                                    }
                                }
                            }

                        }
                    }

                    // Assure-toi de cacher les choix excédentaires si le sondage en contient moins que le nombre de choix préparés
                    if (survey?.choices?.size ?: 0 < 5) {
                        listOf(surveyHolder.binding.choiceOne, surveyHolder.binding.choiceTwo, surveyHolder.binding.choiceThree, surveyHolder.binding.choiceFour, surveyHolder.binding.choiceFive).forEachIndexed { index, choiceBinding ->
                            if (index >= (survey?.choices?.size ?: 0)) {
                                choiceBinding.parent.visibility = View.GONE
                            }
                        }
                    }
                }

                TYPE_POST -> {
                    val binding = (holder as ViewHolder).binding
                    val meId = EntourageApplication.get().me()?.id
                    binding.tvTitleILike.setText(context.getText(R.string.text_title_i_like))
                    binding.tvIComment.setText(context.getText(R.string.text_title_comment))

                    binding.layoutPostParent.setOnClickListener {
                        binding.layoutReactions.visibility = View.GONE
                    }


                    // Ajouter un listener pour l'appui long sur le bouton "j'aime"
                    binding.btnILike.setOnLongClickListener {
                        AnalyticsEvents.logEvent(
                            AnalyticsEvents.Clic_Post_List_Reactions
                        )
                        binding.layoutReactions.visibility = if (binding.layoutReactions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        true
                    }

                    with(postsList[position]) {
                        if(this.reactionId == null ){
                            binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_grey))
                            binding.tvTitleILike.setTextColor(context.getColor(R.color.black))
                        }else if (this.reactionId != 0 ) {
                            // Si le pouce est orange, le changer en gris
                            binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_orange))
                            binding.tvTitleILike.setTextColor(context.getColor(R.color.orange))
                        } else {
                            // Si le pouce est gris, le changer en orange
                            binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_grey))
                            binding.tvTitleILike.setTextColor(context.getColor(R.color.black))
                        }


                        binding.btnILike.setOnClickListener {
                            AnalyticsEvents.logEvent(
                                AnalyticsEvents.Clic_Post_Like
                            )
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
                        if(reactions?.isEmpty() == true && commentsCount == 0){
                            binding.postCommentsNumberLayout.visibility =  View.GONE

                        }
                        binding.btnIComment.setOnClickListener {
                            binding.layoutReactions.visibility =  View.GONE
                            onClick(this, true)
                        }
                        binding.btnReaction.setOnClickListener {
                            AnalyticsEvents.logEvent(
                                AnalyticsEvents.Clic_ListReactions_Contact
                            )
                            binding.layoutReactions.visibility =  View.GONE
                            reactionCallback.seeMemberReaction(this)
                        }
                        binding.postCommentsNumberLayout.setOnClickListener {

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
                            binding.layoutReactions.visibility =  View.GONE
                        }
                        binding.imageViewComments.setOnClickListener {
                            onClick(this, false)
                            binding.layoutReactions.visibility =  View.GONE
                        }
                        binding.postNoComments.setOnClickListener {
                            onClick(this, false)
                            binding.layoutReactions.visibility =  View.GONE
                        }
                        val noCommentsText = "" // Utilise le texte approprié depuis tes ressources
                        val spannableNoCommentsText = SpannableString(noCommentsText)
                        spannableNoCommentsText.setSpan(UnderlineSpan(), 0, noCommentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                        binding.postNoComments.text = spannableNoCommentsText
                        binding.postCommentsNumber.setOnClickListener {
                            onClick(this, false)
                            binding.layoutReactions.visibility =  View.GONE
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
                            binding.postNoComments.visibility = View.GONE
                            binding.postCommentsNumber.visibility = View.GONE
                        } else {
                            binding.postCommentsNumber.visibility = View.VISIBLE
                            binding.postNoComments.visibility = View.GONE
                            if (commentsCount != null) {
                                val resId = if (commentsCount > 1) R.string.posts_comment_number else R.string.posts_comment_number_singular
                                val commentsText = String.format(holder.itemView.context.getString(resId), commentsCount)

                                val spannableString = SpannableString(commentsText)
                                spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

                                binding.postCommentsNumber.text = spannableString
                            } else {
                                // Gérer le cas où commentsCount est null si nécessaire
                                binding.postCommentsNumber.text = "" // Ou une valeur par défaut
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
                                this.id?.let {
                                        it1 -> onClickImage(imageUrl, it1)
                                    binding.layoutReactions.visibility =  View.GONE
                                }
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
                            binding.layoutReactions.visibility =  View.GONE
                        }
                        binding.name.text = user?.displayName
                        binding.image.setOnClickListener {
                            showUserDetail(binding.image.context,this.user?.userId)
                            binding.layoutReactions.visibility =  View.GONE
                        }
                        binding.btnReportPost.setOnClickListener {
                            binding.layoutReactions.visibility =  View.GONE
                            val userId = postsList[position].user?.id?.toInt()
                            if (userId != null){
                                postsList[position].id?.let { it1 -> onReport(it1,userId)
                                }
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
                            binding.btnsLayout.visibility = View.GONE
                            binding.separatorLayout.visibility = View.GONE
                            reactionImageViews.forEach { it.visibility = View.GONE }
                            binding.numberReaction.visibility = View.GONE

                        }else{
                            binding.btnsLayout.visibility = View.VISIBLE
                            binding.separatorLayout.visibility = View.VISIBLE
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
                        //HERE DISABLE CLICK ON REACTIONS IF NOT MEMBER
                        if(isMember == false){
                            binding.btnReportPost.setOnClickListener {
                                reactionCallback.onReactionClicked(this, 0)
                            }
                            binding.btnILike.setOnClickListener {
                                reactionCallback.onReactionClicked(this, 0)

                            }
                            binding.btnIComment.setOnClickListener {
                                reactionCallback.onReactionClicked(this, 0)

                            }
                            binding.btnILike.setOnLongClickListener {
                                reactionCallback.onReactionClicked(this, 0)

                                true
                            }
                        }
                    }
                }
            }

        }
    }
    private fun handleReactionClick(post: Post, reactionType: ReactionType) {
        val currentUserReactionId = post.reactionId ?: 0
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