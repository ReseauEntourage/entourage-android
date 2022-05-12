package social.entourage.android.entourage.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_my_messages_main.*
import social.entourage.android.Constants
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.api.ConversationsAPI
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.base.BaseDialogFragment
import social.entourage.android.base.BasePagination
import social.entourage.android.entourage.my.MyMessagesRecyclerViewAdapter.Companion.LOADER_CARD
import java.util.ArrayList

/**
 * Created by Jr on 21/12/2021.
 */
class MyMessagesMainFragment : BaseDialogFragment(), MyMessagesRecyclerViewAdapter.LoadMoreCallback {
    private val entouragesPagination = BasePagination(Constants.ITEMS_PER_PAGE)
    private var entouragesAdapter: MyMessagesRecyclerViewAdapter? = null

    var messagesList: ArrayList<BaseEntourage> = ArrayList()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_messages_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeSelector()
        setupRecyclerView()

        refreshMyMessages(true)

        retrieveCounts()
    }

    private fun initializeSelector() {
        ui_mymessages_tab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    refreshMyMessages(true)
                }
                else {
                    refreshMyMessages(false)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        ui_messages_layout_no_items?.visibility = View.GONE
    }

    fun setupRecyclerView() {
        ui_mymessages_recyclerview?.layoutManager = LinearLayoutManager(context)
        entouragesAdapter = MyMessagesRecyclerViewAdapter { position ->
            if(position<messagesList.size){
                messagesList[position].numberOfUnreadMessages = 0
            }
            entouragesAdapter?.updateItemAtPositon(position)
        }
        entouragesAdapter?.setLoaderCallback(this)
        ui_mymessages_recyclerview?.adapter = entouragesAdapter
        ui_mymessages_swipeRefreshLayout?.setOnRefreshListener {
            if (ui_mymessages_tab?.selectedTabPosition == 0) {
                refreshMyMessages(true)
            }
            else {
                refreshMyMessages(false)
            }
        }
    }

    private fun retrieveCounts() {
        ConversationsAPI(EntourageApplication.get()).getMessagesMetadatas { conversations, error ->

            conversations?.let {
                val unread_private:Int = it.conversations.unread ?: 0
                val unread_actions:Int = it.actions.unread ?: 0
                val unread_outings:Int = it.outings.unread ?: 0
                val unread_group = unread_actions + unread_outings

                if (unread_private > 0) {
                    ui_layout_bubble_private?.visibility = View.VISIBLE
                    ui_tv_count_private?.text = "$unread_private"
                }
                else {
                    ui_layout_bubble_private?.visibility = View.INVISIBLE
                }

                if (unread_group > 0) {
                    ui_layout_bubble_group?.visibility = View.VISIBLE
                    ui_tv_count_group?.text = "$unread_group"
                }
                else {
                    ui_layout_bubble_group?.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun retrieveMyMessages(isGroup:Boolean) {
        if (!entouragesPagination.isLoading) {
            entouragesPagination.isLoading = true
            getMyMessages(entouragesPagination.page, entouragesPagination.itemsPerPage, isGroup)
        }
    }

    private fun refreshMyMessages(isGroup:Boolean) {
        ui_mymessages_swipeRefreshLayout?.isRefreshing = true
        // remove the current feed
        messagesList.clear()
        entouragesAdapter?.updateDatas(messagesList)
        entouragesPagination.reset()
        // request a new feed
        retrieveMyMessages(isGroup)
        ui_mymessages_swipeRefreshLayout?.isRefreshing = false
    }

    fun getMyMessages(page: Int, per: Int,isGroup:Boolean) {
        if (!isGroup) {
            ConversationsAPI(EntourageApplication.get()).getMessagesOne2One(page, per) { conversations, error ->
                conversations?.messages?.let { messages ->
                    onMessagesReceived(messages)
                }
            }
        }
        else {
            ConversationsAPI(EntourageApplication.get()).getMessagesGroup(page, per) { conversations, error ->
                conversations?.messages?.let { messages ->
                    onMessagesReceived(messages)
                }
            }
        }
    }

    fun onMessagesReceived(messagesList: List<BaseEntourage>?) {
        //reset the loading indicator
        entouragesPagination.isLoading = false
        if (!isAdded) {
            return
        }
        //ignore errors
        if (messagesList == null) return

        //On supprime le loader s'il existe
        val lastPosition = this.messagesList.size - 1
        if (this.messagesList.isNotEmpty() && this.messagesList[lastPosition].category == LOADER_CARD) {
            this.messagesList.removeAt(lastPosition)
        }

        if (messagesList.isNotEmpty()) {
            for (message in messagesList) {
                if ((message as? BaseEntourage)?.let { findMessage(it) }  == null) {
                    this.messagesList.add(message)
                }
            }

            //increase page and items count
            entouragesPagination.loadedItems(messagesList.size)

            if (entouragesPagination.nextPageAvailable) {
                val loadingEnt = BaseEntourage()
                loadingEnt.category = LOADER_CARD
                this.messagesList.add(loadingEnt)
            }
        }

        entouragesAdapter?.updateDatas(this.messagesList)

        if (this.messagesList.size == 0) {
            ui_messages_layout_no_items?.visibility = View.VISIBLE
        }
        else {
            ui_messages_layout_no_items?.visibility = View.GONE
        }
    }

    fun findMessage(item: BaseEntourage): BaseEntourage? {
        for (message in this.messagesList) {
            if (message == item) {
                return message
            }
        }
        return null
    }

    override fun loadMoreItems() {
        if (ui_mymessages_tab?.selectedTabPosition == 0) {
            retrieveMyMessages(true)
        }
        else {
            retrieveMyMessages(false)
        }
    }

    companion object {
        const val TAG = "social.entourage.android.my.messages.main"
    }
}