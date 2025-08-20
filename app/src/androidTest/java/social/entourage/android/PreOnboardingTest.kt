package social.entourage.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withParent
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingStartActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class PreOnboardingTest : EntourageTestBeforeLogin() {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(PreOnboardingStartActivity::class.java)

    private val nextButton = onView(
            allOf(
                withId(R.id.ui_button_next),
                withText(R.string.pre_onboard_button_next),
                isDisplayed()
            )
    )

    private val titleTv = onView(
        allOf(
            withId(R.id.ui_tv_title),
                withParent(withParent(withId(android.R.id.content))),
                isDisplayed()
        )
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

    @Test
    fun skipPreOnboarding() {
        val connectButton = onView(
            allOf(
                withId(R.id.ui_button_connect),
                withText(R.string.bt_pass),
                isDisplayed()
            )
        )
        connectButton.perform(click())

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
        nextButton.perform(click())
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
        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.intro_title_2)))
    }

    /*private fun checkPage2WithScrolling() {
        swipeLeftFromPage(0)
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title2)))
    }*/

    private fun checkPage3() {
        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.intro_title_3)))
    }

    /*private fun checkPage3WithScrolling() {
        swipeLeftFromPage(1)
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title3)))
    }*/

    private fun checkSignupAndLoginButtonsExist() {
        val button = onView(
            allOf(
                withId(R.id.ui_button_signup),
                withText(R.string.pre_onboard_choice_signup),
                isDisplayed()))
        button.check(matches(isDisplayed()))

        val button2 = onView(
            allOf(
                withId(R.id.ui_button_login),
                withText(R.string.pre_onboard_choice_login),
                isDisplayed()))
        button2.check(matches(isDisplayed()))
    }
}
