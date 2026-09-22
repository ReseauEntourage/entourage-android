package social.entourage.android.afterLogin.discussions

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import social.entourage.android.EntourageApplication
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.discussions.DetailConversationActivity
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class DetailConversationActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("discussion_details")

    private fun fetchConversationIdFromApi(): Int {
        checkUserIsLoggedIn()

        return runBlocking(Dispatchers.IO) {
            val response = EntourageApplication.get().apiModule.discussionsRequest
                .getAllConversations(
                    page = 1,
                    per = 10,
                )
                .execute()

            response.body()?.allConversations?.firstOrNull()?.id
        } ?: throw IllegalStateException("Aucune conversation trouvée depuis l'API")
    }

    @Test
    fun test_detail_conversation_activity_launches() {
        val conversationID = fetchConversationIdFromApi()

        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            DetailConversationActivity::class.java,
        ).apply {
            putExtra(Const.ID, conversationID)
        }

        ActivityScenario.launch<DetailConversationActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                super.setUp(activity)
            }

            onView(withId(R.id.header)).check(matches(isDisplayed()))
            onView(withId(R.id.comment_message)).check(matches(isDisplayed()))
            screenshot.shoot("détails d'une conversation")
        }
    }
}
