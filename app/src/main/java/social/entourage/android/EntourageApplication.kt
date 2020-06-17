package social.entourage.android

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.multidex.MultiDexApplication
import com.google.firebase.analytics.FirebaseAnalytics
import me.leolin.shortcutbadger.ShortcutBadger
import net.danlew.android.joda.JodaTimeAndroid
import social.entourage.android.api.ApiModule
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.User
import social.entourage.android.api.model.feed.FeedItem
import social.entourage.android.authentication.AuthenticationModule
import social.entourage.android.authentication.login.LoginActivity
import social.entourage.android.message.push.PushNotificationManager
import social.entourage.android.newsfeed.FeedItemsStorage
import social.entourage.android.onboarding.LoginNewActivity
import timber.log.Timber
import java.util.*

/**
 * Application setup for Analytics, JodaTime and Dagger
 */
class EntourageApplication : MultiDexApplication() {
    lateinit var entourageComponent: EntourageComponent
    private val activities: ArrayList<EntourageActivity>  = ArrayList()
    var badgeCount = 0
        private set
    private lateinit var feedItemsStorage: FeedItemsStorage
    lateinit var sharedPreferences: SharedPreferences
    private lateinit var librariesSupport: LibrariesSupport

    enum class WhiteLabelApp {
        ENTOURAGE_APP, PFP_APP
    }

    // ----------------------------------
    // LIFECYCLE
    // ----------------------------------
    override fun onCreate() {
        super.onCreate()
        activities.clear()
        instance = this
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        librariesSupport = LibrariesSupport()
        librariesSupport.setupLibraries(this)
        JodaTimeAndroid.init(this)
        setupDagger()
        setupFeedItemsStorage()
        setupSharedPreferences()
        setupBadgeCount()
    }

    private fun setupDagger() {
        entourageComponent = DaggerEntourageComponent.builder()
                .entourageModule(EntourageModule(this))
                .apiModule(ApiModule())
                .authenticationModule(AuthenticationModule())
                .build()
        entourageComponent.inject(this)
    }

    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
    }

    private fun setupBadgeCount() {
        updateBadgeCount()
    }

    val firebase: FirebaseAnalytics
        get() = librariesSupport.firebaseAnalytics

    fun me(): User? {
        return entourageComponent.authenticationController?.user ?: return null
    }

    fun onActivityCreated(activity: EntourageActivity) {
        activities.add(activity)
    }

    fun onActivityDestroyed(activity: EntourageActivity) {
        activities.remove(activity)
        saveFeedItemsStorage()
    }

    private val loginActivity: LoginNewActivity?
        get() {
            for (activity in activities) {
                if (activity is LoginNewActivity) {
                    return activity
                }
            }
            return null
        }

    fun finishLoginActivity() {
        Timber.d("Finishing login activity")
        loginActivity?.finish()
    }

    // ----------------------------------
    // Push notifications and badge handling
    // ----------------------------------
    private fun updateBadgeCount() {
        val me = me() ?:return
        if (badgeCount == feedItemsStorage.getBadgeCount(me.id)) {
            return
        }
        badgeCount = feedItemsStorage.getBadgeCount(me.id)
        if (badgeCount == 0) {
            ShortcutBadger.removeCount(applicationContext)
        } else {
            ShortcutBadger.applyCount(applicationContext, badgeCount)
        }
    }

    fun addPushNotification(message: Message) {
        PushNotificationManager.addPushNotification(message)
        if (storeNewPushNotification(message, true) > 1) {
            //feedItem badge was already set
            return
        }
        updateBadgeCount()
    }

    fun removePushNotificationsForFeedItem(feedItem: FeedItem) {
        val count = PushNotificationManager.removePushNotificationsForFeedItem(feedItem)
        if (count > 0) {
            updateStorageFeedItem(feedItem)
        }
    }

    fun removePushNotification(message: Message) {
        val count = PushNotificationManager.removePushNotification(message)
        if (count > 0) {
            if (storeNewPushNotification(message, false) == 0) {
                //feedItem badge was set to 0
                updateBadgeCount()
            }
        }
    }

    fun removePushNotification(feedItem: FeedItem, userId: Int, pushType: String?) {
        removePushNotification(feedItem.id, feedItem.type, userId, pushType)
    }

    fun removePushNotification(feedId: Long, feedType: Int, userId: Int, pushType: String?) {
        val count = PushNotificationManager.removePushNotification(feedId, feedType, userId, pushType)
        if (count > 0) {
            updateBadgeCount()
        }
    }

    fun removeAllPushNotifications() {
        PushNotificationManager.removeAllPushNotifications()
        // reset the badge count
        updateBadgeCount()
    }

    fun updateBadgeCountForFeedItem(feedItem: FeedItem?) {
        updateStorageFeedItem(feedItem)
        updateBadgeCount()
    }

    // ----------------------------------
    // FeedItemsStorage
    // ----------------------------------
    private fun setupFeedItemsStorage() {
        feedItemsStorage = entourageComponent.complexPreferences?.getObject(FeedItemsStorage.KEY, FeedItemsStorage::class.java) ?: FeedItemsStorage()
    }

    private fun saveFeedItemsStorage() {
        val preferences = entourageComponent.complexPreferences ?: return
        preferences.putObject(FeedItemsStorage.KEY, feedItemsStorage)
        preferences.commit()
    }

    fun storeNewPushNotification(message: Message?, isAdded: Boolean): Int {
        val me = entourageComponent.authenticationController?.user ?: return -1
        return feedItemsStorage.saveFeedItem(me.id, message, isAdded)
    }

    fun updateStorageInvitationCount(count: Int) {
        feedItemsStorage.updateInvitationCount(count)
    }

    private fun updateStorageFeedItem(feedItem: FeedItem?) {
        val me = entourageComponent.authenticationController?.user ?: return
        feedItemsStorage.updateFeedItem(me.id, feedItem)
    }

    fun clearFeedStorage(): Boolean {
        val me = entourageComponent.authenticationController?.user ?: return false
        return feedItemsStorage.clear(me.id)
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
        // ----------------------------------
        // MEMBERS
        // ----------------------------------
        private lateinit var instance: EntourageApplication
        @JvmStatic
        fun get(): EntourageApplication {
            return instance
        }

        const val ENTOURAGE_APP = "entourage"
        const val PFP_APP = "pfp"
        @JvmStatic
        operator fun get(context: Context?): EntourageApplication {
            return (if (context != null) context.applicationContext as EntourageApplication else get())
        }

        @JvmStatic
        fun me(context: Context?): User? {
            return get(context).me()
        }

        // ----------------------------------
        // Multiple App support methods
        // ----------------------------------
        private fun isCurrentApp(appName: String): Boolean {
            return BuildConfig.FLAVOR.contains(appName)
        }
    }
}