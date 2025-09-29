package social.entourage.android.discussions

import android.content.Intent
import android.content.pm.PackageManager
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
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.text.HtmlCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsAnimationCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import social.entourage.android.BuildConfig
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.Events
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.Post
import social.entourage.android.api.model.SmallTalk
import social.entourage.android.api.model.User
import social.entourage.android.api.model.toGroupMember
import social.entourage.android.comment.CommentActivity
import social.entourage.android.comment.CommentsListAdapter
import social.entourage.android.comment.MentionAdapter
import social.entourage.android.discussions.members.MembersConversationFragment
import social.entourage.android.events.EventsPresenter
import social.entourage.android.events.details.feed.EventFeedActivity
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.profile.ProfileFullActivity
import social.entourage.android.small_talks.SmallTalkGuidelinesActivity
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.tools.view.WebViewFragment
import social.entourage.android.ui.ActionSheetFragment
import social.entourage.android.ui.SheetMode
import timber.log.Timber
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class DetailConversationActivity : CommentActivity() {

    companion object {
        var isSmallTalkMode: Boolean = false
        var smallTalkId: String = ""
        var shouldResetToHome: Boolean = false
    }

    // Presenters / VM
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    // Launchers
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>

    // State
    private var detailConversation: Conversation? = null
    private var smallTalk: SmallTalk? = null
    private var mentionSearchJob: Job? = null

    private var hasToShowFirstMessage = false
    var hasSeveralPeople = false
    private var conversationTitle: String? = null
    private var allMembers: List<GroupMember> = emptyList()
    private var lastMentionStartIndex = -1

    // Pagination vers le haut
    private var isLoadingOlder = false
    private var pendingAnchorKey: String? = null
    private var anchorOffsetTop: Int = 0

    private var event: Events? = null

    // Refresh (page 1 -> append en bas)
    private val refreshHandler = Handler(Looper.getMainLooper())
    private val refreshIntervalMs = 1_500L
    private var refreshRunnable: Runnable? = null

    // Clé stable pour les items
    private fun Post.diffKey(): String =
        if (isDatePostOnly) "SEP_$datePostText" else (id?.toString() ?: idInternal.toString())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isMember = isSmallTalkMode
        hasToShowFirstMessage = intent.getBooleanExtra(Const.HAS_TO_SHOW_MESSAGE, false)
        binding.emptyState.visibility = View.GONE

        setupHeader()
        setupMentionList()
        setupMentionTextWatcher()
        setupOptionMenu()
        setupOptionActions()
        observeDeletedMessage()

        // Observers
        discussionsPresenter.detailConversation.observe(this) { handleDetailConversation(it) }
        eventPresenter.getEvent.observe(this) { handleGetEvent(it) }
        eventPresenter.getMembersSearch.observe(this) { handleMembersSearch(it) }
        binding.comments.layoutManager = LinearLayoutManager(this)
        setupScrollPagination()

        if (isSmallTalkMode) {
            smallTalkViewModel.smallTalkDetail.observe(this) { handleSmallTalkDetail(it) }
            smallTalkViewModel.messages.observe(this) { handleSmallTalkMessages(it) }
            smallTalkViewModel.participants.observe(this) { handleParticipants(it) }
            smallTalkViewModel.createdMessage.observe(this) { scrollAfterLayout() }

            smallTalkViewModel.getSmallTalk(smallTalkId)
            smallTalkViewModel.loadInitialMessages(smallTalkId) // page 1 initiale
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
                startActivity(Intent(this, SmallTalkGuidelinesActivity::class.java))
            }
        } else {
            discussionsPresenter.getDetailConversation(id)
            discussionsPresenter.getAllComments.observe(this) { handleGetPostComments(it) }
            discussionsPresenter.commentPosted.observe(this) { scrollAfterLayout() }
            discussionsPresenter.loadInitialComments(id) // page 1 initiale
        }

        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) {
                val uri = createImageUri()
                photoUri = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(this, "Permission caméra requise", Toast.LENGTH_LONG).show()
            }
        }

        cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && photoUri != null) {
                binding.comment.isEnabled = true
                showThumbnail(photoUri!!)
            }
        }

        galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                photoUri = uri
                binding.comment.isEnabled = true
                showThumbnail(uri)
            }
        }

        binding.header.headerIconSettings.setOnClickListener {
            buildAndShowActionSheet()
        }


    }

    // ===== Refresh page 1 (append en bas) =====
    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view_detail)
        startRefreshing()
    }

    override fun onPause() {
        super.onPause()
        stopRefreshing()
    }

    private fun buildAndShowActionSheet() {
        val mode = resolveSheetMode()
        Timber.d("Mode résolu : $mode, event = $event, eventId = ${event?.id}, detailConversationId = ${detailConversation?.id}")

        // Si on est en mode EVENT, on vérifie que eventId est valide
        if (mode == SheetMode.EVENT) {
            val eventId = event?.id ?: detailConversation?.id ?: 0
            if (eventId <= 0) {
                Timber.e("Impossible d'ouvrir l'ActionSheet en mode EVENT : eventId invalide ($eventId)")
                Toast.makeText(this, "Impossible d'ouvrir les paramètres de l'événement", Toast.LENGTH_SHORT).show()
                return // On ne continue pas si eventId est invalide
            }

            // On passe TOUS les arguments nécessaires (comme dans EventFeedFragment)
            ActionSheetFragment.newEvent(
                eventId = eventId,
                conversationId = id,
                canManageParticipants = false, // À adapter selon ta logique métier
                eventTitle = event?.title ?: "",
                participantsCount = event?.membersCount ?: 0,
                eventAddress = event?.metadata?.displayAddress ?: ""
            ).show(supportFragmentManager, "ActionSheetFragment")

        } else {
            // Pour les autres modes (GROUP, DISCUSSION_ONE_TO_ONE, DISCUSSION_GROUP)
            val sheet = when (mode) {
                SheetMode.DISCUSSION_ONE_TO_ONE -> {
                    val otherUserId = detailConversation?.members
                        ?.firstOrNull { it?.id != EntourageApplication.get().me()?.id }
                        ?.id ?: 0
                    ActionSheetFragment.newDiscussion(
                        conversationId = id,
                        isOneToOne = true,
                        userId = otherUserId,
                        username = detailConversation?.title,
                        blocked = detailConversation?.hasBlocker() == true && detailConversation?.imBlocker() == true
                    )
                }
                SheetMode.DISCUSSION_GROUP -> {
                    ActionSheetFragment.newDiscussion(
                        conversationId = id,
                        isOneToOne = false,
                        userId = 0,
                        username = null,
                        blocked = false
                    )
                }
                SheetMode.GROUP -> {
                    ActionSheetFragment.newGroup(detailConversation?.id ?: id)
                }
                SheetMode.MESSAGE_ACTIONS -> {
                    ActionSheetFragment.newGroup(detailConversation?.id ?: id)
                }
                else -> {
                    Timber.e("Mode non géré : $mode, fallback vers GROUP")
                    ActionSheetFragment.newGroup(detailConversation?.id ?: id)
                }
            }
            sheet.show(supportFragmentManager, "ActionSheetFragment")
        }
    }

    private fun resolveSheetMode(): SheetMode {
        return when {
            isSmallTalkMode -> SheetMode.DISCUSSION_GROUP
            event != null -> SheetMode.EVENT
            detailConversation?.memberCount == 2 -> SheetMode.DISCUSSION_ONE_TO_ONE
            (detailConversation?.memberCount ?: 0) > 2 -> SheetMode.DISCUSSION_GROUP
            detailConversation?.type == "outing" -> SheetMode.EVENT
            else -> SheetMode.GROUP
        }
    }

    private fun isClassicDiscussion(): Boolean {
        return !isSmallTalkMode && detailConversation?.type != "outing"
    }

    private fun startRefreshing() {
        if (refreshRunnable != null) return
        refreshRunnable = object : Runnable {
            override fun run() {
                if (!isLoadingOlder) {
                    if (isSmallTalkMode) {
                        smallTalkViewModel.listChatMessages(smallTalkId, page = 1)
                    } else {
                        discussionsPresenter.getPostComments(id) // page 1
                    }
                    // Ajoutez un délai pour laisser le temps à la liste de se mettre à jour
                    Handler(Looper.getMainLooper()).postDelayed({
                        if (isAtBottom()) {
                            scrollAfterLayout()
                        }
                    }, 300) // Délai court pour laisser le temps à la liste de se recharger
                }
                refreshHandler.postDelayed(this, refreshIntervalMs)
            }
        }
        refreshHandler.postDelayed(refreshRunnable!!, refreshIntervalMs)
    }


    private fun stopRefreshing() {
        refreshRunnable?.let { refreshHandler.removeCallbacks(it) }
        refreshRunnable = null
    }

    // ===== API héritée =====
    override fun reloadView() {
        shouldOpenKeyboard = false
        when {
            isSmallTalkMode -> smallTalkViewModel.listChatMessages(smallTalkId, page = 1)
            detailConversation?.type == "outing" -> {
                discussionsPresenter.getPostComments(id) // page 1
                detailConversation?.id?.toString()?.let { eventPresenter.getEvent(it) }
            }
            else -> discussionsPresenter.getPostComments(id) // page 1
        }
    }

    override fun translateView(id: Int) {
        (binding.comments.adapter as? CommentsListAdapter)?.translateItem(id)
    }

    // ===== Header / UI divers =====
    private fun setCameraIcon() {
        binding.header.headerIconCamera.isVisible = !smallTalk?.meetingUrl.isNullOrBlank()
        binding.header.headerIconCamera.setImageResource(R.drawable.ic_camera)
        val transparent = ContextCompat.getColor(this, R.color.transparent)
        binding.header.headerCardIconCamera.setBackgroundColor(transparent)
        binding.header.headerIconCamera.setBackgroundColor(transparent)
        binding.header.headerIconCamera.setOnClickListener {
            Timber.d("SmallTalk meeting url: ${smallTalk?.meetingUrl}")
            AnalyticsEvents.logEvent(AnalyticsEvents.CLIC__SMALLTALK__VISIO_ICON)
            smallTalk?.meetingUrl?.let { url -> WebViewFragment.launchURL(this, url) }
        }
    }

    private fun isAtBottom(): Boolean {
        val lm = binding.comments.layoutManager as? LinearLayoutManager ?: return true
        val last = lm.findLastCompletelyVisibleItemPosition()
        val total = lm.itemCount
        return last >= total - 2
    }

    private fun setupOptionMenu() {
        var isOptionsVisible = false

        binding.optionButton.setOnClickListener {
            isOptionsVisible = !isOptionsVisible
            if (isOptionsVisible) {
                val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
                binding.layoutOption.startAnimation(slideIn)
                binding.layoutOption.visibility = View.VISIBLE
                binding.optionButton.animate().rotation(45f).setDuration(300).start()
            } else {
                val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
                binding.layoutOption.startAnimation(slideOut)
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.layoutOption.visibility = View.GONE
                }, 250)
                binding.optionButton.animate().rotation(0f).setDuration(300).start()
            }
        }

        binding.optionCamera.ivOption.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_conversation_option_photo)
        )
        binding.optionGalery.ivOption.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.ic_conversation_option_galerie)
        )
        binding.optionCamera.tvOption.setText(R.string.discussion_option_camera)
        binding.optionGalery.tvOption.setText(R.string.discussion_option_gallery)
    }

    private fun setupOptionActions() {
        binding.optionCamera.layoutParent.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED -> {
                    val uri = createImageUri()
                    photoUri = uri
                    cameraLauncher.launch(uri)
                }
                else -> cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
        binding.optionGalery.layoutParent.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        binding.btnQuitPhoto.setOnClickListener {
            photoUri = null
            binding.layoutPhoto.visibility = View.GONE
            binding.optionButton.visibility = View.VISIBLE
        }
    }

    private fun createImageUri(): Uri {
        val image = File.createTempFile("camera_img", ".jpg", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return FileProvider.getUriForFile(this, "$packageName.fileprovider", image)
    }

    private fun handleMembersSearch(members: List<EntourageUser>?) {
        val currentUserId = EntourageApplication.get().me()?.id
        val filtered = members
            ?.filter { it?.id != currentUserId?.toLong() }
            ?.map { it.toGroupMember() }
            ?: emptyList()
        showMentionSuggestions(filtered)
    }

    // ===== Pagination vers le haut =====
    private fun setupScrollPagination() {
        binding.comments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy < 0 && !rv.canScrollVertically(-1) && !isLoadingOlder) {
                    val lm = rv.layoutManager as LinearLayoutManager
                    val firstPos = lm.findFirstVisibleItemPosition()
                    if (firstPos == RecyclerView.NO_POSITION || firstPos >= commentsList.size) return

                    // Capture ancre (clé + offset) du 1er visible
                    pendingAnchorKey = commentsList[firstPos].diffKey()
                    anchorOffsetTop = lm.findViewByPosition(firstPos)?.top ?: 0
                    isLoadingOlder = true

                    // Charger page suivante (plus ancienne)
                    if (isSmallTalkMode) {
                        smallTalkViewModel.loadMoreMessagesIfPossible(smallTalkId)
                    } else {
                        discussionsPresenter.loadMoreComments(id)
                    }
                }
            }
        })
    }

    private fun prependOlderWithAnchor(newItems: List<Post>) {
        val lm = binding.comments.layoutManager as LinearLayoutManager

        // Dédoublonner séparateur jour en tête
        val firstExistingDate = commentsList.firstOrNull { it.isDatePostOnly }?.datePostText
        val toInsert = newItems.toMutableList()
        if (toInsert.firstOrNull()?.isDatePostOnly == true &&
            firstExistingDate != null &&
            toInsert.first().datePostText == firstExistingDate
        ) {
            toInsert.removeAt(0)
        }
        if (toInsert.isEmpty()) {
            isLoadingOlder = false
            return
        }

        // PREPEND en haut
        commentsList.addAll(0, toInsert)
        binding.comments.adapter?.notifyItemRangeInserted(0, toInsert.size)

        // Restaure l’ancre
        pendingAnchorKey?.let { key ->
            val idx = commentsList.indexOfFirst { it.diffKey() == key }
            if (idx >= 0) lm.scrollToPositionWithOffset(idx, anchorOffsetTop)
        }
        pendingAnchorKey = null
        isLoadingOlder = false
    }

    // ===== SmallTalk =====
    private fun handleSmallTalkDetail(smallTalk: SmallTalk?) {
        Timber.d("SmallTalk detail: ${Gson().toJson(smallTalk)}")
        binding.postBlocked.isVisible = false
        smallTalkId = smallTalk?.id.toString()
        this.smallTalk = smallTalk
        setCameraIcon()
    }

    private fun handleSmallTalkMessages(messages: List<Post>?) {
        handleGetPostComments(messages?.toMutableList())
    }

    // ===== Image UI =====
    private fun showThumbnail(uri: Uri) {
        binding.optionButton.visibility = View.GONE
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
        binding.layoutOption.startAnimation(slideOut)
        Handler(Looper.getMainLooper()).postDelayed({
            binding.layoutOption.visibility = View.GONE
        }, 250)
        binding.optionButton.animate().rotation(0f).setDuration(300).start()

        binding.layoutPhoto.visibility = View.VISIBLE
        binding.ivPhotoPreview.setImageURI(uri)
        if (!binding.ivPhotoPreview.isVisible) {
            binding.ivPhotoPreview.alpha = 0f
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.ivPhotoPreview.animate().alpha(1f).setDuration(300).start()
        }

        binding.comment.background = ContextCompat.getDrawable(this, R.drawable.new_circle_orange_button_fill)
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

    // ===== Envoi image =====
    private fun sendImageMessage(uri: Uri) {
        val file: File = Utils.getFileFromUri(this, uri) ?: run {
            Toast.makeText(this, "Impossible de lire l'image", Toast.LENGTH_SHORT).show()
            return
        }
        val spanned = binding.commentMessage.editableText
        val caption = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spanned, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.toHtml(spanned)
        }.trim().ifEmpty { null }
        if (isSmallTalkMode) {
            smallTalkViewModel.addMessageWithImage(smallTalkId, caption, file)
        } else if (detailConversation?.type == "outing") {
            val eventId: Int = detailConversation?.id ?: return
            eventPresenter.addPost(caption, file, eventId)
        } else {
            val conversationId: Int = detailConversation?.id ?: return
            discussionsPresenter.addCommentWithImage(conversationId, caption, file)
        }
        binding.commentMessage.text.clear()
        binding.layoutPhoto.visibility = View.GONE
        binding.optionButton.visibility = View.VISIBLE
        // Force le scroll après l'envoi
        scrollAfterLayout()
    }



    // ===== Détails conversation =====
    private fun handleDetailConversation(conversation: Conversation?) {
        conversation ?: return
        this.detailConversation = conversation
        isMember = conversation.member == true
        updateView(false)

        if (conversation.memberCount > 2) isOne2One = false

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

        if (conversation.type == "outing") {
            binding.optionButton.visibility = View.VISIBLE
        }else {
            binding.optionButton.visibility = View.GONE
        }
        if (conversation.type == "outing" || isSmallTalkMode) {
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
                if (role.contains("Équipe Entourage")) hasToShowFirstMessage = false
            }
        }

        checkAndShowPopWarning()

        conversationTitle = conversation.title
        binding.header.title = conversationTitle

        val memberCount = conversation.members?.size ?: 0
        if (memberCount > 2) {
            hasSeveralPeople = true
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

            binding.header.headerIvEvent.visibility = View.GONE
            val thumb = event.metadata?.portraitThumbnailUrl
            if (thumb.isNullOrBlank()) {
                Glide.with(this).load(R.drawable.placeholder_my_event).into(binding.header.headerIvEvent)
            } else {
                Glide.with(this)
                    .load(thumb)
                    .transform(RoundedCorners(10))
                    .error(R.drawable.placeholder_my_event)
                    .into(binding.header.headerIvEvent)
            }
            binding.header.headerSubtitle.visibility = View.VISIBLE
            binding.header.headerTitle.text = event.title
            binding.header.headerSubtitle.text = formatDate(event.metadata?.startsAt.toString())
            binding.emptyState.visibility = View.GONE
        }
    }

    // ===== Envoi texte =====
    override fun addComment() {
        val selectedUri = photoUri
        if (selectedUri != null && binding.layoutPhoto.isVisible) {
            sendImageMessage(selectedUri)
            photoUri = null
            return
        }
        val spanned = binding.commentMessage.editableText
        val html = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.toHtml(spanned, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
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
        // Force le scroll après l'envoi
        scrollAfterLayout()
    }


    // ===== Réception des messages =====
    override fun handleGetPostComments(allComments: MutableList<Post>?) {
        allComments ?: return
        val formatted = sortAndExtractDays(allComments, this) ?: return

        if (isLoadingOlder) {
            val existingKeys = HashSet<String>(commentsList.size).apply {
                commentsList.forEach { add(it.diffKey()) }
            }
            val toPrepend = formatted.filter { existingKeys.add(it.diffKey()) }
            if (toPrepend.isEmpty()) {
                isLoadingOlder = false
                return
            }
            prependOlderWithAnchor(toPrepend)
            return
        }

        val indexByKey = HashMap<String, Int>(commentsList.size)
        commentsList.forEachIndexed { idx, p -> indexByKey[p.diffKey()] = idx }
        formatted.forEach { serverItem ->
            if (!serverItem.isDatePostOnly) {
                val idx = indexByKey[serverItem.diffKey()]
                if (idx != null && idx in commentsList.indices) {
                    val local = commentsList[idx]
                    if (hasMeaningfulDiff(local, serverItem)) {
                        commentsList[idx] = serverItem
                        binding.comments.adapter?.notifyItemChanged(idx)
                    }
                }
            }
        }

        val existingKeys = HashSet<String>(commentsList.size).apply {
            commentsList.forEach { add(it.diffKey()) }
        }
        val toAppend = formatted.filter { existingKeys.add(it.diffKey()) }.toMutableList()
        if (toAppend.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            return
        }

        val lastExistingDate = commentsList.lastOrNull { it.isDatePostOnly }?.datePostText
        if (toAppend.firstOrNull()?.isDatePostOnly == true &&
            lastExistingDate != null &&
            toAppend.first().datePostText == lastExistingDate
        ) {
            toAppend.removeAt(0)
            if (toAppend.isEmpty()) {
                binding.progressBar.visibility = View.GONE
                return
            }
        }

        val wasAtBottom = isAtBottom()
        val insertPos = commentsList.size
        commentsList.addAll(toAppend)
        binding.comments.adapter?.notifyItemRangeInserted(insertPos, toAppend.size)
        if (wasAtBottom) scrollAfterLayout()

        binding.progressBar.visibility = View.GONE
        updateView(commentsList.isEmpty())
    }

    private fun hasMeaningfulDiff(local: Post, remote: Post): Boolean {
        return local.status != remote.status ||
                (local.content ?: "") != (remote.content ?: "") ||
                (local.contentHtml ?: "") != (remote.contentHtml ?: "") ||
                (local.imageUrl ?: "") != (remote.imageUrl ?: "") ||
                (local.contentTranslations?.translation ?: "") != (remote.contentTranslations?.translation ?: "") ||
                (local.contentTranslationsHtml?.translation ?: "") != (remote.contentTranslationsHtml?.translation ?: "")
    }


    // ===== Utils =====
    fun sortAndExtractDays(allEvents: MutableList<Post>?, context: android.content.Context): MutableList<Post>? {
        if (allEvents == null) return null
        val sorted = allEvents.sortedBy { it.createdTime } // ordre croissant
        val grouped = sorted.groupBy { it.getFormatedStr() }
        val out = mutableListOf<Post>()
        grouped.forEach { (dateStr, posts) ->
            val sep = Post().apply {
                isDatePostOnly = true
                datePostText = dateStr.replaceFirstChar { it.uppercaseChar() }
            }
            out += sep
            out += posts
        }
        return out
    }

    private fun observeDeletedMessage() {
        eventPresenter.isEventDeleted.observe(this) { isDeleted ->
            if (isDeleted) eventPresenter.getEvent(this.event?.id.toString())
        }
        discussionsPresenter.isMessageDeleted.observe(this) { isDeleted ->
            if (isDeleted) discussionsPresenter.getPostComments(id) // page 1
        }
    }

    private fun setupHeader() {
        binding.header.headerIconSettings.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.new_settings)
        )
        val transparent = ContextCompat.getColor(this, R.color.transparent)
        binding.header.headerCardIconSetting.setBackgroundColor(transparent)
        binding.header.headerIconSettings.setBackgroundColor(transparent)
    }

    // ===== Mentions =====
    private val mentionAdapter: MentionAdapter by lazy {
        MentionAdapter(emptyList()) { user -> insertMentionIntoEditText(user) }
    }

    private fun setupMentionList() {
        binding.mentionSuggestionsRecycler.layoutManager = LinearLayoutManager(this)
        binding.mentionSuggestionsRecycler.adapter = mentionAdapter
    }

    private fun setupMentionTextWatcher() {
        binding.commentMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s ?: return
                val cursorPos = binding.commentMessage.selectionStart
                val substring = text.subSequence(0, cursorPos)
                val lastAt = substring.lastIndexOf('@')

                if (lastAt >= 0) {
                    val query = substring.substring(lastAt + 1, cursorPos)
                    lastMentionStartIndex = lastAt
                    mentionSearchJob?.cancel()
                    mentionSearchJob = lifecycleScope.launch {
                        delay(200) // debounce
                        if (query.isEmpty()) {
                            showMentionSuggestions(allMembers)
                        } else {
                            if (detailConversation?.type == "outing") {
                                event?.id?.let { eventId ->
                                    eventPresenter.searchEventMembers(eventId, query)
                                }
                            } else {
                                filterAndShowMentions(query)
                            }
                        }
                    }
                } else {
                    hideMentionSuggestions()
                    lastMentionStartIndex = -1
                }
            }

            override fun afterTextChanged(s: Editable?) {}
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

    // ===== Dates / infos =====
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
        if (hasToShowFirstMessage && !isClassicDiscussion()) {
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
        MembersConversationFragment.isFromDiscussion = false
        smallTalkId = ""
        stopRefreshing()
        mentionSearchJob?.cancel()
    }
}
