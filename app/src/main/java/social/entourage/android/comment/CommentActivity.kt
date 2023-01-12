package social.entourage.android.comment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Post
import social.entourage.android.databinding.NewActivityCommentsBinding
import social.entourage.android.groups.details.feed.CommentsListAdapter
import social.entourage.android.groups.details.feed.OnItemClickListener
import social.entourage.android.report.ReportModalFragment
import social.entourage.android.report.ReportTypes
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.focusAndShowKeyboard
import social.entourage.android.tools.utils.scrollToPositionSmooth
import social.entourage.android.tools.view.WebViewFragment
import java.util.*

abstract class CommentActivity : AppCompatActivity() {
    lateinit var binding: NewActivityCommentsBinding

    var id = Const.DEFAULT_VALUE
    var postId = Const.DEFAULT_VALUE
    protected var postAuthorID = Const.DEFAULT_VALUE
    protected var isMember = false
    protected var titleName:String? = null
    var commentsList: MutableList<Post> = mutableListOf()
    private var shouldOpenKeyboard = false
    var messagesFailed: MutableList<Post?> = mutableListOf()
    var comment: Post? = null

    protected var isOne2One = false
    protected var isConversation = false
    var currentParentPost:Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_comments
        )
        id = intent.getIntExtra(Const.ID, Const.DEFAULT_VALUE)
        postId = intent.getIntExtra(Const.POST_ID, Const.DEFAULT_VALUE)
        postAuthorID = intent.getIntExtra(Const.POST_AUTHOR_ID, Const.DEFAULT_VALUE)
        isMember = intent.getBooleanExtra(Const.IS_MEMBER, false)
        titleName = intent.getStringExtra(Const.NAME)
        isOne2One = intent.getBooleanExtra(Const.IS_CONVERSATION_1TO1, false)
        isConversation = intent.getBooleanExtra(Const.IS_CONVERSATION, false)
        shouldOpenKeyboard = intent.getBooleanExtra(Const.SHOULD_OPEN_KEYBOARD, false)
        initializeComments()
        handleCommentAction()
        openEditTextKeyboard()
        handleBackButton()
        setSettingsIcon()

        if (isConversation) {
            handleReportPost(id)
        }
        else {
            handleReportPost(postId)
        }

        handleSendButtonState()
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
            adapter = CommentsListAdapter(commentsList, meId,isOne2One,isConversation,currentParentPost, object : OnItemClickListener {
                override fun onItemClick(comment: Post) {
                    addComment()
                    commentsList.remove(comment)
                }

                override fun onCommentReport(commentId: Int?) {
                    commentId?.let { handleReport(it, ReportTypes.REPORT_COMMENT) }
                }

                override fun onShowWeb(url: String) {
                    var urlNew = url
                    if (url.contains("http:")) {
                        urlNew = url.replace("http","https")
                    }
                    if (!url.contains("http:") && !url.contains("https:")) {
                        urlNew = "https://$url"
                    }
                    WebViewFragment.newInstance(urlNew, 0, true)
                        .show(supportFragmentManager, WebViewFragment.TAG)
                }
            })
        }
    }

    private fun handleCommentAction() {
        binding.comment.setOnClickListener {
            val message = binding.commentMessage.text.toString()
            if (!message.isNullOrBlank() && !message.isNullOrEmpty()) {
                val user = EntourageUser()
                user.userId = EntourageApplication.me(this)?.id!!
                user.avatarURLAsString = EntourageApplication.me(this)?.avatarURL
                comment =
                    Post(
                        idInternal = UUID.randomUUID(),
                        content = message,
                        postId = postId,
                        user = user
                    )
                addComment()
            }
            binding.commentMessage.text.clear()
            Utils.hideKeyboard(this)
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

        binding.header.title = getString(R.string.comments)
        binding.header.iconSettings.isVisible = true
        binding.header.iconSettings.setImageResource(R.drawable.new_report_group)
    }

    private fun handleReport(id: Int, type: ReportTypes) {
        val reportGroupBottomDialogFragment =
            ReportModalFragment.newInstance(id, this.id, type)
        reportGroupBottomDialogFragment.show(
            supportFragmentManager,
            ReportModalFragment.TAG
        )
    }

    protected open fun handleReportPost(id: Int) {
        binding.header.iconSettings.setOnClickListener {
            handleReport(id, ReportTypes.REPORT_POST)
        }
    }

    private fun openEditTextKeyboard() {
        if (shouldOpenKeyboard) {
            binding.commentMessage.focusAndShowKeyboard()
        }
    }

    abstract fun addComment()
}