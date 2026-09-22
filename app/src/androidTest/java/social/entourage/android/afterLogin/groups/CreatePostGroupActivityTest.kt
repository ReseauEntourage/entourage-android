package social.entourage.android.afterLogin.groups

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.groups.details.feed.CreatePostGroupActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class CreatePostGroupActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("create_post_group")

    @Test
    fun test_activity_launches() {
        checkUserIsLoggedIn()
        val groupID = fetchGroupIdFromApi()

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            CreatePostGroupActivity::class.java
        ).apply {
            putExtra(Const.GROUP_ID, groupID)
        }
        ActivityScenario.launch<CreatePostGroupActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                super.setUp(activity)
            }
            Espresso.onView(
                Matchers.allOf(
                    ViewMatchers.withId(R.id.header_title),
                    ViewMatchers.withText(R.string.create_post)
                )
            ).check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            screenshot.shoot("creation_post_groupe_lancement")
            //TODO do all the steps
        }
    }
}