package social.entourage.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
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
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.onboarding.login.LoginChangePhoneActivity
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest : EntourageTestBeforeLogin() {
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
        checkFirstConnectionScreen()
    }

    @After
    override fun tearDown() {
        //keep it just for the annotation
        super.tearDown()
    }
    private fun closeAutofill() {
        activityRule.scenario.onActivity { activity ->
            closeAutofill(activity)
        }
    }

    @Test
    fun loginOK() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())
        checkLoginSuccessful()
    }

    @Test
    fun loginOKWithoutCountryCode() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN.replaceFirst("\\+33".toRegex(), "0")), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())
        checkLoginSuccessful()
    }

    @Test
    fun loginFailureWrongPassword() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText("999999"), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())
        checkLoginFailure()
    }

    @Test
    fun loginFailureShortPassword() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText("9999"), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())
        checkLoginFailure(R.string.attention_pop_title, R.string.close)
    }

    @Test
    fun loginFailureWrongPhoneNumberFormat() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText("012345678"), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText("000000"), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        checkLoginFailure()
    }

    @Test
    fun loginFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        //Check that error is displayed
        onView(withText(R.string.login_error_network)).check(matches(isDisplayed()))
        //onView(withText(R.string.login_retry_label)).perform(click())
    }

    @Test
    fun resendCodeButtonWithoutClick() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())

        onView(withId(R.id.ui_login_button_resend_code)).perform(click())

        onView(withText(R.string.login_button_resend_code_action)).check(matches(isDisplayed()))
    }

    @Test
    fun resendCodeButtonWithEmptyPhoneNumber() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(clearText())

        onView(withId(R.id.ui_login_button_resend_code)).perform(click())

        checkLoginFailure(R.string.attention_pop_title, R.string.close)
    }

    @Test
    fun displayChangePhoneNumberScreen() {
        onView(withId(R.id.ui_login_button_change_phone)).perform(click())
        intended(hasComponent(LoginChangePhoneActivity::class.java.name))
    }

    @Test
    fun clickGoBack() {
        onView(withId(R.id.icon_back)).perform(click())
        intended(hasComponent(PreOnboardingChoiceActivity::class.java.name))
    }

    @Test
    fun displayTermsAndConditions() {
        onView(withId(R.id.tv_condition_generales))
            .perform(click())
        //TODO intended(hasComponent(PreOnboardingChoiceActivity::class.java.name))
    }

    @Test
    fun resendCodeFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to resend code
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(allOf(withId(R.id.ui_login_button_resend_code), isDisplayed())).perform(click())
        onView(allOf(withText(R.string.login_button_resend_code_action), isDisplayed())).perform(click())

        //Check that error is displayed
        //TODO onView(withText(R.string.login_error_network)).inRoot(ToastMatcher()).check(matches(isDisplayed()))

    }

    /*class ToastMatcher : TypeSafeMatcher<Root>() {
        override fun matchesSafely(item: Root?): Boolean {
            item?.windowLayoutParams?.get()?.type?.let { type ->
                if (type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
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
    }*/
}