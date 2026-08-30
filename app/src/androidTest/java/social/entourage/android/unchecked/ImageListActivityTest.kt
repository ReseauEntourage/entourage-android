package social.entourage.android.unchecked

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
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
        Espresso.onView(ViewMatchers.withText(R.string.image_option_title))
            .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
    }
}