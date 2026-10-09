package social.entourage.android.afterLogin

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
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.profile.EditProfileActivity

@RunWith(AndroidJUnit4::class)
class EditProfileActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("edit_profile")

    @get:Rule
    val activityRule = ActivityScenarioRule(EditProfileActivity::class.java)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun test_activity_launches() {
        Espresso.onView(ViewMatchers.withId(R.id.header))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        Espresso.onView(ViewMatchers.withId(R.id.image_profile))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
        screenshot.shoot("détails du profil")
    }
}