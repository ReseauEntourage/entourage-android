package social.entourage.android.new_v8.events.details.feed

import android.os.Bundle
import androidx.collection.ArrayMap
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.posts.CreatePostActivity
import java.io.File

class CreatePostEventActivity : CreatePostActivity() {
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventPresenter.hasPost.observe(this, ::handlePost)
    }

    override fun addPostWithImage(file: File) {
        eventPresenter.addPost(
            binding.message.text.toString(),
            file,
            groupId
        )
    }

    override fun addPostWithoutImage(request: ArrayMap<String, Any>) {
        eventPresenter.addPost(groupId, request)
    }
}