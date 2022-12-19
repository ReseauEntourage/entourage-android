package social.entourage.android.new_v8

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import social.entourage.android.EntourageApplication
import social.entourage.android.MainActivity_v7
import social.entourage.android.MainPresenter
import social.entourage.android.R
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.Message
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.base.location.EntLocation
import social.entourage.android.old_v7.entourage.information.FeedItemInformationFragment
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.new_v8.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.new_v8.home.UnreadMessages
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity
import social.entourage.android.tools.log.AnalyticsEvents


class MainActivity : BaseSecuredActivity() {

    private lateinit var navController: NavController
    private val presenter: MainPresenter = MainPresenter(MainActivity_v7())

    private lateinit var viewModel: CommunicationHandlerBadgeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[CommunicationHandlerBadgeViewModel::class.java]

        viewModel.badgeCount.observe(this,::handleUpdateBadgeResponse)

        setContentView(R.layout.new_activity_main)
        initializeNavBar()
        initializeMetaData()
        if (authenticationController.isAuthenticated) {
            //refresh the user info from the server
            presenter.updateUserLocation(EntLocation.currentLocation)
            //initialize the push notifications
            initializePushNotifications()
        }
    }

    private fun handleUpdateBadgeResponse(unreadMessages: UnreadMessages) {
        addBadge(unreadMessages.unreadCount ?: 0)
    }

    override fun onResume() {
        super.onResume()

        intent?.action?.let { action ->
            checkIntentAction(action, intent?.extras)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    fun logout() {
        //remove user phone
        val sharedPreferences = EntourageApplication.get().sharedPreferences
        val editor = sharedPreferences.edit()
        authenticationController.me?.let { me ->
            (sharedPreferences.getStringSet(
                EntourageApplication.KEY_TUTORIAL_DONE,
                HashSet()
            ) as HashSet<String?>?)?.let { loggedNumbers ->
                loggedNumbers.remove(me.phone)
                editor.putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers)
            }
        }
        presenter.deleteApplicationInfo()
        editor.remove(EntourageApplication.KEY_REGISTRATION_ID)
        editor.remove(EntourageApplication.KEY_NOTIFICATIONS_ENABLED)
        editor.remove(EntourageApplication.KEY_GEOLOCATION_ENABLED)
        editor.remove(EntourageApplication.KEY_NO_MORE_DEMAND)
        editor.putInt(EntourageApplication.KEY_NB_OF_LAUNCH, 0)
        editor.apply()

        authenticationController.logOutUser()
        EntourageApplication.get(applicationContext).removeAllPushNotifications()
        AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_LOGOUT)
        startActivity(Intent(this, PreOnboardingStartActivity::class.java))
        finish()
    }

    private fun checkIntentAction(action: String, extras: Bundle?) {
        val message = extras?.get(PushNotificationManager.PUSH_MESSAGE) as? Message
        message?.let {
            PushNotificationLinkManager().presentAction(this,supportFragmentManager,message.content?.extra?.instance,message.content?.extra?.instanceId)
        }

        intent = null
    }

    private fun initializePushNotifications() {
        val notificationsEnabled = EntourageApplication.get().sharedPreferences.getBoolean(
            EntourageApplication.KEY_NOTIFICATIONS_ENABLED,
            true
        )
        if (notificationsEnabled) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                presenter.updateApplicationInfo(token)
            }
        } else {
            presenter.deleteApplicationInfo()
        }
    }


    // ----------------------------------
    // PUSH NOTIFICATION HANDLING
    // ----------------------------------
    fun displayMessageOnCurrentEntourageInfoFragment(message: Message): Boolean {
        val fragment =
            supportFragmentManager.findFragmentByTag(FeedItemInformationFragment.TAG) as FeedItemInformationFragment?
        return fragment != null && fragment.onPushNotificationChatMessageReceived(message)
    }

    private fun initializeMetaData() {
        if (MetaDataRepository.metaData.value == null) MetaDataRepository.getMetaData()
        if (MetaDataRepository.groupImages.value == null) MetaDataRepository.getGroupImages()
        if (MetaDataRepository.eventsImages.value == null) MetaDataRepository.getEventsImages()
    }

    private fun initializeNavBar() {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_new_activity_main
        ) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Tabbar_home)
                }
                R.id.navigation_donations -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Tabbar_help)
                }
                R.id.navigation_messages -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Tabbar_messages)
                }
                R.id.navigation_groups -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Tabbar_groups)
                }
                R.id.navigation_events -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Tabbar_events)
                }
            }

            val navController: NavController =
            androidx.navigation.Navigation.findNavController(this, social.entourage.android.R.id.nav_host_fragment_new_activity_main)

            NavigationUI.onNavDestinationSelected(item, navController)
        }
    }

    private fun addBadge(count : Int) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)
        val badge: BadgeDrawable = bottomNavigationView.getOrCreateBadge(
            R.id.navigation_messages)
        badge.number = count
        badge.isVisible = true
        badge.maxCharacterCount = 2
        badge.verticalOffsetWithText = 10
        badge.backgroundColor = resources.getColor(R.color.light_orange)
        badge.badgeTextColor = resources.getColor(R.color.white)
        if (count == 0) {
            bottomNavigationView.removeBadge(R.id.navigation_messages)
        }
    }
}