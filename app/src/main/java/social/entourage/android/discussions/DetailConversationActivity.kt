package social.entourage.android.discussions

import android.animation.ObjectAnimator
import android.content.Context
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
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
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
    private val refreshIntervalMs = 30000L // 5 secondes
    private lateinit var cameraLauncher: ActivityResultLauncher<Uri>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
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
    private var isLoadingOlder = false
    private var storedFirstPos = 0
    private var storedOffset = 0

    private var event: Events? = null
    private fun Post.diffKey(): String =
        if (isDatePostOnly) {
            "SEP_$datePostText"                // séparateur de jour
        } else {
            (id?.toString() ?: idInternal.toString())
        }

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

        // Toujours charger le détail de la conversation (titre, membres, événement...)
        discussionsPresenter.detailConversation.observe(this) { handleDetailConversation(it) }
        eventPresenter.getEvent.observe(this) { handleGetEvent(it) }
        eventPresenter.getMembersSearch.observe(this) { handleMembersSearch(it) }

        binding.comments.layoutManager = LinearLayoutManager(this)
        setupScrollPagination()

        if (isSmallTalkMode) {
            smallTalkViewModel.smallTalkDetail.observe(this) { handleSmallTalkDetail(it) }
            smallTalkViewModel.messages.observe(this) { handleSmallTalkMessages(it) }
            smallTalkViewModel.participants.observe(this) { handleParticipants(it) }
            smallTalkViewModel.createdMessage.observe(this) {
                scrollAfterLayout()
            }
            smallTalkViewModel.getSmallTalk(smallTalkId)
            smallTalkViewModel.loadInitialMessages(smallTalkId)
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
            discussionsPresenter.loadInitialComments(id)
            discussionsPresenter.getAllComments.observe(this) { handleGetPostComments(it) }
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

        binding.btnQuitPhoto.setOnClickListener {
            photoUri = null
            binding.layoutPhoto.visibility = View.GONE
        }
    }


    private fun loadMoreDiscussionComments() {
        discussionsPresenter.loadMoreComments(id)
    }

    private fun handleMembersSearch(members: List<EntourageUser>?) {
        val currentUserId = EntourageApplication.get().me()?.id
        val filteredMembers = members?.filter { it?.id != currentUserId?.toLong() }?.map { it.toGroupMember() } ?: emptyList()
        showMentionSuggestions(filteredMembers)
    }

    private fun setupScrollPagination() {
        binding.comments.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                // scroll vers le haut et on ne peut plus scroller au-dessus ⇒ top atteint
                if (dy < 0 && !rv.canScrollVertically(-1) && !isLoadingOlder) {
                    isLoadingOlder = true
                    val lm = rv.layoutManager as LinearLayoutManager
                    storedFirstPos = lm.findFirstVisibleItemPosition()
                    storedOffset = lm.findViewByPosition(storedFirstPos)?.top ?: 0

                    if (isSmallTalkMode) {
                        smallTalkViewModel.loadMoreMessagesIfPossible(smallTalkId)
                    } else {
                        discussionsPresenter.loadMoreComments(id)
                    }
                }
            }
        })
    }
    private fun formatAndAddOlder(
        newItems: List<Post>,
        prevFirstPos: Int,
        prevOffset: Int
    ) {
        // 1) si pas de nouveaux messages => pas de chargement
        if (newItems.isEmpty()) {
            isLoadingOlder = false
            return
        }

        // 2) on regroupe + trie + extrait les séparateurs de date
        //    (assure un ordre chronologique)
        val incoming = sortAndExtractDays(newItems.toMutableList(), this) ?: run {
            isLoadingOlder = false
            return
        }

        // 3) on cherche le premier séparateur de date déjà présent
        //    (indépendamment de la présence ou non d’un detailPost en index 0)
        val firstExistingDate = commentsList.firstOrNull { it.isDatePostOnly }
        //    et on compare avec le premier de incoming
        if (incoming.firstOrNull()?.isDatePostOnly == true
            && firstExistingDate != null
            && incoming.first().datePostText == firstExistingDate.datePostText
        ) {
            // même jour => on vire le séparateur “en double”
            incoming.removeAt(0)
        }

        // 4) insertion en tête
        commentsList.addAll(0, incoming)
        binding.comments.adapter?.notifyItemRangeInserted(0, incoming.size)

        // 5) restauration de la position de scroll pour éviter le “jump”
        val lm = binding.comments.layoutManager as LinearLayoutManager
        lm.scrollToPositionWithOffset(prevFirstPos + incoming.size, prevOffset)

        // 6) fin du chargement “older”
        isLoadingOlder = false
    }

    /**
     * Trie par createdTime, groupe par date formatée, puis injecte
     * un Post séparateur avant chaque groupe.
     */
    fun sortAndExtractDays(allEvents: MutableList<Post>?, context: Context): MutableList<Post>? {
        if (allEvents == null) return null

        // 1) tri chronologique
        val sorted = allEvents.sortedBy { it.createdTime }

        // 2) groupement par date “jj MMMM yyyy”
        val grouped = sorted.groupBy { it.getFormatedStr() }

        // 3) pour chaque date, on crée un Post “séparateur” + on ajoute les messages
        val out = mutableListOf<Post>()
        grouped.forEach { (dateStr, posts) ->
            // séparateur de jour
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
            if (isDeleted) {
                eventPresenter.getEvent(this.event?.id.toString())
            }
        }

        discussionsPresenter.isMessageDeleted.observe(this) { isDeleted ->
            if (isDeleted) {
                discussionsPresenter.getPostComments(id)
            }
        }
    }



    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view_detail)
        startRefreshingMessages()

    }

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
                discussionsPresenter.getPostComments(id)
                detailConversation?.id
                    ?.toString()
                    ?.let { eventPresenter.getEvent(it) }
            }
            else -> {
                discussionsPresenter.getPostComments(id)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission accordée
            Toast.makeText(this, "Permission caméra accordée", Toast.LENGTH_SHORT).show()
        } else {
            // Permission refusée
            Toast.makeText(this, "La caméra nécessite une permission", Toast.LENGTH_LONG).show()
        }
    }

    private fun isAtBottom(): Boolean {
        val layoutManager = binding.comments.layoutManager as? LinearLayoutManager ?: return true
        val lastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition()
        val totalItemCount = layoutManager.itemCount
        return lastVisibleItem >= totalItemCount - 2 // on tolère 1-2 items de marge
    }

    fun setOptions() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1001)
        }
        var isOptionsVisible = false

        binding.optionButton.setOnClickListener {
            isOptionsVisible = !isOptionsVisible

            if (isOptionsVisible) {
                // Animation du layout vers le haut
                val slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in_up)
                binding.layoutOption.startAnimation(slideIn)
                binding.layoutOption.visibility = View.VISIBLE

                // Rotation vers le haut (180°)
                binding.optionButton.animate().rotation(45f).setDuration(300).start()

            } else {
                // Animation du layout vers le bas
                val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
                binding.layoutOption.startAnimation(slideOut)

                // Cache le layout après l’animation
                Handler(Looper.getMainLooper()).postDelayed({
                    binding.layoutOption.visibility = View.GONE
                }, 250)

                // Retour rotation à 0°
                binding.optionButton.animate().rotation(0f).setDuration(300).start()
            }
        }

        // Initialisation des icônes et textes d’option
        binding.optionCamera.ivOption.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_conversation_option_photo))
        binding.optionGalery.ivOption.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_conversation_option_galerie))
        binding.optionCamera.tvOption.setText(R.string.discussion_option_camera)
        binding.optionGalery.tvOption.setText(R.string.discussion_option_gallery)

        // Actions
        binding.optionCamera.layoutParent.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                photoUri = createImageUri()
                cameraLauncher.launch(photoUri)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), 1001)
            }
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
                    smallTalkViewModel.listChatMessages(smallTalkId, page = 1)
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

    private fun handleSmallTalkDetail(smallTalk: SmallTalk?) {
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
        //Hide options
        // Animation du layout vers le bas
        binding.optionButton.visibility = View.GONE
        val slideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out_down)
        binding.layoutOption.startAnimation(slideOut)

        // Cache le layout après l’animation
        Handler(Looper.getMainLooper()).postDelayed({
            binding.layoutOption.visibility = View.GONE
        }, 250)

        // Retour rotation à 0°
        binding.optionButton.animate().rotation(0f).setDuration(300).start()

        binding.layoutPhoto.visibility = View.VISIBLE
        binding.ivPhotoPreview.setImageURI(uri)

        if (binding.ivPhotoPreview.visibility != View.VISIBLE) {
            binding.ivPhotoPreview.alpha = 0f
            binding.ivPhotoPreview.visibility = View.VISIBLE
            binding.ivPhotoPreview.animate()
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
        binding.layoutPhoto.visibility = View.GONE
        binding.optionButton.visibility = View.VISIBLE
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
        if (conversation.type == "outing" && !isSmallTalkMode) {
            binding.optionButton.visibility = View.VISIBLE
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
        if (selectedUri != null && binding.layoutPhoto.isVisible) {
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
        allComments ?: return
        for(comment in allComments){
            Timber.wtf("wtf comment : " + comment?.messageType)
        }
        if (isLoadingOlder) {
            // Extrait les vrais « plus anciens » et insère en tête
            val incoming = sortAndExtractDays(allComments, this) ?: return
            val existingKeys = HashSet<String>(commentsList.size).apply {
                commentsList.forEach { add(it.diffKey()) }
            }
            val toInsert = incoming.filter { existingKeys.add(it.diffKey()) }
            formatAndAddOlder(toInsert, storedFirstPos, storedOffset)
            return
        }

        // Cas normal : ajout en bas (refresh ou premier chargement)
        val incoming = sortAndExtractDays(allComments, this) ?: return
        val wasAtBottom = isAtBottom()
        val existingKeys = HashSet<String>(commentsList.size).apply {
            commentsList.forEach { add(it.diffKey()) }
        }

        // --- 🔁 Mise à jour des messages déjà présents (statut, contenu) ---
        val incomingMap = allComments.associateBy { it.diffKey() }
        commentsList.forEachIndexed { index, existing ->
            val updated = incomingMap[existing.diffKey()]
            if (updated != null &&
                (existing.status != updated.status || existing.content != updated.content || existing.contentHtml != updated.contentHtml)
            ) {
                commentsList[index] = updated
                binding.comments.adapter?.notifyItemChanged(index + if (currentParentPost != null) 1 else 0)
            }
        }

        // --- ➕ Nouveaux messages à ajouter ---
        val toAdd = incoming.filter { existingKeys.add(it.diffKey()) }.toMutableList()

        // --- 🧹 Nettoyage : suppression de la dernière cellule "date" si elle est seule ---
        if (toAdd.isNotEmpty() && toAdd.last().isDatePostOnly) {
            val lastDate = toAdd.last().datePostText
            val lastDateIndex = toAdd.indexOfLast { it.isDatePostOnly && it.datePostText == lastDate }
            val hasMessagesAfter = toAdd.anyIndexed { i, post ->
                i > lastDateIndex && !post.isDatePostOnly
            }
            if (!hasMessagesAfter) {
                toAdd.removeAt(toAdd.lastIndex)
            }
        }

        if (toAdd.isEmpty()) {
            binding.progressBar.visibility = View.GONE
            return
        }

        val insertPos = commentsList.size
        commentsList.addAll(toAdd)
        binding.comments.adapter?.notifyItemRangeInserted(insertPos, toAdd.size)

        if (wasAtBottom) scrollAfterLayout()
        binding.progressBar.visibility = View.GONE
        updateView(commentsList.isEmpty())
    }

    private inline fun <T> List<T>.anyIndexed(predicate: (Int, T) -> Boolean): Boolean {
        forEachIndexed { index, item ->
            if (predicate(index, item)) return true
        }
        return false
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

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s == null) return
                val cursorPos = binding.commentMessage.selectionStart
                val substring = s.subSequence(0, cursorPos)
                val lastAt = substring.lastIndexOf('@')

                if (lastAt >= 0) {
                    val query = substring.substring(lastAt + 1, cursorPos)
                    lastMentionStartIndex = lastAt
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
