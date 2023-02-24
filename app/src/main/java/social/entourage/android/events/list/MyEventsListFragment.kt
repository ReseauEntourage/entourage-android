package social.entourage.android.events.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.databinding.NewFragmentMyEventsListBinding
import social.entourage.android.events.EventsPresenter
import social.entourage.android.api.model.Events
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class MyEventsListFragment : Fragment() {
    private var _binding: NewFragmentMyEventsListBinding? = null
    val binding: NewFragmentMyEventsListBinding get() = _binding!!

    private lateinit var eventsPresenter: EventsPresenter
    private var myId: Int? = null

    lateinit var eventsAdapter: GroupEventsListAdapter
    private var page: Int = 0
    private var currentFilters = EventActionLocationFilters()

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
        eventsPresenter = ViewModelProvider(requireActivity()).get(EventsPresenter::class.java)
        myId = EntourageApplication.me(activity)?.id
        eventsAdapter =
            GroupEventsListAdapter(requireContext(), sections, myId)
        loadEvents()
        eventsPresenter.getAllMyEvents.observe(requireActivity(), ::handleResponseGetEvents)
        eventsPresenter.getAllEvents.observe(requireActivity(), ::handleDiscoverEvent)
        initializeEvents()
        initializeDiscoverEventButton()
        handleSwipeRefresh()
        AnalyticsEvents.logEvent(AnalyticsEvents.Event_view_my)
    }



    private fun initializeDiscoverEventButton(){
        binding.btnDiscoverEvent.setOnClickListener {
            eventsPresenter.changePage()
        }
    }

    private fun initializeNoEventCreateButton(){
        binding.btnDiscoverEvent.setOnClickListener {
            eventsPresenter.launchCreateEvent()
        }
    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        sections = Utils.getSectionHeaders(allEvents, sections)
        binding.progressBar.visibility = View.GONE
        updateView(sections.isEmpty())
        eventsAdapter.notifyDataChanged(sections)
    }
    private fun handleDiscoverEvent(allEvents: MutableList<Events>?) {
        if(isAdded){
            binding.progressBar.visibility = View.GONE
            if(allEvents?.isEmpty() == true){
                binding.btnDiscoverEvent.setText(getString(R.string.create_event))
                initializeNoEventCreateButton()
            }else{
                binding.btnDiscoverEvent.setText(getString(R.string.discover_events))
                initializeDiscoverEventButton()
            }
        }

    }

    private fun updateView(isListEmpty: Boolean) {
        binding.emptyStateLayout.isVisible = isListEmpty
        binding.recyclerView.isVisible = !isListEmpty
        currentFilters.resetToDefault()
        eventsPresenter.getAllEvents(page, EVENTS_PER_PAGE, currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),"")
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