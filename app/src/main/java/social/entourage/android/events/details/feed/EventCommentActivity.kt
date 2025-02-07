package social.entourage.android.events.details.feed

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
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
    private var allMembers: List<EntourageUser> = emptyList()

    // Pour retenir l'index du dernier '@' tapé (initialisé à -1)
    private var lastMentionStartIndex = -1

    // On crée UNIQUEMENT le MentionAdapter (pour ne pas le recréer à chaque filtrage)
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user ->
            insertMentionIntoEditText(user)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observers pour récupérer commentaires, post parent, etc.
        eventPresenter.getAllComments.observe(this, ::handleGetPostComments)
        eventPresenter.commentPosted.observe(this, ::handleCommentPosted)
        eventPresenter.getCurrentParentPost.observe(this, ::handleParentPost)

        // Charge les commentaires pour l'événement
        eventPresenter.getPostComments(id, postId)

        // Charge la liste des membres (pour la mention)
        eventPresenter.getMembers.observe(this) { members ->
            allMembers = members
        }
        eventPresenter.getEventMembers(id)

        // Configure la RecyclerView des suggestions de mention
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mentionSuggestionsRecycler.adapter = mentionAdapter

        // Configure l'adapter des commentaires (si déjà assigné, on peut aussi appeler setForEvent)
        setAdapterForEvent()

        // Mise en place de la détection du caractère '@'
        setupMentionTextWatcher()

        // Indique qu'il s'agit d'un événement
        super.setIsEventTrue()
    }

    // --- Publication de commentaire ---
    override fun addComment() {
        // 1) Récupère le contenu (Spanned) de l'EditText
        val spannedText = binding.commentMessage.editableText

        // 2) Convertit ce contenu en HTML pour conserver les balises <a> s'il y en a
        val fullHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            Html.toHtml(spannedText, Html.FROM_HTML_MODE_LEGACY)
        else
            Html.toHtml(spannedText)

        // 3) Vérifie la présence d'une balise <a href="...">
        val hasLink = fullHtml.contains("<a href=")

        // 4) Détermine le contenu final à envoyer
        val finalContent = if (hasLink) fullHtml else spannedText.toString()

        // 5) Construit le Post en renseignant l'utilisateur et le postId
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
        eventPresenter.addComment(id, comment)

        // 7) Nettoyage de l'EditText et du clavier
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
        (binding.comments.adapter as? CommentsListAdapter)?.translateItem(id)
    }

    // --- Gestion de l'adapter / données ---
    private fun setAdapterForEvent() {
        (binding.comments.adapter as? CommentsListAdapter)?.setForEvent()
    }

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

    // --- Logique de mention "@"
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
                    if (mentionQuery.isEmpty()) {
                        showMentionSuggestions(allMembers)
                    } else {
                        filterAndShowMentions(mentionQuery)
                    }
                } else {
                    hideMentionSuggestions()
                    lastMentionStartIndex = -1
                }
            }
        })
    }

    private fun filterAndShowMentions(query: String) {
        // On limite à 5 suggestions
        val filtered = allMembers.filter {
            it.displayName?.contains(query, ignoreCase = true) == true
        }.take(5)
        Timber.wtf("filterAndShowMentions: query=$query, found=${filtered.size}")
        showMentionSuggestions(filtered)
    }

    private fun showMentionSuggestions(members: List<EntourageUser>) {
        if (members.isEmpty()) {
            hideMentionSuggestions()
            return
        }
        binding.mentionSuggestionsContainer.visibility = View.VISIBLE
        // On met à jour la liste du mentionAdapter (il s'agit d'une mise à jour fluide)
        mentionAdapter.updateList(members)
    }

    private fun hideMentionSuggestions() {
        binding.mentionSuggestionsContainer.visibility = View.GONE
    }

    /**
     * Insère la mention au format HTML (<a href="...">@Nom</a>) dans l'EditText.
     */
    fun insertMentionIntoEditText(user: EntourageUser) {
        Timber.wtf("insertMentionIntoEditText: user=${user.displayName}")
        val cursorPos = binding.commentMessage.selectionStart
        val editable = binding.commentMessage.editableText ?: return
        if (lastMentionStartIndex < 0) return
        val mentionHtml = """<a href="https://preprod.entourage.social/app/user/${user.userId}">@${user.displayName}</a>"""
        val mentionSpanned = HtmlCompat.fromHtml(mentionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
        editable.replace(lastMentionStartIndex, cursorPos, mentionSpanned)
        binding.commentMessage.setSelection(lastMentionStartIndex + mentionSpanned.length)
        hideMentionSuggestions()
        lastMentionStartIndex = -1
    }
}
