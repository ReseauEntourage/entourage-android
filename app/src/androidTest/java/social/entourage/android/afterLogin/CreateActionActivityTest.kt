package social.entourage.android.afterLogin

import android.Manifest
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
abstract class CreateActionActivityTest(isActionDemand: Boolean) : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("create_action")

    @get:Rule
    val composeTestRule = AndroidComposeTestRule(
        activityRule = ActivityScenarioRule<CreateActionActivity>(
            Intent(ApplicationProvider.getApplicationContext(), CreateActionActivity::class.java).apply {
                // Set to true for a Demand, false for a Contribution
                putExtra(Const.IS_ACTION_DEMAND, isActionDemand)
            }
        ),
        activityProvider = { rule ->
            var activity: CreateActionActivity? = null
            rule.scenario.onActivity { activity = it }
            activity ?: error("Activity was not created")
        }
    )

//    @get:Rule
//    var activityRule = ActivityScenarioRule<CreateActionActivity>(
//        Intent(ApplicationProvider.getApplicationContext(), CreateActionActivity::class.java).apply {
//            // Set to true for a Demand, false for a Contribution
//            putExtra(Const.IS_ACTION_DEMAND, isActionDemand)
//        }
//    )
    // This rule will grant the POST_NOTIFICATIONS permission before each test in this class
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    fun testCreateContribActivity(stringTitle: Int) {
        val context = composeTestRule.activity
        composeTestRule.onNodeWithText(context.getString(R.string.action_cgu_header_title))
            .assertIsDisplayed()
        composeTestRule.onNodeWithText(context.getString(R.string.action_cgu_accept_button))
            .performClick()
        composeTestRule.waitForIdle()
        screenshot.shoot("cgus_accepted")

        onView(withText(stringTitle)).check(matches(isDisplayed()))
        onView(withText(R.string.action_social_name))//.check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.next)).check(matches(isDisplayed()))
            .perform(click())
        Thread.sleep(1000)
        onView(Matchers.allOf(withId(R.id.action_name), isDisplayed())).perform(
            ViewActions.typeText("test"), ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.action_description)).perform(
            ViewActions.typeText("description "),
            ViewActions.closeSoftKeyboard()
        )
        screenshot.shoot("action_info_filled")

        onView(withText(R.string.next)).check(matches(isDisplayed()))
            .perform(click())
        Thread.sleep(1000)
        onView(Matchers.allOf(withId(R.id.location),isDisplayed())).perform(
            click()
        )
        Thread.sleep(1000)
        onView(Matchers.allOf(withId(R.id.ui_onboard_bt_location),isDisplayed())).perform(
            click()
        )
        Thread.sleep(5000)
        onView(withId(R.id.ui_onboard_place_tv_location)).check(matches(Matchers.not(withText(""))))
        onView(withText(R.string.validate)).check(matches(isDisplayed()))
            .perform(click())
        screenshot.shoot("location_selected")

        Thread.sleep(1000)
        onView(withText(R.string.next)).check(matches(isDisplayed()))
            .perform(click())
        Thread.sleep(1000)
        onView(withText(R.string.no)).check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.create)).check(matches(isDisplayed()))
            .perform(click())
        screenshot.shoot("action_created")

        Thread.sleep(1000)
        onView(withText(R.string.action_create_end_finish_bt)).check(matches(isDisplayed()))
            .perform(click())
        checkNoOnboarding()
        onView(withText(R.string.home_title)).check(matches(isDisplayed()))
        screenshot.shoot("home_screen_after_action")
    }
}

class CreateContribActivityTest : CreateActionActivityTest(false) {
    @Test
    fun testCreateContribActivity() {
        testCreateContribActivity(R.string.action_create_contrib_title)
    }
}

class CreateDemandActivityTest : CreateActionActivityTest(true) {
    @Test
    fun testCreateDemandActivity() {
        testCreateContribActivity(R.string.action_create_demand_title)
    }
}
