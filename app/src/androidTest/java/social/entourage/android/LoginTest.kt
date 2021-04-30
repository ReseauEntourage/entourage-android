package social.entourage.android

import android.view.autofill.AutofillManager
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.TypeTextAction
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.jakewharton.espresso.OkHttp3IdlingResource
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
        activityRule.scenario.onActivity { activity ->
            val client = EntourageApplication[activity].components.okHttpClient
            resource = OkHttp3IdlingResource.create("OkHttp", client)
            IdlingRegistry.getInstance().register(resource)
            afM = activity.getSystemService(AutofillManager::class.java)
            afM?.disableAutofillServices()
        }
    }

    private fun checkNoUserIsLoggedIn() {
        try {
            activityRule.scenario.onActivity { activity ->
                EntourageApplication[activity].components.authenticationController.logOutUser()
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
        checkFistConnexionScreen()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(TypeTextAction(BuildConfig.TEST_ACCOUNT_LOGIN), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.doesNotExist())
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()
    }

    @Test
    fun loginOKwithoutCountryCode() {
        Intents.init()
        checkFistConnexionScreen()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN.replaceFirst("\\+33".toRegex(), "0")), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_PWD), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.doesNotExist())
        Intents.intended(IntentMatchers.hasComponent(MainActivity::class.java.name))
        Intents.release()
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

    private fun checkFistConnexionScreen() {
        try {
            Espresso.onView(ViewMatchers.withId(R.id.ui_button_login)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            Espresso.onView(ViewMatchers.withId(R.id.ui_button_login)).perform(ViewActions.click())
        } catch (e: NoMatchingViewException) {
            Timber.w(e)
        }
    }

    @Test
    fun loginFailureWrongPassword() {
        checkNoUserIsLoggedIn()
        checkFistConnexionScreen()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(ViewActions.typeText(BuildConfig.TEST_ACCOUNT_LOGIN), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(ViewActions.typeText("999999"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(R.string.login_retry_label)).perform(ViewActions.click())
        //Espresso.onView(ViewMatchers.withId(R.id.login_back_button)).perform(ViewActions.click())
    }

    @Test
    fun loginFailureWrongPhoneNumberFormat() {
        checkFistConnexionScreen()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone)).perform(ViewActions.typeText("012345678"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code)).perform(ViewActions.typeText("000000"), ViewActions.closeSoftKeyboard())
        closeAutofill()
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup)).perform(ViewActions.click())
        Espresso.onView(ViewMatchers.withText(R.string.login_error_title)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withText(R.string.login_retry_label)).perform(ViewActions.click())
        //Espresso.onView(ViewMatchers.withId(R.id.login_back_button)).perform(ViewActions.click())
    }
}