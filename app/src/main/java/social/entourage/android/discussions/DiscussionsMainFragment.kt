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
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.RefreshController
import social.entourage.android.api.model.Conversation
import social.entourage.android.databinding.NewFragmentMessagesBinding
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.notifications.NotificationDemandActivity
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.updatePaddingTopForEdgeToEdge
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.VibrationUtil
import kotlin.math.abs

enum class FilterMode {
    ALL, PRIVATE, OUTINGS
}

const val messagesPerPage = 25

class DiscussionsMainFragment : Fragment() {

    private var isFromRefresh = false
    private var page: Int = 0
    private var _binding: NewFragmentMessagesBinding? = null
    val binding: NewFragmentMessagesBinding get() = _binding!!

    private var messagesList: MutableList<Conversation> = ArrayList()

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private var currentFilterMode: FilterMode = FilterMode.ALL // Mode par défaut

    private lateinit var discussionsAdapter: DiscussionsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMessagesBinding.inflate(inflater, container, false)
        updatePaddingTopForEdgeToEdge(binding.appBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE
        initializeSearchBar()
        initializeRV()
        handleSwipeRefresh()
        discussionsPresenter.getAllMessages.observe(viewLifecycleOwner, ::handleResponseGetDiscussions)
        handleImageViewAnimation()
        discussionsPresenter.unreadMessages.observe(requireActivity(), ::updateUnreadCount)
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
        //UNCOMMENT FOR VIDEO CALL FEATURE
/*        binding.buttonCall.setOnClickListener {
            val intent = Intent(requireContext(), VideoCallActivity::class.java)
            startActivity(intent)
        }*/
        checkNotificationsState()

    }

    override fun onStop() {
        super.onStop()
        messagesList.clear()
        page = 0
    }

    private fun checkNotificationsState() {
        val notificationManager =
            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val areNotificationsEnabled = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.areNotificationsEnabled()
        } else {
            Settings.Secure.getInt(
                requireContext().contentResolver,
                "notification_sound", 1
            ) == 1
        }

        if (!areNotificationsEnabled) {
            binding.layoutAskNotif.visibility = View.VISIBLE
            binding.tvAskNotif.text = getString(R.string.notifications_disabled_message)
            setStyledText()
            binding.layoutAskNotif.setOnClickListener {
                binding.layoutAskNotif.visibility = View.GONE
                NotificationDemandActivity.comeFromSettings = true
                val intent = Intent(requireContext(), NotificationDemandActivity::class.java)
                this.startActivity(intent)
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
            // Style : souligné
            spannableString.setSpan(UnderlineSpan(), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            // Style : gras
            spannableString.setSpan(StyleSpan(Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        binding.tvAskNotif.text = spannableString
    }

    private fun handleResponseGetDiscussions(allGroups: MutableList<Conversation>?) {
        allGroups?.let {
            if (page == 1) { // Si c'est la première page, on reset
                messagesList.clear()
            }
            messagesList.addAll(it) // Ajoute les nouveaux messages sans supprimer les anciens
        }

        binding.progressBar.visibility = View.GONE
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }


    private fun updateUnreadCount(unreadMessages: UnreadMessages?) {
        val count:Int = unreadMessages?.unreadCount ?: 0
        EntourageApplication.get().mainActivity?.let {
            val viewModel = ViewModelProvider(it)[CommunicationHandlerBadgeViewModel::class.java]
            viewModel.badgeCount.postValue(UnreadMessages(count))
        }
    }

    private fun loadMessages() {
        binding.swipeRefresh.isRefreshing = false
        page += 1 // On incrémente la page pour la pagination

        when (currentFilterMode) {
            FilterMode.ALL -> discussionsPresenter.fetchAllConversations(page, messagesPerPage)
            FilterMode.PRIVATE -> discussionsPresenter.fetchPrivateConversations(page, messagesPerPage)
            FilterMode.OUTINGS -> discussionsPresenter.fetchOutingConversations(page, messagesPerPage)
        }
    }


    private fun showDetail(position:Int) {
        val conversation = messagesList[position]
        VibrationUtil.vibrate(requireContext())
        DetailConversationActivity.isSmallTalkMode = false
        startActivity(
            Intent(context, DetailConversationActivity::class.java)
                .putExtras(
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

    private fun initializeSearchBar() {
        binding.filter1.buttonStart.text = getString(R.string.filter_all)
        binding.filter2.buttonStart.text = getString(R.string.filter_discussions)
        binding.filter3.buttonStart.text = getString(R.string.filter_events)

        setFilterActive(binding.filter1.root)
        setFilterInactive(binding.filter2.root)
        setFilterInactive(binding.filter3.root)

        binding.filter1.buttonStart.setOnClickListener {
            if (currentFilterMode != FilterMode.ALL) { // Vérifie si on change de filtre
                currentFilterMode = FilterMode.ALL
                resetMessagesList() // Reset seulement si changement de filtre
            }
            setFilterActive(binding.filter1.root)
            setFilterInactive(binding.filter2.root)
            setFilterInactive(binding.filter3.root)
            loadMessages()
        }

        binding.filter2.buttonStart.setOnClickListener {
            if (currentFilterMode != FilterMode.PRIVATE) {
                currentFilterMode = FilterMode.PRIVATE
                resetMessagesList()
            }
            setFilterInactive(binding.filter1.root)
            setFilterActive(binding.filter2.root)
            setFilterInactive(binding.filter3.root)
            loadMessages()
        }

        binding.filter3.buttonStart.setOnClickListener {
            if (currentFilterMode != FilterMode.OUTINGS) {
                currentFilterMode = FilterMode.OUTINGS
                resetMessagesList()
            }
            setFilterInactive(binding.filter1.root)
            setFilterInactive(binding.filter2.root)
            setFilterActive(binding.filter3.root)
            loadMessages()
        }

        binding.filterLayout.visibility = View.VISIBLE
    }

    /**
     * Réinitialise la liste des messages et la page pour un nouveau filtre.
     */
    private fun resetMessagesList() {
        messagesList.clear()
        discussionsPresenter.isLastPage = false
        discussionsPresenter.getAllMessages.postValue(messagesList)
        page = 0
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    /**
     * Active un filtre (change son background en activé)
     */
    private fun setFilterActive(filter: View) {
        filter.findViewById<Button>(R.id.button_start).apply {
            setBackgroundResource(R.drawable.shape_filter_discussion_activated)
            setTextColor(resources.getColor(android.R.color.white, null))
        }
    }

    /**
     * Désactive un filtre (change son background en désactivé)
     */
    private fun setFilterInactive(filter: View) {
        filter.findViewById<Button>(R.id.button_start).apply {
            setBackgroundResource(R.drawable.shape_filter_discussion_desactivated)
            setTextColor(resources.getColor(R.color.orange, null)) // Change la couleur selon ton design
        }
    }

    private fun initializeRV() {
        discussionsAdapter = DiscussionsListAdapter(messagesList, object : OnItemClick {
            override fun onItemClick(position: Int) {
                showDetail(position)
            }
        })

        binding.recyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = discussionsAdapter
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
        binding.recyclerView.adapter?.notifyDataSetChanged()
        discussionsPresenter.getAllMessages.value?.clear()
        discussionsPresenter.isLastPage = false
        loadMessages()
    }

    private val recyclerViewOnScrollListener: RecyclerView.OnScrollListener =
        object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handlePagination(recyclerView)
            }
        }

    fun handlePagination(recyclerView: RecyclerView) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        layoutManager?.let {
            val visibleItemCount: Int = layoutManager.childCount
            val totalItemCount: Int = layoutManager.itemCount
            val firstVisibleItemPosition: Int = layoutManager.findFirstVisibleItemPosition()

            if (!discussionsPresenter.isLoading && !discussionsPresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= messagesPerPage) {
                    loadMessages() // Charge la page suivante en fonction du filtre actif
                }
            }
        }
    }


    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.img.alpha = 1f - res
        }
    }
}