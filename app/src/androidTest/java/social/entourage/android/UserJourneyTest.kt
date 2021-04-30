package social.entourage.android


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class UserJourneyTest {

    @Rule
    @JvmField
    var mActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        checkNoUserIsLoggedIn()
    }

    private fun checkNoUserIsLoggedIn() {
        EntourageApplication.get(mActivityTestRule.activity).components.authenticationController.logOutUser()
    }

    @Test
    fun testFromLaunchToHome() {
        testLoginButton()
        testAlreadyHaveAccountButton()
        testConnectionButton()
    }

    private fun testLoginButton() {
        val appCompatButton = onView(
                allOf(withId(R.id.ui_button_connect), withText("Me connecter"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                2),
                        isDisplayed()))
        appCompatButton.perform(click())

        val viewGroup = onView(
                allOf(withId(R.id.ui_button_login), withText("J’ai déjà un compte"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()))
        viewGroup.check(matches(isDisplayed()))
    }

    private fun testAlreadyHaveAccountButton() {
        val appCompatButton2 = onView(
                allOf(withId(R.id.ui_button_login), withText("J’ai déjà un compte"),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()))
        appCompatButton2.perform(click())

        val viewGroup2 = onView(
                allOf(withId(R.id.ui_login_button_signup), withText("Je me connecte"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.onboard_login_mainlayout),
                                        5),
                                3),
                        isDisplayed()))
        viewGroup2.check(matches(isDisplayed()))
    }

    private fun testConnectionButton() {
        val appCompatEditText = onView(
                allOf(withId(R.id.ui_login_phone_et_phone),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.onboard_login_mainlayout),
                                        5),
                                1),
                        isDisplayed()))
        appCompatEditText.perform(replaceText("651234145"), closeSoftKeyboard())

        val appCompatEditText2 = onView(
                allOf(withId(R.id.ui_login_et_code),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.onboard_login_mainlayout),
                                        5),
                                2),
                        isDisplayed()))
        appCompatEditText2.perform(replaceText("661192"), closeSoftKeyboard())

        val appCompatButton3 = onView(
                allOf(withId(R.id.ui_login_button_signup), withText("Je me connecte"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.onboard_login_mainlayout),
                                        5),
                                3),
                        isDisplayed()))
        appCompatButton3.perform(click())

        val viewGroup = onView(
                allOf(withParent(allOf(withId(R.id.ui_recyclerview),
                        withParent(withId(R.id.ui_home_swipeRefresh)))),
                        isDisplayed()))
        viewGroup.check(matches(isDisplayed()))
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
