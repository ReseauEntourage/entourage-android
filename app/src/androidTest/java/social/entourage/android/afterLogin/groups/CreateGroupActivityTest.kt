package social.entourage.android.afterLogin.groups

import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.groups.create.CreateGroupActivity

@RunWith(AndroidJUnit4::class)
class CreateGroupActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("create_group")

    @get:Rule
    val activityRule = ActivityScenarioRule(CreateGroupActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun test_activity_launches() {
        Espresso.onView(ViewMatchers.withText(R.string.new_group))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        screenshot.shoot("creation_groupe_lancement")
        //TODO all steps
    }
}