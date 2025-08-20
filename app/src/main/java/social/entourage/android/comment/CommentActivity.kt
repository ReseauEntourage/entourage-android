package social.entourage.android.comment

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Post
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.ActivityCommentsBinding
import social.entourage.android.deeplinks.UniversalLinkManager
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.report.onDissmissFragment
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.scrollToPositionSmooth
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.ui.ActionSheetFragment
import social.entourage.android.ui.SheetMode
import java.util.UUID
import kotlin.getValue

abstract class CommentActivity : BaseActivity(), onDissmissFragment {
lateinit var binding: ActivityCommentsBinding

var id = Const.DEFAULT_VALUE
var postId = Const.DEFAULT_VALUE
protected var postAuthorID = Const.DEFAULT_VALUE
protected var isMember = false
protected var titleName:String? = null
var commentsList: MutableList<Post> = mutableListOf()
var shouldOpenKeyboard = false
var messagesFailed: MutableList<Post?> = mutableListOf()
var comment: Post? = null
var isEvent = false
var isGroup = false
lateinit var viewModel: DiscussionsPresenter
var haveReloadFromDelete = false


protected var isOne2One = false
protected var isConversation = false
protected var isFromNotif = false
var currentParentPost:Post? = null
private val universalLinkManager = UniversalLinkManager(this)
var photoUri: Uri? = null
private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
private val smallTalkViewModel: SmallTalkViewModel by viewModels()
private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityCommentsBinding.inflate(layoutInflater)
    setContentView(binding.root)



    viewModel = ViewModelProvider(this).get(DiscussionsPresenter::class.java)
    id = intent.getIntExtra(Const.ID, Const.DEFAULT_VALUE)
    postId = intent.getIntExtra(Const.POST_ID, Const.DEFAULT_VALUE)
    postAuthorID = intent.getIntExtra(Const.POST_AUTHOR_ID, Const.DEFAULT_VALUE)
    isMember = intent.getBooleanExtra(Const.IS_MEMBER, false)
    titleName = intent.getStringExtra(Const.NAME)
    isOne2One = intent.getBooleanExtra(Const.IS_CONVERSATION_1TO1, false)
    isFromNotif = intent.getBooleanExtra(Const.IS_FROM_NOTIF, false)
    isConversation = intent.getBooleanExtra(Const.IS_CONVERSATION, false)
    shouldOpenKeyboard = intent.getBooleanExtra(Const.SHOULD_OPEN_KEYBOARD, false)
    viewModel.isMessageDeleted.observe(this,::handleMessageDeleted)
    initializeComments()
    handleCommentAction()
    openEditTextKeyboard()
    handleBackButton()
    setSettingsIcon()
    val postLang = comment?.contentTranslations?.fromLang ?: ""

    handleSendButtonState()

    updatePaddingTopForEdgeToEdge(binding.header.layout)
}

fun setIsEventTrue(){
    this.isEvent = true
}
fun setIsEventFalse(){
    this.isEvent = false
}

protected open fun handleGetPostComments(allComments: MutableList<Post>?) {
    commentsList.clear()
    allComments?.let { commentsList.addAll(it) }
    binding.progressBar.visibility = View.GONE
    allComments?.isEmpty()?.let { updateView(it) }
    scrollAfterLayout()
}

protected fun scrollAfterLayout() {
    binding.comments.viewTreeObserver
        .addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    binding.comments.scrollToPosition(commentsList.size - 1)
                    binding.comments.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
}

private fun handleMessageDeleted(isMessageDeleted:Boolean){

}

protected fun handleCommentPosted(post: Post?) {
    post?.let {
        commentsList.add(post)
    } ?: run {
        messagesFailed.add(comment)
        comment?.let { commentsList.add(it) }
    }
    binding.comments.scrollToPositionSmooth(commentsList.size)
    updateView(false)
}

fun updateView(emptyState: Boolean) {
    if (emptyState) {
        binding.emptyState.visibility = View.GONE
        binding.comments.visibility = if (currentParentPost != null) View.VISIBLE else View.GONE
    } else {
        binding.emptyState.visibility = View.GONE
        binding.comments.visibility = View.VISIBLE
        binding.comments.adapter?.notifyDataSetChanged()
    }
    if (isFromNotif){
        isMember = true
    }
    if (isMember) {
        binding.shouldBeMember.visibility = View.GONE
        binding.postComment.visibility = View.VISIBLE
    } else {
        binding.shouldBeMember.visibility = View.VISIBLE
        binding.shouldBeMember.text = String.format(
            getString(R.string.join_group_to_comment),
            titleName
        )
        binding.postComment.visibility = View.GONE
    }
}

    // CommentActivity.kt
    private fun reportComment(
        commentId: Int?,
        isForEvent: Boolean,
        isForGroup: Boolean,
        isMe: Boolean,
        commentLang: String,
        messageHtml: String? = null
    ) {
        commentId ?: return

        val (containerId, type) = when {
            isForEvent -> id to ReportTypes.REPORT_POST_EVENT
            isForGroup -> id to ReportTypes.REPORT_POST
            else       -> id to ReportTypes.REPORT_COMMENT
        }

        val plain = when {
            !messageHtml.isNullOrBlank() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ->
                Html.fromHtml(messageHtml, Html.FROM_HTML_MODE_LEGACY).toString()
            !messageHtml.isNullOrBlank() ->
                @Suppress("DEPRECATION") Html.fromHtml(messageHtml).toString()
            else -> ""
        }
        if (plain.isNotBlank()) DataLanguageStock.updateContentToCopy(plain)

        ReportModalFragment.newInstance(
            id = commentId,                       // ✅ reportedId = le message
            groupId = containerId,                // ✅ groupId = contexte (conv/groupe/event)
            reportType = type,
            isFromMe = isMe,
            isConv = !(isForEvent || isForGroup),
            isOneToOne = (isConversation && isOne2One),
            contentCopied = plain,
            openDirectSignal = true               // ✅ ouvre directement le step "signalement"
        ).show(supportFragmentManager, ReportModalFragment.TAG)
    }



    private fun initializeComments() {
        binding.comments.apply {
            layoutManager = LinearLayoutManager(context)
            val meId = EntourageApplication.get().me()?.id ?: postAuthorID
            adapter = CommentsListAdapter(
                context,
                commentsList,
                meId,
                isOne2One,
                isConversation,
                currentParentPost,
                object : OnItemClickListener {
                    override fun onItemClick(comment: Post) {
                        addComment()
                        commentsList.remove(comment)
                    }

                    override fun onCommentReport(
                        commentId: Int?,
                        isForEvent: Boolean,
                        isForGroup: Boolean,
                        isMe: Boolean,
                        commentLang: String
                    ) {
                        commentId ?: return
                        reportComment(commentId, isForEvent, isForGroup, isMe, commentLang, null)
                    }

                    override fun onShowWeb(url: String) {
                        WebViewFragment.newInstance(
                            if (!url.startsWith("http")) "https://$url" else url,
                            0,
                            true
                        ).show(supportFragmentManager, WebViewFragment.TAG)
                    }

                    override fun onMessageLongPress(comment: Post, isMe: Boolean) {
                        showMessageOptions(comment, isMe)
                    }
                }
            )
            (adapter as? CommentsListAdapter)?.initiateList()
        }
    }

    private fun showMessageOptions(comment: Post, isMe: Boolean) {
        val conversationId = if (isConversation) id else 0
        val groupId = if (isGroup) id else 0
        val eventId = if (isEvent) id else 0

        val sheet = ActionSheetFragment.newMessageActions(
            conversationId = conversationId,
            groupId = groupId,
            eventId = eventId,
            messageId = comment.id ?: 0,
            messageHtml = comment.content ?: comment.contentHtml,
            isMyMessage = isMe,
            isEventContext = isEvent,
            isGroupContext = isGroup
        )
        sheet.show(supportFragmentManager, "MessageActionsSheet")
    }






    private fun handleCommentAction() {
    binding.comment.setOnClickListener {
        // Convertir le contenu de l'EditText en HTML pour préserver les retours à la ligne
        val message = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(binding.commentMessage.text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
        } else {
            @Suppress("DEPRECATION")
            Html.toHtml(binding.commentMessage.text)
        }

        if (message.isNotBlank() || photoUri != null) {
            // Désactiver le bouton et afficher la progress bar
            binding.comment.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            // Créer l'utilisateur et le commentaire
            val user = EntourageUser().apply {
                userId = EntourageApplication.me(this@CommentActivity)?.id ?: 0
                avatarURLAsString = EntourageApplication.me(this@CommentActivity)?.avatarURL
            }
            comment = Post(
                idInternal = UUID.randomUUID(),
                content = message,
                postId = postId,
                imageUrl = photoUri?.toString(),
                user = user
            )

            // Envoi du commentaire
            addComment()

            // Simuler un délai de 2 secondes pour la réactivation du bouton
            binding.comment.postDelayed({
                binding.comment.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }, 2000)

            // Nettoyer le champ de saisie et cacher le clavier
            binding.commentMessage.text.clear()
            Utils.hideKeyboard(this)
        }
    }
}



private fun handleBackButton() {
    binding.header.iconBack.setOnClickListener {
        finish()
    }
}

private fun handleSendButtonState() {
    binding.commentMessage.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable) {
            binding.comment.background = ResourcesCompat.getDrawable(
                resources,
                if (s.isEmpty() || s.isBlank()) R.drawable.new_bg_rounded_inactive_button_light_orange
                else R.drawable.new_circle_orange_button_fill,
                null
            )
        }
    })
}

private fun setSettingsIcon() {
    binding.header.title = getString(R.string.comments_title)
    binding.header.iconSettings.isVisible = true
    binding.header.iconSettings.setImageResource(R.drawable.new_report_group)
    binding.header.cardIconSetting.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
    binding.header.iconSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
}

// CommentActivity.kt

    // CommentActivity.kt
    protected fun handleReport(
        reportTargetId: Int,     // id du message/post signalé
        type: ReportTypes,
        isEventComment: Boolean,
        isGroupComment: Boolean,
        isMe: Boolean,
        commentLang: String
    ) {
        // NB: this.id = l'ID "contexte" de l'écran courant (conversation / groupe / event)
        val conversationId = this.id

        val mode = when {
            isConversation && isOne2One -> SheetMode.DISCUSSION_ONE_TO_ONE
            isConversation && !isOne2One -> SheetMode.DISCUSSION_GROUP
            isEvent -> SheetMode.EVENT
            isGroup -> SheetMode.GROUP
            else -> SheetMode.GROUP
        }

        val sheet = when (mode) {
            SheetMode.DISCUSSION_ONE_TO_ONE -> {
                ActionSheetFragment.newDiscussion(
                    conversationId = conversationId,   // <-- pas le reportTargetId !
                    isOneToOne = true,
                    userId = postAuthorID,
                    username = titleName,
                    blocked = false // à brancher si tu as l'info
                )
            }
            SheetMode.DISCUSSION_GROUP -> {
                ActionSheetFragment.newDiscussion(
                    conversationId = conversationId,
                    isOneToOne = false,
                    userId = 0,
                    username = null,
                    blocked = false
                )
            }
            SheetMode.EVENT -> {
                // Si tu as un vrai eventId séparé, remplace conversationId par cette variable
                ActionSheetFragment.newEvent(
                    eventId = conversationId,
                    conversationId = conversationId
                )
            }
            SheetMode.GROUP -> {
                ActionSheetFragment.newGroup(
                    groupId = conversationId
                )
            }
            SheetMode.MESSAGE_ACTIONS -> {
                // Branche "exhaustive" : on ouvre directement le sheet d’actions de message.
                // Ici on n’a pas le HTML du message : on passe null pour le copier/coller (ou récupère via DataLanguageStock si tu veux).
                ActionSheetFragment.newMessageActions(
                    conversationId = if (isConversation) conversationId else 0,
                    groupId = if (isGroupComment) conversationId else 0,
                    eventId = if (isEventComment) conversationId else 0,
                    messageId = reportTargetId,
                    messageHtml = null,            // ou DataLanguageStock.getContentToCopy() si dispo
                    isMyMessage = isMe,
                    isEventContext = isEventComment,
                    isGroupContext = isGroupComment
                )
            }
        }

        sheet.show(supportFragmentManager, "ActionSheetFragment")
    }





private fun openEditTextKeyboard() {
//        if (shouldOpenKeyboard) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                binding.commentMessage.setTextColor(getColor(R.color.black))
//            }
//            binding.commentMessage.focusAndShowKeyboard()
//        }
}

abstract fun addComment()
}