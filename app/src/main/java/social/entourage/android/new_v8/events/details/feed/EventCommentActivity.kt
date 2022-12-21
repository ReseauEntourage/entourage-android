package social.entourage.android.new_v8.events.details.feed

import android.os.Bundle
import social.entourage.android.new_v8.comment.CommentActivity
import social.entourage.android.new_v8.events.EventsPresenter

class EventCommentActivity : CommentActivity() {

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventPresenter.getPostComments(id, postId)
        eventPresenter.getAllComments.observe(this, ::handleGetPostComments)
        eventPresenter.commentPosted.observe(this, ::handleCommentPosted)
    }

    override fun addComment() {
        eventPresenter.addComment(id, comment)
    }
}