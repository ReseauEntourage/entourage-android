package social.entourage.android.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import social.entourage.android.R
import social.entourage.android.api.model.Group
import social.entourage.android.databinding.BottomSheetNationalGroupsBinding
import social.entourage.android.groups.GroupPresenter

class NationalGroupsBottomSheet : BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetNationalGroupsBinding
    private lateinit var adapter: NationalGroupsAdapter
    private lateinit var groupPresenter: GroupPresenter
    private var groupsList: List<Group> = emptyList()
    var onGroupJoined: (() -> Unit)? = null
    var onDismissCallback: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetNationalGroupsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        groupPresenter = ViewModelProvider(this).get(GroupPresenter::class.java)
        setupRecyclerView()
        setupCloseButton()
        setupObservers()
    }

    private fun setupObservers() {
        groupPresenter.hasUserJoinedGroup.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                // Refresh the groups list to get updated member status
                onGroupJoined?.invoke()
            }
        }

        groupPresenter.hasUserLeftGroup.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                // Refresh the groups list to get updated member status
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NationalGroupsAdapter(groupsList) { group, position ->
            onJoinButtonClick(group, position)
        }
        binding.groupsRecyclerview.layoutManager = LinearLayoutManager(requireContext())
        binding.groupsRecyclerview.adapter = adapter
    }

    private fun setupCloseButton() {
        binding.closeButton.setOnClickListener {
            dismiss()
        }
    }

    private fun onJoinButtonClick(group: Group, position: Int) {
        if (group.member) {
            // User is already a member, leave the group
            group.id?.let { groupId ->
                groupPresenter.leaveGroup(groupId)
            }
        } else {
            // User is not a member, join the group
            group.id?.let { groupId ->
                groupPresenter.joinGroup(groupId)
            }
        }
    }

    fun setGroups(groups: List<Group>) {
        this.groupsList = groups
        if (::adapter.isInitialized) {
            adapter = NationalGroupsAdapter(groupsList) { group, position ->
                onJoinButtonClick(group, position)
            }
            binding.groupsRecyclerview.adapter = adapter
        }
    }

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        onDismissCallback?.invoke()
    }

    companion object {
        const val TAG = "NationalGroupsBottomSheet"
    }
}