package social.entourage.android.discussions

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
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
import social.entourage.android.tools.view.WebViewFragment
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class DetailConversationActivity : CommentActivity() {

    companion object {
        /** Mettre à true pour échanger les messages via SmallTalkViewModel */
        var isSmallTalkMode: Boolean = false
        var smallTalkId: String = ""
        var shouldResetToHome: Boolean = false
    }
    // Présenters & ViewModel
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()
    private var refreshMessagesRunnable: Runnable? = null
    private val refreshHandler = android.os.Handler()
    private val refreshIntervalMs = 5000L // 5 secondes
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private var photoUri: Uri? = null
    private var detailConversation: Conversation? = null
    private var smallTalk: SmallTalk? = null
    private var itemDeletedId = ""
    // UI state
    private var hasToShowFirstMessage = false
    var hasSeveralpeople = false
    private var conversationTitle: String? = null
    private var allMembers: List<GroupMember> = emptyList()
    private var lastMentionStartIndex = -1
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user -> insertMentionIntoEditText(user) }
    }
    private var event: Events? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMember = isSmallTalkMode
        hasToShowFirstMessage = intent.getBooleanExtra(Const.HAS_TO_SHOW_MESSAGE, false)
        binding.emptyState.visibility = View.GONE
        setOptions()
        setupHeader()
        setupMentionList()
        setupMentionTextWatcher()
        observeDeletedMessage()

        // Toujours charger le détail de la conversation (titre, membres, événement…)
        discussionsPresenter.detailConversation.observe(this) { handleDetailConversation(it) }
        eventPresenter.getEvent.observe(this) { handleGetEvent(it) }

        binding.comments.layoutManager = LinearLayoutManager(this)
        setupScrollPagination() // 👈 Ajout scroll top pagination

        if (isSmallTalkMode) {
            smallTalkViewModel.smallTalkDetail.observe(this) { handleSmallTakDetail(it) }
            smallTalkViewModel.messages.observe(this) { handleSmallTalkMessages(it) }
            smallTalkViewModel.participants.observe(this) { handleParticipants(it) }
            smallTalkViewModel.createdMessage.observe(this) {
                scrollAfterLayout()
            }
            smallTalkViewModel.getSmallTalk(smallTalkId)
            smallTalkViewModel.loadInitialMessages(smallTalkId) // 👈 Nouveau chargement avec pagination
            smallTalkViewModel.listSmallTalkParticipants(smallTalkId)

            binding.btnSeeEvent.text = getString(R.string.small_talk_btn_charte)
            binding.ivBtnEvent.apply {
                imageTintList = null
                imageTintMode = null
                clearColorFilter()
                setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_book))
            }
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

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                showThumbnail(photoUri!!)
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                showThumbnail(uri)
            }
        }
    }

    private fun setupScrollPagination() {
        (binding.comments.layoutManager as? LinearLayoutManager)?.let { layoutManager ->
            binding.comments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (!isSmallTalkMode) return

                    val firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                    if (firstVisibleItem <= 2) {
                        Timber.d("🌀 Scroll top détecté, chargement messages SmallTalk")
                        smallTalkViewModel.loadMoreMessagesIfPossible(smallTalkId)
                    }
                }
            })
        }
    }


    private fun observeDeletedMessage() {
        smallTalkViewModel.messageDeleted.observe(this) {
            if (it) smallTalkViewModel.listChatMessages(smallTalkId)
        }

        eventPresenter.isEventDeleted.observe(this) {
            eventPresenter.getEvent(this.event?.id.toString())
        }

        discussionsPresenter.isMessageDeleted.observe(this) {
            if (it) discussionsPresenter.getPostComments(id)
        }
    }

    private fun handleDeletedMessage(boolean: Boolean) {

    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view_detail)
        startRefreshingMessages()

    }



    private fun generateJitsiUrl(displayName: String, roomName: String = "Bonnes ondes " + smallTalk?.uuid): String {
        val encodedDisplayName = displayName
            .replace(" ", "%20")
            .replace("\"", "%22")
        val baseUrl = "https://meet.jit.si/$roomName"
        val params = "#userInfo.displayName=%22$encodedDisplayName%22&config.startWithAudioMuted=false&config.startWithVideoMuted=false"
        return "$baseUrl$params"
    }

/*    private fun setCameraIcon() {
        val roomName = "Bonnes ondes " + smallTalk?.uuid
        val displayName = "Invité"

        val url = generateJitsiUrl(displayName, roomName)
        //smallTalk?.meetingUrl = url

        binding.header.iconCamera.isVisible = true
        binding.header.iconCamera.setImageResource(R.drawable.ic_camera)
        binding.header.cardIconCamera.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.iconCamera.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))

        binding.header.iconCamera.setOnClickListener {
            Timber.wtf("📹 Lancement visio Jitsi URL : $url")
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__VISIO_ICON)
            WebViewFragment.launchURL(this, url)
        }
    }*/


    private fun setCameraIcon() {
        binding.header.iconCamera.isVisible = !smallTalk?.meetingUrl.isNullOrBlank()
        binding.header.iconCamera.setImageResource(R.drawable.ic_camera)
        binding.header.cardIconCamera.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.iconCamera.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent))
        binding.header.iconCamera.setOnClickListener {
            Timber.wtf("eho url : ${smallTalk?.meetingUrl}")
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__VISIO_ICON)
            smallTalk?.meetingUrl?.let { url ->
                WebViewFragment.launchURL(this, url)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        stopRefreshingMessages()
    }

    override fun reloadView() {
        shouldOpenKeyboard = false

        when {
            isSmallTalkMode -> {
                smallTalkViewModel.listChatMessages(smallTalkId)
            }
            detailConversation?.type == "outing" -> {
                // recharge les commentaires de la discussion…
                discussionsPresenter.getPostComments(id)
                // …et si tu veux mettre à jour aussi l’entête événement
                detailConversation?.id
                    ?.toString()
                    ?.let { eventPresenter.getEvent(it) }
            }
            else -> {
                discussionsPresenter.getPostComments(id)
            }
        }
    }


    private fun isAtBottom(): Boolean {
        val layoutManager = binding.comments.layoutManager as? LinearLayoutManager ?: return true
        val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount
        return lastVisibleItem >= totalItemCount - 2 // on tolère 1-2 items de marge
    }

    fun setOptions() {
        var isOptionsVisible = false

        binding.optionButton.setOnClickListener {
            isOptionsVisible = !isOptionsVisible

            if (isOptionsVisible) {
                // Animation du layout
                val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
                binding.layoutOption.startAnimation(slideIn)
                binding.layoutOption.visibility = View.VISIBLE

                // Rotation du bouton
                val rotate = ObjectAnimator.ofFloat(binding.optionButton, View.ROTATION, 0f, 180f)
                rotate.duration = 300
                rotate.doOnEnd {
                    binding.optionButton.setImageDrawable(getDrawable(R.drawable.ic_conversation_more_option))
                }
                rotate.start()

            } else {
                // Animation de sortie
                val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
                binding.layoutOption.startAnimation(slideOut)

                // Cache le layout après l’animation
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.layoutOption.visibility = View.GONE
                }, 250)

                // Rotation retour
                val rotateBack = ObjectAnimator.ofFloat(binding.optionButton, View.ROTATION, 180f, 0f)
                rotateBack.duration = 300
                rotateBack.start()
            }
        }

        // Initialisation des options
        binding.optionCamera.ivOption.setImageDrawable(getDrawable(R.drawable.ic_conversation_option_photo))
        binding.optionGalery.ivOption.setImageDrawable(getDrawable(R.drawable.ic_conversation_option_galerie))
        binding.optionCamera.tvOption.setText(R.string.discussion_option_camera)
        binding.optionGalery.tvOption.setText(R.string.discussion_option_gallery)
        binding.optionCamera.layoutParent.setOnClickListener {
            photoUri = createImageUri()
            cameraLauncher.launch(photoUri)
        }

        binding.optionGalery.layoutParent.setOnClickListener {
            galleryLauncher.launch("image/*")
        }
    }

    private fun createImageUri(): Uri {
        val image = File.createTempFile("camera_img", ".jpg", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(this, "${packageName}.fileprovider", image)
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
        Timber.wtf("wtf smallTalk : " + Gson().toJson(smallTalk))
        binding.postBlocked.isVisible = false
        smallTalkId = smallTalk?.id.toString()
        this.smallTalk = smallTalk
        setCameraIcon()

    }
    // --- SmallTalk messages mapping ---
    private fun handleSmallTalkMessages(messages: List<Post>?) {
        handleGetPostComments(messages?.toMutableList())
    }

    private fun showThumbnail(uri: Uri) {
        binding.ivThumbnail.setImageURI(uri)
        if (binding.ivThumbnail.visibility != View.VISIBLE) {
            binding.ivThumbnail.alpha = 0f
            binding.ivThumbnail.visibility = View.VISIBLE
            binding.ivThumbnail.animate()
                .alpha(1f)
                .setDuration(300)
                .start()
        }
        binding.comment.background = ResourcesCompat.getDrawable(
            resources,
            R.drawable.new_circle_orange_button_fill,
            null
        )
        binding.comment.isEnabled = true
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

    private fun sendImageMessage(uri: Uri) {
        val file = Utils.getFileFromUri(this, uri) ?: return
        val content = binding.commentMessage.text?.toString()?.takeIf { it.isNotBlank() }

        when {
            isSmallTalkMode -> {
                smallTalkViewModel.addMessageWithImage(smallTalkId, content, file)
            }
            detailConversation?.type == "outing" -> {
                val eventId = detailConversation?.id ?: return
                eventPresenter.addPost(content, file, eventId)
            }
            else -> {
                detailConversation?.id?.toInt()?.let { discussionsPresenter.addCommentWithImage(it, content, file) }
            }
        }

        binding.commentMessage.text.clear()
        binding.ivThumbnail.visibility = View.GONE
    }


    // --- Discussion detail (inchangé) ---
    private fun handleDetailConversation(conversation: Conversation?) {
        conversation ?: return
        this.detailConversation = conversation
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
            this.event = event
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
        val selectedUri = photoUri
        if (selectedUri != null && binding.ivThumbnail.isVisible) {
            sendImageMessage(selectedUri)
            photoUri = null
            return
        }

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
        val wasAtBottom = isAtBottom()
        commentsList.clear()
        newComments?.let { commentsList.addAll(it) }
        binding.comments.adapter?.notifyDataSetChanged()

        if (wasAtBottom) scrollAfterLayout() // 👈 garde uniquement cette ligne pour scroller
        binding.progressBar.visibility = View.GONE
        newComments?.isEmpty()?.let { updateView(it) }
    }
    override fun handleReportPost(id: Int, commentLang: String) {
        binding.header.iconSettings.setOnClickListener {
            SettingsDiscussionModalFragment.isSmallTalk = isSmallTalkMode
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
