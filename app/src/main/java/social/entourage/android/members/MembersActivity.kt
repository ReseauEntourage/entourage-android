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
import com.google.android.material.textfield.TextInputLayout
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.databinding.NewFragmentMembersBinding
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.discussions.DiscussionsPresenter
import social.entourage.android.events.EventsPresenter
import social.entourage.android.groups.GroupPresenter
import social.entourage.android.api.model.Conversation
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.CompleteReactionsResponse
import social.entourage.android.api.model.ReactionType
import social.entourage.android.base.BaseActivity
import social.entourage.android.groups.details.members.MembersListAdapter
import social.entourage.android.groups.details.members.OnItemShowListener
import social.entourage.android.tools.utils.Const
import social.entourage.android.tools.utils.Utils
import social.entourage.android.tools.log.AnalyticsEvents

enum class MembersType(val code: Int) {
    GROUP(0),
    EVENT(1),
}

open class MembersActivity : BaseActivity() {

    private lateinit var binding: NewFragmentMembersBinding

    private var membersList: MutableList<EntourageUser> = mutableListOf()
    private var membersListSearch: MutableList<EntourageUser> = ArrayList()
    private var id: Int? = Const.DEFAULT_VALUE
    private var typeCode: Int? = Const.DEFAULT_VALUE
    private var type: MembersType? = MembersType.GROUP
    private val groupPresenter: GroupPresenter by lazy { GroupPresenter() }
    private val eventPresenter: EventsPresenter by lazy { EventsPresenter() }

    private val discussionPresenter: DiscussionsPresenter by lazy { DiscussionsPresenter() }
    private var reactionIterator:Int = 0
    private var reactionList:MutableList<ReactionType> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = NewFragmentMembersBinding.inflate(layoutInflater)
        AnalyticsEvents.logEvent(
            AnalyticsEvents.VIEW_GROUP_MEMBER_SHOW_LIST
        )
        id = intent.getIntExtra("ID", Const.DEFAULT_VALUE) // Utilise une valeur par défaut appropriée
        typeCode = intent.getIntExtra("TYPE", MembersType.GROUP.code)
        type = MembersType.values().firstOrNull { it.code == typeCode } ?: MembersType.GROUP

        getMembers()
        handleCloseButton()
        initializeMembers()
        initializeMembersSearch()
        handleEnterButton()
        handleSearchOnFocus()
        handleCross()
        handleCrossButton()
        binding.searchBarLayout.endIconMode = TextInputLayout.END_ICON_NONE

        //Use to show or create conversation 1 to 1
        discussionPresenter.newConversation.observe(this, ::handleGetConversation)
        setContentView(binding.root)
    }


    override fun onResume() {
        super.onResume()
        if(isFromReact){
            binding.headerTitle.text = this.getString(R.string.see_member_react)

        }else {
            if(type == MembersType.GROUP){
                binding.headerTitle.text = this.getString(R.string.see_members)
                binding.searchBar.hint = this.getString(R.string.search_member_hint)
            }
            else if(type == MembersType.EVENT){
                binding.headerTitle.text = this.getString(R.string.see_participants)
                binding.searchBar.hint = this.getString(R.string.search_participant_hint)
            }


        }
    }

    private fun getMembers() {

        if (type == MembersType.GROUP) {
            groupPresenter.getMembers.observe(this, ::handleResponseGetMembers)
            groupPresenter.getMembersReactResponse.observe(this, ::handleResponseGetMembersReact)
            groupPresenter.getMembersSearch.observe(
                this,
                ::handleResponseGetMembersSearch
            )
        } else if (type == MembersType.EVENT) {
            eventPresenter.getMembers.observe(this, ::handleResponseGetMembers)
            eventPresenter.getMembersReactResponse.observe(this, ::handleResponseGetMembersReact)
            eventPresenter.getMembersSearch.observe(
                this,
                ::handleResponseGetMembersSearch
            )
        }
        if(isFromReact) {
            binding.layoutSearchbar.visibility = View.GONE
            if(type == MembersType.GROUP) {
                groupPresenter.getReactDetails(id!!,MembersActivity.postId)

            } else if(type == MembersType.EVENT) {
                eventPresenter.getReactDetails(id!!,MembersActivity.postId)

            }
            return
        }
        if (type == MembersType.GROUP) {
            id?.let {
                groupPresenter.getGroupMembers(it)
            }
        } else if (type == MembersType.EVENT) {
            id?.let {
                eventPresenter.getEventMembers(it)
            }
        }

    }

    private fun handleResponseGetMembers(allMembers: MutableList<EntourageUser>?) {
        membersList.clear()
        allMembers?.let { membersList.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allMembers?.isEmpty()?.let { updateView(it) }
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }
    private fun handleResponseGetMembersReact(reactionsResponse: CompleteReactionsResponse) {
        membersList.clear()
        reactionList.clear()

        reactionsResponse.user_reactions.forEach { userReaction ->
            // Ajoute l'utilisateur
            membersList.add(userReaction.user)

            // Trouve la réaction correspondante dans MainActivity.reactionsList
            val matchingReaction = MainActivity.reactionsList?.find { it.id == userReaction.reaction_id }

            // Si une réaction correspondante est trouvée, l'utilise; sinon, crée un nouvel objet ReactionType avec des valeurs par défaut
            val reaction = matchingReaction ?: ReactionType(
                id = userReaction.reaction_id,
                key = matchingReaction?.key , // Ici, tu pourrais mettre une valeur par défaut ou laisser null si aucune correspondance n'est trouvée
                imageUrl = matchingReaction?.imageUrl // Idem pour imageUrl
            )

            reactionList.add(reaction)
        }

        updateView(membersList.isEmpty())
        binding.progressBar.visibility = View.GONE
        (binding.recyclerView.adapter as MembersListAdapter).resetData(membersList, reactionList)
    }





    private fun handleResponseGetMembersSearch(allMembersSearch: MutableList<EntourageUser>?) {
        membersListSearch.clear()
        allMembersSearch?.let { membersListSearch.addAll(it) }
        binding.progressBar.visibility = View.GONE
        allMembersSearch?.isEmpty()?.let { updateViewSearch(it) }
        binding.searchRecyclerView.adapter?.notifyDataSetChanged()
    }

    private fun handleCloseButton() {
        binding.iconBack.setOnClickListener {
            finish()
        }
    }

    private fun initializeMembers() {
        val context:Context = this
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.new_divider)
            ?.let { itemDecorator.setDrawable(it) }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MembersListAdapter(context,membersList,reactionList, object :
                OnItemShowListener {
                override fun onShowConversation(userId: Int) {
                    discussionPresenter.createOrGetConversation(userId)
                }
            })
            addItemDecoration(itemDecorator)
        }
    }

    private fun handleGetConversation(conversation: Conversation?) {
        conversation?.let {
            startActivityForResult(
                Intent(this, DetailConversationActivity::class.java)
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

    private fun initializeMembersSearch() {
        val context: Context = this
        val itemDecorator = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        ContextCompat.getDrawable(this, R.drawable.new_divider)
            ?.let { itemDecorator.setDrawable(it) }
        binding.searchRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MembersListAdapter(context,membersListSearch, reactionList, object : OnItemShowListener {
                override fun onShowConversation(userId: Int) {
                    discussionPresenter.createOrGetConversation(userId)
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
            binding.title.text = getString(R.string.group_list_empty_state_title)
        } else {
            binding.recyclerView.visibility = View.VISIBLE
        }
    }



    private fun handleEnterButton() {
        binding.searchBar.setOnEditorActionListener { _, actionId, _ ->
            var handled = false
            AnalyticsEvents.logEvent(
                AnalyticsEvents.ACTION_GROUP_MEMBER_SEARCH_VALIDATE
            )
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Utils.hideKeyboard(this)
                binding.searchRecyclerView.visibility = View.GONE
                binding.recyclerView.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.GONE
                binding.progressBar.visibility = View.VISIBLE
                Utils.hideKeyboard(this)
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

    override fun onDestroy() {
        super.onDestroy()
        MembersActivity.isFromReact = false
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
            Utils.hideKeyboard(this)
        }
    }

    companion object {
        var isFromReact = false
        var postId = 0
    }

}