package social.entourage.android

import android.content.Intent
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PushNotificationTest {

    private val isAppStarted = true

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
    //TODO
    /*@Test
    fun testNotifMessageIntent() {
        val intent = Intent(getApplicationContext<Application>(), MainActivity_v7::class.java)
        intent.action = PushNotificationContent.TYPE_NEW_CHAT_MESSAGE
        val args = Bundle()
        val myobject = "title"
        val content = "{\"extra\":{\"joinable_id\":$entourageID,\"joinable_type\":\"Entourage\",\"group_type\":\"action\",\"type\":\"NEW_CHAT_MESSAGE\"},\"message\":\"Notif vers entourage de test\"}"
        val message = Message("testeur Entourage", myobject, content,  0, null)
        //
        PushNotificationManager.displayFCMPushNotification(BuildConfig.DEEP_LINKS_SCHEME + "://profile","InApp vers Profil", "Doit ouvrir le profil", getApplicationContext<Application>())
        //
        PushNotificationManager.handlePushNotification(message, getApplicationContext<Application>())
        //
        args.putSerializable(PushNotificationManager.PUSH_MESSAGE, message)
        intent.putExtras(args)
        startIntent(intent)
        Espresso.onView(ViewMatchers.withId(R.id.entourage_info_comment)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }*/
}