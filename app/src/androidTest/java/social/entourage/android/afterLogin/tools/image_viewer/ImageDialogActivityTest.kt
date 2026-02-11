package social.entourage.android.afterLogin.tools.image_viewer

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.tools.image_viewer.ImageViewerActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class ImageViewerActivityTest : EntourageTestAfterLogin() {
    private val postID = 25778
    private val groupID = 511

    @get:Rule
    val activityRule = ActivityScenarioRule<ImageViewerActivity>(
        Intent(
            ApplicationProvider.getApplicationContext(),
            ImageViewerActivity::class.java
        ).apply {
            // Set to true for a Demand, false for a Contribution
            putExtra(Const.POST_ID, postID)
            putExtra(Const.GROUP_ID, groupID)
        }
    )

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun testImageViewer() {
        onView(withId(R.id.photo_view)).check(matches(isDisplayed()))
        //TODO how to test Image Dialog ?
        onView(withId(R.id.btn_close)).check(matches(isDisplayed()))
            .perform(click())
    }
}
