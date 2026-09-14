package social.entourage.android.afterLogin.enhanced_onboarding

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.enhanced_onboarding.EnhancedOnboarding

@RunWith(AndroidJUnit4::class)
@MediumTest
class EnhancedOnboardingTest : EntourageTestAfterLogin() {

    @get:Rule
    val activityRule = ActivityScenarioRule(EnhancedOnboarding::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun testEnhancedOnboardingContainerAndBackButtonAreDisplayed() {
        // Container is displayed
        onView(withId(R.id.fragment_container)).check(matches(isDisplayed()))

        // Back button is hidden on initial step (OnboardingPresentationFragment)
        onView(withId(R.id.btn_back)).check(matches(not(isDisplayed())))

        // Navigate to second step (OnboardingActionWishesFragment)
        onView(withId(R.id.button_start)).perform(scrollTo(), click())

        // Back button is displayed on second step
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()))
    }
}
