package social.entourage.android.comment

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Reaction
import social.entourage.android.api.model.ReactionType
import social.entourage.android.api.model.Survey
import social.entourage.android.databinding.NewLayoutPostBinding
import social.entourage.android.databinding.SurveyLayoutBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.survey.ResponseSurveyActivity
import social.entourage.android.tools.displayHtml
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.tools.utils.px
import timber.log.Timber
import java.text.SimpleDateFormat

interface SurveyInteractionListener {
    fun onSurveyOptionClicked(postId: Int, surveyResponse: MutableList<Boolean>)
    fun onDeleteSurveyClick(postId: Int, surveyResponse: MutableList<Boolean>)
    fun showParticipantWhoVote(survey: Survey, postId: Int, question: String)
}
interface ReactionInterface {
    fun onReactionClicked(postId: Post, reactionId: Int)
    fun seeMemberReaction(post: Post)
    fun deleteReaction(post: Post)
}

class PostAdapter(
    var context: Context,
    var reactionCallback: ReactionInterface,
    var surveyCallback: SurveyInteractionListener,
    var postsList: MutableList<Post>,
    var isMember: Boolean? = false,
    var onClick: (Post, Boolean) -> Unit,
    var onReport: (Int, Int) -> Unit,
    var onClickImage: (imageUrl: String, postId: Int) -> Unit,
    var memberList: MutableList<EntourageUser>? = null,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val translationExceptions = mutableSetOf<Int>()
    private val localSurveyResponseState: MutableMap<Int, MutableList<Boolean>> = mutableMapOf()

    companion object {
        const val TYPE_POST = 0
        const val TYPE_SURVEY = 1
    }

    fun initiateList() {
        val translatedByDefault = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        ).getBoolean("translatedByDefault", false)

        if (translatedByDefault) {
            postsList.forEach {
                // On regarde contentTranslationsHtml (au lieu de contentTranslations)
                if (it.contentTranslationsHtml != null) {
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
        if (index != -1) {
            postsList.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, postsList.size)
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

    inner class ViewHolder(val binding: NewLayoutPostBinding)
        : RecyclerView.ViewHolder(binding.root)

    inner class SurveyViewHolder(val binding: SurveyLayoutBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_SURVEY -> {
                val binding = SurveyLayoutBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                SurveyViewHolder(binding)
            }
            else -> {
                val binding = NewLayoutPostBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_SURVEY -> bindSurvey(holder as SurveyViewHolder, position)
            TYPE_POST -> bindPost(holder as ViewHolder, position)
        }
    }

    // -------------------------
    // BLOC : GESTION DES SURVEYS
    // -------------------------
    private fun bindSurvey(surveyHolder: SurveyViewHolder, position: Int) {
        val post = postsList[position]

        if (post.survey?.multiple == true) {
            surveyHolder.binding.tvSelectAnswer.text =
                context.getString(R.string.title_switch_mutiples_choices)
        } else {
            surveyHolder.binding.tvSelectAnswer.text =
                context.getString(R.string.title_switch_single_choice)
        }

        // Gérer le localSurveyResponseState
        val localState = localSurveyResponseState.getOrPut(post.id ?: position) {
            post.surveyResponse ?: post.survey?.choices?.map { false }?.toMutableList() ?: mutableListOf()
        }

        // Bouton report
        surveyHolder.binding.btnReportPost.setOnClickListener {
            val userId = post.user?.id?.toInt()
            if (userId != null) {
                post.id?.let { postId -> onReport(postId, userId) }
            }
        }

        // Date
        post.createdTime?.let {
            val locale = LanguageManager.getLocaleFromPreferences(context)
            surveyHolder.binding.date.text = SimpleDateFormat(
                context.getString(R.string.post_date),
                locale
            ).format(it)
        }

        // Gérer le badge admin/ambassador/partner via la memberList
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
            surveyHolder.binding.tvAmbassador.visibility = View.GONE
        } else {
            surveyHolder.binding.tvAmbassador.visibility = View.VISIBLE
            if (tagsString.endsWith("• ")) {
                tagsString = tagsString.dropLast(2)
            }
            surveyHolder.binding.tvAmbassador.text = tagsString
        }

        // Charger l’avatar
        surveyHolder.binding.name.text = post.user?.displayName ?: "Nom inconnu"
        Glide.with(context)
            .load(post.user?.avatarURLAsString ?: R.drawable.placeholder_user)
            .placeholder(R.drawable.placeholder_user)
            .error(R.drawable.placeholder_user)
            .circleCrop()
            .into(surveyHolder.binding.authorImage)

        // On affiche la question
        surveyHolder.binding.surveyQuestion.text = post.content ?: "Question non disponible"

        val survey = post.survey
        if (survey == null) return

        // Afficher/Masquer les 5 choix
        val choiceBindings = listOf(
            surveyHolder.binding.choiceOne,
            surveyHolder.binding.choiceTwo,
            surveyHolder.binding.choiceThree,
            surveyHolder.binding.choiceFour,
            surveyHolder.binding.choiceFive
        )
        choiceBindings.forEachIndexed { index, choiceBinding ->
            if (index >= survey.choices.size) {
                choiceBinding.parent.visibility = View.GONE
            }
        }

        // Copie locale du summary (pour affichage dynamique)
        val localSummary = survey.summary.toMutableList()

        // Gérer chaque choix
        survey.choices.forEachIndexed { index, choice ->
            val bindingChoice = choiceBindings.getOrNull(index) ?: return@forEachIndexed
            bindingChoice.parent.visibility = View.VISIBLE
            bindingChoice.tvQuestionSurvey.text = choice

            // Sélection locale
            val isSelected = localState.getOrNull(index) ?: false
            bindingChoice.uiIvCheck.isChecked = isSelected

            // Calcul progress bar
            val choiceResponses = localSummary.getOrNull(index) ?: 0
            val totalResponses = localSummary.sum()
            val progress = if (totalResponses > 0) (choiceResponses * 100 / totalResponses) else 0
            bindingChoice.progressBar3.progress = progress
            bindingChoice.tvNumberAnswer.text = "$choiceResponses"

            // Clic sur un choix
            bindingChoice.parent.setOnClickListener {
                val wasSelected = localState[index]
                if (wasSelected) {
                    surveyCallback.onDeleteSurveyClick(post.id!!, localState)
                    survey.summary[index] = survey.summary[index] - 1
                } else {
                    survey.summary[index] = survey.summary[index] + 1
                }
                if (survey.multiple) {
                    // Choix multiples
                    localState[index] = !wasSelected
                    if (localState[index]) {
                        localSummary[index] = localSummary.getOrNull(index)?.plus(1) ?: 1
                    } else {
                        localSummary[index] = (localSummary.getOrNull(index)?.minus(1))?.coerceAtLeast(0) ?: 0
                    }
                } else {
                    // Choix unique
                    val previouslySelectedIndex = localState.indexOf(true)
                    if (previouslySelectedIndex != -1) {
                        localSummary[previouslySelectedIndex] =
                            (localSummary[previouslySelectedIndex] - 1).coerceAtLeast(0)
                        localState[previouslySelectedIndex] = false
                    }
                    localState.fill(false)
                    localState[index] = true
                    localSummary[index] = localSummary.getOrNull(index)?.plus(1) ?: 1
                }
                surveyCallback.onSurveyOptionClicked(post.id!!, localState)

                // Mettre à jour l’UI pour tous les choix
                survey.choices.forEachIndexed { choiceIndex, _ ->
                    updateSurveyUI(surveyHolder, choiceIndex, survey, localState, localSummary)
                }

                // Mise à jour du texte "vote"
                val totalAfter = localSummary.sum()
                val resId = if (totalAfter > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                val commentsText = if (post.commentsCount != null) {
                    " - " + String.format(context.getString(resId), totalAfter)
                } else {
                    String.format(context.getString(resId), totalAfter)
                }
                val spannableString = SpannableString(commentsText)
                spannableString.setSpan(UnderlineSpan(), 0, commentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE)
                surveyHolder.binding.postVoteNumber.text = spannableString

                if (totalAfter == 0) {
                    surveyHolder.binding.postVoteNumber.visibility = View.GONE
                } else {
                    surveyHolder.binding.postVoteNumber.visibility = View.VISIBLE
                }
            }
        }

        // --------------------------------------------------------------------
        // AJOUT MANQUANT : gérant "deleted" / "offensive" / "offensible"
        // --------------------------------------------------------------------
        if (
            post.status == "deleted" ||
            post.status == "offensive" ||
            post.status == "offensible"
        ) {
            surveyHolder.binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
            // On adapte le texte
            when (post.status) {
                "offensive", "offensible" ->
                    surveyHolder.binding.surveyQuestion.text = context.getText(R.string.offensive_message)
                else -> // "deleted"
                    surveyHolder.binding.surveyQuestion.text = context.getText(R.string.deleted_publi)
            }
            surveyHolder.binding.surveyQuestion.setTextColor(context.getColor(R.color.deleted_grey))
            surveyHolder.binding.surveyQuestion.visibility = View.VISIBLE
            // On cache tout le reste
            surveyHolder.binding.btnReportPost.visibility = View.GONE
            surveyHolder.binding.choiceOne.parent.visibility = View.GONE
            surveyHolder.binding.choiceTwo.parent.visibility = View.GONE
            surveyHolder.binding.choiceThree.parent.visibility = View.GONE
            surveyHolder.binding.choiceFour.parent.visibility = View.GONE
            surveyHolder.binding.choiceFive.parent.visibility = View.GONE
        }
        // --------------------------------------------------------------------

        // Textes "J'aime" / "Commenter"
        surveyHolder.binding.tvTitleILike.text = context.getText(R.string.text_title_i_like)
        surveyHolder.binding.tvIComment.text = context.getText(R.string.text_title_comment)

        // Clic sur zone vide => ferme layoutReactions
        surveyHolder.binding.surveyLayout.setOnClickListener {
            surveyHolder.binding.layoutReactions.visibility = View.GONE
        }

        // Long click sur "j'aime" => affiche la liste des réactions
        surveyHolder.binding.btnILike.setOnLongClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Post_List_Reactions)
            val isVisible = (surveyHolder.binding.layoutReactions.visibility == View.VISIBLE)
            surveyHolder.binding.layoutReactions.visibility =
                if (isVisible) View.GONE else View.VISIBLE
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
            }
            true
        }

        // Gérer le "like" principal
        with(post) {
            if (reactionId == null || reactionId == 0) {
                // pouce gris
                surveyHolder.binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_grey))
                surveyHolder.binding.tvTitleILike.setTextColor(context.getColor(R.color.black))
            } else {
                // pouce orange
                surveyHolder.binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_orange))
                surveyHolder.binding.tvTitleILike.setTextColor(context.getColor(R.color.orange))
            }

            surveyHolder.binding.btnILike.setOnClickListener {
                val firstReactionType = MainActivity.reactionsList?.firstOrNull()
                if (firstReactionType != null) {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Post_Like)
                    surveyHolder.binding.layoutReactions.visibility = View.GONE
                    handleReactionClick(this, firstReactionType)
                }
            }

            // Images des réactions au long-press
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
                            handleReactionClick(this, reactionType)
                            surveyHolder.binding.layoutReactions.visibility = View.GONE
                        }
                    }
                }
            }

            // Afficher la liste (max 5) des réactions
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
            // Tout cacher d’emblée
            reactionsLayouts.forEach { it.visibility = View.GONE }

            // Puis on affiche celles du post
            reactions?.forEachIndexed { index, r ->
                if (index < reactionsLayouts.size) {
                    val foundType = reactionTypes?.find { it.id == r.reactionId }
                    foundType?.let {
                        Glide.with(context).load(it.imageUrl).into(reactionViews[index].reactionImage)
                        reactionsLayouts[index].visibility = View.VISIBLE
                    }
                }
            }

            // Nombre total de réactions
            val totalReactionsCount = reactions?.sumOf { it.reactionsCount } ?: 0
            surveyHolder.binding.numberReaction.text = totalReactionsCount.toString()
            if (totalReactionsCount > 0) {
                surveyHolder.binding.numberReaction.visibility = View.VISIBLE
            } else {
                surveyHolder.binding.numberReaction.visibility = View.GONE
            }

            // Affichage des commentaires
            if (reactions?.isEmpty() == true && commentsCount == 0 && survey.summary.isEmpty()) {
                surveyHolder.binding.postCommentsNumberLayout.visibility = View.GONE
            } else {
                surveyHolder.binding.postCommentsNumberLayout.visibility = View.VISIBLE
            }
            surveyHolder.binding.btnIComment.setOnClickListener {
                surveyHolder.binding.layoutReactions.visibility = View.GONE
                onClick(this, true)
            }
            surveyHolder.binding.btnReaction.setOnClickListener {
                AnalyticsEvents.logEvent(AnalyticsEvents.Clic_ListReactions_Contact)
                surveyHolder.binding.layoutReactions.visibility = View.GONE
                reactionCallback.seeMemberReaction(this)
            }

            // Nombre de commentaires
            if (hasComments == false) {
                surveyHolder.binding.postNoComments.visibility = View.GONE
                surveyHolder.binding.postCommentsNumber.visibility = View.GONE
            } else {
                surveyHolder.binding.postCommentsNumber.visibility = View.VISIBLE
                surveyHolder.binding.postNoComments.visibility = View.GONE
                if (commentsCount != null) {
                    val resId = if (commentsCount > 1)
                        R.string.posts_comment_number
                    else
                        R.string.posts_comment_number_singular

                    val commentsText = String.format(context.getString(resId), commentsCount)
                    val spannableString = SpannableString(commentsText)
                    spannableString.setSpan(
                        UnderlineSpan(),
                        0,
                        commentsText.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    surveyHolder.binding.postCommentsNumber.text = spannableString
                } else {
                    surveyHolder.binding.postCommentsNumber.text = ""
                }
            }
            surveyHolder.binding.postCommentsNumber.setOnClickListener {
                onClick(this, true)
            }

            // Gérer le vote number
            if (survey.summary.isEmpty()) {
                surveyHolder.binding.postVoteNumber.visibility = View.GONE
                surveyHolder.binding.postCommentsNumber.visibility = View.GONE
            } else {
                surveyHolder.binding.postVoteNumber.setOnClickListener {
                    ResponseSurveyActivity.myVote.clear()
                    ResponseSurveyActivity.myVote = localState.toMutableList()
                    surveyCallback.showParticipantWhoVote(survey, id!!, content ?: "")
                }
                surveyHolder.binding.postVoteNumber.visibility = View.VISIBLE
                surveyHolder.binding.postNoComments.visibility = View.GONE
                val totalResponses = survey.summary.sum()
                if (commentsCount != null && commentsCount != 0) {
                    val resId = if (totalResponses > 1)
                        R.string.posts_vote_number
                    else
                        R.string.posts_vote_number_singular
                    val commentsText = " - " + String.format(context.getString(resId), totalResponses)
                    val spannableString = SpannableString(commentsText)
                    spannableString.setSpan(
                        UnderlineSpan(),
                        0,
                        commentsText.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    surveyHolder.binding.postVoteNumber.text = spannableString
                } else {
                    val resId = if (totalResponses > 1)
                        R.string.posts_vote_number
                    else
                        R.string.posts_vote_number_singular
                    val commentsText = String.format(context.getString(resId), totalResponses)
                    val spannableString = SpannableString(commentsText)
                    spannableString.setSpan(
                        UnderlineSpan(),
                        0,
                        commentsText.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    surveyHolder.binding.postVoteNumber.text = spannableString
                }
                if (totalResponses == 0) {
                    surveyHolder.binding.postVoteNumber.visibility = View.GONE
                }
            }
        }
    }

    // Mise à jour de l’affichage d’un choix (Survey)
    private fun updateSurveyUI(
        surveyHolder: SurveyViewHolder,
        index: Int,
        survey: Survey,
        localState: MutableList<Boolean>,
        localSummary: List<Int>
    ) {
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

            val choiceResponses = localSummary.getOrNull(index) ?: 0
            val totalResponses = localSummary.sum()
            val progress = if (totalResponses > 0) (choiceResponses * 100 / totalResponses) else 0
            it.progressBar3.progress = progress
            it.tvNumberAnswer.text = "$choiceResponses"
            it.parent.visibility = View.VISIBLE
        }
    }


    // -------------------------
    // BLOC : GESTION DES POSTS
    // -------------------------
    private fun bindPost(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val post = postsList[position]
        val meId = EntourageApplication.get().me()?.id

        binding.tvTitleILike.setText(context.getText(R.string.text_title_i_like))
        binding.tvIComment.setText(context.getText(R.string.text_title_comment))

        binding.layoutPostParent.setOnClickListener {
            binding.layoutReactions.visibility = View.GONE
        }

        // Long click sur "j'aime" => affiche la liste des réactions
        binding.btnILike.setOnLongClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Post_List_Reactions)
            if (binding.layoutReactions.visibility != View.VISIBLE) {
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

        // On gère l’autoPostFrom
        if (post.autoPostFrom != null) {
            binding.layoutActionInPubli.visibility = View.VISIBLE
        } else {
            binding.layoutActionInPubli.visibility = View.GONE
        }

        // Gérer l’icône de "like" local
        if (post.reactionId == null || post.reactionId == 0) {
            binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_grey))
            binding.tvTitleILike.setTextColor(context.getColor(R.color.black))
        } else {
            binding.ivILike.setImageDrawable(context.getDrawable(R.drawable.ic_pouce_orange))
            binding.tvTitleILike.setTextColor(context.getColor(R.color.orange))
        }

        // Clique rapide sur "like" => applique la 1ère réaction
        binding.btnILike.setOnClickListener {
            VibrationUtil.vibrate(context)
            val firstReactionType = MainActivity.reactionsList?.firstOrNull()
            if (firstReactionType != null) {
                AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Post_Like)
                binding.layoutReactions.visibility = View.GONE
                handleReactionClick(post, firstReactionType)
            }
        }

        // Préparer le layout "réactions"
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
                        handleReactionClick(post, reactionType)
                        binding.layoutReactions.visibility = View.GONE
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
        reactionsLayouts.forEach { it.visibility = View.GONE }

        post.reactions?.forEachIndexed { index, r ->
            if (index < reactionsLayouts.size) {
                val foundType = reactionTypes?.find { it.id == r.reactionId }
                foundType?.let {
                    Glide.with(context).load(it.imageUrl).into(reactionViews[index].reactionImage)
                    reactionsLayouts[index].visibility = View.VISIBLE
                }
            }
        }

        // Nombre total de réactions
        val totalReactionsCount = post.reactions?.sumOf { it.reactionsCount } ?: 0
        binding.numberReaction.text = totalReactionsCount.toString()
        if (totalReactionsCount > 0) {
            binding.numberReaction.visibility = View.VISIBLE
        } else {
            binding.numberReaction.visibility = View.GONE
        }

        if (post.reactions?.isEmpty() == true && post.commentsCount == 0) {
            binding.postCommentsNumberLayout.visibility = View.GONE
        } else {
            binding.postCommentsNumberLayout.visibility = View.VISIBLE
        }

        binding.btnIComment.setOnClickListener {
            binding.layoutReactions.visibility = View.GONE
            onClick(post, true)
        }
        binding.btnReaction.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_ListReactions_Contact)
            binding.layoutReactions.visibility = View.GONE
            reactionCallback.seeMemberReaction(post)
        }
        binding.postCommentsNumberLayout.setOnClickListener {
            // (Au besoin, si tu veux un clic)
        }

        // Copie du texte
        binding.postMessage.setOnLongClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_CopyPaste_LongClic)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                context.getString(R.string.copied_text),
                binding.postMessage.text
            )
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, context.getString(R.string.copied_text), Toast.LENGTH_SHORT).show()
            true
        }

        // Logique de traduction
        if (DataLanguageStock.userLanguage == post.contentTranslationsHtml?.fromLang ||
            (post.user?.id == meId?.toLong() && (post.content == ""))) {
            binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
        } else {
            binding.postTranslationButton.layoutCsTranslate.visibility = View.VISIBLE
        }
        if (post.contentTranslationsHtml == null) {
            binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
        }

        // Déterminer si le post est "traduit" ou non
        val isTranslated = !translationExceptions.contains(post.id)
        val textButton = if (isTranslated) {
            context.getString(R.string.layout_translate_title_translation)
        } else {
            context.getString(R.string.layout_translate_title_original)
        }
        val spannableTextButton = SpannableString(textButton)
        spannableTextButton.setSpan(UnderlineSpan(), 0, textButton.length, 0)
        binding.postTranslationButton.tvTranslate.text = spannableTextButton

        binding.postTranslationButton.layoutCsTranslate.setOnClickListener {
            translateItem(post.id ?: 0)
            binding.layoutReactions.visibility = View.GONE
        }

        // Icône commentaire
        binding.imageViewComments.setOnClickListener {
            onClick(post, false)
            binding.layoutReactions.visibility = View.GONE
        }
        binding.postNoComments.setOnClickListener {
            onClick(post, false)
            binding.layoutReactions.visibility = View.GONE
        }

        val noCommentsText = ""
        val spannableNoCommentsText = SpannableString(noCommentsText)
        spannableNoCommentsText.setSpan(
            UnderlineSpan(), 0, noCommentsText.length, Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        binding.postNoComments.text = spannableNoCommentsText

        binding.postCommentsNumber.setOnClickListener {
            onClick(post, false)
            binding.layoutReactions.visibility = View.GONE
        }

        // Affichage du contenu en HTML
        var contentToShow = post.contentHtml
        if (post.contentTranslationsHtml != null) {
            contentToShow = if (isTranslated) {
                post.contentTranslationsHtml.original
            } else {
                post.contentTranslationsHtml.translation
            }
        }
        // On applique displayHtml
        binding.postMessage.visibility = if (contentToShow.isNullOrEmpty()) View.GONE else View.VISIBLE
        binding.postMessage.displayHtml(contentToShow ?: "")

        // Gestion "no comments" / "comment count"
        if (post.hasComments == false) {
            binding.postNoComments.visibility = View.GONE
            binding.postCommentsNumber.visibility = View.GONE
        } else {
            binding.postCommentsNumber.visibility = View.VISIBLE
            binding.postNoComments.visibility = View.GONE
            post.commentsCount?.let { cc ->
                val resId = if (cc > 1) R.string.posts_comment_number else R.string.posts_comment_number_singular
                val commentsText = String.format(context.getString(resId), cc)
                val spannableString = SpannableString(commentsText)
                spannableString.setSpan(
                    UnderlineSpan(),
                    0,
                    commentsText.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                )
                binding.postCommentsNumber.text = spannableString
            }
        }

        // Date
        post.createdTime?.let {
            val locale = LanguageManager.getLocaleFromPreferences(context)
            binding.date.text = SimpleDateFormat(
                context.getString(R.string.post_date),
                locale
            ).format(it)
        }

        // Image éventuelle dans le post
        if (!post.imageUrl.isNullOrEmpty()) {
            binding.photoPost.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                .placeholder(R.drawable.new_group_illu)
                .error(R.drawable.new_group_illu)
                .into(binding.photoPost)
            binding.photoPost.setOnClickListener {
                onClickImage(post.imageUrl ?: "", post.id ?: 0)
                binding.layoutReactions.visibility = View.GONE
            }
        } else {
            binding.photoPost.visibility = View.GONE
        }

        // Avatar user
        Glide.with(holder.itemView.context)
            .load(post.user?.avatarURLAsString ?: R.drawable.placeholder_user)
            .placeholder(R.drawable.placeholder_user)
            .error(R.drawable.placeholder_user)
            .circleCrop()
            .into(binding.image)

        // Gérer roles (ambassador, admin, etc.)
        binding.tvAmbassador.visibility = View.VISIBLE
        val rolesStringBuilder = StringBuilder()
        post.user?.roles?.let { roles ->
            if (roles.isNotEmpty()) {
                rolesStringBuilder.append(roles[0])
            }
        }
        if (rolesStringBuilder.isEmpty()) {
            binding.tvAmbassador.visibility = View.GONE
        } else {
            binding.tvAmbassador.visibility = View.VISIBLE
            binding.tvAmbassador.text = rolesStringBuilder.toString()
        }
        if (rolesStringBuilder.toString().contains("Association")) {
            binding.tvAmbassador.text = post.user?.partner?.name
        }
        // Clic profil
        binding.name.setOnClickListener {
            showUserDetail(binding.name.context, post.user?.userId)
            binding.layoutReactions.visibility = View.GONE
        }
        binding.name.text = post.user?.displayName
        binding.image.setOnClickListener {
            showUserDetail(binding.image.context, post.user?.userId)
            binding.layoutReactions.visibility = View.GONE
        }

        // Report
        binding.btnReportPost.setOnClickListener {
            binding.layoutReactions.visibility = View.GONE
            val userId = post.user?.id?.toInt()
            if (userId != null) {
                post.id?.let { postId -> onReport(postId, userId) }
            }
        }

        // --------------------------------------------------------------------
        // AJOUT MANQUANT : gérant "deleted" / "offensive" / "offensible"
        // --------------------------------------------------------------------
        if (
            post.status == "deleted" ||
            post.status == "offensive" ||
            post.status == "offensible"
        ) {
            binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
            when (post.status) {
                "offensive", "offensible" ->
                    binding.postMessage.text = context.getText(R.string.offensive_message)
                else -> // "deleted"
                    binding.postMessage.text = context.getText(R.string.deleted_publi)
            }
            binding.postMessage.setTextColor(context.getColor(R.color.deleted_grey))
            binding.postMessage.visibility = View.VISIBLE
            // On désactive tout un tas d’éléments
            binding.btnReportPost.visibility = View.GONE
            binding.btnsLayout.visibility = View.GONE
            binding.separatorLayout.visibility = View.GONE
            reactionImageViews.forEach { it.visibility = View.GONE }
            binding.numberReaction.visibility = View.GONE
        }
        // --------------------------------------------------------------------

        // Désactiver les clics de réaction si l’utilisateur n’est pas membre
        if (isMember == false) {
            binding.btnReportPost.setOnClickListener {
                reactionCallback.onReactionClicked(post, 0)
            }
            binding.btnILike.setOnClickListener {
                reactionCallback.onReactionClicked(post, 0)
            }
            binding.btnIComment.setOnClickListener {
                reactionCallback.onReactionClicked(post, 0)
            }
            binding.btnILike.setOnLongClickListener {
                reactionCallback.onReactionClicked(post, 0)
                true
            }
        }
    }

    // --------------------------------
    // Gestion du nombre de votes (pour sondage)
    // --------------------------------
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

    // --------------------------------
    // Gérer le changement d’état local (résumé sondage)
    // --------------------------------
    fun adjustSurveyTotalsBasedOnLocalState(
        survey: Survey,
        localState: MutableList<Boolean>,
        previousLocalState: MutableList<Boolean>
    ): List<Int> {
        val adjustedSummary = survey.summary.toMutableList()
        localState.forEachIndexed { index, isSelected ->
            val wasSelected = previousLocalState[index]
            if (isSelected && !wasSelected) {
                adjustedSummary[index] = adjustedSummary[index] + 1
            } else if (!isSelected && wasSelected) {
                adjustedSummary[index] = (adjustedSummary[index] - 1).coerceAtLeast(0)
            }
        }
        previousLocalState.clear()
        previousLocalState.addAll(localState)
        return adjustedSummary
    }

    // --------------------------------
    // Animations
    // --------------------------------
    private fun animateReactionLayout(reactionViews: List<ImageView>) {
        reactionViews.forEachIndexed { index, imageView ->
            imageView.visibility = View.VISIBLE
            imageView.scaleX = 0f
            imageView.scaleY = 0f
            imageView.animate()
                .scaleX(1f)
                .scaleY(1f)
                .translationY(-10f)
                .setStartDelay(index * 100L)
                .setDuration(300L)
                .withEndAction {
                    imageView.animate()
                        .translationY(0f)
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
                        layoutReactions.visibility = View.GONE
                        onAnimationEnd()
                    }
                    .start()
            }
            .start()
    }

    // --------------------------------
    // Gestion des réactions
    // --------------------------------
    private fun handleReactionClick(post: Post, reactionType: ReactionType) {
        val currentUserReactionId = post.reactionId ?: 0
        val isSameReaction = (currentUserReactionId == reactionType.id)

        if (currentUserReactionId != 0) {
            val existingReaction = post.reactions?.find { it.reactionId == currentUserReactionId }
            if (isSameReaction) {
                // Supprimer la réaction
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
                // On supprime d’abord l’ancienne côté serveur
                reactionCallback.deleteReaction(post)
                // Puis un petit délai avant d’ajouter la nouvelle
                Handler(Looper.getMainLooper()).postDelayed({
                    addOrUpdateReaction(post, reactionType)
                    reactionCallback.onReactionClicked(post, reactionType.id)
                    notifyItemChanged(postsList.indexOf(post))
                }, 500)
            }
        } else {
            // Première réaction
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
            existingReaction.reactionsCount++
        } else {
            val newReaction = Reaction().apply {
                reactionId = reactionType.id
                reactionsCount = 1
            }
            post.reactions?.add(newReaction)
        }
    }

    private fun showUserDetail(context: Context, userId: Int?) {
        ProfileFullActivity.isMe = false
        userId?.let {
            ProfileFullActivity.userId = it.toString()
            (context as? Activity)?.startActivity(
                Intent(context, ProfileFullActivity::class.java)
                    .putExtra(Const.USER_ID, it)
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            )
        }
    }

    override fun getItemCount(): Int {
        return postsList.size
    }
}
