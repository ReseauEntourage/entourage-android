package social.entourage.android.events.details.feed

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
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
import social.entourage.android.events.EventsPresenter
import social.entourage.android.tools.utils.Utils
import timber.log.Timber
import java.util.UUID

class EventCommentActivity : CommentActivity() {

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    // Retient l'index du dernier '@' tapé. -1 => pas de mention en cours
    private var lastMentionStartIndex = -1

    // Adapter qui affichera les suggestions de mention
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user ->
            insertMentionIntoEditText(user)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observe la liste de commentaires
        eventPresenter.getAllComments.observe(this, ::handleGetPostComments)
        eventPresenter.commentPosted.observe(this, ::handleCommentPosted)
        eventPresenter.getCurrentParentPost.observe(this, ::handleParentPost)

        // Récupère les commentaires existants
        eventPresenter.getPostComments(id, postId)

        // 1) Observe le résultat distant de la recherche mention
        eventPresenter.getMembersSearch.observe(this) { members ->
            if (members.isEmpty()) {
                hideMentionSuggestions()
            } else {
                showMentionSuggestions(members)
            }
        }

        // 2) Configure la recyclerView pour les suggestions
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mentionSuggestionsRecycler.adapter = mentionAdapter

        // 3) Marque qu'il s'agit d'un Event
        super.setIsEventTrue()

        // 4) Ajoute un TextWatcher pour détecter les '@'
        setupMentionTextWatcher()

        // 5) Configure l'adapter de commentaires pour l'événement
        setAdapterForEvent()
    }
    override fun onResume() {
        super.onResume()
        this.isEvent = true
    }

    private fun animateMentionSuggestions(show: Boolean) {
        val container = binding.mentionSuggestionsContainer

        // Si la vue n'est pas visible, la rendre visible temporairement pour qu'elle soit mesurée
        if (container.visibility != View.VISIBLE) {
            container.visibility = View.INVISIBLE
            container.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        }

        val targetHeight = container.measuredHeight

        if (show) {
            // Prépare la vue pour l'animation : elle commence en bas et est invisible
            container.apply {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = targetHeight.toFloat()
            }
            // Animation d'apparition
            container.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        } else {
            // Animation de sortie vers le bas
            container.animate()
                .translationY(targetHeight.toFloat())
                .alpha(0f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction { container.visibility = View.GONE }
                .start()
        }
    }


    // ---------------------------------------------------------------------------
    // Publication du commentaire
    // ---------------------------------------------------------------------------
    override fun addComment() {
        // 1) Récupère le contenu (Spanned) de l'EditText
        val spannedText = binding.commentMessage.editableText

        // 2) Convertit ce contenu en HTML pour conserver <a href="..."> si présent
        val fullHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spannedText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.toHtml(spannedText)
        }

        // 3) Vérifie s'il y a une balise <a href="...">
        val hasLink = fullHtml.contains("<a href=")

        // 4) Choix final : HTML ou texte brut
        val finalContent = if (hasLink) fullHtml else spannedText.toString()

        // 5) Construit le Post
        val currentUserId = EntourageApplication.me(this)?.id ?: 0
        val currentUserAvatar = EntourageApplication.me(this)?.avatarURL
        val user = EntourageUser().apply {
            userId = currentUserId
            avatarURLAsString = currentUserAvatar
        }

        comment = Post(
            idInternal = UUID.randomUUID(),
            content = finalContent,
            postId = postId,
            user = user
        )

        Timber.d("Envoi commentaire => eventId=$id, postId=$postId, content=$finalContent")

        // 6) Envoie au presenter
        eventPresenter.addComment(id, comment)

        // 7) Nettoyage
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
    // Gestion de l'adapter
    // ---------------------------------------------------------------------------
    private fun setAdapterForEvent() {
        (binding.comments.adapter as? CommentsListAdapter)?.setForEvent()
    }


    // ---------------------------------------------------------------------------
    // Gestion du post parent
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
            eventPresenter.getCurrentParentPost(id, postId)
            return
        }
        binding.progressBar.visibility = View.GONE
        scrollAfterLayout()
    }

    // ---------------------------------------------------------------------------
    // Détection du "@"
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
                    val mentionQuery = substring.substring(lastAt + 1, cursorPos)
                    lastMentionStartIndex = lastAt

                    // On appelle la recherche distante
                    // - Si mentionQuery est vide => on peut envoyer query = "" pour tout récupérer
                    eventPresenter.searchEventMembers(id, mentionQuery)
                } else {
                    hideMentionSuggestions()
                    lastMentionStartIndex = -1
                }
            }
        })
    }

    private fun showMentionSuggestions(members: List<EntourageUser>) {
        // Récupérer l'utilisateur courant
        val me = EntourageApplication.me(this)
        // Filtrer la liste pour retirer l'utilisateur courant
        val filteredMembers = members.filter { it.id != me?.id?.toLong() }

        mentionAdapter.updateList(filteredMembers)
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

        // Nettoyer le displayName pour ne conserver que des lettres (Unicode inclus)
        val cleanedDisplayName = user.displayName?.replace(Regex("[^\\p{L}]"), "") + ". "

        val mentionHtml = """<a href="$baseUrl/app/users/${user.userId}">@${cleanedDisplayName}</a>"""
        val mentionSpanned = HtmlCompat.fromHtml(mentionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Remplace la partie depuis '@' jusqu'au curseur
        editable.replace(lastMentionStartIndex, cursorPos, mentionSpanned)

        // Positionne le curseur juste après la mention
        binding.commentMessage.setSelection(lastMentionStartIndex + mentionSpanned.length)

        hideMentionSuggestions()
        lastMentionStartIndex = -1
        smoothScrollCommentsToBottom()
    }

}
