package social.entourage.android

import android.content.Context
import android.view.autofill.AutofillManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.jakewharton.espresso.OkHttp3IdlingResource
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.authentication.login.LoginActivity

@RunWith(AndroidJUnit4::class)
@LargeTest
class LoginTest {
    @get:Rule
    var activityRule = ActivityTestRule(LoginActivity::class.java)
    private var resource: IdlingResource? = null
    private var afM: AutofillManager? = null

    @Before
    fun setUp() {
        checkNoUserIsLoggedIn()
        val context: Context = activityRule.activity
        val client = EntourageApplication.get(context).entourageComponent.okHttpClient
        resource = OkHttp3IdlingResource.create("OkHttp", client)
        IdlingRegistry.getInstance().register(resource)
        afM = context.getSystemService(AutofillManager::class.java)
        afM?.disableAutofillServices()
    }

    private fun checkTCDisplay() {
        //PFP we have a TC validation screen
        if (EntourageApplication.isPfpApp()) {
            Espresso.onView(ViewMatchers.withId(R.id.register_welcome_start_button)).perform(ViewActions.click())
        }
    }

    private fun checkNoUserIsLoggedIn() {
        EntourageApplication.get(activityRule.activity).entourageComponent.authenticationController?.logOutUser()
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(resource)
        checkNoUserIsLoggedIn()
    }

    @Test
    fun loginOK() {
        checkNoUserIsLoggedIn()
        Espresso.onView(ViewMatchers.withId(R.id.login_button_login)).perform(ViewActions.click())
        checkTCDisplay()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_phone)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_code)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_button_signin)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.doesNotExist())
        //checkNoUserIsLoggedIn();
    }

    @Test
    fun loginOKwithoutCountryCode() {
        Espresso.onView(ViewMatchers.withId(R.id.login_button_login)).perform(ViewActions.click())
        checkTCDisplay()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_phone)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN.replaceFirst("\\+33".toRegex(), "0")), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_code)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_button_signin)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.doesNotExist())
        //checkNoUserIsLoggedIn();
    }

    private fun closeAutofill() {
        if (afM == null) {
            afM = activityRule.activity.getSystemService(AutofillManager::class.java)
        }
        afM?.cancel()
        afM?.commit()
    }

    @Test
    fun loginFailureWrongPassword() {
        checkNoUserIsLoggedIn()
        Espresso.onView(ViewMatchers.withId(R.id.login_button_login)).perform(ViewActions.click())
        checkTCDisplay()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_phone)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_code)).perform(ViewActions.typeText("999999"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_button_signin)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(R.string.login_retry_label)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.login_back_button)).perform(ViewActions.click())
    }

    @Test
    fun loginFailureWrongPhoneNumberFormat() {
        Espresso.onView(ViewMatchers.withId(R.id.login_button_login)).perform(ViewActions.click())
        checkTCDisplay()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_phone)).perform(ViewActions.typeText("012345678"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_edit_code)).perform(ViewActions.typeText("000000"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.login_button_signin)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(R.string.login_retry_label)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withId(R.id.login_back_button)).perform(ViewActions.click())
    }
}