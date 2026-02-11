package social.entourage.android.afterLogin

import android.Manifest
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.MainActivity
import social.entourage.android.R

@RunWith(AndroidJUnit4::class)
class MainActivityTest : EntourageTestAfterLogin() {

    @get:Rule
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    //This rule will grant the POST_NOTIFICATIONS permission before each test in this class
    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        //Manifest.permission.ACCESS_COARSE_LOCATION,
        //Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun testSimpleStartActivity() {
        checkNoOnboarding()
        myTakeSnapshot(this::class.java.simpleName)
        Espresso.onView(ViewMatchers.withText(R.string.home_title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }


}