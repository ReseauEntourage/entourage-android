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

        // 1) Observe le résultat de la recherche mention
        eventPresenter.getMembersSearch.observe(this) { members ->
            updateMentionList(members)
        }

        // Récupération initiale des membres (optionnel)
        if (groupId != -1) {
            eventPresenter.getEventMembers(groupId)
        }
    }

    // --------------------------------------------------------------------------------
    // Logique mention : on redéfinit onMentionQuery
    // --------------------------------------------------------------------------------
    override fun onMentionQuery(query: String) {
        // Appelle la route de recherche mention pour les events
        eventPresenter.searchEventMembers(groupId, query)
    }

    // Publication d'un post avec image
    override fun addPostWithImage(file: File) {
        eventPresenter.addPost(binding.message.text.toString(), file, groupId)
    }

    // Publication d'un post sans image
    override fun addPostWithoutImage(request: ArrayMap<String, Any>) {
        eventPresenter.addPost(groupId, request)
    }
}
