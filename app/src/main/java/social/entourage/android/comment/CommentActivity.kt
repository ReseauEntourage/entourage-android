package social.entourage.android.comment

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
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
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.report.onDissmissFragment
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.scrollToPositionSmooth
import social.entourage.android.tools.view.WebViewFragment
import timber.log.Timber
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
lateinit var viewModel: DiscussionsPresenter
var haveReloadFromDelete = false


protected var isOne2One = false
protected var isConversation = false
protected var isFromNotif = false
var currentParentPost:Post? = null
private val universalLinkManager = UniversalLinkManager(this)
var photoUri: Uri? = null

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
    if (isConversation) {
        handleReportPost(id,postLang)
    }
    else {
        handleReportPost(postId,postLang)
    }

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

private fun initializeComments() {
    binding.comments.apply {
        layoutManager = LinearLayoutManager(context)
        val meId = EntourageApplication.get().me()?.id ?: postAuthorID
        adapter = CommentsListAdapter(context, commentsList, meId,isOne2One,isConversation,currentParentPost, object : OnItemClickListener {
            override fun onItemClick(comment: Post) {
                addComment()
                commentsList.remove(comment)
            }
            override fun onCommentReport(commentId: Int?, isForEvent: Boolean, isForGroup:Boolean, isMe:Boolean,commentLang:String) {
                if(isForEvent){
                    commentId?.let { handleReport(it, ReportTypes.REPORT_POST_EVENT, true, false, isMe, commentLang) }
                }else if(isForGroup){
                    commentId?.let { handleReport(it, ReportTypes.REPORT_POST, false, true, isMe, commentLang) }
                }else{
                    commentId?.let { handleReport(it, ReportTypes.REPORT_COMMENT , false, false, isMe,commentLang) }
                }
            }

            override fun onShowWeb(url: String) {
                /*//TODO remove test Here test with launching WebViewActivityForTest activity
                WebViewActivityForTest.EXTRA_URL = url
                startActivity(Intent(context, WebViewActivityForTest::class.java))
                return*/

                if(url.contains("www.entourage.social") || url.contains("preprod.entourage.social")){
                    val uri = Uri.parse(url)
                    universalLinkManager.handleUniversalLink(uri)
                    return
                }
                var urlNew = url
                if (url.contains("http:")) {
                    urlNew = url.replace("http","https")
                }
                //TODO CORRECTION CHARTE LIST
                if (!url.contains("http:") && !url.contains("https:")) {
                    urlNew = "https://$url"
                }
                WebViewFragment.newInstance(urlNew, 0, true)
                    .show(supportFragmentManager, WebViewFragment.TAG)
            }
        })
        (adapter as? CommentsListAdapter)?.initiateList()
    }
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

protected fun handleReport(id: Int, type: ReportTypes, isEventComment :Boolean, isGroupComment:Boolean, isMe:Boolean, commentLang:String) {
    val fromLang =  commentLang
    var isNotTranslatable = false
    if (fromLang == null || fromLang.equals("")){
        DataLanguageStock.updatePostLanguage(fromLang)
    }else{
        isNotTranslatable = true
    }

    var description = comment?.content ?: ""
    val reportGroupBottomDialogFragment =
        ReportModalFragment.newInstance(id, this.id, type, isMe ,true, this.isOne2One, contentCopied = DataLanguageStock.contentToCopy, isNotTranslatable = isNotTranslatable)
    if(isEventComment){
        reportGroupBottomDialogFragment.setEventComment()
    }
    if(isGroupComment){
        reportGroupBottomDialogFragment.setGroupComment()
    }
    reportGroupBottomDialogFragment.setDismissCallback(this)
    reportGroupBottomDialogFragment.show(
        supportFragmentManager,
        ReportModalFragment.TAG
    )
}

protected open fun handleReportPost(id: Int, commentLang: String) {
    binding.header.iconSettings.setOnClickListener {
        if(isEvent){
            handleReport(id, ReportTypes.REPORT_POST_EVENT, isEvent, isGroup, false/*checkIsME*/,commentLang)
        }else{
            handleReport(id, ReportTypes.REPORT_POST, isEvent, isGroup, false/*checkIsME*/,commentLang)
        }
    }
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