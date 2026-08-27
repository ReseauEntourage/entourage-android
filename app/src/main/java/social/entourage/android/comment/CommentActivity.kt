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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.Reaction
import social.entourage.android.api.model.ReactionType
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
import social.entourage.android.sockets.ConversationSocketManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.scrollToPositionSmooth
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.ui.ActionSheetFragment
import social.entourage.android.ui.SheetMode
import java.util.UUID

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
var isSmallTalk = false
lateinit var viewModel: DiscussionsPresenter
var haveReloadFromDelete = false
protected var editingMessageId: Int? = null

// chat_message_id ciblé par une notification (deep link vers un commentaire précis dans ce
// fil) : consommé une seule fois par scrollAndHighlightIfNeeded(), puis remis à null.
private var targetChatMessageId: Int? = null

// Vrai uniquement pour l'écran de discussion (DetailConversationActivity) : c'est le
// seul contexte où on a un endpoint PATCH confirmé pour éditer un message.
protected open val allowsMessageEdit: Boolean get() = false

// Vrai pour tout écran où les réactions sur message sont proposées (discussion +
// commentaires de publication).
protected open val allowsMessageReactions: Boolean get() = false

private var socketEventsJob: Job? = null
protected var hasUnseenNewMessages = false


protected var isOne2One = false
protected var isConversation = false
protected var isFromNotif = false
var currentParentPost:Post? = null
val universalLinkManager = UniversalLinkManager(this)
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
    intent.getIntExtra(Const.CHAT_MESSAGE_ID, Const.DEFAULT_VALUE).let {
        targetChatMessageId = if (it != Const.DEFAULT_VALUE) it else null
    }
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
    binding.layoutStaffBanner.visibility = View.GONE

    handleSendButtonState()
    setupConversationChips()
    setupWindowInsets()
    binding.btnCancelEditMessage.setOnClickListener { cancelEditingMessage() }
    binding.tvNewMessagesBanner.setOnClickListener {
        hideNewMessagesBanner()
        scrollAfterLayout()
    }
}

/**
 * Met le message dans le champ de saisie du bas et bascule le bouton d'envoi en mode
 * "modifier" : le prochain envoi fera un PATCH (via [updateComment]) au lieu de créer
 * un nouveau message.
 */
fun startEditingMessage(messageId: Int, messageHtml: String?) {
    if (messageId == 0) return
    editingMessageId = messageId
    val html = messageHtml ?: ""
    val spanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        @Suppress("DEPRECATION") Html.fromHtml(html)
    }
    binding.commentMessage.setText(spanned)
    binding.commentMessage.setSelection(binding.commentMessage.text?.length ?: 0)
    binding.layoutEditingMessage.visibility = View.VISIBLE
    binding.commentMessage.requestFocus()
    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as? android.view.inputmethod.InputMethodManager
    imm?.showSoftInput(binding.commentMessage, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
}

fun cancelEditingMessage() {
    editingMessageId = null
    binding.commentMessage.text?.clear()
    binding.layoutEditingMessage.visibility = View.GONE
    Utils.hideKeyboard(this)
}

/** Overridden by DetailConversationActivity to PATCH the edited message. */
abstract fun updateComment(messageId: Int, newContentHtml: String)

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

/**
 * Variante de [scrollAfterLayout] qui scrolle et met en évidence [targetChatMessageId] s'il
 * y en a un en attente (deep link notif vers un commentaire précis), sinon se comporte comme
 * [scrollAfterLayout] (scroll vers le dernier message). Le fil de commentaires n'étant pas
 * paginé côté client (chargé en un seul appel), pas besoin d'aller chercher une page
 * supplémentaire : si le message ciblé existe dans ce fil, il est déjà en mémoire.
 */
protected fun scrollAndHighlightIfNeeded() {
    val targetId = targetChatMessageId
    if (targetId == null) {
        scrollAfterLayout()
        return
    }
    targetChatMessageId = null

    val commentIndex = commentsList.indexOfFirst { it.id == targetId }
    val isParentPost = commentIndex == -1 && currentParentPost?.id == targetId
    if (commentIndex == -1 && !isParentPost) {
        // Message ciblé introuvable dans ce fil (racine ou commentaire) : comportement par défaut.
        scrollAfterLayout()
        return
    }

    val adapterIndex = if (isParentPost) 0 else commentIndex + parentPostOffset()
    binding.comments.viewTreeObserver.addOnGlobalLayoutListener(
        object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.comments.viewTreeObserver.removeOnGlobalLayoutListener(this)
                binding.comments.scrollToPosition(adapterIndex)
                if (!isParentPost) highlightCommentAt(adapterIndex, targetId)
            }
        })
}

private fun highlightCommentAt(adapterIndex: Int, targetId: Int) {
    val adapter = binding.comments.adapter as? CommentsListAdapter ?: return
    adapter.highlightedMessageId = targetId
    adapter.notifyItemChanged(adapterIndex)
    binding.comments.postDelayed({
        if (adapter.highlightedMessageId == targetId) {
            adapter.highlightedMessageId = null
            adapter.notifyItemChanged(adapterIndex)
        }
    }, HIGHLIGHT_DURATION_MS)
}

companion object {
    private const val HIGHLIGHT_DURATION_MS = 1200L
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
    val showConvEmptyState = emptyState && isConversation && !isSmallTalk && !isEvent && !isGroup && currentParentPost == null
    if (emptyState) {
        binding.emptyState.visibility = if (showConvEmptyState) View.VISIBLE else View.GONE
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

private fun setupWindowInsets() {
    ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
        val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val navBars   = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val ime       = insets.getInsets(WindowInsetsCompat.Type.ime())

        // Header pushed below status bar
        binding.header.headerLayout.updatePadding(top = statusBars.top)

        // Root bottom padding = nav bar height normally; keyboard height when IME is visible.
        // On pre-15 with adjustResize the IME inset is 0 (window is resized instead).
        // On Android 15+ edge-to-edge the IME inset is dispatched and replaces the resize.
        binding.root.updatePadding(bottom = maxOf(navBars.bottom, ime.bottom))

        // Scroll to latest message when keyboard opens so the conversation stays in view
        val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
        if (imeVisible && commentsList.isNotEmpty()) {
            binding.comments.scrollToPosition(commentsList.size - 1)
        }

        insets
    }
}

private fun setupConversationChips() {
    val chips = listOf(
        binding.chipHello to R.string.conv_draft_hello,
        binding.chipHelp to R.string.conv_draft_help,
        binding.chipQuestion to R.string.conv_draft_question,
        binding.chipEvent to R.string.conv_draft_event,
    )
    chips.forEach { (chip, draftRes) ->
        chip.setOnClickListener {
            chips.forEach { (c, _) -> c.setBackgroundResource(R.drawable.bg_chip_conversation_suggestion) }
            chip.setBackgroundResource(R.drawable.bg_chip_conversation_suggestion_selected)
            binding.commentMessage.setText(getString(draftRes))
            binding.commentMessage.requestFocus()
            binding.commentMessage.setSelection(binding.commentMessage.text.length)
        }
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
                        val fullUrl = if (!url.startsWith("http")) "https://$url" else url
                        val uri = Uri.parse(fullUrl)
                        // Les mentions @ sont insérées sous forme de lien universel
                        // (https://<DEEP_LINKS_URL>/app/users/<id>), cf.
                        // DetailConversationActivity.insertMentionIntoEditText : on les route
                        // vers l'écran in-app plutôt que de les ouvrir dans la WebView.
                        if (uri.host == universalLinkManager.prodURL || uri.host == universalLinkManager.stagingURL) {
                            universalLinkManager.handleUniversalLink(uri)
                            return
                        }
                        WebViewFragment.newInstance(fullUrl, 0, true)
                            .show(supportFragmentManager, WebViewFragment.TAG)
                    }

                    override fun onMessageLongPress(comment: Post, isMe: Boolean) {
                        showMessageOptions(comment, isMe)
                    }
                }
            )
            (adapter as? CommentsListAdapter)?.initiateList()
            (adapter as? CommentsListAdapter)?.allowsReactions = allowsMessageReactions
        }
    }

    private fun showMessageOptions(comment: Post, isMe: Boolean) {
        val conversationId = if (isConversation) id else 0
        val groupId = if (isGroup) id else 0
        val eventId = if (isEvent) id else 0
        val canEdit = allowsMessageEdit && isMe && comment.status !in listOf("deleted", "offensive", "offensible")

        val sheet = ActionSheetFragment.newMessageActions(
            conversationId = conversationId,
            groupId = groupId,
            eventId = eventId,
            messageId = comment.id ?: 0,
            messageHtml = comment.content ?: comment.contentHtml,
            isMyMessage = isMe,
            isEventContext = isEvent,
            isGroupContext = isGroup,
            canEditMessage = canEdit,
            // Pas de réaction sur son propre message, ni là où l'écran ne les propose pas
            // (ex. commentaires de sortie) — même règle que l'ancien bouton sous la bulle.
            allowsReactions = allowsMessageReactions && !isMe,
            myReactionId = comment.reactionId ?: 0
        )
        sheet.show(supportFragmentManager, "MessageActionsSheet")
    }

    /** Overridden by subclasses to actually send/remove the reaction. */
    protected open fun onMessageReactionClicked(comment: Post, reactionType: ReactionType) {}

    /** Appelé par ActionSheetFragment (barre de réactions en haut du sheet d'actions) quand
     * l'utilisateur choisit/retape une réaction pour [messageId]. */
    fun applyReactionFromMessageActions(messageId: Int, reactionType: ReactionType) {
        val comment = commentsList.firstOrNull { it.id == messageId } ?: return
        onMessageReactionClicked(comment, reactionType)
    }

    // ==================================================================================
    // Websocket temps réel (ConversationChannel) — partagé entre DetailConversationActivity
    // et GroupCommentActivity : connexion/déconnexion, fusion des messages entrants sans
    // voler le scroll, bandeau "nouveaux messages", et mise à jour optimiste des réactions.
    // ==================================================================================

    /** [belongsToThisScreen] filtre les événements reçus (utile quand la souscription
     * est plus large que l'écran affiché, ex. tout un groupe alors qu'on ne regarde que
     * les commentaires d'un post précis). [onReconnected] est appelé quand la souscription
     * est reconfirmée après une coupure (voir ChatEvent.Reconnected) : aucun historique
     * n'étant rejoué par le serveur, l'écran doit recharger via REST pour combler le trou. */
    protected fun connectChatSocket(
        instanceType: String,
        instanceId: Int,
        belongsToThisScreen: (Post) -> Boolean = { true },
        onReconnected: () -> Unit = {}
    ) {
        ConversationSocketManager.connect(instanceType, instanceId)
        socketEventsJob?.cancel()
        socketEventsJob = lifecycleScope.launch {
            ConversationSocketManager.events.collect { event -> onChatSocketEvent(event, belongsToThisScreen, onReconnected) }
        }
    }

    protected fun disconnectChatSocket() {
        socketEventsJob?.cancel()
        socketEventsJob = null
        ConversationSocketManager.disconnect()
    }

    private fun onChatSocketEvent(
        event: ConversationSocketManager.ChatEvent,
        belongsToThisScreen: (Post) -> Boolean,
        onReconnected: () -> Unit
    ) {
        when (event) {
            is ConversationSocketManager.ChatEvent.MessageCreated ->
                if (belongsToThisScreen(event.message)) mergeIncomingMessage(event.message)
            is ConversationSocketManager.ChatEvent.MessageUpdated ->
                if (belongsToThisScreen(event.message)) updateExistingMessageInPlace(event.message)
            is ConversationSocketManager.ChatEvent.ReactionAdded ->
                applyReactionAdded(event.chatMessageId, event.reactionId)
            is ConversationSocketManager.ChatEvent.ReactionRemoved ->
                applyReactionRemoved(event.chatMessageId, event.reactionId)
            is ConversationSocketManager.ChatEvent.Reconnected -> onReconnected()
        }
    }

    /** Décalage d'index dû au "post parent" affiché en position 0 (commentaires de publication). */
    private fun parentPostOffset(): Int = if (currentParentPost != null) 1 else 0

    protected fun isAtBottomOfComments(): Boolean {
        val lm = binding.comments.layoutManager as? LinearLayoutManager ?: return true
        val last = lm.findLastCompletelyVisibleItemPosition()
        return last >= lm.itemCount - 2
    }

    protected fun showNewMessagesBanner() {
        hasUnseenNewMessages = true
        binding.tvNewMessagesBanner.visibility = View.VISIBLE
    }

    protected fun hideNewMessagesBanner() {
        hasUnseenNewMessages = false
        binding.tvNewMessagesBanner.visibility = View.GONE
    }

    /**
     * Insère ou met à jour un message reçu (confirmation d'envoi du serveur, ou message
     * poussé par le websocket). Ne force le scroll que si on était déjà en bas de la
     * liste (ou si c'est notre propre message) : sinon on affiche juste le bandeau
     * "nouveaux messages", comme sur Messenger.
     */
    protected open fun mergeIncomingMessage(post: Post, forceScrollIfMine: Boolean = true) {
        val existingIdx = if (post.id != null) commentsList.indexOfFirst { it.id == post.id } else -1
        if (existingIdx >= 0) {
            commentsList[existingIdx] = post
            binding.comments.adapter?.notifyItemChanged(existingIdx + parentPostOffset())
            return
        }
        val wasAtBottom = isAtBottomOfComments()
        val insertPos = commentsList.size
        commentsList.add(post)
        binding.comments.adapter?.notifyItemInserted(insertPos + parentPostOffset())
        val isMine = post.user?.userId == EntourageApplication.get().me()?.id
        if (wasAtBottom || (isMine && forceScrollIfMine)) {
            scrollAfterLayout()
            hideNewMessagesBanner()
        } else {
            showNewMessagesBanner()
        }
        binding.progressBar.visibility = View.GONE
        updateView(commentsList.isEmpty())
    }

    private fun updateExistingMessageInPlace(post: Post) {
        val idx = commentsList.indexOfFirst { it.id != null && it.id == post.id }
        if (idx >= 0) {
            commentsList[idx] = post
            binding.comments.adapter?.notifyItemChanged(idx + parentPostOffset())
        }
    }

    private fun applyReactionAdded(chatMessageId: Int, reactionId: Int) {
        val idx = commentsList.indexOfFirst { it.id == chatMessageId }
        if (idx >= 0) {
            addOrUpdateReactionBucket(commentsList[idx], reactionId)
            binding.comments.adapter?.notifyItemChanged(idx + parentPostOffset())
        }
    }

    private fun applyReactionRemoved(chatMessageId: Int, reactionId: Int) {
        val idx = commentsList.indexOfFirst { it.id == chatMessageId }
        if (idx >= 0) {
            removeReactionBucket(commentsList[idx], reactionId)
            binding.comments.adapter?.notifyItemChanged(idx + parentPostOffset())
        }
    }

    protected fun addOrUpdateReactionBucket(post: Post, reactionId: Int) {
        val bucket = post.reactions?.firstOrNull { it.reactionId == reactionId }
        if (bucket != null) {
            bucket.reactionsCount += 1
        } else {
            val list = post.reactions ?: mutableListOf<Reaction>().also { post.reactions = it }
            list.add(Reaction().apply { this.reactionId = reactionId; this.reactionsCount = 1 })
        }
    }

    protected fun removeReactionBucket(post: Post, reactionId: Int) {
        val bucket = post.reactions?.firstOrNull { it.reactionId == reactionId } ?: return
        if (bucket.reactionsCount <= 1) post.reactions?.remove(bucket)
        else bucket.reactionsCount -= 1
    }






    private fun handleCommentAction() {
    binding.comment.setOnClickListener {
        // Convertir le contenu de l'EditText en HTML pour préserver les retours à la ligne
        val rawText = binding.commentMessage.text.toString().trim()
        val message = if (rawText.isEmpty()) {
            ""
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.toHtml(binding.commentMessage.text, Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL)
            } else {
                @Suppress("DEPRECATION")
                Html.toHtml(binding.commentMessage.text)
            }
        }

        val messageIdBeingEdited = editingMessageId
        if (messageIdBeingEdited != null) {
            if (message.isNotBlank()) {
                updateComment(messageIdBeingEdited, message)
            }
            cancelEditingMessage()
            return@setOnClickListener
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
    binding.header.headerIconBack.setOnClickListener {
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
    binding.header.headerIconSettings.isVisible = true
    binding.header.headerIconSettings.setImageResource(R.drawable.new_report_group)
    binding.header.headerCardIconSetting.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
    binding.header.headerIconSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
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
//            binding.commentMessage.focusAndShowKeyboard()
//        }
}

abstract fun addComment()
}