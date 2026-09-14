package social.entourage.android.afterLogin.events

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.events.EventFiltersActivity

@RunWith(AndroidJUnit4::class)
@MediumTest
class EventFiltersActivityTest : EntourageTestAfterLogin() {

    @get:Rule
    val activityRule = ActivityScenarioRule(EventFiltersActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun testEventFiltersUiElementsAreDisplayed() {
        onView(withId(R.id.title_search)).check(matches(isDisplayed()))
        onView(withId(R.id.validate)).check(matches(isDisplayed()))
        onView(withId(R.id.seekbar)).check(matches(isDisplayed()))
    }
}
