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
import social.entourage.android.BuildConfig
import social.entourage.android.R
import social.entourage.android.onboarding.onboard.OnboardingStartActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignUpTest : EntourageTestBeforeLogin() {

    @get:Rule
    var activityRule = ActivityScenarioRule(OnboardingStartActivity::class.java)

    @Before
    fun setup() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    /****************************** Views ******************************/

    private val firstNameEt = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_onboard_names_et_firstname),
            ViewMatchers.isDisplayed()
        )
    )

    private val lastNameEt = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_onboard_names_et_lastname),
            ViewMatchers.isDisplayed()
        )
    )

    private val askCodeTv = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_onboard_code_tv_description),
            ViewMatchers.isDisplayed()
        )
    )

    private val phoneNumberEt = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_onboard_phone_et_phone),
            ViewMatchers.isDisplayed()
        )
    )

    private val nextButton = Espresso.onView(
        Matchers.allOf(
            ViewMatchers.withId(R.id.ui_onboarding_bt_next),
            ViewMatchers.isDisplayed()
        )
    )

    /****************************** OnboardingNamesFragment ******************************/
    //TODO test for new account @Test
    /*fun validFirstNameAndLastNameTest() {
        fillValidNames()

        //Check that OnboardingPhoneFragment is displayed
        askCodeTv.check(matches(withText(R.string.onboard_phone_sub)))

        //Check that OnboardingNamesFragment is not displayed
        firstNameEt.check(doesNotExist())
        lastNameEt.check(doesNotExist())
    }*/

    @Test
    fun emptyFirstNameAndLastNameTest() {
        firstNameEt.perform(ViewActions.typeText(""), ViewActions.closeSoftKeyboard())
        lastNameEt.perform(ViewActions.typeText(""), ViewActions.closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        lastNameEt.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askCodeTv.check(ViewAssertions.doesNotExist())
    }

    @Test
    fun emptyFirstNameTest() {
        firstNameEt.perform(ViewActions.typeText(""), ViewActions.closeSoftKeyboard())
        lastNameEt.perform(ViewActions.typeText("Dupont"), ViewActions.closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        lastNameEt.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askCodeTv.check(ViewAssertions.doesNotExist())
    }

    @Test
    fun emptyLastNameTest() {
        firstNameEt.perform(ViewActions.typeText("Jean"), ViewActions.closeSoftKeyboard())
        lastNameEt.perform(ViewActions.typeText(""), ViewActions.closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        lastNameEt.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askCodeTv.check(ViewAssertions.doesNotExist())
    }

    /****************************** OnboardingPhoneFragment ******************************/

    //TODO For now there is no simple way to send a phone number and remove it to be able running the test again
    // It would need for instance an API call that remove the sent number so that it is not already used for the next test call
//    @Test
//    fun validPhoneNumberTest() {
//        fillValidNames()
//
//        phoneNumberEt.perform(typeText(""), closeSoftKeyboard())
//        clickNextButton()
//
//        //Check that toast shows given message
//        onView(withText(R.string.login_smscode_sent)).inRoot(ToastMatcher()).check(matches(isDisplayed()))
//    }

    @Test
    fun emptyPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(ViewActions.typeText(""), ViewActions.closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingPhoneFragment is still displayed
        phoneNumberEt.check(ViewAssertions.matches(ViewMatchers.isDisplayed()))

        //Check that OnboardingPasscodeFragment is not displayed
        askCodeTv.check(ViewAssertions.doesNotExist())
    }

    @Test
    fun invalidPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(ViewActions.typeText("000000000"), ViewActions.closeSoftKeyboard())
        clickNextButton()

        //Check that error shows given message
        Espresso.onView(ViewMatchers.withText(R.string.login_error_invalid_phone_format))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO is it really blocking ? @Test
    fun invalidEmailTest() {
        fillValidNames()

        val emailEt = Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.ui_onboard_email),
                ViewMatchers.isDisplayed()
            )
        )

        phoneNumberEt.perform(
            ViewActions.typeText(unused_phone_number),
            ViewActions.closeSoftKeyboard()
        )
        emailEt.perform(
            ViewActions.typeText("jean.dupont@jeandupont"),
            ViewActions.closeSoftKeyboard()
        )

        clickNextButton()

        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.error_message_email),
                ViewMatchers.isDisplayed()
            )
        ).check (ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun alreadyUsedPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN),
            ViewActions.closeSoftKeyboard()
        )
        clickNextButton()

        //Check that dialog shows given message
        Espresso.onView(ViewMatchers.withText(R.string.login_already_registered_go_back))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    //TODO @Test
    fun phoneNumberFailureNoInternetConnectionTest() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to submit phone number
        fillValidNames()
        phoneNumberEt.perform(ViewActions.typeText("123456789"), ViewActions.closeSoftKeyboard())
        clickNextButton()

        //Check that error is displayed
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.login_error_network),
                ViewMatchers.isDisplayed()
            )
        )
            .check(ViewAssertions.matches(ViewMatchers.withText(R.string.login_error_network)))
    }

    /****************************** Utils ******************************/

    private fun clickNextButton() {
        nextButton.perform(ViewActions.click())
    }

    private fun fillValidNames() {
        firstNameEt.perform(ViewActions.typeText("Jean"), ViewActions.closeSoftKeyboard())
        lastNameEt.perform(ViewActions.typeText("Dupont"), ViewActions.closeSoftKeyboard())
        clickNextButton()
    }

}