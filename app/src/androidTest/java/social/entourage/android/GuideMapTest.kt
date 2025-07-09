package social.entourage.android

import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Rule
import social.entourage.android.onboarding.login.LoginActivity

//TODO @LargeTest
//TODO @RunWith(AndroidJUnit4::class)
class GuideMapTest {

    @Rule
    @JvmField
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    private val login: String = "651234145"
    private val password: String = "108674"

    @Before
    fun setUp() {
        //Logout
        activityRule.scenario.onActivity { activity ->
            EntourageApplication.Companion[activity].authenticationController.logOutUser()
        }

        //Login
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_phone_et_phone))
            .perform(ViewActions.typeText(login), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_et_code))
            .perform(ViewActions.typeText(password), ViewActions.closeSoftKeyboard())
        Espresso.onView(ViewMatchers.withId(R.id.ui_login_button_signup))
            .perform(ViewActions.click())

        Thread.sleep(4000)
    }
    //TODO
    /*@Test
    fun retrievePOIsFailureNoInternetConnection() {
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to retrieve POIs
        val bottomBarGuideButton = onView(allOf(withId(R.id.bottom_bar_guide), withContentDescription(R.string.action_guide), isDisplayed()))
        bottomBarGuideButton.perform(click())

        val guideMapLayout = onView(allOf(withId(R.id.ui_layout_cell_1), isDisplayed()))
        guideMapLayout.perform(scrollTo(), click())

        //Check that error is displayed
        onView(allOf(withText(R.string.network_error))).check(matches(isDisplayed()))

        //Enable wifi and data
        enableWifiAndData(true)
    }*/

    private fun enableWifiAndData(enable: Boolean) {
        val parameter = if (enable) "enable" else "disable"
        InstrumentationRegistry.getInstrumentation().uiAutomation.apply {
            executeShellCommand("svc wifi $parameter")
            executeShellCommand("svc data $parameter")
        }
    }
}