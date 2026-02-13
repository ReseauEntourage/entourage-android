package social.entourage.android.beforeLogin

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class PreOnboardingTest : EntourageTestBeforeLogin() {

    @get:Rule
    val activityRule = ActivityScenarioRule(PreOnboardingStartActivity::class.java)

    private val nextButton = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_button_next),
            ViewMatchers.withText(R.string.pre_onboard_button_next),
            ViewMatchers.isDisplayed()
        )
    )

    private val titleTv = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_tv_title),
            ViewMatchers.withParent(ViewMatchers.withParent(ViewMatchers.withId(android.R.id.content))),
            ViewMatchers.isDisplayed()
        )
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun skipPreOnboarding() {
        val connectButton = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.ui_button_connect),
                ViewMatchers.withText(R.string.bt_pass),
                ViewMatchers.isDisplayed()
            )
        )
        connectButton.perform(ViewActions.click())

        checkSignupAndLoginButtonsExist()
    }

    @Test
    fun skipPreOnboardingAtPage2Test() {
        checkPage2()
        skipPreOnboarding()
    }

    @Test
    fun skipPreOnboardingAtPage3Test() {
        checkPage2()
        checkPage3()
        nextButton.perform(ViewActions.click())
        checkSignupAndLoginButtonsExist()
    }
    //TODO
    /*@Test
    fun skipPreOnboardingAtPage2WithScrollingTest() {
        checkPage2WithScrolling()
        skipPreOnboardingTest()
    }*/
    //TODO
    /*@Test
    fun skipPreOnboardingAtPage3WithScrollingTest() {
        checkPage2WithScrolling()
        checkPage3WithScrolling()
        skipPreOnboardingTest()
    }*/

    private fun checkPage2() {
        nextButton.perform(ViewActions.click())
        titleTv.check(ViewAssertions.matches(ViewMatchers.withText(R.string.intro_title_2)))
    }

    /*private fun checkPage2WithScrolling() {
        swipeLeftFromPage(0)
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title2)))
    }*/

    private fun checkPage3() {
        nextButton.perform(ViewActions.click())
        titleTv.check(ViewAssertions.matches(ViewMatchers.withText(R.string.intro_title_3)))
    }

    /*private fun checkPage3WithScrolling() {
        swipeLeftFromPage(1)
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title3)))
    }*/

    private fun checkSignupAndLoginButtonsExist() {
        val button = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.ui_button_signup),
                ViewMatchers.withText(R.string.pre_onboard_choice_signup),
                ViewMatchers.isDisplayed()
            )
        )
        button.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        val button2 = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.ui_button_login),
                ViewMatchers.withText(R.string.pre_onboard_choice_login),
                ViewMatchers.isDisplayed()
            )
        )
        button2.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}
