package social.entourage.android.newsfeed

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.View
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_map.*
import social.entourage.android.EntourageApplication
import social.entourage.android.PlusFragment
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.Newsfeed
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.map.Entourage
import social.entourage.android.api.model.map.FeedItem
import social.entourage.android.api.model.map.Tour
import social.entourage.android.api.model.map.TourUser
import social.entourage.android.api.tape.Events.*
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.service.EntourageService
import social.entourage.android.service.EntourageService.LocalBinder
import social.entourage.android.service.EntourageServiceListener
import social.entourage.android.tour.join.TourJoinRequestFragment
import social.entourage.android.view.EntourageSnackbar.make
import timber.log.Timber
import java.util.*

open class NewsFeedFragment : BaseNewsfeedFragment(), EntourageServiceListener {
    private val connection = ServiceConnection()

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

    protected open fun checkAction(action: String) {
        when (action) {
            PlusFragment.KEY_CREATE_CONTRIBUTION -> createAction(EntourageCategoryManager.getInstance().getDefaultCategory(Entourage.TYPE_CONTRIBUTION), Entourage.TYPE_ACTION)
            PlusFragment.KEY_CREATE_DEMAND -> createAction(EntourageCategoryManager.getInstance().getDefaultCategory(Entourage.TYPE_DEMAND), Entourage.TYPE_ACTION)
            PlusFragment.KEY_CREATE_OUTING -> createAction(null, Entourage.TYPE_OUTING)
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    open fun onUserChoiceChanged(event: OnUserChoiceEvent) {
    }

    @Subscribe
    open fun onUserInfoUpdated(event: OnUserInfoUpdatedEvent?) {
        if (newsfeedAdapter == null) return
        val meAsAuthor = EntourageApplication.me(context)?.asTourAuthor() ?: return
        val dirtyList: MutableList<TimestampedObject> = ArrayList()
        // See which cards needs updating
        for (timestampedObject in newsfeedAdapter!!.items) {
            if (timestampedObject !is FeedItem) continue
            // Skip null author
            val author = timestampedObject.author ?: continue
            // Skip not same author id
            if (author.userID != meAsAuthor.userID) continue
            // Skip if nothing changed
            if (author.isSame(meAsAuthor)) continue
            // Update the tour author
            meAsAuthor.userName = author.userName
            timestampedObject.author = meAsAuthor
            // Mark as dirty
            dirtyList.add(timestampedObject)
        }
        // Update the dirty cards
        for (dirty in dirtyList) {
            newsfeedAdapter!!.updateCard(dirty)
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------
    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    override fun showLongClickOnMapOptions(latLng: LatLng) {
        if(this is NewsFeedWithTourFragment) return super.showLongClickOnMapOptions(latLng)
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
    override fun feedItemViewRequested(event: OnFeedItemInfoViewRequestedEvent) {
        super.feedItemViewRequested(event)
    }

    @Subscribe
    override fun userActRequested(event: OnUserActEvent) {
        super.userActRequested(event)
    }

    override fun onFeedItemClosed(closed: Boolean, feedItem: FeedItem) {
        if (closed) {
            refreshFeed()
            if (fragment_map_main_layout != null) {
                make(fragment_map_main_layout, feedItem.closedToastMessage, Snackbar.LENGTH_SHORT).show()
            }
        }
        loaderStop?.dismiss()
        loaderStop = null
    }

    override fun onUserStatusChanged(user: TourUser, feedItem: FeedItem) {
        if (activity == null || requireActivity().isFinishing) return
        if (feedItem.type == TimestampedObject.TOUR_CARD || feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
            feedItem.joinStatus = user.status
            if (user.status == Tour.JOIN_STATUS_PENDING) {
                try {
                    TourJoinRequestFragment.newInstance(feedItem).show(requireActivity().supportFragmentManager, TourJoinRequestFragment.TAG)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }
        updateNewsfeedJoinStatus(feedItem)
        isRequestingToJoin--
    }

    override fun removeRedundantNewsfeed(currentFeedList: List<Newsfeed>): List<Newsfeed> {
        val tempList = super.removeRedundantNewsfeed(currentFeedList).toMutableList()
        val newList = ArrayList<Newsfeed>()
        try {
            for (newsfeed in tempList) {
                val card = newsfeed.data
                if (card !is TimestampedObject) {
                    continue
                }
                val retrievedCard = newsfeedAdapter?.findCard(card)
                if (retrievedCard!=null && (Tour.NEWSFEED_TYPE == newsfeed.type) && ((retrievedCard as Tour).isSame(card as Tour))) {
                    continue
                }
                newList.add(newsfeed)
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
        if (entourageService == null) return
        if (content.isTourRelated) {
            val timestampedObject = newsfeedAdapter?.findCard(TimestampedObject.TOUR_CARD, content.joinableId)
            if (timestampedObject is Tour) {
                val user = TourUser()
                user.userId = userId
                user.status = status
                entourageService?.notifyListenersUserStatusChanged(user, timestampedObject)
            }
        }
    }

    override fun displayFeedItemOptions(feedItem: FeedItem) {
        if (activity != null) {
            FeedItemOptionsFragment.newInstance(feedItem).show(requireActivity().supportFragmentManager, FeedItemOptionsFragment.TAG)
        }
    }

    @Subscribe
    override fun feedItemCloseRequested(event: OnFeedItemCloseRequestEvent) {
        super.feedItemCloseRequested(event)
    }

    @Subscribe
    open fun checkIntentAction(event: OnCheckIntentActionEvent) {
        if (activity == null) {
            Timber.w("No activity found")
            return
        }
        checkAction(event.action)
        val message: Message = event.extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as Message?
                ?: return
        val content = message.content
                ?: return
        val extra = content.extra
        when (event.action) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE,
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST,
            PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED -> if (content.isTourRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.TOUR_CARD)
            } else if (content.isEntourageRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.ENTOURAGE_CARD)
            }
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION -> if (extra != null) {
                displayChosenFeedItem(extra.entourageId.toString(), TimestampedObject.ENTOURAGE_CARD, extra.invitationId.toLong())
            }
            PushNotificationContent.TYPE_INVITATION_STATUS -> if (extra != null && (content.isEntourageRelated || content.isTourRelated)) {
                displayChosenFeedItem(content.joinableUUID, if (content.isTourRelated) TimestampedObject.TOUR_CARD else TimestampedObject.ENTOURAGE_CARD)
            }
        }
    }

    open fun updateFragmentFromService() {}

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private inner class ServiceConnection : android.content.ServiceConnection {
        protected var isBound = false

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity == null) {
                isBound = false
                Timber.e("No activity for service")
                return
            }
            entourageService = (service as LocalBinder).service
            if (entourageService == null) {
                Timber.e("Service not found")
                isBound = false
                return
            }
            entourageService!!.registerServiceListener(this@NewsFeedFragment)
            entourageService!!.registerApiListener(this@NewsFeedFragment)
            updateFragmentFromService()
            entourageService!!.updateNewsfeed(pagination, selectedTab)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entourageService?.unregisterServiceListener(this@NewsFeedFragment)
            entourageService?.unregisterApiListener(this@NewsFeedFragment)
            entourageService = null
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
                    val intent = Intent(it, EntourageService::class.java)
                    it.startService(intent)
                    it.bindService(intent, this, Context.BIND_AUTO_CREATE)
                } catch (e: IllegalStateException) {
                    Timber.w(e)
                }
            }
        }

        fun doUnbindService() {
            if (!isBound) return
            entourageService?.unregisterServiceListener(this@NewsFeedFragment)
            activity?.unbindService(this)
            isBound = false
        }

    }
}