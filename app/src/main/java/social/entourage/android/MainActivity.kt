package social.entourage.android

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import social.entourage.android.api.tape.Events.*
import social.entourage.android.base.BackPressable
import social.entourage.android.base.BaseSecuredActivity
import social.entourage.android.base.location.EntLocation.currentLocation
import social.entourage.android.base.location.LocationUtils.isLocationEnabled
import social.entourage.android.base.location.LocationUtils.isLocationPermissionGranted
import social.entourage.android.deeplinks.DeepLinksManager.handleCurrentDeepLink
import social.entourage.android.deeplinks.DeepLinksManager.storeIntent
import social.entourage.android.entourage.EntourageDisclaimerFragment
import social.entourage.android.entourage.information.EntourageInformationFragment
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
import social.entourage.android.user.*
import social.entourage.android.user.edit.UserEditFragment
import social.entourage.android.user.edit.photo.PhotoChooseInterface
import social.entourage.android.user.edit.photo.PhotoEditFragment
import social.entourage.android.user.edit.place.UserEditActionZoneFragment
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class MainActivity : BaseSecuredActivity(),
    EntourageDisclaimerFragment.OnFragmentInteractionListener,
    PhotoChooseInterface,
    UserEditActionZoneFragment.FragmentListener,
    AvatarUploadView {
    // ----------------------------------
    // ATTRIBUTES
    // ----------------------------------

    private val presenter: MainPresenter = MainPresenter(this)
    private val avatarUploadPresenter: AvatarUploadPresenter

    init {
        avatarUploadPresenter = AvatarUploadPresenter(
            this,
            AvatarUploadRepository(),
            presenter
        )
    }

    private val bottomBar
        get() = (bottom_navigation as? EntBottomNavigationView)

    private var isAnalyticsSendFromStart = false

    private val homeFragment: HomeExpertFragment?
        get() = supportFragmentManager.findFragmentByTag(HomeExpertFragment.TAG) as? HomeExpertFragment

    private val infoFragment: EntourageInformationFragment?
        get() = supportFragmentManager.findFragmentByTag(FeedItemInformationFragment.TAG) as? EntourageInformationFragment

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
            authenticationController.me?.unreadCount?.let { bottomBar?.updateBadgeCountForUser(it) }
        }
        checkShowInfo()
    }

    private fun checkShowInfo() {
        //Check to show Action info
        val isShowFirstLogin = EntourageApplication.get().sharedPreferences.getBoolean(
            EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN,
            false
        )
        val noMoreDemand = EntourageApplication.get().sharedPreferences.getBoolean(
            EntourageApplication.KEY_NO_MORE_DEMAND,
            false
        )
        var nbOfLaunch = EntourageApplication.get().sharedPreferences.getInt(
            EntourageApplication.KEY_NB_OF_LAUNCH,
            0
        )

        nbOfLaunch += 1
        EntourageApplication.get().sharedPreferences.edit()
            .putInt(EntourageApplication.KEY_NB_OF_LAUNCH, nbOfLaunch)
            .apply()

        var hasToShow = false
        if (!noMoreDemand) {
            hasToShow = nbOfLaunch % 4 == 0
        }

        if (isShowFirstLogin || hasToShow) {
            val sharedPreferences = EntourageApplication.get().sharedPreferences
            sharedPreferences.edit()
                .putBoolean(EntourageApplication.KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN, false).apply()
            if (authenticationController.me?.goal == null || authenticationController.me?.goal?.length == 0) {
                AlertDialog.Builder(this)
                    .setTitle(R.string.login_pop_information)
                    .setMessage(R.string.login_info_pop_action)
                    .setNegativeButton(R.string.login_info_pop_action_no) { _, _ -> }
                    .setPositiveButton(R.string.login_info_pop_action_yes) { dialog, _ ->
                        dialog.dismiss()
                        showEditProfileAction()
                    }
                    .setNeutralButton(R.string.login_info_pop_action_noMore) { _, _ ->
                        EntourageApplication.get().sharedPreferences.edit()
                            .putBoolean(EntourageApplication.KEY_NO_MORE_DEMAND, true)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        this.intent = intent
        storeIntent(intent)
    }

    override fun onBackPressed() {
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.main_fragment) as? BackPressable
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
            if (intent.action == EntService.KEY_LOCATION_PROVIDER_DISABLED) {
                displayLocationProviderDisabledAlert()
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
            checkIntentAction(action, intent?.extras)
            homeFragment?.checkIntentAction(action, intent?.extras)
        }
    }

    override fun onStop() {
        try {
            EntBus.unregister(this)
        } catch (e: IllegalStateException) {
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
                .setPositiveButton("Oui") { _: DialogInterface?, _: Int ->
                    startActivity(
                        Intent(
                            Settings.ACTION_LOCATION_SOURCE_SETTINGS
                        )
                    )
                }
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
        infoFragment?.dismiss()
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
        infoFragment?.dismiss()
        bottomBar?.showActionsTab()
    }

    fun showTutorial(forced: Boolean) {
        presenter.displayTutorial(forced)
    }

    fun showProfileTab() {
        bottomBar?.showProfileTab()
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

    private fun updateAnalyticsInfo() {
        authenticationController.me?.let { user ->
            updateUserInfo(
                user,
                applicationContext,
                NotificationManagerCompat.from(this).areNotificationsEnabled()
            )
        }
    }

    public override fun logout() {
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
        super.logout()
    }

    fun checkIntentAction(action: String, extras: Bundle?) {
        //if (!isSafeToCommit()) return;
        val message =
            extras?.getSerializable(PushNotificationManager.PUSH_MESSAGE) as Message?
        if (message != null) {
            message.content?.let { content ->
                if (PushNotificationContent.TYPE_NEW_CHAT_MESSAGE == action && !content.isEntourageRelated) {
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

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
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
    fun onLocationPermissionGranted(event: OnLocationPermissionGranted?) {
        if (event == null) return
        onLocationPermissionGranted(event.isPermissionGranted)
    }

    @Subscribe
    fun onUserUpdateEvent(event: OnUserInfoUpdatedEvent) {
        updateAnalyticsInfo()
        event.user.unreadCount?.let {
            bottomBar?.updateBadgeCountForUser(it)
        }
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
        Toast.makeText(this@MainActivity, R.string.user_photo_error_not_saved, Toast.LENGTH_SHORT)
            .show()
        dismissProgressDialog()
        (supportFragmentManager.findFragmentByTag(PhotoEditFragment.TAG) as? PhotoEditFragment)?.onPhotoSent(
            false
        )
    }

    // ----------------------------------
    // Deeplink actions handling
    // ----------------------------------
    fun createEntourage() {
        showFeed()
        homeFragment?.displayEntourageDisclaimer()
    }

    // ----------------------------------
    // PUSH NOTIFICATION HANDLING
    // ----------------------------------
    fun displayMessageOnCurrentEntourageInfoFragment(message: Message): Boolean {
        val fragment =
            supportFragmentManager.findFragmentByTag(FeedItemInformationFragment.TAG) as FeedItemInformationFragment?
        return fragment != null && fragment.onPushNotificationChatMessageReceived(message)
    }

    // ----------------------------------
    // ACTION ZONE HANDLING
    // ----------------------------------
    fun showEditActionZoneFragment(
        extraFragmentListener: UserEditActionZoneFragment.FragmentListener? = null,
        isSecondaryAddress: Boolean = false
    ) {
        val me = authenticationController.me ?: return
        if (me.address?.displayAddress?.isNotEmpty() == true || me.isUserTypeAsso) {
            return
        }
        if (authenticationController.editActionZoneShown || authenticationController.isIgnoringActionZone) {
            return  //noNeedToShowEditScreen
        }
        val userEditActionZoneFragment =
            UserEditActionZoneFragment.newInstance(null, isSecondaryAddress)
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
        (supportFragmentManager.findFragmentByTag(UserEditActionZoneFragment.TAG) as UserEditActionZoneFragment?)?.let { fragment ->
            if (!fragment.isStateSaved) {
                fragment.dismiss()
            }
        }
    }

    // ----------------------------------
    // BUS LISTENERS
    // ----------------------------------
    @Subscribe
    fun onUnreadCountUpdate(unreadCount: OnUnreadCountUpdate?) {
        unreadCount?.unreadCount?.let {
            bottomBar?.updateBadgeCountForUser(it)
        }
    }
}