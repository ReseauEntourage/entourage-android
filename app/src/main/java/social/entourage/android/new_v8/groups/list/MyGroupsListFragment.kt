package social.entourage.android.new_v8.groups.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import social.entourage.android.EntourageApplication
import social.entourage.android.databinding.NewFragmentMyGroupsListBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Group


class MyGroupsListFragment : Fragment() {


    private var _binding: NewFragmentMyGroupsListBinding? = null
    val binding: NewFragmentMyGroupsListBinding get() = _binding!!
    private var page: Int = 0

    private var groupsList: MutableList<Group> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private var myId: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMyGroupsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        myId = EntourageApplication.me(activity)?.id
        loadGroups()
        groupPresenter.getAllMyGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        initializeGroups()
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
        else binding.recyclerView.visibility = View.VISIBLE
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
            }, myId)
        }
    }

    private fun loadGroups() {
        page++
        myId?.let { groupPresenter.getMyGroups(page, groupPerPage, it) } ?: run {
            binding.progressBar.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.VISIBLE
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
}