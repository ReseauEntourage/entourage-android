package social.entourage.android.new_v8.events.create

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
import social.entourage.android.databinding.NewFragmentCreateEventStepFiveBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.groups.list.groupPerPage
import social.entourage.android.new_v8.models.Group


class CreateEventStepFiveFragment : Fragment() {
    private var _binding: NewFragmentCreateEventStepFiveBinding? = null
    val binding: NewFragmentCreateEventStepFiveBinding get() = _binding!!
    private var groupsList: MutableList<Group> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var selectedGroupsIdList: MutableList<Int> = mutableListOf()
    private var myId: Int? = null
    private var page: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentCreateEventStepFiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myId = EntourageApplication.me(activity)?.id
        setShareSelection()
        initializeGroups()
        loadGroups()
        groupPresenter.getAllMyGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
    }


    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let { groupsList.addAll(it) }
        binding.layout.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun setShareSelection() {
        binding.layout.rbShareInGroups.setOnCheckedChangeListener { _, checkedId ->
            binding.layout.recyclerView.isVisible = checkedId == R.id.share_in_groups
            CommunicationHandler.isButtonClickable.value =
                (checkedId == R.id.dont_share) || (checkedId == R.id.share_in_groups && selectedGroupsIdList.isNotEmpty())
            if (checkedId == R.id.dont_share) CommunicationHandler.event.neighborhoodIds.clear()
            else CommunicationHandler.event.neighborhoodIds.addAll(selectedGroupsIdList)
        }
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
                        selectedGroupsIdList.add(it)
                        CommunicationHandler.isButtonClickable.value = groupsHaveBeenSelected()
                        CommunicationHandler.event.neighborhoodIds.add(it)
                    }
                }

                override fun onItemUncheck(item: Group) {
                    selectedGroupsIdList.remove(item.id)
                    CommunicationHandler.isButtonClickable.value = groupsHaveBeenSelected()
                    CommunicationHandler.event.neighborhoodIds.remove(item.id)
                }
            })

        binding.layout.recyclerView.apply {
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

    private fun handleOnClickNext(onClick: Boolean) {
        if (onClick) {
            if (selectedGroupsIdList.isEmpty()) {
                CommunicationHandler.isCondition.value = false
                binding.layout.error.root.visibility = View.VISIBLE
                binding.layout.error.errorMessage.text =
                    getString(R.string.error_categories_create_group_image)
            } else {
                CommunicationHandler.isCondition.value = true
                binding.layout.error.root.visibility = View.GONE
                CommunicationHandler.clickNext.removeObservers(viewLifecycleOwner)
            }
        }
    }

    fun groupsHaveBeenSelected(): Boolean {
        return selectedGroupsIdList.isNotEmpty()
    }


    override fun onResume() {
        super.onResume()
        CommunicationHandler.resetValues()
        CommunicationHandler.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        CommunicationHandler.isButtonClickable.value = groupsHaveBeenSelected()
    }
}