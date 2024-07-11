package social.entourage.android.events.list

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.Events
import social.entourage.android.databinding.NewFragmentDiscoverEventsListBinding
import social.entourage.android.events.EventFiltersActivity
import social.entourage.android.events.EventsPresenter
import social.entourage.android.homev2.HomeEventAdapter
import social.entourage.android.main_filter.MainFilterActivity
import social.entourage.android.main_filter.MainFilterMode
import social.entourage.android.tools.log.AnalyticsEvents

const val EVENTS_PER_PAGE = 20

class DiscoverEventsListFragment : Fragment() {

    private var _binding: NewFragmentDiscoverEventsListBinding? = null
    val binding: NewFragmentDiscoverEventsListBinding get() = _binding!!

    private lateinit var eventsPresenter: EventsPresenter
    private var myId: Int? = null
    private lateinit var eventsAdapter: AllEventAdapter
    private lateinit var myeventsAdapter: HomeEventAdapter
    private lateinit var adapterSearch: AllEventAdapter
    private var page: Int = 0
    private var pageMyEvent: Int = 0
    private var currentFilters = EventActionLocationFilters()
    private var activityResultLauncher: ActivityResultLauncher<Intent>? = null

    private var isFromFilters = false
    private var isLoading = false
    private var isFirstResumeWithFilters = true
    private var lastFiltersHash = 0
    private var isSearching = false
    private var currentSearchQuery: String? = null
    private var isLoadMoreSearchResults = false
    private var searchResultsList: MutableList<Events> = mutableListOf()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val filters = result.data?.getSerializableExtra(EventFiltersActivity.FILTERS) as? EventActionLocationFilters
            filters?.let {
                this.currentFilters = filters
                eventsPresenter.tellParentFragmentToupdateLocation(this.currentFilters)
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
        eventsPresenter = ViewModelProvider(requireActivity()).get(EventsPresenter::class.java)
        myId = EntourageApplication.me(activity)?.id
        eventsAdapter = AllEventAdapter(myId, requireContext())
        myeventsAdapter = HomeEventAdapter(requireContext())
        adapterSearch = AllEventAdapter(myId, requireContext())

        // Observers for events
        eventsPresenter.getAllEvents.observe(viewLifecycleOwner, ::handleResponseGetEvents)
        eventsPresenter.getFilteredEvents.observe(viewLifecycleOwner, ::handleResponseGetEvents)
        eventsPresenter.allEventsSearch.observe(viewLifecycleOwner, ::handleResponseSearchEvents)

        // Observers for my events
        eventsPresenter.getAllMyEvents.observe(viewLifecycleOwner, ::handleResponseGetMYEvents)
        eventsPresenter.getFilteredMyEvents.observe(viewLifecycleOwner, ::handleResponseGetMYEvents)

        eventsPresenter.hasChangedFilter.observe(viewLifecycleOwner, ::handleFilterChange)
        initializeEvents()
        setRVScrollListener()
        handleSwipeRefresh()
        setupSearchView()
        initView()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchEditText.hasFocus()) {
                    binding.searchEditText.clearFocus()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (MainFilterActivity.savedGroupInterests.size > 0) {
            binding.cardFilterNumber.visibility = View.VISIBLE
            binding.tvNumberOfFilter.text = MainFilterActivity.savedGroupInterests.size.toString()
            binding.layoutFilter.background = resources.getDrawable(R.drawable.bg_selected_filter_main)
        } else {
            binding.cardFilterNumber.visibility = View.GONE
            binding.layoutFilter.background = resources.getDrawable(R.drawable.bg_unselected_filter_main)
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.View__Event__List)

        val currentFiltersHash = currentFilters.hashCode()
        if (currentFiltersHash != lastFiltersHash) {
            isFirstResumeWithFilters = true
            lastFiltersHash = currentFiltersHash
        }

        if (isFirstResumeWithFilters) {
            isFirstResumeWithFilters = false
            resetDataAndApplyFilters()
        }
    }

    private fun resetDataAndApplyFilters() {
        eventsAdapter.clearList()
        myeventsAdapter.clearList()
        page = 0
        pageMyEvent = 0
        eventsPresenter.isLastPage = false
        eventsPresenter.isLastPageMyEvent = false
        if (MainFilterActivity.savedGroupInterests.isNotEmpty() ||
            MainFilterActivity.savedRadius != 0 ||
            MainFilterActivity.savedLocation != null) {
            applyFilters()
        } else {
            loadEvents()
            loadMyEvents()
        }
    }

    fun initView() {
        isLoading = false
        binding.progressBar.visibility = View.VISIBLE
        binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
        eventsAdapter.clearList()
        myeventsAdapter.clearList()
        binding.layoutFilter.setOnClickListener {
            isFirstResumeWithFilters = true
            MainFilterActivity.mod = MainFilterMode.GROUP
            val intent = Intent(activity, MainFilterActivity::class.java)
            startActivity(intent)
        }
        eventsPresenter.isLastPage = false
        eventsPresenter.isLastPageMyEvent = false
        page = 0
        pageMyEvent = 0

    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        if (allEvents != null && allEvents.isNotEmpty()) {
            if (isFromFilters) {
                eventsAdapter.resetData(allEvents)
                isFromFilters = false
            } else {
                eventsAdapter.addData(allEvents)
            }
            updateView(false)
        } else {
            updateView(true)
        }
        isLoading = false
        binding.progressBar.visibility = View.GONE
    }

    private fun handleResponseGetMYEvents(myEvents: MutableList<Events>?) {
        if (myEvents != null && myEvents.isNotEmpty()) {
            myeventsAdapter.addData(myEvents)
            binding.rvMyEvent.visibility = View.VISIBLE
            binding.separator.visibility = View.VISIBLE
            binding.titleSectionHeaderMyEvent.visibility = View.VISIBLE
        } else {
            binding.rvMyEvent.visibility = View.GONE
            binding.separator.visibility = View.GONE
            binding.titleSectionHeaderMyEvent.visibility = View.GONE
        }
        isLoading = false
        binding.progressBar.visibility = View.GONE
    }

    private fun handleResponseSearchEvents(allEvents: MutableList<Events>?) {
        if (isLoadMoreSearchResults) {
            adapterSearch.addData(allEvents ?: mutableListOf())
        } else {
            adapterSearch.resetData(allEvents ?: mutableListOf())
        }
        updateView(allEvents.isNullOrEmpty())
        isLoading = false
        binding.progressBar.visibility = View.GONE
    }

    private fun setRVScrollListener() {
        binding.nestedScrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            if (scrollY > oldScrollY) {
                eventsPresenter.tellParentFragmentToMoveButton(false)
            } else if (scrollY < oldScrollY) {
                eventsPresenter.tellParentFragmentToMoveButton(true)
            }
            if (!binding.nestedScrollView.canScrollVertically(1) && !isLoading) {
                binding.progressBar.visibility = View.VISIBLE
                if (isSearching) {
                    currentSearchQuery?.let { query ->
                        searchEvents(query, isLoadMore = true)
                    }
                } else {
                    if (!eventsPresenter.isLastPage) {
                        loadEvents()
                    }
                }
            }
        })

        binding.rvMyEvent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (totalItemCount <= (lastVisibleItemPosition + 2) && !isLoading) {
                    binding.progressBar.visibility = View.VISIBLE
                    if (isSearching) {
                        currentSearchQuery?.let { query ->
                            searchEvents(query, isLoadMore = true)
                        }
                    } else {
                        if (!eventsPresenter.isLastPageMyEvent) {
                            loadMyEvents()
                        }
                    }
                }
            }
        })
    }

    private fun handleFilterChange(hasChangedFilter: Boolean) {
        if (hasChangedFilter) {
            val intent = Intent(context, EventFiltersActivity::class.java)
            intent.putExtra(EventFiltersActivity.FILTERS, currentFilters)
            activityResultLauncher?.launch(intent)
            eventsPresenter.hasChangedFilter()
        }
    }

    private fun updateView(isListEmpty: Boolean) {
        binding.emptyStateLayout.isVisible = isListEmpty
        binding.recyclerView.isVisible = !isListEmpty
    }

    private fun initializeEvents() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
            isNestedScrollingEnabled = false
        }
        binding.rvMyEvent.apply {
            val settinglayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = settinglayoutManager
            val offsetInPixels = resources.getDimensionPixelSize(R.dimen.horizontal_offset)
            setPadding(offsetInPixels, 0, 0, 0)
            clipToPadding = false
            adapter = myeventsAdapter
        }

        binding.rvSearch.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterSearch
        }
    }

    private fun updateFilters() {
        isFromFilters = true
        page = 0
        applyFilters()
    }

    private fun applyFilters() {
        eventsAdapter.clearList()
        myeventsAdapter.clearList()
        page = 0
        pageMyEvent = 0
        eventsPresenter.isLastPage = false
        eventsPresenter.isLastPageMyEvent = false
        loadEvents()
        loadMyEvents()
    }

    private fun loadEvents() {
        page++
        if (!eventsPresenter.isLastPage && !isLoading) {
            isLoading = true
            Log.wtf("wtf" , "save interest : ${MainFilterActivity.savedGroupInterests}")
            if (MainFilterActivity.savedGroupInterests.isEmpty()){
                // Si aucun filtre n'est sélectionné, utiliser getAllEvents
                eventsPresenter.getAllEvents(
                    page, EVENTS_PER_PAGE,
                    currentFilters.travel_distance(), currentFilters.latitude(), currentFilters.longitude(), "future"
                )
            } else {
                // Sinon, utiliser getAllEventsWithFilter avec les filtres
                val radius = MainFilterActivity.savedRadius.takeIf { it != 0 } ?: currentFilters.travel_distance()
                val latitude = MainFilterActivity.savedLocation?.lat ?: currentFilters.latitude()
                val longitude = MainFilterActivity.savedLocation?.lng ?: currentFilters.longitude()
                eventsPresenter.getAllEventsWithFilter(
                    page, EVENTS_PER_PAGE,
                    MainFilterActivity.savedGroupInterests.joinToString(","),
                    radius, latitude, longitude, "future"
                )
            }
        }
    }

    private fun loadMyEvents() {
        pageMyEvent++
        binding.swipeRefresh.isRefreshing = false
        myId = EntourageApplication.me(activity)?.id
        if (myId != null) {
            if (MainFilterActivity.savedGroupInterests.isEmpty()) {
                // Si aucun filtre n'est sélectionné, utiliser getMyEvents
                eventsPresenter.getMyEvents(
                    myId!!,
                    pageMyEvent, EVENTS_PER_PAGE
                )
            } else {
                // Sinon, utiliser getMyEventsWithFilter avec les filtres
                val radius = MainFilterActivity.savedRadius.takeIf { it != 0 } ?: currentFilters.travel_distance()
                val latitude = MainFilterActivity.savedLocation?.lat ?: currentFilters.latitude()
                val longitude = MainFilterActivity.savedLocation?.lng ?: currentFilters.longitude()
                eventsPresenter.getMyEventsWithFilter(
                    myId!!,
                    pageMyEvent, EVENTS_PER_PAGE,
                    MainFilterActivity.savedGroupInterests.joinToString(","),
                    radius, latitude, longitude, "future"
                )
            }
        }
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.progressBar.visibility = View.VISIBLE
            eventsAdapter.clearList()
            myeventsAdapter.clearList()
            eventsPresenter.isLastPage = false
            eventsPresenter.isLastPageMyEvent = false
            page = 0
            pageMyEvent = 0
            if (isSearching && currentSearchQuery != null) {
                searchEvents(currentSearchQuery!!)
            } else {
                loadEvents()
                loadMyEvents()
            }
        }
    }

    private fun tintDrawable(drawable: Drawable?, color: Int) {
        drawable?.let {
            DrawableCompat.setTint(DrawableCompat.wrap(it), color)
        }
    }

    private fun setupSearchView() {
        val clearDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_cross_orange)
        val backDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.ic_arrow_left_black)

        val color = ContextCompat.getColor(requireContext(), android.R.color.black)
        tintDrawable(clearDrawable, color)
        tintDrawable(backDrawable, color)

        binding.searchEditText.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                hideMainViews()
                binding.rvSearch.visibility = View.VISIBLE
                binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(backDrawable, null, clearDrawable, null)
            } else {
                val query = binding.searchEditText.text.toString()
                if (query.isEmpty()) {
                    showMainViews()
                    binding.rvSearch.visibility = View.GONE
                    binding.searchEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
            }
        }

        binding.searchEditText.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val clearIcon = binding.searchEditText.compoundDrawables[2]
                val backIcon = binding.searchEditText.compoundDrawables[0]
                if (clearIcon != null && event.rawX >= (binding.searchEditText.right - clearIcon.bounds.width())) {
                    binding.searchEditText.text.clear()
                    return@setOnTouchListener true
                } else if (backIcon != null && event.rawX <= (binding.searchEditText.left + backIcon.bounds.width())) {
                    binding.searchEditText.clearFocus()
                    return@setOnTouchListener true
                }
            }
            false
        }

        // Ajouter un TextWatcher pour surveiller les changements de texte
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.isEmpty()) {

                } else {
                    performSearch(query)
                }
            }
        })
    }

    private fun performSearch(query: String) {
        isSearching = true
        currentSearchQuery = query
        searchResultsList.clear()
        adapterSearch.resetData(searchResultsList)
        page = 0
        searchEvents(query)
    }

    private fun searchEvents(query: String, isLoadMore: Boolean = false) {
        if (!isLoadMore) {
            page = 0
            searchResultsList.clear()
            adapterSearch.resetData(searchResultsList)
            isLoadMoreSearchResults = false
        } else {
            isLoadMoreSearchResults = true
        }
        page++
        eventsPresenter.getAllEventsWithSearchQuery(query, page, EVENTS_PER_PAGE)
    }

    private fun hideMainViews() {
        val layoutParams = binding.searchEditText.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginEnd = 70
        binding.searchEditText.layoutParams = layoutParams

        binding.recyclerView.visibility = View.GONE
        binding.rvMyEvent.visibility = View.GONE
        binding.titleSectionHeaderMyEvent.visibility = View.GONE
        binding.titleSectionHeaderEvent.visibility = View.GONE
        binding.separator.visibility = View.GONE
        binding.separator2.visibility = View.GONE
        binding.rvSearch.visibility = View.VISIBLE
        binding.layoutFilter.visibility = View.GONE
        eventsPresenter.hideButton()
    }

    private fun showMainViews() {
        val layoutParams = binding.searchEditText.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.marginEnd = 8
        binding.searchEditText.layoutParams = layoutParams

        binding.recyclerView.visibility = View.VISIBLE
        binding.rvMyEvent.visibility = View.VISIBLE
        binding.titleSectionHeaderMyEvent.visibility = View.VISIBLE
        binding.titleSectionHeaderEvent.visibility = View.VISIBLE
        binding.separator.visibility = View.VISIBLE
        binding.separator2.visibility = View.VISIBLE
        binding.rvSearch.visibility = View.GONE
        binding.layoutFilter.visibility = View.VISIBLE
        eventsPresenter.showButton()
    }
}
