package social.entourage.android.discussions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import social.entourage.android.EntourageApplication
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.ConversationMembership
import social.entourage.android.api.model.HomeModerator
import social.entourage.android.api.model.LastMessage
import social.entourage.android.api.model.SmallTalk
import social.entourage.android.events.create.CommunicationHandler
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.HomePresenter
import social.entourage.android.home.UnreadMessages
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.small_talks.SmallTalkIntroActivity
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.utils.VibrationUtil
import timber.log.Timber

enum class FilterMode {
    PRIVATE, OUTINGS, SMALLTALKS
}

/**
 * EN-9489 : onglet Discussions, entièrement en Compose (cf. DiscussionsScreen) d'après la
 * maquette maquette-discussions-versions_1.html — header épuré, card "Bonnes ondes", contact
 * dédié épinglé, filtre multi-sélection derrière l'icône curseurs.
 */
class DiscussionsMainFragment : Fragment() {

    private val eventsPresenter: social.entourage.android.events.EventsPresenter by lazy { social.entourage.android.events.EventsPresenter() }
    private val groupPresenter: social.entourage.android.groups.GroupPresenter by lazy { social.entourage.android.groups.GroupPresenter() }
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val homePresenter: HomePresenter by lazy { HomePresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by lazy {
        ViewModelProvider(requireActivity())[SmallTalkViewModel::class.java]
    }

    /** Liste brute complète (toutes memberships confondues) ; le filtre EN-9489 est appliqué
     * côté Compose au moment du rendu, pas ici. */
    private val messagesList = mutableStateListOf<Conversation>()
    private val selectedFilters = mutableStateOf<Set<FilterMode>>(emptySet())
    private val dedicatedContact = mutableStateOf<HomeModerator?>(null)
    private val notificationBannerVisible = mutableStateOf(false)
    private val isRefreshing = mutableStateOf(false)

    private var isFromDetail = false
    private var page = 0
    private var isFromRefresh = false
    private val readConversationIds = mutableSetOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val conversations = messagesList
                val filters by selectedFilters
                val moderator = dedicatedContact.value
                // EN-9489 (suite) : la conversation 1-1 avec le contact dédié est retrouvée par
                // correspondance de nom dans la liste des memberships — l'API ne renvoie pas
                // l'id utilisateur du contact dédié sur cet endpoint (cf. commentaires du ticket).
                val dedicatedContactConversation = remember(conversations.toList(), moderator) {
                    moderator?.let { mod -> conversations.firstOrNull { it.type == "private" && it.title == mod.displayName } }
                }
                val displayed = remember(conversations.toList(), filters, dedicatedContactConversation) {
                    conversations.filter { matchesSelectedFilters(it, filters) && it !== dedicatedContactConversation }
                }
                MaterialTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        DiscussionsScreen(
                            conversations = displayed,
                            dedicatedContact = moderator,
                            dedicatedContactConversation = dedicatedContactConversation,
                            isFilterActive = filters.isNotEmpty(),
                            notificationBannerVisible = notificationBannerVisible.value,
                            isRefreshing = isRefreshing.value,
                            onEnableNotifications = ::openNotificationSettings,
                            onSmallTalkCtaClick = ::openSmallTalkIntro,
                            onFilterClick = ::openFilterBottomSheet,
                            onDedicatedContactClick = ::openDedicatedContactConversation,
                            onConversationClick = ::showDetail,
                            onRefresh = ::reloadFromStart,
                            onLoadMore = ::loadMoreIfNeeded,
                        )
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getBoolean("isSmallTalkFilter")?.let { isSmallTalkFilter ->
            if (isSmallTalkFilter) {
                selectedFilters.value = setOf(FilterMode.SMALLTALKS)
            }
        }

        discussionsPresenter.getAllMessages.observe(viewLifecycleOwner, ::handleResponseGetDiscussions)
        discussionsPresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        smallTalkViewModel.smallTalks.observe(viewLifecycleOwner, ::handleResponseGetSmallTalks)
        discussionsPresenter.memberships.observe(viewLifecycleOwner, ::handleResponseGetMemberships)
        discussionsPresenter.hasUserLeftConversation.observe(viewLifecycleOwner, ::handleConversationLeft)
        eventsPresenter.hasUserLeftEvent.observe(viewLifecycleOwner, ::handleConversationLeft)
        groupPresenter.hasUserLeftGroup.observe(viewLifecycleOwner, ::handleConversationLeft)
        smallTalkViewModel.shouldLeave.observe(viewLifecycleOwner, ::handleConversationLeft)
        discussionsPresenter.newConversation.observe(viewLifecycleOwner, ::handleNewConversation)
        homePresenter.summary.observe(viewLifecycleOwner) { summary ->
            dedicatedContact.value = summary?.moderator
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view)
    }

    override fun onResume() {
        super.onResume()
        // IMPORTANT : on ne recharge pas si on revient du détail ; on consomme le flag ici
        if (RefreshController.shouldRefreshFragment) {
            RefreshController.shouldRefreshFragment = false
            isFromRefresh = true
            reloadFromStart()
        } else if (isFromDetail) {
            isFromDetail = false
        } else {
            reloadFromStart()
        }
        discussionsPresenter.getUnreadCount()
        homePresenter.getSummary()
        checkNotificationsState()
    }

    override fun onStop() {
        super.onStop()
        page = 0
    }

    override fun onPause() {
        super.onPause()
        // Ne PAS remettre isFromDetail à false ici ; on veut le consommer dans onResume()
    }

    // -------------------- FILTRE (EN-9489) --------------------

    private fun openFilterBottomSheet() {
        DiscussionsFilterBottomSheet.newInstance(selectedFilters.value).apply {
            onApply = { newSelection -> selectedFilters.value = newSelection }
        }.show(childFragmentManager, DiscussionsFilterBottomSheet.TAG)
    }

    private fun matchesSelectedFilters(conversation: Conversation, filters: Set<FilterMode>): Boolean {
        if (filters.isEmpty()) return true
        return filters.any { filter ->
            when (filter) {
                FilterMode.PRIVATE    -> conversation.type == "private"
                FilterMode.OUTINGS    -> conversation.type == "outing"
                FilterMode.SMALLTALKS -> conversation.type == "small_talk"
            }
        }
    }

    // -------------------- BONNES ONDES / CONTACT DÉDIÉ --------------------

    private fun openSmallTalkIntro() {
        startActivity(Intent(context, SmallTalkIntroActivity::class.java))
    }

    /** EN-9489 : ouvre (ou crée) la conversation 1-1 avec le contact dédié — pattern documenté
     * dans CLAUDE.md (discussionsPresenter.createOrGetConversation + observer newConversation). */
    private fun openDedicatedContactConversation() {
        dedicatedContact.value?.id?.let { discussionsPresenter.createOrGetConversation(it.toString()) }
    }

    private fun handleNewConversation(conversation: Conversation?) {
        conversation?.let { showDetail(it) }
    }

    // -------------------- LOGIQUE MESSAGES --------------------

    private fun reloadFromStart() {
        resetMessagesList()
        page = 0
        discussionsPresenter.isLastPage = false
        discussionsPresenter.getAllMessages.value?.clear()

        discussionsPresenter.currentPageMemberships = 1
        discussionsPresenter.isLastPageMemberships = false

        isRefreshing.value = true
        discussionsPresenter.fetchMemberships(null, reset = true)
    }

    private fun loadMoreIfNeeded() {
        if (!discussionsPresenter.isLoadingMemberships && !discussionsPresenter.isLastPageMemberships) {
            discussionsPresenter.fetchMemberships(null)
        }
    }

    private fun resetMessagesList() {
        messagesList.clear()
        page = 0
        discussionsPresenter.isLastPage = false
        discussionsPresenter.getAllMessages.value?.clear()
    }

    private fun handleResponseGetDiscussions(allGroups: MutableList<Conversation>?) {
        if (page == 1) messagesList.clear()
        allGroups?.let { messagesList.addAll(it) }
        applyReadState()
        isRefreshing.value = false
    }

    private fun handleResponseGetSmallTalks(allSmallTalks: List<SmallTalk>?) {
        messagesList.clear()
        allSmallTalks?.let { list -> messagesList.addAll(list.map { smallTalkToConversation(it) }) }
        isRefreshing.value = false
    }

    private fun smallTalkToConversation(smallTalk: SmallTalk): Conversation {
        return Conversation(
            id = smallTalk.id,
            uuid_v2 = smallTalk.uuid,
            type = "small_talk",
            title = smallTalk.name.orEmpty(),
            imageUrl = smallTalk.imageUrl,
            lastMessage = smallTalk.lastMessage,
            numberUnreadMessages = smallTalk.numberOfUnreadMessages ?: 0,
            members = smallTalk.members,
            memberCount = smallTalk.membersCount ?: 0
        )
    }

    private fun handleResponseGetMemberships(memberships: List<ConversationMembership>?) {
        isRefreshing.value = false
        messagesList.clear()
        memberships?.let { list -> messagesList.addAll(list.map { membershipToConversation(it) }) }
        applyReadState()
    }

    private fun applyReadState() {
        messagesList.forEach { conv ->
            if (readConversationIds.contains(conv.id ?: 0)) {
                conv.numberUnreadMessages = 0
            }
        }
    }

    private fun showDetail(conversation: Conversation) {
        conversation.numberUnreadMessages = 0
        conversation.id?.let { readConversationIds.add(it) }

        VibrationUtil.vibrate(requireContext())
        DetailConversationActivity.isSmallTalkMode = (conversation.type == "small_talk")
        if (DetailConversationActivity.isSmallTalkMode) {
            DetailConversationActivity.smallTalkId = conversation.id.toString()
        }
        startActivity(
            Intent(context, DetailConversationActivity::class.java).putExtras(
                bundleOf(
                    Const.ID to conversation.id,
                    Const.POST_AUTHOR_ID to conversation.user?.id,
                    Const.SHOULD_OPEN_KEYBOARD to false,
                    Const.NAME to conversation.title,
                    Const.IS_CONVERSATION_1TO1 to conversation.isOneToOne(),
                    Const.IS_MEMBER to true,
                    Const.IS_CONVERSATION to true,
                    Const.HAS_TO_SHOW_MESSAGE to conversation.hasToShowFirstMessage()
                )
            )
        )
        // ✅ on signale qu'on part au détail ; on NE remettra PAS ce flag à false dans onPause()
        isFromDetail = true
    }

    // -------------------- OUTILS --------------------

    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        EntourageApplication.get().mainActivity?.let {
            val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
            viewModel.badgeCount.postValue(unreadMessages)
        }
        CommunicationHandler.resetValues()
    }

    private fun checkNotificationsState() {
        notificationBannerVisible.value =
            !NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()
    }

    private fun openNotificationSettings() {
        notificationBannerVisible.value = false
        NotificationDemandActivity.comeFromSettings = true
        startActivity(Intent(requireContext(), NotificationDemandActivity::class.java))
    }

    private fun membershipToConversation(m: ConversationMembership): Conversation {
        var date: java.util.Date? = null
        m.lastChatMessageDate?.let {
            try {
                val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US)
                date = inputFormat.parse(it)
            } catch (e: Exception) {
                try {
                    val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                    date = inputFormat.parse(it)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }

        val lastMessage = if (m.lastChatMessageText != null || m.lastChatMessageImageUrl != null) {
            LastMessage(m.lastChatMessageText, date, m.lastChatMessageImageUrl)
        } else {
            null
        }
        return Conversation(
            id = m.joinableId,
            type = when (m.joinableType?.lowercase()) {
                "outing"       -> "outing"
                "conversation" -> "private"
                "smalltalk"    -> "small_talk"
                "neighborhood" -> "group"
                else           -> "group"
            },
            title = (m.name),
            imageUrl = m.imageUrl,
            subname = m.getParsedDate()?.let { Utils.formatEventDateWithTime(it, requireContext()) } ?: "",
            lastMessage = lastMessage,
            numberUnreadMessages = m.numberOfUnreadMessages ?: 0,
            memberCount = m.numberOfPeople ?: 0
        )
    }

    private fun handleConversationLeft(hasLeft: Boolean) {
        if (hasLeft) {
            reloadFromStart()
        }
    }
}
