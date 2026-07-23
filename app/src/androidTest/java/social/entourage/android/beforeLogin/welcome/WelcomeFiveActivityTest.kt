package social.entourage.android.beforeLogin.welcome

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.welcome.WelcomeFiveActivity

@RunWith(AndroidJUnit4::class)
class WelcomeFiveActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(WelcomeFiveActivity::class.java)

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
        onView(withId(R.id.main_button)).check(matches(isDisplayed()))
    }
}
