package social.entourage.android.home

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.google.android.gms.maps.model.LatLng
import com.squareup.otto.Subscribe
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.PlusFragment
import social.entourage.android.R
import social.entourage.android.api.model.*
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events
import social.entourage.android.base.BaseFragment
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.configuration.Configuration
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.home.actions.NewsFeedActionsFragment
import social.entourage.android.home.expert.HomeExpertFragment
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tour.ToursFragment
import timber.log.Timber

@Deprecated("You should use HomeExpertFragment")
class HomeFragment : BaseFragment() {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    private var entService: EntService? = null

    // requested entourage category
    private var entourageCategory: EntourageCategory? = null

    //private val connection = ServiceConnection()
    private var isTourPostSend = false

    // requested group type
    private lateinit var groupType: String

    var feedItemTemporary:FeedItem? = null
    var countDownTimer:CountDownTimer? = null
    var popInfoCreateEntourageFragment:PopInfoCreateEntourageFragment? = null
    val countDown = 5000L

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.beginTransaction()
            .replace(R.id.ui_container, HomeExpertFragment())
            .commit()
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    private fun displayChosenFeedItem(feedItemUUID: String, feedItemType: Int, invitationId: Long = 0) {
        //display the feed item
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        //presenter.openFeedItemFromUUID(feedItemUUID, feedItemType, invitationId)
    }

    private fun displayChosenFeedItem(feedItem: FeedItem, feedRank: Int,isFromCreate:Boolean) {
        displayChosenFeedItem(feedItem, 0, feedRank,isFromCreate)
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    private fun displayChosenFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int = 0,isFromCreate: Boolean) {
        if (context == null || isStateSaved) return
        // decrease the badge count
        EntourageApplication.get(context).removePushNotificationsForFeedItem(feedItem)
        //check if we are not already displaying the tour
        (activity?.supportFragmentManager?.findFragmentByTag(FeedItemInformationFragment.TAG) as? FeedItemInformationFragment)?.let {
            if (it.getItemType() == feedItem.type && it.feedItemId != null && it.feedItemId.equals(feedItem.uuid, ignoreCase = true)) {
                //TODO refresh the tour info screen
                return
            }
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)

        if (!isFromCreate) {
            //presenter.openFeedItem(feedItem, 0 , 0,false)
            return
        }
        feedItemTemporary = feedItem

        updatePopCreateAndShow()
    }

    private fun updatePopCreateAndShow() {
        var title = ""
        var subtitle = ""

        if (feedItemTemporary is EntourageEvent) {
            title = getString(R.string.infoPopCreateEventTitle)
            subtitle = getString(R.string.infoPopCreateEvent)
        }
        else {
            if (feedItemTemporary is EntourageContribution) {
                title = getString(R.string.infoPopCreateContribTitle)
                subtitle = getString(R.string.infoPopCreateContrib)
            }
            else {
                title = getString(R.string.infoPopCreateAskTitle)
                subtitle = getString(R.string.infoPopCreateAsk)
            }
        }

        popInfoCreateEntourageFragment = PopInfoCreateEntourageFragment.newInstance(title,subtitle)
        //popInfoCreateEntourageFragment?.homeFragment = this
        popInfoCreateEntourageFragment?.show(requireActivity().supportFragmentManager,PopInfoCreateEntourageFragment.TAG)

        countDownTimer = object : CountDownTimer(countDown, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                closePopAndGo()
            }
        }
        countDownTimer?.start()
    }

    fun closePopAndGo() {
        popInfoCreateEntourageFragment?.dismiss()
        countDownTimer?.cancel()
        countDownTimer = null
        //feedItemTemporary?.let { presenter.openFeedItem(it, 0 , 0,true) }
    }

    private fun displayChosenFeedItemFromShareURL(feedItemShareURL: String, feedItemType: Int) {
        //display the feed item
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        //presenter.openFeedItemFromShareURL(feedItemShareURL, feedItemType)
    }

    fun displayEntourageDisclaimer() {
        // Check if we need to show the entourage disclaimer
        if (Configuration.showEntourageDisclaimer()) {
            displayEntourageDisclaimer(groupType)
        } else {
            (activity as? MainActivity)?.onEntourageDisclaimerAccepted(null)
        }
    }

    fun createEntourage() {
        var location = EntLocation.lastCameraPosition.target
        if (!BaseEntourage.GROUPTYPE_OUTING.equals(groupType, ignoreCase = true)) {
            // For demand/contribution, by default select the action zone location, if set
            EntourageApplication.me(activity)?.address?.let { address ->
                location = LatLng(address.latitude, address.longitude)
            }
        }
        createEntourage(location, groupType, entourageCategory)
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------
    fun createAction(newGroupType: String, newActionGroupType: String) {
        entourageCategory = EntourageCategoryManager.getDefaultCategory(newActionGroupType)
        groupType = newGroupType
        entourageCategory?.isNewlyCreated = true
        displayEntourageDisclaimer()
    }

    fun createAction(newEntourageGroupType: String) {
        entourageCategory = null
        groupType = newEntourageGroupType
        displayEntourageDisclaimer()
    }

    fun setGroupType(groupString:String) {
        groupType = groupString
    }

    // ----------------------------------
    // PRIVATE METHODS (tours events)
    // ----------------------------------
    fun pauseTour(tour: Tour) {
        if (entService?.isRunning == true) {
            if (entService?.currentTourId.equals(tour.uuid, ignoreCase = true)) {
                entService?.pauseTreatment()
            }
        }
    }

    fun saveOngoingTour() {
        entService?.updateOngoingTour()
    }


    private fun createEntourage(location: LatLng?, groupType: String, category: EntourageCategory?) {
        if (!isStateSaved) {
            val fragmentManager = activity?.supportFragmentManager ?: return
            BaseCreateEntourageFragment.newExpertInstance(location, groupType, category).show(fragmentManager, BaseCreateEntourageFragment.TAG)
        }
    }

    private fun displayEntourageDisclaimer(groupType: String) {
        if (!isStateSaved) {
            val fragmentManager = activity?.supportFragmentManager ?:return
            EntourageDisclaimerFragment.newInstance(groupType).show(fragmentManager, EntourageDisclaimerFragment.TAG)
        }
    }

    private fun showActions(isAction:Boolean) {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment, NewsFeedActionsFragment.newInstance(isAction, false),
                NewsFeedActionsFragment.TAG)
            addToBackStack(NewsFeedActionsFragment.TAG)
            val navKey = if (isAction) "action" else "event"
            //presenter.saveInfo(true,navKey)
        }
    }

    private fun showTour() {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment, ToursFragment(),ToursFragment.TAG)
            addToBackStack(ToursFragment.TAG)
            //presenter.saveInfo(true,"tour")
        }
    }

    @Subscribe
    fun checkIntentAction(event: Events.OnCheckIntentActionEvent) {
        if (activity == null) {
            Timber.w("No activity found")
            return
        }
        checkAction(event.action)
        val content = (event.extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as? Message)?.content
                ?: return
        when (event.action) {
            PushNotificationContent.TYPE_NEW_CHAT_MESSAGE,
            PushNotificationContent.TYPE_NEW_JOIN_REQUEST,
            PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED -> if (content.isTourRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.TOUR_CARD)
            } else if (content.isEntourageRelated) {
                displayChosenFeedItem(content.joinableUUID, TimestampedObject.ENTOURAGE_CARD)
            }
            PushNotificationContent.TYPE_ENTOURAGE_INVITATION -> content.extra?.let { extra ->
                displayChosenFeedItem(extra.entourageId.toString(), TimestampedObject.ENTOURAGE_CARD, extra.invitationId.toLong())
            }
            PushNotificationContent.TYPE_INVITATION_STATUS -> content.extra?.let {
                if (content.isEntourageRelated || content.isTourRelated) {
                    displayChosenFeedItem(content.joinableUUID, if (content.isTourRelated) TimestampedObject.TOUR_CARD else TimestampedObject.ENTOURAGE_CARD)
                }
            }
        }
    }

    private fun checkAction(action: String) {
        when (action) {
            PlusFragment.KEY_CREATE_CONTRIBUTION -> createAction(BaseEntourage.GROUPTYPE_ACTION, BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
            PlusFragment.KEY_CREATE_DEMAND -> createAction(BaseEntourage.GROUPTYPE_ACTION, BaseEntourage.GROUPTYPE_ACTION_DEMAND)
            PlusFragment.KEY_CREATE_OUTING -> createAction(BaseEntourage.GROUPTYPE_OUTING)
            "android.intent.action.MAIN", "android.intent.action.VIEW" -> {}
            else -> {
                //Use for Tour
                if (isTourPostSend) return

                val frag = ToursFragment.newInstance()
                requireActivity().supportFragmentManager.commit {
                    add(R.id.main_fragment, frag,ToursFragment.TAG)
                    addToBackStack(ToursFragment.TAG)
                    //presenter.saveInfo(true,"tour")

                    isTourPostSend = true
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        EntBus.post(Events.OnCheckIntentActionEvent(action, null))
                        Handler(Looper.getMainLooper()).postDelayed({
                            isTourPostSend = false
                        }, 2000)
                    }, 1000)
                }
            }
        }
    }

    //To Handle deeplink for Event
    fun onShowEvents() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWEVENTS)
        (activity?.supportFragmentManager?.findFragmentByTag(HomeExpertFragment.TAG) as? HomeExpertFragment)?.onShowEvents()
    }

    fun onShowAll() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWALL)
        (activity?.supportFragmentManager?.findFragmentByTag(HomeExpertFragment.TAG) as? HomeExpertFragment)?.onShowAll()
    }

    /*****
     ** Method from NewsFeedFragment for handling closing action/event/tour
     *****/
    @Subscribe
    fun feedItemCloseRequested(event: Events.OnFeedItemCloseRequestEvent) {
        val feedItem = event.feedItem

        // Only the author can close entourages/tours
        val myId = EntourageApplication.me(context)?.id
                ?: return
        val author = feedItem.author ?: return
        if (author.userID != myId) {
            return
        }
        if (!feedItem.isClosed()) {
            // close
            stopFeedItem(feedItem, event.isSuccess,event.comment)
        } else {
            (feedItem as? Tour)?.let { tour ->
                if (!tour.isFreezed()) {
                    freezeTour(tour)
                }
            }
        }
    }

    private fun stopFeedItem(feedItem: FeedItem?, success: Boolean, comment:String?) {
        activity?.let { activity ->
            entService?.let { service ->
                if (feedItem != null
                        && (!service.isRunning
                                || feedItem.type != TimestampedObject.TOUR_CARD
                                || service.currentTourId.equals(feedItem.uuid, ignoreCase = true))) {
                    service.stopFeedItem(feedItem, success,comment)
                } else if (service.isRunning) {
                    service.endTreatment()
                    AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_STOP_TOUR)
                }
            }
        }
    }

    private fun freezeTour(tour: Tour) {
        entService?.freezeTour(tour)
    }

    /*****
     ** Methods & service for Tour add Encounter
    *****/
    fun onAddEncounter() {
        showTour()
        (activity?.supportFragmentManager?.findFragmentByTag(ToursFragment.TAG) as? ToursFragment)?.onAddEncounter()
    }

    companion object {
        const val TAG = "social.entourage.android.fragment_home"
    }
}