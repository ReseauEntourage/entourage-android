package social.entourage.android.new_v8.groups.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.databinding.NewFragmentGroupsListBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.models.Group

class GroupsListFragment : Fragment() {

    private var _binding: NewFragmentGroupsListBinding? = null
    val binding: NewFragmentGroupsListBinding get() = _binding!!
    private var groupsList: MutableList<Group> = ArrayList()
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        groupPresenter.getAllGroups()
        groupPresenter.getAllGroups.observe(viewLifecycleOwner, ::handleResponseGetGroups)
        initializeGroups()
    }

    private fun handleResponseGetGroups(allGroups: MutableList<Group>?) {
        groupsList.clear()
        allGroups?.let { groupsList.addAll(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentGroupsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initializeGroups() {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GroupsListAdapter(groupsList, object : OnItemCheckListener {
                override fun onItemCheck(item: Group) {
                }

                override fun onItemUncheck(item: Group) {
                }
            })
        }
    }

}