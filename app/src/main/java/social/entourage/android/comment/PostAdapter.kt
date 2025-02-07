package social.entourage.android.comment

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.*
import social.entourage.android.databinding.NewLayoutPostBinding
import social.entourage.android.databinding.SurveyLayoutBinding
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.survey.ResponseSurveyActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.setHyperlinkClickable
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

/**
 * Adapter pour afficher soit un "Post" (TYPE_POST),
 * soit un "Survey" (TYPE_SURVEY).
 */
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

    /**
     * Appelée pour initialiser la liste et marquer
     * les posts qui ont "contentTranslations" si
     * la config "translatedByDefault" est true.
     */
    fun initiateList() {
        val translatedByDefault = context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
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
        if (index != -1) {
            postsList.removeAt(index)
            notifyItemRemoved(index)
            notifyItemRangeChanged(index, postsList.size)
        }
    }

    /**
     * Inverse l'état "traduction" d'un Post
     */
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

            // --------------------------------------------------------------------------
            // TYPE_SURVEY
            // --------------------------------------------------------------------------
            TYPE_SURVEY -> {
                val surveyHolder = holder as SurveyViewHolder
                val post = postsList[position]

                if (post.survey?.multiple == true) {
                    surveyHolder.binding.tvSelectAnswer.text =
                        context.getString(R.string.title_switch_mutiples_choices)
                } else {
                    surveyHolder.binding.tvSelectAnswer.text =
                        context.getString(R.string.title_switch_single_choice)
                }

                val localState = localSurveyResponseState.getOrPut(post.id ?: position) {
                    post.surveyResponse
                        ?: post.survey?.choices?.map { false }?.toMutableList()
                        ?: mutableListOf()
                }

                surveyHolder.binding.btnReportPost.setOnClickListener {
                    val userId = post.user?.id?.toInt()
                    if (userId != null) {
                        post.id?.let { it1 -> onReport(it1, userId) }
                    }
                }

                // Date
                post.createdTime?.let {
                    val locale = LanguageManager.getLocaleFromPreferences(context)
                    surveyHolder.binding.date.text = SimpleDateFormat(
                        surveyHolder.itemView.context.getString(R.string.post_date),
                        locale
                    ).format(it)
                }

                // Tag "ambassador"/"admin"/partner
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

                val localSummary = post.survey?.summary?.toMutableList() ?: mutableListOf()

                // Affichage user
                surveyHolder.binding.name.text = post.user?.displayName ?: "Nom inconnu"
                Glide.with(context)
                    .load(post.user?.avatarURLAsString ?: R.drawable.placeholder_user)
                    .placeholder(R.drawable.placeholder_user)
                    .error(R.drawable.placeholder_user)
                    .circleCrop()
                    .into(surveyHolder.binding.image)

                // Question
                surveyHolder.binding.surveyQuestion.text = post.content ?: "Question non disponible"

                // Gère la liste "choices"
                val survey = post.survey ?: return
                val choicesViews = listOf(
                    surveyHolder.binding.choiceOne,
                    surveyHolder.binding.choiceTwo,
                    surveyHolder.binding.choiceThree,
                    surveyHolder.binding.choiceFour,
                    surveyHolder.binding.choiceFive
                )
                choicesViews.forEachIndexed { index, choiceBinding ->
                    if (index >= survey.choices.size) {
                        choiceBinding.parent.visibility = View.GONE
                    }
                }

                // On set la data pour chaque choice
                survey.choices.forEachIndexed { index, choice ->
                    val bindingChoice = when (index) {
                        0 -> surveyHolder.binding.choiceOne
                        1 -> surveyHolder.binding.choiceTwo
                        2 -> surveyHolder.binding.choiceThree
                        3 -> surveyHolder.binding.choiceFour
                        4 -> surveyHolder.binding.choiceFive
                        else -> null
                    }
                    bindingChoice?.let { c ->
                        c.parent.visibility = View.VISIBLE
                        c.tvQuestionSurvey.text = choice
                        val isSelected = localState.getOrNull(index) ?: false
                        c.uiIvCheck.isChecked = isSelected
                        val choiceResponses = localSummary.getOrNull(index) ?: 0
                        val totalResponses = localSummary.sum()
                        val progress =
                            if (totalResponses > 0) (choiceResponses * 100 / totalResponses) else 0
                        c.progressBar3.progress = progress
                        c.tvNumberAnswer.text = "$choiceResponses"

                        // Clic sur un choix
                        c.parent.setOnClickListener {
                            val wasSelected = localState[index]
                            if (wasSelected) {
                                surveyCallback.onDeleteSurveyClick(post.id!!, localState)
                                survey.summary[index] = survey.summary[index] - 1
                            } else {
                                survey.summary[index] = survey.summary[index] + 1
                            }
                            if (survey.multiple) {
                                localState[index] = !wasSelected
                                if (localState[index]) {
                                    localSummary[index] =
                                        localSummary.getOrNull(index)?.plus(1) ?: 1
                                } else {
                                    localSummary[index] = (localSummary.getOrNull(index)?.minus(1))
                                        ?.coerceAtLeast(0) ?: 0
                                }
                            } else {
                                val previouslySelectedIndex = localState.indexOf(true)
                                if (previouslySelectedIndex != -1) {
                                    localSummary[previouslySelectedIndex] =
                                        (localSummary[previouslySelectedIndex] - 1).coerceAtLeast(0)
                                    localState[previouslySelectedIndex] = false
                                }
                                localState.fill(false)
                                localState[index] = true
                                localSummary[index] =
                                    localSummary.getOrNull(index)?.plus(1) ?: 1
                            }
                            surveyCallback.onSurveyOptionClicked(post.id!!, localState)
                            survey.choices.forEachIndexed { choiceIndex, _ ->
                                updateSurveyUI(
                                    surveyHolder,
                                    choiceIndex,
                                    survey,
                                    localState,
                                    localSummary
                                )
                            }

                            val totalResp = localSummary.sum()
                            if (post.commentsCount != null) {
                                val resId = if (totalResp > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                                val commentsText = " - " + String.format(
                                    surveyHolder.itemView.context.getString(resId),
                                    totalResp
                                )
                                val spannableString = SpannableString(commentsText)
                                spannableString.setSpan(
                                    UnderlineSpan(),
                                    0,
                                    commentsText.length,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                                )
                                surveyHolder.binding.postVoteNumber.text = spannableString
                            } else {
                                val resId = if (totalResp > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                                val commentsText = String.format(
                                    surveyHolder.itemView.context.getString(resId),
                                    totalResp
                                )
                                val spannableString = SpannableString(commentsText)
                                spannableString.setSpan(
                                    UnderlineSpan(),
                                    0,
                                    commentsText.length,
                                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
                                )
                                surveyHolder.binding.postVoteNumber.text = spannableString
                            }
                            if (totalResp == 0) {
                                surveyHolder.binding.postVoteNumber.visibility = View.GONE
                            }
                            updateVoteNumberText(surveyHolder, localSummary)
                        }
                    }
                }

                // Statuts "deleted"/"offensive"
                if (post.status in listOf("deleted", "offensive", "offensible")) {
                    surveyHolder.binding.surveyQuestion.text =
                        context.getText(R.string.deleted_publi)
                    if (post.status in listOf("offensive", "offensible")) {
                        surveyHolder.binding.surveyQuestion.text =
                            context.getText(R.string.offensive_message)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        surveyHolder.binding.surveyQuestion.setTextColor(
                            context.getColor(R.color.deleted_grey)
                        )
                    }
                    surveyHolder.binding.surveyQuestion.visibility = View.VISIBLE
                    surveyHolder.binding.btnReportPost.visibility = View.GONE
                    surveyHolder.binding.choiceOne.parent.visibility = View.GONE
                    surveyHolder.binding.choiceTwo.parent.visibility = View.GONE
                    surveyHolder.binding.choiceThree.parent.visibility = View.GONE
                    surveyHolder.binding.choiceFour.parent.visibility = View.GONE
                    surveyHolder.binding.choiceFive.parent.visibility = View.GONE
                }

                // Boutons iLike/comment
                surveyHolder.binding.tvTitleILike.setText(context.getText(R.string.text_title_i_like))
                surveyHolder.binding.tvIComment.setText(context.getText(R.string.text_title_comment))

                surveyHolder.binding.surveyLayout.setOnClickListener {
                    surveyHolder.binding.layoutReactions.visibility = View.GONE
                }
                surveyHolder.binding.btnILike.setOnLongClickListener {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Post_List_Reactions)
                    surveyHolder.binding.layoutReactions.visibility =
                        if (surveyHolder.binding.layoutReactions.visibility == View.VISIBLE)
                            View.GONE
                        else
                            View.VISIBLE
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

                // Réactions
                with(post) {
                    if (reactionId == null) {
                        surveyHolder.binding.ivILike.setImageDrawable(
                            context.getDrawable(R.drawable.ic_pouce_grey)
                        )
                        surveyHolder.binding.tvTitleILike.setTextColor(
                            context.getColor(R.color.black)
                        )
                    } else if (reactionId != 0) {
                        surveyHolder.binding.ivILike.setImageDrawable(
                            context.getDrawable(R.drawable.ic_pouce_orange)
                        )
                        surveyHolder.binding.tvTitleILike.setTextColor(
                            context.getColor(R.color.orange)
                        )
                    } else {
                        surveyHolder.binding.ivILike.setImageDrawable(
                            context.getDrawable(R.drawable.ic_pouce_grey)
                        )
                        surveyHolder.binding.tvTitleILike.setTextColor(
                            context.getColor(R.color.black)
                        )
                    }
                    surveyHolder.binding.btnILike.setOnClickListener {
                        val firstReactionType = MainActivity.reactionsList?.firstOrNull()
                        if (firstReactionType != null) {
                            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Post_Like)
                            surveyHolder.binding.layoutReactions.visibility = View.GONE
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
                            Glide.with(context).load(reactionType.imageUrl)
                                .into(reactionImageViews[index])
                            reactionImageViews[index].setOnClickListener {
                                animateBubbleEffect(
                                    reactionImageViews[index],
                                    surveyHolder.binding.layoutReactions
                                ) {
                                    handleReactionClick(post, reactionType)
                                    surveyHolder.binding.layoutReactions.visibility = View.GONE
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
                    reactionsLayouts.forEach { it.visibility = View.GONE }

                    reactions?.forEachIndexed { index, r ->
                        if (index < reactionsLayouts.size) {
                            val reactionType = reactionTypes?.find { it.id == r.reactionId }
                            reactionType?.let {
                                Glide.with(context)
                                    .load(it.imageUrl)
                                    .into(reactionViews[index].image)
                                reactionsLayouts[index].visibility = View.VISIBLE
                            }
                        }
                    }
                    reactionViews.forEach { it.layoutItemReactionParent.visibility = View.GONE }
                    val totalReactionsCount = reactions?.sumOf { it.reactionsCount } ?: 0
                    surveyHolder.binding.numberReaction.text = totalReactionsCount.toString()
                    surveyHolder.binding.numberReaction.visibility =
                        if (totalReactionsCount > 0) View.VISIBLE else View.GONE

                    if (reactions.isNullOrEmpty() && commentsCount == 0 && survey.summary.isEmpty()) {
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
                    surveyHolder.binding.postCommentsNumberLayout.setOnClickListener { }

                    if (hasComments == false) {
                        surveyHolder.binding.postNoComments.visibility = View.GONE
                        surveyHolder.binding.postCommentsNumber.visibility = View.GONE
                    } else {
                        surveyHolder.binding.postCommentsNumber.visibility = View.VISIBLE
                        surveyHolder.binding.postNoComments.visibility = View.GONE
                        if (commentsCount != null) {
                            val resId =
                                if (commentsCount > 1) R.string.posts_comment_number else R.string.posts_comment_number_singular
                            val commentsText = String.format(
                                surveyHolder.itemView.context.getString(resId),
                                commentsCount
                            )
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
                    surveyHolder.binding.postCommentsNumber?.setOnClickListener {
                        onClick(this, true)
                    }

                    // S'il n'y a pas de réponses, on masque "VoteNumber"
                    if (survey.summary.isEmpty()) {
                        surveyHolder.binding.postVoteNumber.visibility = View.GONE
                        surveyHolder.binding.postCommentsNumber.visibility = View.GONE
                    } else {
                        surveyHolder.binding.postVoteNumber?.setOnClickListener {
                            ResponseSurveyActivity.myVote.clear()
                            ResponseSurveyActivity.myVote = localState.toMutableList()
                            surveyCallback.showParticipantWhoVote(
                                post.survey,
                                post.id!!,
                                post.content!!
                            )
                        }
                        surveyHolder.binding.postVoteNumber.visibility = View.VISIBLE
                        surveyHolder.binding.postNoComments.visibility = View.GONE
                        val totalResponses = survey.summary.sum()
                        if (commentsCount != null && commentsCount != 0) {
                            val resId =
                                if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                            val commentsText = " - " + String.format(
                                surveyHolder.itemView.context.getString(resId),
                                totalResponses
                            )
                            val spannableString = SpannableString(commentsText)
                            spannableString.setSpan(
                                UnderlineSpan(),
                                0,
                                commentsText.length,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE
                            )
                            surveyHolder.binding.postVoteNumber.text = spannableString
                        } else {
                            val resId =
                                if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
                            val commentsText = String.format(
                                surveyHolder.itemView.context.getString(resId),
                                totalResponses
                            )
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

            // --------------------------------------------------------------------------
            // TYPE_POST
            // --------------------------------------------------------------------------
            TYPE_POST -> {
                val binding = (holder as ViewHolder).binding
                val meId = EntourageApplication.get().me()?.id

                binding.tvTitleILike.setText(context.getText(R.string.text_title_i_like))
                binding.tvIComment.setText(context.getText(R.string.text_title_comment))

                binding.layoutPostParent.setOnClickListener {
                    binding.layoutReactions.visibility = View.GONE
                }

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

                val post = postsList[position]
                with(post) {
                    // autoPostFrom
                    if (this.autoPostFrom != null) {
                        binding.layoutActionInPubli.visibility = View.VISIBLE
                    } else {
                        binding.layoutActionInPubli.visibility = View.GONE
                    }

                    // Gère la reactionId
                    if (this.reactionId == null) {
                        binding.ivILike.setImageDrawable(
                            context.getDrawable(R.drawable.ic_pouce_grey)
                        )
                        binding.tvTitleILike.setTextColor(
                            context.getColor(R.color.black)
                        )
                    } else if (this.reactionId != 0) {
                        binding.ivILike.setImageDrawable(
                            context.getDrawable(R.drawable.ic_pouce_orange)
                        )
                        binding.tvTitleILike.setTextColor(
                            context.getColor(R.color.orange)
                        )
                    } else {
                        binding.ivILike.setImageDrawable(
                            context.getDrawable(R.drawable.ic_pouce_grey)
                        )
                        binding.tvTitleILike.setTextColor(
                            context.getColor(R.color.black)
                        )
                    }

                    binding.btnILike.setOnClickListener {
                        VibrationUtil.vibrate(context)
                        val firstReactionType = MainActivity.reactionsList?.firstOrNull()
                        if (firstReactionType != null) {
                            AnalyticsEvents.logEvent(AnalyticsEvents.Clic_Post_Like)
                            binding.layoutReactions.visibility = View.GONE
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
                    reactionTypes?.take(5)?.forEachIndexed { index, reactionType ->
                        Glide.with(context).load(reactionType.imageUrl)
                            .into(reactionImageViews[index])
                        reactionImageViews[index].setOnClickListener {
                            animateBubbleEffect(
                                reactionImageViews[index],
                                binding.layoutReactions
                            ) {
                                handleReactionClick(this@with, reactionType)
                                binding.layoutReactions.visibility = View.GONE
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

                    reactions?.forEachIndexed { index, r ->
                        if (index < reactionsLayouts.size) {
                            val reactionType = reactionTypes?.find { it.id == r.reactionId }
                            reactionType?.let {
                                Glide.with(context)
                                    .load(it.imageUrl)
                                    .into(reactionViews[index].image)
                                reactionsLayouts[index].visibility = View.VISIBLE
                            }
                        }
                    }
                    reactionViews.forEach { it.layoutItemReactionParent.visibility = View.GONE }
                    val totalReactionsCount = reactions?.sumOf { it.reactionsCount } ?: 0
                    binding.numberReaction.text = totalReactionsCount.toString()
                    binding.numberReaction.visibility =
                        if (totalReactionsCount > 0) View.VISIBLE else View.GONE

                    if (reactions.isNullOrEmpty() && commentsCount == 0) {
                        binding.postCommentsNumberLayout.visibility = View.GONE
                    } else {
                        binding.postCommentsNumberLayout.visibility = View.VISIBLE
                    }
                    binding.btnIComment.setOnClickListener {
                        binding.layoutReactions.visibility = View.GONE
                        onClick(this@with, true)
                    }
                    binding.btnReaction.setOnClickListener {
                        AnalyticsEvents.logEvent(AnalyticsEvents.Clic_ListReactions_Contact)
                        binding.layoutReactions.visibility = View.GONE
                        reactionCallback.seeMemberReaction(this@with)
                    }
                    binding.postCommentsNumberLayout.setOnClickListener { }

                    // Long click => copier le message
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

                    // ---------------- GESTION TRADUCTION (TOGGLE) ----------------
                    val sp = context.getSharedPreferences(
                        context.getString(R.string.preference_file_key),
                        Context.MODE_PRIVATE
                    )
                    val translatedByDefault = sp.getBoolean("translatedByDefault", false)

                    // Est-ce que ce post est "en mode traduite" ou non ?
                    val isTranslated = !translationExceptions.contains(id)
                    val textTrans = if (isTranslated) {
                        context.getString(R.string.layout_translate_title_translation)
                    } else {
                        context.getString(R.string.layout_translate_title_original)
                    }
                    val titleButton = SpannableString(textTrans)
                    titleButton.setSpan(UnderlineSpan(), 0, textTrans.length, 0)
                    binding.postTranslationButton.tvTranslate.text = titleButton

                    // Gérer l'apparition du bouton "Traduction" :
                    //  1) S'il n'y a pas de contentTranslations => pas de bouton
                    //  2) Si userLanguage == contentTranslations.fromLang => pas besoin
                    //  3) Si "translatedByDefault" est activé => on masque le bouton
                    val hasTranslation = this@with.contentTranslations?.translation?.isNotBlank() == true
                    val sameLang =
                        (DataLanguageStock.userLanguage == this@with.contentTranslations?.fromLang)

                    if (!hasTranslation) {
                        // Pas de traduction => on cache
                        binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
                    } else {
                        if (sameLang) {
                            // Si c'est la même langue => inutile
                            binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
                        } else {
                            // Si "auto-translation" => on cache le bouton
                            if (translatedByDefault) {
                                binding.postTranslationButton.layoutCsTranslate.visibility = View.GONE
                            } else {
                                // Sinon, on affiche le bouton
                                binding.postTranslationButton.layoutCsTranslate.visibility = View.VISIBLE
                            }
                        }
                    }

                    // Au clic : toggler la traduction
                    binding.postTranslationButton.layoutCsTranslate.setOnClickListener {
                        translateItem(id ?: this@with.id!!)
                        binding.layoutReactions.visibility = View.GONE
                    }

                    // Ouvre la liste de commentaires
                    binding.imageViewComments.setOnClickListener {
                        onClick(this@with, false)
                        binding.layoutReactions.visibility = View.GONE
                    }
                    binding.postNoComments.setOnClickListener {
                        onClick(this@with, false)
                        binding.layoutReactions.visibility = View.GONE
                    }

                    // hasComments
                    val noCommentsText = ""
                    val spannableNoCommentsText = SpannableString(noCommentsText)
                    spannableNoCommentsText.setSpan(
                        UnderlineSpan(),
                        0,
                        noCommentsText.length,
                        Spanned.SPAN_INCLUSIVE_INCLUSIVE
                    )
                    binding.postNoComments.text = spannableNoCommentsText

                    binding.postCommentsNumber.setOnClickListener {
                        onClick(this@with, false)
                        binding.layoutReactions.visibility = View.GONE
                    }

                    // --------------- GESTION DU CONTENU HTML + LIENS ---------------
                    var finalContent = getFinalContentForPost(this@with, isTranslated)
                    if (finalContent.isNullOrEmpty()) finalContent = ""

                    // On supprime les <p> qui ajoutent des sauts de ligne
                    finalContent = fixHtmlSpacing(finalContent)

                    // On parse en HTML
                    val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(finalContent, Html.FROM_HTML_MODE_LEGACY)
                    } else {
                        Html.fromHtml(finalContent)
                    }
                    // Rends les liens (URLSpan) en ClickableSpan custom
                    val clickableSpannable = makeLinksClickable(spanned)

                    binding.postMessage.text = clickableSpannable
                    binding.postMessage.movementMethod = LinkMovementMethod.getInstance()
                    // Couleur des liens (sinon ils restent noirs)
                    binding.postMessage.setLinkTextColor(
                        ContextCompat.getColor(context, R.color.bright_blue)
                    )

                    // ---------------------------------------------------------------

                    if (hasComments == false) {
                        binding.postNoComments.visibility = View.GONE
                        binding.postCommentsNumber.visibility = View.GONE
                    } else {
                        binding.postCommentsNumber.visibility = View.VISIBLE
                        binding.postNoComments.visibility = View.GONE
                        if (commentsCount != null) {
                            val resId =
                                if (commentsCount > 1) R.string.posts_comment_number else R.string.posts_comment_number_singular
                            val commentsText = String.format(
                                binding.root.context.getString(resId),
                                commentsCount
                            )
                            val spannableString = SpannableString(commentsText)
                            spannableString.setSpan(
                                UnderlineSpan(),
                                0,
                                commentsText.length,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE
                            )
                            binding.postCommentsNumber.text = spannableString
                        } else {
                            binding.postCommentsNumber.text = ""
                        }
                    }

                    // Date
                    val locale = LanguageManager.getLocaleFromPreferences(context)
                    binding.date.text = SimpleDateFormat(
                        binding.root.context.getString(R.string.post_date),
                        locale
                    ).format(createdTime)

                    // imageUrl => illu
                    this.imageUrl?.let { avatarURL ->
                        binding.photoPost.visibility = View.VISIBLE
                        Glide.with(holder.itemView.context)
                            .load(avatarURL)
                            .transform(CenterCrop(), RoundedCorners(Const.ROUNDED_CORNERS_IMAGES.px))
                            .placeholder(R.drawable.new_group_illu)
                            .error(R.drawable.new_group_illu)
                            .into(binding.photoPost)
                        binding.photoPost.setOnClickListener {
                            this@with.id?.let { postId ->
                                onClickImage(avatarURL, postId)
                                binding.layoutReactions.visibility = View.GONE
                            }
                        }
                    } ?: run {
                        binding.photoPost.visibility = View.GONE
                    }

                    // Avatar user
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

                    // Rôles (admin, ambassador, etc.)
                    binding.tvAmbassador.visibility = View.VISIBLE
                    val _post = this@with
                    var tagsString2 = ""
                    if (_post.user?.roles?.isNotEmpty() == true) {
                        _post.user?.roles?.let { roles ->
                            if (roles.isNotEmpty()) {
                                tagsString2 += roles[0]
                            }
                        }
                    }
                    if (tagsString2.isEmpty()) {
                        holder.binding.tvAmbassador.visibility = View.GONE
                    } else {
                        holder.binding.tvAmbassador.visibility = View.VISIBLE
                        if (tagsString2.endsWith("• ")) {
                            tagsString2 = tagsString2.dropLast(2)
                        }
                        holder.binding.tvAmbassador.text = tagsString2
                    }

                    binding.name.setOnClickListener {
                        showUserDetail(binding.name.context, this@with.user?.userId)
                        binding.layoutReactions.visibility = View.GONE
                    }
                    binding.name.text = user?.displayName
                    binding.image.setOnClickListener {
                        showUserDetail(binding.image.context, this@with.user?.userId)
                        binding.layoutReactions.visibility = View.GONE
                    }
                    binding.btnReportPost.setOnClickListener {
                        binding.layoutReactions.visibility = View.GONE
                        val userId = user?.id?.toInt()
                        if (userId != null) {
                            id?.let { it1 -> onReport(it1, userId) }
                        }
                    }

                    // Statut "deleted"/"offensive"
                    if (status in listOf("deleted", "offensive", "offensible")) {
                        binding.postMessage.text = context.getText(R.string.deleted_publi)
                        if (status in listOf("offensive", "offensible")) {
                            binding.postMessage.text = context.getText(R.string.offensive_message)
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            binding.postMessage.setTextColor(context.getColor(R.color.deleted_grey))
                        }
                        binding.postMessage.visibility = View.VISIBLE
                        binding.btnReportPost.visibility = View.GONE
                        binding.btnsLayout.visibility = View.GONE
                        binding.separatorLayout.visibility = View.GONE
                        reactionImageViews.forEach { it.visibility = View.GONE }
                        binding.numberReaction.visibility = View.GONE
                    } else {
                        binding.btnsLayout.visibility = View.VISIBLE
                        binding.separatorLayout.visibility = View.VISIBLE
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            binding.postMessage.setTextColor(context.getColor(R.color.black))
                        }
                        binding.postMessage.visibility = View.VISIBLE
                        binding.btnReportPost.visibility = View.VISIBLE
                    }

                    // Si pas membre, on désactive
                    if (isMember == false) {
                        binding.btnReportPost.setOnClickListener {
                            reactionCallback.onReactionClicked(this@with, 0)
                        }
                        binding.btnILike.setOnClickListener {
                            reactionCallback.onReactionClicked(this@with, 0)
                        }
                        binding.btnIComment.setOnClickListener {
                            reactionCallback.onReactionClicked(this@with, 0)
                        }
                        binding.btnILike.setOnLongClickListener {
                            reactionCallback.onReactionClicked(this@with, 0)
                            true
                        }
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

    // --------------------------------------------------------------------------------------------
    // Gestion Survey
    // --------------------------------------------------------------------------------------------
    fun updateVoteNumberText(
        surveyHolder: SurveyViewHolder,
        localSummary: MutableList<Int>
    ) {
        val totalResponses = localSummary.sum()
        val context = surveyHolder.itemView.context
        val resId = if (totalResponses > 1) R.string.posts_vote_number else R.string.posts_vote_number_singular
        val commentsText = String.format(context.getString(resId), totalResponses)
        val spannableString = SpannableString(commentsText)
        spannableString.setSpan(
            UnderlineSpan(),
            0,
            commentsText.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        surveyHolder.binding.postVoteNumber.text = spannableString
        if (totalResponses == 0) {
            surveyHolder.binding.postVoteNumber.visibility = View.GONE
        } else {
            surveyHolder.binding.postVoteNumber.visibility = View.VISIBLE
        }
    }

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

    // --------------------------------------------------------------------------------------------
    // Réactions
    // --------------------------------------------------------------------------------------------
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

    private fun handleReactionClick(post: Post, reactionType: ReactionType) {
        val currentUserReactionId = post.reactionId ?: 0
        val isSameReaction = (currentUserReactionId == reactionType.id)

        if (currentUserReactionId != 0) {
            val existingReaction = post.reactions?.find { it.reactionId == currentUserReactionId }
            if (isSameReaction) {
                // Retirer la réaction
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
                Handler(Looper.getMainLooper()).postDelayed({
                    addOrUpdateReaction(post, reactionType)
                    reactionCallback.onReactionClicked(post, reactionType.id)
                    notifyItemChanged(postsList.indexOf(post))
                }, 500)
            }
        } else {
            // Ajouter une première réaction
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

    // --------------------------------------------------------------------------------------------
    // Ouvre le détail d'un user
    // --------------------------------------------------------------------------------------------
    private fun showUserDetail(context: Context, userId: Int?) {
        ProfileFullActivity.isMe = false
        ProfileFullActivity.userId = userId.toString()
        (context as? Activity)?.startActivityForResult(
            Intent(context, ProfileFullActivity::class.java).putExtra(
                Const.USER_ID,
                userId
            ), 0
        )
    }

    // --------------------------------------------------------------------------------------------
    // Renvoie la chaîne finale (original vs traduction)
    // --------------------------------------------------------------------------------------------
    private fun getFinalContentForPost(post: Post, isTranslated: Boolean): String {
        return if (!isTranslated) {
            // Original
            post.contentHtml
                ?.takeIf { it.isNotBlank() }
                ?: post.content
                    ?.takeIf { it.isNotBlank() }
                ?: ""
        } else {
            // Traduction
            post.contentTranslationsHtml?.translation
                ?.takeIf { it.isNotBlank() }
                ?: post.contentTranslations?.translation
                    ?.takeIf { it.isNotBlank() }
                ?: ""
        }
    }

    // --------------------------------------------------------------------------------------------
    // Supprime les <p> et </p> qui provoquent des sauts de ligne inattendus
    // --------------------------------------------------------------------------------------------
    private fun fixHtmlSpacing(html: String): String {
        val withoutOpeningP = html.replace(Regex("<p[^>]*>"), "")
        val withoutClosingP = withoutOpeningP.replace("</p>", "")
        return withoutClosingP
    }

    // --------------------------------------------------------------------------------------------
    // Convertit un Spanned (avec URLSpan) en un SpannableStringBuilder (avec ClickableSpan)
    // pour gérer soi-même le clic (deeplink, navigateur, etc.).
    // --------------------------------------------------------------------------------------------
    private fun makeLinksClickable(spanned: Spanned): Spannable {
        val urlSpans = spanned.getSpans(0, spanned.length, URLSpan::class.java)
        if (urlSpans.isEmpty()) {
            // Pas de lien -> on renvoie le spanned d'origine
            return spanned as Spannable
        }
        val sb = SpannableStringBuilder(spanned)
        for (span in urlSpans) {
            val start = sb.getSpanStart(span)
            val end = sb.getSpanEnd(span)
            val flags = sb.getSpanFlags(span)
            val url = span.url
            // Retirer l'URLSpan
            sb.removeSpan(span)

            // Ajouter un ClickableSpan custom
            sb.setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    // ICI : gère le clic. Ex:
                    // -> Ouvrir un écran in-app selon l'URL
                    // -> Ouvrir un navigateur
                    // -> etc.
                    // Pour un deep link, parse 'url' et fais ce qu'il faut.
                    Toast.makeText(widget.context, "Lien cliqué : $url", Toast.LENGTH_SHORT).show()
                    // Ex : si on veut juste l'ouvrir dans un browser :
                    // onShowWeb(url)
                }

                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.isUnderlineText = true // si tu veux un soulignement
                }
            }, start, end, flags)
        }
        return sb
    }
}
