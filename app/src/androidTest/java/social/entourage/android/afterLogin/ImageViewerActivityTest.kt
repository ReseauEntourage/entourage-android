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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.tools.image_viewer.ImageViewerActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class ImageViewerActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("image_viewer")

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

    private fun fetchGroupAndPostIdFromApi(): Pair<Int, Int> {
        checkUserIsLoggedIn()

        return runBlocking(Dispatchers.IO) {
            val groupsResponse = EntourageApplication.get().apiModule.groupRequest
                .getAllGroups(page = 1, per = 10)
                .execute()

            val group = groupsResponse.body()?.allGroups?.firstOrNull()
                ?: throw IllegalStateException("Aucun groupe trouvé depuis l'API")
            val groupId = group.id ?: throw IllegalStateException("ID du groupe est nul")

            val postsResponse = EntourageApplication.get().apiModule.groupRequest
                .getGroupPosts(groupId, page = 1, per = 10)
                .execute()

            val post = postsResponse.body()?.posts?.firstOrNull()
                ?: throw IllegalStateException("Aucun post trouvé dans le groupe $groupId depuis l'API")
            val postId = post.id ?: throw IllegalStateException("ID du post est nul")

            Pair(groupId, postId)
        }
    }

    @Test
    fun testImageViewer() {
        val (groupID, postID) = fetchGroupAndPostIdFromApi()

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
            screenshot.shoot("affichage_photo")
            //TODO how to test Image Dialog ?
            Espresso.onView(ViewMatchers.withId(R.id.btn_close))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
                .perform(ViewActions.click())
        }
    }
}