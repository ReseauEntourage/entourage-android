package social.entourage.android.groups.details.feed

import android.os.Build
import android.os.Bundle
import android.text.*
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
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
    private fun animateMentionSuggestions(show: Boolean) {
        val container = binding.mentionSuggestionsContainer

        // Si la hauteur est nulle, attendre la fin du layout
        if (container.height == 0) {
            container.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    animateMentionSuggestions(show)
                }
            })
            return
        }

        val targetHeight = container.height.toFloat()

        if (show) {
            // Prépare la vue pour l'apparition
            container.apply {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = targetHeight
            }
            // Animation d'apparition : slide up + fondu
            container.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } else {
            // Animation de disparition : slide down + fondu
            container.animate()
                .translationY(targetHeight)
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { container.visibility = View.GONE }
                .start()
        }
    }


    private fun showMentionSuggestions(members: List<EntourageUser>) {
        // Récupérer l'utilisateur courant
        val me = EntourageApplication.me(this)
        // Filtrer la liste pour retirer l'utilisateur courant
        val filteredMembers = members.filter { it.id != me?.id?.toLong() }

        binding.mentionSuggestionsContainer.visibility = View.VISIBLE

        val adapter = MentionAdapter(filteredMembers) { user ->
            insertMentionIntoEditText(user)
        }
        binding.mentionSuggestionsRecycler.adapter = adapter
        animateMentionSuggestions(true)
    }

    private fun hideMentionSuggestions() {
        animateMentionSuggestions(false)
    }

    private fun smoothScrollCommentsToBottom() {
        binding.comments.adapter?.let { adapter ->
            if (adapter.itemCount > 0) {
                binding.comments.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }
    }


    /**
     * Insère la mention au format <a href="...">@Nom</a> dans l'EditText
     */
    fun insertMentionIntoEditText(user: EntourageUser) {
        val cursorPos = binding.commentMessage.selectionStart
        val editable = binding.commentMessage.editableText ?: return

        if (lastMentionStartIndex < 0) return

        var baseUrl = "https://" + BuildConfig.DEEP_LINKS_URL
        baseUrl = baseUrl.removeSuffix("/")

        // Clean du displayName : on garde lettres et chiffres seulement
        val cleanedDisplayName = user.displayName
            ?.replace(Regex("[^\\p{L}\\p{N}]"), "") // supprime tout sauf lettres/chiffres
            ?: "membre"

        // Génère une seule balise avec @Nom et un espace après
        val mentionHtml = """<a href="$baseUrl/app/users/${user.userId}">@${cleanedDisplayName}</a>&nbsp;"""
        val mentionSpanned = HtmlCompat.fromHtml(mentionHtml, HtmlCompat.FROM_HTML_MODE_COMPACT)

        // Remplace dans l’EditText
        editable.replace(lastMentionStartIndex, cursorPos, mentionSpanned)

        // Place le curseur juste après la mention
        binding.commentMessage.setSelection(lastMentionStartIndex + mentionSpanned.length)
        hideMentionSuggestions()
        lastMentionStartIndex = -1
        smoothScrollCommentsToBottom()
    }
}
