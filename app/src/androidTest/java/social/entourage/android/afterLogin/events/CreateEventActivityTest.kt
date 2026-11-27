package social.entourage.android.afterLogin.events

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import social.entourage.android.R
import social.entourage.android.afterLogin.EntourageTestAfterLogin
import social.entourage.android.e2e.E2EScreenshot
import social.entourage.android.events.create.CreateEventActivity

@RunWith(AndroidJUnit4::class)
class CreateEventActivityTest : EntourageTestAfterLogin() {

    private val screenshot = E2EScreenshot("create_event")

    private val activityRule = ActivityScenarioRule(CreateEventActivity::class.java)

    // Le parcours de création d'événement est en Compose (CreateEventScreen) : son titre n'est pas un TextView.
    private val composeRule = createEmptyComposeRule()

    @get:Rule
    val chain: RuleChain = RuleChain.outerRule(composeRule).around(activityRule)

    @Before
    fun setUp() {
        activityRule.scenario.onActivity { activity ->
            super.setUp(activity)
        }
    }

    @Test
    fun test_activity_launches() {
        val title = ApplicationProvider.getApplicationContext<Context>().getString(R.string.new_event)
        composeRule.waitUntil(TIMEOUT_MS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText(title)[0].assertIsDisplayed()
        screenshot.shoot("creation_evenement_lancement")
        //TODO all steps
    }

    private companion object {
        const val TIMEOUT_MS = 15_000L
    }
}
