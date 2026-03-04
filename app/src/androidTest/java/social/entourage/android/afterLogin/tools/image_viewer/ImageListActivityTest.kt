package social.entourage.android.afterLogin.tools.image_viewer

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.tools.image_viewer.ImageListActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class ImageListActivityTest : EntourageTestAfterLogin() {

    private val conversationID = 511

    @get:Rule
    val activityRule = ActivityScenarioRule<ImageListActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            ImageListActivity::class.java
        ).apply {
            // Set to true for a Demand, false for a Contribution
            putExtra(Const.CONVERSATION_ID, conversationID)
        }
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity {
            super.setUp(it)
        }
    }

    @Test
    fun testSimpleIntent() {
        onView(withText(R.string.image_option_title)).check(matches(isDisplayed()))
    }
}