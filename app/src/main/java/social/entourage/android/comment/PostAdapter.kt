package social.entourage.android.comment

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Survey
import social.entourage.android.api.model.Reaction
import social.entourage.android.api.model.ReactionType
import social.entourage.android.api.model.User
import social.entourage.android.databinding.NewLayoutPostBinding
import social.entourage.android.databinding.SurveyLayoutBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.survey.ResponseSurveyActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.setHyperlinkClickable
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.tools.utils.px
import java.text.SimpleDateFormat

interface SurveyInteractionListener {
    fun onSurveyOptionClicked(postId: Int, surveyResponse: MutableList<Boolean>)
    fun onDeleteSurveyClick(postId: Int, surveyResponse: MutableList<Boolean>)
    fun showParticipantWhoVote(survey:Survey, postId: Int, question:String)
}
interface ReactionInterface{
    fun onReactionClicked(postId: Post, reactionId: Int)
    fun seeMemberReaction(post: Post)
    fun deleteReaction(post: Post)
}
class PostAdapter(
    var context:Context,
    var reactionCallback: ReactionInterface,
    var surveyCallback: SurveyInteractionListener,
    var postsList: MutableList<Post>,
    var isMember: Boolean? = false,
    var onClick: (Post, Boolean) -> Unit,
    var onReport: (Int,Int) -> Unit,
    var onClickImage: (imageUrl:String, postId:Int) -> Unit,
    var memberList:MutableList<EntourageUser>? = null,
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val translationExceptions = mutableSetOf<Int>()
    private val localSurveyResponseState: MutableMap<Int, MutableList<Boolean>> = mutableMapOf()

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
        return if (post.survey != null) TYPE_SURVEY else TYPE_POST
    }

    fun deleteItem(postId: Int) {
        val index = postsList.indexOfFirst { it.id == postId }
        if (index != -1) { // Vérifiez que le post a été trouvé
            postsList.removeAt(index)
            notifyItemRemoved(index) // Notifiez que l'item a été supprimé, permet une animation
            notifyItemRangeChanged(index, postsList.size) // Mettez à jour les positions pour le reste des éléments
        }
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
                    if(post.survey?.multiple == true) {
                        surveyHolder.binding.tvSelectAnswer.text = context.getString(R.string.title_switch_mutiples_choices)
                    }else{
                        surveyHolder.binding.tvSelectAnswer.text = context.getString(R.string.title_switch_single_choice)
                    }
                    val localState = localSurveyResponseState.getOrPut(post.id ?: position) {
                        post.surveyResponse ?: post.survey?.choices?.map { false }?.toMutableList() ?: mutableListOf()
                    }
                    surveyHolder.binding.btnReportPost.setOnClickListener {
                        val userId = postsList[position].user?.id?.toInt()
                        if (userId != null){
                            postsList[position].id?.let { it1 -> onReport(it1,userId)
                            }
                        }
                    }
                    postsList[position].createdTime.let {
                        var locale = LanguageManager.getLocaleFromPreferences(context)
                        surveyHolder.binding.date.text = SimpleDateFormat(
                            itemView.context.getString(R.string.post_date),
                            locale
                        ).format(
                            it!!
                        )
                    }
                    surveyHolder.binding.tvAmbassador.visibility = View.VISIBLE

                    val member = memberList?.find { it.id.toInt() == post.user?.id?.toInt() }

                    var tagsString = ""

                    if (member?.isAdmin() == true) {
                        tagsString += context.getString(R.string.admin) + " • "
                    } else if (member?.isAmbassador() == true) {
                        tagsString += context.getString(R.string.ambassador) + " • "
                    } else if (member?.partner != null) {
                        tagsString += member.partner!!.name
                    }
                    if (tagsString.isEmpty()) {
                        holder.binding.tvAmbassador.visibility = View.GONE
                    } else {
                        holder.binding.tvAmbassador.visibility = View.VISIBLE
                        if (tagsString.endsWith("• ")) {
                            tagsString = tagsString.dropLast(2)
                        }
                        holder.binding.tvAmbassador.text = tagsString
                    }

                    // Création d'une copie locale de summary pour ajustements
                    val localSummary = post.survey?.summary?.toMutableList() ?: mutableListOf()

                    surveyHolder.binding.name.text = post.user?.displayName ?: "Nom inconnu"
                    Glide.with(context)
                        .load(post.user?.avatarURLAsString ?: R.drawable.placeholder_user)
                        .placeholder(R.drawable.placeholder_user)
                        .error(R.drawable.placeholder_user)
                        .circleCrop()
                        .into(surveyHolder.binding.image)

                    surveyHolder.binding.surveyQuestion.text = post.content ?: "Question non disponible"

                    val survey = post.survey ?: return@with
                    listOf(surveyHolder.binding.choiceOne, surveyHolder.binding.choiceTwo, surveyHolder.binding.choiceThree, surveyHolder.binding.choiceFour, surveyHolder.binding.choiceFive).forEachIndexed { index, choiceBinding ->
                        if (index >= survey.choices.size) {
                            choiceBinding.parent.visibility = View.GONE
                        }
                    }

                    survey.choices.forEachIndexed { index, choice ->
                        val binding = when (index) {
                            0 -> surveyHolder.binding.choiceOne
                            1 -> surveyHolder.binding.choiceTwo
                            2 -> surveyHolder.binding.choiceThree
                            3 -> surveyHolder.binding.choiceFour
                            4 -> surveyHolder.binding.choiceFive
                            else -> null
                        }

                        binding?.let {
                            it.parent.visibility = View.VISIBLE
                            it.tvQuestionSurvey.text = choice
                            val isSelected = localState.getOrNull(index) ?: false

                            //it.uiIvCheck.setImageResource(if (isSelected) R.drawable.new_bg_selected_filter else R.drawable.new_bg_unselected_filter)
                            it.uiIvCheck.isChecked = isSelected

                            // Utilisation de localSummary pour l'UI
                            val choiceResponses = localSummary.getOrNull(index) ?: 0
                            var totalResponses = localSummary.sum()
                            val progress = if (totalResponses > 0) (choiceResponses * 100 / totalResponses) else 0
                            it.progressBar3.progress = progress
                            it.tvNumberAnswer.text = "$choiceResponses"

                            it.parent.setOnClickListener {
                                val wasSelected = localState[index]
                                if(wasSelected){
                                    surveyCallback.onDeleteSurveyClick(post.id!!, localState)
                                    survey.summary[index] = survey.summary[index] - 1
                                }else{
                                    survey.summary[index] = survey.summary[index] + 1
                                }
                                if (survey.multiple) {
                                    // Logique pour sondages à choix multiples
                                    localState[index] = !wasSelected
                                    if (localState[index]) {
                                        localSummary[index] = localSummary.getOrNull(index)?.plus(1) ?: 1
                                    } else {
                                        localSummary[index] = (localSummary.getOrNull(index)?.minus(1))?.coerceAtLeast(0) ?: 0
                                    }

                                } else {
                                    // Logique pour sondages à choix unique
                                    val previouslySelectedIndex = localState.indexOf(true) // Trouve l'ancienne réponse sélectionnée
                                    if (previouslySelectedIndex != -1) {
                                        // Décrémente l'ancienne réponse si elle existe
                                        localSummary[previouslySelectedIndex] = (localSummary[previouslySelectedIndex] - 1).coerceAtLeast(0)
                                        localState[previouslySelectedIndex] = false
                                    }
                                    // Sélectionne la nouvelle réponse
                                    localState.fill(false) // Désélectionne toutes les réponses
                                    localState[index] = true // Sélectionne la nouvelle réponse
                                    localSummary[index] = localSummary.getOrNull(index)?.plus(1) ?: 1
                                }
                                surveyCallback.onSurveyOptionClicked(post.id!!, localState)

                                // Mise à jour de l'UI pour tous les choix après ajustement
                                survey.choices.forEachIndexed { choiceIndex, _ ->
                                    updateSurveyUI(surveyHolder, choiceIndex, survey, localState, localSummary)

                                }

                                if (postsList[position].commentsCount != null) {
                                    val resId = if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                                    val commentsText = " - " + String.format(holder.itemView.context.getString(resId), totalResponses)
                                    val spannableString = SpannableString(commentsText)
                                    spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                                    surveyHolder.binding.postVoteNumber.text = spannableString
                                } else {

                                    // Gérer le cas où commentsCount est null si nécessaire
                                    val resId = if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                                    val commentsText = String.format(holder.itemView.context.getString(resId), totalResponses)
                                    val spannableString = SpannableString(commentsText)
                                    spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                                    surveyHolder.binding.postVoteNumber.text = spannableString
                                }
                                if(totalResponses == 0 ){
                                    surveyHolder.binding.postVoteNumber.visibility = View.GONE
                                }
                                updateVoteNumberText(surveyHolder, localSummary)  // Appelle cette fonction pour mettre à jour le texte du nombre de votes
                            }
                        }

                    }
                    if(postsList[position].status == "deleted" || postsList[position].status == "offensive" || postsList[position].status == "offensible"){
                        surveyHolder.binding.surveyQuestion.text = context.getText(R.string.deleted_publi)
                        if(postsList[position].status == "offensive" || postsList[position].status == "offensible"){
                            surveyHolder.binding.surveyQuestion.text = context.getText(R.string.offensive_message)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            surveyHolder.binding.surveyQuestion.setTextColor(context.getColor(R.color.deleted_grey))
                        }
                        surveyHolder.binding.surveyQuestion.visibility = View.VISIBLE
                        surveyHolder.binding.btnReportPost.visibility = View.GONE
                        surveyHolder.binding.choiceOne.parent.visibility = View.GONE
                        surveyHolder.binding.choiceTwo.parent.visibility = View.GONE
                        surveyHolder.binding.choiceThree.parent.visibility = View.GONE
                        surveyHolder.binding.choiceFour.parent.visibility = View.GONE
                        surveyHolder.binding.choiceFive.parent.visibility = View.GONE
                    }
                    surveyHolder.binding.tvTitleILike.setText(context.getText(R.string.text_title_i_like))
                    surveyHolder.binding.tvIComment.setText(context.getText(R.string.text_title_comment))

                    surveyHolder.binding.surveyLayout.setOnClickListener {
                        surveyHolder.binding.layoutReactions.visibility = View.GONE
                    }


                    // Ajouter un listener pour l'appui long sur le bouton "j'aime"
                    surveyHolder.binding.btnILike.setOnLongClickListener {
                        AnalyticsEvents.logEvent(
                            AnalyticsEvents.Clic_Post_List_Reactions
                        )
                        surveyHolder.binding.layoutReactions.visibility = if (surveyHolder.binding.layoutReactions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
                        if (surveyHolder.binding.layoutReactions.visibility == View.VISIBLE) {
                            animateReactionLayout(
                                listOf(
                                    surveyHolder.binding.ivReactOne,
                                    surveyHolder.binding.ivReactTwo,
                                    surveyHolder.binding.ivReactThree,
                                    surveyHolder.binding.ivReactFour,
                                    surveyHolder.binding.ivReactFive
                                )
                            )
                        } else {
                            surveyHolder.binding.layoutReactions.visibility = View.VISIBLE
                        }
                        true
                    }

                    with(postsList[position]) {
                        if(this.reactionId == null ){
                            surveyHolder.binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_grey))
                            surveyHolder.binding.tvTitleILike.setTextColor(context.getColor(R.color.black))
                        }else if (this.reactionId != 0 ) {
                            // Si le pouce est orange, le changer en gris
                            surveyHolder.binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_orange))
                            surveyHolder.binding.tvTitleILike.setTextColor(context.getColor(R.color.orange))
                        } else {
                            // Si le pouce est gris, le changer en orange
                            surveyHolder.binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_grey))
                            surveyHolder.binding.tvTitleILike.setTextColor(context.getColor(R.color.black))
                        }
                        surveyHolder.binding.btnILike.setOnClickListener {

                            val firstReactionType = MainActivity.reactionsList?.firstOrNull()
                            if(firstReactionType != null){
                                AnalyticsEvents.logEvent(
                                    AnalyticsEvents.Clic_Post_Like
                                )
                                surveyHolder.binding.layoutReactions.visibility =  View.GONE
                                handleReactionClick(this, firstReactionType)
                            }
                        }
                        val reactionImageViews = listOf(
                            surveyHolder.binding.ivReactOne,
                            surveyHolder.binding.ivReactTwo,
                            surveyHolder.binding.ivReactThree,
                            surveyHolder.binding.ivReactFour,
                            surveyHolder.binding.ivReactFive
                        )
                        val reactionTypes = MainActivity.reactionsList
                        reactionTypes?.let { types ->
                            types.take(5).forEachIndexed { index, reactionType ->
                                Glide.with(context).load(reactionType.imageUrl).into(reactionImageViews[index])
                                reactionImageViews[index].setOnClickListener {
                                    animateBubbleEffect(reactionImageViews[index], surveyHolder.binding.layoutReactions) {
                                        handleReactionClick(post, reactionType)
                                        surveyHolder.binding.layoutReactions.visibility =  View.GONE
                                    }
                                }
                            }
                        }

                        val reactionViews = listOf(
                            surveyHolder.binding.reaction1,
                            surveyHolder.binding.reaction2,
                            surveyHolder.binding.reaction3,
                            surveyHolder.binding.reaction4,
                            surveyHolder.binding.reaction5
                        )
                        val reactionsLayouts = arrayOf(
                            surveyHolder.binding.reaction1.layoutItemReactionParent,
                            surveyHolder.binding.reaction2.layoutItemReactionParent,
                            surveyHolder.binding.reaction3.layoutItemReactionParent,
                            surveyHolder.binding.reaction4.layoutItemReactionParent,
                            surveyHolder.binding.reaction5.layoutItemReactionParent
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
                        surveyHolder.binding.numberReaction.text = totalReactionsCount.toString()
                        if (totalReactionsCount > 0) {
                            surveyHolder.binding.numberReaction.visibility = View.VISIBLE
                        } else {
                            surveyHolder.binding.numberReaction.visibility = View.GONE
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
                        if(reactions?.isEmpty() == true && commentsCount == 0 && survey.summary.isEmpty()){
                            surveyHolder.binding.postCommentsNumberLayout.visibility =  View.GONE

                        }else{
                            surveyHolder.binding.postCommentsNumberLayout.visibility =  View.VISIBLE
                        }
                        surveyHolder.binding.btnIComment.setOnClickListener {
                            surveyHolder.binding.layoutReactions.visibility =  View.GONE
                            onClick(this, true)
                        }
                        surveyHolder.binding.btnReaction.setOnClickListener {
                            AnalyticsEvents.logEvent(
                                AnalyticsEvents.Clic_ListReactions_Contact
                            )
                            surveyHolder.binding.layoutReactions.visibility =  View.GONE
                            reactionCallback.seeMemberReaction(this)
                        }
                        surveyHolder.binding.postCommentsNumberLayout.setOnClickListener {

                        }
                        if (hasComments == false) {
                            surveyHolder.binding.postNoComments.visibility = View.GONE
                            surveyHolder.binding.postCommentsNumber.visibility = View.GONE
                        } else {
                            surveyHolder.binding.postCommentsNumber.visibility = View.VISIBLE
                            surveyHolder.binding.postNoComments.visibility = View.GONE
                            if (commentsCount != null) {
                                val resId = if (commentsCount > 1) R.string.posts_comment_number else R.string.posts_comment_number_singular
                                val commentsText = String.format(holder.itemView.context.getString(resId), commentsCount)

                                val spannableString = SpannableString(commentsText)
                                spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)

                                surveyHolder.binding.postCommentsNumber.text = spannableString
                            } else {
                                // Gérer le cas où commentsCount est null si nécessaire
                                surveyHolder.binding.postCommentsNumber.text = "" // Ou une valeur par défaut
                            }
                        }

                        surveyHolder.binding.postCommentsNumber?.setOnClickListener {
                            onClick(this, true)
                        }

                        if (survey.summary.isEmpty()) {
                            surveyHolder.binding.postVoteNumber.visibility = View.GONE
                            surveyHolder.binding.postCommentsNumber.visibility = View.GONE
                        } else {
                            surveyHolder.binding?.postVoteNumber?.setOnClickListener {
                                ResponseSurveyActivity.myVote.clear() // Nettoie les anciens votes s'il s'agit d'un choix unique
                                ResponseSurveyActivity.myVote = localState.toMutableList()
                                surveyCallback.showParticipantWhoVote(post.survey, post.id!!, post.content!!)
                            }
                            surveyHolder.binding.postVoteNumber.visibility = View.VISIBLE
                            surveyHolder.binding.postNoComments.visibility = View.GONE
                            val totalResponses = survey.summary.sum()
                            if (commentsCount != null && commentsCount != 0) {
                                val resId = if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                                val commentsText = " - " + String.format(holder.itemView.context.getString(resId), totalResponses)
                                val spannableString = SpannableString(commentsText)
                                spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                                surveyHolder.binding.postVoteNumber.text = spannableString
                            } else {

                                // Gérer le cas où commentsCount est null si nécessaire
                                val resId = if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                                val commentsText = String.format(holder.itemView.context.getString(resId), totalResponses)
                                val spannableString = SpannableString(commentsText)
                                spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                                surveyHolder.binding.postVoteNumber.text = spannableString
                            }
                            if(totalResponses == 0 ){
                                surveyHolder.binding.postVoteNumber.visibility = View.GONE
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
                        // Rendre `layoutReactions` visible, même si aucune réaction n'existe
                        if (binding.layoutReactions.visibility == View.VISIBLE) {

                        } else {
                            binding.layoutReactions.visibility = View.VISIBLE
                            animateReactionLayout(
                                listOf(
                                    binding.ivReactOne,
                                    binding.ivReactTwo,
                                    binding.ivReactThree,
                                    binding.ivReactFour,
                                    binding.ivReactFive
                                )
                            )
                        }
                        true
                    }


                    with(postsList[position]) {
                        if(this.autoPostFrom != null){
                            binding.layoutActionInPubli.visibility = View.VISIBLE
                        }else{
                            binding.layoutActionInPubli.visibility = View.GONE
                        }

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
                            VibrationUtil.vibrate(context)
                            val firstReactionType = MainActivity.reactionsList?.firstOrNull()
                            if(firstReactionType != null){
                                AnalyticsEvents.logEvent(
                                    AnalyticsEvents.Clic_Post_Like
                                )
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
                                    animateBubbleEffect(reactionImageViews[index], binding.layoutReactions) {
                                        handleReactionClick(postsList[position], reactionType)
                                        binding.layoutReactions.visibility =  View.GONE
                                    }
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

                        }else{
                            binding.postCommentsNumberLayout.visibility =  View.VISIBLE
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

                        val _post = postsList[position]
                        var tagsString = ""
                        if (_post.user?.roles?.isNotEmpty() == true) {
                            _post.user?.roles?.let { roles ->
                                if (roles.isNotEmpty()) {
                                    val role = roles[0]
                                    tagsString += role
                                }
                            }
                        }
                        if (tagsString.isEmpty()) {
                            holder.binding.tvAmbassador.visibility = View.GONE
                        } else {
                            holder.binding.tvAmbassador.visibility = View.VISIBLE
                            if (tagsString.endsWith("• ")) {
                                tagsString = tagsString.dropLast(2)
                            }
                            holder.binding.tvAmbassador.text = tagsString
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
                        if(status == "deleted" || status == "offensive" || status == "offensible"){
                            binding.postMessage.text = context.getText(R.string.deleted_publi)
                            if(status == "offensive" || status == "offensible"){
                                binding.postMessage.text = context.getText(R.string.offensive_message)
                            }
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

    fun updateVoteNumberText(surveyHolder: SurveyViewHolder, localSummary: MutableList<Int>) {
        val totalResponses = localSummary.sum()
        val context = surveyHolder.itemView.context
        val resId = if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
        val commentsText = String.format(context.getString(resId), totalResponses)
        val spannableString = SpannableString(commentsText)
        spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
        surveyHolder.binding.postVoteNumber.text = spannableString
        if (totalResponses == 0) {
            surveyHolder.binding.postVoteNumber.visibility = View.GONE
        } else {
            surveyHolder.binding.postVoteNumber.visibility = View.VISIBLE
        }
    }

    private fun updateSurveyUI(surveyHolder: SurveyViewHolder, index: Int, survey: Survey, localState: MutableList<Boolean>, localSummary: List<Int>) {
        val binding = when (index) {
            0 -> surveyHolder.binding.choiceOne
            1 -> surveyHolder.binding.choiceTwo
            2 -> surveyHolder.binding.choiceThree
            3 -> surveyHolder.binding.choiceFour
            4 -> surveyHolder.binding.choiceFive
            else -> null
        }

        binding?.let {
            val isSelected = localState.getOrNull(index) ?: false
            it.uiIvCheck.isChecked = isSelected
            //it.uiIvCheck.setImageResource(if (isSelected) R.drawable.new_bg_selected_filter else R.drawable.new_bg_unselected_filter)

            // Utilise les totaux ajustés de localSummary pour l'affichage
            val choiceResponses = localSummary.getOrNull(index) ?: 0
            val totalResponses = localSummary.sum()
            val progress = if (totalResponses > 0) (choiceResponses * 100 / totalResponses) else 0

            it.progressBar3.progress = progress
            it.tvNumberAnswer.text = "$choiceResponses"
            it.parent.visibility = View.VISIBLE
        }
    }



    fun adjustSurveyTotalsBasedOnLocalState(survey: Survey, localState: MutableList<Boolean>, previousLocalState: MutableList<Boolean>): List<Int> {
        // Copie les totaux originaux pour ne pas les modifier directement
        val adjustedSummary = survey.summary.toMutableList()

        // Parcourt l'état local pour ajuster les totaux
        localState.forEachIndexed { index, isSelected ->
            val wasSelected = previousLocalState[index]
            if (isSelected && !wasSelected) {
                // Si l'option est maintenant sélectionnée mais ne l'était pas avant, incrémente le total
                adjustedSummary[index] = adjustedSummary[index] + 1
            } else if (!isSelected && wasSelected) {
                // Si l'option était sélectionnée mais ne l'est plus, décrémente le total
                // en veillant à ne pas aller en dessous de zéro
                adjustedSummary[index] = (adjustedSummary[index] - 1).coerceAtLeast(0)
            }
        }

        // Met à jour le précédent état local pour refléter l'état actuel après ajustement
        previousLocalState.clear()
        previousLocalState.addAll(localState)

        return adjustedSummary
    }

    private fun animateReactionLayout(reactionViews: List<ImageView>) {
        reactionViews.forEachIndexed { index, imageView ->
            imageView.visibility = View.VISIBLE // Assurez-vous qu'elle est visible
            imageView.scaleX = 0f
            imageView.scaleY = 0f

            imageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(-10f) // Rebond léger vers le haut
                .setStartDelay(index * 100L) // Décalage entre chaque image
                .setDuration(300L) // Durée de chaque animation
                .withEndAction {
                    imageView.animate()
                        .translationY(0f) // Retour à la position initiale
                        .setDuration(300L)
                        .start()
                }
                .start()
        }
    }

    private fun animateBubbleEffect(
        clickedView: ImageView,
        layoutReactions: View,
        onAnimationEnd: () -> Unit
    ) {
        clickedView.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150L)
            .withEndAction {
                clickedView.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150L)
                    .withEndAction {
                        layoutReactions.visibility = View.GONE // Cache le layout après l'animation
                        onAnimationEnd()
                    }
                    .start()
            }
            .start()
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
        ProfileFullActivity.isMe = false
        ProfileFullActivity.userId = userId!!
        (context as? Activity)?.startActivityForResult(
            Intent(context, ProfileFullActivity::class.java).putExtra(
                Const.USER_ID,
                userId
            ), 0
        )
    }
    override fun getItemCount(): Int {
        return postsList.size
    }
}