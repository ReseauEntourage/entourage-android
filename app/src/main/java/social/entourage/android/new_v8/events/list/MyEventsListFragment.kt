package social.entourage.android.new_v8.events.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.databinding.NewFragmentMyEventsListBinding
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents

class MyEventsListFragment : Fragment() {
    private var _binding: NewFragmentMyEventsListBinding? = null
    val binding: NewFragmentMyEventsListBinding get() = _binding!!

    private val eventsPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var myId: Int? = null

    lateinit var eventsAdapter: GroupEventsListAdapter
    private var page: Int = 0

    private var sections: MutableList<SectionHeader> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMyEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myId = EntourageApplication.me(activity)?.id
        eventsAdapter =
            GroupEventsListAdapter(requireContext(), sections, myId)
        loadEvents()
        eventsPresenter.getAllMyEvents.observe(viewLifecycleOwner, ::handleResponseGetEvents)
        initializeEvents()
        handleSwipeRefresh()
        AnalyticsEvents.logEvent(AnalyticsEvents.Event_view_my)
    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        sections = Utils.getSectionHeaders(allEvents, sections)
        binding.progressBar.visibility = View.GONE
        updateView(sections.isEmpty())
        eventsAdapter.notifyDataChanged(sections)
    }

    private fun updateView(isListEmpty: Boolean) {
        binding.emptyStateLayout.isVisible = isListEmpty
        binding.recyclerView.isVisible = !isListEmpty
    }

    private fun initializeEvents() {
        binding.recyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
        }
    }

    private fun loadEvents() {
        binding.swipeRefresh.isRefreshing = false
        page++
        myId?.let { eventsPresenter.getMyEvents(it, page, EVENTS_PER_PAGE) } ?: run {
            binding.progressBar.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
        }
    }


    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.progressBar.visibility = View.VISIBLE
            sections.clear()
            eventsAdapter.notifyDataChanged(sections)
            eventsPresenter.getAllEvents.value?.clear()
            eventsPresenter.isLastPage = false
            page = 0
            loadEvents()
        }
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
            if (!eventsPresenter.isLoading && !eventsPresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= EVENTS_PER_PAGE) {
                    loadEvents()
                }
            }
        }
    }
}