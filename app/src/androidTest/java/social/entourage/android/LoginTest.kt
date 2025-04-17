package social.entourage.android

import android.view.autofill.AutofillManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.clearText
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.espresso.OkHttp3IdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.login.LoginActivity
import social.entourage.android.onboarding.login.LoginChangePhoneActivity
import social.entourage.android.onboarding.pre_onboarding.PreOnboardingChoiceActivity
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)
    private var resource: IdlingResource? = null
    private var afM: AutofillManager? = null

    @Before
    fun setUp() {
        checkNoUserIsLoggedIn()
        activityRule.scenario.onActivity { activity ->
            val client = EntourageApplication[activity].apiModule.okHttpClient
            resource = OkHttp3IdlingResource.create("OkHttp", client)
            IdlingRegistry.getInstance().register(resource)
            afM = activity.getSystemService(AutofillManager::class.java)
            afM?.disableAutofillServices()
        }
        Intents.init()
        checkFirstConnectionScreen()
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
        Intents.release()

        IdlingRegistry.getInstance().unregister(resource)
        checkNoUserIsLoggedIn()
        enableWifiAndData(true)
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
    fun resendCodeButton_withPhoneNumber_showsError() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())

        onView(withId(R.id.ui_login_button_resend_code)).perform(click())

        onView(withText(R.string.login_button_resend_code_action)).check(matches(isDisplayed()))
    }

    @Test
    fun resendCodeButton_withEmptyPhoneNumber_showsError() {
        onView(withId(R.id.ui_login_phone_et_phone)).perform(clearText())

        onView(withId(R.id.ui_login_button_resend_code)).perform(click())

        checkLoginFailure(R.string.attention_pop_title, R.string.close)
    }

    @Test
    fun changePhoneNumberButton_opensLoginChangePhoneActivity() {
        onView(withId(R.id.ui_login_button_change_phone)).perform(click())
        intended(hasComponent(LoginChangePhoneActivity::class.java.name))
    }

    @Test
    fun testGoBack() {
        onView(withId(R.id.icon_back)).perform(click())
        intended(hasComponent(PreOnboardingChoiceActivity::class.java.name))
    }

    @Test
    fun termsAndConditions_DisplayedAndClickable() {
        onView(withId(R.id.tv_condition_generales))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }

    /*@Test
    fun resendCodeFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to resend code
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(allOf(withId(R.id.ui_login_button_resend_code), isDisplayed())).perform(click())

        //Check that error is displayed
        //onView(withText(R.string.login_error_network)).inRoot(SignUpTest.ToastMatcher()).check(matches(isDisplayed()))

        //Enable wifi and data
        enableWifiAndData(true)
    }*/

    private fun checkLoginSuccessful() {
        intended(hasComponent(MainActivity::class.java.name))
    }

    private fun checkLoginFailure(
        title_id: Int = R.string.login_error_title,
        action_id: Int = R.string.login_retry_label
    ) {
        onView(withText(title_id)).check(matches(isDisplayed()))
        onView(withText(action_id)).perform(click())
    }

    private fun closeAutofill() {
        if (afM == null) {
            activityRule.scenario.onActivity { activity ->
                afM = activity.getSystemService(AutofillManager::class.java)
            }
        }
        afM?.cancel()
        afM?.commit()

        // Attempt to dismiss password suggestion dialog using UI Automator (less reliable)
        /*try {
            val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
            val dismissButton = device.findObject(UiSelector().clickable(true).instance(0)) // Adjust selector as needed
            if (dismissButton.exists()) {
                dismissButton.click()
            }
        } catch (e: Exception) {
            Timber.d(e)
        }*/
    }

    private fun checkFirstConnectionScreen() {
        try {
            onView(withId(R.id.ui_button_login)).apply {
                check(matches(isDisplayed()))
                perform(click())
            }
        } catch (e: NoMatchingViewException) {
            Timber.w(e)
        }
    }

    private fun enableWifiAndData(enable: Boolean) {
        val parameter = if (enable) "enable" else "disable"
        InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
            executeShellCommand("svc wifi $parameter")
            executeShellCommand("svc data $parameter")
        }
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