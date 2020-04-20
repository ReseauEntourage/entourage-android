package social.entourage.android

import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.api.model.Message
import social.entourage.android.api.model.PushNotificationContent
import social.entourage.android.message.push.PushNotificationManager

@RunWith(AndroidJUnit4::class)
class PushNotificationTest {

    @Rule
    @JvmField
    val activityTestRule = ActivityTestRule(MainActivity::class.java)
    //Staging
    //private val entourageHash =
    //private val entourageID = "2300"
    //Prod
    private val entourageHash = if(BuildConfig.FLAVOR_env=="prod") "eu_LTSFD6OEc" else "eP8v6B2UYM44"
    private val entourageID = if(BuildConfig.FLAVOR_env=="prod") "46569" else "2300"

    @Before
    fun setUp() {
    }

    @Test
    fun testNotifMessageIntent() {
        val intent = Intent(getApplicationContext<Application>(), MainActivity::class.java)
        intent.action = PushNotificationContent.TYPE_NEW_CHAT_MESSAGE
        val args = Bundle()
        val myobject = "title"
        val content = "{\"extra\":{\"joinable_id\":"+entourageID+",\"joinable_type\":\"Entourage\",\"group_type\":\"action\",\"type\":\"NEW_CHAT_MESSAGE\"},\"message\":\"test\"}"
        val message = Message("test Entourage", myobject, content,  0, null)
        args.putSerializable(PushNotificationManager.PUSH_MESSAGE, message)
        intent.putExtras(args);
        activityTestRule.activity.startActivity(intent)
        Espresso.onView(ViewMatchers.withId(R.id.entourage_info_comment)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}