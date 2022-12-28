package social.entourage.android.groups.details.feed

import android.os.Bundle
import social.entourage.android.comment.CommentActivity
import social.entourage.android.groups.GroupPresenter

class GroupCommentActivity : CommentActivity() {

    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupPresenter.getPostComments(id, postId)
        groupPresenter.getAllComments.observe(this, ::handleGetPostComments)
        groupPresenter.commentPosted.observe(this, ::handleCommentPosted)
    }

    override fun addComment() {
        groupPresenter.addComment(id, comment)
    }
}