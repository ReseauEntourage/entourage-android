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
import social.entourage.android.api.OnboardingAPI
import social.entourage.android.onboarding.OnboardingMainActivity
import java.util.*

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
        fillValidNames()

        //Check that OnboardingPhoneFragment is displayed
        askPhoneNumberTv.check(matches(withText(R.string.onboard_phone_sub)))

        //Check that OnboardingNamesFragment is not displayed
        firstNameEt.check(doesNotExist())
        lastNameEt.check(doesNotExist())
    }

    @Test
    fun emptyFirstNameAndLastNameTest() {
        firstNameEt.perform(replaceText(""), closeSoftKeyboard())
        lastNameEt.perform(replaceText(""), closeSoftKeyboard())
        nextButton.perform(click())

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askPhoneNumberTv.check(doesNotExist())
    }

    @Test
    fun emptyFirstNameTest() {
        firstNameEt.perform(replaceText(""), closeSoftKeyboard())
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askPhoneNumberTv.check(doesNotExist())
    }

    @Test
    fun emptyLastNameTest() {
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(replaceText(""), closeSoftKeyboard())
        nextButton.perform(click())

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askPhoneNumberTv.check(doesNotExist())
    }

    //TODO For now there is no simple way to send a phone number and remove it to be able running the test again
    // It would need for instance an API call that remove the sent number so that it is not already used for the next test call
//    @Test
//    fun validPhoneNumberTest() {
//        fillValidNames()
//
//        phoneNumberEt.perform(replaceText(""), closeSoftKeyboard())
//        nextButton.perform(click())
//
//        //Check that toast shows given message
//        onView(withText(R.string.login_smscode_sent)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
//    }

    @Test
    fun emptyPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(replaceText(""), closeSoftKeyboard())
        nextButton.perform(click())

        //Check that OnboardingPhoneFragment is still displayed
        phoneNumberEt.check(matches(isDisplayed()))

        //Check that OnboardingPasscodeFragment is not displayed
        val inputCodeTitleTv = onView(allOf(withId(R.id.ui_onboard_code_tv_title), isDisplayed()))
        inputCodeTitleTv.check(doesNotExist())
    }

    @Test
    fun invalidPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(replaceText("000000000"), closeSoftKeyboard())
        nextButton.perform(click())

        //Check that toast shows given message
        onView(withText(R.string.login_text_invalid_format)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
    }

    @Test
    fun alreadyUsedPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(replaceText("123456789"), closeSoftKeyboard())
        nextButton.perform(click())

        //Check that dialog shows given message
        Thread.sleep(5000) // Wait for API response
        onView(withText(R.string.login_already_registered_go_back)).check(matches(isDisplayed()))
    }

    @Test
    fun validPasscodeTest() {
        fillValidNames()

        //Skip phone number validation and go to passcode fragment
        goNextStep()

        //Login to an already existing account
        login("0651234145", "661192")

        //Skip passcode validation and go to type fragment
        goNextStep()

        //Check that OnboardingTypeFragment is displayed
        val typeSubTitleTv = onView(
                allOf(withId(R.id.ui_onboard_type_tv_description),
                        withParent(allOf(withId(R.id.onboard_phone_mainlayout))),
                        isDisplayed()))
        typeSubTitleTv.check(matches(withText(R.string.onboard_type_sub)))
    }

    @Test
    fun neighbourTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_neighbour)

        //Check that OnboardingPlaceFragment is displayed
        val placeTitleTv = onView(
                allOf(withId(R.id.ui_onboard_place_tv_title),
                        withParent(allOf(withId(R.id.onboard_phone_mainlayout))),
                        isDisplayed()))
        placeTitleTv.check(matches(withText(R.string.onboard_place_title)))
    }

    @Test
    fun aloneTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_alone)

        //Check that OnboardingPlaceFragment is displayed
        val placeTitleTv = onView(
                allOf(withId(R.id.ui_onboard_place_tv_title),
                        withParent(allOf(withId(R.id.onboard_phone_mainlayout))),
                        isDisplayed()))
        placeTitleTv.check(matches(withText(R.string.onboard_place_title_sdf)))
    }

    @Test
    fun assosTypeTest() {
        clickTypeButton(R.id.ui_onboard_type_layout_assos)

        //Check that OnboardingAssoStartFragment is displayed
        val assoTitleTv = onView(
                allOf(withId(R.id.ui_onboard_asso_start_tv_title),
                    withParent(allOf(withId(R.id.onboard_phone_mainlayout))),
                    isDisplayed()))
        assoTitleTv.check(matches(withText(R.string.onboard_asso_start_title)))
    }

    private fun fillValidNames() {
        firstNameEt.perform(replaceText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(replaceText("Dupont"), closeSoftKeyboard())
        nextButton.perform(click())
    }

    private fun goNextStep() {
        activityRule.scenario.onActivity { activity ->
            activity.goNextStep()
        }
    }

    private fun clickTypeButton(buttonId: Int) {
        fillValidNames()

        //Skip phone number validation and go to passcode fragment
        goNextStep()

        //Login to an already existing account
        login("0651234145", "661192")

        //Skip passcode validation and go to type fragment
        goNextStep()

        val typeButton = onView(
                allOf(withId(buttonId),
                        isDisplayed()))
        typeButton.perform(click())

        Thread.sleep(1000)
        nextButton.perform(click())
    }

    private fun login(phoneNumber: String, codePwd: String) {
        val authenticationController = EntourageApplication.get().components.authenticationController
        OnboardingAPI.getInstance().login(phoneNumber, codePwd) { isOK, loginResponse, _ ->
            if (isOK) {
                loginResponse?.let {
                    authenticationController.saveUser(loginResponse.user)
                }
                authenticationController.saveUserPhoneAndCode(phoneNumber, codePwd)
                authenticationController.saveUserToursOnly(false)

                //set the tutorial as done
                val sharedPreferences = EntourageApplication.get().sharedPreferences
                (sharedPreferences.getStringSet(EntourageApplication.KEY_TUTORIAL_DONE, HashSet()) as HashSet<String>?)?.let {loggedNumbers ->
                    loggedNumbers.add(phoneNumber)
                    sharedPreferences.edit().putStringSet(EntourageApplication.KEY_TUTORIAL_DONE, loggedNumbers).apply()
                }
            }
        }
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
