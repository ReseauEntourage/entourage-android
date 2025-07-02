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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputLayout
import social.entourage.android.R
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.GroupMember
import social.entourage.android.api.model.User
import social.entourage.android.api.model.toGroupMembers
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.databinding.NewFragmentMembersDiscussionBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.groups.details.members.OnItemShowListener
import social.entourage.android.small_talks.SmallTalkViewModel
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import timber.log.Timber

class MembersConversationFragment : BaseDialogFragment() {

    private var _binding: NewFragmentMembersDiscussionBinding? = null
    private val binding get() = _binding!!

    private val discussionsPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private val smallTalkViewModel: SmallTalkViewModel by viewModels()

    private var userCreatorId: Int? = null
    private var conversationId: Int? = Const.DEFAULT_VALUE

    private val membersList = mutableListOf<GroupMember>()
    private val searchMembersList = mutableListOf<GroupMember>()

    private lateinit var membersAdapter: MembersConversationListAdapter
    private lateinit var searchAdapter: MembersConversationListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            conversationId = it.getInt(ARG_CONVID).takeIf { id -> id != -1 }
                ?: DetailConversationActivity.smallTalkId.toInt()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = NewFragmentMembersDiscussionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setupAdapters()
        setupObservers()
        setupListeners()
        loadData()
    }

    private fun initUI() {
        binding.searchBarLayout.endIconMode = TextInputLayout.END_ICON_NONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun setupAdapters() {
        membersAdapter = MembersConversationListAdapter(membersList, userCreatorId, object : OnItemShowListener {
            override fun onShowConversation(userId: Int) {
                discussionsPresenter.createOrGetConversation(userId.toString())
            }
        })

        searchAdapter = MembersConversationListAdapter(searchMembersList, userCreatorId, object : OnItemShowListener {
            override fun onShowConversation(userId: Int) {
                discussionsPresenter.createOrGetConversation(userId.toString())
            }
        })

        val divider = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL).apply {
            ContextCompat.getDrawable(requireContext(), R.drawable.new_divider)?.let { setDrawable(it) }
        }

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = membersAdapter
            addItemDecoration(divider)
        }

        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = searchAdapter
            addItemDecoration(divider)
        }
    }

    private fun setupObservers() {
        if (DetailConversationActivity.isSmallTalkMode) {
            smallTalkViewModel.participants.observe(viewLifecycleOwner, ::handleSmallTalkUsers)
        } else {
            discussionsPresenter.detailConversation.observe(viewLifecycleOwner, ::handleConversation)
            discussionsPresenter.conversationUsers.observe(viewLifecycleOwner, ::handleConversationUsers)
            discussionsPresenter.getMembersSearch.observe(viewLifecycleOwner, ::handleSearchResults)
            discussionsPresenter.newConversation.observe(requireActivity(), ::openConversation)
        }
    }

    private fun setupListeners() {
        binding.header.headerIconBack.setOnClickListener { dismiss() }

        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(requireActivity())
                updateVisibilityForSearch()
                conversationId?.let {
                    discussionsPresenter.getMembersSearch(binding.searchBar.text.toString())
                }
                true
            } else false
        }

        binding.searchBar.setOnFocusChangeListener { _, _ ->
            binding.recyclerView.visibility = View.GONE
            binding.searchRecyclerView.visibility = View.VISIBLE
        }

        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.searchBarLayout.endIconMode =
                    if (s.isNullOrEmpty()) TextInputLayout.END_ICON_NONE
                    else TextInputLayout.END_ICON_CUSTOM
                binding.searchBarLayout.setEndIconOnClickListener {
                    clearSearch()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadData() {
        if (DetailConversationActivity.isSmallTalkMode) {
            membersList.clear()
            updateView(membersList.isEmpty())
            membersAdapter.updateCreatorId(userCreatorId)
            conversationId?.let { smallTalkViewModel.listSmallTalkParticipants(it.toString()) }
        } else {
            conversationId?.let { discussionsPresenter.getDetailConversation(it) }
        }
    }

    // ---------- HANDLERS ----------

    private fun handleSmallTalkUsers(users: List<User>) {
        membersList.clear()
        membersList.addAll(users.toGroupMembers())
        binding.progressBar.visibility = View.GONE
        updateView(membersList.isEmpty())
        membersAdapter.notifyDataSetChanged()
    }

    private fun handleConversation(conversation: Conversation?) {
        membersList.clear()
        conversation?.members?.let { membersList.addAll(it) }
        userCreatorId = conversation?.author?.id
        membersAdapter.updateCreatorId(userCreatorId)
        updateView(membersList.isEmpty())
        binding.progressBar.visibility = View.GONE
        conversation?.id?.let { discussionsPresenter.fetchUsersForConversation(it) }
    }

    private fun handleConversationUsers(users: MutableList<GroupMember>?) {
        Timber.d("handleGetUsers")
        membersList.clear()
        users?.let { membersList.addAll(it) }
        updateView(membersList.isEmpty())
        membersAdapter.updateCreatorId(userCreatorId)
        binding.progressBar.visibility = View.GONE
        membersAdapter.notifyDataSetChanged()
    }

    private fun handleSearchResults(results: MutableList<GroupMember>?) {
        searchMembersList.clear()
        results?.let { searchMembersList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        updateViewSearch(searchMembersList.isEmpty())
        searchAdapter.notifyDataSetChanged()
    }

    private fun openConversation(conversation: Conversation?) {
        conversation?.let {
            DetailConversationActivity.isSmallTalkMode = false
            startActivity(
                Intent(context, DetailConversationActivity::class.java).putExtras(
                    Bundle().apply {
                        putInt(Const.ID, it.id!!)
                        putInt(Const.POST_AUTHOR_ID, it.user?.id ?: 0)
                        putBoolean(Const.SHOULD_OPEN_KEYBOARD, false)
                        putString(Const.NAME, it.title)
                        putBoolean(Const.IS_CONVERSATION_1TO1, true)
                        putBoolean(Const.IS_MEMBER, true)
                        putBoolean(Const.IS_CONVERSATION, true)
                        putBoolean(Const.HAS_TO_SHOW_MESSAGE, it.hasToShowFirstMessage())
                    }
                )
            )
        }
    }

    // ---------- VIEW HELPERS ----------

    private fun updateView(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.title.text = getString(R.string.discussion_settings_search_empty)
    }

    private fun updateViewSearch(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.searchRecyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.recyclerView.visibility = View.GONE
        binding.title.text = getString(R.string.group_list_search_empty_state_title)
    }

    private fun updateVisibilityForSearch() {
        binding.searchRecyclerView.visibility = View.GONE
        binding.recyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun clearSearch() {
        binding.searchBar.text?.clear()
        binding.searchRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        updateView(membersList.isEmpty())
        Utils.hideKeyboard(requireActivity())
    }

    // ---------- COMPANION ----------

    companion object {
        private const val ARG_CONVID = "conversationid"
        const val TAG = "MembersConversationFragment"

        fun newInstance(conversationId: Int?): MembersConversationFragment {
            return MembersConversationFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_CONVID, conversationId ?: 0)
                }
            }
        }
    }
}
