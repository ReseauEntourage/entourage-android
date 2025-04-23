package social.entourage.android

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.GeneralLocation
import androidx.test.espresso.action.GeneralSwipeAction
import androidx.test.espresso.action.Press
import androidx.test.espresso.action.Swipe
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

    private val nextButton = onView(
            allOf(withId(R.id.ui_button_next), withText(R.string.pre_onboard_button_next),
                    childAtPosition(
                            childAtPosition(
                                    withId(android.R.id.content),
                                    0),
                            9),
                    isDisplayed()
            )
    )

    private val imagesRecyclerView = onView(
            allOf(withId(R.id.ui_recyclerView),
                    isDisplayed()
            )
    )

    private val titleTv = onView(
            allOf(withId(R.id.ui_tv_title),
                    withParent(withParent(withId(android.R.id.content))),
                    isDisplayed()
            )
    )

    @Before
    fun setUp() {
        checkNoUserIsLoggedIn()
    }

    private fun checkNoUserIsLoggedIn() {
        try {
            activityRule.scenario.onActivity { activity ->
                EntourageApplication[activity].authenticationController.logOutUser()
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
    //TODO
    /*@Test
    fun skipPreOnboardingAtPage2Test() {
        checkPage2()
        skipPreOnboardingTest()
    }*/
    //TODO
    /*@Test
    fun skipPreOnboardingAtPage3Test() {
        checkPage2()
        checkPage3()
        skipPreOnboardingTest()
    }*/
    //TODO
    /*@Test
    fun skipPreOnboardingAtPage4Test() {
        checkPage2()
        checkPage3()
        checkPage4()
        skipPreOnboardingTest()
    }*/
    //TODO
    /*@Test
    fun fullPreOnboardingTest() {
        checkPage2()
        checkPage3()
        checkPage4()
        nextButton.perform(click())
        checkSignupAndLoginButtonsExist()
    }*/
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
    //TODO
    /*@Test
    fun skipPreOnboardingAtPage4WithScrollingTest() {
        checkPage2WithScrolling()
        checkPage3WithScrolling()
        checkPage4WithScrolling()
        skipPreOnboardingTest()
    }*/
    //TODO
    /*@Test
    fun fullPreOnboardingWithScrollingTest() {
        checkPage2WithScrolling()
        checkPage3WithScrolling()
        checkPage4WithScrolling()
        nextButton.perform(click())
        checkSignupAndLoginButtonsExist()
    }*/

    /*private fun checkPage2() {
        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title2)))
    }

    private fun checkPage2WithScrolling() {
        swipeLeftFromPage(0)
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title2)))
    }

    private fun checkPage3() {
        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title3)))
    }

    private fun checkPage3WithScrolling() {
        swipeLeftFromPage(1)
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title3)))
    }

    private fun checkPage4() {
        nextButton.perform(click())
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title4)))
    }

    private fun checkPage4WithScrolling() {
        swipeLeftFromPage(2)
        titleTv.check(matches(withText(R.string.pre_onboard_tutorial_title4)))
    }*/

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
