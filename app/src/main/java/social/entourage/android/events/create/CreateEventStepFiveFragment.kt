package social.entourage.android.events.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.FragmentCreateEventStepFiveBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.list.groupPerPage
import social.entourage.android.tools.log.AnalyticsEvents

/**
 * Étape 5 : partage de l'événement dans zéro, un ou plusieurs groupes de l'utilisateur.
 * Les groupes cochés vivent dans le ViewModel (`form.neighborhoodIds`).
 */
class CreateEventStepFiveFragment : Fragment() {

    private var _binding: FragmentCreateEventStepFiveBinding? = null
    val binding: FragmentCreateEventStepFiveBinding get() = _binding!!

    private val viewModel: CreateEventViewModel by activityViewModels()

    private var groupsList: MutableList<Group> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var myId: Int? = null
    private var page: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateEventStepFiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myId = EntourageApplication.me(activity)?.id
        initializeGroups()
        groupPresenter.getAllMyGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        loadGroups()
        if (!viewModel.isEdition) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_5)
        }
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let { groupsList.addAll(it) }
        groupsList.forEach { it.isSelected = viewModel.form.neighborhoodIds.contains(it.id) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun loadGroups() {
        page++
        myId?.let { groupPresenter.getMyGroups(page, groupPerPage, it) }
    }

    private fun initializeGroups() {
        val groupsListAdapter =
            ChooseGroupEventListAdapter(groupsList, object : OnItemCheckListener {
                override fun onItemCheck(item: Group) {
                    item.id?.let {
                        if (!viewModel.form.neighborhoodIds.contains(it)) viewModel.form.neighborhoodIds.add(it)
                        viewModel.onFormChanged()
                    }
                }

                override fun onItemUncheck(item: Group) {
                    viewModel.form.neighborhoodIds.remove(item.id)
                    viewModel.onFormChanged()
                }
            })

        binding.recyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = groupsListAdapter
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
            if (!groupPresenter.isLoading && !groupPresenter.isLastPage) {
                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0 && totalItemCount >= groupPerPage) {
                    loadGroups()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
