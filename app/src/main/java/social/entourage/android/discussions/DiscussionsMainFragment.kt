package social.entourage.android.discussions

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.AppBarLayout
import social.entourage.android.EntourageApplication
import social.entourage.android.databinding.NewFragmentMessagesBinding
import social.entourage.android.RefreshController
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.api.model.Conversation
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber
import kotlin.math.abs

const val messagesPerPage = 25

class DiscussionsMainFragment : Fragment() {

    private var isFromRefresh = false
    private var page: Int = 0
    private var _binding: NewFragmentMessagesBinding? = null
    val binding: NewFragmentMessagesBinding get() = _binding!!

    private var messagesList: MutableList<Conversation> = ArrayList()

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    lateinit var discussionsAdapter: DiscussionsListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMessagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressBar.visibility = View.VISIBLE

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
    }

    override fun onStop() {
        super.onStop()
        messagesList.clear()
        page = 0
    }



    private fun handleResponseGetDiscussions(allGroups: MutableList<Conversation>?) {
        allGroups?.let {
            //messagesList.clear()
            messagesList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        binding.recyclerView.adapter?.notifyDataSetChanged()
        if (isFromRefresh) {
            isFromRefresh = false
            binding.recyclerView.scrollToPosition(0)
        }
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
        page += 1
        discussionsPresenter.getAllMessages(page, messagesPerPage)
    }

    private fun showDetail(position:Int) {
        val conversation = messagesList[position]

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
            val firstVisibleItemPosition: Int =
                layoutManager.findFirstVisibleItemPosition()
            if (!discussionsPresenter.isLoading && !discussionsPresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= messagesPerPage) {
                    loadMessages()
                }
            }
        }
    }

    private fun handleImageViewAnimation() {
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset ->
            val res: Float =
                abs(verticalOffset).toFloat() / appBarLayout.totalScrollRange
            binding.img.alpha = 1f - res
        })
    }
}