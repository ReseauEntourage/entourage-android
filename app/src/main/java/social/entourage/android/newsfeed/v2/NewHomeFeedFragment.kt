package social.entourage.android.newsfeed.v2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.commit
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.fragment_new_home_feed.*
import social.entourage.android.EntourageApplication
import social.entourage.android.PlusFragment
import social.entourage.android.R
import social.entourage.android.api.model.BaseEntourage
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.tape.Events
import social.entourage.android.location.EntLocation
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.newsfeed.*
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tour.encounter.CreateEncounterActivity
import timber.log.Timber


class NewHomeFeedFragment : BaseNewsfeedFragment() {

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

        ui_bt_actions?.setOnClickListener {
            showActions(true)
        }

        ui_bt_events?.setOnClickListener {
            showActions(false)
        }

        ui_bt_announces?.setOnClickListener {
            showAnnounces()
        }

        ui_bt_tour?.setOnClickListener {
            showTour()
        }

        ui_bt_old?.setOnClickListener {
            requireActivity().supportFragmentManager.commit {
                add(R.id.main_fragment,NewsFeedFragment(),"homeNew")
                addToBackStack("homeNew")
            }
        }

        if(EntourageApplication.get().me()?.isPro == false) {
            ui_bt_tour?.visibility = View.INVISIBLE
        }
        else {
            ui_bt_tour?.visibility = View.VISIBLE
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
                "announcement" -> {
                    showAnnounces()
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
            add(R.id.main_fragment,NewsFeedActionsFragment.newInstance(isAction),"homeNew")
            addToBackStack("homeNew")
            val navKey = if (isAction) "action" else "event"
            saveInfos(true,navKey)
        }
    }

    fun showAnnounces() {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment,AnnouncementsFeedFragment(),"homeNew")
            addToBackStack("homeNew")
            saveInfos(true,"announcement")
        }
    }

    fun showTour() {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_fragment, ToursFragment(),"homeNew")
            addToBackStack("homeNew")
            saveInfos(true,"tour")
        }
    }

    fun isNavigation() : Boolean {
        val isNav = EntourageApplication.get().sharedPreferences
                .getBoolean("isNavNews", false)

        return isNav
    }

    fun navType() : String? {
        val navType = EntourageApplication.get().sharedPreferences
                .getString("navType", null)

        return navType
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
            "android.intent.action.MAIN" -> {}
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
                it.updateNewsfeed(pagination, selectedTab)
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
}