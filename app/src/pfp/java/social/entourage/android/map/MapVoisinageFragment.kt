package social.entourage.android.map

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.annotation.NonNull
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.pfp.fragment_map.*
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
import social.entourage.android.entourage.EntourageCloseFragment
import social.entourage.android.entourage.EntourageCloseFragment.Companion.newInstance
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.newsfeed.FeedItemOptionsFragment
import social.entourage.android.service.EntourageService
import social.entourage.android.service.EntourageService.LocalBinder
import social.entourage.android.service.EntourageServiceListener
import social.entourage.android.tour.join.TourJoinRequestFragment
import social.entourage.android.view.EntourageSnackbar.make
import timber.log.Timber
import java.util.*

class MapVoisinageFragment : MapFragment(), EntourageServiceListener {
    // ----------------------------------
    // CONSTANTS
    // ----------------------------------
    private val connection = ServiceConnection()
    private var isBound = false

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isBound) {
            doBindService()
        }
    }

    override fun onDestroy() {
        if (isBound && entourageService != null) {
            entourageService.unregisterServiceListener(this)
            doUnbindService()
        }
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_create_outing?.setOnClickListener {createAction(null, Entourage.TYPE_OUTING)}
    }

    private fun checkAction(action: String) {
        if (activity != null && isBound) {
            if (PlusFragment.KEY_CREATE_OUTING == action) {
                createAction(null, Entourage.TYPE_OUTING)
            }
        }
    }

    // ----------------------------------
    // Long clicks on map handler
    // ----------------------------------
    override fun showLongClickOnMapOptions(latLng: LatLng) { }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun onUserInfoUpdated(event: OnUserInfoUpdatedEvent?) {
        if (newsfeedAdapter == null) return
        val meAsAuthor = EntourageApplication.me(context)?.asTourAuthor() ?: return
        val dirtyList: MutableList<TimestampedObject> = ArrayList()
        // See which cards needs updating
        for (timestampedObject in newsfeedAdapter.items) {
            if (timestampedObject !is FeedItem) continue
            val author = timestampedObject.author ?: continue
            // Skip null author
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
            newsfeedAdapter.updateCard(dirty)
        }
    }

    // ----------------------------------
    // CLICK CALLBACKS
    // ----------------------------------
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
            if (layoutMain != null) {
                make(layoutMain, feedItem.closedToastMessage, Snackbar.LENGTH_SHORT).show()
            }
        }
        loaderStop?.dismiss()
        loaderStop = null
    }

    override fun onUserStatusChanged(user: TourUser, feedItem: FeedItem) {
        if (activity == null || requireActivity().isFinishing) return
        if (feedItem.type == TimestampedObject.ENTOURAGE_CARD) {
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

    @Subscribe
    override fun onLocationPermissionGranted(event: OnLocationPermissionGranted) {
        super.onLocationPermissionGranted(event)
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------
    override fun redrawWholeNewsfeed(newsFeeds: List<Newsfeed>) {
        if (map != null && newsFeeds.isNotEmpty() && newsfeedAdapter != null) {
            //redraw the whole newsfeed
            for (timestampedObject in newsfeedAdapter.items) {
                if (timestampedObject.type == TimestampedObject.ENTOURAGE_CARD) {
                    drawNearbyEntourage(timestampedObject as Entourage)
                }
            }
            mapClusterManager.cluster()
        }
    }

    private fun updateNewsfeedJoinStatus(timestampedObject: TimestampedObject) {
        if (newsfeedAdapter != null) {
            newsfeedAdapter.updateCard(timestampedObject)
        }
    }

    // ----------------------------------
    // SERVICE BINDING METHODS
    // ----------------------------------
    private fun doBindService() {
        if (activity != null) {
            EntourageApplication.me(activity)
                    ?: return // Don't start the service
            try {
                val intent = Intent(activity, EntourageService::class.java)
                requireActivity().startService(intent)
                requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)
            } catch (e: IllegalStateException) {
                Timber.w(e)
            }
        }
    }

    private fun doUnbindService() {
        if (isBound) {
            activity?.unbindService(connection)
            isBound = false
        }
    }

    override fun displayFeedItemOptions(feedItem: FeedItem) {
        if (activity != null) {
            if (!feedItem.isMine(context) || feedItem.isFreezed || !feedItem.canBeClosed()) {
                val feedItemOptionsFragment = FeedItemOptionsFragment.newInstance(feedItem)
                feedItemOptionsFragment.show(requireActivity().supportFragmentManager, FeedItemOptionsFragment.TAG)
                return
            }
            newInstance(feedItem).show(requireActivity().supportFragmentManager, EntourageCloseFragment.TAG, context)
        }
    }

    @Subscribe
    override fun feedItemCloseRequested(event: OnFeedItemCloseRequestEvent) {
        super.feedItemCloseRequested(event)
    }

    @Subscribe
    fun checkIntentAction(event: OnCheckIntentActionEvent?) {
        if (activity == null) {
            Timber.w("No activity found")
            return
        }
        val intent = requireActivity().intent
        if (intent.action != null) {
            checkAction(intent.action!!)
        }

        val content = (intent.extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as Message?)?.content
                ?:return
        when (intent.action) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE, PushNotificationContent.TYPE_NEW_JOIN_REQUEST, PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED -> if (content.isTourRelated) {
                    displayChosenFeedItem(content.joinableUUID, TimestampedObject.TOUR_CARD)
                } else if (content.isEntourageRelated) {
                    displayChosenFeedItem(content.joinableUUID, TimestampedObject.ENTOURAGE_CARD)
                }
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION -> if (content.extra != null) {
                displayChosenFeedItem(content.extra.entourageId.toString(), TimestampedObject.ENTOURAGE_CARD, content.extra.invitationId.toLong())
            }
            PushNotificationContent.TYPE_INVITATION_STATUS -> if (content.isEntourageRelated || content.isTourRelated) {
                displayChosenFeedItem(content.joinableUUID, if (content.isTourRelated) TimestampedObject.TOUR_CARD else TimestampedObject.ENTOURAGE_CARD)
            }
        }
    }

    // ----------------------------------
    // INNER CLASSES
    // ----------------------------------
    private inner class ServiceConnection : android.content.ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity == null) {
                Timber.e("No activity for service")
                return
            }
            entourageService = (service as LocalBinder).service
            if (entourageService == null) {
                Timber.e("Service not found")
                return
            }
            entourageService.registerServiceListener(this@MapVoisinageFragment)
            entourageService.registerApiListener(this@MapVoisinageFragment)
            if (entourageService.isRunning) {
                updateFloatingMenuOptions()
            }
            entourageService.updateNewsfeed(pagination, selectedTab)
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entourageService?.unregisterServiceListener(this@MapVoisinageFragment)
            entourageService?.unregisterApiListener(this@MapVoisinageFragment)
            entourageService = null
        }
    }
}