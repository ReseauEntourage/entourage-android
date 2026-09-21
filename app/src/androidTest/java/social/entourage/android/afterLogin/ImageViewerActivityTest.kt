package social.entourage.android.afterLogin

import android.Manifest
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.tools.image_viewer.ImageViewerActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class ImageViewerActivityTest : EntourageTestAfterLogin() {
    private val postID = 25778
    private val groupID = 511

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Test
    fun testImageViewer() {
        checkUserIsLoggedIn()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ImageViewerActivity::class.java
        ).apply {
            putExtra(Const.POST_ID, postID)
            putExtra(Const.GROUP_ID, groupID)
        }
        ActivityScenario.launch<ImageViewerActivity>(intent).use {
            Espresso.onView(ViewMatchers.withId(R.id.photo_view))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            //TODO how to test Image Dialog ?
            Espresso.onView(ViewMatchers.withId(R.id.btn_close))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click())
        }
    }
}