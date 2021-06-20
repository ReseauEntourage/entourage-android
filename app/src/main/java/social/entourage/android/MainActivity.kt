package social.entourage.android

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_main.*
import social.entourage.android.EntourageApplication.Companion.get
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.BackPressable
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.configuration.Configuration
import social.entourage.android.deeplinks.DeepLinksManager.handleCurrentDeepLink
import social.entourage.android.deeplinks.DeepLinksManager.storeIntent
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.entourage.my.MyEntouragesFragment
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.location.EntLocation.currentLocation
import social.entourage.android.location.LocationUtils.isLocationEnabled
import social.entourage.android.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.map.filter.MapFilterFactory.mapFilter
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.navigation.BottomNavigationDataSource
import social.entourage.android.newsfeed.BaseNewsfeedFragment
import social.entourage.android.newsfeed.v2.NewHomeFeedFragment
import social.entourage.android.onboarding.OnboardingPhotoFragment
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.log.AnalyticsEvents.logEvent
import social.entourage.android.tools.log.AnalyticsEvents.onLocationPermissionGranted
import social.entourage.android.tools.log.AnalyticsEvents.updateUserInfo
import social.entourage.android.tour.TourInformationFragment.OnTourInformationFragmentFinish
import social.entourage.android.tour.choice.ChoiceFragment
import social.entourage.android.tour.choice.ChoiceFragment.OnChoiceFragmentFinish
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment.Companion.newInstance
import social.entourage.android.tour.encounter.CreateEncounterActivity
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment
import social.entourage.android.tour.encounter.ReadEncounterActivity
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.UserFragment
import social.entourage.android.user.UserFragment.Companion.newInstance
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragmentCompat
import social.entourage.android.user.edit.UserEditFragment
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import social.entourage.android.user.edit.photo.PhotoEditFragment
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

class MainActivity : BaseSecuredActivity(),
        OnTourInformationFragmentFinish,
        OnChoiceFragmentFinish,
        EntourageDisclaimerFragment.OnFragmentInteractionListener,
        EncounterDisclaimerFragment.OnFragmentInteractionListener,
        PhotoChooseInterface,
        UserEditActionZoneFragment.FragmentListener,
        AvatarUploadView {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------
    @Inject lateinit var presenter: MainPresenter

    @Inject lateinit var avatarUploadPresenter: AvatarUploadPresenter

    //Tooltips
    private var postionTooltip = 0
    private var navigationDataSource: BottomNavigationDataSource = BottomNavigationDataSource()
    private var isAnalyticsSendFromStart = false

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isFinishing) return
        ui_layout_tooltips?.visibility = View.GONE
        configureBottombar()
        if (intent != null) {
            storeIntent(intent)
        }
        if (authenticationController.isAuthenticated) {
            //refresh the user info from the server
            presenter.updateUserLocation(currentLocation)
            //initialize the push notifications
            initializePushNotifications()
            updateAnalyticsInfo()
        }
        checkShowInfo()
    }

    fun checkShowInfo() {
        //Check to show Action info
        val isShowFirstLogin = get().sharedPreferences.getBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN,false)
        val noMoreDemand = get().sharedPreferences.getBoolean(EntourageApplication.KEY_NO_MORE_DEMAND,false)
        var nbOfLaunch = get().sharedPreferences.getInt(EntourageApplication.KEY_NB_OF_LAUNCH,0)

        nbOfLaunch += 1
        get().sharedPreferences.edit()
                .putInt(EntourageApplication.KEY_NB_OF_LAUNCH,nbOfLaunch)
                .apply()

        var hasToShow = false
        if (!noMoreDemand) {
            hasToShow = nbOfLaunch % 4 == 0
        }

        if (isShowFirstLogin || hasToShow) {
            val sharedPreferences = get().sharedPreferences
            sharedPreferences.edit().putBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN,false).apply()
            if (authenticationController.me?.goal == null || authenticationController.me?.goal?.length == 0) {
                AlertDialog.Builder(this)
                        .setTitle(R.string.login_pop_information)
                        .setMessage(R.string.login_info_pop_action)
                        .setNegativeButton(R.string.login_info_pop_action_no) { _,_ ->}
                        .setPositiveButton(R.string.login_info_pop_action_yes) { dialog, _ ->
                            dialog.dismiss()
                            showEditProfileAction()
                        }
                        .setNeutralButton(R.string.login_info_pop_action_noMore) { _,_ ->
                            get().sharedPreferences.edit()
                                    .putBoolean(EntourageApplication.KEY_NO_MORE_DEMAND,true)
                                    .apply()
                        }
                        .create()
                        .show()
                return
            }
        }
    }

    fun showEditProfileAction() {
        val fragment = UserEditFragment.newInstance(true)
        fragment.show(supportFragmentManager, UserEditFragment.TAG)
    }

    override fun setupComponent(entourageComponent: EntourageComponent?) {
        DaggerMainComponent.builder()
                .entourageComponent(entourageComponent)
                .mainModule(MainModule(this))
                .build()
                .inject(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        storeIntent(intent)
        setIntentAction(intent)
    }

    override fun onBackPressed() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.main_fragment) as? BackPressable
        if (currentFragment?.onBackPressed() == true) {
            //backAction is done in the fragment
            return
        }
        finish()
    }

    override fun onStart() {
        EntBus.register(this)
        presenter.checkForUpdate()
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (intent?.action != null) {
            when (intent.action) {
                EntService.KEY_LOCATION_PROVIDER_DISABLED -> {
                    displayLocationProviderDisabledAlert()
                    sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                }
                EntService.KEY_NOTIFICATION_PAUSE_TOUR, EntService.KEY_NOTIFICATION_STOP_TOUR -> sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            }
        }
        if (!isAnalyticsSendFromStart) {
            isAnalyticsSendFromStart = true
            logEvent(AnalyticsEvents.SHOW_START_HOME)
        }
        sendNewsfeedFragmentExtras()
        if (intent == null || intent.action == null) {
            // user just returns to the app, update analytics
            updateAnalyticsInfo()
        }
        refreshBadgeCount()
        intent?.action?.let { action ->
            EntBus.post(OnCheckIntentActionEvent(action, intent.extras))
        }
      //  checkOnboarding()
    }

    override fun onStop() {
        try {
            EntBus.unregister(this)
        } catch(e: IllegalStateException) {
            Timber.w(e)
        }
        super.onStop()
    }

    // ----------------------------------
    // PRIVATE METHODS
    // ----------------------------------
    fun checkOnboarding() {
        val sharedPreferences = get().sharedPreferences
        val isFromOnboarding = sharedPreferences.getBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, false)
        if (isFromOnboarding) {
            sharedPreferences.edit().putBoolean(EntourageApplication.KEY_IS_FROM_ONBOARDING, false).apply()
            ui_tooltip_layout_bottom?.visibility = View.INVISIBLE
            ui_tooltip_iv_bottom2?.visibility = View.INVISIBLE
            ui_tooltip_iv_bottom1?.visibility = View.INVISIBLE
            ui_tooltip_iv_bottom_bt1?.visibility = View.INVISIBLE
            ui_tooltip_iv_bottom_bt2?.visibility = View.INVISIBLE
            ui_layout_tooltips?.visibility = View.VISIBLE
            ui_layout_tooltips_ignore?.setOnClickListener {
                ui_layout_tooltips?.visibility = View.GONE
                when (postionTooltip) {
                    0 -> logEvent(AnalyticsEvents.EVENT_ACTION_TOOLTIP_FILTER_CLOSE)
                    1 -> logEvent(AnalyticsEvents.EVENT_ACTION_TOOLTIP_GUIDE_CLOSE)
                    else -> logEvent(AnalyticsEvents.EVENT_ACTION_TOOLTIP_PLUS_CLOSE)
                }
            }
            ui_tooltip_button_next_top?.setOnClickListener {
                ui_tooltip_layout_top?.visibility = View.GONE
                ui_tooltip_button_filter?.visibility = View.GONE
                val _txt = String.format(getString(R.string.tooltip_step_format), "2")
                ui_tooltip_tv_step?.text = _txt
                ui_tooltip_tv_bottom?.setText(R.string.tooltip_desc2)
                ui_tooltip_tv_title?.setText(R.string.tooltip_title2)
                ui_tooltip_iv_bottom1?.visibility = View.VISIBLE
                ui_tooltip_iv_bottom2?.visibility = View.INVISIBLE
                ui_tooltip_iv_bottom_bt1?.visibility = View.VISIBLE
                ui_tooltip_iv_bottom_bt2?.visibility = View.INVISIBLE
                ui_tooltip_layout_bottom?.visibility = View.VISIBLE
                logEvent(AnalyticsEvents.EVENT_ACTION_TOOLTIP_FILTER_NEXT)
            }
            ui_tooltip_button_next_bottom?.setOnClickListener {
                postionTooltip++
                if (postionTooltip == 1) {
                    val _txt = String.format(getString(R.string.tooltip_step_format), "3")
                    ui_tooltip_tv_step?.text = _txt
                    ui_tooltip_tv_bottom?.setText(R.string.tooltip_desc3)
                    ui_tooltip_tv_title?.setText(R.string.tooltip_title3)
                    ui_tooltip_iv_bottom2?.visibility = View.VISIBLE
                    ui_tooltip_iv_bottom1?.visibility = View.INVISIBLE
                    ui_tooltip_iv_bottom_bt1?.visibility = View.INVISIBLE
                    ui_tooltip_iv_bottom_bt2?.visibility = View.VISIBLE
                    logEvent(AnalyticsEvents.EVENT_ACTION_TOOLTIP_GUIDE_NEXT)
                } else {
                    ui_layout_tooltips?.visibility = View.GONE
                    logEvent(AnalyticsEvents.EVENT_ACTION_TOOLTIP_PLUS_NEXT)
                }
            }
            val usertype = sharedPreferences.getInt(EntourageApplication.KEY_ONBOARDING_USER_TYPE, 0)
            setupFiltersAfterOnboarding(usertype)
        } else {
            ui_layout_tooltips?.visibility = View.GONE
        }
    }

    private fun setupFiltersAfterOnboarding(userType: Int) {
        /*when (userType) {
            1 -> mapFilter.setNeighbourFilters()
            2 -> mapFilter.setAloneFilters()
            3 -> mapFilter.setDefaultValues()
            else -> mapFilter.setDefaultValues()
        }*/
        mapFilter.setDefaultValues()
        authenticationController.saveMapFilter()
        EntBus.post(OnMapFilterChanged())
    }

    private fun displayLocationProviderDisabledAlert() {
        if (isLocationEnabled() && isLocationPermissionGranted()) {
            Timber.i("No need to ask for permission: false alert...")
            return
        }
        try {
            AlertDialog.Builder(this)
                    .setMessage(getString(R.string.error_dialog_disabled))
                    .setCancelable(false)
                    .setPositiveButton("Oui") { _: DialogInterface?, _: Int -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) }
                    .setNegativeButton("Non") { dialogInterface: DialogInterface, _: Int -> dialogInterface.cancel() }
                    .create()
                    .show()
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private val newsfeedFragment: BaseNewsfeedFragment?
        get() = supportFragmentManager.findFragmentByTag(BaseNewsfeedFragment.TAG) as BaseNewsfeedFragment?

    private fun sendNewsfeedFragmentExtras() {
        authenticationController.me?.let { me -> newsfeedFragment?.onNotificationExtras(me.id, authenticationController.isUserToursOnly)}
    }

    private fun setIntentAction(intent: Intent) {
        if (intent.action != null) {
            when (intent.action) {
                PushNotificationContent.TYPE_NEW_CHAT_MESSAGE, PushNotificationContent.TYPE_NEW_JOIN_REQUEST, PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED, PushNotificationContent.TYPE_ENTOURAGE_INVITATION, PushNotificationContent.TYPE_INVITATION_STATUS, EntService.KEY_LOCATION_PROVIDER_DISABLED, EntService.KEY_NOTIFICATION_PAUSE_TOUR, EntService.KEY_NOTIFICATION_STOP_TOUR, TourEndConfirmationFragment.KEY_RESUME_TOUR, TourEndConfirmationFragment.KEY_END_TOUR, PlusFragment.KEY_START_TOUR, PlusFragment.KEY_ADD_ENCOUNTER, PlusFragment.KEY_CREATE_CONTRIBUTION, PlusFragment.KEY_CREATE_DEMAND, PlusFragment.KEY_CREATE_OUTING -> {
                }
                else -> {
                }
            }
        }
    }

    fun dismissNewsfeedFragmentDialogs() {
        newsfeedFragment?.dismissAllDialogs()
    }

    private fun configureBottombar() {
        (bottom_navigation as? BottomNavigationView)?.let { bottomBar ->
            // we need to set the listener first, to respond to the default selected tab request
            bottomBar.setOnNavigationItemSelectedListener { item: MenuItem ->
                if (shouldBypassNavigation(item.itemId)) {
                    return@setOnNavigationItemSelectedListener false
                }
                if (bottomBar.selectedItemId != item.itemId) {
                    sendAnalyticsTapTabbar(item.itemId)
                    loadFragment(item.itemId)
                }
                true
            }
            //TODO: a remettre l'auto ?
            //navigationDataSource.isEngaged = authenticationController.me?.isEngaged ?: false
            navigationDataSource.isEngaged = true
            val defaultId = navigationDataSource.defaultSelectedTab
            bottomBar.selectedItemId = defaultId
            loadFragment(defaultId)
            val messageBadge = bottomBar.getOrCreateBadge(navigationDataSource.myMessagesTabIndex)
            messageBadge.backgroundColor = ResourcesCompat.getColor(resources, R.color.map_announcement_background, null)
            messageBadge.badgeTextColor = ResourcesCompat.getColor(resources, R.color.primary, null)
            messageBadge.maxCharacterCount = 3
        }
    }

    private fun sendAnalyticsTapTabbar(@IdRes itemId: Int) {
        when (itemId) {
            R.id.bottom_bar_newsfeed -> logEvent(AnalyticsEvents.ACTION_TAB_FEEDS)
            R.id.bottom_bar_guide -> logEvent(AnalyticsEvents.ACTION_TAB_GDS)
            R.id.bottom_bar_plus -> logEvent(AnalyticsEvents.ACTION_TAB_PLUS)
            R.id.bottom_bar_mymessages -> logEvent(AnalyticsEvents.ACTION_TAB_MESSAGES)
            R.id.bottom_bar_profile -> logEvent(AnalyticsEvents.ACTION_TAB_PROFIL)
        }
    }

    private fun shouldBypassNavigation(@IdRes itemId: Int): Boolean {
        if (itemId == navigationDataSource.actionMenuId) {
            //Handling special cases
            if (!Configuration.showPlusScreen()) {
                // Show directly the create entourage disclaimer
                createEntourage()
                return true
            } else if (authenticationController.savedTour != null) {
                // Show directly the create encounter
                //TODO should be bound to service
                addEncounter()
                return true
            }
        }
        return false
    }

    private fun selectNavigationTab(menuIndex: Int) { //TODO: Voir pour afficher le bon frgagment de l'onglet Home si on est en nav horizontale
        (bottom_navigation as? BottomNavigationView)?.let {
            if (it.selectedItemId != menuIndex) {
                it.selectedItemId = menuIndex
            }
        }
    }

    fun selectMenuProfileItem(position: String) {
        if (position == "") {
            return
        }
        presenter.handleMenuProfile(position)
    }

    private fun loadFragment(menuId: Int) {
        try {
            val tag = navigationDataSource.getFragmentTagAtIndex(menuId)
            if (!supportFragmentManager.popBackStackImmediate(tag, 0)) {
                val newFragment = navigationDataSource.getFragmentAtIndex(menuId) ?: return
                val fragmentTransaction = supportFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.main_fragment, newFragment, tag)
                fragmentTransaction.addToBackStack(tag)
                fragmentTransaction.commit()
            }
            else {
                if (tag.equals(BaseNewsfeedFragment.TAG)) {
                    if (supportFragmentManager.fragments.first() != null && (supportFragmentManager.fragments.first() is NewHomeFeedFragment) ) {
                            (supportFragmentManager.fragments.first() as NewHomeFeedFragment).checkNavigation()
                    }
                }
            }
            //TODO check if we need to execute pending actions
            //supportFragmentManager.executePendingTransactions();
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    fun showMapFilters() {
        newsfeedFragment?.onShowFilter()
    }

    fun showFeed() {
        selectNavigationTab(navigationDataSource.feedTabIndex)
    }

    fun showGuide() {
        selectNavigationTab(navigationDataSource.guideTabIndex)
    }

    fun showGuideMap() {
        val intent = Intent(this, GDSMainActivity::class.java)

        startActivity(intent)
    }

    fun showEvents() {
        EntBus.post(OnShowEventDeeplink())
        selectNavigationTab(navigationDataSource.feedTabIndex)
        newsfeedFragment?.onShowEvents()
    }

    fun showAllActions() {
        selectNavigationTab(navigationDataSource.feedTabIndex)
        newsfeedFragment?.onShowAll()
    }

    fun showMyEntourages() {
        selectNavigationTab(navigationDataSource.myMessagesTabIndex)
    }

    fun showActionsTab() {
        EntBus.post(OnShowEventDeeplink())
        selectNavigationTab(navigationDataSource.agirTabIndex)
    }

    fun showTutorial(forced: Boolean) {
        presenter.displayTutorial(forced)
    }

    fun showProfileTab() {
        selectNavigationTab(navigationDataSource.profilTabIndex)
    }

    private fun initializePushNotifications() {
        val notificationsEnabled = get().sharedPreferences.getBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, true)
        if (notificationsEnabled) {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token -> presenter.updateApplicationInfo(token) }
        } else {
            presenter.deleteApplicationInfo()
        }
    }

    private fun updateAnalyticsInfo() {
        authenticationController.me?.let { user ->
            updateUserInfo(user, applicationContext, NotificationManagerCompat.from(this).areNotificationsEnabled())
        }
    }

    public override fun logout() {
        newsfeedFragment?.saveOngoingTour()
        //remove user phone
        val sharedPreferences = get().sharedPreferences
        val editor = sharedPreferences.edit()
        authenticationController.me?.let { me ->
            (sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String?>?)?.let { loggedNumbers ->
                loggedNumbers.remove(me.phone)
                editor.putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers)
            }
        }
        presenter.deleteApplicationInfo()
        editor.remove(EntourageApplication.KEY_REGISTRATION_ID)
        editor.remove(EntourageApplication.KEY_NOTIFICATIONS_ENABLED)
        editor.remove(EntourageApplication.KEY_GEOLOCATION_ENABLED)
        editor.remove(EntourageApplication.KEY_NO_MORE_DEMAND)
        editor.putInt(EntourageApplication.KEY_NB_OF_LAUNCH,0)
        editor.remove(EntourageApplication.KEY_HOME_IS_EXPERTMODE)
        editor.remove(EntourageApplication.KEY_HOME_IS_ALREADYINFO_NEO)
        editor.apply()
        super.logout()
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun onGCMTokenObtained(event: OnGCMTokenObtainedEvent) {
        presenter.updateApplicationInfo(event.registrationId)
    }

    @Subscribe
    fun checkIntentAction(event: OnCheckIntentActionEvent) {
        //if (!isSafeToCommit()) return;
        val message = event.extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as Message?
        if (message != null) {
            message.content?.let { content ->
                if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE == event.action && !content.isTourRelated && !content.isEntourageRelated) {
                    showMyEntourages()
                }
            }
            get().removePushNotification(message)
            refreshBadgeCount()
        } else {
            // Handle the deep link
            handleCurrentDeepLink(this)
        }
        intent = null
    }

    @Subscribe
    fun userViewRequested(event: OnUserViewRequestedEvent) {
        logEvent(AnalyticsEvents.EVENT_FEED_USERPROFILE)
        try {
            val fragment = newInstance(event.userId)
            fragment.show(supportFragmentManager, UserFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    @Subscribe
    fun tourEncounterViewRequested(event: OnTourEncounterViewRequestedEvent) {
        val encounter = event.encounter
        if (encounter.isReadOnly) {
            val intent = Intent(this, ReadEncounterActivity::class.java)
            val extras = Bundle()
            extras.putSerializable(ReadEncounterActivity.BUNDLE_KEY_ENCOUNTER, encounter)
            intent.putExtras(extras)
            this.startActivity(intent)
        } else {
            val intent = Intent(this, CreateEncounterActivity::class.java)
            val extras = Bundle()
            extras.putSerializable(CreateEncounterActivity.BUNDLE_KEY_ENCOUNTER, encounter)
            intent.putExtras(extras)
            this.startActivity(intent)
        }
    }

    @Subscribe
    fun onUnauthorized(event: OnUnauthorizedEvent) {
        logout()
    }

    @Subscribe
    fun onLocationPermissionGranted(event: OnLocationPermissionGranted?) {
        if (event == null) return
        onLocationPermissionGranted(event.isPermissionGranted)
    }

    @Subscribe
    fun onShowURLRequested(event: OnShowURLEvent) {
        showWebView(event.url)
    }

    @Subscribe
    fun onUserUpdateEvent(event: OnUserInfoUpdatedEvent?) {
        updateAnalyticsInfo()
    }

    @Subscribe
    fun onPoiViewDetail(event: OnPoiViewDetail) {
        logEvent(AnalyticsEvents.EVENT_FEED_USERPROFILE)
        try {
            val poi = Poi()
            poi.uuid = event.poiId
            val fragment = ReadPoiFragment.newInstance(poi)
            fragment.show(supportFragmentManager, ReadPoiFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun showStopTourActivity(tour: Tour) {
        newsfeedFragment?.pauseTour(tour)
        val tourEndConfirmationFragment = newInstance(tour)
        tourEndConfirmationFragment.show(supportFragmentManager, TourEndConfirmationFragment.TAG)
    }

    override fun closeChoiceFragment(fragment: ChoiceFragment, tour: Tour?) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
        if (tour != null) {
            newsfeedFragment?.displayChosenFeedItem(tour, 0)
        }
    }

    override fun onEntourageDisclaimerAccepted(fragment: EntourageDisclaimerFragment?) {
        // Save the entourage disclaimer shown flag
        try {
            authenticationController.entourageDisclaimerShown = true
            // Dismiss the disclaimer fragment
            fragment?.dismiss()
            // Show the create entourage fragment
            newsfeedFragment?.createEntourage()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun onEncounterDisclaimerAccepted(fragment: EncounterDisclaimerFragment) {
        // Save the entourage disclaimer shown flag
        authenticationController.encounterDisclaimerShown = true

        // Dismiss the disclaimer fragment
        fragment.dismiss()
        newsfeedFragment?.addEncounter()
    }

    override fun onPhotoBack() {
        // Do nothing
    }

    override fun onPhotoIgnore() {
        // Do nothing
    }

    override fun onPhotoChosen(photoURI: Uri?, photoSource: Int) {
        photoURI?.path?.let { path ->
            if (photoSource == OnboardingPhotoFragment.TAKE_PHOTO_REQUEST) {
                logEvent(AnalyticsEvents.EVENT_PHOTO_SUBMIT)
            }
            //Upload the photo to Amazon S3
            showProgressDialog(R.string.user_photo_uploading)
            avatarUploadPresenter.uploadPhoto(File(path))
        }
    }

    override fun onUploadError() {
        Toast.makeText(this@MainActivity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT).show()
        dismissProgressDialog()
        val photoEditFragment = supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as PhotoEditFragment?
        photoEditFragment?.onPhotoSent(false)
    }

    // ----------------------------------
    // Deeplink actions handling
    // ----------------------------------
    fun createEntourage() {
        showFeed()
        dismissNewsfeedFragmentDialogs()
        newsfeedFragment?.displayEntourageDisclaimer()
    }

    private fun addEncounter() {
        showFeed()
        dismissNewsfeedFragmentDialogs()
        newsfeedFragment?.onAddEncounter()
    }

    // ----------------------------------
    // PUSH NOTIFICATION HANDLING
    // ----------------------------------
    @Subscribe
    fun onPushNotificationReceived(event: OnPushNotificationReceived) {
        val message = event.message
        val content = message.content ?: return
        if (content.joinableId == 0L) {
            return
        }
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            when (content.type) {
                PushNotificationContent.TYPE_NEW_CHAT_MESSAGE -> if (displayMessageOnCurrentTourInfoFragment(message)) {
                    //already displayed
                    removePushNotification(content, content.type)
                } else {
                    addPushNotification(message)
                }
                PushNotificationContent.TYPE_JOIN_REQUEST_CANCELED ->                     //@TODO should we update current tour info fragment ?
                    removePushNotification(content, PushNotificationContent.TYPE_NEW_JOIN_REQUEST)
                PushNotificationContent.TYPE_JOIN_REQUEST_ACCEPTED -> {
                    addPushNotification(message)
                    val mapFragment = newsfeedFragment
                    mapFragment?.userStatusChanged(content, FeedItem.JOIN_STATUS_ACCEPTED)
                }
                else -> addPushNotification(message)                    /*TYPE_NEW_JOIN_REQUEST,TYPE_ENTOURAGE_INVITATION,TYPE_INVITATION_STATUS*/
            }
        }
    }

    private fun removePushNotification(content: PushNotificationContent, contentType: String) {
        if (content.isTourRelated) {
            get().removePushNotification(content.joinableId, TimestampedObject.TOUR_CARD, content.userId, contentType)
        } else if (content.isEntourageRelated) {
            get().removePushNotification(content.joinableId, TimestampedObject.ENTOURAGE_CARD, content.userId, contentType)
        }
    }

    private fun displayMessageOnCurrentTourInfoFragment(message: Message): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(FeedItemInformationFragment.TAG) as FeedItemInformationFragment?
        return fragment != null && fragment.onPushNotificationChatMessageReceived(message)
    }

    private fun addPushNotification(message: Message) {
        newsfeedFragment?.onPushNotificationReceived(message)
        val myEntouragesFragment = supportFragmentManager.findFragmentByTag(MyEntouragesFragment.TAG) as MyEntouragesFragment?
        myEntouragesFragment?.onPushNotificationReceived(message)
        refreshBadgeCount()
    }

    // ----------------------------------
    // ACTION ZONE HANDLING
    // ----------------------------------
    fun showEditActionZoneFragment(extraFragmentListener: UserEditActionZoneFragment.FragmentListener? = null, isSecondaryAddress: Boolean = false) {
        val me = authenticationController.me ?: return
        if (me.address?.displayAddress?.isNotEmpty() == true) {
            return
        }
        if (authenticationController.editActionZoneShown || authenticationController.isIgnoringActionZone) {
            return  //noNeedToShowEditScreen
        }
        val userEditActionZoneFragment = UserEditActionZoneFragment.newInstance(null, isSecondaryAddress)
        userEditActionZoneFragment.setupListener(extraFragmentListener ?: this)
        userEditActionZoneFragment.show(supportFragmentManager, UserEditActionZoneFragment.TAG)
        authenticationController.editActionZoneShown = true
    }

    override fun onUserEditActionZoneFragmentDismiss() {}
    override fun onUserEditActionZoneFragmentIgnore() {
        storeActionZone(true)
    }

    override fun onUserEditActionZoneFragmentAddressSaved() {
        storeActionZone(false)
    }

    private fun storeActionZone(ignoreZone: Boolean) {
        if (authenticationController.isAuthenticated) {
            authenticationController.isIgnoringActionZone = ignoreZone
        }
        (supportFragmentManager.findFragmentByTag(UserEditActionZoneFragmentCompat.TAG) as UserEditActionZoneFragmentCompat?)?.let { fragment ->
            if (!fragment.isStateSaved) {
                fragment.dismiss()
            }
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun onMyEntouragesForceRefresh(event: OnMyEntouragesForceRefresh) {
        event.feedItem?.let {item ->
            get().updateBadgeCountForFeedItem(item)
            refreshBadgeCount()
        }
    }

    @Subscribe
    fun onUnreadCountUpdate(unreadCount:OnUnreadCountUpdate?) {
        unreadCount?.unreadCount?.let { get().updateBadgeCountForCount(it) }
        refreshBadgeCount()
    }

    // ----------------------------------
    // Helper functions
    // ----------------------------------
    private fun refreshBadgeCount() {
        (bottom_navigation as? BottomNavigationView)?.let {
            val messageBadge = it.getOrCreateBadge(navigationDataSource.myMessagesTabIndex)
                    ?: return
            val badgeCount = get().badgeCount
            if (badgeCount > 0) {
                messageBadge.isVisible = true
                messageBadge.number = badgeCount
            } else {
                messageBadge.isVisible = false
            }
        }
    }
}