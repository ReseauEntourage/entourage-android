package social.entourage.android.afterLogin

import android.Manifest
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso
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
import social.entourage.android.tools.image_viewer.ImageListActivity
import social.entourage.android.tools.utils.Const

@RunWith(AndroidJUnit4::class)
class ImageListActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("image_list")

    @get:Rule
    val permissionRule = GrantPermissionRule.grant(
        Manifest.permission.POST_NOTIFICATIONS
    )

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
    fun testSimpleIntent1() {
        internalCall(-1)
    }

    @Test
    fun testSimpleIntent2() {
        val conversationID = fetchConversationIdFromApi()
        internalCall(conversationID)
    }

    fun internalCall(convId: Int) {
        checkUserIsLoggedIn()
        val intent = Intent(
            ApplicationProvider.getApplicationContext(),
            ImageListActivity::class.java
        ).apply {
            putExtra(Const.CONVERSATION_ID, convId)
        }
        ActivityScenario.launch<ImageListActivity>(intent).use {
            Espresso.onView(ViewMatchers.withText(R.string.image_option_title))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()))
            screenshot.shoot("image_list_affichage")
        }
    }
}