package social.entourage.android.discussions

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.ConversationMembership
import social.entourage.android.api.model.LastMessage
import social.entourage.android.api.model.SmallTalk
import social.entourage.android.databinding.FragmentMessagesBinding
import social.entourage.android.events.create.CommunicationHandler
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.small_talks.SmallTalkIntroActivity
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.VibrationUtil
import social.entourage.android.tools.utils.Utils
import timber.log.Timber
import kotlin.math.abs

enum class FilterMode {
    PRIVATE, OUTINGS, SMALLTALKS
}

class DiscussionsMainFragment : Fragment() {

    private val eventsPresenter: social.entourage.android.events.EventsPresenter by lazy { social.entourage.android.events.EventsPresenter() }
    private val groupPresenter: social.entourage.android.groups.GroupPresenter by lazy { social.entourage.android.groups.GroupPresenter() }

    private var _binding: FragmentMessagesBinding? = null
    private val binding get() = _binding!!
    private var isFromDetail = false

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by lazy {
        ViewModelProvider(requireActivity())[SmallTalkViewModel::class.java]
    }

    private val messagesList = mutableListOf<Conversation>()
    /** Sous-ensemble de [messagesList] réellement montré à l'écran une fois le filtre
     * EN-9489 appliqué — c'est cette liste (pas [messagesList]) qui est liée à l'adapter. */
    private val displayedList = mutableListOf<Conversation>()
    private lateinit var discussionsAdapter: DiscussionsListAdapter

    private val selectedFilters = mutableSetOf<FilterMode>()
    private var page = 0
    private var isFromRefresh = false
    private val readConversationIds = mutableSetOf<Int>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        updatePaddingTopForEdgeToEdge(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE
        initializeFilterAndCard()

        arguments?.getBoolean("isSmallTalkFilter")?.let { isSmallTalkFilter ->
            if (isSmallTalkFilter) {
                selectedFilters.clear()
                selectedFilters.add(FilterMode.SMALLTALKS)
                updateFilterBadge()
            }
        }

        initializeRecyclerView()
        handleSwipeRefresh()
        binding.btnValidate.visibility = View.GONE
        binding.btnValidate.setOnClickListener {
            discussionsAdapter.isDeletionMode = false
            binding.btnValidate.visibility = View.GONE
        }

        discussionsPresenter.getAllMessages.observe(viewLifecycleOwner, ::handleResponseGetDiscussions)
        discussionsPresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        smallTalkViewModel.smallTalks.observe(viewLifecycleOwner, ::handleResponseGetSmallTalks)
        discussionsPresenter.memberships.observe(viewLifecycleOwner, ::handleResponseGetMemberships)
        discussionsPresenter.hasUserLeftConversation.observe(viewLifecycleOwner, ::handleConversationLeft)
        eventsPresenter.hasUserLeftEvent.observe(viewLifecycleOwner, ::handleConversationLeft)
        groupPresenter.hasUserLeftGroup.observe(viewLifecycleOwner, ::handleConversationLeft)
        smallTalkViewModel.shouldLeave.observe(viewLifecycleOwner, ::handleConversationLeft)

        handleImageViewAnimation()

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
        checkNotificationsState()
    }

    override fun onStop() {
        super.onStop()
        page = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // -------------------- INIT UI --------------------

    private fun initializeFilterAndCard() {
        binding.cardSmallTalkCta.setOnClickListener {
            startActivity(Intent(context, SmallTalkIntroActivity::class.java))
        }
        binding.uiLayoutFilter.setOnClickListener {
            DiscussionsFilterBottomSheet.newInstance(selectedFilters).apply {
                onApply = { newSelection ->
                    selectedFilters.clear()
                    selectedFilters.addAll(newSelection)
                    updateFilterBadge()
                    refreshDisplayedList()
                }
            }.show(childFragmentManager, DiscussionsFilterBottomSheet.TAG)
        }
        updateFilterBadge()
    }

    private fun updateFilterBadge() {
        binding.filterActiveDot.visibility = if (selectedFilters.isEmpty()) View.GONE else View.VISIBLE
    }

    /** EN-9489 : filtre multi-sélection appliqué côté client sur la liste déjà récupérée
     * (toutes les memberships sont toujours fetchées ; aucune sélection = tout afficher). */
    private fun matchesSelectedFilters(conversation: Conversation): Boolean {
        if (selectedFilters.isEmpty()) return true
        return selectedFilters.any { filter ->
            when (filter) {
                FilterMode.PRIVATE    -> conversation.type == "private"
                FilterMode.OUTINGS    -> conversation.type == "outing"
                FilterMode.SMALLTALKS -> conversation.type == "small_talk"
            }
        }
    }

    /** Recalcule [displayedList] (celle liée à l'adapter) à partir de [messagesList]
     * (la liste brute complète) et du filtre courant. */
    private fun refreshDisplayedList() {
        displayedList.clear()
        displayedList.addAll(messagesList.filter { matchesSelectedFilters(it) })
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun initializeRecyclerView() {
        discussionsAdapter = DiscussionsListAdapter(displayedList).apply {
            setOnItemClickListener(object : DiscussionsListAdapter.OnItemClickListener {
                override fun onItemClick(position: Int, conversation: Conversation) {
                    if (discussionsAdapter.isDeletionMode) {
                        deleteConversation(conversation)
                    } else {
                        showDetail(position)
                    }
                }
                override fun onItemLongClick(position: Int, conversation: Conversation): Boolean {
                    //discussionsAdapter.isDeletionMode = true
                    //binding.btnValidate.visibility = View.VISIBLE
                    return true
                }
            })
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = discussionsAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Reset propre côté presenter (évite accumulation/duplication)
            discussionsPresenter.fetchMemberships(null, reset = true)
        }
    }

    private fun reloadFromStart() {
        resetMessagesList()
        page = 0
        discussionsPresenter.isLastPage = false
        discussionsPresenter.getAllMessages.value?.clear()

        discussionsPresenter.currentPageMemberships = 1
        discussionsPresenter.isLastPageMemberships = false

        // ✅ déclenche un refetch propre qui efface la LiveData d'accumulation
        binding.progressBar.visibility = View.VISIBLE
        discussionsPresenter.fetchMemberships(null, reset = true)
    }

    private fun resetMessagesList() {
        messagesList.clear()
        displayedList.clear()
        page = 0
        discussionsPresenter.isLastPage = false
        discussionsPresenter.getAllMessages.value?.clear()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    // -------------------- LOGIQUE MESSAGES --------------------

    private fun handleResponseGetDiscussions(allGroups: MutableList<Conversation>?) {
        if (page == 1) messagesList.clear()
        allGroups?.let { messagesList.addAll(it) }

        // Réappliquez l'état "lu" après un rechargement
        messagesList.forEach { conv ->
            if (readConversationIds.contains(conv.id ?: 0)) {
                conv.numberUnreadMessages = 0
            }
        }

        binding.progressBar.visibility = View.GONE
        refreshDisplayedList()
    }

    private fun handleResponseGetSmallTalks(allSmallTalks: List<SmallTalk>?) {
        messagesList.clear()
        allSmallTalks?.let { list ->
            messagesList.addAll(list.map { smallTalkToConversation(it) })
        }
        binding.progressBar.visibility = View.GONE
        refreshDisplayedList()
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

    private fun showDetail(position: Int) {
        if (position >= displayedList.size) {
            Timber.e("Position out of bound in DiscussionsMainFragment:ShowDetail")
            return
        }
        val conversation = displayedList[position]
        // 1. Marquez comme lu
        conversation.numberUnreadMessages = 0
        conversation.id?.let { id ->
            readConversationIds.add(id)
        }
        discussionsAdapter.notifyItemChanged(position)

        // 2. Lancez l'activité
        VibrationUtil.vibrate(requireContext())
        DetailConversationActivity.isSmallTalkMode = (conversation.type == "small_talk")
        if (DetailConversationActivity.isSmallTalkMode) {
            DetailConversationActivity.smallTalkId = conversation.id.toString()
        }
        startActivity(
            Intent(context, DetailConversationActivity::class.java).putExtras(
                Bundle().apply {
                    conversation.id?.let { putInt(Const.ID, it) }
                    conversation.user?.id?.let { putInt(Const.POST_AUTHOR_ID, it) }
                    putBoolean(Const.SHOULD_OPEN_KEYBOARD, false)
                    putString(Const.NAME, conversation.title)
                    putBoolean(Const.IS_CONVERSATION_1TO1, conversation.isOneToOne())
                    putBoolean(Const.IS_MEMBER, true)
                    putBoolean(Const.IS_CONVERSATION, true)
                    putBoolean(Const.HAS_TO_SHOW_MESSAGE, conversation.hasToShowFirstMessage())
                }
            )
        )
        // ✅ on signale qu'on part au détail ; on NE remettra PAS ce flag à false dans onPause()
        isFromDetail = true
    }

    override fun onPause() {
        super.onPause()
        // Ne PAS remettre isFromDetail à false ici ; on veut le consommer dans onResume()
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
        val areNotificationsEnabled = NotificationManagerCompat.from(requireContext()).areNotificationsEnabled()

        if (!areNotificationsEnabled) {
            binding.layoutAskNotif.visibility = View.VISIBLE
            setStyledText()
            binding.layoutAskNotif.setOnClickListener {
                binding.layoutAskNotif.visibility = View.GONE
                NotificationDemandActivity.comeFromSettings = true
                startActivity(Intent(requireContext(), NotificationDemandActivity::class.java))
            }
        } else {
            binding.layoutAskNotif.visibility = View.GONE
        }
    }

    private fun setStyledText() {
        val fullText = getString(R.string.notifications_disabled_message)
        val boldText = getString(R.string.enable_notifications)
        val spannableString = SpannableString(fullText)

        val startIndex = fullText.indexOf(boldText)
        val endIndex = startIndex + boldText.length

        if (startIndex != -1) {
            spannableString.setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvAskNotif.text = spannableString
    }

    private val recyclerViewOnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            val lm = recyclerView.layoutManager as? LinearLayoutManager ?: return
            val visible = lm.childCount
            val total = lm.itemCount
            val first = lm.findFirstVisibleItemPosition()
            if (!discussionsPresenter.isLoadingMemberships && !discussionsPresenter.isLastPageMemberships) {
                if (visible + first >= total && first >= 0 && total >= discussionsPresenter.perPageMemberships) {
                    discussionsPresenter.fetchMemberships(null)
                }
            }
        }
    }

    private fun handleResponseGetMemberships(memberships: List<ConversationMembership>?) {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        messagesList.clear()
        memberships?.let { list ->
            messagesList.addAll(list.map { membershipToConversation(it) })
            // Réappliquez l'état "lu" aux conversations déjà marquées
            messagesList.forEach { conv ->
                if (readConversationIds.contains(conv.id ?: 0)) {
                    conv.numberUnreadMessages = 0
                }
            }
        }
        refreshDisplayedList()
    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.img.alpha = 1f - ratio
        }
    }

    private fun membershipToConversation(m: ConversationMembership): Conversation {
        var date:java.util.Date? = null
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

    private fun deleteConversation(conversation: Conversation) {
        val displayPosition = displayedList.indexOf(conversation)
        if (displayPosition != -1) {
            displayedList.removeAt(displayPosition)
            discussionsAdapter.notifyItemRemoved(displayPosition)
        }
        messagesList.remove(conversation)
        val id = conversation.id ?: return
        when (conversation.type) {
            "small_talk" -> smallTalkViewModel.leaveSmallTalk(id.toString())
            "outing" -> eventsPresenter.leaveEvent(id)
            "private", "group" -> discussionsPresenter.leaveConverstion(id)
            else -> discussionsPresenter.leaveConverstion(id)
        }
    }
}
