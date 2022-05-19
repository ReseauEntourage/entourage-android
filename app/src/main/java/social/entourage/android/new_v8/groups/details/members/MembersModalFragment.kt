package social.entourage.android.new_v8.groups.details.members

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.databinding.NewFragmentMembersModalBinding
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.groups.details.rules.MembersListAdapter
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils


open class MembersModalFragment : DialogFragment() {

    private var _binding: NewFragmentMembersModalBinding? = null
    val binding: NewFragmentMembersModalBinding get() = _binding!!
    private var membersList: MutableList<EntourageUser> = mutableListOf()
    private var membersListSearch: MutableList<EntourageUser> = ArrayList()
    private var groupId: Int? = Const.DEFAULT_VALUE
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }

    private val navArgs: MembersModalFragmentArgs by navArgs()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_FRAME, R.style.FullScreenDialog)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.attributes?.windowAnimations = R.style.MyDialogAnimation
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMembersModalBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getGroupId()
        groupId?.let {
            groupPresenter.getGroupMembers(it)
        }
        groupPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetMembers)
        groupPresenter.getMembersSearch.observe(
            viewLifecycleOwner,
            ::handleResponseGetMembersSearch
        )
        handleCloseButton()
        initializeMembers()
        initializeMembersSearch()
        handleEnterButton()
        handleSearchOnFocus()
        handleCross()
        hideView()
    }

    private fun handleResponseGetMembers(allMembers: MutableList<EntourageUser>?) {
        membersList.clear()
        allMembers?.let { membersList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allMembers?.isEmpty()?.let { updateView(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun handleResponseGetMembersSearch(allMembersSearch: MutableList<EntourageUser>?) {
        membersListSearch.clear()
        allMembersSearch?.let { membersListSearch.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allMembersSearch?.isEmpty()?.let { updateViewSearch(it) }
        binding.searchRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            // findNavController().popBackStack()
            requireActivity().onBackPressed()
        }
    }

    private fun initializeMembers() {
        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.new_divider)
            ?.let { itemDecorator.setDrawable(it) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MembersListAdapter(membersList)
            addItemDecoration(itemDecorator)
        }
    }

    private fun initializeMembersSearch() {
        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.new_divider)
            ?.let { itemDecorator.setDrawable(it) }
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MembersListAdapter(membersListSearch)
            addItemDecoration(itemDecorator)
        }
    }


    private fun updateViewSearch(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.title.text = getString(R.string.group_list_search_empty_state_title)
            binding.recyclerView.visibility = View.GONE

        } else {
            binding.emptyStateLayout.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun updateView(isListEmpty: Boolean) {
        if (isListEmpty) {
            binding.emptyStateLayout.visibility = View.VISIBLE
            binding.title.text = getString(R.string.group_list_empty_state_title)
        } else {
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun getGroupId() {
        groupId = navArgs.groupID
    }

    private fun hideView() {
        binding.header.view.visibility = View.GONE
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
                groupId?.let {
                    groupPresenter.getGroupMembersSearch(
                        binding.searchBar.text.toString()
                    )
                }
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

    private fun handleCross() {
        binding.searchBarLayout.setEndIconOnClickListener {
            binding.searchBar.text?.clear()
            binding.searchRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.GONE
            updateView(membersList.isEmpty())
            Utils.hideKeyboard(requireActivity())
        }
    }
}