package social.entourage.android.discussions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.Post
import social.entourage.android.comment.CommentActivity
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.comment.MentionAdapter
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import java.util.Locale
import java.util.UUID

/**
 * Classe gérant le détail d’une conversation : liste de messages, mentions, etc.
 */
class DetailConversationActivity : CommentActivity() {

    // --- Variables existantes ---
    private var hasToShowFirstMessage = false
    var hasSeveralpeople = false

    // Nom de la conversation pour mise à jour du header
    private var conversationTitle: String? = null

    // --- Pour la logique de mention ---
    private var allMembers: List<GroupMember> = emptyList()  // liste brute
    private var lastMentionStartIndex = -1                  // dernier '@' détecté

    // Adapter pour suggestions de mention (utilise EntourageUser, on mappe en interne)
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user ->
            insertMentionIntoEditText(user)
        }
    }

    // --- Instanciation du Presenter (selon ton architecture) ---
    private val discussionsPresenter: DiscussionsPresenter by lazy {
        // Selon ta config (ViewModelProvider ou injection)
        DiscussionsPresenter()
    }

    // -----------------------------------------------------------------------------------
    // cycle de vie
    // -----------------------------------------------------------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération éventuelle de certains extras
        hasToShowFirstMessage = intent.getBooleanExtra(Const.HAS_TO_SHOW_MESSAGE, false)

        // Observateurs sur le Presenter
        with(discussionsPresenter) {
            // Observateur : liste des commentaires
            getAllComments.observe(this@DetailConversationActivity) { handleGetPostComments(it) }
            // Observateur : commentaire posté
            commentPosted.observe(this@DetailConversationActivity) { handleCommentPosted(it) }
            // Observateur : détail conversation
            detailConversation.observe(this@DetailConversationActivity) { handleDetailConversation(it) }
        }

        // Charger la conversation et ses messages
        discussionsPresenter.getDetailConversation(id)
        discussionsPresenter.getPostComments(id)

        // Configuration du header
        binding.header.iconSettings.setImageDrawable(resources.getDrawable(R.drawable.new_settings))
        binding.header.cardIconSetting.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.iconSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))

        // Ouvrir le profil auteur si conversation 1-to-1
        if (isOne2One) {
            binding.header.headerTitle.setOnClickListener {
                ProfileFullActivity.isMe = false
                ProfileFullActivity.userId = postAuthorID.toString()
                startActivityForResult(
                    Intent(this, ProfileFullActivity::class.java)
                        .putExtra(Const.USER_ID, postAuthorID),
                    0
                )
            }
        }

        // Mise en place de la RecyclerView pour les suggestions de mention
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mentionSuggestionsRecycler.adapter = mentionAdapter
        setupMentionTextWatcher() // Détection du "@"
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view_detail)
    }

    /**
     * Pour forcer un reload (par ex. après suppression)
     */
    override fun reloadView() {
        lifecycleScope.launch {
            shouldOpenKeyboard = false
            recreate()
        }
    }

    /**
     * Pour activer la traduction d’un item précis
     */
    override fun translateView(id: Int) {
        (binding.comments.adapter as? CommentsListAdapter)?.translateItem(id)
    }

    // -----------------------------------------------------------------------------------
    // détail de la conversation
    // -----------------------------------------------------------------------------------
    private fun handleDetailConversation(conversation: Conversation?) {
        conversation ?: return

        // Parcourt la liste de messages pour détecter l'éventuel rôle "Équipe Entourage"
        conversation.message?.forEach { message ->
            message.userRole?.let { role ->
                if (role.contains("Équipe Entourage")) {
                    hasToShowFirstMessage = false
                }
            }
        }

        // Vérifie si on doit afficher la pop d’info
        checkAndShowPopWarning()

        // Mise à jour du titre de la conversation (header)
        conversationTitle = conversation.title
        binding.header.title = conversationTitle

        // Nombre de membres
        val memberCount = conversation.members?.size ?: 0
        if (memberCount > 2) {
            hasSeveralpeople = true
            val displayName = conversation.user?.displayName ?: ""
            val convMemberCount = conversation.memberCount
            binding.header.title = "$displayName + ${convMemberCount - 1} membres"
        }

        // Bloqueurs ?
        if (conversation.hasBlocker()) {
            binding.postBlocked.isVisible = true
            val _name = conversationTitle ?: ""
            binding.commentBlocked.hint = if (conversation.imBlocker()) {
                String.format(getString(R.string.message_user_blocked_by_me), _name)
            } else {
                String.format(getString(R.string.message_user_blocked_by_other), _name)
            }
        } else {
            binding.postBlocked.isVisible = false
        }

        // -- RÉCUPÉRATION DES MEMBRES POUR LES MENTIONS --
        // conversation.members est de type ArrayList<GroupMember>?
        // On le stocke dans allMembers
        allMembers = conversation.members ?: emptyList()
    }

    private fun checkAndShowPopWarning() {
        if (hasToShowFirstMessage) {
            // Affiche le layout d’information si nécessaire
            binding.layoutInfoNewDiscussion.isVisible = true
            binding.uiIvCloseNew.setOnClickListener {
                binding.layoutInfoNewDiscussion.visibility = View.GONE
            }
        }
    }

    // -----------------------------------------------------------------------------------
    // ajout de commentaire
    // -----------------------------------------------------------------------------------
    override fun addComment() {
        // 1) Récupère le contenu Spanned de l'EditText
        val spannedText = binding.commentMessage.editableText

        // 2) Convertit ce contenu en HTML pour conserver d’éventuelles balises <a>
        val fullHtml = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spannedText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.toHtml(spannedText)
        }

        // 3) Vérifie la présence d’une balise <a href="...">
        val hasLink = fullHtml.contains("<a href=")

        // 4) Contenu final
        val finalContent = if (hasLink) fullHtml else spannedText.toString()

        // 5) Construit le Post pour l’envoi
        val currentUserId = EntourageApplication.me(this)?.id ?: 0
        val currentUserAvatar = EntourageApplication.me(this)?.avatarURL
        val user = EntourageUser().apply {
            userId = currentUserId
            avatarURLAsString = currentUserAvatar
        }
        comment = Post(
            idInternal = UUID.randomUUID(),
            content = finalContent,
            user = user
        )

        // 6) Appel au Presenter pour poster
        discussionsPresenter.addComment(id, comment)

        // 7) Nettoyage du champ
        binding.commentMessage.text.clear()
        Utils.hideKeyboard(this)
    }

    // -----------------------------------------------------------------------------------
    // récupération de la liste de commentaires
    // -----------------------------------------------------------------------------------
    override fun handleGetPostComments(allComments: MutableList<Post>?) {
        val newComments = sortAndExtractDays(allComments, this)
        commentsList.clear()
        newComments?.let { commentsList.addAll(it) }

        binding.progressBar.visibility = View.GONE
        newComments?.isEmpty()?.let { updateView(it) }
        scrollAfterLayout()
    }

    override fun handleReportPost(id: Int, commentLang: String) {
        binding.header.iconSettings.setOnClickListener {
            DataLanguageStock.updatePostLanguage(commentLang)
            AnalyticsEvents.logEvent(AnalyticsEvents.Message_action_param)
            SettingsDiscussionModalFragment.isSeveralPersonneInConversation = this.hasSeveralpeople
            SettingsDiscussionModalFragment.newInstance(
                postAuthorID,
                id,
                isOne2One,
                conversationTitle,
                discussionsPresenter.detailConversation.value?.imBlocker()
            ).show(supportFragmentManager, SettingsDiscussionModalFragment.TAG)
        }
    }

    /**
     * Ajoute dans la liste un séparateur par date, comme dans tes autres activités
     */
    fun sortAndExtractDays(allEvents: MutableList<Post>?, context: Context): MutableList<Post>? {
        val languageCode = LanguageManager.loadLanguageFromPreferences(context)
        val locale = Locale(languageCode)
        val groupedEvents = allEvents?.groupBy { it.getFormatedStr() }
        val newList = ArrayList<Post>()
        groupedEvents?.forEach { (formattedStr, posts) ->
            val datePost = Post().apply {
                isDatePostOnly = true
                datePostText = formattedStr.capitalize(locale)
            }
            newList.add(datePost)
            newList.addAll(posts)
        }
        return newList
    }

    fun updateDiscussion() {
        discussionsPresenter.getDetailConversation(id)
    }

    // -----------------------------------------------------------------------------------
    // LOGIQUE DE DÉTECTION DU "@" POUR LES MENTIONS
    // -----------------------------------------------------------------------------------
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

    /**
     * Filtre la liste des `GroupMember` en fonction de `query`.
     * Comme le `MentionAdapter` gère des `EntourageUser`, on convertit.
     */
    private fun filterAndShowMentions(query: String) {
        val filtered = allMembers.filter { gm ->
            gm.displayName?.contains(query, ignoreCase = true) == true
        }.take(5)

        showMentionSuggestions(filtered)
    }

    /**
     * Convertit la liste de `GroupMember` en liste de `EntourageUser`
     * et l’envoie à l’adapter.
     */
    private fun showMentionSuggestions(members: List<GroupMember>) {
        if (members.isEmpty()) {
            hideMentionSuggestions()
            return
        }
        binding.mentionSuggestionsContainer.visibility = View.VISIBLE

        // Récupérer l'utilisateur courant
        val me = EntourageApplication.me(this)
        // Filtrer les membres pour retirer l'utilisateur courant avant la conversion
        val entourageUsers = members
            .filter { gm -> gm.id != me?.id } // ou gm.id != me.id, selon votre implémentation
            .map { gm ->
                EntourageUser().apply {
                    userId = gm.id ?: 0
                    displayName = gm.displayName
                    avatarURLAsString = gm.avatarUrl
                }
            }
        mentionAdapter.updateList(entourageUsers)
    }


    private fun hideMentionSuggestions() {
        binding.mentionSuggestionsContainer.visibility = View.GONE
    }

    /**
     * Insère la mention au format HTML <a href="...">@Nom</a> dans l’EditText.
     */
    private fun insertMentionIntoEditText(user: EntourageUser) {
        val cursorPos = binding.commentMessage.selectionStart
        val editable = binding.commentMessage.editableText ?: return
        if (lastMentionStartIndex < 0) return

        var baseUrl = "https://" + BuildConfig.DEEP_LINKS_URL
        baseUrl = baseUrl.removeSuffix("/")


        val mentionHtml = """<a href="$baseUrl/app/users/${user.userId}">@${user.displayName}</a>"""
        val mentionSpanned = HtmlCompat.fromHtml(mentionHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)

        // On remplace depuis '@' jusqu'au curseur par la mention
        editable.replace(lastMentionStartIndex, cursorPos, mentionSpanned)

        // Positionne le curseur juste après la mention
        binding.commentMessage.setSelection(lastMentionStartIndex + mentionSpanned.length)

        hideMentionSuggestions()
        lastMentionStartIndex = -1
    }
}
