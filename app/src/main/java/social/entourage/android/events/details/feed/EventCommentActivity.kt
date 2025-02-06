package social.entourage.android.events.details.feed

import android.os.Build
import android.os.Bundle
import android.text.*
import android.view.View
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Post
import social.entourage.android.comment.CommentActivity
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.comment.MentionAdapter
import social.entourage.android.databinding.ActivityCommentsBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.tools.utils.Utils
import timber.log.Timber

class EventCommentActivity : CommentActivity() {

    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var allMembers: List<EntourageUser> = emptyList()

    // Retient l'index du dernier '@' tapé. -1 = pas de mention en cours
    private var lastMentionStartIndex = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Observers pour la récupération des commentaires et l'ajout de commentaire
        eventPresenter.getAllComments.observe(this, ::handleGetPostComments)
        eventPresenter.commentPosted.observe(this, ::handleCommentPosted)
        eventPresenter.getCurrentParentPost.observe(this, ::handleParentPost)

        // Initialisation de la liste des commentaires pour l'Event
        eventPresenter.getPostComments(id, postId)

        // Récupération de la liste des membres (pour les mentions)
        eventPresenter.getMembers.observe(this) { members ->
            allMembers = members
        }
        eventPresenter.getEventMembers(id)

        // Définit le LayoutManager pour la liste de suggestions (@mentions)
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)

        // Prépare l'Adapter "comments" pour un Event
        setAdapterForEvent()

        // Met en place la détection du '@' dans l'EditText
        setupMentionTextWatcher()

        // Indique qu'il s'agit d'un event (hérité de CommentActivity)
        super.setIsEventTrue()
    }

    // -------------------------------------------------------------------------------------------
    // Méthodes liées à la publication de commentaires
    // -------------------------------------------------------------------------------------------

    /**
     * Appelée quand l'utilisateur clique sur le bouton Envoyer (hérité de CommentActivity).
     * Ici, on détecte si le texte contient des mentions (balises <a href="...">)
     * et on envoie du HTML uniquement dans ce cas. Sinon, on envoie le texte brut.
     */
    override fun addComment() {
        // 1) Récupère le contenu Spanned de l'EditText
        val spannedText = binding.commentMessage.editableText

        // 2) Convertit ce Spanned en HTML (garder les <a> si elles existent)
        val fullHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spannedText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.toHtml(spannedText)
        }
        // Exemple : "<p>Salut <a href=\"...\"><b>@Bob</b></a></p>\n" etc.

        // 3) Vérifie si on a une balise <a href="..."> => la preuve qu'il y a une mention
        val hasLink = fullHtml.contains("<a href=")

        // 4) Choix du content : HTML si mention(s), sinon texte brut
        val finalContent = if (hasLink) {
            // Envoie tout le HTML
            fullHtml
        } else {
            // Envoie du texte normal (sans balises)
            spannedText.toString()
        }

        // 5) Construire le Post (hérité de CommentActivity, variable "comment")
        comment = Post(
            content = finalContent
        )

        // 6) On envoie le commentaire via l'EventsPresenter
        eventPresenter.addComment(id, comment)

        // 7) Nettoyage éventuel du champ
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
     * Méthode qui gère l'éventuelle traduction (hérité de CommentActivity).
     */
    override fun translateView(id: Int) {
        val adapter = binding.comments.adapter as? CommentsListAdapter
        adapter?.translateItem(id)
    }

    // -------------------------------------------------------------------------------------------
    // Gestion de l'adapter et des données
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

        if (currentParentPost == null) {
            eventPresenter.getCurrentParentPost(id, postId)
            return
        }

        binding.progressBar.visibility = View.GONE
        scrollAfterLayout()
    }

    // -------------------------------------------------------------------------------------------
    // Détection du '@' pour la fonctionnalité de mention
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
                        // Juste '@'
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

    /**
     * Filtre dans la liste des membres ceux dont le displayName contient query
     * (ignoreCase) et limite à 5 résultats.
     */
    private fun filterAndShowMentions(query: String) {
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
     * Insère une mention HTML <a href="...">@Nom</a> dans l'EditText
     */
    fun insertMentionIntoEditText(user: EntourageUser) {
        val cursorPos = binding.commentMessage.selectionStart
        val editable = binding.commentMessage.editableText ?: return

        // Vérifie qu'on est bien en pleine saisie de mention
        if (lastMentionStartIndex < 0) return

        // Construit la balise <a href="...">@Nom</a>
        val mentionHtml = """<a href="https://preprod.entourage.social/app/user/${user.userId}">@${user.displayName}</a>"""
        // Convertit en Spanned
        val mentionSpanned = HtmlCompat.fromHtml(mentionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // Remplace le texte depuis '@' jusqu'à la position courante par la balise <a>
        editable.replace(lastMentionStartIndex, cursorPos, mentionSpanned)

        // Place le curseur juste après la mention
        binding.commentMessage.setSelection(lastMentionStartIndex + mentionSpanned.length)

        // Ferme la popup
        hideMentionSuggestions()
        lastMentionStartIndex = -1
    }
}
