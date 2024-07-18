package social.entourage.android.actions.list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.actions.ActionsPresenter
import social.entourage.android.api.model.Action
import social.entourage.android.api.model.EventActionLocationFilters
import social.entourage.android.databinding.FragmentActionListBinding
import social.entourage.android.main_filter.MainFilterActivity

class ActionListFragment : Fragment() {

    private var _binding: FragmentActionListBinding? = null
    private val binding: FragmentActionListBinding get() = _binding!!

    private lateinit var actionsPresenter: ActionsPresenter
    private var myId: Int? = null

    private lateinit var actionAdapter: ActionsListAdapter
    private var currentFilters = EventActionLocationFilters()

    // Variables
    private var page: Int = 0

    private var isContrib = false
    private var isSearching = false

    private var demandeActions: MutableList<Action> = mutableListOf()

    private var currentSectionsFilters = MainFilterActivity.savedActionInterests
    var query = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActionListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionsPresenter = ViewModelProvider(requireActivity()).get(ActionsPresenter::class.java)
        myId = EntourageApplication.me(activity)?.id
        initializeAdapter()
        initializeRecyclerView()
        initializeSwipeRefresh()
        observeQuery()
        observeActions()
    }

    override fun onResume() {
        super.onResume()
        isContrib = actionsPresenter.isContrib
        reloadActions()
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun initializeSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            reloadActions()
        }
    }

    fun reloadActions() {
        page = 0
        demandeActions.clear()
        actionAdapter.notifyDataSetChanged()
        actionsPresenter.isLastPage = false
        loadAction()
        Log.d("ActionListFragment", "reloadActions")
    }

    fun loadAction() {
        if (actionsPresenter.isLoading || actionsPresenter.isLastPage) return
        actionsPresenter.isLoading = true

        page++
        if (isSearching) {
            if (isContrib) {
                Log.d("ActionListFragment", "isSearching contrib")
                actionsPresenter.getAllContribsWithSearchQuery(query, page, EVENTS_PER_PAGE)
            } else {
                Log.d("ActionListFragment", "isSearching demand")
                actionsPresenter.getAllDemandsWithSearchQuery(query, page, EVENTS_PER_PAGE)
            }
        } else if (MainFilterActivity.hasFilter) {
            val sections = MainFilterActivity.savedActionInterests.joinToString(",")
            if (isContrib) {
                Log.d("ActionListFragment", "sections contrib")
                actionsPresenter.getAllContribsWithFilter(page, EVENTS_PER_PAGE, MainFilterActivity.savedRadius, MainFilterActivity.savedLocation?.lat, MainFilterActivity.savedLocation?.lng, sections)
            } else {
                Log.d("ActionListFragment", "sections demand")
                actionsPresenter.getAllDemandsWithFilter(page, EVENTS_PER_PAGE, MainFilterActivity.savedRadius, MainFilterActivity.savedLocation?.lat, MainFilterActivity.savedLocation?.lng, sections)
            }
        } else {
            if (isContrib) {
                Log.d("ActionListFragment", "all contrib")
                actionsPresenter.getAllContribs(page, EVENTS_PER_PAGE, currentFilters.travel_distance(), currentFilters.latitude(), currentFilters.longitude(), null)
            } else {
                Log.d("ActionListFragment", "all demand")
                actionsPresenter.getAllDemands(page, EVENTS_PER_PAGE, currentFilters.travel_distance(), currentFilters.latitude(), currentFilters.longitude(), null)
            }
        }
    }

    private fun initializeAdapter() {
        actionAdapter = ActionsListAdapter(myId, isContrib, requireContext())
    }

    private fun initializeRecyclerView() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = actionAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val totalItemCount = layoutManager.itemCount
                    val visibleItemCount = layoutManager.childCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (!recyclerView.canScrollVertically(1) && !actionsPresenter.isLoading && !actionsPresenter.isLastPage) {
                        loadAction()
                    }
                }
            })
        }
    }

    private fun observeQuery() {
        actionsPresenter.searchQuery.observe(viewLifecycleOwner, { query ->
            handleSearchQuery(query)
        })
    }

    private fun observeActions() {
        actionsPresenter.getAllActions.observe(viewLifecycleOwner, { actions ->
            handleActions(actions)
        })
    }

    private fun handleSearchQuery(query: String) {
        isSearching = query.isNotEmpty()
        this.query = query
        this.reloadActions()
    }

    private fun handleActions(actions: List<Action>) {
        actionsPresenter.isLoading = false
        if (actions.isEmpty() && page == 1) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
        this.demandeActions.addAll(actions)
        actionAdapter.resetData(demandeActions, isContrib)
        binding.progressBar.visibility = View.GONE
    }

    companion object {
        const val EVENTS_PER_PAGE: Int = 20
        const val IS_CONTRIB = "isContrib"
        fun newInstance(isContrib: Boolean, ismine: Boolean): ActionListFragment {
            val fragment = ActionListFragment()
            val args = Bundle()
            args.putBoolean(IS_CONTRIB, isContrib)
            fragment.arguments = args
            return fragment
        }
    }
}
