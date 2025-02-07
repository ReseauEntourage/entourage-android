package social.entourage.android.groups.details.feed

import android.os.Bundle
import androidx.collection.ArrayMap
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.posts.CreatePostActivity
import java.io.File

class CreatePostGroupActivity : CreatePostActivity() {

    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    companion object {
        var idGroupForPost: Int? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe la réussite de la création de post
        groupPresenter.hasPost.observe(this) { hasPost ->
            handlePost(hasPost)
        }

        // Observe la liste des membres pour les mentions
        groupPresenter.getMembers.observe(this) { members ->
            setAllMembers(members)
        }

        // Lance la récupération des membres
        if (groupId != -1) {
            groupPresenter.getGroupMembers(groupId)
        }
    }

    // Publication d'un post avec image
    override fun addPostWithImage(file: File) {
        groupPresenter.addPost(
            binding.message.text.toString(),
            file,
            groupId
        )
    }

    // Publication d'un post sans image
    override fun addPostWithoutImage(request: ArrayMap<String, Any>) {
        groupPresenter.addPost(groupId, request)
    }
}
