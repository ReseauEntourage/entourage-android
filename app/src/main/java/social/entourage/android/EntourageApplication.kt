package social.entourage.android

import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.google.firebase.analytics.FirebaseAnalytics
import social.entourage.android.api.ApiModule
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.api.model.TimestampedObject
import social.entourage.android.api.model.User
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.api.tape.Events
import social.entourage.android.authentication.AuthenticationController
import social.entourage.android.authentication.ComplexPreferences
import social.entourage.android.base.BaseActivity
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.navigation.EntBottomNavigationView
import social.entourage.android.base.newsfeed.UserFeedItemListCache
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.tools.LibrariesSupport
import timber.log.Timber
import java.util.*

/**
 * Application setup for Analytics, JodaTime and Dagger
 */
class EntourageApplication : MultiDexApplication() {
    private val activities: ArrayList<BaseActivity> = ArrayList()
    private lateinit var userFeedItemListCache: UserFeedItemListCache
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var librariesSupport: LibrariesSupport
    lateinit var authenticationController: AuthenticationController
    lateinit var complexPreferences: ComplexPreferences
    lateinit var apiModule: ApiModule


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
        setupFeedItemsStorage()
        setupSharedPreferences()
    }

    private fun setupSharedPreferences() {
        sharedPreferences =
            getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
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
        saveFeedItemsStorage()
    }

    private val loginActivity: LoginActivity?
        get() {
            activities.filterIsInstance<LoginActivity>().forEach {
                return it
            }
            return null
        }


    private val newMainActivity: social.entourage.android.new_v8.MainActivity?
        get() {
            activities.filterIsInstance<social.entourage.android.new_v8.MainActivity>().forEach {
                return it
            }
            return null
        }

    fun finishLoginActivity() {
        Timber.d("Finishing login activity")
        loginActivity?.finish()
    }

    fun logOut() {
        newMainActivity?.logout()
    }

    fun getMainActivity() : social.entourage.android.new_v8.MainActivity? {
        return newMainActivity
    }

    // ----------------------------------
    // Push notifications and badge handling
    // ----------------------------------
    fun onPushNotificationReceived(message: Message) {
        val content = message.content ?: return
        if (content.joinableId == 0L) {
            return
        }
        Handler(Looper.getMainLooper())
            .post {
                when (content.type) {
                    PushNotificationContent.TYPE_NEW_CHAT_MESSAGE -> {
                        if (newMainActivity?.displayMessageOnCurrentEntourageInfoFragment(message) == true) {
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

    fun addPushNotification(message: Message) {
        PushNotificationManager.addPushNotification(message)
        if (storeNewPushNotification(message, true) > 1) {
            //feedItem badge was already set
            return
        }
        EntBottomNavigationView.increaseBadgeCount()
    }

    fun removePushNotificationsForFeedItem(feedItem: FeedItem) {
        val count = PushNotificationManager.removePushNotificationsForFeedItem(feedItem)
        if (count > 0) {
            updateStorageFeedItem(feedItem)
            EntBottomNavigationView.decreaseBadgeCount()
        }
    }

    fun removePushNotification(message: Message) {
        val count = PushNotificationManager.removePushNotification(message)
        if (count > 0) {
            if (storeNewPushNotification(message, false) == 0) {
                //feedItem badge was set to 0
                EntBottomNavigationView.decreaseBadgeCount()
            }
        }
    }

    fun removePushNotification(feedItem: FeedItem, userId: Int, pushType: String?) {
        removePushNotification(feedItem.id, feedItem.type, userId, pushType)
    }

    fun removePushNotification(content: PushNotificationContent, contentType: String) {
        if (content.isEntourageRelated) {
            removePushNotification(
                content.joinableId,
                TimestampedObject.ENTOURAGE_CARD,
                content.userId,
                contentType
            )
        }
    }

    fun removePushNotification(feedId: Long, feedType: Int, userId: Int, pushType: String?) {
        val count =
            PushNotificationManager.removePushNotification(feedId, feedType, userId, pushType)
        if (count > 0) {
            EntBottomNavigationView.decreaseBadgeCount()
        }
    }

    fun removeAllPushNotifications() {
        PushNotificationManager.removeAllPushNotifications()
        // reset the badge count
        EntBottomNavigationView.resetBadgeCount()
    }

    fun updateBadgeCountForFeedItem(feedItem: FeedItem) {
        updateStorageFeedItem(feedItem)
    }

    // ----------------------------------
    // FeedItemsStorage
    // ----------------------------------
    private fun setupFeedItemsStorage() {
        userFeedItemListCache = complexPreferences.getObject(
            UserFeedItemListCache.KEY,
            UserFeedItemListCache::class.java
        )
            ?: UserFeedItemListCache()
    }

    private fun saveFeedItemsStorage() {
        complexPreferences.apply {
            this.putObject(UserFeedItemListCache.KEY, userFeedItemListCache)
            this.commit()
        }
    }

    fun storeNewPushNotification(message: Message, isAdded: Boolean): Int {
        val me = authenticationController.me ?: return -1
        return userFeedItemListCache.saveFeedItemFromNotification(me.id, message, isAdded)
    }

    private fun updateStorageFeedItem(feedItem: FeedItem) {
        val me = authenticationController.me ?: return
        userFeedItemListCache.updateFeedItem(me.id, feedItem)
    }

    fun clearFeedStorage(): Boolean {
        val me = authenticationController.me ?: return false
        return userFeedItemListCache.clear(me.id)
    }

    companion object {
        // ----------------------------------
        // CONSTANTS
        // ----------------------------------
        const val KEY_TUTORIAL_DONE = "social.entourage.android.KEY_TUTORIAL_DONE"
        const val KEY_REGISTRATION_ID = "ENTOURAGE_REGISTRATION_ID"
        const val KEY_NOTIFICATIONS_ENABLED = "ENTOURAGE_NOTIFICATION_ENABLED"
        const val KEY_GEOLOCATION_ENABLED = "ENTOURAGE_GEOLOCATION_ENABLED"

        const val KEY_IS_FROM_ONBOARDING = "isFromOnboarding"
        const val KEY_ONBOARDING_USER_TYPE = "userType"
        const val KEY_ONBOARDING_SHOW_POP_FIRSTLOGIN = "isFirstLogin"
        const val KEY_NB_OF_LAUNCH = "nbOfLaunch"
        const val KEY_NO_MORE_DEMAND = "noMoreDemand"

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