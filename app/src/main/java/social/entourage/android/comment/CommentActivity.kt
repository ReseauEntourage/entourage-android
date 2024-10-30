package social.entourage.android.comment

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
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
import social.entourage.android.discussions.WebViewActivityForTest
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.report.onDissmissFragment
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.focusAndShowKeyboard
import social.entourage.android.tools.utils.scrollToPositionSmooth
import social.entourage.android.tools.view.WebViewFragment
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
    lateinit var viewModel: DiscussionsPresenter
    var haveReloadFromDelete = false


    protected var isOne2One = false
    protected var isConversation = false
    protected var isFromNotif = false
    var currentParentPost:Post? = null
    private val universalLinkManager = UniversalLinkManager(this)

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
        if(isMessageDeleted){
        }
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
            binding.emptyState.visibility = View.VISIBLE
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
                override fun onCommentReport(commentId: Int?, isForEvent: Boolean, isMe:Boolean,commentLang:String) {
                    if(isForEvent){
                        commentId?.let { handleReport(it, ReportTypes.REPORT_POST_EVENT, true, isMe, commentLang) }
                    }else{
                        commentId?.let { handleReport(it, ReportTypes.REPORT_COMMENT , true, isMe,commentLang) }

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
            val message = binding.commentMessage.text.toString()
            if (!message.isNullOrBlank() && !message.isNullOrEmpty()) {
                // Disable the send button and show the progress bar
                binding.comment.isEnabled = false
                binding.progressBar.visibility = View.GONE

                // Create the user and post objects as before
                val user = EntourageUser().apply {
                    userId = EntourageApplication.me(this@CommentActivity)?.id ?: 0
                    avatarURLAsString = EntourageApplication.me(this@CommentActivity)?.avatarURL
                }

                comment = Post(
                    idInternal = UUID.randomUUID(),
                    content = message,
                    postId = postId,
                    user = user
                )

                // Send the comment
                addComment()

                // Simulate a delay of 2 seconds (2000 milliseconds)
                binding.comment.postDelayed({
                    // Re-enable the button and hide the progress bar
                    binding.comment.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }, 2000)

                // Clear the input field and hide the keyboard
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

    protected fun handleReport(id: Int, type: ReportTypes, isEventComment :Boolean, isMe:Boolean, commentLang:String) {
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
        reportGroupBottomDialogFragment.setDismissCallback(this)
        reportGroupBottomDialogFragment.show(
            supportFragmentManager,
            ReportModalFragment.TAG
        )
    }

    protected open fun handleReportPost(id: Int, commentLang: String) {
        binding.header.iconSettings.setOnClickListener {
            if(isEvent){
                handleReport(id, ReportTypes.REPORT_POST_EVENT, false, false/*checkIsME*/,commentLang)
            }else{
                handleReport(id, ReportTypes.REPORT_POST, false, false/*checkIsME*/,commentLang)
            }
        }
    }

    private fun openEditTextKeyboard() {
        if (shouldOpenKeyboard) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                binding.commentMessage.setTextColor(getColor(R.color.black))
            }
            binding.commentMessage.focusAndShowKeyboard()
        }
    }

    abstract fun addComment()
}