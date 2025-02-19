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

        // 1) Observe le résultat distant pour les mentions
        groupPresenter.getMembersSearch.observe(this) { members ->
            // On met à jour la liste => c'est la méthode de CreatePostActivity
            updateMentionList(members)
        }

        // Lance la récupération basique des membres si besoin
        // (optionnel si on veut par ex. connaître le total,
        //  mais la mention passera par la recherche ci-dessus)
        if (groupId != -1) {
            groupPresenter.getGroupMembers(groupId)
        }
    }

    // --------------------------------------------------------------------------------
    // Logique mention : on redéfinit la méthode abstraite onMentionQuery
    // --------------------------------------------------------------------------------
    override fun onMentionQuery(query: String) {
        // Appelle la route de recherche asynchrone du groupPresenter
        // (query vide => tous les membres, si le back l'autorise)
        groupPresenter.searchGroupMembers(groupId, query)
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
