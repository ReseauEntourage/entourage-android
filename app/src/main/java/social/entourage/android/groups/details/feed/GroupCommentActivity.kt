package social.entourage.android.groups.details.feed

import android.os.Build
import android.os.Bundle
import android.text.*
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Post
import social.entourage.android.comment.CommentActivity
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.comment.MentionAdapter
import social.entourage.android.databinding.ActivityCommentsBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import timber.log.Timber
import java.util.UUID

class GroupCommentActivity : CommentActivity() {

    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    // Retient l'index du dernier '@' tapé. -1 => pas de mention en cours
    private var lastMentionStartIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observers pour la récupération des commentaires
        groupPresenter.getAllComments.observe(this, ::handleGetPostComments)
        groupPresenter.commentPosted.observe(this, ::handleCommentPosted)
        groupPresenter.getCurrentParentPost.observe(this, ::handleParentPost)

        // Charge les commentaires du groupe
        groupPresenter.getPostComments(id, postId)

        // --- Observateur pour la recherche de membres (mention) ---
        // Dès que la requête "searchGroupMembers" a un résultat, on met à jour l'affichage
        groupPresenter.getMembersSearch.observe(this) { members ->
            if (members.isEmpty()) {
                hideMentionSuggestions()
            } else {
                showMentionSuggestions(members)
            }
        }

        // LayoutManager pour la liste de suggestions (mentions)
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)

        // Indique qu'il ne s'agit pas d'un Event
        super.setIsEventFalse()

        // Détection du "@" dans l'EditText
        setupMentionTextWatcher()
    }

    // ---------------------------------------------------------------------------
    // Publication du commentaire
    // ---------------------------------------------------------------------------
    /**
     * On convertit le Spanned en HTML si on détecte <a href="...">,
     * sinon on envoie du texte brut.
     * Puis on restaure la logique qui définit postId et user pour le Post.
     */
    override fun addComment() {
        // 1) Récupère le contenu Spanned de l'EditText
        val spannedText = binding.commentMessage.editableText

        // 2) Convertit en HTML pour garder les éventuelles balises <a>
        val fullHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spannedText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.toHtml(spannedText)
        }

        // 3) Vérifie si on a un lien <a href="...">
        val hasLink = fullHtml.contains("<a href=")

        // 4) Choix final du contenu
        val finalContent = if (hasLink) {
            fullHtml
        } else {
            spannedText.toString()
        }

        // 5) Construit le Post comme dans l'ancienne version
        val currentUserId = EntourageApplication.me(this)?.id ?: 0
        val currentUserAvatar = EntourageApplication.me(this)?.avatarURL

        val user = EntourageUser().apply {
            userId = currentUserId
            avatarURLAsString = currentUserAvatar
        }

        comment = Post(
            idInternal = UUID.randomUUID(),
            content = finalContent,
            postId = postId, // On répond au postId si c'est un commentaire
            user = user
        )

        // 6) Envoi au Presenter => addComment(groupId, comment)
        groupPresenter.addComment(id, comment)

        // 7) Nettoyage de l'EditText
        binding.commentMessage.text.clear()
        Utils.hideKeyboard(this)
    }

    override fun reloadView() {
        lifecycleScope.launch {
            shouldOpenKeyboard = false
            recreate()
        }
    }

    override fun translateView(id: Int) {
        val adapter = binding.comments.adapter as? CommentsListAdapter
        adapter?.translateItem(id)
    }

    // ---------------------------------------------------------------------------
    // Gestion de la liste de commentaires
    // ---------------------------------------------------------------------------
    private fun handleParentPost(currentPost: Post?) {
        this.currentParentPost = currentPost
        binding.progressBar.visibility = View.GONE
        (binding.comments.adapter as? CommentsListAdapter)?.updateData(this.currentParentPost)
        scrollAfterLayout()
        updateView(commentsList.isEmpty())
    }

    override fun handleGetPostComments(allComments: MutableList<Post>?) {
        commentsList.clear()
        allComments?.let { commentsList.addAll(it) }

        updateView(commentsList.isEmpty())

        if (currentParentPost == null) {
            groupPresenter.getCurrentParentPost(id, postId)
            return
        }
        binding.progressBar.visibility = View.GONE
        scrollAfterLayout()
    }

    // ---------------------------------------------------------------------------
    // Détection du "@" pour la fonctionnalité mention => on appelle l'API
    // ---------------------------------------------------------------------------
    private fun setupMentionTextWatcher() {
        binding.commentMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null) return

                val cursorPos = binding.commentMessage.selectionStart
                val substring = s.subSequence(0, cursorPos)
                val lastAt = substring.lastIndexOf('@')

                if (lastAt >= 0) {
                    // On récupère la partie après '@'
                    val mentionQuery = substring.substring(lastAt + 1, cursorPos)
                    lastMentionStartIndex = lastAt

                    // Si mentionQuery est vide => on recherche tous les membres, sinon on filtre côté API
                    groupPresenter.searchGroupMembers(id, mentionQuery)
                } else {
                    hideMentionSuggestions()
                    lastMentionStartIndex = -1
                }
            }
        })
    }

    private fun showMentionSuggestions(members: List<EntourageUser>) {
        binding.mentionSuggestionsContainer.visibility = View.VISIBLE

        val adapter = MentionAdapter(members) { user ->
            insertMentionIntoEditText(user)
        }
        binding.mentionSuggestionsRecycler.adapter = adapter
    }

    private fun hideMentionSuggestions() {
        binding.mentionSuggestionsContainer.visibility = View.GONE
    }

    /**
     * Insère la mention au format <a href="...">@Nom</a> dans l'EditText
     */
    fun insertMentionIntoEditText(user: EntourageUser) {
        val cursorPos = binding.commentMessage.selectionStart
        val editable = binding.commentMessage.editableText ?: return

        if (lastMentionStartIndex < 0) return

        val baseUrl = BuildConfig.ENTOURAGE_URL


        val mentionHtml = """<a href="$baseUrl/app/users/${user.userId}">@${user.displayName}</a>"""
        val mentionSpanned = HtmlCompat.fromHtml(mentionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // On remplace la partie depuis '@' jusqu'à la position du curseur
        editable.replace(lastMentionStartIndex, cursorPos, mentionSpanned)

        // On positionne le curseur juste après la mention
        binding.commentMessage.setSelection(lastMentionStartIndex + mentionSpanned.length)

        hideMentionSuggestions()
        lastMentionStartIndex = -1
    }
}
