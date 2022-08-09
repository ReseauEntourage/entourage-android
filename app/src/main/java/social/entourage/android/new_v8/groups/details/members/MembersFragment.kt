package social.entourage.android.new_v8.groups.details.members

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.databinding.NewFragmentMembersBinding
import social.entourage.android.new_v8.events.EventsPresenter
import social.entourage.android.new_v8.groups.GroupPresenter
import social.entourage.android.new_v8.utils.Const
import social.entourage.android.new_v8.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber


enum class MembersType(val code: Int) {
    GROUP(0),
    EVENT(1),
}

open class MembersFragment : Fragment() {

    private var _binding: NewFragmentMembersBinding? = null
    val binding: NewFragmentMembersBinding get() = _binding!!
    private var membersList: MutableList<EntourageUser> = mutableListOf()
    private var membersListSearch: MutableList<EntourageUser> = ArrayList()
    private var id: Int? = Const.DEFAULT_VALUE
    private var type: MembersType? = MembersType.GROUP
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    private val navArgs: MembersFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMembersBinding.inflate(inflater, container, false)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_MEMBER_SHOW_LIST
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getArgs()
        getMembers()
        handleCloseButton()
        initializeMembers()
        initializeMembersSearch()
        handleEnterButton()
        handleSearchOnFocus()
        handleCross()
        handleCrossButton()
        binding.searchBarLayout.endIconMode = TextInputLayout.END_ICON_NONE
    }

    private fun getMembers() {
        if (type == MembersType.GROUP) {
            id?.let {
                groupPresenter.getGroupMembers(it)
            }
            groupPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetMembers)
            groupPresenter.getMembersSearch.observe(
                viewLifecycleOwner,
                ::handleResponseGetMembersSearch
            )
        } else if (type == MembersType.EVENT) {
            id?.let {
                eventPresenter.getEventMembers(it)
            }
            eventPresenter.getMembers.observe(viewLifecycleOwner, ::handleResponseGetMembers)
            eventPresenter.getMembersSearch.observe(
                viewLifecycleOwner,
                ::handleResponseGetMembersSearch
            )
        }
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
            findNavController().popBackStack()
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

    private fun getArgs() {
        id = navArgs.id
        type = navArgs.type
    }


    private fun handleEnterButton() {
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_MEMBER_SEARCH_VALIDATE
            )
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(requireActivity())
                binding.searchRecyclerView.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                Utils.hideKeyboard(requireActivity())
                id?.let {
                    if (type == MembersType.GROUP)
                        groupPresenter.getGroupMembersSearch(
                            binding.searchBar.text.toString()
                        )
                    else if (type == MembersType.EVENT)
                        eventPresenter.getEventMembersSearch(
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
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_MEMBER_SEARCH_START
            )
            binding.recyclerView.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
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
                    binding.searchBarLayout.endIconMode = TextInputLayout.END_ICON_NONE

                } else {
                    binding.searchBarLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
                    handleCross()
                }
            }

        })
    }

    private fun handleCross() {
        binding.searchBarLayout.setEndIconOnClickListener {
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_MEMBER_SEARCH_DELETE
            )
            binding.searchBar.text?.clear()
            binding.searchRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.GONE
            updateView(membersList.isEmpty())
            Utils.hideKeyboard(requireActivity())
        }
    }
}