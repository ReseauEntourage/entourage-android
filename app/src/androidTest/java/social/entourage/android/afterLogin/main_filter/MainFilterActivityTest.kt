package social.entourage.android.afterLogin.main_filter

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.swipeUp
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
import social.entourage.android.main_filter.MainFilterActivity

@RunWith(AndroidJUnit4::class)
@MediumTest
class MainFilterActivityTest : EntourageTestAfterLogin() {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainFilterActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun testHeaderElementsAreDisplayedAndStayVisibleOnScroll() {
        // Verify title and back icon are displayed initially
        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
        onView(withId(R.id.icon_back)).check(matches(isDisplayed()))
        onView(withId(R.id.button_start)).check(matches(isDisplayed()))
        onView(withId(R.id.button_configure_later)).check(matches(isDisplayed()))

        // Scroll the content
        onView(withId(R.id.scrollView)).perform(swipeUp())

        // Verify title and back icon remain displayed after scroll
        onView(withId(R.id.tv_title)).check(matches(isDisplayed()))
        onView(withId(R.id.icon_back)).check(matches(isDisplayed()))
    }
}
