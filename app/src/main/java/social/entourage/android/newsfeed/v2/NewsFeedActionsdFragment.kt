package social.entourage.android.newsfeed.v2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_home_news.*
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_map.fragment_map_filter_button
import kotlinx.android.synthetic.main.fragment_map.fragment_map_main_layout
import kotlinx.android.synthetic.main.fragment_map.tour_stop_button
import social.entourage.android.EntourageApplication
import social.entourage.android.PlusFragment
import social.entourage.android.R
import social.entourage.android.api.model.*
import social.entourage.android.api.model.feed.*
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.BackPressable
import social.entourage.android.entourage.FeedItemOptionsFragment
import social.entourage.android.service.EntService
import social.entourage.android.service.EntService.LocalBinder
import social.entourage.android.service.EntourageServiceListener
import social.entourage.android.entourage.join.EntourageJoinRequestFragment
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.newsfeed.NewsfeedTabItem
import social.entourage.android.tour.join.TourJoinRequestFragment
import social.entourage.android.tools.view.EntSnackbar.make
import timber.log.Timber
import java.util.*

open class NewsFeedActionsFragment : BaseNewsfeedFragment(), EntourageServiceListener, BackPressable {

    var isActionSelected = true
    private val connection = ServiceConnection()

    /******
     * BackPressable
     */

    override fun onBackPressed(): Boolean {
        Timber.d("***** ici onback FG -- ${requireActivity().supportFragmentManager.fragments}")
        requireActivity().supportFragmentManager.popBackStack(BaseNewsfeedFragment.TAG,0)

        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",false)
        editor.putString("navType",null)
        editor.apply()

        return true
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connection.doBindService()
    }

    override fun onDestroy() {
        connection.doUnbindService()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_news, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui_bt_back?.setOnClickListener {
            requireActivity().onBackPressed()
        }

        if (isActionSelected) {
            fragment_map_filter_button?.visibility = View.VISIBLE
        }
        else {
            fragment_map_filter_button?.visibility = View.GONE
        }
    }

    protected open fun checkAction(action: String) {
        when (action) {
            PlusFragment.KEY_CREATE_CONTRIBUTION -> createAction(BaseEntourage.GROUPTYPE_ACTION, BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
            PlusFragment.KEY_CREATE_DEMAND -> createAction(BaseEntourage.GROUPTYPE_ACTION, BaseEntourage.GROUPTYPE_ACTION_DEMAND)
            PlusFragment.KEY_CREATE_OUTING -> createAction(BaseEntourage.GROUPTYPE_OUTING)
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    open fun onUserChoiceChanged(event: OnUserChoiceEvent) {}

    @Subscribe
    open fun onUserInfoUpdated(event: OnUserInfoUpdatedEvent?) {
        if (newsfeedAdapter == null) return
        val meAsAuthor = EntourageApplication.me(context)?.asTourAuthor() ?: return
        val dirtyList: MutableList<TimestampedObject> = ArrayList()
        // See which cards needs updating
        newsfeedAdapter?.items?.filterIsInstance<FeedItem>()?.forEach { feedItem ->
            // Skip null author
            val author = feedItem.author ?: return@forEach
            // Skip not same author id
            if (author.userID != meAsAuthor.userID) return@forEach
            // Skip if nothing changed
            if (!author.isSame(meAsAuthor)) {
                // Update the tour author
                meAsAuthor.userName = author.userName
                feedItem.author = meAsAuthor
                // Mark as dirty
                dirtyList.add(feedItem)
            }
        }
        // Update the dirty cards
        for (dirty in dirtyList) {
            newsfeedAdapter?.updateCard(dirty)
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------
    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    override fun showLongClickOnMapOptions(latLng: LatLng) {
      //  if(this is NewsFeedWithTourFragment) return super.showLongClickOnMapOptions(latLng)
        //for public user, start the create entourage funnel directly
        // save the tap coordinates
        longTapCoordinates = latLng
        //hide the FAB menu
        tour_stop_button?.visibility = View.GONE
        displayEntourageDisclaimer()
    }

    // ----------------------------------
    // BUS LISTENERS : needs to be in final class (not in parent class
    // ----------------------------------
    @Subscribe
    override fun onMyEntouragesForceRefresh(event: OnMyEntouragesForceRefresh) {
        super.onMyEntouragesForceRefresh(event)
    }

    @Subscribe
    override fun onEntourageCreated(event: OnEntourageCreated) {
        super.onEntourageCreated(event)
    }

    @Subscribe
    override fun onEntourageUpdated(event: OnEntourageUpdated) {
        super.onEntourageUpdated(event)
    }

    @Subscribe
    override fun onNewsfeedLoadMoreRequested(event: OnNewsfeedLoadMoreEvent) {
        super.onNewsfeedLoadMoreRequested(event)
    }

    @Subscribe
    override fun onMapFilterChanged(event: OnMapFilterChanged) {
        super.onMapFilterChanged(event)
    }

    @Subscribe
    override fun onBetterLocation(event: OnBetterLocationEvent) {
        super.onBetterLocation(event)
    }

    @Subscribe
    override fun userActRequested(event: OnUserActEvent) {
        super.userActRequested(event)
    }

    override fun onFeedItemClosed(closed: Boolean, updatedFeedItem: FeedItem) {
        if (closed) {
            refreshFeed()
            fragment_map_main_layout?.let { layout -> make(layout, updatedFeedItem.getClosedToastMessage(), Snackbar.LENGTH_SHORT).show() }
        }
        loaderStop?.dismiss()
        loaderStop = null
    }

    override fun onUserStatusChanged(user: EntourageUser, updatedFeedItem: FeedItem) {
        activity?.let {activity ->
            if (activity.isFinishing) return
            try {
                updatedFeedItem.joinStatus = user.status ?: ""
                if (user.status == FeedItem.JOIN_STATUS_PENDING) {
                    if (updatedFeedItem is Tour) {
                        TourJoinRequestFragment.newInstance(updatedFeedItem).show(activity.supportFragmentManager, TourJoinRequestFragment.TAG)
                    } else if (updatedFeedItem is BaseEntourage) {
                        EntourageJoinRequestFragment.newInstance(updatedFeedItem).show(activity.supportFragmentManager, EntourageJoinRequestFragment.TAG)
                    }
                }
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
            updateNewsfeedJoinStatus(updatedFeedItem)
            isRequestingToJoin--
        }
    }

    override fun removeRedundantNewsfeed(currentFeedList: List<NewsfeedItem>): List<NewsfeedItem> {
        val tempList = super.removeRedundantNewsfeed(currentFeedList)
        val newList = ArrayList<NewsfeedItem>()
        try {
            for (newsfeed in tempList) {
                (newsfeed.data as? Tour)?.let {card ->
                    //TODO verify if we can write !=true instead of !()==true when not a tour
                    if (!((newsfeedAdapter?.findCard(card) as? Tour)?.isSame(card)==true)) {
                        newList.add(newsfeed)
                    }
                } ?: run {
                    if(newsfeed.data != null) {
                        newList.add(newsfeed)
                    }
                }
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
        return newList
    }

    /////////////////////////////////////
    @Subscribe
    override fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        super.onLocationPermissionGranted(event)
    }

    private fun updateNewsfeedJoinStatus(timestampedObject: TimestampedObject) {
        newsfeedAdapter?.updateCard(timestampedObject)
    }

    override fun userStatusChanged(content: PushNotificationContent, status: String) {
        super.userStatusChanged(content, status)
        if (entService == null) return
        if (content.isTourRelated) {
            val timestampedObject = newsfeedAdapter?.findCard(TimestampedObject.TOUR_CARD, content.joinableId)
            if (timestampedObject is Tour) {
                val user = EntourageUser()
                user.userId = userId
                user.status = status
                entService?.notifyListenersUserStatusChanged(user, timestampedObject)
            }
        }
    }

    override fun displayFeedItemOptions(feedItem: FeedItem) {
        if (activity != null) {
            FeedItemOptionsFragment.show(feedItem, requireActivity().supportFragmentManager)
        }
    }

    @Subscribe
    override fun feedItemCloseRequested(event: OnFeedItemCloseRequestEvent) {
        super.feedItemCloseRequested(event)
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private inner class ServiceConnection : android.content.ServiceConnection {
        private var isBound = false

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity == null) {
                isBound = false
                Timber.e("No activity for service")
                return
            }
            entService = (service as LocalBinder).service
            entService?.let {
                it.registerServiceListener(this@NewsFeedActionsFragment)
                it.registerApiListener(this@NewsFeedActionsFragment)
                it.updateNewsfeed(pagination, selectedTab)
                isBound = true
            } ?: run {
                Timber.e("Service not found")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entService?.unregisterServiceListener(this@NewsFeedActionsFragment)
            entService?.unregisterApiListener(this@NewsFeedActionsFragment)
            entService = null
            isBound = false
        }
        // ----------------------------------
        // SERVICE BINDING METHODS
        // ----------------------------------
        fun doBindService() {
            if(isBound) return
            activity?.let {
                if(EntourageApplication.me(it) ==null) {
                    // Don't start the service
                    return
                }
                try {
                    val intent = Intent(it, EntService::class.java)
                    it.startService(intent)
                    it.bindService(intent, this, Context.BIND_AUTO_CREATE)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }

        fun doUnbindService() {
            if (!isBound) return
            entService?.unregisterServiceListener(this@NewsFeedActionsFragment)
            activity?.unbindService(this)
            isBound = false
        }

    }

    companion object {
        fun newInstance(isAction:Boolean): NewsFeedActionsFragment {
            val _intent = NewsFeedActionsFragment()
            _intent.selectedTab = if (isAction) NewsfeedTabItem.ALL_TAB else NewsfeedTabItem.EVENTS_TAB
            _intent.isActionSelected = isAction
            return _intent
        }
    }
}