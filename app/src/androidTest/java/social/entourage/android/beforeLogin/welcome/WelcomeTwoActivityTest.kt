package social.entourage.android.beforeLogin.welcome

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.welcome.WelcomeTwoActivity

@RunWith(AndroidJUnit4::class)
class WelcomeTwoActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(WelcomeTwoActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }


    @Test
    fun test_display() {
        onView(withId(R.id.button_say_hello)).check(matches(isDisplayed()))
    }
    @Test
    fun test_close_button() {
        onView(withId(R.id.btn_close)).perform(click())
    }
}
