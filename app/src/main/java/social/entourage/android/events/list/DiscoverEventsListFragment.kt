package social.entourage.android.events.list

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_header_bottom_sheet.view.view
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentDiscoverEventsListBinding
import social.entourage.android.events.EventFiltersActivity
import social.entourage.android.events.EventsPresenter
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.api.model.Events
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents

const val EVENTS_PER_PAGE = 200

class DiscoverEventsListFragment : Fragment() {

    private var _binding: NewFragmentDiscoverEventsListBinding? = null
    val binding: NewFragmentDiscoverEventsListBinding get() = _binding!!

    private lateinit var eventsPresenter: EventsPresenter
    private var myId: Int? = null
    lateinit var eventsAdapter: AllEventAdapterV2
    lateinit var myeventsAdapter: MyEventRVAdapter
    private var page: Int = 0
    private var pageMyEvent: Int = 0

    private var currentFilters = EventActionLocationFilters()

    private var activityResultLauncher:ActivityResultLauncher<Intent>? = null

    private var isFromFilters = false
    private var isLoading = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
            { result ->
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
        eventsAdapter = AllEventAdapterV2(myId)
        myeventsAdapter = MyEventRVAdapter()
        eventsPresenter.getAllEvents.observe(viewLifecycleOwner, ::handleResponseGetEvents)
        eventsPresenter.hasChangedFilter.observe(viewLifecycleOwner, ::handleFilterChange)
        eventsPresenter.getAllMyEvents.observe(viewLifecycleOwner,::handleResponseGetMYEvents)
        initializeEvents()
        setRVScrollListener()
        handleSwipeRefresh()

    }

    override fun onResume() {
        super.onResume()
        AnalyticsEvents.logEvent(AnalyticsEvents.View__Event__List)
        binding.progressBar.visibility = View.VISIBLE
        eventsAdapter.clearList()
        myeventsAdapter.clearList()
        eventsPresenter.isLastPage = false
        eventsPresenter.isLastPageMyEvent = false
        page = 0
        pageMyEvent = 0
        CoroutineScope(Dispatchers.IO).launch {
            loadEvents()
            loadMyEvents()
        }
    }

    override fun onStop() {
        super.onStop()
        binding.progressBar.visibility = View.VISIBLE
        eventsAdapter.clearList()
        eventsPresenter.isLastPage = false
        eventsPresenter.isLastPageMyEvent = false
        page = 0
        pageMyEvent = 0
    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        binding.progressBar.visibility = View.GONE
        if(allEvents != null && allEvents.size > 0){
            eventsAdapter.resetData(allEvents)
            updateView(false)
        }else{
            updateView(true)
        }
        if (isFromFilters) {
            binding.recyclerView.layoutManager?.scrollToPosition(0)
            isFromFilters = false
        }
        isLoading = false
    }

    private fun handleResponseGetMYEvents(myEvents: MutableList<Events>?) {
        if(myEvents != null && myEvents.size > 0 ) {
            myeventsAdapter.resetData(myEvents!!)
            binding.rvMyEvent.visibility = View.VISIBLE
            binding.separator.visibility = View.VISIBLE
            binding.titleSectionHeaderMyEvent.visibility = View.VISIBLE
        }else{
            binding.rvMyEvent.visibility = View.GONE
            binding.separator.visibility = View.GONE
            binding.titleSectionHeaderMyEvent.visibility = View.GONE
        }

    }

    fun setRVScrollListener() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.nestedScrollView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                if (scrollY > oldScrollY) {
                    eventsPresenter.tellParentFragmentToMoveButton(false)
                } else if (scrollY < oldScrollY) {
                    eventsPresenter.tellParentFragmentToMoveButton(true)
                }
                val nestedScrollView = v as NestedScrollView

                // Calcul de la hauteur totale du contenu à l'intérieur du NestedScrollView
                val contentHeight = v.getChildAt(0).measuredHeight
                // Calcul de la hauteur actuellement visible (la hauteur de la fenêtre de NestedScrollView)
                val visibleHeight = v.height
                // Vérification si la position de défilement actuelle est proche du bas
                if ((scrollY + visibleHeight >= contentHeight - 300) && !isLoading && !eventsPresenter.isLastPage) {
                    isLoading = true
                    loadEvents()
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        }

        binding.rvMyEvent.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                if (totalItemCount <= (lastVisibleItemPosition + 2) && !isLoading && !eventsPresenter.isLastPageMyEvent) {
                    isLoading = true
                    loadMyEvents()
                    binding.progressBar.visibility = View.VISIBLE
                }
            }
        })
    }


    private fun handleFilterChange(hasChangedFilter:Boolean){
        if(hasChangedFilter){
            val intent = Intent(context, EventFiltersActivity::class.java)
            intent.putExtra(EventFiltersActivity.FILTERS,currentFilters)
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
            // Pagination
            layoutManager = LinearLayoutManager(context)
            adapter = eventsAdapter
            isNestedScrollingEnabled = false
        }
        binding.rvMyEvent.apply {
            // Pagination
            val settinglayoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            layoutManager = settinglayoutManager
            val offsetInPixels = resources.getDimensionPixelSize(R.dimen.horizontal_offset) // Define this in your resources
            setPadding(offsetInPixels, 0, 0, 0)
            clipToPadding = false
            adapter = myeventsAdapter
        }
    }

    private fun updateFilters() {
        isFromFilters = true
        page = 0
    }

    private fun loadEvents() {
        if(!eventsPresenter.isLastPage){
            binding.swipeRefresh.isRefreshing = false
            page++
            eventsPresenter.getAllEvents(page, EVENTS_PER_PAGE, currentFilters.travel_distance(),currentFilters.latitude(),currentFilters.longitude(),"future")
        }
    }
    private fun loadMyEvents() {
        binding.swipeRefresh.isRefreshing = false
        pageMyEvent++
        myId = EntourageApplication.me(activity)?.id
        if(myId != null){
            eventsPresenter.getMyEvents(myId!!, pageMyEvent, EVENTS_PER_PAGE)
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
            CoroutineScope(Dispatchers.IO).launch {
                loadEvents()
                loadMyEvents()
            }
        }
    }
}