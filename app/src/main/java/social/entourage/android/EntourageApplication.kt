package social.entourage.android

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.google.firebase.analytics.FirebaseAnalytics
import social.entourage.android.api.ApiModule
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.User
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.authentication.ComplexPreferences
import social.entourage.android.base.BaseActivity
import social.entourage.android.notifications.PushNotificationManager
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity
import social.entourage.android.profile.settings.SettingsPresenter
import social.entourage.android.tools.LibrariesSupport
import social.entourage.android.tools.log.AnalyticsEvents
import timber.log.Timber

/**
 * Application setup for Analytics, JodaTime and Dagger
 */
class EntourageApplication : MultiDexApplication() {
    private val activities: ArrayList<BaseActivity> = ArrayList()
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var librariesSupport: LibrariesSupport
    lateinit var authenticationController: AuthenticationController
    lateinit var complexPreferences: ComplexPreferences
    lateinit var apiModule: ApiModule
    private val settingsPresenter = SettingsPresenter()

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate() {
        super.onCreate()
        activities.clear()
        instance = this
        complexPreferences = ComplexPreferences(this, "userPref", Context.MODE_PRIVATE)
        authenticationController = AuthenticationController()
        apiModule = ApiModule()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        librariesSupport = LibrariesSupport()
        librariesSupport.setupLibraries(this)
        setupSharedPreferences()
    }

    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    val firebase: FirebaseAnalytics
        get() = librariesSupport.firebaseAnalytics

    fun me(): User? {
        return authenticationController.me
    }

    fun onActivityCreated(activity: BaseActivity) {
        activities.add(activity)
    }

    fun onActivityDestroyed(activity: BaseActivity) {
        activities.remove(activity)
    }

    private val loginActivity: LoginActivity?
        get() {
            activities.filterIsInstance<LoginActivity>().any {
                return it
            }
            return null
        }

    val mainActivity: MainActivity?
        get() {
            activities.filterIsInstance<MainActivity>().any() {
                return it
            }
            return null
        }

    fun finishLoginActivity() {
        Timber.d("Finishing login activity")
        loginActivity?.finish()
    }

     fun deleteAccount() {
        settingsPresenter.deleteAccount()

    }

    fun logOut() {
        authenticationController.me?.let { me ->
            //remove user phone
            mainActivity?.deleteApplicationInfo(){
                val sharedPreferences = sharedPreferences
                val editor = sharedPreferences.edit()
                authenticationController.logOutUser()
                (sharedPreferences.getStringSet(KEY_TUTORIAL_DONE,HashSet()) as HashSet<String?>?)?.let { loggedNumbers ->
                    loggedNumbers.remove(me.phone)
                    editor.putStringSet(KEY_TUTORIAL_DONE, loggedNumbers)
                }
                editor.remove(KEY_REGISTRATION_ID)
                editor.remove(KEY_NOTIFICATIONS_ENABLED)
                editor.remove(KEY_GEOLOCATION_ENABLED)
                editor.remove(KEY_NO_MORE_DEMAND)
                editor.remove(isFirstTimeHome)
                editor.putInt(KEY_NB_OF_LAUNCH, 0)
                editor.putBoolean("translatedByDefault", true)
                editor.apply()
                removeAllPushNotifications()
                AnalyticsEvents.logEvent(AnalyticsEvents.EVENT_LOGOUT)
                mainActivity?.let {
                    startActivity(Intent(this, PreOnboardingStartActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    it.finish()
                }
            }
        } ?: run {
            Timber.d("not needed to logout")
        }
    }

    // ----------------------------------
    // Push notifications and badge handling
    // ----------------------------------
    fun onPushNotificationReceived(pushNotificationMessage: PushNotificationMessage) {
        val content = pushNotificationMessage.content ?: return
        if (content.joinableId == 0L) {
            return
        }
        Handler(Looper.getMainLooper())
            .post {
                when (content.type) {
                    PushNotificationContent.TYPE_NEW_CHAT_MESSAGE -> {
                        if (mainActivity?.displayMessageOnCurrentEntourageInfoFragment(pushNotificationMessage) == true) {
                            //already displayed
                            removePushNotification(content, content.type)
                        }
                    }
                    PushNotificationContent.TYPE_JOIN_REQUEST_CANCELED ->                     //@TODO should we update current entourage info fragment ?
                        removePushNotification(
                            content,
                            PushNotificationContent.TYPE_NEW_JOIN_REQUEST
                        )
                }
            }
    }

    fun addPushNotification(pushNotificationMessage: PushNotificationMessage) {
        //TODO EntBottomNavigationView.increaseBadgeCount()
    }

    private fun removePushNotification(content: PushNotificationContent, contentType: String) {
        if (content.isEntourageRelated) {
            removePushNotification(
                content.joinableId,
                TimestampedObject.ENTOURAGE_CARD,
                content.userId,
                contentType
            )
        }
    }

    private fun removePushNotification(feedId: Long, feedType: Int, userId: Int, pushType: String?) {
        val count =
            PushNotificationManager.removePushNotification(feedId, feedType, userId, pushType)
        if (count > 0) {
            //TODO EntBottomNavigationView.decreaseBadgeCount()
        }
    }

    private fun removeAllPushNotifications() {
        PushNotificationManager.removeAllPushNotifications()
        // reset the badge count
        //TODO EntBottomNavigationView.resetBadgeCount()
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE"
        const val KEY_REGISTRATION_ID = "ENTOURAGE_REGISTRATION_ID"
        const val KEY_NOTIFICATIONS_ENABLED = "ENTOURAGE_NOTIFICATION_ENABLED_V8"
        const val KEY_GEOLOCATION_ENABLED = "ENTOURAGE_GEOLOCATION_ENABLED"
        const val KEY_MIGRATION_V7_OK = "ENTOURAGE_MIGRATION_V7_OK"

        const val KEY_IS_FROM_ONBOARDING = "isFromOnboarding"
        const val KEY_ONBOARDING_USER_TYPE = "userType"
        const val KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN = "isFirstLogin"
        const val KEY_NB_OF_LAUNCH = "nbOfLaunch"
        const val KEY_NO_MORE_DEMAND = "noMoreDemand"
        const val isFirstTimeHome = "noMoreDemand"

        // ----------------------------------
        // MEMBERS
        // ----------------------------------
        private lateinit var instance: EntourageApplication
        fun get(): EntourageApplication {
            return instance
        }

        operator fun get(context: Context?): EntourageApplication {
            return (context?.applicationContext as? EntourageApplication) ?: get()
        }

        fun me(context: Context?): User? {
            return get(context).me()
        }
    }
}