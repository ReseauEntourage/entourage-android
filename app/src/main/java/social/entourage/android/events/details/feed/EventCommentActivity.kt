package social.entourage.android.events.details.feed

import android.os.Build
import android.os.Bundle
import android.text.*
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

    // Pour retenir l'index du dernier '@' tapé (on le met en -1 par défaut)
    private var lastMentionStartIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observers pour récupérer les commentaires, parent post, etc.
        eventPresenter.getAllComments.observe(this, ::handleGetPostComments)
        eventPresenter.commentPosted.observe(this, ::handleCommentPosted)
        eventPresenter.getCurrentParentPost.observe(this, ::handleParentPost)

        // Charge les commentaires pour l'Event
        eventPresenter.getPostComments(id, postId)

        // Charge la liste des membres (pour la mention @)
        eventPresenter.getMembers.observe(this) { members ->
            allMembers = members
        }
        eventPresenter.getEventMembers(id)

        // Définit le LayoutManager pour la RecyclerView des suggestions
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)

        // Prépare l'adapter "comments" pour un Event (optionnel, s'il faut faire setForEvent())
        setAdapterForEvent()

        // Met en place la détection de "@" dans l'EditText
        setupMentionTextWatcher()

        // Indique qu'il s'agit d'un event
        super.setIsEventTrue()
    }

    // -------------------------------------------------------------------------------------------
    // Méthodes liées à la publication de commentaires
    // -------------------------------------------------------------------------------------------

    /**
     * Appelée quand l'utilisateur clique sur le bouton "Envoyer" (hérité de CommentActivity).
     * On check si le texte contient une mention (balise <a href="...">). Si oui => envoie du HTML.
     * Sinon => envoie du texte brut.
     * On restaure la logique pour renseigner user et postId dans le Post.
     */
    override fun addComment() {
        // 1) Récupère le Spanned de l'EditText
        val spannedText = binding.commentMessage.editableText

        // 2) Convertit ce Spanned en HTML (pour conserver <a> s'il y en a)
        val fullHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spannedText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.toHtml(spannedText)
        }
        // Par ex: "<p>Hello <a href=\"...\"><b>@Bob</b></a></p>\n"

        // 3) Vérifie s'il y a une balise <a href="...">
        val hasLink = fullHtml.contains("<a href=")

        // 4) On décide du contenu final
        val finalContent = if (hasLink) {
            fullHtml
        } else {
            spannedText.toString()
        }

        // 5) Construit le Post comme dans l'ancienne version,
        //    en fixant user, postId, etc.
        val currentUserId = EntourageApplication.me(this)?.id ?: 0
        val currentUserAvatar = EntourageApplication.me(this)?.avatarURL

        val user = EntourageUser().apply {
            userId = currentUserId
            avatarURLAsString = currentUserAvatar
        }

        // postId => si c'est un commentaire sur un post, on le met
        // sinon, si c'est un "nouveau post", on peut mettre -1 ou 0 => dépend de l'API
        // ici on suppose qu'on répond au post dont l'ID = postId
        comment = Post(
            idInternal = UUID.randomUUID(),
            content = finalContent,
            postId = postId, // On répond au post dont l'ID = postId
            user = user
        )

        // 6) Envoie le commentaire via eventPresenter
        Timber.d("Envoi commentaire => eventId=$id, postId=$postId, content=$finalContent")
        eventPresenter.addComment(id, comment)

        // 7) Nettoyage de l'UI
        binding.commentMessage.text.clear()
        Utils.hideKeyboard(this)
    }

    override fun reloadView() {
        lifecycleScope.launch {
            shouldOpenKeyboard = false
            recreate()
        }
    }

    /**
     * Méthode qui gère la traduction (héritée de CommentActivity).
     */
    override fun translateView(id: Int) {
        val adapter = binding.comments.adapter as? CommentsListAdapter
        adapter?.translateItem(id)
    }

    // -------------------------------------------------------------------------------------------
    // Gestion de l'adapter / datas
    // -------------------------------------------------------------------------------------------

    private fun setAdapterForEvent() {
        if (binding.comments.adapter is CommentsListAdapter) {
            val adapter = binding.comments.adapter as CommentsListAdapter
            adapter.setForEvent()
        }
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

        // Récupération du post parent si pas déjà fait
        if (currentParentPost == null) {
            eventPresenter.getCurrentParentPost(id, postId)
            return
        }
        binding.progressBar.visibility = View.GONE
        scrollAfterLayout()
    }

    // -------------------------------------------------------------------------------------------
    // Logique de mention "@"
    // -------------------------------------------------------------------------------------------
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
                        // L'utilisateur vient de taper '@'
                        showMentionSuggestions(allMembers)
                    } else {
                        // Filtrer sur la saisie
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
        // On limite à 5
        val filtered = allMembers.filter {
            it.displayName?.contains(query, ignoreCase = true) == true
        }.take(5)
        showMentionSuggestions(filtered)
    }

    private fun showMentionSuggestions(members: List<EntourageUser>) {
        if (members.isEmpty()) {
            hideMentionSuggestions()
            return
        }
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
     * Insère la mention <a href="...">@Nom</a> dans l'EditText
     */
    fun insertMentionIntoEditText(user: EntourageUser) {
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
