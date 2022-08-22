package social.entourage.android.new_v8.groups.details.feed

import android.os.Bundle
import androidx.collection.ArrayMap
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.posts.CreatePostActivity
import java.io.File

class CreatePostGroupActivity : CreatePostActivity() {
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        groupPresenter.hasPost.observe(this, ::handlePost)
    }

    override fun addPostWithImage(file: File) {
        groupPresenter.addPost(
            binding.message.text.toString(),
            file,
            groupId
        )
    }

    override fun addPostWithoutImage(request: ArrayMap<String, Any>) {
        groupPresenter.addPost(groupId, request)
    }
}