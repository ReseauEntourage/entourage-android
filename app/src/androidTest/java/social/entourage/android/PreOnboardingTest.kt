package social.entourage.android


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class PreOnboardingTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(PreOnboardingStartActivity::class.java)

    @Before
    fun setUp() {
        checkNoUserIsLoggedIn()
    }

    private fun checkNoUserIsLoggedIn() {
        try {
            activityRule.scenario.onActivity { activity ->
                EntourageApplication[activity].components.authenticationController.logOutUser()
            }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    @After
    fun tearDown() {
        checkNoUserIsLoggedIn()
    }

    @Test
    fun fullPreOnboardingTest() {
        val nextButton = onView(
                allOf(withId(R.id.ui_button_next), withText(R.string.pre_onboard_button_next),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                9),
                        isDisplayed()))

        val titleTv = onView(
                allOf(withId(R.id.ui_tv_title),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))

        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title2)))
        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title3)))
        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title4)))
        nextButton.perform(click())

        checkSignupAndLoginButtonsExist()
    }

    @Test
    fun skipPreOnboardingTest() {
        val connectButton = onView(
                allOf(withId(R.id.ui_button_connect), withText(R.string.pre_onboard_button_connect),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()))
        connectButton.perform(click())

        checkSignupAndLoginButtonsExist()
    }

    private fun checkSignupAndLoginButtonsExist() {
        val button = onView(
                allOf(withId(R.id.ui_button_signup), withText(R.string.pre_onboard_choice_signup),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))
        button.check(matches(isDisplayed()))

        val button2 = onView(
                allOf(withId(R.id.ui_button_login), withText(R.string.pre_onboard_choice_login),
                        withParent(withParent(withId(android.R.id.content))),
                        isDisplayed()))
        button2.check(matches(isDisplayed()))
    }

    private fun childAtPosition(
            parentMatcher: Matcher<View>, position: Int): Matcher<View> {

        return object : TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("Child at position $position in parent ")
                parentMatcher.describeTo(description)
            }

            public override fun matchesSafely(view: View): Boolean {
                val parent = view.parent
                return parent is ViewGroup && parentMatcher.matches(parent)
                        && view == parent.getChildAt(position)
            }
        }
    }
}
