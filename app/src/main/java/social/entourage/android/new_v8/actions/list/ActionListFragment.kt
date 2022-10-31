package social.entourage.android.new_v8.actions.list

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_fragment_action_list.view.*
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentActionListBinding
import social.entourage.android.new_v8.actions.*
import social.entourage.android.new_v8.models.*

const val EVENTS_PER_PAGE = 10
const val IS_CONTRIB = "isContrib"

class ActionListFragment : Fragment() {

    private var _binding: NewFragmentActionListBinding? = null
    val binding: NewFragmentActionListBinding get() = _binding!!

    private val actionsPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    private var myId: Int? = null
    lateinit var actionAdapter: ActionsListAdapter
    private var page: Int = 0

    private var currentFilters = EventActionLocationFilters()
    private var currentSectionsFilters = ActionSectionFilters()

    private var activityResultLauncher:ActivityResultLauncher<Intent>? = null

    private var isFromFilters = false
    private var isContrib = true

    private var allActions:MutableList<Action>  = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentActionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isContrib = arguments?.getBoolean(IS_CONTRIB,false) ?: false

        myId = EntourageApplication.me(activity)?.id
        actionAdapter =
            ActionsListAdapter(allActions,myId, isContrib)
        initializeEmptyState()
        loadActions()
        actionsPresenter.getAllActions.observe(viewLifecycleOwner, ::handleResponseGetDemands)
        initializeEvents()
        handleSwipeRefresh()
        addParentFragmentListener()
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
        binding.emptyStateLayout.title.text = if (isContrib) getString(R.string.action_contrib_empty_state_title) else getString(R.string.action_demand_empty_state_title)
        binding.emptyStateLayout.subtitle.text = if (isContrib) getString(R.string.action_contrib_empty_state_desc) else getString(R.string.action_demand_empty_state_desc)
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
        fun newInstance(isContrib: Boolean): ActionListFragment {
            val fragment = ActionListFragment()
            val args = Bundle()
            args.putBoolean(IS_CONTRIB,isContrib)
            fragment.arguments = args
            return fragment
        }
    }
}