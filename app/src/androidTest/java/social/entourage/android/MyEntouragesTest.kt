package social.entourage.android

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.isSelected
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.filters.LargeTest
import org.hamcrest.Matchers.allOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import social.entourage.android.onboarding.login.LoginActivity


@LargeTest
//TODO @RunWith(AndroidJUnit4::class)
class MyEntouragesTest : EntourageTestAfterLogin() {

    @get:Rule
    var activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
        //Thread.sleep(4000)
    }

    @After
    override fun tearDown() {
        //keep it just for the annotation
        super.tearDown()
    }

    override fun closeAutofill() {
        super.closeAutofill()
        activityRule.scenario.onActivity { activity ->
            closeAutofill(activity)
        }
    }

    @Test
    fun retrieveEntourages() {
        forceLogIn()

        //Try to retrieve feeds
        val bottomBarMessagesButton = onView(allOf(withId(R.id.navigation_donations), isDisplayed()))
        bottomBarMessagesButton.perform(click())
        val myEntouragesTab = onView(allOf(withText(R.string.actions_tab_mygroup), isDisplayed()))
        myEntouragesTab.perform(click())
        onView(allOf(withText(R.string.actions_tab_mygroup), isDisplayed(), isSelected()))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    @Test
    fun retrieveFeedsFailureNoInternetConnection() {
        forceLogIn()
        //Disable wifi and data
        enableWifiAndData(false)

        //Try to retrieve feeds
        val bottomBarMessagesButton = onView(allOf(withId(R.id.navigation_donations), isDisplayed()))
        bottomBarMessagesButton.perform(click())

        //Check that error is displayed
        onView(allOf(withText(R.string.network_error))).check(ViewAssertions.matches(isDisplayed()))
    }
}