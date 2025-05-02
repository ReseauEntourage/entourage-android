package social.entourage.android.groups.list

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout.END_ICON_CUSTOM
import com.google.android.material.textfield.TextInputLayout.END_ICON_NONE
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.NewFragmentGroupsListBinding
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents

const val groupPerPage = 10

class DiscoverGroupsListFragment : Fragment() , UpdateGroupInter {

    private var _binding: NewFragmentGroupsListBinding? = null
    val binding: NewFragmentGroupsListBinding get() = _binding!!
    private var groupsList: MutableList<Group> = ArrayList()
    private var groupsListSearch: MutableList<Group> = ArrayList()
    private lateinit var groupPresenter: GroupPresenter
    private var page: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupPresenter = ViewModelProvider(requireActivity()).get(GroupPresenter::class.java)
        loadGroups()
        groupPresenter.getAllGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        groupPresenter.getGroupsSearch.observe(viewLifecycleOwner, ::handleResponseGetGroupsSearch)
        initializeGroups()
        initializeSearchGroups()
        handleEnterButton()
        handleSearchOnFocus()
        handleCross()
        handleSwipeRefresh()
        handleCrossButton()
        setRVScrollListener()
        binding.searchBarLayout.endIconMode = END_ICON_NONE

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupsListBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_SHOW_DISCOVER
        )
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        groupsList.clear()
        groupsListSearch.clear()
        groupPresenter.isLastPage = false
        (binding.recyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsList)
        (binding.searchRecyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsListSearch)
        page = 0
        loadGroups()
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        //groupsList.clear()
        binding.progressBar.visibility = View.GONE
        allGroups?.let { groupsList.addAll(it)
        }
        binding.progressBar.visibility = View.GONE
        allGroups?.isEmpty()?.let { updateView(it) }
        (binding.recyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsList)
        groupPresenter.isLoading = false

    }

    private fun handleResponseGetGroupsSearch(allGroupsSearch: MutableList<Group>?) {
        groupsListSearch.clear()
        binding.progressBar.visibility = View.GONE
        allGroupsSearch?.let { groupsListSearch.addAll(it) }
        allGroupsSearch?.isEmpty()?.let {
            updateViewSearch(it) }
        (binding.searchRecyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsListSearch)

    }

    private fun updateView(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.title.text = getString(R.string.group_list_empty_state_title)
            binding.subtitle.text = getString(R.string.group_list_empty_state_subtitle)
            binding.arrow.visibility = View.VISIBLE
        } else {
            binding.list.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateViewSearch(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.title.text = getString(R.string.group_list_search_empty_state_title)
            binding.subtitle.text = getString(R.string.group_list_search_empty_state_subtitle)
            binding.arrow.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.list.visibility = View.VISIBLE

        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.list.visibility = View.VISIBLE
            binding.searchRecyclerView.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        }
    }

    private fun initializeGroups() {
        // Pagination
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = GroupsListAdapter(groupsList, null, FromScreen.DISCOVER, this)
        ( binding.recyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsList)


    }

    private fun initializeSearchGroups() {
        // Pagination
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = GroupsListAdapter(groupsListSearch, null, FromScreen.DISCOVER_SEARCH, this)
        ( binding.recyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsList)

    }

    private fun loadGroups() {
        groupPresenter.isLoading = true
        binding.swipeRefresh.isRefreshing = false
        page += 1
        groupPresenter.getAllGroups(page, groupPerPage)
    }

    private fun setRVScrollListener(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            binding.recyclerView.setOnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
                handlePagination(binding.recyclerView)

            }
        }
    }



    fun handlePagination(recyclerView: RecyclerView) {

        val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
        layoutManager?.let {
            val visibleItemCount: Int = layoutManager.childCount
            val totalItemCount: Int = layoutManager.itemCount
            val firstVisibleItemPosition: Int = layoutManager.findFirstCompletelyVisibleItemPosition()
            val lastVisibleItemPosition: Int = layoutManager.findLastVisibleItemPosition()
            val itemlist = groupsList.size - 5
            if(firstVisibleItemPosition > 0 ){
                binding.searchBarLayout.visibility = View.GONE
            }else{
                binding.searchBarLayout.visibility = View.VISIBLE
            }
            if (!groupPresenter.isLoading &&  !groupPresenter.isLastPage) {
                if (lastVisibleItemPosition >= groupsList.size - 5) {
                    binding.progressBar.visibility = View.VISIBLE
                    loadGroups()
                }
            }
        }

    }

    private fun handleEnterButton() {
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(requireActivity())
                binding.searchRecyclerView.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                Utils.hideKeyboard(requireActivity())
                groupPresenter.getGroupsSearch(binding.searchBar.text.toString())
                handled = true
                AnalyticsEvents.logEvent(
                    AnalyticsEvents.ACTION_GROUP_SEARCH_VALIDATE
                )
            }
            handled
        }
    }

    private fun handleCrossButton() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrEmpty()) {
                    binding.searchBarLayout.endIconMode = END_ICON_NONE

                } else {
                    binding.searchBarLayout.endIconMode = END_ICON_CUSTOM
                    handleCross()
                }
            }

        })
    }

    private fun handleSearchOnFocus() {
        binding.searchBar.setOnFocusChangeListener { _, _ ->
            binding.recyclerView.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_SEARCH_START
            )
        }
    }

    private fun handleSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            page = 0
            groupsList.clear()
            groupsListSearch.clear()
            groupPresenter.isLastPage = false
            (binding.recyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsList)
            (binding.searchRecyclerView.adapter as? GroupsListAdapter)?.updateGroupsList(groupsListSearch)
            loadGroups()
        }
    }

    private fun handleCross() {
        binding.searchBarLayout.setEndIconOnClickListener {
            binding.searchBar.text?.clear()
            binding.searchRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.GONE
            updateView(groupsList.isEmpty())
            Utils.hideKeyboard(requireActivity())
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_SEARCH_DELETE
            )
        }
    }

    override fun updateGroup() {

    }
}