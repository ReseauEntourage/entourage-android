package social.entourage.android.actions.list

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
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.actions.CATEGORIES_FILTERS
import social.entourage.android.actions.FILTERS
import social.entourage.android.actions.FILTERS2
import social.entourage.android.actions.LOCATION_FILTERS
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.ActionSectionFilters
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.databinding.FragmentActionListBinding
import social.entourage.android.tools.log.AnalyticsEvents


class ActionListFragment : Fragment() {

    private var _binding: FragmentActionListBinding? = null
    val binding: FragmentActionListBinding get() = _binding!!

    private val actionsPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    private var myId: Int? = null
    private lateinit var actionAdapter: ActionsListAdapter
    private var page: Int = 0

    private var currentFilters = EventActionLocationFilters()
    private var currentSectionsFilters = ActionSectionFilters()

    private var isFromFilters = false
    private var isContrib = true

    private var allActions:MutableList<Action>  = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isContrib = arguments?.getBoolean(IS_CONTRIB,false) ?: false

        myId = EntourageApplication.me(activity)?.id
        actionAdapter =
            ActionsListAdapter(allActions,myId, isContrib, requireContext())
        initializeEmptyState()
        loadActions()
        actionsPresenter.getAllActions.observe(viewLifecycleOwner, ::handleResponseGetDemands)
        initializeEvents()
        handleSwipeRefresh()
        addParentFragmentListener()

        if (isContrib) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_contrib)
        }
        else {
            AnalyticsEvents.logEvent(AnalyticsEvents.Help_view_demand)
        }
    }

    private fun addParentFragmentListener() {
        //Use to receive datas from parent Fragment ;)
        if (isContrib) {
            parentFragmentManager.setFragmentResultListener(FILTERS,this) { _, bundle ->
                val locFilters = bundle.getSerializable(LOCATION_FILTERS) as? EventActionLocationFilters
                val catFilters = bundle.getSerializable(CATEGORIES_FILTERS) as? ActionSectionFilters

                locFilters?.let {
                    this.currentFilters = it
                }
                catFilters?.let {
                    this.currentSectionsFilters = it
                }
                updateFilters()
            }
        }
        else {
            parentFragmentManager.setFragmentResultListener(FILTERS2,this) { _, bundle ->
                val locFilters = bundle.getSerializable(LOCATION_FILTERS) as? EventActionLocationFilters
                val catFilters = bundle.getSerializable(CATEGORIES_FILTERS) as? ActionSectionFilters

                locFilters?.let {
                    this.currentFilters = it
                }
                catFilters?.let {
                    this.currentSectionsFilters = it
                }
                updateFilters()
            }
        }
    }

    private fun initializeEmptyState() {
        binding.title.text = if (isContrib) getString(R.string.action_contrib_empty_state_title) else getString(R.string.action_demand_empty_state_title)
        binding.subtitle.text = if (isContrib) getString(R.string.action_contrib_empty_state_desc) else getString(R.string.action_demand_empty_state_desc)
    }

    private fun handleResponseGetDemands(allDemands: MutableList<Action>?) {
        binding.progressBar.visibility = View.GONE

        if (isFromFilters) {
            allActions.clear()
        }

        allDemands?.let { allActions.addAll(it) }
        updateView(allActions.isEmpty())
        actionAdapter.notifyDataSetChanged()

        if (isFromFilters) {
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
            adapter = actionAdapter
        }
    }

    private fun updateFilters() {
        isFromFilters = true
        page = 0
        loadActions()
    }

    private fun loadActions() {
        binding.swipeRefresh.isRefreshing = false
        page++
        if(isContrib) {
            actionsPresenter.getAllContribs(page, EVENTS_PER_PAGE, currentFilters.travel_distance(),
                currentFilters.latitude(),currentFilters.longitude(),
                currentSectionsFilters.getSectionsForWS())
        }
        else {
            actionsPresenter.getAllDemands(page, EVENTS_PER_PAGE, currentFilters.travel_distance(),
                currentFilters.latitude(),currentFilters.longitude(),
                currentSectionsFilters.getSectionsForWS())
        }
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.progressBar.visibility = View.VISIBLE
            actionAdapter.notifyDataSetChanged()

            allActions.clear()
            actionAdapter.notifyDataSetChanged()
            actionsPresenter.getAllActions.value?.clear()
            actionsPresenter.isLastPage = false
            page = 0
            loadActions()
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
            if (!actionsPresenter.isLoading && !actionsPresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= EVENTS_PER_PAGE) {
                    loadActions()
                }
            }
        }
    }

    companion object {
        const val TAG = "ActionListFragment"
        const val EVENTS_PER_PAGE = 10
        const val IS_CONTRIB = "isContrib"
        fun newInstance(isContrib: Boolean): ActionListFragment {
            val fragment = ActionListFragment()
            val args = Bundle()
            args.putBoolean(IS_CONTRIB,isContrib)
            fragment.arguments = args
            return fragment
        }
    }
}