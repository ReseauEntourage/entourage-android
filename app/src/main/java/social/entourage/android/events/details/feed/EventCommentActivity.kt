package social.entourage.android.events.details.feed

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import social.entourage.android.api.model.Post
import social.entourage.android.comment.CommentActivity
import social.entourage.android.events.EventsPresenter
import social.entourage.android.comment.CommentsListAdapter

class EventCommentActivity : CommentActivity() {

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventPresenter.getAllComments.observe(this, ::handleGetPostComments)
        eventPresenter.commentPosted.observe(this, ::handleCommentPosted)
        eventPresenter.getCurrentParentPost.observe(this, ::handleParentPost)
        eventPresenter.getPostComments(id, postId)
        setAdapterForEvent()
        super.setIsEventTrue()
    }

    override fun addComment() {
        eventPresenter.addComment(id, comment)
    }

    override fun reloadView() {
        lifecycleScope.launch {
            shouldOpenKeyboard = false
            recreate()
        }
    }

    override fun translateView(id: Int) {
        val adapter = binding.comments.adapter as? CommentsListAdapter
        adapter?.translateItem(id)
    }

    private fun setAdapterForEvent(){
        if(binding.comments.adapter is CommentsListAdapter){
            var _adapter = binding.comments.adapter as? CommentsListAdapter
            _adapter!!.setForEvent()
        }
    }

    private fun handleParentPost(currentPost: Post?) {
        this.currentParentPost = currentPost
        binding.progressBar.visibility = View.GONE
        (binding.comments.adapter as? CommentsListAdapter)?.updateData(this.currentParentPost)
        scrollAfterLayout()
        updateView(commentsList.size == 0)
    }

    override fun handleGetPostComments(allComments: MutableList<Post>?) {
        commentsList.clear()
        allComments?.let { commentsList.addAll(it) }

        allComments?.isEmpty()?.let { updateView(it) }

        if (currentParentPost == null) {
            eventPresenter.getCurrentParentPost(id,postId)
            return
        }

        binding.progressBar.visibility = View.GONE
        scrollAfterLayout()
    }

}