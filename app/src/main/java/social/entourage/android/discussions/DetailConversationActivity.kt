package social.entourage.android.discussions

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.*
import social.entourage.android.comment.CommentActivity
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.comment.MentionAdapter
import social.entourage.android.discussions.members.MembersConversationFragment
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.language.LanguageManager
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.report.DataLanguageStock
import social.entourage.android.small_talks.SmallTalkGuidelinesActivity
import social.entourage.android.small_talks.SmallTalkListOtherBands
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.VibrationUtil
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class DetailConversationActivity : CommentActivity() {

    companion object {
        /** Mettre à true pour échanger les messages via SmallTalkViewModel */
        var isSmallTalkMode: Boolean = false
        var smallTalkId: String = ""
    }

    // Présenters & ViewModel
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()
    private var refreshMessagesRunnable: Runnable? = null
    private val refreshHandler = android.os.Handler()
    private val refreshIntervalMs = 5000L // 5 secondes

    // UI state
    private var hasToShowFirstMessage = false
    var hasSeveralpeople = false
    private var conversationTitle: String? = null
    private var allMembers: List<GroupMember> = emptyList()
    private var lastMentionStartIndex = -1
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user -> insertMentionIntoEditText(user) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMember = isSmallTalkMode
        hasToShowFirstMessage = intent.getBooleanExtra(Const.HAS_TO_SHOW_MESSAGE, false)
        binding.emptyState.visibility = View.GONE

        setupHeader()
        setupMentionList()
        setupMentionTextWatcher()

        // Toujours charger le détail de la conversation (titre, membres, événement…)
        discussionsPresenter.detailConversation.observe(this) { handleDetailConversation(it) }
        eventPresenter.getEvent.observe(this) { handleGetEvent(it) }

        // Messages : selon le mode
        if (isSmallTalkMode) {
            smallTalkViewModel.smallTalkDetail.observe(this){handleSmallTakDetail(it)}
            smallTalkViewModel.messages.observe(this) { handleSmallTalkMessages(it) }
            smallTalkViewModel.participants.observe(this) { handleParticipants(it) }
            smallTalkViewModel.createdMessage.observe(this) {
                scrollAfterLayout()
            }
            smallTalkViewModel.getSmallTalk(smallTalkId)
            smallTalkViewModel.listChatMessages(smallTalkId)
            smallTalkViewModel.listSmallTalkParticipants(smallTalkId)
            binding.btnSeeEvent.text = getString(R.string.small_talk_btn_charte)
            binding.layoutEventConv.visibility = View.VISIBLE
            binding.btnSeeEvent.setOnClickListener {
                VibrationUtil.vibrate(this)
                startActivity(
                    Intent(this, SmallTalkGuidelinesActivity::class.java)
                )
            }

        } else {
            discussionsPresenter.getDetailConversation(id)
            discussionsPresenter.getAllComments.observe(this) { handleGetPostComments(it) }
            discussionsPresenter.commentPosted.observe(this) { handleCommentPosted(it) }
            discussionsPresenter.getPostComments(id)
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view_detail)
        startRefreshingMessages()

    }

    override fun onPause() {
        super.onPause()
        stopRefreshingMessages()
    }

    override fun reloadView() {
        lifecycleScope.launch {
            shouldOpenKeyboard = false
            recreate()
        }
    }

    private fun startRefreshingMessages() {
        refreshMessagesRunnable = object : Runnable {
            override fun run() {
                if (isSmallTalkMode) {
                    smallTalkViewModel.listChatMessages(smallTalkId)
                } else {
                    discussionsPresenter.getPostComments(id)
                }
                refreshHandler.postDelayed(this, refreshIntervalMs)
            }
        }
        refreshHandler.postDelayed(refreshMessagesRunnable!!, refreshIntervalMs)
    }

    private fun stopRefreshingMessages() {
        refreshMessagesRunnable?.let { refreshHandler.removeCallbacks(it) }
    }

    override fun translateView(id: Int) {
        (binding.comments.adapter as? CommentsListAdapter)?.translateItem(id)
    }

    // --- Header setup ---
    private fun setupHeader() {
        binding.header.iconSettings.setImageDrawable(resources.getDrawable(R.drawable.new_settings))
        binding.header.cardIconSetting.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.iconSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
    }

    private fun handleSmallTakDetail(smallTalk: SmallTalk?) {
        binding.postBlocked.isVisible = false

    }
    // --- SmallTalk messages mapping ---
    private fun handleSmallTalkMessages(messages: List<Post>?) {
        handleGetPostComments(messages?.toMutableList())
    }

    private fun handleParticipants(participants: List<User>?) {
        val currentUserId = EntourageApplication.get().me()?.id
        val names = participants
            ?.filter { it?.id != currentUserId }
            ?.take(5)
            ?.mapNotNull { it?.displayName }
            ?.map { if (it.length > 2) it.dropLast(3) else it }
        binding.header.title = names?.joinToString(", ") ?: ""
        allMembers = participants?.map { it.toGroupMember() } ?: emptyList()
    }

    // --- Discussion detail (inchangé) ---
    private fun handleDetailConversation(conversation: Conversation?) {
        conversation ?: return
        isMember = conversation.member == true
        updateView(false)
        if (conversation.memberCount > 2) {
            isOne2One = false
        }
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
        } else {
            binding.header.headerTitle.setOnClickListener {
                MembersConversationFragment.newInstance(id).show(supportFragmentManager, "")
            }
        }
        if (conversation.type == "outing" || isSmallTalkMode ) {
            binding.layoutEventConv.visibility = View.VISIBLE
            binding.layoutInfoNewDiscussion.visibility = View.GONE
            conversation.id?.let { eventPresenter.getEvent(it.toString()) }
        } else {
            binding.layoutEventConv.visibility = View.GONE
            binding.header.headerSubtitle.visibility = View.GONE
            SettingsDiscussionModalFragment.isEvent = false
        }

        conversation.message?.forEach { msg ->
            msg.userRole?.let { role ->
                if (role.contains("Équipe Entourage")) {
                    hasToShowFirstMessage = false
                }
            }
        }

        checkAndShowPopWarning()

        conversationTitle = conversation.title
        binding.header.title = conversationTitle

        val memberCount = conversation.members?.size ?: 0
        if (memberCount > 2) {
            hasSeveralpeople = true
            val displayName = conversation.user?.displayName ?: ""
            binding.header.title = "$displayName + ${conversation.memberCount - 1} membres"
        }
        if (conversation.memberCount > 2 && conversation.members != null) {
            val currentUserId = EntourageApplication.get().me()?.id
            val names = conversation.members
                .filter { it?.id != currentUserId }
                .take(5)
                .mapNotNull { it?.displayName }
                .map { if (it.length > 2) it.dropLast(3) else it }
            binding.header.title = names.joinToString(", ")
        } else {
            binding.header.title = conversation.title
        }
        if (conversation.hasBlocker()) {
            binding.postBlocked.isVisible = true
            val name = conversationTitle ?: ""
            binding.commentBlocked.hint = if (conversation.imBlocker()) {
                String.format(getString(R.string.message_user_blocked_by_me), name)
            } else {
                String.format(getString(R.string.message_user_blocked_by_other), name)
            }
        } else {
            binding.postBlocked.isVisible = false
        }
        allMembers = conversation.members ?: emptyList()
    }

    // --- Event detail (inchangé) ---
    private fun handleGetEvent(event: Events?) {
        binding.emptyState.visibility = View.GONE
        event?.let {
            SettingsDiscussionModalFragment.isEvent = true
            binding.header.headerTitle.setOnClickListener {
                VibrationUtil.vibrate(this)
                startActivity(
                    Intent(this, EventFeedActivity::class.java)
                        .putExtra(Const.EVENT_ID, event.id)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }
            binding.btnSeeEvent.setOnClickListener {
                VibrationUtil.vibrate(this)
                startActivity(
                    Intent(this, EventFeedActivity::class.java)
                        .putExtra(Const.EVENT_ID, event.id)
                        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                )
            }
            if (event.metadata?.portraitThumbnailUrl.isNullOrBlank()) {
                binding.header.ivEvent.visibility = View.GONE
                Glide.with(this).load(R.drawable.placeholder_my_event).into(binding.header.ivEvent)
            } else {
                binding.header.ivEvent.visibility = View.GONE
                Glide.with(this)
                    .load(event.metadata?.portraitThumbnailUrl)
                    .transform(RoundedCorners(10))
                    .error(R.drawable.placeholder_my_event)
                    .into(binding.header.ivEvent)
            }
            binding.header.headerSubtitle.visibility = View.VISIBLE
            binding.header.headerTitle.text = event.title
            binding.header.headerSubtitle.text = formatDate(event.metadata?.startsAt.toString())
            binding.emptyState.visibility = View.GONE
        }
    }

    // --- Envoi message / commentaire ---
    override fun addComment() {
        val spanned = binding.commentMessage.editableText
        val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spanned, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.toHtml(spanned)
        }
        val content = if (html.contains("<a href=")) html else spanned.toString()

        if (isSmallTalkMode) {
            smallTalkViewModel.createChatMessage(smallTalkId, content)
        } else {
            val currentUserId = EntourageApplication.me(this)?.id ?: 0
            val currentUserAvatar = EntourageApplication.me(this)?.avatarURL
            val user = EntourageUser().apply {
                userId = currentUserId
                avatarURLAsString = currentUserAvatar
            }
            comment = Post(
                idInternal = UUID.randomUUID(),
                content = content,
                user = user
            )
            discussionsPresenter.addComment(id, comment)
        }

        binding.commentMessage.text.clear()
        Utils.hideKeyboard(this)
    }

    // --- Récupération / affichage commentaires ---
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
            SettingsDiscussionModalFragment.isSeveralPersonneInConversation = hasSeveralpeople
            SettingsDiscussionModalFragment.newInstance(
                postAuthorID,
                id,
                isOne2One,
                conversationTitle,
                discussionsPresenter.detailConversation.value?.imBlocker()
            ).show(supportFragmentManager, SettingsDiscussionModalFragment.TAG)
        }
    }

    // --- Suppression de message (appel adapter) ---
    fun deleteMessage(messageId: String) {
        if (isSmallTalkMode) smallTalkViewModel.deleteChatMessage(smallTalkId, messageId)
        else discussionsPresenter.deleteMessage(id, messageId.toInt())
    }

    // --- Mentions infra ---
    private fun setupMentionList() {
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mentionSuggestionsRecycler.adapter = mentionAdapter
    }

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
                    val query = substring.substring(lastAt + 1, cursorPos)
                    lastMentionStartIndex = lastAt
                    if (query.isEmpty()) showMentionSuggestions(allMembers)
                    else filterAndShowMentions(query)
                } else {
                    hideMentionSuggestions()
                    lastMentionStartIndex = -1
                }
            }
        })
    }

    private fun showMentionSuggestions(members: List<GroupMember>) {
        if (members.isEmpty()) {
            hideMentionSuggestions()
            return
        }
        binding.mentionSuggestionsContainer.visibility = View.VISIBLE
        val me = EntourageApplication.me(this)
        val users = members.filter { it.id != me?.id }.map {
            EntourageUser().apply {
                userId = it.id ?: 0
                displayName = it.displayName
                avatarURLAsString = it.avatarUrl
            }
        }
        mentionAdapter.updateList(users)
    }

    private fun filterAndShowMentions(query: String) {
        val filtered = allMembers.filter { it.displayName?.contains(query, true) == true }.take(5)
        showMentionSuggestions(filtered)
    }

    private fun hideMentionSuggestions() {
        binding.mentionSuggestionsContainer.visibility = View.GONE
    }

    private fun insertMentionIntoEditText(user: EntourageUser) {
        val cursorPos = binding.commentMessage.selectionStart
        val editable = binding.commentMessage.editableText ?: return
        if (lastMentionStartIndex < 0) return
        val baseUrl = "https://${BuildConfig.DEEP_LINKS_URL}".removeSuffix("/")
        val html = """<a href="$baseUrl/app/users/${user.userId}">@${user.displayName}</a>"""
        val span = HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
        editable.replace(lastMentionStartIndex, cursorPos, span)
        binding.commentMessage.setSelection(lastMentionStartIndex + span.length)
        hideMentionSuggestions()
        lastMentionStartIndex = -1
    }

    // --- Utilitaires ---
    fun sortAndExtractDays(allEvents: MutableList<Post>?, context: Context): MutableList<Post>? {
        val code = LanguageManager.loadLanguageFromPreferences(context)
        val loc = Locale(code)
        val grouped = allEvents?.groupBy { it.getFormatedStr() }
        val out = ArrayList<Post>()
        grouped?.forEach { (dateStr, posts) ->
            val sep = Post().apply {
                isDatePostOnly = true
                datePostText = dateStr.replaceFirstChar { it.uppercaseChar() }
            }
            out.add(sep)
            out.addAll(posts)
        }
        return out
    }

    fun formatDate(inputDate: String): String {
        val fmt = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
        val date = fmt.parse(inputDate) ?: return ""
        val cal = Calendar.getInstance().apply { time = date }
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val dow = cal.get(Calendar.DAY_OF_WEEK)
        val mon = cal.get(Calendar.MONTH)
        val year = cal.get(Calendar.YEAR)
        val dayName = when (dow) {
            Calendar.MONDAY -> getString(R.string.enhanced_onboarding_time_disponibility_day_monday)
            Calendar.TUESDAY -> getString(R.string.enhanced_onboarding_time_disponibility_day_tuesday)
            Calendar.WEDNESDAY -> getString(R.string.enhanced_onboarding_time_disponibility_day_wednesday)
            Calendar.THURSDAY -> getString(R.string.enhanced_onboarding_time_disponibility_day_thursday)
            Calendar.FRIDAY -> getString(R.string.enhanced_onboarding_time_disponibility_day_friday)
            Calendar.SATURDAY -> getString(R.string.enhanced_onboarding_time_disponibility_day_saturday)
            Calendar.SUNDAY -> getString(R.string.enhanced_onboarding_time_disponibility_day_sunday)
            else -> ""
        }
        val monName = when (mon) {
            Calendar.JANUARY -> getString(R.string.january)
            Calendar.FEBRUARY -> getString(R.string.february)
            Calendar.MARCH -> getString(R.string.march)
            Calendar.APRIL -> getString(R.string.april)
            Calendar.MAY -> getString(R.string.may)
            Calendar.JUNE -> getString(R.string.june)
            Calendar.JULY -> getString(R.string.july)
            Calendar.AUGUST -> getString(R.string.august)
            Calendar.SEPTEMBER -> getString(R.string.september)
            Calendar.OCTOBER -> getString(R.string.october)
            Calendar.NOVEMBER -> getString(R.string.november)
            Calendar.DECEMBER -> getString(R.string.december)
            else -> ""
        }
        return "$dayName $day $monName $year"
    }

    private fun checkAndShowPopWarning() {
        if (hasToShowFirstMessage) {
            binding.layoutInfoNewDiscussion.isVisible = true
            binding.uiIvCloseNew.setOnClickListener {
                binding.layoutInfoNewDiscussion.visibility = View.GONE
            }
        }
    }

    fun updateDiscussion() {
        discussionsPresenter.getDetailConversation(id)
    }

    override fun onDestroy() {
        super.onDestroy()
        isSmallTalkMode = false
        smallTalkId = ""
    }
}
