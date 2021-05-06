package social.entourage.android


import android.view.View
import android.view.ViewGroup
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.OnboardingMainActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignUpTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(OnboardingMainActivity::class.java)

    @Test
    fun onboardingMainActivityTest() {
        val firstNameEt = onView(
                allOf(withId(R.id.ui_onboard_names_et_firstname),
                        childAtPosition(
                                allOf(withId(R.id.onboard_names_mainlayout),
                                        childAtPosition(
                                                withId(R.id.ui_container),
                                                0)),
                                2),
                        isDisplayed()))
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())

        val lastNameEt = onView(
                allOf(withId(R.id.ui_onboard_names_et_lastname),
                        childAtPosition(
                                allOf(withId(R.id.onboard_names_mainlayout),
                                        childAtPosition(
                                                withId(R.id.ui_container),
                                                0)),
                                3),
                        isDisplayed()))
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())

        val nextButton = onView(
                allOf(withId(R.id.ui_bt_next),
                        childAtPosition(
                                childAtPosition(
                                        withId(android.R.id.content),
                                        0),
                                3),
                        isDisplayed()))
        nextButton.perform(click())

        val askPhoneNumberTv = onView(
                allOf(withId(R.id.ui_onboard_phone_tv_description),
                        withParent(allOf(withId(R.id.onboard_phone_mainlayout))),
                        isDisplayed()))
        askPhoneNumberTv.check(matches(withText(R.string.onboard_phone_sub)))

//        val phoneEt = onView(
//                allOf(withId(R.id.ui_onboard_phone_et_phone),
//                        childAtPosition(
//                                allOf(withId(R.id.onboard_phone_mainlayout),
//                                        childAtPosition(
//                                                withId(R.id.ui_container),
//                                                0)),
//                                3),
//                        isDisplayed()))
//        phoneEt.perform(replaceText("601928374"), closeSoftKeyboard())
//
//        nextButton.perform(click())
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
