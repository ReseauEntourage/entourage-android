package social.entourage.android.new_v8.discussions

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import social.entourage.android.R
import social.entourage.android.new_v8.comment.CommentActivity
import social.entourage.android.new_v8.models.Post
import social.entourage.android.new_v8.user.UserProfileActivity
import social.entourage.android.new_v8.utils.Const

/**
 * Created by - on 15/11/2022.
 */
class DetailConversationActivity : CommentActivity() {

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    private var hasToShowFirstMessage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasToShowFirstMessage = intent.getBooleanExtra(Const.HAS_TO_SHOW_MESSAGE, false)

        discussionsPresenter.getPostComments(id)
        discussionsPresenter.getAllComments.observe(this, ::handleGetPostComments)
        discussionsPresenter.commentPosted.observe(this, ::handleCommentPosted)

        binding.header.iconSettings.setImageDrawable(resources.getDrawable(R.drawable.new_settings))
        binding.header.title = titleName

        if (isOne2One) {
            binding.header.headerTitle.setOnClickListener {
                startActivity(
                    Intent(this, UserProfileActivity::class.java).putExtra(
                        Const.USER_ID, postAuthorID
                    ))
            }
        }
        checkAndShowPopWarning()
    }

    fun checkAndShowPopWarning() {
        if (hasToShowFirstMessage) {
            binding.layoutInfoNewDiscussion.isVisible = true
            binding.uiIvCloseNew.setOnClickListener {
                binding.layoutInfoNewDiscussion.visibility = View.GONE
            }
        }
    }

    override fun addComment() {
        discussionsPresenter.addComment(id, comment)
    }

    override fun handleGetPostComments(allComments: MutableList<Post>?) {
        val newComments = sortAndExtractDays(allComments)
        commentsList.clear()
        newComments?.let { commentsList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        newComments?.isEmpty()?.let { updateView(it) }
        scrollAfterLayout()
    }

    override fun handleReportPost(id: Int) {
        binding.header.iconSettings.setOnClickListener {
            SettingsDiscussionModalFragment.newInstance(postAuthorID,id,isOne2One)
                .show(supportFragmentManager, SettingsDiscussionModalFragment.TAG)
        }
    }

    fun sortAndExtractDays(allEvents: MutableList<Post>?) : MutableList<Post>? {
        val _allevents = allEvents?.groupBy { it.getFormatedStr() }
        val newList = ArrayList<Post>()
        _allevents?.let {
            for (mappp in _allevents) {
                val datePost = Post()
                datePost.isDatePostOnly = true
                datePost.datePostText = mappp.key
                newList.add(datePost)
                for (_msg in mappp.value) {
                    newList.add(_msg)
                }
            }
        }
        return newList
    }
}