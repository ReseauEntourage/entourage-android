package social.entourage.android.newsfeed.v2

import android.content.*
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import androidx.recyclerview.widget.LinearLayoutManager
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_home_expert.*
import kotlinx.android.synthetic.main.fragment_map.*
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.api.model.EntourageUser
import social.entourage.android.api.model.feed.Announcement
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.feed.NewsfeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.deeplinks.DeepLinksManager
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.service.EntService
import social.entourage.android.service.EntourageServiceListener
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import timber.log.Timber

class HomeExpertFragment : BaseNewsfeedFragment(), EntourageServiceListener {

    private val connection = ServiceConnection()
    private var currentTourUUID = ""
    var isTourPostSend = false

    var adapterHome: NewHomeFeedAdapter? = null
    var arrayEmpty = ArrayList<HomeCard>()

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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home_expert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        createEmptyArray()

        AnalyticsEvents.logEvent(AnalyticsEvents.VIEW_START_EXPERTFEED)

        ui_bt_tour?.setOnClickListener {
            AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_EXPERTFEED_Tour)
            showTour()
        }

        ui_bt_tour?.visibility = if(EntourageApplication.get().me()?.isPro == false) View.INVISIBLE else View.VISIBLE

        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()
        entService?.updateHomefeed(pagination)
    }

    @Subscribe
    fun OnGetHomeFeed(response:HomeCard.OnGetHomeFeed) {
        parseFeed(response.responseString)
    }

    //To intercept eventbus info for event/action close
    @Subscribe
    override fun feedItemCloseRequested(event: Events.OnFeedItemCloseRequestEvent) {
        super.feedItemCloseRequested(event)
    }

    override fun onFeedItemClosed(closed: Boolean, updatedFeedItem: FeedItem) {
        loaderStop?.dismiss()
        loaderStop = null
    }

    override fun onUserStatusChanged(user: EntourageUser, updatedFeedItem: FeedItem) {
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
        val listener = object : HomeViewHolderListener{
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

                    feedItemViewRequested(Events.OnFeedItemInfoViewRequestedEvent(item))
                }
            }

            override fun onShowDetail(type: HomeCardType,isArrow:Boolean,subtype:HomeCardType) {
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
                    showActions(false,HomeCardType.NONE)
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
                homeHelp.show(activity.supportFragmentManager,HomeHelpFragment.TAG)
            }

            override fun onShowChangeMode() {
                (activity as? MainActivity)?.showProfileTab()
            }
        }

        adapterHome = NewHomeFeedAdapter(listener)
        ui_recyclerview?.layoutManager = LinearLayoutManager(context)
        ui_recyclerview?.adapter = adapterHome

        EntourageApplication.me(activity)?.let { user ->
            var isNeighbour = false
            if (user.isUserTypeNeighbour) {
                isNeighbour = true
            }
            adapterHome?.updateDatas(arrayEmpty,isNeighbour,true)
        } ?: run { adapterHome?.updateDatas(arrayEmpty,false,true) }
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
            adapterHome?.updateDatas(_arrayTest,isNeighbour,false)
        } ?: run { adapterHome?.updateDatas(_arrayTest,false,false) }
    }

//    fun checkNavigation() {
//        if (isNavigation()) {
//            when(navType()) {
//                "action" -> {
//                    showActions(true)
//                }
//                "event" -> {
//                    showActions(false)
//                }
////                "announcement" -> {
////                    showAnnounces()
////                }
//                "tour" -> {
//                    showTour()
//                }
//                else -> {}
//            }
//        }
//    }

    fun showActions(isAction:Boolean,subtype:HomeCardType) {
        var tag = AnalyticsEvents.VIEW_FEEDVIEW_EVENTS
        if (isAction) {
            if (subtype == HomeCardType.ACTIONS_ASK) {
               tag = AnalyticsEvents.VIEW_FEEDVIEW_ASKS
            }
            else {
                tag = AnalyticsEvents.VIEW_FEEDVIEW_CONTRIBS
            }
        }
        AnalyticsEvents.logEvent(tag)

        requireActivity().supportFragmentManager.commit {
            val isExpertAsk = if(subtype == HomeCardType.ACTIONS_ASK) true else false
            val isExpertContrib = if(subtype == HomeCardType.ACTIONS_CONTRIB) true else false

            add(R.id.main_fragment,NewsFeedActionsFragment.newInstance(isAction,false,isExpertAsk,isExpertContrib),"homeNew")
            addToBackStack("homeNew")
            val navKey = if (isAction) "action" else "event"
            saveInfos(true,navKey)
        }
    }

    fun showTour() {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment, ToursFragment(),"homeNew")
            addToBackStack("homeNew")
            saveInfos(true,"tour")
        }
    }

//    fun isNavigation() : Boolean {
//        val isNav = EntourageApplication.get().sharedPreferences
//                .getBoolean("isNavNews", false)
//
//        return isNav
//    }
//
//    fun navType() : String? {
//        val navType = EntourageApplication.get().sharedPreferences
//                .getString("navType", null)
//
//        return navType
//    }

    fun saveInfos(isNav:Boolean,type:String?) {
        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",isNav)
        editor.putString("navType",type)
        editor.apply()
    }

    //To Handle deeplink for Event
    fun onShowEvents() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWEVENTS)
        showActions(false,HomeCardType.NONE)
    }

    fun onShowAll() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWALL)
        showActions(true,HomeCardType.NONE)
    }

    fun updateFragmentFromService() {
        entService?.let {
            if (it.isRunning) {
                updateFloatingMenuOptions()
                currentTourUUID = it.currentTourId
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
                it.registerServiceListener(this@HomeExpertFragment)
                it.registerApiListener(this@HomeExpertFragment)
                updateFragmentFromService()
                it.updateHomefeed(pagination)
                isBound = true
            } ?: run {
                Timber.e("Service not found")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entService?.unregisterServiceListener(this@HomeExpertFragment)
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
            entService?.unregisterServiceListener(this@HomeExpertFragment)
            activity?.unbindService(this)
            isBound = false
        }
    }
}