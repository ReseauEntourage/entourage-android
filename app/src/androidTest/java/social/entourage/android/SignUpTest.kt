package social.entourage.android


import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
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

    val firstNameEt = onView(
            allOf(withId(R.id.ui_onboard_names_et_firstname),
                    childAtPosition(
                            allOf(withId(R.id.onboard_names_mainlayout),
                                    childAtPosition(
                                            withId(R.id.ui_container),
                                            0)),
                            2),
                    isDisplayed()))

    val lastNameEt = onView(
            allOf(withId(R.id.ui_onboard_names_et_lastname),
                    childAtPosition(
                            allOf(withId(R.id.onboard_names_mainlayout),
                                    childAtPosition(
                                            withId(R.id.ui_container),
                                            0)),
                            3),
                    isDisplayed()))

    val askPhoneNumberTv = onView(
            allOf(withId(R.id.ui_onboard_phone_tv_description),
                    withParent(allOf(withId(R.id.onboard_phone_mainlayout))),
                    isDisplayed()))

    val phoneNumberEt = onView(
            allOf(withId(R.id.ui_onboard_phone_et_phone),
                    childAtPosition(
                            allOf(withId(R.id.onboard_phone_mainlayout),
                                    childAtPosition(
                                            withId(R.id.ui_container),
                                            0)),
                            3),
                    isDisplayed()))

    val inputCodeTitleTv = onView(allOf(withId(R.id.ui_onboard_code_tv_title), isDisplayed()))

    val nextButton = onView(
            allOf(withId(R.id.ui_bt_next),
                    childAtPosition(
                            childAtPosition(
                                    withId(android.R.id.content),
                                    0),
                            3),
                    isDisplayed()))

    @Test
    fun validFirstNameAndLastNameTest() {
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())

        askPhoneNumberTv.check(matches(withText(R.string.onboard_phone_sub)))
        firstNameEt.check(doesNotExist())
        lastNameEt.check(doesNotExist())
    }

    @Test
    fun emptyFirstNameAndLastNameTest() {
        firstNameEt.perform(replaceText(""), closeSoftKeyboard())
        lastNameEt.perform(replaceText(""), closeSoftKeyboard())
        nextButton.perform(click())

        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))
        askPhoneNumberTv.check(doesNotExist())
    }

    @Test
    fun emptyFirstNameTest() {
        firstNameEt.perform(replaceText(""), closeSoftKeyboard())
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())

        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))
        askPhoneNumberTv.check(doesNotExist())
    }

    @Test
    fun emptyLastNameTest() {
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(replaceText(""), closeSoftKeyboard())
        nextButton.perform(click())

        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))
    }

    @Test
    fun emptyPhoneNumberTest() {
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())

        phoneNumberEt.perform(replaceText(""), closeSoftKeyboard())
        nextButton.perform(click())

        phoneNumberEt.check(matches(isDisplayed()))
        inputCodeTitleTv.check(doesNotExist())
    }

    @Test
    fun invalidPhoneNumberTest() {
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())

        phoneNumberEt.perform(replaceText("000000000"), closeSoftKeyboard())
        nextButton.perform(click())

        onView(withText(R.string.login_text_invalid_format)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }

    @Test
    fun alreadyUsedPhoneNumberTest() {
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())

        phoneNumberEt.perform(replaceText("123456789"), closeSoftKeyboard())
        nextButton.perform(click())

        Thread.sleep(5000) // Wait for API response
        onView(withText(R.string.login_already_registered_go_back)).check(matches(isDisplayed()))
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

    class ToastMatcher : TypeSafeMatcher<Root>() {
        override fun matchesSafely(item: Root?): Boolean {
            item?.windowLayoutParams?.get()?.type?.let { type ->
                if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                    val windowToken = item.decorView.windowToken
                    val appToken = item.decorView.applicationWindowToken
                    if (windowToken == appToken) {
                        //Means this window isn't contained by any other windows
                        return true
                    }
                }
            }
            return false
        }

        override fun describeTo(description: Description?) {}
    }
}
