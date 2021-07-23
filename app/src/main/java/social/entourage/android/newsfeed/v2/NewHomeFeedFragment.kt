package social.entourage.android.newsfeed.v2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.squareup.otto.Subscribe
import social.entourage.android.*
import social.entourage.android.api.HomeTourArea
import social.entourage.android.api.model.*
import social.entourage.android.api.model.Message
import social.entourage.android.api.tape.Events
import social.entourage.android.base.BackPressable
import social.entourage.android.location.EntLocation
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.newsfeed.*
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tour.encounter.CreateEncounterActivity
import timber.log.Timber


class NewHomeFeedFragment : BaseNewsfeedFragment(), BackPressable {

    private val connection = ServiceConnection()
    private var currentTourUUID = ""
    var isTourPostSend = false


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
        return inflater.inflate(R.layout.fragment_new_home_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentChild:Fragment
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

        if (isExpertMode) {
            fragmentChild = HomeExpertFragment()
        }
        else {
            fragmentChild = HomeNeoMainFragment()
        }

        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(R.id.ui_container, fragmentChild).commit()
    }

    // Home Neo navigation

    fun goActions() {
        val fg = HomeNeoActionFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeNeoActionFragment.TAG)
        transaction.add(R.id.ui_container, fg).commit()
    }

    //Actions call from fg
    fun createAction2(newGroupType: String, newActionGroupType: String, newActionType:String,tagNameAnalytic:String) {
        createActionFromNeo(newGroupType,newActionGroupType,newActionType,tagNameAnalytic)
        presenter.displayEntourageDisclaimer(newGroupType,tagNameAnalytic,true)
    }

    fun goDetailActions() {
        val frag = NewsFeedActionsFragment.newInstance(true,true)
        requireActivity().supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
            add(R.id.main_fragment, frag,"homeNew")
            addToBackStack("homeNew")
        }
    }

    fun goDetailEvents() {
        val frag = NewsFeedActionsFragment.newInstance(false,true)
        requireActivity().supportFragmentManager.commit {
            setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
            add(R.id.main_fragment,frag ,"homeNew")
            addToBackStack("homeNew")
        }
    }

    fun goHelp() {
        val fg = HomeNeoHelpFragment()
        val transaction = childFragmentManager.beginTransaction()
        transaction.setCustomAnimations(R.anim.slide_in_from_right,R.anim.slide_in_from_right)
        transaction.addToBackStack(HomeHelpFragment.TAG)
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
        if (childFragmentManager.fragments.size == 1) return false
        childFragmentManager.popBackStackImmediate()
        return true
    }

    @Subscribe
    override fun feedItemViewRequested(event: Events.OnFeedItemInfoViewRequestedEvent) {
        if (isFromNeo) {
            goDetailActions()
        }
        else {
            super.feedItemViewRequested(event)
        }
    }

    fun checkNavigation() {
        if (isNavigation()) {
            when(navType()) {
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
            add(R.id.main_fragment,NewsFeedActionsFragment.newInstance(isAction,false),"homeNew")
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

    fun isNavigation(): Boolean {
        return EntourageApplication.get().sharedPreferences
            .getBoolean("isNavNews", false)
    }

    fun navType(): String? {
        return EntourageApplication.get().sharedPreferences
            .getString("navType", null)
    }

    fun saveInfos(isNav:Boolean,type:String?) {
        val editor = EntourageApplication.get().sharedPreferences.edit()
        editor.putBoolean("isNavNews",isNav)
        editor.putString("navType",type)
        editor.apply()
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

    fun checkAction(action: String) {
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
                    add(R.id.main_fragment, frag,"homeNew")
                    addToBackStack("homeNew")
                    saveInfos(true,"tour")

                    isTourPostSend = true
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({
                        EntBus.post(Events.OnCheckIntentActionEvent(action, null))
                        val _handler = Handler(Looper.getMainLooper())
                        _handler.postDelayed({
                            isTourPostSend = false
                        }, 2000)
                    }, 1000)
                }
            }
        }
    }

    //To Handle deeplink for Event
    override fun onShowEvents() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWEVENTS)
        showActions(false)
    }

    override fun onShowAll() {
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_FEED_SHOWALL)
        showActions(true)
    }

    /*****
     ** Methods & service for Tour add Encounter
    *****/
    override fun onAddEncounter() {
        if (presenter.shouldDisplayEncounterDisclaimer()) {
            presenter.displayEncounterDisclaimer()
        } else {
            addEncounter()
        }
    }

    override fun addEncounter() {
        if (activity != null) {
            if (currentTourUUID.equals("")) {
                entService?.let { currentTourUUID = it.currentTourId }
            }
            saveCameraPosition()
            val args = Bundle()
            args.putString(CreateEncounterActivity.BUNDLE_KEY_TOUR_ID, currentTourUUID)
            val encounterPosition  = longTapCoordinates ?: EntLocation.currentLatLng ?: EntLocation.lastCameraPosition.target
            args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LATITUDE, encounterPosition.latitude)
            args.putDouble(CreateEncounterActivity.BUNDLE_KEY_LONGITUDE, encounterPosition.longitude)
            longTapCoordinates = null
            val intent = Intent(activity, CreateEncounterActivity::class.java)
            intent.putExtras(args)
            startActivity(intent)

            // show the disclaimer only once per tour
            presenter.setDisplayEncounterDisclaimer(false)
        }
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
                it.registerServiceListener(this@NewHomeFeedFragment)
                it.registerApiListener(this@NewHomeFeedFragment)
                updateFragmentFromService()
                it.updateHomefeed(pagination)
                isBound = true
            } ?: run {
                Timber.e("Service not found")
                isBound = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            entService?.unregisterServiceListener(this@NewHomeFeedFragment)
            entService?.unregisterApiListener(this@NewHomeFeedFragment)
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
            entService?.unregisterServiceListener(this@NewHomeFeedFragment)
            activity?.unbindService(this)
            isBound = false
        }
    }

    companion object {
        const val TAG = "social.entourage.android.fragment_home"
    }
}