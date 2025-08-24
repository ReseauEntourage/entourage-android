package social.entourage.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
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

    @After
    override fun tearDown() {
        //just keep it for the annotation
        super.tearDown()
    }

    /****************************** Views ******************************/

    private val firstNameEt = onView(
        allOf(
            withId(R.id.ui_onboard_names_et_firstname),
            isDisplayed()
        )
    )

    private val lastNameEt = onView(
        allOf(
            withId(R.id.ui_onboard_names_et_lastname),
            isDisplayed()
        )
    )

    private val askCodeTv = onView(
        allOf(
            withId(R.id.ui_onboard_code_tv_description),
            isDisplayed()
        )
    )

    private val phoneNumberEt = onView(
        allOf(
            withId(R.id.ui_onboard_phone_et_phone),
            isDisplayed()
        )
    )

    private val nextButton = onView(
        allOf(
            withId(R.id.ui_onboarding_bt_next),
            isDisplayed()
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
        firstNameEt.perform(typeText(""), closeSoftKeyboard())
        lastNameEt.perform(typeText(""), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askCodeTv.check(doesNotExist())
    }

    @Test
    fun emptyFirstNameTest() {
        firstNameEt.perform(typeText(""), closeSoftKeyboard())
        lastNameEt.perform(typeText("Dupont"), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askCodeTv.check(doesNotExist())
    }

    @Test
    fun emptyLastNameTest() {
        firstNameEt.perform(typeText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(typeText(""), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingNamesFragment is still displayed
        firstNameEt.check(matches(isDisplayed()))
        lastNameEt.check(matches(isDisplayed()))

        //Check that OnboardingPhoneFragment is not displayed
        askCodeTv.check(doesNotExist())
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

        phoneNumberEt.perform(typeText(""), closeSoftKeyboard())
        clickNextButton()

        //Check that OnboardingPhoneFragment is still displayed
        phoneNumberEt.check(matches(isDisplayed()))

        //Check that OnboardingPasscodeFragment is not displayed
        askCodeTv.check(doesNotExist())
    }

    @Test
    fun invalidPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(typeText("000000000"), closeSoftKeyboard())
        clickNextButton()

        //Check that error shows given message
        onView(withText(R.string.login_error_invalid_phone_format))
            .check(matches(isDisplayed()))
    }

    @Test
    fun invalidEmailTest() {
        fillValidNames()

        val emailEt = onView(
            allOf(withId(R.id.ui_onboard_email),
                isDisplayed()))

        phoneNumberEt.perform(typeText("0699999900"), closeSoftKeyboard())
        emailEt.perform(typeText("jean.dupont@jeandupont"), closeSoftKeyboard())

        clickNextButton()

        onView(
            allOf(withId(R.id.error_message_email),
                isDisplayed())).check (matches(isDisplayed()))
    }

    @Test
    fun alreadyUsedPhoneNumberTest() {
        fillValidNames()

        phoneNumberEt.perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        clickNextButton()

        //Check that dialog shows given message
        onView(withText(R.string.login_already_registered_go_back))
            .check(matches(isDisplayed()))
    }

    @Test
    fun phoneNumberFailureNoInternetConnectionTest() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to submit phone number
        fillValidNames()
        phoneNumberEt.perform(typeText("123456789"), closeSoftKeyboard())
        clickNextButton()

        //Check that error is displayed
        onView(allOf(
            withText(R.string.login_error_network),
            isDisplayed()))
            .check(matches(withText(R.string.login_error_network)))
    }

    /****************************** Utils ******************************/

    private fun clickNextButton() {
        nextButton.perform(click())
    }

    private fun fillValidNames() {
        firstNameEt.perform(typeText("Jean"), closeSoftKeyboard())
        lastNameEt.perform(typeText("Dupont"), closeSoftKeyboard())
        clickNextButton()
    }

}
