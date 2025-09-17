package social.entourage.android.members

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.CompleteReactionsResponse
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.ReactionType
import social.entourage.android.base.BaseActivity
import social.entourage.android.databinding.NewFragmentMembersBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.groups.details.members.MembersListAdapter
import social.entourage.android.groups.details.members.OnItemShowListener
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils

/**
 * Unique Activity affichant la liste des membres (ou participants) d’un
 * groupe / évènement avec :
 *  • Pagination inf : chargement par page à mesure du scroll (implémentée
 *    dans GroupPresenter / EventsPresenter).
 *  • Recherche locale + API.
 *  • Affichage des réacteurs (isFromReact).
 *  • Lancement d’une conversation 1‑to‑1.
 */
class MembersActivity : BaseActivity() {

    private lateinit var binding: NewFragmentMembersBinding

    // --- Données ---
    private val membersList: MutableList<EntourageUser> = mutableListOf()
    private val membersListSearch: MutableList<EntourageUser> = mutableListOf()
    private val reactionList: MutableList<ReactionType> = mutableListOf()

    private var id: Int = Const.DEFAULT_VALUE
    private var type: MembersType = MembersType.GROUP

    // --- Presenters ---
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }
    private val discussionPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }

    // --- Lifecycle ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewFragmentMembersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_GROUP_MEMBER_SHOW_LIST)

        // Params
        id = intent.getIntExtra("ID", Const.DEFAULT_VALUE)
        val typeCode = intent.getIntExtra("TYPE", MembersType.GROUP.code)
        type = MembersType.values().firstOrNull { it.code == typeCode } ?: MembersType.GROUP

        setupToolbar()
        setupLists()
        setupSearchBar()
        setupObservers()
        loadFirstPage()
    }

    override fun onResume() {
        super.onResume()
        updateHeaderTexts()
    }

    override fun onDestroy() {
        MembersActivity.isFromReact = false
        super.onDestroy()
    }

    // ---------------------------------------------------------------------
    // Initialisation UI
    // ---------------------------------------------------------------------

    private fun setupToolbar() {
        binding.iconBack.setOnClickListener { finish() }
    }

    private fun setupLists() {
        val ctx: Context = this
        val divider = DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
            ContextCompat.getDrawable(this@MembersActivity, R.drawable.new_divider)?.let { setDrawable(it) }
        }

        // Liste principale (avec pagination)
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = MembersListAdapter(ctx, membersList, reactionList, conversationCallback())
            addItemDecoration(divider)
            addOnScrollListener(paginationScrollListener())
        }

        // Liste pour la recherche
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(ctx)
            adapter = MembersListAdapter(ctx, membersListSearch, reactionList, conversationCallback())
            addItemDecoration(divider)
        }
    }

    private fun setupSearchBar() {
        // Validation clavier
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performRemoteSearch(binding.searchBar.text.toString())
                true
            } else false
        }

        // FOCUS ➜ on passe sur la liste search
        binding.searchBar.setOnFocusChangeListener { _, _ ->
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_MEMBER_SEARCH_START)
            switchToSearchMode()
        }

        // Gestion croix / texte
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                binding.searchBarLayout.endIconMode = if (s.isNullOrEmpty())
                    TextInputLayout.END_ICON_NONE else TextInputLayout.END_ICON_CUSTOM
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.searchBarLayout.setEndIconOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_MEMBER_SEARCH_DELETE)
            binding.searchBar.text?.clear()
            exitSearchMode()
        }
    }

    private fun conversationCallback(): OnItemShowListener = object : OnItemShowListener {
        override fun onShowConversation(userId: Int) {
            discussionPresenter.createOrGetConversation(userId.toString())
        }
    }

    // ---------------------------------------------------------------------
    // Observateurs LiveData
    // ---------------------------------------------------------------------

    private fun setupObservers() {
        // Conversation 1‑to‑1
        discussionPresenter.newConversation.observe(this, ::openConversation)

        if (isFromReact) {
            // Réactions uniquement
            observeReactions()
            return
        }

        when (type) {
            MembersType.GROUP -> {
                observeGroupMembers()
                observeGroupSearch()
            }
            MembersType.EVENT -> {
                observeEventMembers()
                observeEventSearch()
            }
        }
    }

    private fun observeReactions() {
        when (type) {
            MembersType.GROUP -> groupPresenter.getMembersReactResponse.observe(this, ::handleReactions)
            MembersType.EVENT -> eventPresenter.getMembersReactResponse.observe(this, ::handleReactions)
        }
    }

    private fun observeGroupMembers() {
        groupPresenter.pagedMembers.observe(this) { updateMainList(it) }
    }

    private fun observeEventMembers() {
        eventPresenter.pagedEventMembers.observe(this) { updateMainList(it) }
    }

    private fun observeGroupSearch() {
        groupPresenter.getMembersSearch.observe(this, ::handleSearchList)
    }

    private fun observeEventSearch() {
        eventPresenter.getMembersSearch.observe(this, ::handleSearchList)
    }

    // ---------------------------------------------------------------------
    // Chargements / pagination
    // ---------------------------------------------------------------------

    private fun loadFirstPage() {
        if (isFromReact) {
            binding.layoutSearchbar.visibility = View.GONE
            when (type) {
                MembersType.GROUP -> groupPresenter.getReactDetails(id, postId)
                MembersType.EVENT -> eventPresenter.getReactDetails(id, postId)
            }
            return
        }

        when (type) {
            MembersType.GROUP -> {
                groupPresenter.resetMembersPaging()
                groupPresenter.loadGroupMembers(id)
            }
            MembersType.EVENT -> {
                eventPresenter.resetEventMembersPaging()
                eventPresenter.loadEventMembers(id)
            }
        }
    }

    /** ScrollListener déclenchant le chargement de la page suivante. */
    private fun paginationScrollListener() = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
            if (dy <= 0 || isFromReact) return // haut ou mode réactions
            val lm = rv.layoutManager as LinearLayoutManager
            val shouldLoadMore = (lm.childCount + lm.findFirstVisibleItemPosition()) >= lm.itemCount - 3
            if (shouldLoadMore) {
                when (type) {
                    MembersType.GROUP -> groupPresenter.loadGroupMembers(id)
                    MembersType.EVENT -> eventPresenter.loadEventMembers(id)
                }
            }
        }
    }

    // ---------------------------------------------------------------------
    // Callbacks : mise à jour UI
    // ---------------------------------------------------------------------

    private fun updateMainList(newData: MutableList<EntourageUser>) {
        membersList.clear()
        membersList.addAll(newData)
        binding.progressBar.visibility = View.GONE
        updateEmptyState(membersList.isEmpty())
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun handleReactions(resp: CompleteReactionsResponse) {
        membersList.clear(); reactionList.clear()
        resp.user_reactions.forEach { ur ->
            membersList += ur.user
            val match = MainActivity.reactionsList?.find { it.id == ur.reaction_id }
            val reaction = match ?: ReactionType(ur.reaction_id, match?.key, match?.imageUrl)
            reactionList += reaction
        }
        binding.progressBar.visibility = View.GONE
        updateEmptyState(membersList.isEmpty())
        (binding.recyclerView.adapter as MembersListAdapter).resetData(membersList, reactionList)
    }

    private fun handleSearchList(list: MutableList<EntourageUser>?) {
        membersListSearch.clear(); list?.let { membersListSearch.addAll(it) }
        binding.progressBar.visibility = View.GONE
        val empty = membersListSearch.isEmpty()
        binding.emptyStateLayout.visibility = if (empty) View.VISIBLE else View.GONE
        binding.title.setText(R.string.group_list_search_empty_state_title)
        binding.searchRecyclerView.visibility = if (empty) View.GONE else View.VISIBLE
        binding.searchRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyStateLayout.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.title.setText(R.string.group_list_empty_state_title)
    }

    // ---------------------------------------------------------------------
    // Recherche distante
    // ---------------------------------------------------------------------

    private fun performRemoteSearch(query: String) {
        if (query.isBlank()) return
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_GROUP_MEMBER_SEARCH_VALIDATE)
        Utils.hideKeyboard(this)
        showLoader()
        when (type) {
            MembersType.GROUP -> groupPresenter.getGroupMembersSearch(query)
            MembersType.EVENT -> eventPresenter.getEventMembersSearch(query)
        }
    }

    private fun showLoader() {
        binding.recyclerView.visibility = View.GONE
        binding.searchRecyclerView.visibility = View.GONE
        binding.emptyStateLayout.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun switchToSearchMode() {
        binding.recyclerView.visibility = View.GONE
        binding.searchRecyclerView.visibility = View.VISIBLE
    }

    private fun exitSearchMode() {
        Utils.hideKeyboard(this)
        binding.searchRecyclerView.visibility = View.GONE
        updateEmptyState(membersList.isEmpty())
    }

    // ---------------------------------------------------------------------
    // Conversation 1‑to‑1
    // ---------------------------------------------------------------------
    private fun openConversation(conv: Conversation?) {
        conv ?: return
        DetailConversationActivity.isSmallTalkMode = false
        startActivityForResult(
            Intent(this, DetailConversationActivity::class.java).putExtras(
                bundleOf(
                    Const.ID to conv.id,
                    Const.POST_AUTHOR_ID to conv.user?.id,
                    Const.SHOULD_OPEN_KEYBOARD to false,
                    Const.NAME to conv.title,
                    Const.IS_CONVERSATION_1TO1 to true,
                    Const.IS_MEMBER to true,
                    Const.IS_CONVERSATION to true,
                    Const.HAS_TO_SHOW_MESSAGE to conv.hasToShowFirstMessage()
                )
            ), 0
        )
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------
    private fun updateHeaderTexts() {
        if (isFromReact) {
            binding.headerTitle.setText(R.string.see_member_react)
            return
        }
        when (type) {
            MembersType.GROUP -> {
                binding.headerTitle.setText(R.string.see_members)
                binding.searchBar.hint = getString(R.string.search_member_hint)
            }
            MembersType.EVENT -> {
                binding.headerTitle.setText(R.string.see_participants)
                binding.searchBar.hint = getString(R.string.search_participant_hint)
            }
        }
    }

    companion object {
        var isFromReact = false
        var postId: Int = 0
    }
}

// -------------------------------------------------------------------------
// ENUM
// -------------------------------------------------------------------------

enum class MembersType(val code: Int) { GROUP(0), EVENT(1) }
