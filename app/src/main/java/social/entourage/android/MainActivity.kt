package social.entourage.android

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.otto.Subscribe
import kotlinx.android.synthetic.main.activity_main.*
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.guide.Poi
import social.entourage.android.api.model.tour.Tour
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.BackPressable
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.base.location.EntLocation.currentLocation
import social.entourage.android.base.location.LocationUtils.isLocationEnabled
import social.entourage.android.base.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.deeplinks.DeepLinksManager.handleCurrentDeepLink
import social.entourage.android.deeplinks.DeepLinksManager.storeIntent
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.information.FeedItemInformationFragment
import social.entourage.android.guide.GDSMainActivity
import social.entourage.android.guide.poi.ReadPoiFragment
import social.entourage.android.home.expert.HomeExpertFragment
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.navigation.EntBottomNavigationView
import social.entourage.android.onboarding.OnboardingPhotoFragment
import social.entourage.android.service.EntService
import social.entourage.android.tools.EntBus
import social.entourage.android.tools.log.AnalyticsEvents
import social.entourage.android.tools.log.AnalyticsEvents.logEvent
import social.entourage.android.tools.log.AnalyticsEvents.onLocationPermissionGranted
import social.entourage.android.tools.log.AnalyticsEvents.updateUserInfo
import social.entourage.android.tour.TourInformationFragment.OnTourInformationFragmentFinish
import social.entourage.android.tour.ToursFragment
import social.entourage.android.tour.choice.ChoiceFragment
import social.entourage.android.tour.confirmation.TourEndConfirmationFragment
import social.entourage.android.tour.encounter.EncounterDisclaimerFragment
import social.entourage.android.user.AvatarUploadPresenter
import social.entourage.android.user.AvatarUploadView
import social.entourage.android.user.UserFragment
import social.entourage.android.user.edit.UserEditFragment
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import social.entourage.android.user.edit.photo.PhotoEditFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragmentCompat
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

class MainActivity : BaseSecuredActivity(),
        OnTourInformationFragmentFinish,
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

    private val bottomBar
        get() = (bottom_navigation as? EntBottomNavigationView)

    private var isAnalyticsSendFromStart = false

    private val homeFragment: HomeExpertFragment?
        get() = supportFragmentManager.findFragmentByTag(HomeExpertFragment.TAG) as? HomeExpertFragment

    private val tourFragment: ToursFragment?
        get() = supportFragmentManager.findFragmentByTag(ToursFragment.TAG) as? ToursFragment

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isFinishing) return
        ui_layout_tooltips?.visibility = View.GONE
        bottomBar?.configure(this)
        if (intent != null) {
            storeIntent(intent)
        }
        if (authenticationController.isAuthenticated) {
            //refresh the user info from the server
            presenter.updateUserLocation(currentLocation)
            //initialize the push notifications
            initializePushNotifications()
            updateAnalyticsInfo()
            authenticationController.me?.unreadCount?.let { bottomBar?.updateBadgeCountForUser(it)}
        }
        checkShowInfo()
    }
    private fun checkShowInfo() {
        //Check to show Action info
        val isShowFirstLogin = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN,false)
        val noMoreDemand = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_NO_MORE_DEMAND,false)
        var nbOfLaunch = EntourageApplication.get().sharedPreferences.getInt(EntourageApplication.KEY_NB_OF_LAUNCH,0)

        nbOfLaunch += 1
        EntourageApplication.get().sharedPreferences.edit()
                .putInt(EntourageApplication.KEY_NB_OF_LAUNCH,nbOfLaunch)
                .apply()

        var hasToShow = false
        if (!noMoreDemand) {
            hasToShow = nbOfLaunch % 4 == 0
        }

        if (isShowFirstLogin || hasToShow) {
            val sharedPreferences = EntourageApplication.get().sharedPreferences
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
                            EntourageApplication.get().sharedPreferences.edit()
                                    .putBoolean(EntourageApplication.KEY_NO_MORE_DEMAND,true)
                                    .apply()
                        }
                        .create()
                        .show()
                return
            }
            return
        }
    }

    private fun showEditProfileAction() {
        UserEditFragment.newInstance(true).show(supportFragmentManager, UserEditFragment.TAG)
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
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                when (intent.action) {
                    EntService.KEY_LOCATION_PROVIDER_DISABLED -> {
                        displayLocationProviderDisabledAlert()
                        sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                    }
                    EntService.KEY_NOTIFICATION_PAUSE_TOUR, EntService.KEY_NOTIFICATION_STOP_TOUR -> sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                }
            }
        } else {
            // user just returns to the app, update analytics
            updateAnalyticsInfo()
        }
        if (!isAnalyticsSendFromStart) {
            isAnalyticsSendFromStart = true
            logEvent(AnalyticsEvents.SHOW_START_HOME)
        }
        bottomBar?.refreshBadgeCount()
        intent?.action?.let { action ->
            EntBus.post(OnCheckIntentActionEvent(action, intent.extras))
        }
    }

    override fun onStop() {
        try {
            EntBus.unregister(this)
        } catch(e: IllegalStateException) {
            Timber.w(e)
        }
        super.onStop()
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

    fun selectMenuProfileItem(position: String) {
        if (position == "") {
            return
        }
        presenter.handleMenuProfile(position)
    }

    fun showFeed() {
        bottomBar?.showFeed()
    }

    fun showGuide() {
        bottomBar?.showGuide()
    }

    fun showGuideMap() {
        val intent = Intent(this, GDSMainActivity::class.java)

        startActivity(intent)
    }

    fun showEvents() {
        EntBus.post(OnShowEventDeeplink())
        bottomBar?.showEvents()
        homeFragment?.onShowEvents()
    }

    fun showAllActions() {
        bottomBar?.showAllActions()
        homeFragment?.onShowAll()
    }

    fun showMyEntourages() {
        bottomBar?.showMyEntourages()
    }

    fun showActionsTab() {
        EntBus.post(OnShowEventDeeplink())
        bottomBar?.showActionsTab()
    }

    fun showTutorial(forced: Boolean) {
        presenter.displayTutorial(forced)
    }

    fun showProfileTab() {
        bottomBar?.showProfileTab()
    }

    private fun initializePushNotifications() {
        val notificationsEnabled = EntourageApplication.get().sharedPreferences.getBoolean(EntourageApplication.KEY_NOTIFICATIONS_ENABLED, true)
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
        homeFragment?.saveOngoingTour()
        //remove user phone
        val sharedPreferences = EntourageApplication.get().sharedPreferences
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
            EntourageApplication.get().removePushNotification(message)
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
            val fragment = UserFragment.newInstance(event.userId)
            fragment.show(supportFragmentManager, UserFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
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
    fun onUserUpdateEvent(event: OnUserInfoUpdatedEvent) {
        updateAnalyticsInfo()
        event.user.unreadCount?.let {
            bottomBar?.updateBadgeCountForUser(it)
        }
    }

    @Subscribe
    fun onPoiViewDetail(event: OnPoiViewDetail) {
        logEvent(AnalyticsEvents.EVENT_FEED_USERPROFILE)
        try {
            val poi = Poi()
            poi.uuid = event.poiId
            val fragment = ReadPoiFragment.newInstance(poi,"")
            fragment.show(supportFragmentManager, ReadPoiFragment.TAG)
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun showStopTourFragment(tour: Tour) {
        homeFragment?.pauseTour(tour)
        TourEndConfirmationFragment
            .newInstance(tour)
            .show(supportFragmentManager, TourEndConfirmationFragment.TAG)
    }

    fun closeChoiceFragment(fragment: ChoiceFragment, tour: Tour) {
        supportFragmentManager.beginTransaction().remove(fragment).commit()
        tourFragment?.displayChosenFeedItem(tour, 0)
    }

    override fun onEntourageDisclaimerAccepted(fragment: EntourageDisclaimerFragment?) {
        // Save the entourage disclaimer shown flag
        try {
            authenticationController.entourageDisclaimerShown = true
            // Dismiss the disclaimer fragment
            fragment?.groupType?.let { homeFragment?.setGroupType(it) }
            fragment?.dismiss()
            // Show the create entourage fragment
            homeFragment?.createEntourage()
        } catch (e: IllegalStateException) {
            Timber.w(e)
        }
    }

    override fun onEncounterDisclaimerAccepted(fragment: EncounterDisclaimerFragment) {
        // Save the entourage disclaimer shown flag
        authenticationController.encounterDisclaimerShown = true

        // Dismiss the disclaimer fragment
        fragment.dismiss()
        addEncounter()
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
        (supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as? PhotoEditFragment)?.onPhotoSent(false)
    }

    // ----------------------------------
    // Deeplink actions handling
    // ----------------------------------
    fun createEntourage() {
        showFeed()
        homeFragment?.displayEntourageDisclaimer()
    }

    fun addEncounter() {
        showFeed()
        homeFragment?.onAddEncounter()
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
                    EntBus.post(OnJoinRequestAccepted(content))
                }
                else -> addPushNotification(message)                    /*TYPE_NEW_JOIN_REQUEST,TYPE_ENTOURAGE_INVITATION,TYPE_INVITATION_STATUS*/
            }
        }
    }

    private fun removePushNotification(content: PushNotificationContent, contentType: String) {
        if (content.isTourRelated) {
            EntourageApplication.get().removePushNotification(content.joinableId, TimestampedObject.TOUR_CARD, content.userId, contentType)
        } else if (content.isEntourageRelated) {
            EntourageApplication.get().removePushNotification(content.joinableId, TimestampedObject.ENTOURAGE_CARD, content.userId, contentType)
        }
    }

    private fun displayMessageOnCurrentTourInfoFragment(message: Message): Boolean {
        val fragment = supportFragmentManager.findFragmentByTag(FeedItemInformationFragment.TAG) as FeedItemInformationFragment?
        return fragment != null && fragment.onPushNotificationChatMessageReceived(message)
    }

    private fun addPushNotification(message: Message) {
        EntBus.post(OnAddPushNotification(message))
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
    fun onUnreadCountUpdate(unreadCount:OnUnreadCountUpdate?) {
        unreadCount?.unreadCount?.let {
            bottomBar?.updateBadgeCountForUser(it)
        }
    }
}