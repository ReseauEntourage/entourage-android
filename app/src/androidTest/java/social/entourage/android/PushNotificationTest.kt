package social.entourage.android

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.notifications.PushNotificationManager

@RunWith(AndroidJUnit4::class)
class PushNotificationTest {

    private val isAppStarted = false

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(MainActivity::class.java, isAppStarted, isAppStarted)
    private val entourageID = if(BuildConfig.FLAVOR_env=="prod") "46569" else "2300"

    @Before
    fun setUp() {
    }

    private fun startIntent(intent: Intent) {
        if(!isAppStarted) {
            activityTestRule.launchActivity(intent)
        } else {
            activityTestRule.activity.startActivity(intent)
        }
    }

    @Test
    fun testNotifMessageIntent() {
        val intent = Intent(getApplicationContext<Application>(), MainActivity::class.java)
        intent.action = PushNotificationContent.TYPE_NEW_CHAT_MESSAGE
        val args = Bundle()
        val myobject = "title"
        val content = "{" +
                    "\"extra\":{" +
                        "\"joinable_id\":$entourageID," +
                        "\"joinable_type\":\"Entourage\"," +
                        "\"group_type\":\"action\"," +
                        "\"instance\":\"NEW_CHAT_MESSAGE\"," +
                        "\"instance_id\":0" +
                    "}," +
                    "\"message\":\"Notif vers entourage de test\"" +
                "}"
        val pushNotificationMessage = PushNotificationMessage("testeur Entourage", myobject, content,  0, null)
        //
        PushNotificationManager.displayFCMPushNotification(BuildConfig.DEEP_LINKS_SCHEME + "://profile","InApp vers Profil", "Doit ouvrir le profil", getApplicationContext<Application>())
        //
        PushNotificationManager.handlePushNotification(pushNotificationMessage, getApplicationContext<Application>())
        //
        args.putSerializable(PushNotificationManager.PUSH_MESSAGE, pushNotificationMessage)
        intent.putExtras(args)
        startIntent(intent)
        //Espresso.onView(ViewMatchers.withId(R.id.fragment_guide_new_entourages_button)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}