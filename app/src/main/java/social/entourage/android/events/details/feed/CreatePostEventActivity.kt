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

        // Observe la réussite de la création de post
        eventPresenter.hasPost.observe(this) { hasPost ->
            handlePost(hasPost)
        }

        // Observe la liste des membres (pour mentions)
        eventPresenter.getMembers.observe(this) { members ->
            setAllMembers(members)
        }

        // Lance la récupération des membres
        if (groupId != -1) {
            eventPresenter.getEventMembers(groupId)
        }
    }

    // Publication d'un post avec image
    override fun addPostWithImage(file: File) {
        eventPresenter.addPost(
            binding.message.text.toString(),
            file,
            groupId
        )
    }

    // Publication d'un post sans image
    override fun addPostWithoutImage(request: ArrayMap<String, Any>) {
        eventPresenter.addPost(groupId, request)
    }
}
