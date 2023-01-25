package social.entourage.android.groups.details.feed

import android.os.Bundle
import android.view.View
import social.entourage.android.api.model.Post
import social.entourage.android.comment.CommentActivity
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.groups.GroupPresenter

class GroupCommentActivity : CommentActivity() {

    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        groupPresenter.getAllComments.observe(this, ::handleGetPostComments)
        groupPresenter.commentPosted.observe(this, ::handleCommentPosted)
        groupPresenter.getCurrentParentPost.observe(this, ::handleParentPost)
        groupPresenter.getPostComments(id, postId)
        super.setIsEventFalse()
    }

    override fun addComment() {
        groupPresenter.addComment(id, comment)
    }

    private fun handleParentPost(currentPost: Post?) {
        this.currentParentPost = currentPost
        binding.progressBar.visibility = View.GONE
        (binding.comments.adapter as? CommentsListAdapter)?.updateDatas(this.currentParentPost)
        scrollAfterLayout()

        updateView(commentsList.size == 0)
    }

    override fun handleGetPostComments(allComments: MutableList<Post>?) {
        commentsList.clear()
        allComments?.let { commentsList.addAll(it) }

        allComments?.isEmpty()?.let { updateView(it) }

        if (currentParentPost == null) {
            groupPresenter.getCurrentParentPost(id,postId)
            return
        }

        binding.progressBar.visibility = View.GONE
        scrollAfterLayout()
    }
}