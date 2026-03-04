package social.entourage.android.afterLogin

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.MockNotificationGenerator
import social.entourage.android.test.BuildConfig

@LargeTest
@RunWith(AndroidJUnit4::class)
class PushNotificationTest : EntourageTestAfterLogin() {

    private val isAppStarted = false
    private val NOTIFICATION_TIMEOUT = 1000L

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    // This rule will grant the POST_NOTIFICATIONS permission before each test in this class
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val entourageID = if (BuildConfig.FLAVOR_env == "prod") "46569" else "2300"

    private fun checkNotifEnabled(activity: Context) {
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            Assert.fail(
                "Notifications not allowed for this app"
            )
        }
        NotificationManagerCompat.from(activity).cancelAll()
    }

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
            checkNotifEnabled(activity)
        }
    }

    private fun startIntent(intent: Intent) {
        if (!isAppStarted) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent)
        }
    }

    @Test
    fun testFCMNotifIntent() {
        val id = MockNotificationGenerator.createFCMNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifConversationMessage() {
        val id = MockNotificationGenerator.createConversationNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifContribMessage() {
        val id = MockNotificationGenerator.createContributionNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifOutingMessage() {
        val id = MockNotificationGenerator.createOutingNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifJoinRequestMessage() {
        val id = MockNotificationGenerator.createJoinRequestNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifInvitationMessage() {
        val id = MockNotificationGenerator.createInvitationNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifJoinRequestAcceptedMessage() {
        val id = MockNotificationGenerator.createJoinRequestAcceptedNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifSolicitationMessage() {
        val id = MockNotificationGenerator.createSolicitationNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifNeighborhoodPostMessage() {
        val id = MockNotificationGenerator.createNeighborhoodPostNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifOutingPostMessage() {
        val id = MockNotificationGenerator.createOutingPostNotification(context)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifWelcomeH1Message() {
        val id = MockNotificationGenerator.createWelcomeNotification(context, "h1", 10)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifWelcomeJ2Message() {
        val id = MockNotificationGenerator.createWelcomeNotification(context, "j2", 11)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifWelcomeJ5Message() {
        val id = MockNotificationGenerator.createWelcomeNotification(context, "j5", 12)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifWelcomeJ8Message() {
        val id = MockNotificationGenerator.createWelcomeNotification(context, "j8", 13)
        checkNotifMessage(id)
    }

    @Test
    fun testNotifWelcomeJ11Message() {
        val id = MockNotificationGenerator.createWelcomeNotification(context, "j11", 14)
        checkNotifMessage(id)
    }

    private fun checkNotifMessage(id: Int) {
        Thread.sleep(NOTIFICATION_TIMEOUT)
        val notifs = NotificationManagerCompat.from(context).activeNotifications
        var notificationId: Int? = null
        notifs.filter({ notification -> notification.id == id }).forEach { notification ->
            notificationId = notification.id
        }
        Assert.assertNotNull(
            "Notification with id '${id}' not found in shade.",
            notificationId
        )
    }
}
