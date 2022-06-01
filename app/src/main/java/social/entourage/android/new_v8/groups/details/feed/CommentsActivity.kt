package social.entourage.android.new_v8.groups.details.feed

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.databinding.NewActivityCommentsBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Post
import social.entourage.android.new_v8.utils.Const
import timber.log.Timber

class CommentsActivity : AppCompatActivity() {
    lateinit var binding: NewActivityCommentsBinding

    private var groupId = Const.DEFAULT_VALUE
    private var postId = Const.DEFAULT_VALUE
    private var postAuthorID = Const.DEFAULT_VALUE
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var commentsList: MutableList<Post> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(
            this,
            R.layout.new_activity_comments
        )
        groupId = intent.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
        postId = intent.getIntExtra(Const.POST_ID, Const.DEFAULT_VALUE)
        postAuthorID = intent.getIntExtra(Const.POST_AUTHOR_ID, Const.DEFAULT_VALUE)
        groupPresenter.getPostComments(groupId, postId)
        groupPresenter.getAllComments.observe(this, ::handleGetPostComments)
        initializeComments()
    }

    private fun handleGetPostComments(allComments: MutableList<Post>?) {
        allComments?.let {
            it.forEach { comment ->
                Timber.e("comment$comment")
            }
        }
        commentsList.clear()
        allComments?.let { commentsList.addAll(it) }
        // binding.progressBar.visibility = View.GONE
//      allMembers?.isEmpty()?.let { updateView(it) }
        binding.comments.adapter?.notifyDataSetChanged()

    }


    private fun initializeComments() {
        binding.comments.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CommentsListAdapter(commentsList, postAuthorID)
        }
    }
}