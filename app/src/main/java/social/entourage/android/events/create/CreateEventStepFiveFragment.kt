package social.entourage.android.events.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.NewFragmentCreateEventStepFiveBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.list.groupPerPage
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.log.AnalyticsEvents

class CreateEventStepFiveFragment : Fragment() {
    private var _binding: NewFragmentCreateEventStepFiveBinding? = null
    val binding: NewFragmentCreateEventStepFiveBinding get() = _binding!!
    private var groupsList: MutableList<Group> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var selectedGroupsIdList: MutableList<Int> = mutableListOf()
    private var myId: Int? = null
    private var page: Int = 0
    private var groupID: Int? = null

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
        groupID = activity?.intent?.getIntExtra(Const.GROUP_ID, Const.DEFAULT_VALUE)
        setShareSelection()
        initializeGroups()
        loadGroups()
        setView()
        adjustTextViewsForRTL(binding.layout.root)
        groupPresenter.getAllMyGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        groupID?.let {
            if (groupID != Const.DEFAULT_VALUE) {
                selectedGroupsIdList.add(it)
                CommunicationHandler.event.neighborhoodIds.add(it)
            }
        }

        if (CommunicationHandler.eventEdited == null) {
            AnalyticsEvents.logEvent(AnalyticsEvents.Event_create_5)
        }
    }

    private fun adjustTextViewsForRTL(view: View) {
        val isRTL = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

        if (isRTL) {
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    val child = view.getChildAt(i)
                    adjustTextViewsForRTL(child) // Récursion pour parcourir toutes les sous-vues
                }
            } else if (view is TextView) {
                // Ajuster la gravité et la direction du texte pour RTL
                view.gravity = View.TEXT_ALIGNMENT_VIEW_END
                view.textDirection = View.TEXT_DIRECTION_RTL
            }
        }
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        allGroups?.let { groupsList.addAll(it) }
        groupsList.forEach {
            if (it.id == groupID) it.isSelected = true
            if (CommunicationHandler.eventEdited?.neighborhoods?.map { group -> group.id }
                    ?.contains(it.id) == true) it.isSelected = true
        }
        binding.layout.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun setShareSelection() {
        if (groupID != Const.DEFAULT_VALUE) {
            binding.layout.shareInGroups.isChecked = true
            CommunicationHandler.isButtonClickable.value = true
            binding.layout.recyclerView.isVisible = true
        }
        binding.layout.rbShareInGroups.setOnCheckedChangeListener { _, checkedId ->
            binding.layout.recyclerView.isVisible = checkedId == R.id.share_in_groups

            CommunicationHandler.isButtonClickable.value =
                (checkedId == R.id.dont_share) || (checkedId == R.id.share_in_groups && selectedGroupsIdList.isNotEmpty())

            if (checkedId == R.id.dont_share) {
                CommunicationHandler.event.neighborhoodIds.clear()
                selectedGroupsIdList.clear()
            }
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
            if (binding.layout.shareInGroups.isChecked) {
                CommunicationHandler.event.neighborhoodIds?.clear()
                CommunicationHandler.event.neighborhoodIds?.addAll(selectedGroupsIdList)
            }
            else {
                CommunicationHandler.event.neighborhoodIds?.clear()
            }

            if (binding.layout.shareInGroups.isChecked && selectedGroupsIdList.isEmpty()) {
                CommunicationHandler.isCondition.value = false
                binding.layout.error.root.visibility = View.VISIBLE
                binding.layout.error.errorMessage.text =
                    getString(R.string.error_mandatory_fields)
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

    private fun setView() {
        with(CommunicationHandler.eventEdited) {
            this?.let {
                if (this.neighborhoods?.isEmpty() == true) binding.layout.dontShare.isChecked = true
                else {
                    binding.layout.shareInGroups.isChecked = true
                    CommunicationHandler.eventEdited?.neighborhoods?.map { group -> group.id }
                        ?.toMutableList()?.forEach {
                            if (it != null) {
                                selectedGroupsIdList.add(it)
                            }
                        }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        CommunicationHandler.resetValues()
        CommunicationHandler.clickNext.observe(viewLifecycleOwner, ::handleOnClickNext)
        CommunicationHandler.isButtonClickable.value = groupsHaveBeenSelected()
    }
}