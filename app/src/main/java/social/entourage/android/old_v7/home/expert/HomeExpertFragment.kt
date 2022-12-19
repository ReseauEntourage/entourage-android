package social.entourage.android.old_v7.home.expert

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_home_expert.*
import social.entourage.android.*
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.*
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.old_v7.base.BackPressable
import social.entourage.android.base.BaseFragment
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.old_v7.base.newsfeed.NewsfeedFragmentLifecycleCallbacks
import social.entourage.android.old_v7.base.newsfeed.NewsfeedPagination
import social.entourage.android.configuration.Configuration
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.old_v7.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.old_v7.entourage.create.CreateEntourageFragment
import social.entourage.android.old_v7.entourage.information.FeedItemInformationFragment
import social.entourage.android.old_v7.home.HomeCard
import social.entourage.android.old_v7.home.HomeCardType
import social.entourage.android.old_v7.home.HomeViewHolderListener
import social.entourage.android.old_v7.home.PopInfoCreateEntourageFragment
import social.entourage.android.old_v7.home.actions.NewsFeedActionsFragment
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.old_v7.MainActivity_v7
import social.entourage.android.old_v7.PlusFragment
import social.entourage.android.onboarding.InputNamesFragment
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.user.edit.photo.ChoosePhotoFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import timber.log.Timber

class HomeExpertFragment : BaseFragment(), BackPressable, ApiConnectionListener, UserEditActionZoneFragment.FragmentListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    var presenter: HomeExpertPresenter
    private var entService: EntService? = null

    private var adapterHome: HomeFeedAdapter? = null
    //pagination
    private var pagination = NewsfeedPagination()

    // keeps tracks of the attached fragments
    private var fragmentLifecycleCallbacks: NewsfeedFragmentLifecycleCallbacks? = null

    // requested group type
    private lateinit var groupType: String

    // requested entourage category
    private var entourageCategory: EntourageCategory? = null

    private val connection = ServiceConnection()
    private var arrayEmpty = ArrayList<HomeCard>()

    private var feedItemTemporary:FeedItem? = null
    private var countDownTimer:CountDownTimer? = null
    private var popInfoCreateEntourageFragment: PopInfoCreateEntourageFragment? = null

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onStart() {
        super.onStart()
        if (!LocationUtils.isLocationPermissionGranted()) {
            (activity as? MainActivity_v7)?.showEditActionZoneFragment(this,false)
        }
    }

    override fun onBackPressed(): Boolean {
        if (childFragmentManager.fragments.size == 1) return false
        childFragmentManager.popBackStackImmediate()
        return true

    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    fun closePopAndGo() {
        try  {
            countDownTimer?.cancel()
            countDownTimer = null
            popInfoCreateEntourageFragment?.dismiss()
            feedItemTemporary?.let { openFeedItem(it, true) }
        } catch(e: IllegalStateException) {
            Timber.w(e)
        }
    }

    private fun updatePopCreateAndShow() {
        var title = ""
        var subtitle = ""

        when (feedItemTemporary) {
            is EntourageEvent -> {
                title = getString(R.string.infoPopCreateEventTitle)
                subtitle = getString(R.string.infoPopCreateEvent)
            }
            is EntourageContribution -> {
                title = getString(R.string.infoPopCreateContribTitle)
                subtitle = getString(R.string.infoPopCreateContrib)
            }
            else -> {
                title = getString(R.string.infoPopCreateAskTitle)
                subtitle = getString(R.string.infoPopCreateAsk)
            }
        }

        popInfoCreateEntourageFragment = PopInfoCreateEntourageFragment.newInstance(title,subtitle)
        popInfoCreateEntourageFragment?.homeFragment = this
        popInfoCreateEntourageFragment?.show(requireActivity().supportFragmentManager,
            PopInfoCreateEntourageFragment.TAG)

        countDownTimer = object : CountDownTimer(countDown, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                closePopAndGo()
            }
        }
        countDownTimer?.start()
    }

    private fun displayChosenFeedItem(feedItem: FeedItem, isFromCreate: Boolean) {
        if (context == null || isStateSaved) return
        // decrease the badge count
        EntourageApplication.get(context).removePushNotificationsForFeedItem(feedItem)
        //check if we are not already displaying the item
        (activity?.supportFragmentManager?.findFragmentByTag(FeedItemInformationFragment.TAG) as? FeedItemInformationFragment)?.let {
            if (it.getItemType() == feedItem.type && it.feedItemId != null && it.feedItemId.equals(feedItem.uuid, ignoreCase = true)) {
                //TODO refresh the entourage info screen
                return
            }
        }
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)

        if (!isFromCreate) {
            openFeedItem(feedItem,false)
        } else {
            feedItemTemporary = feedItem
            updatePopCreateAndShow()
        }
    }

    fun displayEntourageDisclaimer() {
        // Check if we need to show the entourage disclaimer
        if (Configuration.showEntourageDisclaimer()) {
            if (!isStateSaved) {
                val fragmentManager = activity?.supportFragmentManager ?:return
                EntourageDisclaimerFragment.newInstance(groupType).show(fragmentManager, EntourageDisclaimerFragment.TAG)
            }
        } else {
            (activity as? MainActivity_v7)?.onEntourageDisclaimerAccepted(null)
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
        if (!isStateSaved) {
            activity?.supportFragmentManager?.let { fragmentManager->
                CreateEntourageFragment.newExpertInstance(location, groupType, entourageCategory).show(fragmentManager, CreateEntourageFragment.TAG)
            }
        }
    }

    private fun openFeedItem(feedItem: FeedItem, isFromActions:Boolean) {
        try {
            activity?.supportFragmentManager?.let { fragmentManager ->
                val oldFragment = fragmentManager.findFragmentByTag(FeedItemInformationFragment.TAG)
                if (oldFragment != null && oldFragment.isAdded()) return

                FeedItemInformationFragment.newInstance(feedItem,0,0,isFromActions).show(fragmentManager, FeedItemInformationFragment.TAG)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun openFeedItem(feedItem: FeedItem, invitationId: Long = 0) {
        try {
            activity?.supportFragmentManager?.let { fragmentManager->
                FeedItemInformationFragment.newInstance(feedItem, invitationId, 0,false).show(fragmentManager, FeedItemInformationFragment.TAG)
            }
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    @Subscribe
    fun feedItemViewRequested(event: Events.OnFeedItemInfoViewRequestedEvent) {
        val feedItem = event.feedItem
        if (feedItem != null) {
            //Check user photo
            presenter.authenticationController.me?.let { me ->
                if (event.isFromCreate && (me.avatarURL.isNullOrEmpty() || me.avatarURL?.equals("null") != false)) {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.info_photo_profile_title)
                        .setMessage(R.string.info_photo_profile_description)
                        .setNegativeButton(R.string.info_photo_profile_ignore) { dialog,_ ->
                            dialog.dismiss()
                            displayChosenFeedItem(feedItem, isFromCreate = true)
                        }
                        .setPositiveButton(R.string.info_photo_profile_add) { dialog, _ ->
                            dialog.dismiss()
                            val fragment = ChoosePhotoFragment.newInstance()
                            fragment.show(parentFragmentManager, ChoosePhotoFragment.TAG)
                        }
                        .create()
                        .show()
                }
                else {
                    displayChosenFeedItem(feedItem, event.isFromCreate)
                }
            }
        } else {
            //check if we are receiving feed type and id
            val feedItemType = event.feedItemType
            if (feedItemType != 0) {
                val feedItemUUID = event.feedItemUUID
                if (feedItemUUID.isNullOrEmpty()) {
                    event.feedItemShareURL?.let {
                        //display the feed item from URL
                        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
                        presenter.openFeedItemFromShareURL(it, feedItemType)
                    }
                } else {
                    presenter.openFeedItemFromUUID(feedItemUUID, feedItemType, event.invitationId)
                }
            }
        }
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------
    override fun onNetworkException() {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
        ui_home_swipeRefresh?.isRefreshing = false
    }

    override fun onServerException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.server_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
        ui_home_swipeRefresh?.isRefreshing = false
    }

    override fun onTechnicalException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
        ui_home_swipeRefresh?.isRefreshing = false
    }

    fun createAction(newActionGroupType: String) {
        entourageCategory = EntourageCategoryManager.getDefaultCategory(newActionGroupType)
        groupType = BaseEntourage.GROUPTYPE_ACTION
        entourageCategory?.isNewlyCreated = true
        displayEntourageDisclaimer()
    }

    fun createOuting() {
        entourageCategory = null
        groupType = BaseEntourage.GROUPTYPE_OUTING
        displayEntourageDisclaimer()
    }

    fun setGroupType(_groupString:String) {
        groupType = _groupString
    }

    // ----------------------------------
    // UserEditActionZoneFragment.FragmentListener
    // ----------------------------------
    override fun onUserEditActionZoneFragmentDismiss() {}

    override fun onUserEditActionZoneFragmentAddressSaved() {
        presenter.storeActionZoneInfo(false)
    }

    override fun onUserEditActionZoneFragmentIgnore() {
        presenter.storeActionZoneInfo(true)
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EntBus.register(this)
        connection.doBindService()
    }

    override fun onResume() {
        super.onResume()
        EntBus.post(Events.OnLocationPermissionGranted(LocationUtils.isLocationPermissionGranted()))
    }

    override fun onDestroy() {
        EntBus.unregister(this)
        connection.doUnbindService()
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_expert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGroupType(BaseEntourage.GROUPTYPE_ACTION)
        if (fragmentLifecycleCallbacks == null) {
            NewsfeedFragmentLifecycleCallbacks().let {
                fragmentLifecycleCallbacks = it
                activity?.supportFragmentManager?.registerFragmentLifecycleCallbacks(it, false)
            }
        }
        (activity as? MainActivity_v7)?.showEditActionZoneFragment()
        entService?.updateHomefeed(pagination)

        presenter.initializeInvitations()
        if(presenter.checkUserNamesInfo()) {
            InputNamesFragment().show(parentFragmentManager,"InputFGTag")
        }

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_START_EXPERTFEED)

        setupRecyclerView()
    }

    init {
        createEmptyArray()
        presenter = HomeExpertPresenter(this)
    }

    @Subscribe
    fun onGetHomeFeed(response: HomeCard.OnGetHomeFeed) {
        parseFeed(response.responseString)
    }

    private fun createEmptyArray() {
        val card = NewsfeedItem()
        val cards = arrayOf(card,card,card)

        val home1 = HomeCard()
        home1.type = HomeCardType.HEADLINES
        home1.arrayCards = ArrayList()
        home1.arrayCards.addAll(cards)

        val home2 = HomeCard()
        home2.type = HomeCardType.EVENTS
        home2.arrayCards = ArrayList()
        home2.arrayCards.addAll(cards)

        val home3 = HomeCard()
        home3.type = HomeCardType.ACTIONS
        home3.arrayCards = ArrayList()
        home3.arrayCards.addAll(cards)

        arrayEmpty.add(home1)
        arrayEmpty.add(home2)
        arrayEmpty.add(home3)
    }

    private fun setupRecyclerView() {
        val listener = object : HomeViewHolderListener {
            override fun onDetailClicked(item: Any, position: Int, isFromHeadline: Boolean, isAction: Boolean) {
                if (item is Announcement) {
                    val actUrl = item.url ?: return
                    val logString = "${AnalyticsEvents.ACTION_EXPERTFEED_News_Announce}${position+1}"
                    AnalyticsEvents.logEvent(logString)

                    val deeplink = DeepLinksManager.findFirstDeeplinkInText(actUrl)
                    deeplink?.let {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deeplink))
                        requireActivity().startActivity(intent)
                    } ?: run {
                        val uri = Uri.parse(actUrl)
                        val action = if (actUrl.contains("mailto:",true)) Intent.ACTION_SENDTO else Intent.ACTION_VIEW
                        try {
                            requireActivity().startActivity(Intent(action,uri))
                        } catch (e: ActivityNotFoundException) {
                            Timber.e(e)
                        }
                    }
                }
                else if (item is FeedItem) {
                    var logString = if (isFromHeadline) {
                        if (isAction) {
                            AnalyticsEvents.ACTION_EXPERTFEED_News_Action
                        } else {
                            AnalyticsEvents.ACTION_EXPERTFEED_News_Event
                        }
                    } else {
                        if (isAction) {
                            AnalyticsEvents.ACTION_EXPERTFEED_Action
                        } else {
                            AnalyticsEvents.ACTION_EXPERTFEED_Event
                        }
                    }
                    logString += "${position + 1}"
                    AnalyticsEvents.logEvent(logString)

                    feedItemViewRequested(Events.OnFeedItemInfoViewRequestedEvent(item))
                }
            }

            override fun onShowDetail(type: HomeCardType, isArrow:Boolean, subtype: HomeCardType) {
                var logString = ""
                if (type == HomeCardType.ACTIONS) {
                    showActions(true,subtype)
                    logString = if (isArrow) {
                        AnalyticsEvents.ACTION_EXPERTFEED_MoreActionArrow
                    } else {
                        AnalyticsEvents.ACTION_EXPERTFEED_MoreAction
                    }
                }
                else if (type == HomeCardType.EVENTS) {
                    showActions(false, HomeCardType.NONE)
                    logString = if (isArrow) {
                        AnalyticsEvents.ACTION_EXPERTFEED_MoreEventArrow
                    } else {
                        AnalyticsEvents.ACTION_EXPERTFEED_MoreEvent
                    }
                }
                AnalyticsEvents.logEvent(logString)
            }

            override fun onShowChangeZone() {
                val activity = (requireActivity() as? MainActivity_v7) ?: return
                AnalyticsEvents.logEvent(AnalyticsEvents.Event_EXPERTFEED_ModifyActionZone)

                val listener = object : UserEditActionZoneFragment.FragmentListener {
                    override fun onUserEditActionZoneFragmentDismiss() {
                    }

                    override fun onUserEditActionZoneFragmentAddressSaved() {
                        entService?.updateHomefeed(pagination)
                    }

                    override fun onUserEditActionZoneFragmentIgnore() {
                    }
                }

                val userEditActionZoneFragment = UserEditActionZoneFragment.newInstance(null, false)
                userEditActionZoneFragment.setupListener(listener)
                userEditActionZoneFragment.show(activity.supportFragmentManager, UserEditActionZoneFragment.TAG)
            }

            override fun onShowEntourageHelp() {
                val activity = (requireActivity() as? MainActivity_v7) ?: return
                AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_EXPERTFEED_HelpDifferent)

                val homeHelp = HomeHelpFragment()
                homeHelp.show(activity.supportFragmentManager, HomeHelpFragment.TAG)
            }

            override fun onShowChangeMode() {
                (activity as? MainActivity_v7)?.showProfileTab()
            }
        }

        if(adapterHome == null) {
            adapterHome = HomeFeedAdapter(listener).apply {
                this.updateDatas(arrayEmpty,true)
            }
        }
        ui_recyclerview?.layoutManager = LinearLayoutManager(context)
        ui_recyclerview?.adapter = adapterHome

        ui_home_swipeRefresh?.setOnRefreshListener { entService?.updateHomefeed(pagination) }
    }

    fun parseFeed(responseString:String) {
        pagination.isLoading = false
        pagination.isRefreshing = false
        val newFeed = HomeCard.parsingFeed(responseString)

        ui_home_swipeRefresh?.isRefreshing = false

        adapterHome?.updateDatas(newFeed,false)
    }

    fun checkNavigation() {
        if (presenter.isNavigation()) {
            when(presenter.navType()) {
                "action" -> {
                    showActions(true)
                }
                "event" -> {
                    showActions(false)
                }
                else -> {}
            }
        }
    }
    fun checkIntentAction(action: String, extras: Bundle?) {
        if (activity == null) {
            Timber.w("No activity found")
            return
        }
        checkAction(action)
        (extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as? Message)?.content?.let { content ->
            presenter.checkIntentAction(content, action)
        }
    }

    private fun checkAction(action: String) {
        when (action) {
            PlusFragment.KEY_CREATE_CONTRIBUTION -> createAction(BaseEntourage.GROUPTYPE_ACTION_CONTRIBUTION)
            PlusFragment.KEY_CREATE_DEMAND -> createAction(BaseEntourage.GROUPTYPE_ACTION_DEMAND)
            PlusFragment.KEY_CREATE_OUTING -> createOuting()
            else -> {}
        }
    }

    private fun showActions(isAction:Boolean) {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment, NewsFeedActionsFragment.newInstance(isAction, false),
                NewsFeedActionsFragment.TAG)
            addToBackStack(NewsFeedActionsFragment.TAG)
            val navKey = if (isAction) "action" else "event"
            presenter.saveInfo(true,navKey)
        }
    }

    fun showActions(isAction:Boolean,subtype: HomeCardType) {
        val tag = if (isAction) {
            if (subtype == HomeCardType.ACTIONS_ASK) {
                AnalyticsEvents.VIEW_FEEDVIEW_ASKS
            } else {
                AnalyticsEvents.VIEW_FEEDVIEW_CONTRIBS
            }
        }
        else  AnalyticsEvents.VIEW_FEEDVIEW_EVENTS
        AnalyticsEvents.logEvent(tag)

        requireActivity().supportFragmentManager.commit {
            val isExpertAsk = subtype == HomeCardType.ACTIONS_ASK
            val isExpertContrib = subtype == HomeCardType.ACTIONS_CONTRIB

            add(R.id.main_fragment,
                NewsFeedActionsFragment.newInstance(isAction, isExpertAsk, isExpertContrib),
                NewsFeedActionsFragment.TAG
            )
            addToBackStack(NewsFeedActionsFragment.TAG)
            val navKey = if (isAction) "action" else "event"
            presenter.saveInfo(true,navKey)
        }
    }

    //To Handle deeplink for Event
    fun onShowEvents() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWEVENTS)
        showActions(false, HomeCardType.NONE)
    }

    fun onShowAll() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWALL)
        showActions(true, HomeCardType.NONE)
    }

    /*****
     ** Method from NewsFeedFragment for handling closing action/event
     *****/
    @Subscribe
    fun feedItemCloseRequested(event: Events.OnFeedItemCloseRequestEvent) {
        val feedItem = event.feedItem

        // Only the author can close actions
        val myId = EntourageApplication.me(context)?.id
            ?: return
        val author = feedItem.author ?: return
        if (author.userID != myId) {
            return
        }
        if (!feedItem.isClosed()) {
            // close
            stopFeedItem(feedItem, event.isSuccess,event.comment)
        }
    }

    private fun stopFeedItem(feedItem: FeedItem?, success: Boolean, comment:String?) {
        activity?.let { _ ->
            entService?.let { service ->
                if (feedItem != null) {
                    service.stopFeedItem(feedItem, success,comment)
                }
            }
        }
    }

    private inner class ServiceConnection : android.content.ServiceConnection {
        private var isBound = false

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (activity == null) {
                isBound = false
                Timber.e("No activity for service")
                return
            }
            entService = (service as EntService.LocalBinder).service
            entService?.let {
                it.registerApiListener(this@HomeExpertFragment)
                it.updateHomefeed(pagination)
                isBound = true
            } ?: run {
                Timber.e("Service not found")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entService?.unregisterApiListener(this@HomeExpertFragment)
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
            activity?.unbindService(this)
            isBound = false
        }
    }

    companion object{
        const val TAG: String = "social.entourage.android.fragment.home.expert"
        const val countDown = 5000L
    }
}