package social.entourage.android.activities.actions.create

import android.Manifest
import android.content.Intent
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
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.actions.create.CreateActionActivity
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.tools.utils.Const

@LargeTest
@RunWith(AndroidJUnit4::class)
abstract class CreateActionActivityTest(isActionDemand: Boolean) : EntourageTestAfterLogin() {

    @get:Rule
    var activityRule = ActivityScenarioRule<CreateActionActivity>(
        Intent(ApplicationProvider.getApplicationContext(), CreateActionActivity::class.java).apply {
            // Set to true for a Demand, false for a Contribution
            putExtra(Const.IS_ACTION_DEMAND, isActionDemand)
        }
    )
    // This rule will grant the POST_NOTIFICATIONS permission before each test in this class
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_COARSE_LOCATION,
        //Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    fun testCreateContribActivity(stringTitle: Int) {
        onView(withText(R.string.ethics)).check(matches(isDisplayed()))
        onView(withId(R.id.accept))
            .perform(click())
        onView(withText(stringTitle)).check(matches(isDisplayed()))
        onView(withText(R.string.action_social_name))//.check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.next)).check(matches(isDisplayed()))
            .perform(click())
        onView(Matchers.allOf(withId(R.id.action_name), isDisplayed())).perform(
            ViewActions.typeText("test"), ViewActions.closeSoftKeyboard()
        )
        onView(withId(R.id.action_description)).perform(
            ViewActions.typeText("description "),
            ViewActions.closeSoftKeyboard()
        )
        onView(withText(R.string.next)).check(matches(isDisplayed()))
            .perform(click())
        onView(Matchers.allOf(withId(R.id.location),isDisplayed())).perform(
            click()
        )
        onView(Matchers.allOf(withId(R.id.ui_onboard_bt_location),isDisplayed())).perform(
            click()
        )
        onView(withText(R.string.validate)).check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.next)).check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.no)).check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.create)).check(matches(isDisplayed()))
            .perform(click())
        onView(withText(R.string.action_create_end_finish_bt)).check(matches(isDisplayed()))
            .perform(click())
        checkNoOnboarding()
        onView(withText(R.string.home_title)).check(matches(isDisplayed()))
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
