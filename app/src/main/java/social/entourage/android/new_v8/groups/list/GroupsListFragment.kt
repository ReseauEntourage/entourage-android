package social.entourage.android.new_v8.groups.list

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentGroupsListBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Group
import timber.log.Timber


const val groupPerPage = 10

class GroupsListFragment : Fragment() {

    private var _binding: NewFragmentGroupsListBinding? = null
    val binding: NewFragmentGroupsListBinding get() = _binding!!
    private var groupsList: MutableList<Group> = ArrayList()
    private var groupsListSearch: MutableList<Group> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var page: Int = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGroups()
        groupPresenter.getAllGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        initializeGroups()
        initializeSearchGroups()
        handleEnterButton()
        handleSearchOnFocus()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        //groupsList.clear()
        allGroups?.let { groupsList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allGroups?.isEmpty()?.let { updateView(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }


    private fun updateView(isListEmpty: Boolean) {
        if (isListEmpty) binding.emptyStateLayout.visibility = View.VISIBLE
        else binding.list.visibility = View.VISIBLE
    }


    private fun initializeGroups() {
        binding.recyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = GroupsListAdapter(groupsList, object : OnItemCheckListener {
                override fun onItemCheck(item: Group) {
                }

                override fun onItemUncheck(item: Group) {
                }
            })
        }
    }

    private fun initializeSearchGroups() {
        binding.searchRecyclerView.apply {
            // Pagination
            addOnScrollListener(recyclerViewOnScrollListener)
            layoutManager = LinearLayoutManager(context)
            adapter = GroupsListAdapter(groupsListSearch, object : OnItemCheckListener {
                override fun onItemCheck(item: Group) {
                }

                override fun onItemUncheck(item: Group) {
                }
            })
        }
    }

    private fun loadGroups() {
        page += 1
        groupPresenter.getAllGroups(page, groupPerPage)
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

    private fun handleEnterButton() {
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Timber.e("button handle")
                handled = true
            }
            handled
        }
    }

    private fun handleSearchOnFocus() {
        binding.searchBar.setOnFocusChangeListener { _, _ ->
            binding.recyclerView.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun handleSearchOnTextListener() {
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable) {}
        })
    }
}