package social.entourage.android.home.expert

import android.content.*
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_home_expert.*
import social.entourage.android.*
import social.entourage.android.api.ApiConnectionListener
import social.entourage.android.api.model.*
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.base.BackPressable
import social.entourage.android.base.BaseFragment
import social.entourage.android.base.location.EntLocation
import social.entourage.android.base.location.LocationUtils
import social.entourage.android.base.newsfeed.*
import social.entourage.android.configuration.Configuration
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.entourage.category.EntourageCategory
import social.entourage.android.entourage.category.EntourageCategoryManager
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.home.*
import social.entourage.android.home.actions.NewsFeedActionsFragment
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
import kotlin.collections.ArrayList

enum class VariantCellType {
    Original,
    VariantA,
    VariantB
}

class HomeExpertFragment : BaseFragment(), BackPressable, ApiConnectionListener, UserEditActionZoneFragment.FragmentListener {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Inject
    lateinit var presenter: HomeExpertPresenter
    private var entService: EntService? = null
    private var isStopped = false

    private var adapterHome: HomeFeedAdapter? = null
    //pagination
    private var pagination = NewsfeedPagination()

    // keeps tracks of the attached fragments
    private var fragmentLifecycleCallbacks: NewsfeedFragmentLifecycleCallbacks? = null

    // requested group type
    private lateinit var groupType: String

    // requested entourage category
    private var entourageCategory: EntourageCategory? = null

    //val userId = presenter.authenticationController.me?.id ?:0

    private val connection = ServiceConnection()
    private var arrayEmpty = ArrayList<HomeCard>()
    var variantType: VariantCellType = VariantCellType.Original
    val remoteConfig = Firebase.remoteConfig

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onStart() {
        super.onStart()
        if (!LocationUtils.isLocationEnabled() && !LocationUtils.isLocationPermissionGranted()) {
            (activity as? MainActivity)?.showEditActionZoneFragment(this,false)
        }
        isStopped = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        isStopped = true
    }

    override fun onBackPressed(): Boolean {
        //before closing the fragment, send the cached tour points to server (if applicable)
        entService?.updateOngoingTour()
        return false
    }

    // ----------------------------------
    // PUBLIC METHODS
    // ----------------------------------
    private fun displayChosenFeedItem(feedItemUUID: String, feedItemType: Int, invitationId: Long = 0) {
        //display the feed item
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItemFromUUID(feedItemUUID, feedItemType, invitationId)
    }

    private fun displayChosenFeedItem(feedItem: FeedItem, feedRank: Int) {
        displayChosenFeedItem(feedItem, 0, feedRank)
    }

    private fun displayChosenFeedItem(feedItem: FeedItem, invitationId: Long, feedRank: Int = 0) {
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
        presenter.openFeedItem(feedItem, invitationId, feedRank)
    }

    private fun displayChosenFeedItemFromShareURL(feedItemShareURL: String, feedItemType: Int) {
        //display the feed item
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_FEED_OPEN_ENTOURAGE)
        presenter.openFeedItemFromShareURL(feedItemShareURL, feedItemType)
    }

    private fun displayEntourageDisclaimer() {
        // Check if we need to show the entourage disclaimer
        if (Configuration.showEntourageDisclaimer()) {
            presenter.displayEntourageDisclaimer(groupType)
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
        presenter.createEntourage(location, groupType, entourageCategory)
    }

    // ----------------------------------
    // BUS LISTENERS : don't susbcribe here but in children !
    // ----------------------------------
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
                            displayChosenFeedItem(feedItem, event.getfeedRank())
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
                    displayChosenFeedItem(feedItem, event.getfeedRank())
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
    }

    override fun onServerException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.server_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

    override fun onTechnicalException(throwable: Throwable) {
        activity?.window?.decorView?.rootView?.let {
            EntSnackbar.make(it, R.string.technical_error, Snackbar.LENGTH_LONG).show()
        }
        if (pagination.isLoading) {
            pagination.isLoading = false
            pagination.isRefreshing = false
        }
    }

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

    private fun setGroupType(_groupString:String) {
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

    override fun onAttach(context: Context) {
        setupComponent(EntourageApplication.get(activity).components)
        super.onAttach(context)
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
        (activity as? MainActivity)?.showEditActionZoneFragment()

        presenter.checkUserNamesInfo()

        createEmptyArray()

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_START_EXPERTFEED)

        ui_bt_tour?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_EXPERTFEED_Tour)
            showTour()
        }

        ui_bt_tour?.visibility = if(EntourageApplication.get().me()?.isPro == false) View.INVISIBLE else View.VISIBLE

        val type = remoteConfig.getLong("cell_home_expert_type")
        if (type == 0L) {
            variantType = VariantCellType.Original
        }
        else if (type == 1L) {
            variantType = VariantCellType.VariantA
        }
        else if (type == 2L) {
            variantType = VariantCellType.VariantB
        }
        // setupRecyclerView()

        setupTesting()
    }

    fun setupTesting() {

        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0//60 * 60 * 24 //TODO remettre les bonnes valuers après tests preprod
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        val defaults = HashMap<String, Any>()
        defaults.put("cell_home_expert_type",0)
        remoteConfig.setDefaultsAsync(defaults)

        remoteConfig.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val type = remoteConfig.getLong("cell_home_expert_type")
                        if (type == 0L) {
                            variantType = VariantCellType.Original
                        }
                        else if (type == 1L) {
                            variantType = VariantCellType.VariantA
                        }
                        else if (type == 2L) {
                            variantType = VariantCellType.VariantB
                        }
                        setupRecyclerView()
                        entService?.updateHomefeed(pagination)
                    } else {
                        Timber.d("Fetch failed")
                    }
                }
    }

    override fun onResume() {
        super.onResume()
        // entService?.updateHomefeed(pagination)
    }

    private fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerHomeExpertComponent.builder()
            .entourageComponent(entourageComponent)
            .homeExpertModule(HomeExpertModule(this))
            .build()
            .inject(this)
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
                        var _action = Intent.ACTION_VIEW
                        if (actUrl.contains("mailto:",true)) {_action = Intent.ACTION_SENDTO }
                        val intent = Intent(_action,uri)
                        try {
                            requireActivity().startActivity(intent)
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

                    if (!isFromHeadline) {
                        // Use for AB Testing precision variant Analytics
                        var tagAB = ""
                        when( variantType) {
                            VariantCellType.Original -> tagAB = "Action__ExpertFeed__Show_O"
                            VariantCellType.VariantA -> tagAB = "Action__ExpertFeed__Show_A"
                            VariantCellType.VariantB -> tagAB = "Action__ExpertFeed__Show_B"
                        }
                        AnalyticsEvents.logEvent("Action__ExpertFeed__Show") //Use for AB Testing tracking
                        AnalyticsEvents.logEvent(tagAB)
                    }

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
                val activity = (requireActivity() as? MainActivity) ?: return
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
                val activity = (requireActivity() as? MainActivity) ?: return
                AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_EXPERTFEED_HelpDifferent)

                val homeHelp = HomeHelpFragment()
                homeHelp.show(activity.supportFragmentManager, HomeHelpFragment.TAG)
            }

            override fun onShowChangeMode() {
                (activity as? MainActivity)?.showProfileTab()
            }
        }

        adapterHome = HomeFeedAdapter(variantType,listener)
        ui_recyclerview?.layoutManager = LinearLayoutManager(context)
        ui_recyclerview?.adapter = adapterHome

        EntourageApplication.me(activity)?.let { user ->
            var isNeighbour = false
            if (user.isUserTypeNeighbour) {
                isNeighbour = true
            }
            adapterHome?.updateDatas(arrayEmpty,isNeighbour,true,variantType)
        } ?: run { adapterHome?.updateDatas(arrayEmpty,false,true,variantType) }
        ui_home_swipeRefresh?.setOnRefreshListener { entService?.updateHomefeed(pagination) }
    }

    fun parseFeed(responseString:String) {
        pagination.isLoading = false
        pagination.isRefreshing = false
        val _arrayTest = HomeCard.parsingFeed(responseString)

        ui_home_swipeRefresh?.isRefreshing = false

        EntourageApplication.me(activity)?.let { user ->
            var isNeighbour = false
            if (user.isUserTypeNeighbour) {
                isNeighbour = true
            }
            adapterHome?.updateDatas(_arrayTest,isNeighbour,false,variantType)
        } ?: run { adapterHome?.updateDatas(_arrayTest,false,false,variantType) }
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
                NewsFeedActionsFragment.newInstance(isAction, false, isExpertAsk, isExpertContrib),
                NewsFeedActionsFragment.TAG
            )
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

    //To Handle deeplink for Event
    fun onShowEvents() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWEVENTS)
        showActions(false, HomeCardType.NONE)
    }

    fun onShowAll() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWALL)
        showActions(true, HomeCardType.NONE)
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
    }
}