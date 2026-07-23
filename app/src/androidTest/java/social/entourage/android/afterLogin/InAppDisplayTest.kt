package social.entourage.android.afterLogin

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.firebase.inappmessaging.FirebaseInAppMessaging
import org.junit.Rule
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@LargeTest
@RunWith(AndroidJUnit4::class)
class InAppDisplayTest : EntourageTestAfterLogin() {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    //TODO @Test
    fun testInAppMessageIsDisplayed() {
        val testEventName = "Test FP DBG"

        val fiam = FirebaseInAppMessaging.getInstance()

        // Make sure messages are not suppressed during the test
        fiam.setMessagesSuppressed(false)

        // Trigger the event
        // We run this on the main thread using onActivity
        val latch = CountDownLatch(1)
        activityRule.scenario.onActivity {
            fiam.triggerEvent(testEventName)
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS) // Wait for trigger to be called

        // Add a small delay to allow the message to be fetched and displayed.
        // For more robust tests, consider using Espresso Idling Resources.
        Thread.sleep(2000)

        // Use Espresso to verify the message is shown.
        // Replace "Your Test Message Title" with the actual title from your campaign.
        Espresso.onView(ViewMatchers.withText("Test FP")).check(
            ViewAssertions.matches(
            ViewMatchers.isDisplayed()))
    }
}