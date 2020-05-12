package social.entourage.android.entourage.my

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_my_entourages.*
import social.entourage.android.*
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.*
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.EntourageDialogFragment
import social.entourage.android.base.EntouragePagination
import social.entourage.android.base.EntourageViewHolderListener
import social.entourage.android.entourage.my.MyEntouragesAdapter.LoaderCallback
import social.entourage.android.entourage.my.filter.MyEntouragesFilter
import social.entourage.android.service.EntourageService
import social.entourage.android.service.EntourageService.LocalBinder
import social.entourage.android.tools.BusProvider.instance
import social.entourage.android.view.EntourageSnackbar.make
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * My Entourages Fragment
 */
class MyEntouragesFragment  : EntourageDialogFragment(), EntourageViewHolderListener, LoaderCallback, ApiConnectionListener {
    // ----------------------------------
    // Attributes
    // ----------------------------------
    @Inject lateinit var presenter: MyEntouragesPresenter

    private val connection = ServiceConnection()

    private val entouragesAdapter: MyEntouragesAdapter = MyEntouragesAdapter()

    private val entouragesPagination = EntouragePagination(Constants.ITEMS_PER_PAGE)

    // Refresh invitations attributes
    internal var isRefreshingInvitations = false

    // ----------------------------------
    // Lifecycle
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection.doBindService()
        instance.register(this)
    }

    override fun onDestroy() {
        instance.unregister(this)
        connection.doUnbindService()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_my_entourages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupComponent(EntourageApplication.get(activity).entourageComponent)
        initializeView()
        refreshInvitations()
        refreshMyFeeds()
        (activity as MainActivity?)?.showEditActionZoneFragment()
    }

    private fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerMyEntouragesComponent.builder()
                .entourageComponent(entourageComponent)
                .myEntouragesModule(MyEntouragesModule(this))
                .build()
                .inject(this)
    }

    // ----------------------------------
    // Private methods
    // ----------------------------------
    private fun initializeView() {
        initializeFilterTab()
        initializeEntouragesView()
    }

    private fun initializeFilterTab() {
        myentourages_tab?.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val filter = MyEntouragesFilter.get(context)
                when (tab.position) {
                    FILTER_TAB_INDEX_ALL -> filter.isShowUnreadOnly = false
                    FILTER_TAB_INDEX_UNREAD -> {
                        filter.isShowUnreadOnly = true
                        EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_FILTER_UNREAD)
                    }
                }
                refreshMyFeeds()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // The tab is initialised ALL
        // So we need to reset the filter flag and no item view in case the user switched to another screen
        // while the UNREAD tab was active
        MyEntouragesFilter.get(context).isShowUnreadOnly = false
        myentourages_layout_no_items?.visibility = View.GONE
    }

    private fun initializeEntouragesView() {
        myentourages_list_view?.layoutManager = LinearLayoutManager(context)
        entouragesAdapter.viewHolderListener = this
        entouragesAdapter.setLoaderCallback(this)
        myentourages_list_view?.adapter = entouragesAdapter
        myentourages_swipeRefreshLayout?.setOnRefreshListener {
            refreshInvitations()
            refreshMyFeeds()
        }
    }

    private fun retrieveMyFeeds() {
        if (!entouragesPagination.isLoading) {
            entouragesPagination.isLoading = true
            presenter.getMyFeeds(entouragesPagination.page, entouragesPagination.itemsPerPage)
        }
    }

    private fun refreshMyFeeds() {
        myentourages_swipeRefreshLayout?.isRefreshing = true
        presenter.clear()
        // remove the current feed
        entouragesAdapter.removeAll()
        entouragesPagination.reset()
        // request a new feed
        retrieveMyFeeds()
        myentourages_swipeRefreshLayout?.isRefreshing = false
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun onMyEntouragesForceRefresh(event: OnMyEntouragesForceRefresh) {
        val item = event.feedItem
        if (item == null) {
            refreshInvitations()
            refreshMyFeeds()
        } else {
            entouragesAdapter.updateCard(item)
        }
    }

    @Subscribe
    fun onEntourageCreated(event: OnEntourageCreated) {
        refreshMyFeeds()
    }

    @Subscribe
    fun onEntourageUpdated(event: OnEntourageUpdated) {
        if (event.entourage == null) return
        entouragesAdapter.updateCard(event.entourage)
    }

    @Subscribe
    fun onInvitationStatusChanged(event: OnInvitationStatusChanged) {
        // Refresh the invitations list
        if(event.feedItem !is BaseEntourage) return
        entouragesAdapter.updateInvitation(event.feedItem as BaseEntourage, event.status)
        // Refresh the entourages list if invitation was accepted
        if (Invitation.STATUS_ACCEPTED == event.status) {
            refreshMyFeeds()
        }
    }

    @Subscribe
    fun feedItemViewRequested(event: OnFeedItemInfoViewRequestedEvent) {
        if (event.feedItem != null) {
            onPushNotificationConsumedForFeedItem(event.feedItem)
        }
    }

    // ----------------------------------
    // Push handling
    // ----------------------------------
    fun onPushNotificationReceived(message: Message) {
        val content = message.content ?: return
        val cardType: Int = if (content.isTourRelated) TimestampedObject.TOUR_CARD else if (content.isEntourageRelated) TimestampedObject.ENTOURAGE_CARD else return
        val card = entouragesAdapter.findCard(cardType, content.joinableId)
        if (card is FeedItem) {
            card.increaseBadgeCount(PushNotificationContent.TYPE_NEW_CHAT_MESSAGE == content.type)
            card.setLastMessage(content.message, message.author)
            //approximate message time with Now //TODO get proper time
            card.updatedTime = Date()
            entouragesAdapter.updateCard(card)
        } else {
            refreshInvitations()
            refreshMyFeeds()
        }
    }

    private fun onPushNotificationConsumedForFeedItem(feedItem: FeedItem?) {
        val feedItemCard = entouragesAdapter.findCard(feedItem) as FeedItem? ?: return
        feedItemCard.decreaseBadgeCount()
        entouragesAdapter.updateCard(feedItemCard)
    }

    private fun refreshInvitations() {
        if (!isRefreshingInvitations) {
            presenter.getMyPendingInvitations()
            isRefreshingInvitations = true
        }
    }

    // ----------------------------------
    // Presenter callbacks
    // ----------------------------------
    fun onNewsfeedReceived(newsfeedItemList: List<NewsfeedItem>?) {
        //reset the loading indicator
        entouragesPagination.isLoading = false
        if (!isAdded) {
            return
        }
        //ignore errors
        if (newsfeedItemList == null) return
        //add the feed
        val showUnreadOnly = MyEntouragesFilter.get(this.context).isShowUnreadOnly
        entouragesAdapter.removeLoader()
        if (newsfeedItemList.isNotEmpty()) {
            for (newsfeed in newsfeedItemList) {
                val feedItem = newsfeed.data as? FeedItem ?: continue

                // show only the unread ones if filter is set
                if (!showUnreadOnly || feedItem.getUnreadMsgNb() > 0) {
                    if (entouragesAdapter.findCard(feedItem) == null) {
                        entouragesAdapter.addCardInfo(feedItem)
                    }
                }
                instance.post(OnMyEntouragesForceRefresh(feedItem))
            }

            //increase page and items count
            entouragesPagination.loadedItems(newsfeedItemList.size)
            if (entouragesPagination.nextPageAvailable) {
                entouragesAdapter.addLoader()
            }
        }
        if (entouragesAdapter.dataItemCount == 0) {
            myentourages_layout_no_items?.visibility = View.VISIBLE
            myentourages_no_items_title?.visibility = if (showUnreadOnly) View.GONE else View.VISIBLE
            myentourages_no_items_details?.setText(if (showUnreadOnly) R.string.myentourages_no_unread_items_details else R.string.myentourages_no_items_details)
        } else {
            myentourages_layout_no_items?.visibility = View.GONE
        }
    }

    fun onNoInvitationReceived() {
        // ignore errors
        isRefreshingInvitations = false
    }

    // ----------------------------------
    // EntourageViewHolderListener
    // ----------------------------------
    override fun onViewHolderDetailsClicked(detailType: Int) {
        EntourageEvents.logEvent(EntourageEvents.EVENT_MYENTOURAGES_MESSAGE_OPEN)
    }

    override fun loadMoreItems() {
        retrieveMyFeeds()
    }

    override fun onNetworkException() {
        if (myentourages_layout != null) {
            make(myentourages_layout!!, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onServerException(throwable: Throwable) {
        if (myentourages_layout != null) {
            make(myentourages_layout!!, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        if (myentourages_layout != null) {
            make(myentourages_layout!!, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
    }

    fun removeOldInvitations(invitationList: List<Invitation?>) {
        entouragesAdapter.removeOldInvitations(invitationList)
    }

    fun addInvitation(it: Invitation) {
        entouragesAdapter.addInvitation(it)
    }

    private inner class ServiceConnection : android.content.ServiceConnection {
        private var entourageService: EntourageService? = null
        private var isBound = false
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity != null) {
                entourageService = (service as LocalBinder).service
                entourageService?.let {
                    it.registerApiListener(this@MyEntouragesFragment)
                    isBound = true
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entourageService?.unregisterApiListener(this@MyEntouragesFragment)
            entourageService = null
            isBound = false
        }

        // ----------------------------------
        // SERVICE BINDING METHODS
        // ----------------------------------
        fun doBindService() {
            if (activity != null) {
                try {
                    val intent = Intent(activity, EntourageService::class.java)
                    requireActivity().startService(intent)
                    requireActivity().bindService(intent, this, Context.BIND_AUTO_CREATE)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }

        fun doUnbindService() {
            if (isBound) {
                entourageService?.unregisterApiListener(this@MyEntouragesFragment)
                activity?.unbindService(this)
                isBound = false
            }
        }
    }

    companion object {
        // ----------------------------------
        // Constants
        // ----------------------------------
        const val TAG = "social.entourage.android.my.entourages"
        private const val FILTER_TAB_INDEX_ALL = 0
        private const val FILTER_TAB_INDEX_UNREAD = 1
    }
}