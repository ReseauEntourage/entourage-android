package social.entourage.android

import android.view.WindowManager
import android.view.autofill.AutofillManager
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.Root
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
//import com.jakewharton.espresso.OkHttp3IdlingResource
import org.hamcrest.Description
import org.hamcrest.Matchers.allOf
import org.hamcrest.TypeSafeMatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.login.LoginActivity
import timber.log.Timber

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {
    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)
    private var resource: IdlingResource? = null
    private var afM: AutofillManager? = null

    @Before
    fun setUp() {
        checkNoUserIsLoggedIn()
        activityRule.scenario.onActivity { activity ->
            val client = EntourageApplication[activity].apiModule.okHttpClient
            //TODO resource = OkHttp3IdlingResource.create("OkHttp", client)
            //TODO IdlingRegistry.getInstance().register(resource)
            afM = activity.getSystemService(AutofillManager::class.java)
            afM?.disableAutofillServices()
        }
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
        IdlingRegistry.getInstance().unregister(resource)
        checkNoUserIsLoggedIn()
    }

    @Test
    fun loginOK() {
        Intents.init()
        checkNoUserIsLoggedIn()
        checkFirstConnectionScreen()

        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())
        onView(withText(R.string.login_error_title)).check(doesNotExist())

        checkLoginSuccessful()
        Intents.release()
    }

    @Test
    fun loginOKWithoutCountryCode() {
        Intents.init()
        checkFirstConnectionScreen()

        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN.replaceFirst("\\+33".toRegex(), "0")), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())
        onView(withText(R.string.login_error_title)).check(doesNotExist())

        checkLoginSuccessful()
        Intents.release()
    }

    @Test
    fun loginFailureWrongPassword() {
        checkNoUserIsLoggedIn()
        checkFirstConnectionScreen()

        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText("999999"), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        checkLoginFailure()
    }

    @Test
    fun loginFailureWrongPhoneNumberFormat() {
        checkFirstConnectionScreen()

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

        //Try to login
        checkFirstConnectionScreen()

        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(BuildConfig.TEST_ACCOUNT_LOGIN), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_et_code)).perform(typeText(BuildConfig.TEST_ACCOUNT_PWD), closeSoftKeyboard())
        closeAutofill()
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        //Check that error is displayed
        onView(withText(R.string.login_error_network)).check(matches(isDisplayed()))
        onView(withText(R.string.login_retry_label)).perform(click())

        //Enable wifi and data
        enableWifiAndData(true)
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
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
    }

    private fun checkLoginFailure() {
        onView(withText(R.string.login_error_title)).check(matches(isDisplayed()))
        onView(withText(R.string.login_retry_label)).perform(click())
    }

    private fun closeAutofill() {
        if (afM == null) {
            activityRule.scenario.onActivity { activity ->
                afM = activity.getSystemService(AutofillManager::class.java)
            }
        }
        afM?.cancel()
        afM?.commit()
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

    class ToastMatcher : TypeSafeMatcher<Root>() {
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
    }
}