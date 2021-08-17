package social.entourage.android


import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.allOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.onboarding.login.LoginActivity

@LargeTest
@RunWith(AndroidJUnit4::class)
class MyEntouragesTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private val login: String = "651234145"
    private val password: String = "108674"

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            EntourageApplication[activity].components.authenticationController.logOutUser()
        }

        //Login
        onView(withId(R.id.ui_login_phone_et_phone)).perform(typeText(login), closeSoftKeyboard())
        onView(withId(R.id.ui_login_et_code)).perform(typeText(password), closeSoftKeyboard())
        onView(withId(R.id.ui_login_button_signup)).perform(click())

        Thread.sleep(4000)
    }

    @Test
    fun retrieveFeedsFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to retrieve feeds
        val bottomBarMessagesButton = onView(allOf(withId(R.id.bottom_bar_mymessages), withContentDescription(R.string.action_my_messages), isDisplayed()))
        bottomBarMessagesButton.perform(click())

        //Check that error is displayed
        onView(allOf(withText(R.string.network_error))).check(ViewAssertions.matches(isDisplayed()))

        //Enable wifi and data
        enableWifiAndData(true)
    }

    private fun enableWifiAndData(enable: Boolean) {
        val parameter = if (enable) "enable" else "disable"
        InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
            executeShellCommand("svc wifi $parameter")
            executeShellCommand("svc data $parameter")
        }
    }
}
