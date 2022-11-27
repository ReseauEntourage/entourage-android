package social.entourage.android.new_v8

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.new_v8.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.new_v8.home.UnreadMessages

class MainActivity : BaseSecuredActivity() {

    private lateinit var navController: NavController
    private val presenter: MainPresenter = MainPresenter(MainActivity_v7())

    private lateinit var viewModel: CommunicationHandlerBadgeViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this)[CommunicationHandlerBadgeViewModel::class.java]

        viewModel.badgeCount.observe(this,::handleupdateBadgeResponse)

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

    private fun handleupdateBadgeResponse(unreadMessages: UnreadMessages) {
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

    fun checkIntentAction(action: String, extras: Bundle?) {
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