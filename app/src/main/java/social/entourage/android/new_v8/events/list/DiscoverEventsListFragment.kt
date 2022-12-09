package social.entourage.android.new_v8.events.list

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.databinding.NewFragmentDiscoverEventsListBinding
import social.entourage.android.new_v8.events.EventFiltersActivity
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.models.EventActionLocationFilters
import social.entourage.android.new_v8.models.Events
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents

const val EVENTS_PER_PAGE = 10

class DiscoverEventsListFragment : Fragment() {

    private var _binding: NewFragmentDiscoverEventsListBinding? = null
    val binding: NewFragmentDiscoverEventsListBinding get() = _binding!!


    private val eventsPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var myId: Int? = null
    lateinit var eventsAdapter: GroupEventsListAdapter
    private var page: Int = 0

    private var sections: MutableList<SectionHeader> = mutableListOf()

    private var currentFilters = EventActionLocationFilters()

    private var activityResultLauncher:ActivityResultLauncher<Intent>? = null

    private var isFromFilters = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
            { result ->
                val filters = result.data?.getSerializableExtra(EventFiltersActivity.FILTERS) as? EventActionLocationFilters
                filters?.let {
                    this.currentFilters = filters
                    updateFilters()
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentDiscoverEventsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myId = EntourageApplication.me(activity)?.id
        eventsAdapter =
            GroupEventsListAdapter(requireContext(), sections, myId)
        loadEvents()
        eventsPresenter.getAllEvents.observe(viewLifecycleOwner, ::handleResponseGetEvents)
        initializeEvents()
        handleSwipeRefresh()
        AnalyticsEvents.logEvent(AnalyticsEvents.Event_view_discover)
    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        if(page == 1) {
            sections.clear()
        }
        sections = Utils.getSectionHeaders(allEvents, sections)
        binding.progressBar.visibility = View.GONE
        updateView(sections.isEmpty())
        eventsAdapter.notifyDataChanged(sections)
        if (isFromFilters && sections.size > 0) {
            binding.recyclerView.layoutManager?.scrollToPosition(0)
            isFromFilters = false
        }
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

        binding.uiLayoutLocationBt.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_action_filter)
            val intent = Intent(context, EventFiltersActivity::class.java)
            intent.putExtra(EventFiltersActivity.FILTERS,currentFilters)
            activityResultLauncher?.launch(intent)
        }

        binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())
    }

    private fun updateFilters() {
        isFromFilters = true
        binding.uiTitleLocationBt.text = currentFilters.getFilterButtonString(requireContext())
        page = 0
        loadEvents()
    }

    private fun loadEvents() {
        binding.swipeRefresh.isRefreshing = false
        page++
        eventsPresenter.getAllEvents(page, EVENTS_PER_PAGE, currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude())
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