package social.entourage.android.new_v8.actions.list.me

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.new_fragment_action_list.view.*
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentActionsMyListBinding
import social.entourage.android.new_v8.actions.ActionsPresenter
import social.entourage.android.new_v8.actions.detail.ActionDetailActivity
import social.entourage.android.new_v8.models.Action
import social.entourage.android.new_v8.utils.Const


class MyActionsListFragment : Fragment() {
    val EVENTS_PER_PAGE = 10

    private var _binding: NewFragmentActionsMyListBinding? = null
    val binding: NewFragmentActionsMyListBinding get() = _binding!!

    private val myActionsPresenter: ActionsPresenter by lazy { ActionsPresenter() }
    lateinit var actionAdapter: MyActionsListAdapter
    private var page: Int = 0
    private var allActions:MutableList<Action>  = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentActionsMyListBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        actionAdapter = MyActionsListAdapter(allActions, object : OnItemClick {
            override fun onItemClick(action: Action) {
                showDetail(action)
            }
        })

        initializeEmptyState()
        loadActions()
        myActionsPresenter.getAllActions.observe(viewLifecycleOwner, ::handleResponseGetDemands)
        setupViews()

        loadActions()
    }

    private fun initializeEmptyState() {
        binding.emptyStateLayout.title.text =  getString(R.string.action_my_empty_title)
        binding.emptyStateLayout.subtitle.text = getString(R.string.action_my_empty_subtitle)
    }

    private fun handleResponseGetDemands(allDemands: MutableList<Action>?) {
        binding.progressBar.visibility = View.GONE

        allDemands?.let { allActions.addAll(it) }

        updateView(allActions.isEmpty())
        actionAdapter.notifyDataSetChanged()
    }

    private fun updateView(isListEmpty: Boolean) {
        binding.emptyStateLayout.isVisible = isListEmpty
        binding.recyclerView.isVisible = !isListEmpty
    }

    private fun setupViews() {
        binding.recyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = actionAdapter
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
            if (!myActionsPresenter.isLoading && !myActionsPresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= EVENTS_PER_PAGE) {
                    loadActions()
                }
            }
        }
    }

    private fun loadActions() {
        page++
        myActionsPresenter.getMyActions(page, EVENTS_PER_PAGE)
    }

    private fun showDetail(action: Action?) {
        if (action == null) return

        val intent = Intent(context, ActionDetailActivity::class.java)
            .putExtra(Const.ACTION_ID, action.id)
            .putExtra(Const.ACTION_TITLE,action.title)
            .putExtra(Const.IS_ACTION_DEMAND,!action.isContrib())
        startActivity(intent)
    }
}