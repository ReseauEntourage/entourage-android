package social.entourage.android.afterLogin.groups

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.groups.details.feed.GroupFeedActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class GroupFeedActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("group_details")

    @Test
    fun test_group_feed_activity_launches() {
        val groupID = fetchGroupIdFromApi()

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            GroupFeedActivity::class.java,
        ).apply {
            putExtra(Const.GROUP_ID, groupID)
        }

        ActivityScenario.launch<GroupFeedActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                super.setUp(activity)
            }

            Espresso.onView(ViewMatchers.withId(R.id.group_name))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            screenshot.shoot("details d'un groupe")
        }
    }
}