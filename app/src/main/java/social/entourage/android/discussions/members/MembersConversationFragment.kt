package social.entourage.android.discussions.members

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import social.entourage.android.R
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.databinding.NewFragmentMembersDiscussionBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.groups.details.members.OnItemShowListener
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.GroupMember
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import timber.log.Timber

class MembersConversationFragment : BaseDialogFragment() {

    private var _binding: NewFragmentMembersDiscussionBinding? = null
    val binding: NewFragmentMembersDiscussionBinding get() = _binding!!
    private var membersList: MutableList<GroupMember> = mutableListOf()
    private var membersListSearch: MutableList<GroupMember> = ArrayList()
    private var id: Int? = Const.DEFAULT_VALUE
    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    private var userCreatorId:Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            id = it.getInt(ARG_CONVID)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NewFragmentMembersDiscussionBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getMembers()
        handleCloseButton()
        initializeMembers()
        initializeMembersSearch()
        handleEnterButton()
        handleSearchOnFocus()
        handleCross()
        handleCrossButton()
        binding.searchBarLayout.endIconMode = TextInputLayout.END_ICON_NONE

        discussionsPresenter.newConversation.observe(requireActivity(), ::handleGetConversation)
        discussionsPresenter.conversationUsers.observe(requireActivity(), ::handleGetUsers)
    }

    private fun getMembers() {
        discussionsPresenter.detailConversation.observe(viewLifecycleOwner, ::handleResponseGetMembers)
        discussionsPresenter.getMembersSearch.observe(
            viewLifecycleOwner,
            ::handleResponseGetMembersSearch
        )
        id?.let {
            discussionsPresenter.getDetailConversation(it)
        }
    }

    private fun handleResponseGetMembers(conversation: Conversation?) {
        membersList.clear()
        conversation?.members?.let { membersList.addAll(it) }
        userCreatorId = conversation?.author?.id
        binding.progressBar.visibility = View.GONE

        updateView(membersList.isEmpty())

        (binding.recyclerView.adapter as? MembersConversationListAdapter)?.updateCreatorId(userCreatorId)
        id?.let { discussionsPresenter.fetchUsersForConversation(it) }

    }

    private fun handleResponseGetMembersSearch(allMembersSearch: MutableList<GroupMember>?) {
        membersListSearch.clear()
        allMembersSearch?.let { membersListSearch.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allMembersSearch?.isEmpty()?.let { updateViewSearch(it) }
        binding.searchRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun handleCloseButton() {
        binding.header.iconBack.setOnClickListener {
            dismiss()
        }
    }

    private fun handleGetConversation(conversation: Conversation?) {
        conversation?.let {
            startActivityForResult(
                Intent(context, DetailConversationActivity::class.java)
                    .putExtras(
                        bundleOf(
                            Const.ID to conversation.id,
                            Const.POST_AUTHOR_ID to conversation.user?.id,
                            Const.SHOULD_OPEN_KEYBOARD to false,
                            Const.NAME to conversation.title,
                            Const.IS_CONVERSATION_1TO1 to true,
                            Const.IS_MEMBER to true,
                            Const.IS_CONVERSATION to true,
                            Const.HAS_TO_SHOW_MESSAGE to conversation.hasToShowFirstMessage()
                        )
                    ), 0
            )
        }
    }

    private fun handleGetUsers(users: MutableList<GroupMember>?) {
        Timber.d("handleGetUsers")
        membersList.clear()
        users?.let { membersList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        (binding.recyclerView.adapter as? MembersConversationListAdapter)?.updateCreatorId(userCreatorId)
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun initializeMembers() {
        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.new_divider)
            ?.let { itemDecorator.setDrawable(it) }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MembersConversationListAdapter(membersList, userCreatorId, object : OnItemShowListener {
                override fun onShowConversation(userId: Int) {
                    discussionsPresenter.createOrGetConversation(userId.toString())
                }
            })
            addItemDecoration(itemDecorator)
        }
    }

    private fun initializeMembersSearch() {
        val itemDecorator = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(requireContext(), R.drawable.new_divider)
            ?.let { itemDecorator.setDrawable(it) }
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MembersConversationListAdapter(membersListSearch,userCreatorId, object : OnItemShowListener {
                override fun onShowConversation(userId: Int) {
                    discussionsPresenter.createOrGetConversation(userId.toString())
                }
            })
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
            binding.title.text = getString(R.string.discussion_settings_search_empty)
        } else {
            binding.recyclerView.visibility = View.VISIBLE
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
                id?.let {
                    discussionsPresenter.getMembersSearch(
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
            binding.searchBar.text?.clear()
            binding.searchRecyclerView.visibility = View.GONE
            binding.emptyStateLayout.visibility = View.GONE
            updateView(membersList.isEmpty())
            Utils.hideKeyboard(requireActivity())
        }
    }

    companion object {
        private const val ARG_CONVID = "conversationid"
        const val TAG = "MembersConversationFragment"
        fun newInstance(conversationId:Int?): MembersConversationFragment {
            val fragment = MembersConversationFragment()
            val args = Bundle()
            args.putInt(ARG_CONVID,conversationId ?: 0)
            fragment.arguments = args
            return fragment
        }
    }
}