package social.entourage.android.discussions

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import social.entourage.android.R
import social.entourage.android.comment.CommentActivity
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.Post
import social.entourage.android.user.UserProfileActivity
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import java.util.*

/**
 * Created by - on 15/11/2022.
 */
class DetailConversationActivity : CommentActivity() {


    private var hasToShowFirstMessage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hasToShowFirstMessage = intent.getBooleanExtra(Const.HAS_TO_SHOW_MESSAGE, false)

        viewModel.getAllComments.observe(this, ::handleGetPostComments)
        viewModel.commentPosted.observe(this, ::handleCommentPosted)

        viewModel.getPostComments(id)


        binding.header.iconSettings.setImageDrawable(resources.getDrawable(R.drawable.new_settings))
        binding.header.cardIconSetting.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.iconSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.title = titleName
        if (isOne2One) {
            binding.header.headerTitle.setOnClickListener {
                startActivityForResult(
                    Intent(this, UserProfileActivity::class.java).putExtra(
                        Const.USER_ID, postAuthorID
                    ), 0)
            }
        }
        checkAndShowPopWarning()

        viewModel.detailConversation.observe(this, ::handleDetailConversation)
        viewModel.getDetailConversation(id)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view_detail)
    }

    override fun reloadView() {
        lifecycleScope.launch {
            shouldOpenKeyboard = false
            recreate()
        }
    }

    private fun handleDetailConversation(conversation: Conversation?) {
        titleName = conversation?.title
        binding.header.title = titleName

        if (conversation?.hasBlocker() == true) {
            binding.postBlocked.isVisible = true
            val _name = titleName ?: ""
            if (conversation.imBlocker()) {
                binding.commentBlocked.hint = String.format(getString(R.string.message_user_blocked_by_me),_name)
            }
            else {
                binding.commentBlocked.hint = String.format(getString(R.string.message_user_blocked_by_other),_name)
            }
        }
        else {
            binding.postBlocked.isVisible = false
        }
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
        viewModel.addComment(id, comment)
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
            AnalyticsEvents.logEvent(AnalyticsEvents.Message_action_param)
            SettingsDiscussionModalFragment.newInstance(
                postAuthorID,
                id,
                isOne2One,
                titleName,
                viewModel.detailConversation.value?.imBlocker()
            )
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
                datePost.datePostText = mappp.key.capitalize(Locale.FRANCE)
                newList.add(datePost)
                for (_msg in mappp.value) {
                    newList.add(_msg)
                }
            }
        }
        return newList
    }

    fun updateDiscussion() {
        viewModel.getDetailConversation(id)
    }
}