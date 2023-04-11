package social.entourage.android

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.base.location.EntLocation
import social.entourage.android.deeplinks.UniversalLinkManager
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.notifications.PushNotificationManager
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.notifications.NotificationActionManager
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

class MainActivity : BaseSecuredActivity() {
    private lateinit var navController: NavController
    private val presenter: MainPresenter = MainPresenter(this)

    private lateinit var viewModel: CommunicationHandlerBadgeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_main)




        viewModel = ViewModelProvider(this)[CommunicationHandlerBadgeViewModel::class.java]

        viewModel.badgeCount.observe(this,::handleUpdateBadgeResponse)

        initializeNavBar()
        if (authenticationController.isAuthenticated) {
            //refresh the user info from the server
            presenter.updateUserLocation(EntLocation.currentLocation)
            //initialize the push notifications
            initializePushNotifications()
            updateAnalyticsInfo()
            //TODO authenticationController.me?.unreadCount?.let { bottomBar?.updateBadgeCountForUser(it) }
        }

        //UniversalLinkHandler
        lifecycleScope.launch(Dispatchers.Main) {
            handleUniversalLinkFromMain()
        }
    }

    override fun onStart() {
        presenter.checkForUpdate(this)
        super.onStart()
    }

    suspend fun handleUniversalLinkFromMain(){
        val uri = intent?.data
        if (uri != null) {
            UniversalLinkManager.handleUniversalLink(this, uri)
        }
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.HOME.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.GROUP.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.OUTING.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.OUTINGS_LIST.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.MESSAGE.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.NEW_CONTRIBUTION.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.NEW_SOLICITATION.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.CONTRIBUTIONS_LIST.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.SOLICITATIONS_LIST.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.CONTRIBUTION_DETAIL.link))
        //UniversalLinkManager.handleUniversalLink(this , Uri.parse(EntourageLink.SOLICITATION_DETAIL.link))
    }

    fun displayAppUpdateDialog() {
        val builder = AlertDialog.Builder(this)
        val dialog = builder.setView(R.layout.layout_dialog_version_update)
            .setCancelable(false)
            .create()
        dialog.show()
        val updateButton = dialog.findViewById<Button>(R.id.update_dialog_button)
        updateButton?.setOnClickListener {
            try {
                val uri = Uri.parse(getString(R.string.market_url, packageName))
                startActivity(Intent(Intent.ACTION_VIEW, uri))
            } catch (e: Exception) {
                Toast.makeText(
                    this,
                    R.string.error_google_play_store_not_installed,
                    Toast.LENGTH_SHORT
                ).show()
                dialog.cancel()
            }
        }
    }

    private fun handleUpdateBadgeResponse(unreadMessages: UnreadMessages) {
        addBadge(unreadMessages.unreadCount ?: 0)
    }

    override fun onResume() {
        super.onResume()
        initializeMetaData()
        //TODO bottomBar?.refreshBadgeCount()
        intent?.action?.let { action ->
            checkIntentAction(action, intent?.extras)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
    }

    private fun updateAnalyticsInfo() {
        authenticationController.me?.let { user ->
            AnalyticsEvents.updateUserInfo(
                user,
                applicationContext,
                NotificationManagerCompat.from(this).areNotificationsEnabled()
            )
        }
    }

    private fun checkIntentAction(action: String, extras: Bundle?) {
        val pushNotificationMessage = extras?.get(PushNotificationManager.PUSH_MESSAGE) as? PushNotificationMessage
        pushNotificationMessage?.content?.extra?.let { extra ->
            extra.instance?.let { instance ->
                extra.instanceId?.let { id ->
                    NotificationActionManager.presentAction(this, supportFragmentManager, instance, id, extra.postId)
                }
            }
        }

        intent = null
    }

    private val requestNotificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            //initializePushNotifications()
        }

    private fun initializePushNotifications() {
        val sharedPref = EntourageApplication.get().sharedPreferences
        if(!sharedPref.contains(EntourageApplication.KEY_NOTIFICATIONS_ENABLED)) {
            //while processing we assume Permission is not granted
            sharedPref.edit()
                .putBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, false)
                .apply()
            requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            var notificationsEnabled = sharedPref.getBoolean(
                EntourageApplication.KEY_NOTIFICATIONS_ENABLED,
                true
            )
            val areNotificationEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled()
            if(notificationsEnabled!=areNotificationEnabled) {
                notificationsEnabled = areNotificationEnabled
                sharedPref.edit()
                    .putBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, areNotificationEnabled)
                    .apply()
            }
            if (notificationsEnabled) {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    presenter.updateApplicationInfo(token)
                }
            } else {

            }
        }
    }

    // ----------------------------------
    // PUSH NOTIFICATION HANDLING
    // ----------------------------------
    fun displayMessageOnCurrentEntourageInfoFragment(pushNotificationMessage: PushNotificationMessage): Boolean {
        /*val fragment =
            supportFragmentManager.findFragmentByTag(FeedItemInformationFragment.TAG) as FeedItemInformationFragment?
        return fragment != null && fragment.onPushNotificationChatMessageReceived(pushNotificationMessage)*/
        //TODO handle notif directly if right fragment
        return false
    }

    private fun initializeMetaData() {
        if (MetaDataRepository.metaData.value == null) MetaDataRepository.getMetaData()
        if (MetaDataRepository.groupImages.value == null) MetaDataRepository.getGroupImages()
        if (MetaDataRepository.eventsImages.value == null) MetaDataRepository.getEventsImages()
    }

    fun goEvent(){
        navController.navigate(R.id.navigation_events)
    }

    fun goConv(){
        navController.navigate(R.id.navigation_messages)
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

    fun showProfile() {
        presenter.handleMenuProfile("editProfile")
    }

    fun showFeed() {
        //TODO bottomBar?.showFeed()
    }

    fun showGuide() {
        //TODO bottomBar?.showGuide()
    }

    fun showEvents() {
        //TODO infoFragment?.dismiss()
        //TODO bottomBar?.showEvents()
    }

    fun showMyEntourages() {
        //TODO bottomBar?.showMyEntourages()
    }

    fun showActionsTab() {
        //TODO infoFragment?.dismiss()
        //TODO bottomBar?.showActionsTab()
    }

    fun showTutorial(forced: Boolean) {
        presenter.displayTutorial(forced)
    }

    fun showGuideMap() {
        val intent = Intent(this, GDSMainActivity::class.java)
        startActivity(intent)
    }

    fun deleteApplicationInfo(listener:() -> Unit) {
        presenter.deleteApplicationInfo(listener)
    }
}


enum class EntourageLink(val link: String) {
    HOME("https://app.entourage.social"),
    GROUP("https://app.entourage.social/groups/bb8c3e77aa95"),
    OUTING("https://app.entourage.social/outings/ebJUCN-woYgM"),
    OUTINGS_LIST("https://app.entourage.social/outings"),
    MESSAGE("https://app.entourage.social/messages/er2BVAa5Vb4U"),
    NEW_CONTRIBUTION("https://app.entourage.social/contributions/new"),
    NEW_SOLICITATION("https://app.entourage.social/solicitations/new"),
    CONTRIBUTIONS_LIST("https://app.entourage.social/contributions"),
    SOLICITATIONS_LIST("https://app.entourage.social/solicitations"),
    CONTRIBUTION_DETAIL("https://app.entourage.social/contributions/er2BVAa5Vb4U"),
    SOLICITATION_DETAIL("https://app.entourage.social/solicitations/eibewY3GW-ek")
}