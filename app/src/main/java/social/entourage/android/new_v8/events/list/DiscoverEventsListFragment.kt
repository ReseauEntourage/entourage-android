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
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentDiscoverEventsListBinding
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.models.Events
import java.util.*

const val eventPerPage = 10

class DiscoverEventsListFragment : Fragment() {

    private var _binding: NewFragmentDiscoverEventsListBinding? = null
    val binding: NewFragmentDiscoverEventsListBinding get() = _binding!!


    private val eventsPresenter: EventsPresenter by lazy { EventsPresenter() }
    private var myId: Int? = null
    lateinit var eventsAdapter: GroupEventsListAdapter
    private var page: Int = 0

    private val sections: MutableList<SectionHeader> = mutableListOf()
    private val childListJanuary: MutableList<Events> = mutableListOf()
    private val childListFebruary: MutableList<Events> = mutableListOf()
    private val childListMarch: MutableList<Events> = mutableListOf()
    private val childListApril: MutableList<Events> = mutableListOf()
    private val childListMai: MutableList<Events> = mutableListOf()
    private val childListJune: MutableList<Events> = mutableListOf()
    private val childListJuly: MutableList<Events> = mutableListOf()
    private val childListAugust: MutableList<Events> = mutableListOf()
    private val childListSeptember: MutableList<Events> = mutableListOf()
    private val childListOctober: MutableList<Events> = mutableListOf()
    private val childListNovember: MutableList<Events> = mutableListOf()
    private val childListDecember: MutableList<Events> = mutableListOf()


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
        eventsAdapter = GroupEventsListAdapter(requireContext(), sections, myId)
        loadEvents()
        eventsPresenter.getAllEvents.observe(viewLifecycleOwner, ::handleResponseGetEvents)
        initializeEvents()
        handleSwipeRefresh()
    }

    private fun handleResponseGetEvents(allEvents: MutableList<Events>?) {
        allEvents?.forEach {
            it.metadata?.startsAt?.let { date ->
                val cal = Calendar.getInstance()
                cal.time = date
                when (cal.get(Calendar.MONTH)) {
                    0 -> childListJanuary.add(it)
                    1 -> childListFebruary.add(it)
                    2 -> childListMarch.add(it)
                    3 -> childListApril.add(it)
                    4 -> childListMai.add(it)
                    5 -> childListJune.add(it)
                    6 -> childListJuly.add(it)
                    7 -> childListAugust.add(it)
                    8 -> childListSeptember.add(it)
                    9 -> childListOctober.add(it)
                    10 -> childListNovember.add(it)
                    11 -> childListDecember.add(it)
                }
            }
        }
        sections.clear()
        if (childListJanuary.isNotEmpty()) sections.add(
            SectionHeader(
                childListJanuary,
                getString(R.string.january)
            )
        )
        if (childListFebruary.isNotEmpty()) sections.add(
            SectionHeader(
                childListFebruary,
                getString(R.string.february)
            )
        )
        if (childListMarch.isNotEmpty()) sections.add(
            SectionHeader(
                childListMarch,
                getString(R.string.march)
            )
        )
        if (childListApril.isNotEmpty()) sections.add(
            SectionHeader(
                childListApril,
                getString(R.string.april)
            )
        )
        if (childListMai.isNotEmpty()) sections.add(
            SectionHeader(
                childListMai,
                getString(R.string.may)
            )
        )
        if (childListJune.isNotEmpty()) sections.add(
            SectionHeader(
                childListJune,
                getString(R.string.june)
            )
        )
        if (childListJuly.isNotEmpty()) sections.add(
            SectionHeader(
                childListJuly,
                getString(R.string.july)
            )
        )
        if (childListAugust.isNotEmpty()) sections.add(
            SectionHeader(
                childListAugust,
                getString(R.string.august)
            )
        )
        if (childListSeptember.isNotEmpty()) sections.add(
            SectionHeader(
                childListSeptember,
                getString(R.string.september)
            )
        )
        if (childListOctober.isNotEmpty()) sections.add(
            SectionHeader(
                childListOctober,
                getString(R.string.october)
            )
        )
        if (childListNovember.isNotEmpty()) sections.add(
            SectionHeader(
                childListNovember,
                getString(R.string.november)
            )
        )
        if (childListDecember.isNotEmpty()) sections.add(
            SectionHeader(
                childListDecember,
                getString(R.string.december)
            )
        )
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
        eventsPresenter.getAllEvents(page, eventPerPage)
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.progressBar.visibility = View.VISIBLE
            sections.clear()
            childListJanuary.clear()
            childListFebruary.clear()
            childListMarch.clear()
            childListApril.clear()
            childListMai.clear()
            childListJune.clear()
            childListJuly.clear()
            childListAugust.clear()
            childListSeptember.clear()
            childListOctober.clear()
            childListNovember.clear()
            childListDecember.clear()
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
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= eventPerPage) {
                    loadEvents()
                }
            }
        }
    }
}