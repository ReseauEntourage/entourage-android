package social.entourage.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.api.MetaDataRepository
import social.entourage.android.api.model.ReactionType
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.base.location.EntLocation
import social.entourage.android.databinding.ActivityMainBinding
import social.entourage.android.deeplinks.UniversalLinkManager
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.home.CommunicationHandlerBadgeViewModel
import social.entourage.android.home.UnreadMessages
import social.entourage.android.language.LanguageManager
import social.entourage.android.notifications.NotificationActionManager
import social.entourage.android.notifications.PushNotificationManager
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.utils.Const
import social.entourage.android.user.UserPresenter
import social.entourage.android.user.UserProfileActivity
import timber.log.Timber

class MainActivity : BaseSecuredActivity() {
    private lateinit var navController: NavController
    private val presenter: MainPresenter = MainPresenter(this)
    private val userPresenter: UserPresenter by lazy { UserPresenter() }

    private lateinit var viewModel: CommunicationHandlerBadgeViewModel
    private val universalLinkManager = UniversalLinkManager(this)
    private var fromDeepLinkGoDiscoverGroup = false
    private lateinit var updateActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>

    override fun onCreate(savedInstanceState: Bundle?) {
        updateMainLanguage()
        super.onCreate(savedInstanceState)

        instance = this
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[CommunicationHandlerBadgeViewModel::class.java]
        viewModel.badgeCount.observe(this,::handleUpdateBadgeResponse)
        userPresenter.isGetUserSuccess.observe(this, ::handleResponse)

        initializeNavBar()
        if (authenticationController.isAuthenticated) {
            //refresh the user info from the server
            presenter.updateUserLocation(EntLocation.currentLocation)
        }
        handleUniversalLinkFromMain(this.intent)

        updateActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                Timber.tag("Update")
                    .e("La mise à jour a échoué ou a été annulée par l'utilisateur.")
            }
        }
        checkForAppUpdate()

    }

    fun setGoDiscoverGroupFromDeepL(bool:Boolean){
        this.fromDeepLinkGoDiscoverGroup = bool
    }

    fun getFromDeepLGoDiscoverGroup():Boolean{
        return this.fromDeepLinkGoDiscoverGroup
    }



    override fun onStart() {
        presenter.checkForUpdate(this)
        super.onStart()
    }

    fun handleUniversalLinkFromMain(intent: Intent){
        val uri = intent.data
        if (uri != null) {
            universalLinkManager.handleUniversalLink(uri)
        }
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
        updateMainLanguage()
        super.onResume()
        initializeMetaData()
        if (authenticationController.isAuthenticated) {
            //initialize the push notifications
            initializePushNotifications()
            updateAnalyticsInfo()
            //TODO authenticationController.me?.unreadCount?.let { bottomBar?.updateBadgeCountForUser(it) }
        }
        //TODO bottomBar?.refreshBadgeCount()

        if(this.intent != null){
            useIntentForRedictection(this.intent)
            this.intent = null
        }
    }

    private fun checkForAppUpdate() {
        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo

        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    AnalyticsEvents.logEvent(AnalyticsEvents.view_update_version)
                appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this, // Ton Activity.
                        UPDATE_REQUEST_CODE // Un code de requête défini par toi.
                    )
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                AnalyticsEvents.logEvent(AnalyticsEvents.clic_update_version_cancel)
                Timber.tag("Update")
                    .e("La mise à jour a échoué ou a été annulée par l'utilisateur.")
            }else{
                AnalyticsEvents.logEvent(AnalyticsEvents.clic_update_version_validate)
            }
        }
    }
     fun updateMainLanguage(){
        updateLanguage()
        val id = EntourageApplication.me(this)?.id
         if (id != null) {
             userPresenter.getUser(id)
         }
        if(id != null){
            userPresenter.updateLanguage(id, LanguageManager.loadLanguageFromPreferences(this))
            LanguageManager.setLocale(this, LanguageManager.loadLanguageFromPreferences(this))
        }
         val sharedPrefs = this.getSharedPreferences(
             getString(R.string.preference_file_key), Context.MODE_PRIVATE
         )
         if(!sharedPrefs.contains("translatedByDefault")){
             val editor = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit()
             editor.putBoolean("translatedByDefault", true)
             editor.apply()
         }
     }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun handleResponse(success: Boolean){

    }

    fun useIntentForRedictection(intent: Intent){
        intent.action?.let { action ->
            checkIntentAction(action, intent.extras)
        }
        val fromWelcomeActivity = intent.getBooleanExtra("fromWelcomeActivity", false)
        val fromWelcomeActivityThreeEvent = intent.getBooleanExtra("fromWelcomeActivityThreeEvent", false)
        val fromWelcomeActivityThreeDemand = intent.getBooleanExtra("fromWelcomeActivityThreeDemand", false)
        val fromWelcomeActivityThreeContrib = intent.getBooleanExtra("fromWelcomeActivityThreeContrib", false)
        val goContrib = intent.getBooleanExtra("goContrib", false)
        val goDemand = intent.getBooleanExtra("goDemand", false)
        val goDiscoverGroup = intent.getBooleanExtra("goDiscoverGroup", false)
        val goDiscoverEvent = intent.getBooleanExtra("goDiscoverEvent", false)


        if(goContrib){
            goContrib()
            return
        }
        if(goDiscoverGroup){
            this.setGoDiscoverGroupFromDeepL(goDiscoverGroup)
            goGroup()
            return
        }
        if(goDiscoverEvent){
            goEvent()
            return
        }
        if(goDemand){
            goDemand()
            return
        }
        if (fromWelcomeActivity) {
            goGroup()
            return
        }
        if (fromWelcomeActivityThreeEvent) {
            goEvent()
            return
        }
        if (fromWelcomeActivityThreeDemand) {
            goDemand()
            return
        }
        if (fromWelcomeActivityThreeContrib) {
            goContrib()
            val newIntent = Intent(this, CreateActionActivity::class.java)
            newIntent.putExtra(Const.IS_ACTION_DEMAND, false)
            startActivity(newIntent)
            return
        }
        this.intent = intent
        handleUniversalLinkFromMain(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        useIntentForRedictection(intent)
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
            storePushNotificationPermision()
        }

    private fun initializePushNotifications() {
        val sharedPref = EntourageApplication.get().sharedPreferences
        if (!sharedPref.contains(EntourageApplication.KEY_NOTIFICATIONS_ENABLED)) {
            //while processing we assume Permission is not granted
            sharedPref.edit()
                .putBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, false)
                .apply()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                //TODO
            }
        } else {
            storePushNotificationPermision()
        }
    }
    private fun storePushNotificationPermision() {
        val sharedPref = EntourageApplication.get().sharedPreferences
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

    fun DisplayErrorFromAppLinks(errorNumber:Int){
        //0 : EVENT
        //1 : GROUP
        //2 : ACTION
        //3 : DISCUSSION
        if(errorNumber == 0){
            Toast.makeText(this, getString(R.string.error_get_event), Toast.LENGTH_LONG).show()
        }
        if(errorNumber == 1){
            Toast.makeText(this, getString(R.string.error_get_group), Toast.LENGTH_LONG).show()

        }
        if(errorNumber == 2){
            Toast.makeText(this, getString(R.string.error_get_action), Toast.LENGTH_LONG).show()

        }
        if(errorNumber == 3){
            Toast.makeText(this, getString(R.string.error_get_discussion), Toast.LENGTH_LONG).show()

        }
    }

    fun goHome(){
        navController.navigate(R.id.navigation_home)

    }

    fun goGroup(){
        navController.navigate(R.id.navigation_groups)

    }



    fun goEvent(){
        navController.navigate(R.id.navigation_events)

    }

    fun goConv(){
        navController.navigate(R.id.navigation_messages)
    }
    fun goContrib(){
        val bundle = bundleOf("isActionDemand" to false) // Mettez ici la valeur souhaitée pour "isActionDemand"
        navController.navigate(R.id.navigation_donations, bundle)

    }
    fun goDemand(){
        val bundle = bundleOf("isActionDemand" to true) // Mettez ici la valeur souhaitée pour "isActionDemand"
        navController.navigate(R.id.navigation_donations, bundle)
    }

    private fun initializeNavBar() {
        val navHostFragment = supportFragmentManager.findFragmentById(
            R.id.nav_host_fragment_activity_main
        ) as NavHostFragment
        navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.nav_view)



        bottomNavigationView.itemIconTintList = null
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    AnalyticsEvents.logEvent(AnalyticsEvents.Action_Tabbar_home)
                    navController.navigate(R.id.navigation_home)

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
            androidx.navigation.Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
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
        AnalyticsEvents.logEvent(AnalyticsEvents.ACTION_PROFILE_MODPROFIL)
        startActivityForResult(Intent(this, UserProfileActivity::class.java), 0)
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
    companion object {
        var instance: MainActivity? = null
        const val UPDATE_REQUEST_CODE = 1001 // Ou tout autre numéro que tu souhaites.
        var reactionsList: MutableList<ReactionType>? = null

    }
}


enum class EntourageLink(val link: String) {
    HOME("https://preprod.entourage.social/app"),
    GROUP("https://preprod.entourage.social/app/groups/bb8c3e77aa95"),
    OUTING("https://preprod.entourage.social/app/outings/ebJUCN-woYgM"),
    OUTINGS_LIST("https://preprod.entourage.social/app/outings"),
    MESSAGE("https://preprod.entourage.social/app/messages/er2BVAa5Vb4U"),
    NEW_CONTRIBUTION("https://preprod.entourage.social/app/contributions/new"),
    NEW_SOLICITATION("https://preprod.entourage.social/app/solicitations/new"),
    CONTRIBUTIONS_LIST("https://preprod.entourage.social/app/contributions"),
    SOLICITATIONS_LIST("https://preprod.entourage.social/app/solicitations"),
    CONTRIBUTION_DETAIL("https://preprod.entourage.social/app/contributions/er2BVAa5Vb4U"),
    SOLICITATION_DETAIL("https://preprod.entourage.social/app/solicitations/eibewY3GW-ek")
}