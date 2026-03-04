package social.entourage.android.beforeLogin

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
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

    private fun closeAutofill() {
        activityRule.scenario.onActivity { activity ->
            closeAutofill(activity)
        }
    }

    @Test
    fun loginOK() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())
        checkLoginSuccessful()
    }

    @Test
    fun loginOKWithoutCountryCode() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(
                BuildConfig.TEST_ACCOUNT_LOGIN.replaceFirst(
                    "\\+33".toRegex(),
                    "0"
                )
            ), ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())
        checkLoginSuccessful()
    }

    @Test
    fun loginFailureWrongPassword() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code))
            .perform(ViewActions.typeText("999999"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())
        checkLoginFailure()
    }

    @Test
    fun loginFailureShortPassword() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code))
            .perform(ViewActions.typeText("9999"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())
        checkLoginFailure(R.string.attention_pop_title, R.string.close)
    }

    @Test
    fun loginFailureWrongPhoneNumberFormat() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone))
            .perform(ViewActions.typeText("012345678"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code))
            .perform(ViewActions.typeText("000000"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())

        checkLoginFailure()
    }

    @Test
    fun loginFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())

        //Check that error is displayed
        Espresso.onView(ViewMatchers.withText(R.string.login_error_network))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        //onView(withText(R.string.login_retry_label)).perform(click())
    }

    @Test
    fun resendCodeButtonWithoutClick() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN),
            ViewActions.closeSoftKeyboard()
        )

        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_resend_code))
            .perform(ViewActions.click())

        Espresso.onView(ViewMatchers.withText(R.string.login_button_resend_code_action))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }

    @Test
    fun resendCodeButtonWithEmptyPhoneNumber() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone))
            .perform(ViewActions.clearText())

        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_resend_code))
            .perform(ViewActions.click())

        checkLoginFailure(R.string.attention_pop_title, R.string.close)
    }

    @Test
    fun displayChangePhoneNumberScreen() {
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_change_phone))
            .perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(LoginChangePhoneActivity::class.java.name))
    }

    @Test
    fun clickGoBack() {
        Espresso.onView(ViewMatchers.withId(R.id.icon_back)).perform(ViewActions.click())
        Intents.intended(IntentMatchers.hasComponent(PreOnboardingChoiceActivity::class.java.name))
    }

    //TODO @Test
    fun displayTermsAndConditions() {
        Espresso.onView(ViewMatchers.withId(R.id.tv_condition_generales))
            .perform(ViewActions.click())
        //TODO intended(hasComponent(PreOnboardingChoiceActivity::class.java.name))
    }

    @Test
    fun resendCodeFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to resend code
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(
            ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN),
            ViewActions.closeSoftKeyboard()
        )
        closeAutofill()
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withId(R.id.ui_login_button_resend_code),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())
        Espresso.onView(
            Matchers.allOf(
                ViewMatchers.withText(R.string.login_button_resend_code_action),
                ViewMatchers.isDisplayed()
            )
        ).perform(ViewActions.click())

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