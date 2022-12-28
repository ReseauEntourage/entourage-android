package social.entourage.android.events.details.feed

import android.os.Bundle
import androidx.collection.ArrayMap
import social.entourage.android.events.EventsPresenter
import social.entourage.android.posts.CreatePostActivity
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