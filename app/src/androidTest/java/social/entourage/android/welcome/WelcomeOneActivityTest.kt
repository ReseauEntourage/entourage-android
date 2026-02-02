package social.entourage.android.welcome

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R
import social.entourage.android.home.pedago.PedagoListActivity

@RunWith(AndroidJUnit4::class)
class WelcomeOneActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(WelcomeOneActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun test_welcome_one_display() {
        onView(withId(R.id.title_welcome_one)).check(matches(isDisplayed()))
        onView(withId(R.id.content_welcome_one)).check(matches(isDisplayed()))
    }

    @Test
    fun test_welcome_one_close_button() {
        onView(withId(R.id.close_button)).perform(click())
        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun test_welcome_one_pedago_link() {
        onView(withId(R.id.tv_endLine)).perform(click())
        intended(hasComponent(PedagoListActivity::class.java.name))
    }
}
