package social.entourage.android.new_v8.groups.details.feed

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.databinding.NewActivityCommentsBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Post
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.new_v8.utils.focusAndShowKeyboard
import java.util.*

class CommentsActivity : AppCompatActivity() {
    lateinit var binding: NewActivityCommentsBinding

    private var groupId = Const.DEFAULT_VALUE
    private var postId = Const.DEFAULT_VALUE
    private var postAuthorID = Const.DEFAULT_VALUE
    private var isMember = false
    private var groupName = ""
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var commentsList: MutableList<Post> = mutableListOf()
    private var shouldOpenKeyboard = false
    private var messagesFailed: MutableList<Post?> = mutableListOf()
    private var comment: Post? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_comments
        )
        groupId = intent.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
        postId = intent.getIntExtra(Const.POST_ID, Const.DEFAULT_VALUE)
        postAuthorID = intent.getIntExtra(Const.POST_AUTHOR_ID, Const.DEFAULT_VALUE)
        isMember = intent.getBooleanExtra(Const.IS_MEMBER, false)
        groupName = intent.getStringExtra(Const.GROUP_NAME).toString()
        shouldOpenKeyboard = intent.getBooleanExtra(Const.SHOULD_OPEN_KEYBOARD, false)
        groupPresenter.getPostComments(groupId, postId)
        groupPresenter.getAllComments.observe(this, ::handleGetPostComments)
        groupPresenter.commentPosted.observe(this, ::handleCommentPosted)
        initializeComments()
        handleCommentAction()
        openEditTextKeyboard()
        handleBackButton()
    }

    private fun handleGetPostComments(allComments: MutableList<Post>?) {
        commentsList.clear()
        allComments?.let { commentsList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allComments?.isEmpty()?.let { updateView(it) }
    }

    private fun updateView(emptyState: Boolean) {
        if (emptyState) {
            binding.emptyState.visibility = View.VISIBLE
            binding.comments.visibility = View.GONE
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
                groupName
            )
            binding.postComment.visibility = View.GONE
        }
    }

    private fun handleCommentPosted(post: Post?) {
        post?.let {
            commentsList.add(0, post)
        } ?: run {
            messagesFailed.add(comment)
            comment?.let { commentsList.add(0, it) }
        }
        updateView(false)
    }

    private fun initializeComments() {
        binding.comments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommentsListAdapter(commentsList, postAuthorID, object : OnItemClickListener {
                override fun onItemClick(comment: Post) {
                    groupPresenter.addComment(groupId, comment)
                    commentsList.remove(comment)
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
                groupPresenter.addComment(groupId, comment)
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

    private fun openEditTextKeyboard() {
        if (shouldOpenKeyboard) {
            binding.commentMessage.focusAndShowKeyboard()
        }
    }
}