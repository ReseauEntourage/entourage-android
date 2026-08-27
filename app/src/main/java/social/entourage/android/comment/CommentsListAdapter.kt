package social.entourage.android.comment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.Post
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.tools.utils.Const
import timber.log.Timber
import java.text.SimpleDateFormat

enum class CommentsTypes(val code: Int) {
    TYPE_LEFT(0),
    TYPE_RIGHT(1),
    TYPE_DATE(2),
    TYPE_DETAIL(3)
}

interface OnItemClickListener {
    fun onItemClick(comment: Post)
    fun onCommentReport(commentId: Int?, isForEvent: Boolean, isForGroup: Boolean, isMe: Boolean, commentLang: String)
    fun onShowWeb(url: String) // si tu veux ouvrir un navigateur ou gérer autrement
    fun onMessageLongPress(comment: Post, isMe: Boolean)
}

/**
 * Rendu des messages/commentaires en Jetpack Compose (chaque ViewHolder est une
 * ComposeView unique). Remplace l'ancien rendu ConstraintLayout XML (layout_comment_item_left/
 * right/date/detail_post_top.xml), source récurrente de bugs d'alignement — la logique de
 * liste (types d'item, pagination, traduction) reste RecyclerView classique, seul le contenu
 * de chaque item change de moteur de rendu.
 */
class CommentsListAdapter(
    var context: Context,
    private var commentsList: List<Post>,
    private var postAuthorId: Int,
    var isOne2One: Boolean,
    var isConversation: Boolean,
    private var currentParentPost: Post?,
    var onItemClick: OnItemClickListener,
) : RecyclerView.Adapter<CommentsListAdapter.ViewHolder>() {

    var isForEvent: Boolean = false
    var isForGroup: Boolean = false
    var allowsReactions: Boolean = false

    // Id du message ciblé par un deep link de notification, à mettre brièvement en évidence
    // une fois scrollé en vue (cf. CommentActivity.highlightCommentAt). Remis à null par
    // l'activité une fois l'effet de mise en évidence terminé.
    var highlightedMessageId: Int? = null

    // Pour savoir si l'utilisateur veut la version traduite ou originale.
    // On inverse si l'ID est dans translationExceptions.
    private val translationExceptions = mutableSetOf<Int>()

    fun initiateList() {
        val translatedByDefault = EntourageApplication.get().sharedPreferences
            .getBoolean("translatedByDefault", true)
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

    fun setForGroup() {
        isForGroup = true
    }

    private fun findFirstMessagePosition(): Int {
        val total = itemCount
        for (i in 0 until total) {
            val vt = getItemViewType(i)
            if (vt == CommentsTypes.TYPE_LEFT.code || vt == CommentsTypes.TYPE_RIGHT.code) {
                return i
            }
        }
        return total
    }

    fun translateItem(commentId: Int) {
        if (translationExceptions.contains(commentId)) {
            translationExceptions.remove(commentId)
        } else {
            translationExceptions.add(commentId)
        }
        notifyItemChanged(commentsList.indexOfFirst { it.id == commentId } + 1)
    }

    inner class ViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val composeView = ComposeView(parent.context).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool)
        }
        return ViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val comment = if (position == 0 && hasCurrentPost()) {
            currentParentPost
        } else {
            commentsList[if (hasCurrentPost()) (position - 1) else position]
        } ?: return

        when (getItemViewType(position)) {
            CommentsTypes.TYPE_DETAIL.code -> holder.composeView.setContent {
                val isDeleted = comment.status.equals("deleted")
                ParentPostHeaderItem(
                    comment = comment,
                    isDeleted = isDeleted,
                    deletedLabel = context.getString(R.string.deleted_publi),
                    dateText = comment.createdTime?.let {
                        "le " + SimpleDateFormat("dd.MM.yyyy", LanguageManager.getLocaleFromPreferences(context)).format(it)
                    }
                )
            }

            CommentsTypes.TYPE_LEFT.code, CommentsTypes.TYPE_RIGHT.code -> holder.composeView.setContent {
                bindMessage(comment)
            }

            CommentsTypes.TYPE_DATE.code -> holder.composeView.setContent {
                val firstMsgPos = findFirstMessagePosition()
                if (position >= firstMsgPos - 1) {
                    DateSeparatorItem(text = comment.datePostText)
                }
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun bindMessage(comment: Post) {
        val isMe = (comment.user?.userId == EntourageApplication.get().me()?.id)
        val isDeletedOrOffensive = comment.status in listOf("deleted", "offensive", "offensible")

        val sharedPrefs = EntourageApplication.get().sharedPreferences
        val isTranslatedByDefault = sharedPrefs.getBoolean("translatedByDefault", true)
        val isTranslated = if (translationExceptions.contains(comment.id)) !isTranslatedByDefault else isTranslatedByDefault
        val contentToShow = getFinalContent(comment, isTranslated).ifEmpty { "" }

        val deletedLabel = when {
            comment.status == "offensive" || comment.status == "offensible" -> context.getString(R.string.offensive_message)
            isOne2One || DetailConversationActivity.isSmallTalkMode -> context.getString(R.string.deleted_message)
            else -> context.getString(R.string.deleted_comment)
        }

        val locale = LanguageManager.getLocaleFromPreferences(context)
        val dateText = comment.createdTime?.let {
            if (isConversation) SimpleDateFormat("HH'h'mm", locale).format(it)
            else "le " + SimpleDateFormat(context.getString(R.string.comments_date), locale).format(it)
        }

        MessageBubbleItem(
            comment = comment,
            isMe = isMe,
            isConversation = isConversation,
            isHighlighted = comment.id != null && comment.id == highlightedMessageId,
            allowsReactions = allowsReactions,
            displayName = if (isMe) "" else (comment.user?.displayName ?: ""),
            contentHtml = contentToShow,
            isDeletedOrOffensive = isDeletedOrOffensive,
            deletedOrOffensiveLabel = deletedLabel,
            dateText = dateText,
            showReportIcon = !isMe && !isConversation,
            onAvatarClick = { openProfile(comment) },
            onLongPress = { onItemClick.onMessageLongPress(comment, isMe) },
            onImageClick = { openImageZoom(comment) },
            onReportClick = {
                val commentLang = comment.contentTranslations?.fromLang ?: ""
                DataLanguageStock.updateContentToCopy(comment.content ?: "")
                onItemClick.onCommentReport(comment.id, isForEvent, isForGroup, isMe, commentLang)
            },
            onLinkClick = { url -> onItemClick.onShowWeb(url) },
            onRetryClick = { onItemClick.onItemClick(comment) },
            reactions = comment.reactions ?: emptyList(),
            reactionTypes = MainActivity.reactionsList ?: emptyList(),
        )
    }

    private fun openProfile(comment: Post) {
        val userId = comment.user?.userId ?: return
        (context as? Activity)?.startActivityForResult(
            Intent(context, ProfileFullActivity::class.java).putExtra(Const.USER_ID, userId), 0
        )
    }

    private fun openImageZoom(comment: Post) {
        context.startActivity(
            Intent(context, ImageZoomActivity::class.java).putExtra("image_url", comment.imageUrl)
        )
    }

    fun updateData(currentParentPost: Post?) {
        this.currentParentPost = currentParentPost
        notifyDataSetChanged()
    }

    private fun hasCurrentPost(): Boolean = currentParentPost != null

    override fun getItemCount(): Int {
        val addOne = if (hasCurrentPost()) 1 else 0
        return commentsList.size + addOne
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0 && hasCurrentPost()) {
            return CommentsTypes.TYPE_DETAIL.code
        }
        val realPos = if (hasCurrentPost()) position - 1 else position

        if (commentsList[realPos].isDatePostOnly) {
            return CommentsTypes.TYPE_DATE.code
        }
        return if (commentsList[realPos].user?.id?.toInt() == postAuthorId) {
            CommentsTypes.TYPE_RIGHT.code
        } else {
            CommentsTypes.TYPE_LEFT.code
        }
    }

    // --------------------------------------------------------------------------------------------
    // Fonction utilitaire pour renvoyer la chaîne la plus adaptée (HTML ou normal,
    // original ou traduction). On en profite pour nettoyer les <p> qui rajoutent
    // des sauts de ligne non désirés.
    // --------------------------------------------------------------------------------------------
    private fun getFinalContent(comment: Post, isTranslated: Boolean): String {
        Timber.d("getFinalContent => isTranslated=$isTranslated contentHtml='${comment.contentHtml}' content='${comment.content}'")

        val baseString = if (!isTranslated) {
            if (!comment.contentHtml.isNullOrBlank()) comment.contentHtml else comment.content
        } else {
            val htmlTranslation = comment.contentTranslationsHtml?.translation
            if (!htmlTranslation.isNullOrBlank()) {
                htmlTranslation
            } else {
                val normalTranslation = comment.contentTranslations?.translation
                if (!normalTranslation.isNullOrBlank()) normalTranslation else comment.contentHtml
            }
        } ?: ""

        return fixHtmlSpacing(baseString)
    }

    // --------------------------------------------------------------------------------------------
    // Remplace les balises <p> ... </p> pour éviter les énormes sauts de ligne.
    // --------------------------------------------------------------------------------------------
    private fun fixHtmlSpacing(html: String): String {
        var result = html.trim()
        result = result.replace(Regex("<p[^>]*>"), "<br>")
        result = result.replace(Regex("</p>"), "")
        result = result.replace("\n", "<br>")
        // NB: replaceFirst(String, String) fait un remplacement littéral, pas une regex —
        // avec des String bruts ici ça ne matchait jamais rien (bug historique). Il faut
        // passer un Regex explicitement pour que le nettoyage des <br> de tête/fin marche.
        result = result.replace(Regex("^(<br>\\s*)+"), "")
        result = result.replace(Regex("(<br>\\s*)+$"), "")
        return result
    }
}
