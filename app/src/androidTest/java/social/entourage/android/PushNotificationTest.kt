package social.entourage.android

import android.Manifest
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.notifications.PushNotificationManager
import social.entourage.android.onboarding.login.LoginActivity

@RunWith(AndroidJUnit4::class)
class PushNotificationTest : EntourageTestAfterLogin() {

    private val isAppStarted = false
    private val NOTIFICATION_TIMEOUT = 10000L

    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    // This rule will grant the LOCATION permission before each test in this class
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private val entourageID = if(BuildConfig.FLAVOR_env=="prod") "46569" else "2300"

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @After
    override fun tearDown() {
        //keep it just for the annotation
        super.tearDown()
    }

    private fun startIntent(intent: Intent) {
        if(!isAppStarted) {
            context.startActivity(intent)
        } else {
            context.startActivity(intent)
        }
    }

    //TODO @Test
    fun testNotifMessageIntent() {
        forceLogIn()
        val intent = Intent(getApplicationContext<Application>(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        intent.action = PushNotificationContent.TYPE_NEW_CHAT_MESSAGE
        val args = Bundle()
        val myobject = "title"
        val myMessage = "Notif vers entourage de test"
        val content = """{
            "extra":{
                "joinable_id":$entourageID,
                "joinable_type":"Entourage",
                "group_type":"action",
                "instance":"$PushNotificationContent.TYPE_NEW_CHAT_MESSAGE",
                "instance_id":0
            },
            "message":"$myMessage"
            }""".trimIndent()
        val pushNotificationMessage = PushNotificationMessage("testeur Entourage", myobject, content, 0, null)
//        //
//        PushNotificationManager.displayFCMPushNotification(BuildConfig.DEEP_LINKS_SCHEME + "://profile","InApp vers Profil", "Doit ouvrir le profil", getApplicationContext<Application>())
//        //
//        PushNotificationManager.handlePushNotification(pushNotificationMessage, getApplicationContext<Application>())
//        //
        args.putSerializable(PushNotificationManager.PUSH_MESSAGE, pushNotificationMessage)
        intent.putExtras(args)
        startIntent(intent)
    }

    @Test
    fun testNotifMessage() {
        forceLogIn()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(instrumentation)
        if(!NotificationManagerCompat.from(EntourageApplication.get()).areNotificationsEnabled()) {
            Assert.fail(
                "Notifications not allowed for this app"
            )
        }

        MockNotificationGenerator.createConversationNotification(context)

        device.openNotification()
        val notificationTitleElement = device.wait(
            Until.findObject(By.textContains(MockNotificationGenerator.message_conversation)),
            NOTIFICATION_TIMEOUT
        )

        Assert.assertNotNull(
            "Notification with title '${MockNotificationGenerator.message_conversation}' not found in shade.",
            notificationTitleElement
        )

        device.pressBack()
    }
}