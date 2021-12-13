package social.entourage.android.home

import android.content.*
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_home.*
import social.entourage.android.*
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.HomeTourArea
import social.entourage.android.api.model.*
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events
import social.entourage.android.base.BackPressable
import social.entourage.android.base.BaseFragment
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.base.newsfeed.*
import social.entourage.android.configuration.Configuration
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.entourage.create.BaseCreateEntourageFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.home.actions.NewsFeedActionsFragment
import social.entourage.android.home.expert.HomeExpertFragment
import social.entourage.android.home.neo.*
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.view.EntSnackbar
import social.entourage.android.tour.ToursFragment
import social.entourage.android.user.edit.photo.ChoosePhotoFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import android.os.CountDownTimer

class HomeFragment : BaseFragment(), ApiConnectionListener, UserEditActionZoneFragment.FragmentListener, BackPressable {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Inject lateinit var presenter: HomePresenter
    private var entService: EntService? = null

    // requested entourage category
    private var entourageCategory: EntourageCategory? = null

    private var isFromNeo = false
    private var tagNameAnalytic = ""

    private val connection = ServiceConnection()
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EntBus.register(this)
        connection.doBindService()
    }

    override fun onDestroy() {
        connection.doUnbindService()
        EntBus.unregister(this)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onAttach(context: Context) {
        setupComponent(EntourageApplication.get(activity).components)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setGroupType(BaseEntourage.GROUPTYPE_ACTION)
        (activity as? MainActivity)?.showEditActionZoneFragment()

        presenter.initializeInvitations()
        presenter.checkUserNamesInfos()

        var isExpertMode:Boolean

        val hasExportKey = EntourageApplication.get().sharedPreferences.contains(EntourageApplication.KEY_HOME_IS_EXPERTMODE)

        if (hasExportKey) {
            isExpertMode = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_HOME_IS_EXPERTMODE,false)
        }
        else {
            isExpertMode = false
            EntourageApplication.me(activity)?.let { user ->
                isExpertMode = false
                if (user.isUserTypeNeighbour) {
                    if (user.isEngaged) {
                        isExpertMode = true
                    }
                }
                EntourageApplication.get().sharedPreferences.edit()
                        .putBoolean(EntourageApplication.KEY_HOME_IS_EXPERTMODE, isExpertMode)
                        .remove("isNavNews")
                        .remove("navType")
                        .apply()
            }
        }

        EntourageApplication.me(activity)?.let { user ->
            if (!user.isUserTypeNeighbour) {
                isExpertMode = true
            }
        }

        val fragmentChild = if (isExpertMode) {
            HomeExpertFragment()
        } else {
            HomeNeoMainFragment()
        }

        childFragmentManager.beginTransaction()
            .replace(R.id.ui_container, fragmentChild)
            .commit()
    }

    override fun onStart() {
        super.onStart()
        if (!LocationUtils.isLocationEnabled() && !LocationUtils.isLocationPermissionGranted()) {
            (activity as? MainActivity)?.showEditActionZoneFragment(this,false)
        }
    }

    override fun onResume() {
        super.onResume()
        EntBus.post(Events.OnLocationPermissionGranted(LocationUtils.isLocationPermissionGranted()))
    }



    // ----------------------------------
    // PRIVATE METHODS (lifecycle)
    // ----------------------------------
    private fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerHomeComponent.builder()
            .entourageComponent(entourageComponent)
            .homeModule(HomeModule(this))
            .build()
            .inject(this)
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    private fun displayChosenFeedItem(feedItemUUID: String, feedItemType: Int, invitationId: Long = 0) {
        //display the feed item
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItemFromUUID(feedItemUUID, feedItemType, invitationId)
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
            presenter.openFeedItem(feedItem, 0 , 0,false)
            return
        }
        feedItemTemporary = feedItem

        updatePopCreateAndShow()
    }

    fun updatePopCreateAndShow() {
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
        popInfoCreateEntourageFragment?.homeFragment = this
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
        feedItemTemporary?.let { presenter.openFeedItem(it, 0 , 0,true) }
    }

    private fun displayChosenFeedItemFromShareURL(feedItemShareURL: String, feedItemType: Int) {
        //display the feed item
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItemFromShareURL(feedItemShareURL, feedItemType)
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
        createEntourage(location, groupType, entourageCategory,isFromNeo,tagNameAnalytic)
    }

    // ----------------------------------
    // SERVICE INTERFACE METHODS
    // ----------------------------------
    override fun onNetworkException() {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.network_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onServerException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.server_error, Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
    }

    fun createAction(newGroupType: String, newActionGroupType: String) {
        entourageCategory = EntourageCategoryManager.getDefaultCategory(newActionGroupType)
        groupType = newGroupType
        entourageCategory?.isNewlyCreated = true
        displayEntourageDisclaimer()
    }

    private fun createActionFromNeo(newGroupType: String, newActionGroupType: String, newActionType:String, tagNameAnalytic:String) {
        entourageCategory = EntourageCategoryManager.findCategory(newActionGroupType,newActionType)
        groupType = newGroupType
        entourageCategory?.isNewlyCreated = true
        isFromNeo = true
        this.tagNameAnalytic = tagNameAnalytic
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

    // Home Neo navigation

    fun goActions() {
        val fg = HomeNeoActionFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeNeoActionFragment.TAG)
        transaction.add(R.id.ui_container, fg).commit()
    }

    private fun createEntourage(location: LatLng?, groupType: String, category: EntourageCategory?, isFromNeo:Boolean, tagAnalyticName:String) {
        if (!isStateSaved) {
            val fragmentManager = activity?.supportFragmentManager ?: return
            if(isFromNeo) {
                BaseCreateEntourageFragment.newNeoInstance(location, groupType, category,tagAnalyticName).show(fragmentManager, BaseCreateEntourageFragment.TAG)
            } else {
                BaseCreateEntourageFragment.newExpertInstance(location, groupType, category).show(fragmentManager, BaseCreateEntourageFragment.TAG)
            }
        }
    }

    private fun displayEntourageDisclaimer(groupType: String) {
        if (!isStateSaved) {
            val fragmentManager = activity?.supportFragmentManager ?:return
            EntourageDisclaimerFragment.newInstance(groupType,"",false).show(fragmentManager, EntourageDisclaimerFragment.TAG)
        }
    }

    private fun displayEntourageDisclaimer(groupType: String, tagAnalyticName:String, isFromNeo: Boolean) {
        if (!isStateSaved) {
            val fragmentManager = activity?.supportFragmentManager ?:return
            EntourageDisclaimerFragment.newInstance(groupType,tagAnalyticName,isFromNeo).show(fragmentManager, EntourageDisclaimerFragment.TAG)
        }
    }

    //Actions call from fg
    fun createAction2(newGroupType: String, newActionGroupType: String, newActionType:String,tagNameAnalytic:String) {
        createActionFromNeo(newGroupType,newActionGroupType,newActionType,tagNameAnalytic)
        displayEntourageDisclaimer(newGroupType,tagNameAnalytic,true)
    }

    fun goDetailActions() {
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_FEEDVIEW_ASKS)
        val frag = NewsFeedActionsFragment.newInstance(isAction = true, isFromNeo = true)
        requireActivity().supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
            add(R.id.main_fragment, frag, NewsFeedActionsFragment.TAG)
            addToBackStack(NewsFeedActionsFragment.TAG)
        }
    }

    fun goDetailEvents() {
        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_FEEDVIEW_EVENTS)
        val frag = NewsFeedActionsFragment.newInstance(isAction = false, isFromNeo = true)
        requireActivity().supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
            add(R.id.main_fragment,frag , NewsFeedActionsFragment.TAG)
            addToBackStack(NewsFeedActionsFragment.TAG)
        }
    }

    fun goHelp() {
        val fg = HomeNeoHelpFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeNeoHelpFragment.TAG)
        transaction.add(R.id.ui_container, fg).commit()
    }

    fun goStreet() {
        val fg = HomeNeoStreetFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeNeoStreetFragment.TAG)
        transaction.add(R.id.ui_container, fg).commit()
    }

    fun showWebLink(slug:String) {
        (activity as MainActivity).showWebViewForLinkId(slug)
    }

    fun goTourStart() {
        val fg = HomeNeoTourStartFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeNeoTourStartFragment.TAG)
        transaction.add(R.id.ui_container, fg).commit()
    }

    fun goTourList() {
        val fg = HomeNeoTourListFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeNeoTourListFragment.TAG)
        transaction.add(R.id.ui_container, fg).commit()
    }

    fun goTourSend(tourArea: HomeTourArea) {
        val fg = HomeNeoTourSendFragment.newInstance(tourArea)
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeNeoTourSendFragment.TAG)
        transaction.add(R.id.ui_container, fg).commit()
    }

    override fun onBackPressed(): Boolean {
        //before closing the fragment, send the cached tour points to server (if applicable)
        entService?.updateOngoingTour()

        if (childFragmentManager.fragments.size == 1) return false
        childFragmentManager.popBackStackImmediate()
        return true
    }

    @Subscribe
    fun feedItemViewRequested(event: Events.OnFeedItemInfoViewRequestedEvent) {
        if (isFromNeo) {
            goDetailActions()
            return
        }
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
                            displayChosenFeedItem(feedItem, event.getfeedRank(),true)
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
                    displayChosenFeedItem(feedItem, event.getfeedRank(),event.isFromCreate)
                }
            }
        } else {
            //check if we are receiving feed type and id
            val feedItemType = event.feedItemType
            if (feedItemType != 0) {
                val feedItemUUID = event.feedItemUUID
                if (feedItemUUID.isNullOrEmpty()) {
                    event.feedItemShareURL?.let { displayChosenFeedItemFromShareURL(it, feedItemType) }
                } else {
                    displayChosenFeedItem(feedItemUUID, feedItemType, event.invitationId)
                }
            }
        }
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
                "tour" -> {
                    showTour()
                }
                else -> {}
            }
        }
    }

    fun showActions(isAction:Boolean) {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment, NewsFeedActionsFragment.newInstance(isAction, false),
                NewsFeedActionsFragment.TAG)
            addToBackStack(NewsFeedActionsFragment.TAG)
            val navKey = if (isAction) "action" else "event"
            presenter.saveInfo(true,navKey)
        }
    }

    private fun showTour() {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment, ToursFragment(),ToursFragment.TAG)
            addToBackStack(ToursFragment.TAG)
            presenter.saveInfo(true,"tour")
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
                    presenter.saveInfo(true,"tour")

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
            stopFeedItem(feedItem, event.isSuccess)
        } else {
            (feedItem as? Tour)?.let { tour ->
                if (!tour.isFreezed()) {
                    freezeTour(tour)
                }
            }
        }
    }

    fun stopFeedItem(feedItem: FeedItem?, success: Boolean) {
        activity?.let { activity ->
            entService?.let { service ->
                if (feedItem != null
                        && (!service.isRunning
                                || feedItem.type != TimestampedObject.TOUR_CARD
                                || service.currentTourId.equals(feedItem.uuid, ignoreCase = true))) {
                    service.stopFeedItem(feedItem, success)
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
                it.registerApiListener(this@HomeFragment)
                isBound = true
            } ?: run {
                Timber.e("Service not found")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entService?.unregisterApiListener(this@HomeFragment)
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

    companion object {
        const val TAG = "social.entourage.android.fragment_home"
    }
}