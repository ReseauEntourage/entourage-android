package social.entourage.android.discussions

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.*
import social.entourage.android.api.model.*
import social.entourage.android.databinding.NewFragmentMessagesBinding
import social.entourage.android.events.create.CommunicationHandler
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.VibrationUtil
import timber.log.Timber
import kotlin.math.abs

enum class FilterMode {
    ALL, PRIVATE, OUTINGS, SMALLTALKS
}

const val messagesPerPage = 25

class DiscussionsMainFragment : Fragment() {

    private var _binding: NewFragmentMessagesBinding? = null
    private val binding get() = _binding!!

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by lazy {
        ViewModelProvider(requireActivity())[SmallTalkViewModel::class.java]
    }

    private val messagesList = mutableListOf<Conversation>()
    private lateinit var discussionsAdapter: DiscussionsListAdapter

    private var currentFilterMode: FilterMode = FilterMode.ALL
    private var page = 0
    private var isFromRefresh = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = NewFragmentMessagesBinding.inflate(inflater, container, false)
        updatePaddingTopForEdgeToEdge(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.progressBar.visibility = View.VISIBLE
        initializeSearchBar()
        initializeRecyclerView()
        handleSwipeRefresh()

        discussionsPresenter.getAllMessages.observe(viewLifecycleOwner, ::handleResponseGetDiscussions)
        discussionsPresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
        smallTalkViewModel.smallTalks.observe(viewLifecycleOwner, ::handleResponseGetSmallTalks)
        discussionsPresenter.memberships.observe(viewLifecycleOwner, ::handleResponseGetMemberships)

        handleImageViewAnimation()

        AnalyticsEvents.logEvent(AnalyticsEvents.Message_view)
    }

    override fun onResume() {
        super.onResume()
        reloadFromStart()
        if (RefreshController.shouldRefreshFragment) {
            RefreshController.shouldRefreshFragment = false
            isFromRefresh = true
        }
        discussionsPresenter.getUnreadCount()
        checkNotificationsState()
    }

    override fun onStop() {
        super.onStop()
        messagesList.clear()
        page = 0
    }

    // -------------------- INIT UI --------------------

    private fun initializeSearchBar() {
        binding.filter1.buttonStart.text = getString(R.string.filter_all)
        binding.filter2.buttonStart.text = getString(R.string.filter_discussions)
        binding.filter3.buttonStart.text = getString(R.string.filter_events)
        binding.filter4.buttonStart.text = getString(R.string.filter_band_solidarity)

        setFilterActive(binding.filter1.root)
        setFilterInactive(binding.filter2.root)
        setFilterInactive(binding.filter3.root)
        setFilterInactive(binding.filter4.root)

        binding.filter1.buttonStart.setOnClickListener {
            changeFilterMode(FilterMode.ALL, binding.filter1.root)
        }
        binding.filter2.buttonStart.setOnClickListener {
            changeFilterMode(FilterMode.PRIVATE, binding.filter2.root)
        }
        binding.filter3.buttonStart.setOnClickListener {
            changeFilterMode(FilterMode.OUTINGS, binding.filter3.root)
        }
        binding.filter4.buttonStart.setOnClickListener {
            changeFilterMode(FilterMode.SMALLTALKS, binding.filter4.root)
        }

        binding.filterLayout.visibility = View.VISIBLE
    }

    private fun initializeRecyclerView() {
        discussionsAdapter = DiscussionsListAdapter(messagesList, object : OnItemClick {
            override fun onItemClick(position: Int) {
                showDetail(position)
            }
        })

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = discussionsAdapter
            addOnScrollListener(recyclerViewOnScrollListener)
        }
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.progressBar.visibility = View.VISIBLE
            reloadFromStart()
        }
    }

    private fun reloadFromStart() {
        messagesList.clear()
        page = 0
        discussionsPresenter.isLastPage = false
        discussionsPresenter.getAllMessages.value?.clear()
        binding.recyclerView.adapter?.notifyDataSetChanged()
        loadMessages()
    }

    private fun changeFilterMode(newMode: FilterMode, activeButton: View) {
        if (currentFilterMode != newMode) {
            currentFilterMode = newMode
            resetMessagesList()
        }

        setFilterActive(activeButton)
        listOf(binding.filter1.root, binding.filter2.root, binding.filter3.root, binding.filter4.root)
            .filter { it != activeButton }
            .forEach { setFilterInactive(it) }

        loadMessages()
    }

    private fun resetMessagesList() {
        messagesList.clear()
        page = 0
        discussionsPresenter.isLastPage = false
        discussionsPresenter.getAllMessages.value?.clear()
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun setFilterActive(view: View) {
        view.findViewById<Button>(R.id.button_start).apply {
            setBackgroundResource(R.drawable.shape_filter_discussion_activated)
            setTextColor(resources.getColor(android.R.color.white, null))
        }
    }

    private fun setFilterInactive(view: View) {
        view.findViewById<Button>(R.id.button_start).apply {
            setBackgroundResource(R.drawable.shape_filter_discussion_desactivated)
            setTextColor(resources.getColor(R.color.orange, null))
        }
    }

    // -------------------- LOGIQUE MESSAGES --------------------

    private fun loadMessages() {
        binding.swipeRefresh.isRefreshing = false
        binding.progressBar.visibility = View.VISIBLE

        // Plus de pagination ici : la nouvelle route ne la supporte pas encore
        when (currentFilterMode) {
            FilterMode.ALL -> discussionsPresenter.fetchMemberships(null)
            FilterMode.PRIVATE -> discussionsPresenter.fetchMemberships("Conversation")
            FilterMode.OUTINGS -> discussionsPresenter.fetchMemberships("Outing")
            FilterMode.SMALLTALKS -> discussionsPresenter.fetchMemberships("Smalltalk")
        }
    }

    private fun handleResponseGetDiscussions(allGroups: MutableList<Conversation>?) {
        if (page == 1) messagesList.clear()
        allGroups?.let { messagesList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun handleResponseGetSmallTalks(allSmallTalks: List<SmallTalk>?) {
        messagesList.clear()
        allSmallTalks?.let { list ->
            messagesList.addAll(list.map { smallTalkToConversation(it) })
        }
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.adapter?.notifyDataSetChanged()
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
        val conversation = messagesList[position]
        VibrationUtil.vibrate(requireContext())
        DetailConversationActivity.isSmallTalkMode = (currentFilterMode == FilterMode.SMALLTALKS || conversation.type == "small_talk")
        if(DetailConversationActivity.isSmallTalkMode){
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
        messagesList[position].numberUnreadMessages = 0
        discussionsPresenter.getAllMessages.postValue(messagesList)
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
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val areNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            Settings.Secure.getInt(requireContext().contentResolver, "notification_sound", 1) == 1
        }

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
            super.onScrolled(recyclerView, dx, dy)
            handlePagination(recyclerView)
        }
    }

    private fun handlePagination(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (!discussionsPresenter.isLoading && !discussionsPresenter.isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount &&
                firstVisibleItemPosition >= 0 &&
                totalItemCount >= messagesPerPage
            ) {
                loadMessages()
            }
        }
    }

    private fun handleResponseGetMemberships(memberships: List<ConversationMembership>?) {
        binding.progressBar.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false

        messagesList.clear()
        memberships?.let { list ->
            Timber.wtf("wtf message list size : ${list.size}")
            messagesList.addAll(list.map { membershipToConversation(it) })
        }

        binding.recyclerView.adapter?.notifyDataSetChanged()
    }
    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val ratio = abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.img.alpha = 1f - ratio
        }
    }

    private fun membershipToConversation(m: ConversationMembership): Conversation {
        return Conversation(
            id = m.joinableId,
            type = when (m.joinableType?.lowercase()) {
                "outing" -> "outing"
                "conversation" -> "private"
                "smalltalk" -> "small_talk"
                "neighborhood" -> "group"
                else -> "group"
            },
            title = m.name ?: "[Sans nom]",
            lastMessage = m.lastChatMessageText?.let { LastMessage(it, null) }, // conversion depuis String
            numberUnreadMessages = m.numberOfUnreadMessages ?: 0,
            memberCount = m.numberOfPeople ?: 0
        )
    }
}
